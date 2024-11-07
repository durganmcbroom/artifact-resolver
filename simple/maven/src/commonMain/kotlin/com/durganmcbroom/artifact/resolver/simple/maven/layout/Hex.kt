package com.durganmcbroom.artifact.resolver.simple.maven.layout

internal object Hex {
    public fun parseHex(hex: CharSequence): ByteArray {
        val length = hex.length
        val byteArray = ByteArray(length / 2)

        for (i in 0 until length step 2) {
            byteArray[i / 2] = ((hex[i].digitToInt(16) shl 4) + hex[i + 1].digitToInt(16)).toByte()
        }

        return byteArray
    }

    public fun formatHex(bytes: ByteArray): String {
        val hexString = StringBuilder(bytes.size * 2)

        for (byte in bytes) {
            val hex = byte.toInt() and 0xFF
            if (hex < 0x10) {
                hexString.append("0") // Append leading zero if necessary
            }
            hexString.append(hex.toString(16))
        }

        return hexString.toString()
    }
}