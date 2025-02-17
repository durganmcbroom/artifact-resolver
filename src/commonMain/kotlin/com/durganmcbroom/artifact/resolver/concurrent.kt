package com.durganmcbroom.artifact.resolver

public expect fun <K, V> concurrentHashMap() : MutableMap<K, V>

public expect fun <T> concurrentList() : MutableList<T>