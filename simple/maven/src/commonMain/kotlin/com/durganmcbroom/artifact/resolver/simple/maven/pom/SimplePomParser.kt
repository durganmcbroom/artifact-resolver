@file:JvmName("SimplePomParser")

package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenMetadataHandler
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.*
import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.JobName
import com.durganmcbroom.jobs.job
import com.durganmcbroom.jobs.map
import com.durganmcbroom.resources.Resource

private val parentResolutionStage = ParentResolutionStage()
private val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
private val primaryInterpolationStage = PrimaryInterpolationStage()
private val pluginManagementInjectionStage = PluginManagementInjectionStage()
private val pluginLoadingStage = PluginLoadingStage()
private val secondaryInterpolationStage = SecondaryInterpolationStage()
private val dependencyManagementInjector = DependencyManagementInjectionStage()

public fun SimpleMavenMetadataHandler.parsePom(resource: Resource): Job<PomData> =
    parseData(resource).map { parsePom(it)().merge() }

public fun SimpleMavenMetadataHandler.parsePom(data: PomData): Job<PomData> =
    job(JobName("Assemble POM data for POM: '${data.groupId}:${data.artifactId}:${data.version}'")) {
        WrappedPomData(data, this@parsePom)
            // Cant use method references here as they are all suspending.
            .let { parentResolutionStage.process(it) }().merge()
            .let { inheritanceAssemblyStage.process(it) }().merge()
            .let { primaryInterpolationStage.process(it) }().merge()
            .let { pluginManagementInjectionStage.process(it) }().merge()
            .let { pluginLoadingStage.process(it) }().merge()
            .let { secondaryInterpolationStage.process(it) }().merge()
            .let { dependencyManagementInjector.process(it) }().merge().data
    }

public const val SUPER_POM_PATH: String = "/pom-4.0.0.xml"

public expect val SUPER_POM: PomData

public expect fun parseData(resource: Resource): Job<PomData>