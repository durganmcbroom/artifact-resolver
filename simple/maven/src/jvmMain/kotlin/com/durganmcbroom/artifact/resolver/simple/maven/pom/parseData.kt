package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.asResource
import com.durganmcbroom.artifact.resolver.open
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

private val mapper =
    XmlMapper().registerModule(KotlinModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

public actual val SUPER_POM: PomData = parseData(
    (SimpleMaven::class.java.getResourceAsStream(SUPER_POM_PATH)
        ?: throw IllegalStateException("Failed to read super pom resource!")).asResource()
)

public actual fun parseData(resource: CheckedResource): PomData {
   return mapper.readValue(resource.open())
}