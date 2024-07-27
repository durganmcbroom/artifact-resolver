package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRepository
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.*
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.DependencyManagementInjectionStage.DependencyManagementInjectionData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.SecondaryInterpolationStage.SecondaryInterpolationData
import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.JobName
import com.durganmcbroom.jobs.job

internal class DependencyManagementInjectionStage :
    PomProcessStage<SecondaryInterpolationData, DependencyManagementInjectionData> {
    override val name: String = "Dependency management injection"

    override fun process(
        i: SecondaryInterpolationData
    ): Job<DependencyManagementInjectionData> = job(JobName("Process pom for stage: '$name'")) {
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
                repo.settings.requireResourceVerification
            )
        }

        val boms = data.dependencyManagement.dependencies
            .filter { it.scope == "import" }
            .map { bom ->
                val bomResource = layouts.firstNotNullOfOrNull { l ->
                    l.resourceOf(
                        bom.groupId,
                        bom.artifactId,
                        bom.version,
                        null,
                        "pom"
                    )().getOrNull()
                } ?: throw PomException.PomNotFound(
                    "'${bom.groupId}:${bom.artifactId}:${bom.version}'",
                    layouts.map(SimpleMavenRepositoryLayout::name),
                    this@DependencyManagementInjectionStage
                )

                parseData(bomResource)().merge()
            }

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

        DependencyManagementInjectionData(newData, repo)
    }

    data class DependencyManagementInjectionData(
        val data: PomData,
        val repo: SimpleMavenArtifactRepository
    ) : PomProcessStage.StageData
}