package com.durganmcbroom.artifact.resolver

public interface CheckedResource {
    public val location: String

    public fun get() : Sequence<Byte>
}