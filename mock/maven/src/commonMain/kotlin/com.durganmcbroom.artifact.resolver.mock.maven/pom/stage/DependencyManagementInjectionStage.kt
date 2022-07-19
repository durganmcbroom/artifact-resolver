package com.durganmcbroom.artifact.resolver.mock.maven.pom.stage

import com.durganmcbroom.artifact.resolver.mock.maven.MavenRepositoryHandler
import com.durganmcbroom.artifact.resolver.mock.maven.layout.DefaultMockMavenLayout
import com.durganmcbroom.artifact.resolver.mock.maven.pom.MavenDependency
import com.durganmcbroom.artifact.resolver.mock.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.mock.maven.pom.PomProcessStage
import com.durganmcbroom.artifact.resolver.mock.maven.pom.parseData

internal class DependencyManagementInjectionStage :
    PomProcessStage<SecondaryInterpolationStage.SecondaryInterpolationData, DependencyManagementInjectionStage.DependencyManagementInjectionData> {
    override fun process(i: SecondaryInterpolationStage.SecondaryInterpolationData): DependencyManagementInjectionData {
        val (data, repo) = i

        val layouts = listOf(repo.layout) + data.repositories
            .map {
                repo.settings.repositoryReferencer.referenceLayout(it) ?: throw IllegalStateException("Failed to get repository layout for bom. Repository was: '$it'")
            }

        val boms = data.dependencyManagement.dependencies.filter { it.scope == "import" }
            .map { bom ->
                parseData(
                    layouts.firstNotNullOfOrNull { l ->
                        l.artifactOf(
                            bom.groupId,
                            bom.artifactId,
                            bom.version,
                            null,
                            "pom"
                        )
                    } ?: throw IllegalStateException(
                        "Failed to find BOM $'${bom.groupId}:${bom.artifactId}:${bom.version}' in repositories: ${layouts.joinToString { (it as? DefaultMockMavenLayout)?.url ?: it.type }}"
                    )
                )
            }

        val managedDependencies =
            data.dependencyManagement.dependencies.filterNot { it.scope == "import" } + boms.flatMap { it.dependencyManagement.dependencies }

        val dependencies = data.dependencies.mapTo(HashSet()) { dep ->
            val managed by lazy { managedDependencies.find { dep.groupId == it.groupId && dep.artifactId == it.artifactId } }

            MavenDependency(
                dep.groupId,
                dep.artifactId,
                dep.version ?: managed?.version
                ?: throw IllegalArgumentException("Failed to determine version for dependency: $'${dep.groupId}:${dep.artifactId}'"),
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
        val repo: MavenRepositoryHandler
    ) : PomProcessStage.StageData
}