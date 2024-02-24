@file:JvmName("ArtifactResolver")

package com.durganmcbroom.artifact.resolver

import com.durganmcbroom.jobs.JobResult
import com.durganmcbroom.jobs.job
import com.durganmcbroom.jobs.jobScope

public interface ArtifactRepositoryContext<R : ArtifactRequest<*>, S : ArtifactStub<R, *>, out A : ArtifactReference<*, S>> {
    public val artifactRepository: ArtifactRepository<R, S, A>
}

public interface StubResolverContext<T : ArtifactStub<*, *>, out A : ArtifactReference<*, T>> {
    public val stubResolver: ArtifactStubResolver<*, T, A>
}

public interface ArtifactComposerContext {
    public val artifactComposer: ArtifactComposer
}

public fun <
        S : RepositorySettings,
        Req : ArtifactRequest<*>,
        Stub : ArtifactStub<Req, *>,
        M : ArtifactMetadata<*, *>,
        Ref : ArtifactReference<M, Stub>,
        R : ArtifactRepository<Req, Stub, Ref>> RepositoryFactory<S, Req, Stub, Ref, R>.createContext(
    settings: S
): ResolutionContext<Req, Stub, M, Ref> {
    val repo: R = createNew(settings)
    val resolver = repo.stubResolver
    val composer: ArtifactComposer = artifactComposer

    return ResolutionContext(
        repo, resolver, composer
    )
}

public sealed class ArtifactResolutionException(message: String) : ArtifactException(message) {
    public data class CircularArtifacts(
        val trace: Set<ArtifactMetadata.Descriptor>,
    ) : ArtifactResolutionException("Circular artifacts found! Trace was: '${
        trace.joinToString(separator = " -> ") { it.name }
    }'")
}

public open class ResolutionContext<R : ArtifactRequest<*>, S : ArtifactStub<R, *>, M : ArtifactMetadata<*, *>, T : ArtifactReference<M, S>>(
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

    public suspend fun getAndResolve(request: R): JobResult<Artifact<M>, ArtifactException> = jobScope {
        val artifact = repositoryContext.artifactRepository.get(request).bind()

        getAndResolve(artifact, HashMap(), setOf()).bind()
    }

    private suspend fun getAndResolve(
        artifact: T,
        cache: MutableMap<R, Artifact<M>>,
        trace: Set<ArtifactMetadata.Descriptor>
    ): JobResult<Artifact<M>, ArtifactException> = job {
        val newChildren = artifact.children.mapNotNull { cache[it.request] } + artifact.children
            .filterNot { cache.contains(it.request) }
            .map { child ->
                if (trace.contains(child.request.descriptor)) raise(ArtifactResolutionException.CircularArtifacts(trace + artifact.metadata.descriptor))

                resolverContext.stubResolver.resolve(child)
                    .mapLeft { child }
                    .map { it to child.request }
            }.map { c ->
                c.map { (it, req) ->
                    getAndResolve(
                        it,
                        cache,
                        trace + artifact.metadata.descriptor
                    ).bind().also { a -> cache[req] = a }
                }.mapLeft { ArtifactException.ArtifactNotFound(it.request.descriptor, it.candidates.map { it.name }) }
                    .bind()
            }

        composerContext.artifactComposer.compose(artifact, newChildren)
    }
}
