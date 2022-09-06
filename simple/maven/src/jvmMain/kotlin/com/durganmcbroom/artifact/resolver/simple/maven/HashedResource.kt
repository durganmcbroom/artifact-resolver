package com.durganmcbroom.artifact.resolver.simple.maven

import arrow.core.Either
import arrow.core.continuations.either
import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.asSequence
import com.durganmcbroom.artifact.resolver.simple.maven.layout.ResourceRetrievalException
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.*

private const val NUM_ATTEMPTS = 3

public actual class HashedResource actual constructor(
    private val hashType: String,
    private val resourceUrl: String,
    private val check: ByteArray,
) : CheckedResource {
    override val location: String by ::resourceUrl

    override fun get(): Sequence<Byte> {
        assert(NUM_ATTEMPTS > 0)

        fun <T> doUntil(attempts: Int, supplier: (Int) -> T?): T? {
            for (i in 0 until attempts) {
                supplier(i)?.let { return@doUntil it }
            }
            return null
        }

        val digest = MessageDigest.getInstance(
            hashType
        )

        return (ByteArrayInputStream(doUntil(NUM_ATTEMPTS) { attempt ->
            digest.reset()

            val b = DigestInputStream(URL(resourceUrl).openStream(), digest).use(InputStream::readAllBytes)

            if (digest.digest().contentEquals(check)) b
            else null

        }
            ?: throw Exception("Failed to load resource: '$resourceUrl' because the checksums failed too many times!"))).asSequence()
    }
}

public actual fun hashedResourceOf(
    hashType: HashType,
    resourceUrl: String,
    checkUrl: String,
): Either<ResourceRetrievalException, HashedResource> = either.eager {
    val connection = URL(checkUrl).openConnection() as HttpURLConnection
    if (connection.responseCode != 200) shift<ResourceRetrievalException>(
        ResourceRetrievalException.ChecksumFileNotFound(
            checkUrl,
            hashType.name
        )
    )

    val type = when (hashType) {
        HashType.SHA1 -> "SHA1"
        HashType.SHA256 -> "SHA256"
        HashType.SHA512 -> "SHA512"
        HashType.MD5 -> "MD5"
    }

    val checkString = String(connection.inputStream.readAllBytes())
        .trim()
        .let { s -> s.subSequence(0 until s.indexOf(' ').let { (if (it == -1) s.length else it)  }) }

    val check = HexFormat.of().parseHex(checkString)

    HashedResource(type, resourceUrl, check)
}

