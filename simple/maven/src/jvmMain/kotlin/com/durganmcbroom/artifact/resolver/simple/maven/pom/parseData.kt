package com.durganmcbroom.artifact.resolver.simple.maven.pom

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.getOrHandle
import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.asResource
import com.durganmcbroom.artifact.resolver.open
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

private val mapper = XmlMapper().registerModule(KotlinModule())
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)

public actual val SUPER_POM: PomData = parseData(
    (SimpleMaven::class.java.getResourceAsStream(SUPER_POM_PATH)
        ?: throw IllegalStateException("Failed to read super pom resource!")).asResource(SUPER_POM_PATH)
).getOrHandle { throw it.exception }

public actual fun parseData(resource: CheckedResource): Either<PomParsingException.InvalidPom, PomData> = Either.catch {
    mapper.readValue<PomData>(resource.open())
}.mapLeft { PomParsingException.InvalidPom(resource.location, it) }
