package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.ArtifactComposer
import com.durganmcbroom.artifact.resolver.ResolutionContext

public class SimpleMavenResolutionContext public constructor(
    repo: SimpleMavenArtifactRepository,
    resolver: SimpleMavenArtifactStubResolver,
    composer: ArtifactComposer
) : ResolutionContext<SimpleMavenArtifactRequest, SimpleMavenArtifactStub, SimpleMavenArtifactReference>(
    repo,
    resolver,
    composer
)