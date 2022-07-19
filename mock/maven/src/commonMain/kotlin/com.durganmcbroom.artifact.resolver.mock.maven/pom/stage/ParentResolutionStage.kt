package com.durganmcbroom.artifact.resolver.mock.maven.pom.stage

import com.durganmcbroom.artifact.resolver.mock.maven.MavenRepositoryHandler
import com.durganmcbroom.artifact.resolver.mock.maven.layout.DefaultMockMavenLayout
import com.durganmcbroom.artifact.resolver.mock.maven.layout.MockMavenCentral
import com.durganmcbroom.artifact.resolver.mock.maven.layout.MockMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.mock.maven.pom.*

internal class ParentResolutionStage : PomProcessStage<WrappedPomData, ParentResolutionStage.ParentResolutionData> {
    override fun process(i: WrappedPomData): ParentResolutionData {
        val (_, repo) = i

        fun recursivelyLoadParents(child: PomData, thisLayout: MockMavenRepositoryLayout): List<PomData> {
            val parent: PomParent = child.parent ?: return listOf(SUPER_POM)

            val mavenCentral = MockMavenCentral(repo.settings.preferredHash)
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
                    (immediateRepos + thisLayout + mavenCentral).map { (it as? DefaultMockMavenLayout)?.url ?: it.type }
                }"
            )

            val parentData = parseData(artifact)

            return listOf(parentData) + recursivelyLoadParents(parentData, thisLayout)
        }

        return ParentResolutionData(i.pomData, i.thisRepo, recursivelyLoadParents(i.pomData, i.thisRepo.layout))
    }

    data class ParentResolutionData(
        val pomData: PomData,
        val thisRepo: MavenRepositoryHandler,
        val parents: List<PomData>
    ) : PomProcessStage.StageData
}