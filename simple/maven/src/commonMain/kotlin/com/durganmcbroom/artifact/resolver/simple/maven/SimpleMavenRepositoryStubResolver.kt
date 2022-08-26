package com.durganmcbroom.artifact.resolver.simple.maven

import arrow.core.Either
import arrow.core.continuations.either
import com.durganmcbroom.artifact.resolver.RepositoryStubResolutionException
import com.durganmcbroom.artifact.resolver.RepositoryStubResolver
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenLocalLayout
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimplePluginProvider

public class SimpleMavenRepositoryStubResolver(
    private val preferredHash: HashType, private val pluginProvider: SimplePluginProvider
) : RepositoryStubResolver<SimpleMavenRepositoryStub, SimpleMavenRepositorySettings> {
    override fun resolve(
        stub: SimpleMavenRepositoryStub
    ): Either<RepositoryStubResolutionException, SimpleMavenRepositorySettings> = either.eager {
        val repo = stub.unresolvedRepository

        val layout = when (repo.layout.lowercase()) {
            "default" -> SimpleMavenDefaultLayout(
                repo.url, preferredHash,
                repo.releases.enabled,
                repo.snapshots.enabled,
            )
            "local" -> SimpleMavenLocalLayout
            else -> shift(RepositoryStubResolutionException("Invalid repository layout: '${repo.layout}"))
        }

        SimpleMavenRepositorySettings(
            layout, preferredHash, pluginProvider
        )
    }
}