package com.durganmcbroom.artifact.resolver.mock.maven

import com.durganmcbroom.artifact.resolver.RepositoryReference
import com.durganmcbroom.artifact.resolver.mock.maven.layout.MockMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.mock.maven.pom.PomRepository

public interface PomRepositoryReferencer {
    public fun reference(repo: PomRepository) : RepositoryReference<*>?

    // Should not be implemented by any non-pom-compliant repositories.
    public fun referenceLayout(repo: PomRepository) : MockMavenRepositoryLayout?
}