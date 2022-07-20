package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.asSequence
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.*

private const val NUM_ATTEMPTS = 3

public actual class HashedResource actual constructor(
    private val hashType: HashType,
    private val resourceURI: String,
    private val check: ByteArray
) : CheckedResource {
    override fun get(): Sequence<Byte> {
        assert(NUM_ATTEMPTS > 0)

        fun <T> doUntil(attempts: Int, supplier: (Int) -> T?): T? {
            for (i in 0 until attempts) {
                supplier(i)?.let { return@doUntil it }
            }
            return null
        }

        val digest = MessageDigest.getInstance(
            when (hashType) {
                HashType.SHA1 -> "SHA1"
                HashType.MD5 -> "MD5"
            }
        )

       return (ByteArrayInputStream(doUntil(NUM_ATTEMPTS) { attempt ->
            digest.reset()

            val b = DigestInputStream(URL(resourceURI).openStream(), digest).use(InputStream::readAllBytes)

            if (digest.digest().contentEquals(check)) b
            else null

        } ?: throw Exception("Failed to load resource: '$resourceURI' because the checksums failed too many times!"))).asSequence()
    }
}

public actual fun hashedResourceOrNull(
    hashType: HashType,
    resourceURI: String,
    checkURI: String
): HashedResource? {
    val connection = URL(checkURI).openConnection() as HttpURLConnection
    if (connection.responseCode != 200) return null

    val checkString = String(connection.inputStream.readAllBytes())
    val check = HexFormat.of().parseHex(checkString.trim().subSequence(0, 40))

    return HashedResource(hashType, resourceURI, check)
}