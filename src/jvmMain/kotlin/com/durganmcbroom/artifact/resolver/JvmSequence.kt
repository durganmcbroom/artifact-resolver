package com.durganmcbroom.artifact.resolver

import java.io.BufferedInputStream
import java.io.InputStream

internal class JvmSequence(
    val stream: InputStream
) : Sequence<Byte> {
    private val iterator = BufferedInputStream(stream).iterator()

    override fun iterator(): Iterator<Byte> = iterator
}