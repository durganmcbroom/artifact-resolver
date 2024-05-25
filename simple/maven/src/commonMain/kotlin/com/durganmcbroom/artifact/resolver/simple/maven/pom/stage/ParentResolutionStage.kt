package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenMetadataHandler
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenCentral
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.*
import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.JobName
import com.durganmcbroom.jobs.job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

internal class ParentResolutionStage : PomProcessStage<WrappedPomData, ParentResolutionStage.ParentResolutionData> {
    override val name: String = "Parent Resolution"

    override fun process(
        i: WrappedPomData
    ): Job<ParentResolutionData> = job(JobName("Process pom for stage: '$name'")) {
        val (data, repo) = i

        fun recursivelyLoadParents(
            child: PomData,
            thisLayout: SimpleMavenRepositoryLayout
        ): Job<List<PomData>> = job(JobName("Recursively load parent for pom stage: '$name'")) loadParents@{
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

            val artifact = runBlocking {
                immediateRepos.map {
                    async {
                        it.resourceOf(
                            parent.groupId,
                            parent.artifactId,
                            parent.version,
                            null,
                            "pom"
                        )
                    }
                }.awaitAll().firstNotNullOfOrNull { it().getOrNull() }
            } ?: throw (
                    PomException.PomNotFound(
                        "${parent.groupId}:${parent.artifactId}:${parent.version}",
                        immediateRepos.map(SimpleMavenRepositoryLayout::name),
                        this@ParentResolutionStage
                    )
                    )

            val parentData = parseData(artifact)().merge()

            listOf(parentData) + recursivelyLoadParents(parentData, thisLayout)().merge()
        }

        ParentResolutionData(data, i.thisRepo, recursivelyLoadParents(data, i.thisRepo.layout)().merge())
    }

    data class ParentResolutionData(
        val pomData: PomData,
        val thisRepo: SimpleMavenMetadataHandler,
        val parents: List<PomData>
    ) : PomProcessStage.StageData
}