package com.durganmcbroom.artifact.resolver.mock.maven.test;

import com.durganmcbroom.artifact.resolver.Resolvers;
import com.durganmcbroom.artifact.resolver.mock.maven.MockMavenRepositorySettings;
import com.durganmcbroom.artifact.resolver.mock.maven.MockMaven;
import org.junit.jupiter.api.Test;

public class JavaResolutionTest {
    @Test
    public void testMavenResolution() {
        var resolver = Resolvers.newResolver(MockMaven.INSTANCE);
        final MockMavenRepositorySettings settings = resolver.newSettings();
        settings.useMavenCentral();
        var processor = resolver.processorFor(settings);
        final var options = processor.emptyOptions();
        options.includeScopes("compile", "runtime", "import", "test");
        options.exclude("stax-ex");

        var artifactOrNull = processor.artifactOf("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.6", options);

        assert artifactOrNull != null;
        TestUtilsKt.prettyPrint(artifactOrNull, "   ");
    }
}
