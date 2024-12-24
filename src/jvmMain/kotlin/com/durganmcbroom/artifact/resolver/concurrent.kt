package com.durganmcbroom.artifact.resolver

import java.util.concurrent.ConcurrentHashMap

public actual fun <K, V> concurrentHashMap(): MutableMap<K, V> {
    return ConcurrentHashMap<K, V>()
}