package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.durganmcbroom.jobs.*
import com.durganmcbroom.resources.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import java.io.FileNotFoundException

private val mapper = XmlMapper().registerModule(KotlinModule())
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)

public actual val SUPER_POM: PomData = runBlocking {
    parseData(
        object : Resource {
            override val location: String = SUPER_POM_PATH

            override suspend fun open(): JobResult<ResourceStream, ResourceException> {
                return SimpleMaven::class.java.getResourceAsStream(SUPER_POM_PATH)?.asResourceStream()?.success()
                    ?: ResourceNotFoundException(location, FileNotFoundException()).failure()
            }
        }
    ).orThrow()
}

public actual suspend fun parseData(resource: Resource): JobResult<PomData, PomParsingException> = jobScope {
    val stream = resource.openStream().mapLeft {
        PomParsingException.ResourceException(resource, it)
    }.bind()

    jobCatching(JobName("Parse POM: '${resource.location}'")) {
        mapper.readValue<PomData>(stream)
    }.mapLeft { PomParsingException.InvalidPom(resource.location, it) }.bind()
}
