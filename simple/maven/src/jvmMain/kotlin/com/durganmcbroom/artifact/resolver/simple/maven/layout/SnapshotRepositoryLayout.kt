package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.artifact.resolver.simple.maven.pom.mapper
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceNotFoundException
import com.durganmcbroom.resources.toByteArray
import com.fasterxml.jackson.module.kotlin.readValue

internal actual suspend fun parseSnapshotMetadata(
    resource: Resource
): Map<ArtifactAddress, String> {
    try {
        val tree = mapper.readValue<Map<String, Any>>(
            resource.open().toByteArray()
        )

        val snapshotVersions =
            ((tree["versioning"] as? Map<String, Any>)?.get("snapshotVersions") as? Map<String, Any>)?.get("snapshotVersion")

        val snapshots = when (snapshotVersions) {
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                snapshotVersions as Map<String, String>

                mapOf(
                    ArtifactAddress(
                        snapshotVersions["classifier"],
                        snapshotVersions["extension"]!!
                    ) to snapshotVersions["value"]!!
                )
            }

            is List<*> -> snapshotVersions.filterIsInstance<Map<String, String>>()
                .associate { ArtifactAddress(it["classifier"], it["extension"]!!) to it["value"]!! }

            else -> null
        }

        return snapshots ?: throw ResourceRetrievalException.MetadataParseFailed(
            resource.location,
            "Unknown type for node 'snapshotVersion'. Expected a map or list."
        )
    } catch (e: ResourceNotFoundException) {
        throw e
    } catch (e: Exception) {
        throw ResourceRetrievalException.MetadataParseFailed(
            resource.location,
            "Error occurred while parsing metadata for resource: '${resource.location}'",
            e
        )
    }
}