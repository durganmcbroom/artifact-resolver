@file:JvmName("SimplePomParser")

package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRepository
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.*
import com.durganmcbroom.resources.Resource

private val parentResolutionStage = ParentResolutionStage()
private val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
private val primaryInterpolationStage = PrimaryInterpolationStage()
private val pluginManagementInjectionStage = PluginManagementInjectionStage()
private val pluginLoadingStage = PluginLoadingStage()
private val secondaryInterpolationStage = SecondaryInterpolationStage()
private val dependencyManagementInjector = DependencyManagementInjectionStage()

public suspend fun SimpleMavenArtifactRepository.parsePom(resource: Resource): PomData =
    parsePom(parseData(resource), resource.location)

public suspend fun SimpleMavenArtifactRepository.parsePom(data: PomData, location: String): PomData =
    runCatching {
        WrappedPomData(data, this@parsePom)
            // Cant use method references here as they are all suspending.
            .let { parentResolutionStage.process(it) }
            .let { inheritanceAssemblyStage.process(it) }
            .let { primaryInterpolationStage.process(it) }
            .let { pluginManagementInjectionStage.process(it) }
            .let { pluginLoadingStage.process(it) }
            .let { secondaryInterpolationStage.process(it) }
            .let { dependencyManagementInjector.process(it) }.data
    }.getOrElse { throw PomException.AssembleException(it, location) }

public const val SUPER_POM_PATH: String = "/pom-4.0.0.xml"

public expect suspend fun getSuperPom(): PomData

public expect suspend fun parseData(resource: Resource): PomData