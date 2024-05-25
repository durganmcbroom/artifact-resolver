package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.durganmcbroom.jobs.*
import com.durganmcbroom.resources.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.FileNotFoundException

private val mapper = XmlMapper().registerModule(KotlinModule())
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)

public actual val SUPER_POM: PomData = parseData(
    object : Resource {
        override val location: String = SUPER_POM_PATH

        override fun open(): ResourceStream {
            return SimpleMaven::class.java.getResourceAsStream(SUPER_POM_PATH)?.asResourceStream()
                ?: throw ResourceNotFoundException(location, FileNotFoundException())
        }
    }
).call(EmptyJobContext).getOrThrow()

public actual fun parseData(resource: Resource): Job<PomData> = job(JobName("Parse pom data: '${resource.location}'")) {
    val stream = resource.openStream()

    mapper.readValue<PomData>(stream)
}.mapException { PomException.ParseException(resource.location, it) }
