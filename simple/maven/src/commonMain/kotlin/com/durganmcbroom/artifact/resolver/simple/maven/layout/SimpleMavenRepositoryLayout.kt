package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.jobs.Job
import com.durganmcbroom.resources.Resource

public interface SimpleMavenRepositoryLayout {
    public val name: String

    public fun resourceOf(groupId: String, artifactId: String, version: String, classifier: String?, type: String) : Job<Resource>
}