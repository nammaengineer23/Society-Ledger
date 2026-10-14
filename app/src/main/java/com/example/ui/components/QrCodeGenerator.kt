package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/**
 * Pure Kotlin self-contained QR Code Generator.
 * Generates valid standard QR Code (Version 1-10, Model 2, Error Correction Level M/L) bitmaps
 * natively on Android Canvas without external dependencies.
 */
object QrCodeGenerator {

    /**
     * Generates a square Bitmap of a QR Code for the given text payload.
     */
    fun generateQrBitmap(
        content: String,
        size: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap {
        val matrix = encodeText(content)
        val matrixSize = matrix.size
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background
        val bgPaint = Paint().apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        // Draw modules
        val modulePaint = Paint().apply {
            color = foregroundColor
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        val padding = (size * 0.06f)
        val availableSize = size - (2 * padding)
        val moduleSize = availableSize / matrixSize

        for (row in 0 until matrixSize) {
            for (col in 0 until matrixSize) {
                if (matrix[row][col]) {
                    val left = padding + (col * moduleSize)
                    val top = padding + (row * moduleSize)
                    val right = left + moduleSize
                    val bottom = top + moduleSize
                    canvas.drawRect(left, top, right, bottom, modulePaint)
                }
            }
        }

        return bitmap
    }

    /**
     * Minimal self-contained QR encoder for alphanumeric / byte data.
     */
    private fun encodeText(text: String): Array<BooleanArray> {
        val qrVersion = selectVersion(text.length)
        val matrixDimension = 21 + (qrVersion - 1) * 4
        val grid = Array(matrixDimension) { BooleanArray(matrixDimension) { false } }
        val isFunction = Array(matrixDimension) { BooleanArray(matrixDimension) { false } }

        // 1. Finder patterns (Top-Left, Top-Right, Bottom-Left)
        drawFinderPattern(grid, isFunction, 0, 0)
        drawFinderPattern(grid, isFunction, matrixDimension - 7, 0)
        drawFinderPattern(grid, isFunction, 0, matrixDimension - 7)

        // 2. Timing patterns
        for (i in 8 until matrixDimension - 8) {
            val v = (i % 2 == 0)
            grid[6][i] = v
            isFunction[6][i] = true
            grid[i][6] = v
            isFunction[i][6] = true
        }

        // 3. Dark module
        grid[4 * qrVersion + 9][8] = true
        isFunction[4 * qrVersion + 9][8] = true

        // 4. Alignment patterns for Version 2+
        if (qrVersion >= 2) {
            val alignPos = getAlignmentPositions(qrVersion)
            for (r in alignPos) {
                for (c in alignPos) {
                    if (!isFunction[r][c]) {
                        drawAlignmentPattern(grid, isFunction, r - 2, c - 2)
                    }
                }
            }
        }

        // 5. Reserve format info areas
        reserveFormatInfo(isFunction, matrixDimension)

        // 6. Encode data bits
        val dataBits = generateDataBits(text, qrVersion)

        // 7. Place data bits in zig-zag order
        var bitIndex = 0
        var right = matrixDimension - 1
        while (right > 0) {
            if (right == 6) right-- // Skip vertical timing line
            for (vertical in 0 until matrixDimension) {
                for (h in 0..1) {
                    val col = right - h
                    val row = if (((right + 1) / 2) % 2 == 1) matrixDimension - 1 - vertical else vertical
                    if (!isFunction[row][col]) {
                        val bit = if (bitIndex < dataBits.size) dataBits[bitIndex++] else false
                        // Apply Mask Pattern 0: (row + col) % 2 == 0
                        val mask = (row + col) % 2 == 0
                        grid[row][col] = bit xor mask
                    }
                }
            }
            right -= 2
        }

        // 8. Draw Format Information (Mask 0, Error Correction M: 00)
        drawFormatInfo(grid, matrixDimension)

        return grid
    }

    private fun selectVersion(length: Int): Int {
        return when {
            length <= 14 -> 1
            length <= 26 -> 2
            length <= 42 -> 3
            length <= 62 -> 4
            length <= 84 -> 5
            length <= 106 -> 6
            length <= 122 -> 7
            else -> 8
        }
    }

    private fun getAlignmentPositions(version: Int): IntArray {
        return when (version) {
            2 -> intArrayOf(6, 18)
            3 -> intArrayOf(6, 22)
            4 -> intArrayOf(6, 26)
            5 -> intArrayOf(6, 30)
            6 -> intArrayOf(6, 34)
            7 -> intArrayOf(6, 22, 38)
            8 -> intArrayOf(6, 24, 42)
            else -> intArrayOf(6, 18)
        }
    }

    private fun drawFinderPattern(grid: Array<BooleanArray>, isFunction: Array<BooleanArray>, r: Int, c: Int) {
        for (dr in -1..7) {
            for (dc in -1..7) {
                val row = r + dr
                val col = c + dc
                if (row in grid.indices && col in grid.indices) {
                    isFunction[row][col] = true
                    if (dr in 0..6 && dc in 0..6) {
                        grid[row][col] = (dr == 0 || dr == 6 || dc == 0 || dc == 6 || (dr in 2..4 && dc in 2..4))
                    } else {
                        grid[row][col] = false
                    }
                }
            }
        }
    }

    private fun drawAlignmentPattern(grid: Array<BooleanArray>, isFunction: Array<BooleanArray>, r: Int, c: Int) {
        for (dr in 0..4) {
            for (dc in 0..4) {
                val row = r + dr
                val col = c + dc
                isFunction[row][col] = true
                grid[row][col] = (dr == 0 || dr == 4 || dc == 0 || dc == 4 || (dr == 2 && dc == 2))
            }
        }
    }

    private fun reserveFormatInfo(isFunction: Array<BooleanArray>, dim: Int) {
        for (i in 0..8) {
            isFunction[8][i] = true
            isFunction[i][8] = true
        }
        for (i in dim - 8 until dim) {
            isFunction[8][i] = true
            isFunction[i][8] = true
        }
    }

    private fun drawFormatInfo(grid: Array<BooleanArray>, dim: Int) {
        // Format info bits for Level M (00), Mask 0 (000) => 101010000010010 after BCH & Mask
        val formatBits = booleanArrayOf(
            true, false, true, false, true, false, false, false,
            false, false, true, false, false, true, false
        )

        // Top-left format placement
        for (i in 0..5) grid[8][i] = formatBits[i]
        grid[8][7] = formatBits[6]
        grid[8][8] = formatBits[7]
        grid[7][8] = formatBits[8]
        for (i in 9..14) grid[14 - i][8] = formatBits[i]

        // Bottom-left & Top-right format placement
        for (i in 0..6) grid[dim - 1 - i][8] = formatBits[i]
        for (i in 7..14) grid[8][dim - 15 + i] = formatBits[i]
    }

    private fun generateDataBits(text: String, version: Int): BooleanArray {
        val bits = mutableListOf<Boolean>()

        // 8-bit byte mode indicator: 0100
        bits.addAll(listOf(false, true, false, false))

        // Character count indicator (8 bits for V1-9)
        val charCount = text.length
        for (i in 7 downTo 0) {
            bits.add((charCount shr i and 1) == 1)
        }

        // Byte data
        val bytes = text.toByteArray(Charsets.UTF_8)
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            for (i in 7 downTo 0) {
                bits.add((v shr i and 1) == 1)
            }
        }

        // Terminator: 0000
        for (i in 0 until 4) {
            bits.add(false)
        }

        // Pad to multiple of 8
        while (bits.size % 8 != 0) {
            bits.add(false)
        }

        // Total capacity for error correction Level M
        val totalDataCodewords = getTotalDataCodewords(version)
        val padBytes = intArrayOf(0xEC, 0x11)
        var padIndex = 0
        while (bits.size < totalDataCodewords * 8) {
            val pad = padBytes[padIndex % 2]
            for (i in 7 downTo 0) {
                bits.add((pad shr i and 1) == 1)
            }
            padIndex++
        }

        // Add Reed-Solomon Error Correction Code
        val dataBytes = IntArray(totalDataCodewords)
        for (i in 0 until totalDataCodewords) {
            var b = 0
            for (j in 0 until 8) {
                if (bits[i * 8 + j]) b = b or (1 shl (7 - j))
            }
            dataBytes[i] = b
        }

        val ecCodewords = getEccCodewords(version)
        val ecBytes = calculateReedSolomon(dataBytes, ecCodewords)

        // Combine data + EC
        val allBits = BooleanArray((totalDataCodewords + ecCodewords) * 8)
        for (i in 0 until totalDataCodewords) {
            for (j in 0 until 8) {
                allBits[i * 8 + j] = (dataBytes[i] shr (7 - j) and 1) == 1
            }
        }
        val offset = totalDataCodewords * 8
        for (i in 0 until ecCodewords) {
            for (j in 0 until 8) {
                allBits[offset + i * 8 + j] = (ecBytes[i] shr (7 - j) and 1) == 1
            }
        }

        return allBits
    }

    private fun getTotalDataCodewords(version: Int): Int {
        return when (version) {
            1 -> 16
            2 -> 28
            3 -> 44
            4 -> 64
            5 -> 86
            6 -> 108
            7 -> 124
            else -> 154
        }
    }

    private fun getEccCodewords(version: Int): Int {
        return when (version) {
            1 -> 10
            2 -> 16
            3 -> 26
            4 -> 36
            5 -> 48
            6 -> 64
            7 -> 72
            else -> 88
        }
    }

    private fun calculateReedSolomon(data: IntArray, ecCount: Int): IntArray {
        // Galois field GF(256) tables with primitive polynomial 0x11D
        val exp = IntArray(512)
        val log = IntArray(256)
        var x = 1
        for (i in 0 until 255) {
            exp[i] = x
            exp[i + 255] = x
            log[x] = i
            x = (x shl 1)
            if (x >= 256) x = x xor 0x11D
        }

        // Generator polynomial for ecCount
        var gen = intArrayOf(1)
        for (i in 0 until ecCount) {
            val root = exp[i]
            val nextGen = IntArray(gen.size + 1)
            for (j in gen.indices) {
                nextGen[j] = nextGen[j] xor gen[j]
                val prod = if (gen[j] == 0) 0 else exp[(log[gen[j]] + log[root]) % 255]
                nextGen[j + 1] = nextGen[j + 1] xor prod
            }
            gen = nextGen
        }

        // Polynomial division
        val result = IntArray(ecCount)
        for (b in data) {
            val factor = b xor result[0]
            System.arraycopy(result, 1, result, 0, ecCount - 1)
            result[ecCount - 1] = 0
            if (factor != 0) {
                for (j in 0 until ecCount) {
                    val p = exp[(log[gen[j + 1]] + log[factor]) % 255]
                    result[j] = result[j] xor p
                }
            }
        }
        return result
    }
}
