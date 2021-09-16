package com.llw.blekotlin.utils

import java.util.*
import kotlin.experimental.and

/**
 *
 * @description ByteUtils
 * @author llw
 * @date 2021/9/13 14:28
 */
object ByteUtils {

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    fun hexStringToBytes(hexString: String): ByteArray {
        val hexString = hexString.uppercase(Locale.getDefault())
        val length = hexString.length / 2
        val hexChars = hexString.toCharArray()
        val byteArrayResult = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
            byteArrayResult[i] = (charToByte(hexChars[pos]).toInt().shl(4) or charToByte(hexChars[pos + 1]).toInt()).toByte()
        }
        return byteArrayResult
    }

    /**
     * Convert byte[] to string
     */
    fun bytesToHexString(src: ByteArray): String {
        val stringBuilder = StringBuilder("")
        for (i in src.indices) {
            val v = (src[i].toInt() and 0xFF)
            val hv = Integer.toHexString(v)
            stringBuilder.append(if (hv.length < 2) 0 else hv)
        }
        return stringBuilder.toString()
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private fun charToByte(c: Char): Byte = "0123456789ABCDEF".indexOf(c).toByte()

}