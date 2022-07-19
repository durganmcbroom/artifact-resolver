package com.durganmcbroom.artifact.resolver.mock.maven

import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.asSequence
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

public actual class LocalResource actual constructor(private val path: String) : CheckedResource {
    override fun get(): Sequence<Byte> = File(path).inputStream().asSequence()
}

public actual fun localResourceOrNull(path: String): CheckedResource? {
    if (!Files.exists(Path.of(path))) return null

    return LocalResource(path)
}