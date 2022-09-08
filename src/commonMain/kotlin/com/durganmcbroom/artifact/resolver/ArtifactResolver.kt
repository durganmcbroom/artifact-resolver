@file:JvmName("ArtifactResolver")

package com.durganmcbroom.artifact.resolver

import arrow.core.Either
import arrow.core.continuations.either

public interface ArtifactRepositoryContext<R : ArtifactRequest<*>, S: ArtifactStub<R, *>, out A : ArtifactReference<*, S>> {
    public val artifactRepository: ArtifactRepository<R, S, A>
}

public interface StubResolverContext<T: ArtifactStub<*, *>, out A: ArtifactReference<*, T>> {
    public val stubResolver: ArtifactStubResolver<*, T, A>
}

public interface ArtifactComposerContext {
    public val artifactComposer: ArtifactComposer
}

public fun <
        S : RepositorySettings,
        Req : ArtifactRequest<*>,
        Stub : ArtifactStub<Req, *>,
        Ref : ArtifactReference<*, Stub>,
        R : ArtifactRepository<Req, Stub, Ref>> RepositoryFactory<S, Req, Stub, Ref, R>.createContext(
    settings: S
): ResolutionContext<Req, Stub, Ref> {
    val repo: R = createNew(settings)
    val resolver = repo.stubResolver
    val composer: ArtifactComposer = artifactComposer

    return ResolutionContext(
        repo, resolver, composer
    )
}

public open class ResolutionContext<R : ArtifactRequest<*>, S : ArtifactStub<R, *>, T : ArtifactReference<*, S>>(
    public val repositoryContext: ArtifactRepositoryContext<R, S, T>,
    public val resolverContext: StubResolverContext<S, T>,
    public val composerContext: ArtifactComposerContext,
) {
    public constructor(
        artifactRepository: ArtifactRepository<R, S, T>,
        stubResolver: ArtifactStubResolver<*, S, T>,
        artifactComposer: ArtifactComposer
    ) : this(object : ArtifactRepositoryContext<R, S, T> {
        override val artifactRepository: ArtifactRepository<R, S, T> = artifactRepository
    }, object : StubResolverContext<S, T> {
        override val stubResolver: ArtifactStubResolver<*, S, T> = stubResolver
    }, object : ArtifactComposerContext {
        override val artifactComposer: ArtifactComposer = artifactComposer
    })

    public fun getAndResolve(request: R): Either<ArtifactException, Artifact> = either.eager {
        val artifact = repositoryContext.artifactRepository.get(request).bind()

        getAndResolve(artifact, HashMap()).bind()
    }

    private fun getAndResolve(
        artifact: T,
        cache: MutableMap<ArtifactMetadata.Descriptor, Artifact>
    ): Either<ArtifactException, Artifact> =
        either.eager {
            val newChildren: List<Either<S, Artifact>> = artifact.children.map { child ->
                resolverContext.stubResolver.resolve(child).bimap({ child }, { it })
            }.map { c ->
                c.map {
                    cache[it.metadata.descriptor] ?: getAndResolve(it, cache).bind()
                        .also { a -> cache[a.metadata.descriptor] = a }
                }
            }

            composerContext.artifactComposer.compose(artifact, newChildren)
        }
}
