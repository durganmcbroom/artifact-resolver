module artifact.resolver.simple.maven {
    requires kotlin.stdlib;
    requires durganmcbroom.artifact.resolver;
    requires com.fasterxml.jackson.kotlin;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires arrow.core.jvm;

    exports com.durganmcbroom.artifact.resolver.simple.maven;
    exports com.durganmcbroom.artifact.resolver.simple.maven.layout;
    exports com.durganmcbroom.artifact.resolver.simple.maven.plugin;
    exports com.durganmcbroom.artifact.resolver.simple.maven.pom;

    opens com.durganmcbroom.artifact.resolver.simple.maven.pom to com.fasterxml.jackson.databind;
}