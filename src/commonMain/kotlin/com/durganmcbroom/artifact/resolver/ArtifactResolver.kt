@file:JvmName("ArtifactResolver")

package com.durganmcbroom.artifact.resolver

import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.job
import com.durganmcbroom.jobs.mapException

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

    public fun getAndResolve(request: R): Job<Artifact<M>> = job {
        val artifact = repositoryContext.artifactRepository.get(request)().mapException {
            if (it is MetadataRequestException.MetadataNotFound) ArtifactException.ArtifactNotFound(
                request.descriptor,
                listOf(repositoryContext.artifactRepository.handler.settings.toString()),
                it
            ) else it
        }.merge()

        getAndResolve(artifact, HashMap(), setOf())().merge()
    }

    private fun getAndResolve(
        artifact: T,
        cache: MutableMap<R, Artifact<M>>,
        trace: Set<ArtifactMetadata.Descriptor>
    ): Job<Artifact<M>> = job {
        val newChildren = artifact.children.mapNotNull { cache[it.request] } + artifact.children
            .filterNot { cache.contains(it.request) }
            .map { child ->
                if (trace.contains(child.request.descriptor)) throw ArtifactResolutionException.CircularArtifacts(trace + artifact.metadata.descriptor)

                resolverContext.stubResolver.resolve(child)()
                    .map { it to child.request }
                    .mapException {
                        if (it is MetadataRequestException.MetadataNotFound)
                            ArtifactException.ArtifactNotFound(
                                child.request.descriptor,
                                child.candidates.map { it.name }, it
                            ) else it
                    }.merge()
            }.map { (it, req) ->
                getAndResolve(
                    it,
                    cache,
                    trace + artifact.metadata.descriptor
                )().merge().also { a -> cache[req] = a }
            }

        composerContext.artifactComposer.compose(artifact, newChildren)
    }
}
