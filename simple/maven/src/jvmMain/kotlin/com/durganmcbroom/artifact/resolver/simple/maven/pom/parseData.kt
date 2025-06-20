package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.durganmcbroom.resources.*
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.FileNotFoundException

internal val mapper =
    XmlMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .build()
        .registerModule(KotlinModule.Builder().build())

public actual suspend fun getSuperPom(): PomData {
    return parseData(
        object : Resource {
            override val location: String = SUPER_POM_PATH

            override suspend fun open(): ResourceStream {
                return SimpleMaven::class.java.getResourceAsStream(SUPER_POM_PATH)?.asResourceStream()
                    ?: throw ResourceNotFoundException(location, FileNotFoundException())
            }
        }
    )
}

public actual suspend fun parseData(resource: Resource): PomData {
    val stream = resource.open().toByteArray()

    return try {
        mapper.readValue<PomData>(stream)
    } catch (e: DatabindException) {
        throw PomException.ParseException(resource.location, e)
    }
}
