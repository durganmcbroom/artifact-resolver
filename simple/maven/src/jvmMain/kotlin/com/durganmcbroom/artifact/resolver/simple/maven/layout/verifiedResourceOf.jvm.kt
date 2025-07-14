package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.resources.*
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.url
import java.net.URL

internal actual suspend fun verifiedResourceOf(
    location: String,
    checksumLocation: String,
    algorithm: ResourceAlgorithm,
    verify: Boolean
): Resource {
    val unverifiedResource =
        RemoteResource(HttpRequestBuilder().apply {
            url(URL(location))
        })

    if (!verify) return unverifiedResource

    val checksum = runCatching {
        URL(checksumLocation).toResource()
    }

    if (checksum.isFailure) {
        return unverifiedResource
    }

    val checkString = String(checksum.getOrNull()!!.open().toByteArray())
        .trim()
        .let { s -> s.subSequence(0 until s.indexOf(' ').let { (if (it == -1) s.length else it) }) }

    val check = Hex.parseHex(checkString)

    return VerifiedResource(
        unverifiedResource,
        algorithm,
        check
    )
}