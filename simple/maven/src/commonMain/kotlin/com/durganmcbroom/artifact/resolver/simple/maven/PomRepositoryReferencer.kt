package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.RepositoryReference
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomRepository

public interface PomRepositoryReferencer {
    public fun reference(repo: PomRepository) : RepositoryReference<*>?

    // Should not be implemented by any non-pom-compliant repositories.
    public fun referenceLayout(repo: PomRepository) : SimpleMavenRepositoryLayout?
}