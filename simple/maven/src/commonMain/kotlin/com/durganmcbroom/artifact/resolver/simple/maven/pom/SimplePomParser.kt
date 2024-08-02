@file:JvmName("SimplePomParser")

package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRepository
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.*
import com.durganmcbroom.jobs.*
import com.durganmcbroom.resources.Resource

private val parentResolutionStage = ParentResolutionStage()
private val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
private val primaryInterpolationStage = PrimaryInterpolationStage()
private val pluginManagementInjectionStage = PluginManagementInjectionStage()
private val pluginLoadingStage = PluginLoadingStage()
private val secondaryInterpolationStage = SecondaryInterpolationStage()
private val dependencyManagementInjector = DependencyManagementInjectionStage()

public fun SimpleMavenArtifactRepository.parsePom(resource: Resource): Job<PomData> =
    parseData(resource).map { parsePom(it, resource.location)().merge() }

public fun SimpleMavenArtifactRepository.parsePom(data: PomData, location: String): Job<PomData> =
    job(JobName("Assemble POM data for POM: '${data.groupId}:${data.artifactId}:${data.version}'")) {
        runCatching {
            WrappedPomData(data, this@parsePom)
                // Cant use method references here as they are all suspending.
                .let { parentResolutionStage.process(it) }().merge()
                .let { inheritanceAssemblyStage.process(it) }().merge()
                .let { primaryInterpolationStage.process(it) }().merge()
                .let { pluginManagementInjectionStage.process(it) }().merge()
                .let { pluginLoadingStage.process(it) }().merge()
                .let { secondaryInterpolationStage.process(it) }().merge()
                .let { dependencyManagementInjector.process(it) }().merge().data

        }.mapException { PomException.AssembleException(it, location) }.merge()
    }

public const val SUPER_POM_PATH: String = "/pom-4.0.0.xml"

public expect val SUPER_POM: PomData

public expect fun parseData(resource: Resource): Job<PomData>