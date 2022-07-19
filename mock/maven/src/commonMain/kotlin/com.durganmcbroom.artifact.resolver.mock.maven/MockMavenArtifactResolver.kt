package com.durganmcbroom.artifact.resolver.mock.maven

import com.durganmcbroom.artifact.resolver.*
import com.durganmcbroom.artifact.resolver.mock.maven.layout.MockMavenRepositoryLayout

public class MockMavenArtifactResolver(
    config: MockMavenResolutionConfig,
    provider: ResolutionProvider<MockMavenResolutionConfig, *>
) : ArtifactResolver<
        MockMavenResolutionConfig,
        MockMavenRepositorySettings,
        MockMavenArtifactResolver.MockMavenArtifactProcessor>(
    config, provider
) {
    override fun newSettings(): MockMavenRepositorySettings = MockMavenRepositorySettings()

    override fun processorFor(settings: MockMavenRepositorySettings): MockMavenArtifactProcessor =
        MockMavenArtifactProcessor(settings.layout, settings.also(Lockable::lock), config.deReferencer, config.graph)

    public class MockMavenArtifactProcessor internal constructor(
        layout: MockMavenRepositoryLayout,
        settings: MockMavenRepositorySettings,
        private val deReferencer: RepositoryDeReferencer<MavenDescriptor, MockMavenArtifactResolutionOptions>,
        graphController: ArtifactGraph.GraphController
    ) : ArtifactProcessor<
            MavenDescriptor,
            MavenArtifactMeta,
            MockMavenRepositorySettings,
            MockMavenArtifactResolutionOptions>(
        MavenRepositoryHandler(layout, settings), graphController
    ) {
        override fun emptyOptions(): MockMavenArtifactResolutionOptions = MockMavenArtifactResolutionOptions()

        // Waiting for kotlinx coroutines to reach version 1.7 for support of the JPMS
        override fun processArtifact(
            meta: MavenArtifactMeta,
            options: MockMavenArtifactResolutionOptions,
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
            meta: MavenArtifactMeta,
            options: MockMavenArtifactResolutionOptions,
            _trace: ArtifactRepository.ArtifactTrace?
        ): Artifact? {
            val trace = ArtifactRepository.ArtifactTrace(_trace, meta.desc)
            val transitives: List<MavenTransitive> = getTransitives(trace, meta, options)

            val artifacts = transitives.map { t ->
                artifactFromTransitive(t, trace, options)
            }

            return if (artifacts.any { it == null }) null
            else Artifact(meta, artifacts as List<Artifact>)
        }

        private fun artifactFromTransitive(
            t: MavenTransitive,
            trace: ArtifactRepository.ArtifactTrace,
            options: MockMavenArtifactResolutionOptions
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
            meta: MavenArtifactMeta,
            options: MockMavenArtifactResolutionOptions
        ): List<MavenTransitive> {
            if (trace.isCyclic(meta.desc)) throw IllegalStateException("Cyclic artifacts found in trace: $trace")

            val transitives: List<MavenTransitive> =
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
