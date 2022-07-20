package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositoryHandler
import com.durganmcbroom.artifact.resolver.simple.maven.layout.DefaultSimpleMavenLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenCentral
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.*

internal class ParentResolutionStage : PomProcessStage<WrappedPomData, ParentResolutionStage.ParentResolutionData> {
    override fun process(i: WrappedPomData): ParentResolutionData {
        val (_, repo) = i

        fun recursivelyLoadParents(child: PomData, thisLayout: SimpleMavenRepositoryLayout): List<PomData> {
            val parent: PomParent = child.parent ?: return listOf(SUPER_POM)

            val mavenCentral = SimpleMavenCentral(repo.settings.preferredHash)
            val immediateRepos = listOf(thisLayout, mavenCentral) + child.repositories
                .map { repo.settings.repositoryReferencer.referenceLayout(it) ?: throw IllegalStateException("Failed to get repository layout for parent pom. Repository was '$it'") }

            val artifact = immediateRepos.firstNotNullOfOrNull {
                it.artifactOf(
                    parent.groupId,
                    parent.artifactId,
                    parent.version,
                    null,
                    "pom"
                )
            } ?: throw IllegalStateException(
                "Failed to find parent: '${parent.groupId}:${parent.artifactId}:${parent.version}' in repositories: ${
                    (immediateRepos + thisLayout + mavenCentral).map { (it as? DefaultSimpleMavenLayout)?.url ?: it.type }
                }"
            )

            val parentData = parseData(artifact)

            return listOf(parentData) + recursivelyLoadParents(parentData, thisLayout)
        }

        return ParentResolutionData(i.pomData, i.thisRepo, recursivelyLoadParents(i.pomData, i.thisRepo.layout))
    }

    data class ParentResolutionData(
        val pomData: PomData,
        val thisRepo: SimpleMavenRepositoryHandler,
        val parents: List<PomData>
    ) : PomProcessStage.StageData
}