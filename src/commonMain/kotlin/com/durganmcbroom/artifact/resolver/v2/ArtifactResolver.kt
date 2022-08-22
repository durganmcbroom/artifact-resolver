@file:JvmName("ArtifactResolver")

package com.durganmcbroom.artifact.resolver.v2

import arrow.core.Either
import arrow.core.continuations.either

public interface ArtifactRepositoryContext<in R : ArtifactRequest, out A : ArtifactReference<*, *>> {
    public val artifactRepository: ArtifactRepository<R, A>
}

public interface StubResolverContext<T : ArtifactStub<*, *>, out A : ArtifactReference<*, T>> {
    public val stubResolver: StubResolver<T, A>
}

public interface ArtifactComposerContext {
    public val artifactComposer: ArtifactComposer
}

public data class ResolutionContext<R : ArtifactRequest, S : ArtifactStub<*, *>, T : ArtifactReference<*, S>>(
    val repositoryContext: ArtifactRepositoryContext<R, T>,
    val resolverContext: StubResolverContext<S, *>,
    val composerContext: ArtifactComposerContext,
) {
    public fun getAndResolve(request: R): Either<ArtifactException, Artifact> = either.eager {
        val artifact = repositoryContext.artifactRepository.get(request).bind()

        getAndResolve(artifact).bind()
    }

    private fun getAndResolve(artifact: T): Either<ArtifactException, Artifact> = either.eager {
        val newChildren: List<Either<S, Artifact>> = artifact.children
            .map { child ->
                resolverContext.stubResolver.resolve(child).bimap({ child }, { it })
            }
            .filterIsInstance<Either<S, T>>()
            .map { c -> c.map { getAndResolve(it).bind() } }

        composerContext.artifactComposer.compose(artifact, newChildren)
    }
}
