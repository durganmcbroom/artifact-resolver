package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.*

public open class SimpleMavenArtifactGraph(
    config: SimpleMavenArtifactGraphConfig,
    provider: ArtifactGraphProvider<SimpleMavenArtifactGraphConfig, *>
) : ArtifactGraph<
        SimpleMavenArtifactGraphConfig,
        SimpleMavenRepositorySettings,
        SimpleMavenArtifactGraph.SimpleMavenArtifactResolver>(
    config, provider
) {
    override fun newRepoSettings(): SimpleMavenRepositorySettings = SimpleMavenRepositorySettings()

    override fun resolverFor(settings: SimpleMavenRepositorySettings): SimpleMavenArtifactResolver =
        SimpleMavenArtifactResolver(
            SimpleMavenRepositoryHandler(settings.layout, settings.also(Lockable::lock)),
            config.deReferencer,
            config.graph
        )

    public open class SimpleMavenArtifactResolver(
        handler: SimpleMavenRepositoryHandler,
        private val deReferencer: RepositoryDeReferencer<SimpleMavenDescriptor, SimpleMavenArtifactResolutionOptions>,
        graphController: GraphController
    ) : ArtifactResolver<
            SimpleMavenDescriptor,
            SimpleMavenArtifactMetadata,
            SimpleMavenRepositorySettings,
            SimpleMavenArtifactResolutionOptions>(
        handler, graphController
    ) {
        override fun emptyOptions(): SimpleMavenArtifactResolutionOptions = SimpleMavenArtifactResolutionOptions()

        // Waiting for kotlinx coroutines to reach version 1.7 for support of the JPMS
        override fun resolve(
            metadata: SimpleMavenArtifactMetadata,
            options: SimpleMavenArtifactResolutionOptions,
            trace: ArtifactRepository.ArtifactTrace?
        ): Artifact? = artifactOfSync(metadata, options.also(Lockable::lock), trace)
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
            metadata: SimpleMavenArtifactMetadata,
            options: SimpleMavenArtifactResolutionOptions,
            _trace: ArtifactRepository.ArtifactTrace?
        ): Artifact? {
            val trace = ArtifactRepository.ArtifactTrace(_trace, metadata.desc)
            val childrenInfo: List<SimpleMavenChildInfo> = getChildrenInfo(trace, metadata, options)

            val artifacts: List<Artifact?> = childrenInfo.map { t ->
                artifactFromChildInfo(t, trace, options)
            }

            return if (artifacts.any { it == null }) null
            else Artifact(metadata, artifacts as List<Artifact>)
        }

        private fun artifactFromChildInfo(
            t: SimpleMavenChildInfo,
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

        private fun getChildrenInfo(
            trace: ArtifactRepository.ArtifactTrace,
            metadata: SimpleMavenArtifactMetadata,
            options: SimpleMavenArtifactResolutionOptions
        ): List<SimpleMavenChildInfo> {
            if (trace.isCyclic(metadata.desc)) throw IllegalStateException("Cyclic artifacts found in trace: $trace")

            val children: List<SimpleMavenChildInfo> =
                if (!options.isTransitive) listOf()
                else metadata.children.filterNot {
                    options.excludes.contains(it.desc.artifact)
                }.filter {
                    options.includeScopes.contains(it.scope) || options.includeScopes.isEmpty()
                }
            return children
        }
    }
}

//public expect fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T
