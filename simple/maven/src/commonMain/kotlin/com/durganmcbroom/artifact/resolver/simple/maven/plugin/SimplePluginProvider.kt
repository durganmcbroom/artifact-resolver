package com.durganmcbroom.artifact.resolver.simple.maven.plugin

public fun interface SimplePluginProvider {
    public fun provide(
        group: String,
        artifact: String,
        version: SimpleMavenPlugin.VersionDescriptor,
        configuration: SimplePluginConfiguration
    ): SimpleMavenPlugin?
}