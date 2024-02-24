package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import arrow.core.identity
import arrow.core.raise.ensureNotNull
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenMetadataHandler
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenCentral
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.*
import com.durganmcbroom.jobs.JobResult
import com.durganmcbroom.jobs.jobScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal class ParentResolutionStage : PomProcessStage<WrappedPomData, ParentResolutionStage.ParentResolutionData> {
    override val name: String = "Parent Resolution"

    override suspend fun process(
        i: WrappedPomData
    ): JobResult<ParentResolutionData, PomParsingException> = jobScope {
        val (data, repo) = i

        suspend fun recursivelyLoadParents(
            child: PomData,
            thisLayout: SimpleMavenRepositoryLayout
        ): JobResult<List<PomData>, PomParsingException> = jobScope loadParents@{
            val parent: PomParent = child.parent ?: return@loadParents listOf(SUPER_POM)

            val mavenCentral = SimpleMavenCentral(repo.settings.preferredHash)
            val immediateRepos = listOf(thisLayout, mavenCentral) + child.repositories.map {
                SimpleMavenDefaultLayout(
                    it.url,
                    repo.settings.preferredHash,
                    it.releases.enabled,
                    it.snapshots.enabled,
                    repo.settings.requireResourceVerification
                )
            }

            val artifact = immediateRepos.map {
                async {
                    it.resourceOf(
                        parent.groupId,
                        parent.artifactId,
                        parent.version,
                        null,
                        "pom"
                    ).getOrNull()
                }
            }.awaitAll().firstNotNullOf(::identity)

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