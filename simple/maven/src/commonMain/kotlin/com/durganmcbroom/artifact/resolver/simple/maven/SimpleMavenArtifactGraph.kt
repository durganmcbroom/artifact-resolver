package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.*
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout

public class SimpleMavenArtifactGraph(
    config: SimpleMavenResolutionConfig,
    provider: ArtifactGraphProvider<SimpleMavenResolutionConfig, *>
) : ArtifactGraph<
        SimpleMavenResolutionConfig,
        SimpleMavenRepositorySettings,
        SimpleMavenArtifactGraph.SimpleMavenArtifactResolver>(
    config, provider
) {
    override fun newRepoSettings(): SimpleMavenRepositorySettings = SimpleMavenRepositorySettings()

    override fun resolverFor(settings: SimpleMavenRepositorySettings): SimpleMavenArtifactResolver =
        SimpleMavenArtifactResolver(settings.layout, settings.also(Lockable::lock), config.deReferencer, config.graph)

    public class SimpleMavenArtifactResolver internal constructor(
        layout: SimpleMavenRepositoryLayout,
        settings: SimpleMavenRepositorySettings,
        private val deReferencer: RepositoryDeReferencer<SimpleMavenDescriptor, SimpleMavenArtifactResolutionOptions>,
        graphController: GraphController
    ) : ArtifactResolver<
            SimpleMavenDescriptor,
            SimpleMavenArtifactMeta,
            SimpleMavenRepositorySettings,
            SimpleMavenArtifactResolutionOptions>(
        SimpleMavenRepositoryHandler(layout, settings), graphController
    ) {
        override fun emptyOptions(): SimpleMavenArtifactResolutionOptions = SimpleMavenArtifactResolutionOptions()

        // Waiting for kotlinx coroutines to reach version 1.7 for support of the JPMS
        override fun resolve(
            meta: SimpleMavenArtifactMeta,
            options: SimpleMavenArtifactResolutionOptions,
            trace: ArtifactRepository.ArtifactTrace?
        ): Artifact? =artifactOfSync(meta, options.also(Lockable::lock), trace)
//            if (options.processAsync)
//            runBlocking { artifactOfAsync(meta, options, trace) }
//        else artifactOfSync(meta, options, trace)

//        private suspend fun artifactOfAsync(
//            meta: MavenArtifactMeta,
//            options: MockMavenArtifactResolutionOptions,
//            _trace: ArtifactRepository.ArtifactTrace?
//        ): Artifact? {
//            val trace = ArtifactRepository.ArtifactTrace(_trace, meta.desc)
//            val transitives: List<MavenTransitive> = getTransitives(trace, meta, options)
//
//            val artifacts = transitives.map { t ->
//                coroutineScope {
//                    async {
//                        artifactFromTransitive(t, trace, options)
//                    }
//                }
//            }.onEach(Deferred<Artifact?>::start).map { it.await() }
//
//            return if (artifacts.any { it == null }) null
//            else Artifact(meta, artifacts as List<Artifact>)
//        }

        private fun artifactOfSync(
            meta: SimpleMavenArtifactMeta,
            options: SimpleMavenArtifactResolutionOptions,
            _trace: ArtifactRepository.ArtifactTrace?
        ): Artifact? {
            val trace = ArtifactRepository.ArtifactTrace(_trace, meta.desc)
            val transitives: List<SimpleMavenTransitive> = getTransitives(trace, meta, options)

            val artifacts = transitives.map { t ->
                artifactFromTransitive(t, trace, options)
            }

            return if (artifacts.any { it == null }) null
            else Artifact(meta, artifacts as List<Artifact>)
        }

        private fun artifactFromTransitive(
            t: SimpleMavenTransitive,
            trace: ArtifactRepository.ArtifactTrace,
            options: SimpleMavenArtifactResolutionOptions
        ): Artifact? {
            check(t.resolutionCandidates.isNotEmpty()) { "Resolution candidates must not be empty, trace was: $trace" }

            return t.resolutionCandidates
                .mapNotNull(deReferencer::deReference)
                .firstNotNullOfOrNull {
                    it.artifactOf(t.desc, options, trace)
                }
        }

        private fun getTransitives(
            trace: ArtifactRepository.ArtifactTrace,
            meta: SimpleMavenArtifactMeta,
            options: SimpleMavenArtifactResolutionOptions
        ): List<SimpleMavenTransitive> {
            if (trace.isCyclic(meta.desc)) throw IllegalStateException("Cyclic artifacts found in trace: $trace")

            val transitives: List<SimpleMavenTransitive> =
                if (!options.isTransitive) listOf()
                else meta.transitives.filterNot {
                    options.excludes.contains(it.desc.artifact)
                }.filter {
                    options.includeScopes.contains(it.scope) || options.includeScopes.isEmpty()
                }
            return transitives
        }
    }
}

//public expect fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T
