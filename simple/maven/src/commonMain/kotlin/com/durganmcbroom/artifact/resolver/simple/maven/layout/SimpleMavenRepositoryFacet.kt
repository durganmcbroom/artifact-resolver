package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.jobs.Job
import com.durganmcbroom.resources.Resource

public interface SimpleMavenRepositoryFacet {
    public val type: String

    public fun resourceOf(groupId: String, artifactId: String, version: String, classifier: String?, type: String) : Job<Resource>
}