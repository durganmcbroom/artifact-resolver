module artifact.resolver.mock.maven {
    requires kotlin.stdlib;
    requires durganmcbroom.artifact.resolver;
    requires com.fasterxml.jackson.kotlin;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
//    requires kotlinx.coroutines.core.jvm;

    exports com.durganmcbroom.artifact.resolver.mock.maven;
    exports com.durganmcbroom.artifact.resolver.mock.maven.layout;
    exports com.durganmcbroom.artifact.resolver.mock.maven.plugin;
    exports com.durganmcbroom.artifact.resolver.mock.maven.pom;
}