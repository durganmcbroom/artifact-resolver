package com.durganmcbroom.artifact.resolver

import java.io.BufferedInputStream
import java.io.InputStream

public fun CheckedResource.open(): InputStream = (get() as? JvmSequence)?.stream ?: throw IllegalStateException("Invalid resource! Can only read resources with JvmSequence's!")

public fun InputStream.asSequence(): Sequence<Byte> = JvmSequence(this)

public fun InputStream.asResource(): CheckedResource = object : CheckedResource {
    override fun get(): Sequence<Byte> = asSequence()
}