package com.durganmcbroom.artifact.resolver.mock.maven.pom.stage

import com.durganmcbroom.artifact.resolver.mock.maven.pom.*


private val propertyMatcher = Regex("^\\$\\{(.*)}$")

internal fun String.matchAsProperty(): String? = propertyMatcher.matchEntire(this)?.groupValues?.get(1)

internal class PropertyReplacer(
    private val sources: List<PomPropertySource>
) {
    fun String.ifAsProperty(): String =
        matchAsProperty()?.let { p ->
            sources.firstNotNullOfOrNull { it.properties[p] }
        }?.ifAsProperty() ?: this
//
//    fun <T : Any> replaceProperties(any: T): T {
//        val kClass: KClass<T> = any::class as KClass<T>
//
//        check(kClass.isData)
//
//        val primaryConstructor = checkNotNull(kClass.primaryConstructor)
//
//        val arguments = primaryConstructor.parameters
//            .map { prop -> kClass.memberProperties.find { it.name == prop.name }!! }
//            .map { it.get(any) }
//            .map { if (it is String) it.ifAsProperty() else it }
//
//        return primaryConstructor.call(*arguments.toTypedArray())
//    }

    companion object {
        fun <R> of(vararg sources: PomPropertySource, block: PropertyReplacer.() -> R): R =
            PropertyReplacer(sources.toList()).run(block)

        fun <R> of(
            data: PomData,
            parents: List<PomData>,
            vararg others: PomPropertySource,
            block: PropertyReplacer.() -> R
        ): R =
            PropertyReplacer(
                listOf(
                    PropertyInterpolationSource(data),
                    ProjectPropertySource(data, parents)
                ) + others
            ).run(block)
    }
}

private class PropertyInterpolationSource(
    private val data: PomData
) : PomPropertySource {
    override val properties: Map<String, String> by data::properties
}

private class ProjectPropertySource(
    private val data: PomData,
    private val parents: List<PomData>
) : PomPropertySource {
    override val properties: Map<String, String> = object : Map<String, String> by HashMap() {
        override fun get(key: String): String? =
            when (key) {
                "project.artifactId" -> data.artifactId
                "project.version" -> data.version
                "project.groupId" -> data.groupId
                "project.parent.artifactId" -> parents.first().artifactId
                "project.parent.version" -> parents.firstNotNullOf { it.version }
                else -> null
            }
    }
}

internal fun PropertyReplacer.doInterpolation(
    data: PomData
): PomData {
    val dependencyManagement = data.dependencyManagement.dependencies.mapTo(HashSet()) {
        ManagedDependency(
            it.groupId.ifAsProperty(),
            it.artifactId.ifAsProperty(),
            it.version.ifAsProperty(),
            it.classifier?.ifAsProperty(),
            it.scope?.ifAsProperty()
        )
    }.let(::DependencyManagement)

    val dependencies = data.dependencies.mapTo(HashSet()) {
        MavenDependency(
            it.groupId.ifAsProperty(),
            it.artifactId.ifAsProperty(),
            it.version?.ifAsProperty(),
            it.classifier?.ifAsProperty(),
            it.scope?.ifAsProperty()
        )
    }
    val repositories = data.repositories.map {
        PomRepository(
            it.url.ifAsProperty(),
            it.layout.ifAsProperty()
        )
    }

    val extensions = data.build.extensions.map {
        PomExtension(
            it.groupId.ifAsProperty(),
            it.artifactId.ifAsProperty(),
            it.version.ifAsProperty(),
            it.configurations
        )
    }
    val plugins = data.build.plugins.map {
        PomPlugin(
            it.groupId.ifAsProperty(),
            it.artifactId.ifAsProperty(),
            it.version?.ifAsProperty(),
            it.extensions,
            it.configurations
        )
    }
    val pluginManagement =
        data.build.pluginManagement.plugins.map{
            PomPlugin(
                it.groupId.ifAsProperty(),
                it.artifactId.ifAsProperty(),
                it.version?.ifAsProperty(),
                it.extensions,
                it.configurations
            )
        }.let(::PomPluginManagement)

    val build = PomBuild(extensions, plugins, pluginManagement)

    val packaging = data.packaging.ifAsProperty()

    return PomData(
        data.groupId,
        data.artifactId,
        data.version,
        data.properties,
        data.parent,
        dependencyManagement,
        dependencies,
        repositories,
        build,
        packaging
    )
}