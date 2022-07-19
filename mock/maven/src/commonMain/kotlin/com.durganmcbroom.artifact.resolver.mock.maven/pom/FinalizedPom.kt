package com.durganmcbroom.artifact.resolver.mock.maven.pom

import com.durganmcbroom.artifact.resolver.RepositoryReference
import com.durganmcbroom.artifact.resolver.mock.maven.MavenDescriptor

public data class FinalizedPom(
    val desc: MavenDescriptor,
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