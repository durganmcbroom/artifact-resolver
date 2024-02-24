package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.jobs.*
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.openStream
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

private val mapper: ObjectMapper = XmlMapper().registerModule(KotlinModule())

internal actual suspend fun parseSnapshotMetadata(
    resource: Resource
): JobResult<Map<ArtifactAddress, String>, ResourceRetrievalException.MetadataParseFailed> =
    job(JobName("Parse snapshot metadata for snapshot artifact: '${resource.location}'")) {
        val tree = mapper.readValue<Map<String, Any>>(
            resource.openStream()
                .mapLeft {
                    ResourceRetrievalException.MetadataParseFailed(
                        resource.location,
                        "Failed to open stream for the given resource"
                    )
                }.bind()
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

        snapshots ?: raise(ResourceRetrievalException.MetadataParseFailed(
            resource.location,
            "Unknown type for node 'snapshotVersion'. Expected a map or list."
        ))
    }