package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRepository
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.*
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.DependencyManagementInjectionStage.DependencyManagementInjectionData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.SecondaryInterpolationStage.SecondaryInterpolationData
import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.JobName
import com.durganmcbroom.jobs.async.mapAsync
import com.durganmcbroom.jobs.job
import kotlinx.coroutines.awaitAll

internal class DependencyManagementInjectionStage :
    PomProcessStage<SecondaryInterpolationData, DependencyManagementInjectionData> {
    override val name: String = "Dependency management injection"

    override suspend fun process(
        i: SecondaryInterpolationData
    ): DependencyManagementInjectionData {
        val (data, repo) = i

        val layouts = listOf(repo.layout) + data.repositories.map {
            if (it.layout != "default") {
                throw PomException.InvalidRepository(
                    it.layout,
                    "${data.groupId}:${data.artifactId}:${data.version}",
                    it.name ?: it.id ?: "<unknown>"
                )
            }

            SimpleMavenDefaultLayout(
                it.url,
                repo.settings.preferredHash,
                it.releases.enabled,
                it.snapshots.enabled,
                { _, _ ->
                    // Only loading poms
                    false
                }
            )
        }

        val boms = data.dependencyManagement.dependencies
            .filter { it.scope == "import" }
            .mapAsync { bom ->
                val bomResource = layouts.firstNotNullOfOrNull { l ->
                    l.resourceOf(
                        bom.groupId,
                        bom.artifactId,
                        bom.version,
                        null,
                        "pom"
                    )
                } ?: throw PomException.PomNotFound(
                    "'${bom.groupId}:${bom.artifactId}:${bom.version}'",
                    layouts.map(SimpleMavenRepositoryLayout::name),
                    this@DependencyManagementInjectionStage
                )

                parseData(bomResource)
            }.awaitAll()

        val managedDependencies =
            data.dependencyManagement.dependencies.filterNot { it.scope == "import" } + boms.flatMap { it.dependencyManagement.dependencies }

        val dependencies = data.dependencies.mapTo(HashSet()) { dep ->
            val managed by lazy { managedDependencies.find { dep.groupId == it.groupId && dep.artifactId == it.artifactId } }

            MavenDependency(
                dep.groupId,
                dep.artifactId,
                dep.version ?: managed?.version ?: throw PomException.DependencyManagementInjectionFailure,
                dep.classifier ?: managed?.classifier,
                dep.scope ?: managed?.scope
            )
        }

        val newData = data.copy(
            dependencies = dependencies
        )

        return DependencyManagementInjectionData(newData, repo)
    }

    data class DependencyManagementInjectionData(
        val data: PomData,
        val repo: SimpleMavenArtifactRepository
    ) : PomProcessStage.StageData
}