@file:JvmName("ArtifactResolver")

package com.durganmcbroom.artifact.resolver

import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.job
import com.durganmcbroom.jobs.mapException

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
    public fun getAndResolve(
        request: R,
    ): Job<Artifact<M>> = job {
        val artifact = repository.get(request)().mapException {
            if (it is MetadataRequestException.MetadataNotFound) ArtifactException.ArtifactNotFound(
                request.descriptor,
                listOf(repository.settings),
                listOf(),
                it
            ) else it
        }.merge()

        getAndResolve(artifact, HashMap(), listOf())().merge()
    }

    private fun getAndResolve(
        metadata: M,
        cache: MutableMap<R, Artifact<M>>,
        trace: List<ArtifactMetadata.Descriptor>
    ): Job<Artifact<M>> = job {
        val newChildren = metadata.parents
            .map { child ->
                if (trace.contains(child.request.descriptor)) throw ArtifactResolutionException.CircularArtifacts(trace + metadata.descriptor)
                cache[child.request] ?: run {
                    val exceptions = mutableListOf<Throwable>()

                    val childMetadata = child.candidates.firstNotNullOfOrNull { candidate ->
                        val childMetadata = repository.factory.createNew(candidate).get(child.request)()

                        childMetadata.getOrElse {
                            exceptions.add(it)
                            null
                        }
                    } ?: if (exceptions.all { it is MetadataRequestException.MetadataNotFound }) {
                        throw ArtifactException.ArtifactNotFound(
                            child.request.descriptor,
                            child.candidates,
                            trace
                        )
                    } else {
                        throw IterableException(
                            "Failed to resolve '${child.request.descriptor}'", exceptions
                        )
                    }

                    getAndResolve(childMetadata, cache, trace + child.request.descriptor)().merge().also {
                        cache[child.request] = it
                    }
                }
            }

        Artifact(
            metadata,
            newChildren,
        )
    }
}
