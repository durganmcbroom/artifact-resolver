package com.durganmcbroom.artifact.resolver.mock.maven.pom.stage

import com.durganmcbroom.artifact.resolver.mock.maven.MavenDescriptor
import com.durganmcbroom.artifact.resolver.mock.maven.pom.FinalizedPom
import com.durganmcbroom.artifact.resolver.mock.maven.pom.PomProcessStage
import com.durganmcbroom.artifact.resolver.mock.maven.pom.mavenToPomDependency

internal class PomFinalizingStage :
    PomProcessStage<DependencyManagementInjectionStage.DependencyManagementInjectionData, FinalizedPom> {
    override fun process(i: DependencyManagementInjectionStage.DependencyManagementInjectionData): FinalizedPom {
        val (data, repo) = i

        return FinalizedPom(
            MavenDescriptor(
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

