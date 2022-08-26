package com.durganmcbroom.artifact.resolver.simple.maven

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.asSequence
import com.durganmcbroom.artifact.resolver.simple.maven.layout.ResourceRetrievalException
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

public actual class LocalResource actual constructor(private val path: String) : CheckedResource {
    override val location: String by ::path

    override fun get(): Sequence<Byte> = File(path).inputStream().asSequence()
}

public actual fun localResourceOf(path: String): Either<ResourceRetrievalException, CheckedResource> {
    if (!Files.exists(Path.of(path))) return ResourceRetrievalException.ResourceNotFound(path).left()

    return LocalResource(path).right()
}