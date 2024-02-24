@file:JvmName("SimplePomParser")

package com.durganmcbroom.artifact.resolver.simple.maven.pom

import arrow.core.left
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenMetadataHandler
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.*
import com.durganmcbroom.jobs.JobName
import com.durganmcbroom.jobs.JobResult
import com.durganmcbroom.jobs.job
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceStream

private val parentResolutionStage = ParentResolutionStage()
private val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
private val primaryInterpolationStage = PrimaryInterpolationStage()
private val pluginManagementInjectionStage = PluginManagementInjectionStage()
private val pluginLoadingStage = PluginLoadingStage()
private val secondaryInterpolationStage = SecondaryInterpolationStage()
private val dependencyManagementInjector = DependencyManagementInjectionStage()

public suspend fun SimpleMavenMetadataHandler.parsePom(resource: Resource): JobResult<PomData, PomParsingException> =
    parseData(resource).fold({ it.left() }, { parsePom(it) })

public suspend fun SimpleMavenMetadataHandler.parsePom(data: PomData): JobResult<PomData, PomParsingException> =
    job(JobName("Assemble POM data for POM: '${data.groupId}:${data.artifactId}:${data.version}'")) {
        WrappedPomData(data, this@parsePom)
            // Cant use method references here as they are all suspending.
            .let { parentResolutionStage.process(it) }.bind()
            .let { inheritanceAssemblyStage.process(it) }.bind()
            .let { primaryInterpolationStage.process(it) }.bind()
            .let { pluginManagementInjectionStage.process(it) }.bind()
            .let { pluginLoadingStage.process(it) }.bind()
            .let { secondaryInterpolationStage.process(it) }.bind()
            .let { dependencyManagementInjector.process(it) }.bind().data
    }

public const val SUPER_POM_PATH: String = "/pom-4.0.0.xml"

public expect val SUPER_POM: PomData

public expect suspend fun parseData(resource: Resource): JobResult<PomData, PomParsingException>