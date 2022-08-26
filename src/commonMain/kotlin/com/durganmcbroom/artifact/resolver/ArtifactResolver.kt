@file:JvmName("ArtifactResolver")

package com.durganmcbroom.artifact.resolver

import arrow.core.Either
import arrow.core.continuations.either

public interface ArtifactRepositoryContext<in R : ArtifactRequest, out A : ArtifactReference<*, *>> {
    public val artifactRepository: ArtifactRepository<R, A>
}

public interface StubResolverContext<T : ArtifactStub<*, *>, out A : ArtifactReference<*, T>> {
    public val stubResolver: ArtifactStubResolver<*, T, A>
}

public interface ArtifactComposerContext {
    public val artifactComposer: ArtifactComposer
}

public fun <S : RepositorySettings, AR : ArtifactRequest, AS : ArtifactStub<AR, *>, A : ArtifactReference<*, AS>, R : ArtifactRepository<AR, A>> RepositoryFactory<S, A, R>.createResolver(
    settings: S
): ResolutionContext<AR, AS, A> {
    val repo = createNew(settings)
    val resolver = repo.stubResolver as ArtifactStubResolver<*, AS, *>
    val composer = artifactComposer

    return ResolutionContext(
        repo, resolver, composer
    )
}

public open class ResolutionContext<R : ArtifactRequest, S : ArtifactStub<*, *>, T : ArtifactReference<*, S>>(
    public val repositoryContext: ArtifactRepositoryContext<R, T>,
    public val resolverContext: StubResolverContext<S, *>,
    public val composerContext: ArtifactComposerContext,
) {
    public constructor(
        artifactRepository: ArtifactRepository<R, T>,
        stubResolver: ArtifactStubResolver<*, S, *>,
        artifactComposer: ArtifactComposer
    ) : this(object : ArtifactRepositoryContext<R, T> {
        override val artifactRepository: ArtifactRepository<R, T> = artifactRepository
    }, object : StubResolverContext<S, ArtifactReference<*, S>> {
        override val stubResolver: ArtifactStubResolver<*, S, ArtifactReference<*, S>> = stubResolver
    }, object : ArtifactComposerContext {
        override val artifactComposer: ArtifactComposer = artifactComposer
    })

    public fun getAndResolve(request: R): Either<ArtifactException, Artifact> = either.eager {
        val artifact = repositoryContext.artifactRepository.get(request).bind()

        getAndResolve(artifact, HashMap()).bind()
    }

    private fun getAndResolve(artifact: T, cache: MutableMap<ArtifactMetadata.Descriptor, Artifact>): Either<ArtifactException, Artifact> =
        either.eager {
            val newChildren: List<Either<S, Artifact>> = artifact.children.map { child ->
                resolverContext.stubResolver.resolve(child).bimap({ child }, { it })
            }.filterIsInstance<Either<S, T>>().map { c ->
                c.map {
                   cache[it.metadata.descriptor] ?: getAndResolve(it, cache).bind().also { a -> cache[a.metadata.descriptor] = a }
                }
            }

            composerContext.artifactComposer.compose(artifact, newChildren)
        }
}
