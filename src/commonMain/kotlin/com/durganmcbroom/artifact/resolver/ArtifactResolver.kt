@file:JvmName("ArtifactResolver")

package com.durganmcbroom.artifact.resolver

import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.async.AsyncJob
import com.durganmcbroom.jobs.async.asyncJob
import com.durganmcbroom.jobs.async.mapAsync
import com.durganmcbroom.jobs.job
import com.durganmcbroom.jobs.mapException
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

public fun <
        S : RepositorySettings,
        R : ArtifactRequest<*>,
        M : ArtifactMetadata<*, ArtifactMetadata.ParentInfo<R, S>>>
        RepositoryFactory<S, ArtifactRepository<S, R, M>>.createContext(
    settings: S,
): ResolutionContext<S, R, M> = ResolutionContext(
    createNew(settings)
)

public sealed class ArtifactResolutionException(message: String) : ArtifactException(message) {
    public data class CircularArtifacts(
        val trace: List<ArtifactMetadata.Descriptor>,
    ) : ArtifactResolutionException("Circular artifacts found! Trace was: '${
        trace.joinToString(separator = " -> ") { it.name }
    }'")
}

public open class ResolutionContext<
        S : RepositorySettings,
        R : ArtifactRequest<*>,
        M : ArtifactMetadata<*, ArtifactMetadata.ParentInfo<R, S>>
        >(
    public open val repository: ArtifactRepository<S, R, M>
) {
    public open fun getAndResolve(
        request: R,
    ): Job<Artifact<M>> = job {
        runBlocking(Dispatchers.IO) {
            getAndResolveAsync(request)().merge()
        }
    }

    public open fun getAndResolveAsync(
        request: R,
    ): AsyncJob<Artifact<M>> = asyncJob {
        val artifact = repository.get(request)().mapException {
            if (it is MetadataRequestException.MetadataNotFound) ArtifactException.ArtifactNotFound(
                request.descriptor,
                listOf(repository.settings),
                listOf(),
                it
            ) else it
        }.merge()

        getAndResolveAsync(artifact, ConcurrentHashMap(), listOf())().merge()
    }

    protected open fun getAndResolveAsync(
        metadata: M,
        cache: MutableMap<R, Deferred<Artifact<M>>>,
        trace: List<ArtifactMetadata.Descriptor>
    ): AsyncJob<Artifact<M>> = asyncJob {
        coroutineScope {
            val newChildren = metadata.parents
                .map { child ->
                    if (trace.contains(child.request.descriptor)) throw ArtifactResolutionException.CircularArtifacts(
                        trace + metadata.descriptor
                    )

                    cache[child.request] ?: async {
                        val exceptions = mutableListOf<Throwable>()

                        val childMetadata = child.candidates.firstNotNullOfOrNull { candidate ->
                            val childMetadata = repository.factory.createNew(candidate).get(child.request)()

                            childMetadata.getOrElse {
                                exceptions.add(it)
                                null
                            }
                        } ?: exceptions
                            .filter { it !is MetadataRequestException.MetadataNotFound }
                            .takeUnless { it.isEmpty() }
                            ?.let { throw it.first() }
                        ?: throw ArtifactException.ArtifactNotFound(
                            child.request.descriptor,
                            child.candidates,
                            trace
                        )

                        getAndResolveAsync(childMetadata, cache, trace + child.request.descriptor)().merge()
                    }.also {
                        cache[child.request] = it
                    }
                }

            Artifact(
                metadata,
                newChildren.awaitAll()
            )
        }
    }
}
