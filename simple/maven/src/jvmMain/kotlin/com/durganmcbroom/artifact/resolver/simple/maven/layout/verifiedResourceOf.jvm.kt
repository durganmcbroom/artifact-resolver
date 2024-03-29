package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.jobs.*
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceAlgorithm
import com.durganmcbroom.resources.VerifiedResource
import com.durganmcbroom.resources.toResource
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

internal actual fun verifiedResourceOf(
    location: String,
    checksumLocation: String,
    algorithm: ResourceAlgorithm,
    requireVerification: Boolean
): Job<Resource> = job(JobName("Verify resource: '$location'")) {
    val checksumConnection = URL(checksumLocation).openConnection() as? HttpURLConnection
        ?: throw ResourceRetrievalException.IllegalState("Upon opening connection expected an HttpURLConnection however found something else. Requested resource was: '$location' and the checksum file was '$checksumLocation'")

    val unverifiedResource = URL(location).toResource()

    if (checksumConnection.responseCode != 200) {
        return@job if (!requireVerification) unverifiedResource
        else throw ResourceRetrievalException.ChecksumFileNotFound(
            location,
            algorithm.name,
        )
    }

    val checkString = String(checksumConnection.inputStream.readAllBytes())
        .trim()
        .let { s -> s.subSequence(0 until s.indexOf(' ').let { (if (it == -1) s.length else it) }) }

    val check = HexFormat.of().parseHex(checkString)

    VerifiedResource(
        unverifiedResource,
        algorithm,
        check
    )
}