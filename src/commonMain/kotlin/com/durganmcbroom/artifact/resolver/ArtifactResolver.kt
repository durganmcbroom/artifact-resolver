@file:JvmName("ArtifactResolver")

package com.durganmcbroom.artifact.resolver

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex

public fun <
        S : RepositorySettings,
        R : ArtifactRequest<*>,
        M : ArtifactMetadata<*, ArtifactMetadata.ParentInfo<R, S>>>
        RepositoryFactory<S, ArtifactRepository<S, R, M>>.createContext(): ResolutionContext<S, R, M> =
    ResolutionContext(
        this
    )

public sealed class ArtifactResolutionException(message: String) : ArtifactException(message) {
    public data class CircularArtifacts(
        val trace: List<ArtifactMetadata.Descriptor>,
    ) : ArtifactResolutionException(
        "Circular artifacts found! Trace was: '${
            trace.joinToString(separator = " -> ") { it.name }
        }'")
}

public open class ResolutionContext<
        S : RepositorySettings,
        R : ArtifactRequest<*>,
        M : ArtifactMetadata<*, ArtifactMetadata.ParentInfo<R, S>>
        >(
    public val factory: RepositoryFactory<S, ArtifactRepository<S, R, M>>
) {
    protected val mutex: Mutex = Mutex()
    protected val cache: MutableMap<Pair<R, S>, Deferred<Artifact<M>?>> = concurrentHashMap()

    public open suspend fun getAndResolveAsync(
        request: R,
        repository: S,
    ): Artifact<M> {
        return getAndResolveAsync(request, listOf(repository), listOf())
    }

    protected open suspend fun getAndResolveAsync(
        request: R,
        candidates: List<S>,

        trace: List<ArtifactMetadata.Descriptor>
    ): Artifact<M> = coroutineScope {
        if (trace.contains(request.descriptor))
            throw ArtifactResolutionException.CircularArtifacts(trace)

        val exceptions = concurrentList<Throwable>()

        val results = try {
            // We lock outside the map because candidates should be unique.
            mutex.lock()
            candidates.map { candidate ->
                cache[request to candidate] ?: run {
                    val job = async {
                        val metadata = runCatching {
                            factory.createNew(candidate).get(request)
                        }.onFailure {
                            exceptions.add(it)
                        }.getOrNull() ?: return@async null

                        val newChildren = metadata.parents
                            .map { child ->
                                getAndResolveAsync(
                                    child.request,
                                    child.candidates,
                                    trace + request.descriptor
                                )
                            }

                        Artifact(
                            metadata,
                            newChildren
                        )
                    }

                    cache[request to candidate] = job

                    job
                }
            }
        } finally {
            mutex.unlock()
        }

        results
            .awaitAll()
            .filterNotNull()
            .firstOrNull() ?: exceptions
            .filter { it !is MetadataRequestException.MetadataNotFound }
            .takeUnless { it.isEmpty() }
            ?.let { throw it.first() }
        ?: throw ArtifactException.ArtifactNotFound(
            request.descriptor,
            candidates,
            trace
        )
    }
}