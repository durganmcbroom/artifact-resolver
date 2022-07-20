package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.artifact.resolver.simple.maven.HashType

internal const val SIMPLE_MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2"


public fun SimpleMavenCentral(hashType: HashType): DefaultSimpleMavenLayout = DefaultSimpleMavenLayout(url = SIMPLE_MAVEN_CENTRAL, hashType)

