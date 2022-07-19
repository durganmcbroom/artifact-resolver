package com.durganmcbroom.artifact.resolver

import java.io.BufferedInputStream
import java.io.InputStream

public fun CheckedResource.open(): InputStream = object : InputStream() {
    private val iterator = get().iterator()

    override fun read(): Int = if (iterator.hasNext()) iterator.next().toInt() else -1
}

public fun InputStream.asSequence(): Sequence<Byte> = BufferedInputStream(this).iterator().asSequence()

public fun InputStream.asResource(): CheckedResource = object : CheckedResource {
    override fun get(): Sequence<Byte> = asSequence()
}