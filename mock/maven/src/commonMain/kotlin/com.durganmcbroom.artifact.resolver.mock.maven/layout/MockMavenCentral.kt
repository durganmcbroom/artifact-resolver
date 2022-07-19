package com.durganmcbroom.artifact.resolver.mock.maven.layout

import com.durganmcbroom.artifact.resolver.mock.maven.HashType

internal const val MOCK_MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2"

public fun MockMavenCentral(hashType: HashType): DefaultMockMavenLayout = DefaultMockMavenLayout(url = MOCK_MAVEN_CENTRAL, hashType)

