module durganmcbroom.artifact.resolver {
    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires arrow.core.jvm;

    // Does compile, just no good way to tell intellij that these packages will be present during compile time!
    exports com.durganmcbroom.artifact.resolver;
    exports com.durganmcbroom.artifact.resolver.group;
}