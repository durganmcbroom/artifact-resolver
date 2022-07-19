package com.durganmcbroom.artifact.resolver.mock.maven.pom

import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.asResource
import com.durganmcbroom.artifact.resolver.mock.maven.MockMaven
import com.durganmcbroom.artifact.resolver.open
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

private val mapper =
    XmlMapper().registerModule(KotlinModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

public actual val SUPER_POM: PomData = parseData(
    (MockMaven::class.java.getResourceAsStream(SUPER_POM_PATH)
        ?: throw IllegalStateException("Failed to read super pom resource!")).asResource()
)

public actual fun parseData(resource: CheckedResource): PomData {
   return mapper.readValue(resource.open())
}