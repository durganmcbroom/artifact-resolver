package com.durganmcbroom.artifact.resolver.simple.maven.test;

import com.durganmcbroom.artifact.resolver.ArtifactGraphs;
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven;
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositorySettings;
import org.junit.jupiter.api.Test;

public class JavaResolutionTest {
    @Test
    public void testMavenResolution() {
        var resolver = ArtifactGraphs.newGraph(SimpleMaven.INSTANCE);
        final SimpleMavenRepositorySettings settings = resolver.newRepoSettings();
        settings.useMavenCentral();
        var processor = resolver.resolverFor(settings);
        final var options = processor.emptyOptions();
        options.includeScopes("compile", "runtime", "import");

        var artifactOrNull = processor.artifactOf("org.springframework:spring-context:5.3.22", options);

        assert artifactOrNull != null;
        TestUtilsKt.prettyPrint(artifactOrNull, "   ");
    }
}
