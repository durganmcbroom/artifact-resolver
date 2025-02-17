package com.durganmcbroom.artifact.resolver

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

public actual fun <K, V> concurrentHashMap(): MutableMap<K, V> {
    return ConcurrentHashMap<K, V>()
}

public actual fun <T> concurrentList() : MutableList<T> {
    return CopyOnWriteArrayList<T>()
}
