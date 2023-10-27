package common.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * encrypt the data with SHA-512 algorithm
 */
fun sha512(data: String, salt: String = ""): String? {
    var enc: String? = null
    try {
        val md: MessageDigest = MessageDigest.getInstance("SHA-512")
        md.update(salt.toByteArray())
        val bytes: ByteArray = md.digest(data.toByteArray())
        val sb = StringBuilder()
        for (i in bytes.indices) {
            sb.append(((bytes[i].toInt() and 0xff) + 0x100).toString(16).substring(1))
        }
        enc = sb.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return enc
}