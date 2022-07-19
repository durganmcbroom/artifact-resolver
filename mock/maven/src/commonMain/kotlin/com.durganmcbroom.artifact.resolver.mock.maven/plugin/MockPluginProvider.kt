package com.durganmcbroom.artifact.resolver.mock.maven.plugin

public fun interface MockPluginProvider {
    public fun provide(
        group: String,
        artifact: String,
        version: MockMavenPlugin.VersionDescriptor,
        configuration: MockPluginConfiguration
    ): MockMavenPlugin?
}