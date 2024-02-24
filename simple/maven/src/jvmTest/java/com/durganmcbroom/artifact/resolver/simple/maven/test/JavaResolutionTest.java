package com.durganmcbroom.artifact.resolver.simple.maven.test;

import arrow.core.Either;
import com.durganmcbroom.artifact.resolver.Artifact;
import com.durganmcbroom.artifact.resolver.ArtifactException;
import com.durganmcbroom.artifact.resolver.ArtifactResolver;
import com.durganmcbroom.artifact.resolver.simple.maven.*;
import com.durganmcbroom.resources.ResourceAlgorithm;
import org.junit.jupiter.api.Test;

public class JavaResolutionTest {
    @Test
    public void testMavenResolution() {
//        final var repo = SimpleMaven.INSTANCE.createNew(SimpleMavenRepositorySettings.mavenCentral(
//                ResourceAlgorithm.SHA1
//        ));
//
//        final var context = new SimpleMavenResolutionContext(
//                repo,
//                repo.getStubResolver(),
//                SimpleMaven.INSTANCE.getArtifactComposer()
//        );
//
//        final Either<ArtifactException, Artifact> artifact = context.getAndResolve(new SimpleMavenArtifactRequest("org.springframework:spring-context:5.3.22"));
//
//        assert artifact.isRight();
//
//        final var a = ((Either.Right<Artifact>) artifact).getValue();
//
//        TestUtilsKt.prettyPrint(a, "   ");
    }

    @Test
    public void testPrettyMavenResolution() {
//        final var context = ArtifactResolver.createContext(SimpleMaven.INSTANCE, SimpleMavenRepositorySettings.mavenCentral(
//                HashType.SHA1
//        ));
//
//        final var either = context.getAndResolve(new SimpleMavenArtifactRequest("org.springframework:spring-context:5.3.22"))  ;
//
//        assert either.isRight();
//
//        final var artifact = ((Either.Right<Artifact>) either).getValue();
//
//        TestUtilsKt.prettyPrint(artifact, "   ");
    }
}
