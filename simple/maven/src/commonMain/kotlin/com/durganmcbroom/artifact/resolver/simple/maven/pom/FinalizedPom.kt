package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.artifact.resolver.RepositoryReference
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenDescriptor

public data class FinalizedPom(
    val desc: SimpleMavenDescriptor,
    val repositories: List<RepositoryReference<*>>,
    val dependencies: List<PomDependency>,
    val packaging: String
) : PomProcessStage.StageData

public data class PomDependency(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val classifier: String?,
    val scope: String,
)

internal fun mavenToPomDependency(dep: MavenDependency) = PomDependency(dep.groupId, dep.artifactId, dep.version!!, dep.classifier, dep.scope ?: "compile")