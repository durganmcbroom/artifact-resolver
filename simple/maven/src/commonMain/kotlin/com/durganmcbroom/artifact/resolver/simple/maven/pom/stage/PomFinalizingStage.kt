package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenDescriptor
import com.durganmcbroom.artifact.resolver.simple.maven.pom.FinalizedPom
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage
import com.durganmcbroom.artifact.resolver.simple.maven.pom.mavenToPomDependency

internal class PomFinalizingStage :
    PomProcessStage<DependencyManagementInjectionStage.DependencyManagementInjectionData, FinalizedPom> {
    override fun process(i: DependencyManagementInjectionStage.DependencyManagementInjectionData): FinalizedPom {
        val (data, repo) = i

        return FinalizedPom(
            SimpleMavenDescriptor(
                data.groupId!!,
                data.artifactId,
                data.version!!,
                null
            ),
            data.repositories.map { repo.settings.repositoryReferencer.reference(it) ?: throw IllegalStateException("Failed to get repository reference for pom repository: $it") },
            data.dependencies.map(::mavenToPomDependency),
            data.packaging
        )
    }
}

