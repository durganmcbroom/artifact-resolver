package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRepository
import com.durganmcbroom.artifact.resolver.simple.maven.pom.*
import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.SuccessfulJob
import kotlin.reflect.KProperty1

internal class PomInheritanceAssemblyStage :
    PomProcessStage<ParentResolutionStage.ParentResolutionData, PomInheritanceAssemblyStage.AssembledPomData> {

    override val name: String = "Pom inheritance assembly"

    // TODO Redo the pom inheritance, its technically not fully correct since list fields dont get added together correctly.
    override fun process(i: ParentResolutionStage.ParentResolutionData): Job<AssembledPomData> {
        val (data, ref, parents) = i

        val all = listOf(data) + parents
        fun <T> travelUntil(req: KProperty1<PomData, T>): T =
            all.firstNotNullOfOrNull {req.get(it)} ?: throw IllegalArgumentException(
                "Failed to find value for field : '${req.name}' in the pom hierarchy."
            )

        val groupId = travelUntil(PomData::groupId)
        val artifactId = data.artifactId
        val version = travelUntil(PomData::version)

        val properties = all.fold(mapOf()) { acc: Map<String, String>, it -> acc + it.properties }
        val parent = data.parent
        val dependencyManagement =
            DependencyManagement(all.flatMapTo(HashSet()) { it.dependencyManagement.dependencies })

        val dependencies = all.flatMapTo(HashSet(), PomData::dependencies)
        val repositories = all.flatMap(PomData::repositories)

        val plugins = all.flatMap { it.build.plugins }
        val extensions = all.flatMap { it.build.extensions }
        val pluginManagement = PomPluginManagement(all.flatMap { it.build.pluginManagement.plugins })

        val build = PomBuild(extensions, plugins, pluginManagement)

        val packaging = data.packaging

        val assembledData = PomData(
            groupId,
            artifactId,
            version,
            properties,
            parent,
            dependencyManagement,
            dependencies,
            repositories,
            build,
            packaging
        )

        return SuccessfulJob { AssembledPomData(assembledData, parents, ref) }
    }

    data class AssembledPomData(
        val pomData: PomData,
        val parents: List<PomData>,
        val thisRepo: SimpleMavenArtifactRepository
    ) : PomProcessStage.StageData
}