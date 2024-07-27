package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.RepositoryFactory

public object SimpleMaven : RepositoryFactory<
        SimpleMavenRepositorySettings,
        SimpleMavenArtifactRepository
        > {
    private val cache = HashMap<SimpleMavenRepositorySettings, SimpleMavenArtifactRepository>()

    override fun createNew(settings: SimpleMavenRepositorySettings): SimpleMavenArtifactRepository =
        cache[settings] ?: SimpleMavenArtifactRepository(
            settings, this
        ).also { cache[settings] = it }
}