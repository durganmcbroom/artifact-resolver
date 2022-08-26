package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.RepositoryStub
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomRepository

public data class SimpleMavenRepositoryStub(
    val unresolvedRepository: PomRepository,
) : RepositoryStub
