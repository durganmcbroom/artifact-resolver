@file:JvmName("SimplePomParser")

package com.durganmcbroom.artifact.resolver.simple.maven.pom

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenMetadataHandler
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.*

private val parentResolutionStage = ParentResolutionStage()
private val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
private val primaryInterpolationStage = PrimaryInterpolationStage()
private val pluginManagementInjectionStage = PluginManagementInjectionStage()
private val pluginLoadingStage = PluginLoadingStage()
private val secondaryInterpolationStage = SecondaryInterpolationStage()
private val dependencyManagementInjector = DependencyManagementInjectionStage()

public fun SimpleMavenMetadataHandler.parsePom(resource: CheckedResource): Either<PomParsingException, PomData> =
    parseData(resource).fold({ it.left() }, { parsePom(it) })

public fun SimpleMavenMetadataHandler.parsePom(data: PomData): Either<PomParsingException, PomData> = either.eager {
    WrappedPomData(data, this@parsePom)
        .let(parentResolutionStage::process).bind()
        .let(inheritanceAssemblyStage::process).bind()
        .let(primaryInterpolationStage::process).bind()
        .let(pluginManagementInjectionStage::process).bind()
        .let(pluginLoadingStage::process).bind()
        .let(secondaryInterpolationStage::process).bind()
        .let(dependencyManagementInjector::process).bind().data
}

public const val SUPER_POM_PATH: String = "/pom-4.0.0.xml"

public expect val SUPER_POM: PomData

public expect fun parseData(resource: CheckedResource): Either<PomParsingException.InvalidPom, PomData>