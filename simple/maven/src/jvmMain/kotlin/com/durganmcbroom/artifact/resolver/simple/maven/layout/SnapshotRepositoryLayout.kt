package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.open
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

private val mapper: ObjectMapper = XmlMapper().registerModule(KotlinModule())


internal actual fun parseSnapshotMetadata(resource: CheckedResource): Map<ArtifactAddress, String> {
    val tree = mapper.readValue<Map<String, Any>>(resource.open())

    val snapshotVersions =
        ((tree["versioning"] as? Map<String, Any>)?.get("snapshotVersions") as? Map<String, Any>)?.get("snapshotVersion")

    val extension = when (snapshotVersions) {
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

    return extension ?: throw IllegalArgumentException("Failed to parse snapshot values of pom: '$resource'")
}