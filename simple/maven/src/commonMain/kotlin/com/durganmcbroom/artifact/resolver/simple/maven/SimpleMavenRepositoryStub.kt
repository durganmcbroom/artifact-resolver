package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.RepositoryStub
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomRepository

public data class SimpleMavenRepositoryStub(
    val unresolvedRepository: PomRepository,
    val requireResourceVerification: Boolean
) : RepositoryStub {
    override val name: String = "${unresolvedRepository.name ?: "<unnamed>"}@${unresolvedRepository.url}"
}
