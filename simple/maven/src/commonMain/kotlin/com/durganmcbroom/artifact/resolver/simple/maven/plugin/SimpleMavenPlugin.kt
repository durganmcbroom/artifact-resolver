package com.durganmcbroom.artifact.resolver.simple.maven.plugin

import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomPropertySource

public abstract class SimpleMavenPlugin(
    protected val configuration: SimplePluginConfiguration
) : PomPropertySource {
    public abstract val mockedGroup: String
    public abstract val mockedArtifact: String
    public open val mockedVersion: VersionDescriptor
        get() = VersionDescriptor(ALL_VERSION)

    public companion object {
        @JvmStatic
        public val ALL_VERSION: String = "<*>"
    }

    public class VersionDescriptor(
        _version: String?
     ) {
        public val version: String = _version ?: ALL_VERSION
        private val isAll: Boolean = version == ALL_VERSION

        public fun matches(other: VersionDescriptor) : Boolean = other.isAll || isAll || other.version == version

        override fun toString(): String = version
    }
}