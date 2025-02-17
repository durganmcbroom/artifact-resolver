@file:JvmName("ArtifactResolver")

package com.durganmcbroom.artifact.resolver

import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.async.AsyncJob
import com.durganmcbroom.jobs.async.asyncJob
import com.durganmcbroom.jobs.async.mapAsync
import com.durganmcbroom.jobs.job
import com.durganmcbroom.jobs.mapException
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap

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
//    public open val repository: ArtifactRepository<S, R, M>
    public val factory: RepositoryFactory<S, ArtifactRepository<S, R, M>>
) {
    protected val mutex: Mutex = Mutex()
    protected val cache: MutableMap<Pair<R, S>, Deferred<Artifact<M>?>> = concurrentHashMap()

    public open fun getAndResolve(
        request: R,
        repository: S,
    ): Job<Artifact<M>> = job {
        runBlocking(Dispatchers.IO) {
            getAndResolveAsync(request, repository)().merge()
        }
    }

    public open fun getAndResolveAsync(
        request: R,
        repository: S,
    ): AsyncJob<Artifact<M>> = asyncJob {
        getAndResolveAsync(request, listOf(repository), listOf())().merge()
    }

    protected open fun getAndResolveAsync(
//        metadata: M,
        request: R,
        candidates: List<S>,

        trace: List<ArtifactMetadata.Descriptor>
    ): AsyncJob<Artifact<M>> = asyncJob {
        coroutineScope {
            if (trace.contains(request.descriptor))
                throw ArtifactResolutionException.CircularArtifacts(trace)

            val exceptions = concurrentList<Throwable>()

            val results = try {
                // We lock outside the map because candidates should be unique.
                mutex.lock()
                candidates.map { candidate ->
                    cache[request to candidate] ?: run {
                        val job = async {
                            val childMetadata = factory.createNew(candidate).get(request)()

                            val metadata = childMetadata.getOrElse {
                                exceptions.add(it)
                                null
                            } ?: return@async null

                            val newChildren = metadata.parents
                                .map { child ->
                                    getAndResolveAsync(
                                        child.request,
                                        child.candidates,
                                        trace + request.descriptor
                                    )().merge()
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
}