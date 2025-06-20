package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.concurrentHashMap
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRepository
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenDescriptor
import com.durganmcbroom.artifact.resolver.simple.maven.layout.MAVEN_CENTRAL_REPO
import com.durganmcbroom.artifact.resolver.simple.maven.layout.ResourceRetrievalException
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.*
import com.durganmcbroom.resources.ResourceException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class ParentResolutionStage : PomProcessStage<WrappedPomData, ParentResolutionStage.ParentResolutionData> {
    override val name: String = "Parent Resolution"

    private val poms: MutableMap<
            SimpleMavenDescriptor,
            Deferred<PomData>
            > = concurrentHashMap()

    override suspend fun process(
        i: WrappedPomData
    ): ParentResolutionData {
        val (data, repo) = i

        suspend fun recursivelyLoadParents(
            child: PomData,
            thisLayout: SimpleMavenRepositoryLayout
        ): List<PomData> = coroutineScope {
            val parent: PomParent = child.parent ?: return@coroutineScope listOf(getSuperPom())

            // Dont need resource verification, only loading poms.
            val mavenCentral = SimpleMavenDefaultLayout(
                MAVEN_CENTRAL_REPO,
                repo.settings.preferredHash,
                true, false,
                { _, _ -> false }
            )
            val immediateRepos = listOf(thisLayout, mavenCentral) + child.repositories.map {
                SimpleMavenDefaultLayout(
                    it.url,
                    repo.settings.preferredHash,
                    it.releases.enabled,
                    it.snapshots.enabled,
                    { _, _ -> false }
                )
            }

            val key = SimpleMavenDescriptor(
                parent.groupId,
                parent.artifactId,
                parent.version,
                null
            )

            val parentJob = poms[key] ?: coroutineScope {
                val job = async {
                    val resource = immediateRepos.map {
                        async {
                            try {
                                it.resourceOf(
                                    parent.groupId,
                                    parent.artifactId,
                                    parent.version,
                                    null,
                                    "pom"
                                )
                            } catch (_: ResourceException) {
                                null
                            } catch (_: ResourceRetrievalException) {
                                null
                            }
                        }
                    }.awaitAll().firstNotNullOfOrNull { it }
                        ?: throw (PomException.PomNotFound(
                            "${parent.groupId}:${parent.artifactId}:${parent.version}",
                            immediateRepos.map(SimpleMavenRepositoryLayout::name),
                            this@ParentResolutionStage
                        ))

                    parseData(resource)
                }

                poms[key] = job

                job
            }

            val parentData = parentJob.await()

            listOf(parentData) + recursivelyLoadParents(parentData, thisLayout)
        }

        return ParentResolutionData(data, i.thisRepo, recursivelyLoadParents(data, i.thisRepo.layout))
    }

    data class ParentResolutionData(
        val pomData: PomData,
        val thisRepo: SimpleMavenArtifactRepository,
        val parents: List<PomData>
    ) : PomProcessStage.StageData
}