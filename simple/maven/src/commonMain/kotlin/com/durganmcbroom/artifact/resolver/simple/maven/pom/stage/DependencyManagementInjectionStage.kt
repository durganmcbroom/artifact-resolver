package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenMetadataHandler
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.*
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.DependencyManagementInjectionStage.DependencyManagementInjectionData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.SecondaryInterpolationStage.SecondaryInterpolationData

internal class DependencyManagementInjectionStage :
    PomProcessStage<SecondaryInterpolationData, DependencyManagementInjectionData> {

    override val name: String = "Dependency management injection"

    override fun process(i: SecondaryInterpolationData): Either<PomParsingException, DependencyManagementInjectionData> =
        either.eager {
            val (data, repo) = i

            val layouts = listOf(repo.layout) + data.repositories.map {
                ensure(it.layout == "default") {
                    PomParsingException.InvalidRepository(
                        it.layout,
                        "${data.groupId}:${data.artifactId}:${data.version}",
                        it.name
                    )
                }

                SimpleMavenDefaultLayout(it.url, repo.settings.preferredHash, it.releases.enabled, it.snapshots.enabled)
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
                        ).orNull()
                    }

                    ensureNotNull(bomResource) {
                        PomParsingException.PomNotFound(
                            "'${bom.groupId}:${bom.artifactId}:${bom.version}'",
                            layouts.map(SimpleMavenRepositoryLayout::name),
                            this@DependencyManagementInjectionStage
                        )
                    }

                    parseData(bomResource).bind()
                }

            val managedDependencies =
                data.dependencyManagement.dependencies.filterNot { it.scope == "import" } + boms.flatMap { it.dependencyManagement.dependencies }

            val dependencies = data.dependencies.mapTo(HashSet()) { dep ->
                val managed by lazy { managedDependencies.find { dep.groupId == it.groupId && dep.artifactId == it.artifactId } }

                MavenDependency(
                    dep.groupId,
                    dep.artifactId,
                    ensureNotNull(
                        dep.version ?: managed?.version
                    ) { PomParsingException.DependencyManagementInjectionFailure },
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
        val repo: SimpleMavenMetadataHandler
    ) : PomProcessStage.StageData
}