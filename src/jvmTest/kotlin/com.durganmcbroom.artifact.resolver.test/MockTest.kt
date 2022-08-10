package com.durganmcbroom.artifact.resolver.test

import com.durganmcbroom.artifact.resolver.*
import com.durganmcbroom.artifact.resolver.group.ResolutionGroup
import com.durganmcbroom.artifact.resolver.group.addResolutionOptionsTransformer
import com.durganmcbroom.artifact.resolver.group.addDescriptionTransformer
import com.durganmcbroom.artifact.resolver.group.graphOf
import kotlin.test.Test

class MockTest {
    @Test
    fun `Test whole system`() {
        val resolver = ArtifactGraph(Mock)

        val processor = resolver.resolverFor(MockRepositorySettings())

        processor.artifactOf("Hey!") {
            // Set resolution options
        }
    }

    @Test
    fun `Test grouping system`() {
        val resolver = ArtifactGraph(ResolutionGroup) {
            graphOf(Mock).register()

            graphOf(SecondMock).register()
        }

        val processor = resolver[Mock]!!.resolverFor(MockRepositorySettings())

        // Will throw a stack overflow, to properly test we need to create a whole new Mock
        println(processor.artifactOf(""))
    }

    class MockRepositorySettings : RepositorySettings()

    data class MockDescriptor(override val name: String) : ArtifactMetadata.Descriptor

    data class MockTransitive(
        override val desc: MockDescriptor, override val resolutionCandidates: List<RepositoryReference<*>>
    ) : ArtifactMetadata.TransitiveInfo

    class MockArtifactMeta(
        desc: MockDescriptor, resource: CheckedResource, transitives: List<MockTransitive>
    ) : ArtifactMetadata<MockDescriptor, MockTransitive>(desc, resource, transitives)

    class MockRepositoryHandler(override val settings: MockRepositorySettings) :
        RepositoryHandler<MockDescriptor, MockArtifactMeta, MockRepositorySettings> {
        override fun descriptorOf(name: String): MockDescriptor = MockDescriptor(name)

        override fun metaOf(descriptor: MockDescriptor): MockArtifactMeta {
            return MockArtifactMeta(
                descriptor, object : CheckedResource {
                    override fun get(): Sequence<Byte> {
                        TODO("Not yet implemented")
                    }
                }, listOf(
                    MockTransitive(
                        MockDescriptor(""), listOf(RepositoryReference(SecondMock, MockRepositorySettings()))
                    )
                )
            )
        }
    }

    class MockResolutionConfig : ArtifactGraphConfig<MockDescriptor, MockArtifactResolutionOptions>(
        MockRepositoryDeReferencer()
    )

    class MockArtifactResolver(
        config: MockResolutionConfig, provider: ArtifactGraphProvider<MockResolutionConfig, *>
    ) : ArtifactGraph<MockResolutionConfig, MockRepositorySettings, MockArtifactProcessor>(
        config, provider
    ) {
        override fun resolverFor(settings: MockRepositorySettings): MockArtifactProcessor = MockArtifactProcessor(
            MockRepositoryHandler(
                MockRepositorySettings()
            ), config.deReferencer, config.graph,
        )

        override fun newRepoSettings(): MockRepositorySettings = MockRepositorySettings()
    }

    class MockArtifactResolutionOptions : ArtifactResolutionOptions()

    class MockArtifactProcessor(
        repository: RepositoryHandler<MockDescriptor, MockArtifactMeta, MockRepositorySettings>,
        val deReferencer: RepositoryDeReferencer<MockDescriptor, MockArtifactResolutionOptions>,
        graphController: ArtifactGraph.GraphController,

        ) : ArtifactGraph.ArtifactResolver<MockDescriptor, MockArtifactMeta, MockRepositorySettings, MockArtifactResolutionOptions>(
        repository, graphController,
    ) {
        override fun emptyOptions(): MockArtifactResolutionOptions = MockArtifactResolutionOptions()
        override fun resolve(
            meta: MockArtifactMeta, options: MockArtifactResolutionOptions, trace: ArtifactRepository.ArtifactTrace?
        ): Artifact {
            return Artifact(meta, meta.transitives.mapNotNull { t ->
                t.resolutionCandidates.firstNotNullOfOrNull {
                    deReferencer.deReference(it)?.artifactOf(t.desc, options, trace)
                }
            })
        }
    }

    class MockRepositoryDeReferencer : RepositoryDeReferencer<MockDescriptor, MockArtifactResolutionOptions> {
        override fun deReference(ref: RepositoryReference<*>): MockArtifactProcessor? = null
    }

    object Mock : ArtifactGraphProvider<MockResolutionConfig, MockArtifactResolver> {
        override val key: ArtifactGraphProvider.Key = MockKey

        override fun provide(config: MockResolutionConfig): MockArtifactResolver = MockArtifactResolver(config, this)

        override fun emptyConfig(): MockResolutionConfig = MockResolutionConfig()

        object MockKey : ArtifactGraphProvider.Key()
    }

    object SecondMock : ArtifactGraphProvider<MockResolutionConfig, MockArtifactResolver> {
        override val key: ArtifactGraphProvider.Key = SecondMockKey

        override fun emptyConfig(): MockResolutionConfig = MockResolutionConfig()

        override fun provide(config: MockResolutionConfig): MockArtifactResolver = MockArtifactResolver(config, this)

        object SecondMockKey : ArtifactGraphProvider.Key()
    }
}
