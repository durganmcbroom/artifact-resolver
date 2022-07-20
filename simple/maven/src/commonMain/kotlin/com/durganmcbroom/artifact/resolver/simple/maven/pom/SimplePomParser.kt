package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositoryHandler
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.*
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.PluginLoadingStage
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.PluginManagementInjectionStage
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.PomFinalizingStage
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.PomInheritanceAssemblyStage
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.PrimaryInterpolationStage

private val parentResolutionStage = ParentResolutionStage()
private val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
private val primaryInterpolationStage = PrimaryInterpolationStage()
private val pluginManagementInjectionStage = PluginManagementInjectionStage()
private val pluginLoadingStage = PluginLoadingStage()
private val secondaryInterpolationStage = SecondaryInterpolationStage()
private val dependencyManagementInjector = DependencyManagementInjectionStage()
private val pomFinalizingStage = PomFinalizingStage()

public fun SimpleMavenRepositoryHandler.parsePom(resource: CheckedResource): FinalizedPom = parsePom(parseData(resource))

public fun SimpleMavenRepositoryHandler.parsePom(data: PomData): FinalizedPom = WrappedPomData(data, this)
    .let(parentResolutionStage::process)
    .let(inheritanceAssemblyStage::process)
    .let(primaryInterpolationStage::process)
    .let(pluginManagementInjectionStage::process)
    .let(pluginLoadingStage::process)
    .let(secondaryInterpolationStage::process)
    .let(dependencyManagementInjector::process)
    .let(pomFinalizingStage::process)

internal fun SimpleMavenRepositoryHandler.parsePomExtensions(data: PomData): List<PomExtension> = WrappedPomData(data, this)
    .let(parentResolutionStage::process)
    .let(inheritanceAssemblyStage::process)
    .let(primaryInterpolationStage::process).pomData.build.extensions

public const val SUPER_POM_PATH: String = "/pom-4.0.0.xml"

public expect val SUPER_POM: PomData

public expect fun parseData(resource: CheckedResource): PomData