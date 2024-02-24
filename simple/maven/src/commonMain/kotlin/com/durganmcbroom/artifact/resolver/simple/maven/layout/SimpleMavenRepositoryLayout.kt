package com.durganmcbroom.artifact.resolver.simple.maven.layout

import arrow.core.Either
import com.durganmcbroom.jobs.JobResult
import com.durganmcbroom.resources.Resource

public interface SimpleMavenRepositoryLayout {
    public val name: String

    public suspend fun resourceOf(groupId: String, artifactId: String, version: String, classifier: String?, type: String) : JobResult<Resource, ResourceRetrievalException>
}