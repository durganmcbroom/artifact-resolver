package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.RepositoryStubResolutionException
import com.durganmcbroom.artifact.resolver.RepositoryStubResolver
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimplePluginProvider
import com.durganmcbroom.jobs.result
import com.durganmcbroom.resources.ResourceAlgorithm

public class SimpleMavenRepositoryStubResolver(
    private val preferredHash: ResourceAlgorithm, private val pluginProvider: SimplePluginProvider
) : RepositoryStubResolver<SimpleMavenRepositoryStub, SimpleMavenRepositorySettings> {
    override fun resolve(
        stub: SimpleMavenRepositoryStub
    ): Result<SimpleMavenRepositorySettings> = result {
        val repo = stub.unresolvedRepository

        val layout = when (repo.layout.lowercase()) {
            "default" -> SimpleMavenDefaultLayout(
                repo.url, preferredHash,
                repo.releases.enabled,
                repo.snapshots.enabled,
                stub.requireResourceVerification
            )

            else -> throw RepositoryStubResolutionException("Invalid repository layout: '${repo.layout}")
        }

        SimpleMavenRepositorySettings(
            layout, preferredHash, pluginProvider, stub.requireResourceVerification
        )
    }
}