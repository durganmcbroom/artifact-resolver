package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.artifact.resolver.simple.maven.HashType

public const val MAVEN_CENTRAL_REPO: String = "https://repo.maven.apache.org/maven2"

public fun SimpleMavenCentral(hashType: HashType): SimpleMavenDefaultLayout = SimpleMavenDefaultLayout(
    MAVEN_CENTRAL_REPO,
    hashType,
    releasesEnabled = true,
    snapshotsEnabled = false
)

