package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.jobs.*
import com.durganmcbroom.resources.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

internal actual fun verifiedResourceOf(
    location: String,
    checksumLocation: String,
    algorithm: ResourceAlgorithm,
    requireVerification: Boolean
): Job<Resource> = job(JobName("Verify resource: '$location'")) {
    val unverifiedResource = URL(location).toResource()

    val checksum = result {  URL(checksumLocation).toResource() }

    if (checksum.isFailure) {
        return@job if (!requireVerification) unverifiedResource
        else throw ResourceRetrievalException.ChecksumFileNotFound(
            location,
            algorithm.name,
            checksum.exceptionOrNull()!!
        )
    }

    val checkString = String(checksum.merge().openStream().readAllBytes())
        .trim()
        .let { s -> s.subSequence(0 until s.indexOf(' ').let { (if (it == -1) s.length else it) }) }

    val check = HexFormat.of().parseHex(checkString)

    VerifiedResource(
        unverifiedResource,
        algorithm,
        check
    )
}