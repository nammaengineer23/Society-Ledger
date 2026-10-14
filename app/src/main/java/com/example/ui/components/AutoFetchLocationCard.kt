package com.example.ui.components

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary
import com.example.util.AddressDetails
import com.example.util.LocationHelper
import kotlinx.coroutines.launch

/**
 * A dedicated, accessible Material 3 component that allows one-tap auto-fetching
 * of the user's present location, automatically resolving and populating:
 * - City
 * - Society Address
 * - PIN Code
 * - State
 */
@Composable
fun AutoFetchLocationCard(
    modifier: Modifier = Modifier,
    onLocationFetched: (AddressDetails) -> Unit,
    testTagPrefix: String = "auto_fetch_location"
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isFetching by remember { mutableStateOf(false) }
    var lastFetchedResult by remember { mutableStateOf<AddressDetails?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun executeLocationFetch() {
        isFetching = true
        errorMessage = null
        coroutineScope.launch {
            val result = LocationHelper.fetchCurrentLocationAddress(context)
            isFetching = false
            result.onSuccess { details ->
                lastFetchedResult = details
                errorMessage = null
                onLocationFetched(details)
                Toast.makeText(
                    context,
                    "Location fetched: ${details.city.ifBlank { "Detected" }} (${details.pinCode.ifBlank { "PIN detected" }})",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { error ->
                errorMessage = error.message ?: "Could not detect location. Please enter manually."
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            executeLocationFetch()
        } else {
            errorMessage = "Location permission denied. Please allow location access or type address manually."
        }
    }

    fun startFetchFlow() {
        if (LocationHelper.hasLocationPermission(context)) {
            executeLocationFetch()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (lastFetchedResult != null) Color(0xFF2E7D32).copy(alpha = 0.5f) else SapphireNavy.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("${testTagPrefix}_container"),
        color = if (lastFetchedResult != null) Color(0xFFE8F5E9).copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (lastFetchedResult != null) Color(0xFF2E7D32) else SapphireNavy,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (lastFetchedResult != null) Icons.Default.CheckCircle else Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (lastFetchedResult != null) "Present Location Detected" else "Auto-Fetch Location",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = if (lastFetchedResult != null) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (lastFetchedResult != null) {
                                "City, address & PIN code applied automatically"
                            } else {
                                "Detect city, street address & pincode via GPS"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!isFetching) {
                    Button(
                        onClick = { startFetchFlow() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (lastFetchedResult != null) Color(0xFF2E7D32) else SapphireNavy
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("${testTagPrefix}_btn")
                    ) {
                        Icon(
                            imageVector = if (lastFetchedResult != null) Icons.Default.Refresh else Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lastFetchedResult != null) "Re-detect" else "Fetch GPS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = SapphireNavy
                        )
                        Text(
                            text = "Locating...",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = SapphireNavy,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Fetched result summary chip
            if (lastFetchedResult != null) {
                val details = lastFetchedResult!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📍 ${details.city.ifBlank { "City Detected" }} ${if (details.pinCode.isNotBlank()) "• PIN: ${details.pinCode}" else ""}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            if (details.state.isNotBlank()) {
                                Text(
                                    text = details.state,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (details.address.isNotBlank()) {
                            Text(
                                text = details.address,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // Error notice if any
            if (errorMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
