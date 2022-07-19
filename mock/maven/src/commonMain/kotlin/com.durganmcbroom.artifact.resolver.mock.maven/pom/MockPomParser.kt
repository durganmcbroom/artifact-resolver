package com.durganmcbroom.artifact.resolver.mock.maven.pom

import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.mock.maven.MavenRepositoryHandler
import com.durganmcbroom.artifact.resolver.mock.maven.pom.stage.*
import com.durganmcbroom.artifact.resolver.mock.maven.pom.stage.PluginLoadingStage
import com.durganmcbroom.artifact.resolver.mock.maven.pom.stage.PluginManagementInjectionStage
import com.durganmcbroom.artifact.resolver.mock.maven.pom.stage.PomFinalizingStage
import com.durganmcbroom.artifact.resolver.mock.maven.pom.stage.PomInheritanceAssemblyStage
import com.durganmcbroom.artifact.resolver.mock.maven.pom.stage.PrimaryInterpolationStage

private val parentResolutionStage = ParentResolutionStage()
private val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
private val primaryInterpolationStage = PrimaryInterpolationStage()
private val pluginManagementInjectionStage = PluginManagementInjectionStage()
private val pluginLoadingStage = PluginLoadingStage()
private val secondaryInterpolationStage = SecondaryInterpolationStage()
private val dependencyManagementInjector = DependencyManagementInjectionStage()
private val pomFinalizingStage = PomFinalizingStage()

public fun MavenRepositoryHandler.parsePom(resource: CheckedResource): FinalizedPom = parsePom(parseData(resource))

public fun MavenRepositoryHandler.parsePom(data: PomData): FinalizedPom = WrappedPomData(data, this)
    .let(parentResolutionStage::process)
    .let(inheritanceAssemblyStage::process)
    .let(primaryInterpolationStage::process)
    .let(pluginManagementInjectionStage::process)
    .let(pluginLoadingStage::process)
    .let(secondaryInterpolationStage::process)
    .let(dependencyManagementInjector::process)
    .let(pomFinalizingStage::process)

internal fun MavenRepositoryHandler.parsePomExtensions(data: PomData): List<PomExtension> = WrappedPomData(data, this)
    .let(parentResolutionStage::process)
    .let(inheritanceAssemblyStage::process)
    .let(primaryInterpolationStage::process).pomData.build.extensions

public const val SUPER_POM_PATH: String = "/pom-4.0.0.xml"

public expect val SUPER_POM: PomData

public expect fun parseData(resource: CheckedResource): PomData