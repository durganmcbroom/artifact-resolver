package com.durganmcbroom.artifact.resolver

public interface CheckedResource {
    public fun get() : Sequence<Byte>
}