package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenMetadataHandler
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenCentral
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.*

internal class ParentResolutionStage : PomProcessStage<WrappedPomData, ParentResolutionStage.ParentResolutionData> {
    override val name: String = "Parent Resolution"

    override fun process(i: WrappedPomData): Either<PomParsingException, ParentResolutionData> = either.eager {
        val (data, repo) = i

        fun recursivelyLoadParents(
            child: PomData,
            thisLayout: SimpleMavenRepositoryLayout
        ): Either<PomParsingException, List<PomData>> = either.eager loadParents@ {
            val parent: PomParent = child.parent ?: return@loadParents listOf(SUPER_POM)

            val mavenCentral = SimpleMavenCentral(repo.settings.preferredHash)
            val immediateRepos = listOf(thisLayout, mavenCentral) + child.repositories.map {
                SimpleMavenDefaultLayout(
                    it.url,
                    repo.settings.preferredHash,
                    it.releases.enabled,
                    it.snapshots.enabled
                )
            }

            val artifact = immediateRepos.firstNotNullOfOrNull {
                it.resourceOf(
                    parent.groupId,
                    parent.artifactId,
                    parent.version,
                    null,
                    "pom"
                ).orNull()
            }

            ensureNotNull(artifact) {
                PomParsingException.PomNotFound(
                    "${parent.groupId}:${parent.artifactId}:${parent.version}",
                    immediateRepos.map(SimpleMavenRepositoryLayout::name),
                    this@ParentResolutionStage
                )
            }


            val parentData = parseData(artifact).bind()

            listOf(parentData) + recursivelyLoadParents(parentData, thisLayout).bind()
        }

        ParentResolutionData(data, i.thisRepo, recursivelyLoadParents(data, i.thisRepo.layout).bind())
    }

    data class ParentResolutionData(
        val pomData: PomData,
        val thisRepo: SimpleMavenMetadataHandler,
        val parents: List<PomData>
    ) : PomProcessStage.StageData
}