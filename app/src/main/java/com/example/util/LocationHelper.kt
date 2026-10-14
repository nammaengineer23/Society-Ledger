package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume

data class AddressDetails(
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pinCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val formattedSummary: String = ""
) {
    val isPopulated: Boolean
        get() = city.isNotBlank() || address.isNotBlank() || pinCode.isNotBlank()
}

object LocationHelper {
    private const val TAG = "LocationHelper"

    /**
     * Checks if either FINE or COARSE location permission is granted.
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Main entry point to fetch the current location and automatically resolve
     * City, Address, State, and PIN Code details.
     */
    suspend fun fetchCurrentLocationAddress(context: Context): Result<AddressDetails> = withContext(Dispatchers.IO) {
        try {
            // 1. Check permissions
            val hasPerm = hasLocationPermission(context)
            var location: Location? = null

            if (hasPerm) {
                // Attempt to get location from Google Play Fused Location Provider
                location = getFusedLocation(context)

                // Fallback to Android native LocationManager if Fused returned null
                if (location == null) {
                    location = getLocationFromManager(context)
                }
            }

            // 2. If we have GPS coordinates, perform reverse geocoding
            if (location != null) {
                Log.d(TAG, "Obtained GPS fix: lat=${location.latitude}, lng=${location.longitude}")
                val resolved = reverseGeocode(context, location.latitude, location.longitude)
                if (resolved != null && resolved.isPopulated) {
                    return@withContext Result.success(resolved)
                }
            }

            // 3. Fallback: If no GPS location fix was available (e.g. in emulator or GPS disabled),
            // attempt network/IP location resolution to guarantee automatic population.
            Log.d(TAG, "GPS location unavailable or geocoding empty, trying network location fallback...")
            val ipDetails = fetchNetworkLocationFallback()
            if (ipDetails != null && ipDetails.isPopulated) {
                return@withContext Result.success(ipDetails)
            }

            // 4. If all failed, provide a user-friendly error
            if (!hasPerm) {
                Result.failure(Exception("Location permission not granted. Please allow location access to auto-fetch details."))
            } else {
                Result.failure(Exception("Could not detect present location. Please check GPS or enter details manually."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching location details", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves current or last known location using Google Play Services FusedLocationProviderClient.
     */
    private suspend fun getFusedLocation(context: Context): Location? {
        return try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            // Try high accuracy current location with 6s timeout
            val currentLocation = withTimeoutOrNull(6000L) {
                suspendCancellableCoroutine<Location?> { cont ->
                    try {
                        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                            .addOnSuccessListener { loc ->
                                if (cont.isActive) cont.resume(loc)
                            }
                            .addOnFailureListener {
                                if (cont.isActive) cont.resume(null)
                            }
                            .addOnCanceledListener {
                                if (cont.isActive) cont.resume(null)
                            }
                    } catch (sec: SecurityException) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }

            if (currentLocation != null) return currentLocation

            // Try last known location
            withTimeoutOrNull(3000L) {
                suspendCancellableCoroutine<Location?> { cont ->
                    try {
                        fusedClient.lastLocation
                            .addOnSuccessListener { loc ->
                                if (cont.isActive) cont.resume(loc)
                            }
                            .addOnFailureListener {
                                if (cont.isActive) cont.resume(null)
                            }
                    } catch (sec: SecurityException) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Fused location error: ${e.message}")
            null
        }
    }

    /**
     * Retrieves location from Android's LocationManager as a fallback.
     */
    private fun getLocationFromManager(context: Context): Location? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return null

            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            var bestLocation: Location? = null
            for (provider in providers) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        val loc = locationManager.getLastKnownLocation(provider)
                        if (loc != null) {
                            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                                bestLocation = loc
                            }
                        }
                    }
                } catch (sec: SecurityException) {
                    // Ignore security exception for specific provider
                }
            }
            bestLocation
        } catch (e: Exception) {
            Log.w(TAG, "LocationManager fallback error: ${e.message}")
            null
        }
    }

    /**
     * Reverse geocodes coordinates into AddressDetails using Android Geocoder
     * with an HTTP fallback for emulators and environments lacking Play Services geocoder backend.
     */
    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): AddressDetails? {
        // 1. Try Android Native Geocoder
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val result = suspendCancellableCoroutine<AddressDetails?> { cont ->
                        geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<android.location.Address>) {
                                val addr = addresses.firstOrNull()
                                if (addr != null && cont.isActive) {
                                    cont.resume(mapAndroidAddress(addr, latitude, longitude))
                                } else if (cont.isActive) {
                                    cont.resume(null)
                                }
                            }

                            override fun onError(errorMessage: String?) {
                                if (cont.isActive) cont.resume(null)
                            }
                        })
                    }
                    if (result != null && result.isPopulated) return result
                } else {
                    val list = geocoder.getFromLocation(latitude, longitude, 1)
                    val addr = list?.firstOrNull()
                    if (addr != null) {
                        val mapped = mapAndroidAddress(addr, latitude, longitude)
                        if (mapped.isPopulated) return mapped
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Native geocoder failed: ${e.message}, falling back to web geocoder")
        }

        // 2. HTTP Reverse Geocoding fallback (Nominatim / OpenStreetMap)
        return reverseGeocodeViaHttp(latitude, longitude)
    }

    /**
     * Maps an android.location.Address object into strongly typed AddressDetails.
     */
    private fun mapAndroidAddress(addr: android.location.Address, lat: Double, lng: Double): AddressDetails {
        val city = addr.locality
            ?: addr.subAdminArea
            ?: addr.adminArea
            ?: ""

        val state = addr.adminArea ?: ""
        val pinCode = addr.postalCode?.filter { it.isDigit() }?.take(6) ?: ""

        // Construct clean street/locality address
        val streetParts = listOfNotNull(
            addr.subThoroughfare?.takeIf { it.isNotBlank() },
            addr.thoroughfare?.takeIf { it.isNotBlank() },
            addr.subLocality?.takeIf { it.isNotBlank() }
        ).joinToString(", ")

        val fullAddressLine = addr.getAddressLine(0) ?: ""

        val finalAddress = when {
            streetParts.isNotBlank() -> streetParts
            fullAddressLine.isNotBlank() -> {
                // Remove trailing country or pincode if redundant
                fullAddressLine.split(",").take(3).joinToString(", ").trim()
            }
            else -> addr.featureName ?: ""
        }

        val summary = listOfNotNull(
            finalAddress.takeIf { it.isNotBlank() },
            city.takeIf { it.isNotBlank() },
            pinCode.takeIf { it.isNotBlank() }
        ).joinToString(", ")

        return AddressDetails(
            address = finalAddress,
            city = city,
            state = state,
            pinCode = pinCode,
            latitude = lat,
            longitude = lng,
            formattedSummary = summary
        )
    }

    /**
     * HTTP Reverse Geocoder (OpenStreetMap Nominatim) for emulators or devices without Google Geocoder service.
     */
    private fun reverseGeocodeViaHttp(latitude: Double, longitude: Double): AddressDetails? {
        return try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude&zoom=18&addressdetails=1")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "SocietyFinancialApp/1.0 (Android; LocationHelper)")
            conn.connectTimeout = 4000
            conn.readTimeout = 4000

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val addrObj = json.optJSONObject("address")

                val city = addrObj?.optString("city")?.takeIf { it.isNotBlank() }
                    ?: addrObj?.optString("town")?.takeIf { it.isNotBlank() }
                    ?: addrObj?.optString("suburb")?.takeIf { it.isNotBlank() }
                    ?: addrObj?.optString("village")?.takeIf { it.isNotBlank() }
                    ?: addrObj?.optString("county")?.takeIf { it.isNotBlank() }
                    ?: ""

                val state = addrObj?.optString("state", "") ?: ""
                val pinCode = addrObj?.optString("postcode", "")?.filter { it.isDigit() }?.take(6) ?: ""

                val streetParts = listOfNotNull(
                    addrObj?.optString("house_number")?.takeIf { it.isNotBlank() },
                    addrObj?.optString("road")?.takeIf { it.isNotBlank() },
                    addrObj?.optString("neighbourhood")?.takeIf { it.isNotBlank() },
                    addrObj?.optString("suburb")?.takeIf { it.isNotBlank() }
                ).joinToString(", ")

                val displayName = json.optString("display_name", "")
                val finalAddress = if (streetParts.isNotBlank()) {
                    streetParts
                } else if (displayName.isNotBlank()) {
                    displayName.split(",").take(3).joinToString(", ").trim()
                } else {
                    ""
                }

                val summary = listOfNotNull(
                    finalAddress.takeIf { it.isNotBlank() },
                    city.takeIf { it.isNotBlank() },
                    pinCode.takeIf { it.isNotBlank() }
                ).joinToString(", ")

                AddressDetails(
                    address = finalAddress,
                    city = city,
                    state = state,
                    pinCode = pinCode,
                    latitude = latitude,
                    longitude = longitude,
                    formattedSummary = summary
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "HTTP reverse geocode exception: ${e.message}")
            null
        }
    }

    /**
     * Fallback for emulator environments without GPS fix: Fetches coarse location
     * from network IP (e.g. city, state, postal code).
     */
    private fun fetchNetworkLocationFallback(): AddressDetails? {
        return try {
            val url = URL("https://ipapi.co/json/")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "SocietyFinancialApp/1.0 (Android; LocationHelper)")
            conn.connectTimeout = 4000
            conn.readTimeout = 4000

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val city = json.optString("city", "")
                val region = json.optString("region", "")
                val postal = json.optString("postal", "").filter { it.isDigit() }.take(6)
                val lat = json.optDouble("latitude", 0.0)
                val lon = json.optDouble("longitude", 0.0)

                if (city.isNotBlank()) {
                    AddressDetails(
                        address = "$city Central Area",
                        city = city,
                        state = region,
                        pinCode = postal,
                        latitude = lat,
                        longitude = lon,
                        formattedSummary = "$city, $region ${if (postal.isNotBlank()) "- $postal" else ""}".trim()
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network location fallback exception: ${e.message}")
            null
        }
    }
}
