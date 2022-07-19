package com.durganmcbroom.artifact.resolver.test

import com.durganmcbroom.artifact.resolver.*
import com.durganmcbroom.artifact.resolver.group.ResolutionGroup
import com.durganmcbroom.artifact.resolver.group.addResolutionOptionsTransformer
import com.durganmcbroom.artifact.resolver.group.addDescriptionTransformer
import kotlin.test.Test

class MockTest {
    @Test
    fun `Test whole system`() {
        val resolver = ArtifactResolver(Mock) {
            // set options here
//            install(object : RepositoryProvider<MockRepositorySettings, MockRepositoryHandler> {
//                override fun get(settings: MockRepositorySettings): MockRepositoryHandler {
//                    TODO("Not yet implemented")
//                }
//            })
        }

        val processor = resolver.processorFor(MockRepositorySettings())

        processor.artifactOf("Hey!") {
            // Set resolution options
        }
    }

    @Test
    fun `Test grouping system`() {
        val resolver = ArtifactResolver(ResolutionGroup) {
            resolver(Mock).register()

            resolver(SecondMock).addDescriptionTransformer(MockDescriptor::class, MockDescriptor::class) {
                it
            }.addResolutionOptionsTransformer(
                MockArtifactResolutionOptions::class,
                MockArtifactResolutionOptions::class
            ) {
                it
            }.register()
        }

        val processor = resolver[Mock]!!.processorFor(MockRepositorySettings())

        // Will throw a stack overflow, to properly test we need to create a whole new Mock
        println(processor.artifactOf(""))
    }

    private class MockRepositorySettings : RepositorySettings()

    private data class MockDescriptor(override val name: String) : ArtifactMeta.Descriptor

    private data class MockTransitive(
        override val desc: MockDescriptor,
        override val resolutionCandidates: List<RepositoryReference<*>>
    ) : ArtifactMeta.Transitive

    private class MockArtifactMeta(
        desc: Descriptor,
        resource: CheckedResource, transitives: List<MockTransitive>
    ) : ArtifactMeta<MockDescriptor, MockTransitive>(desc, resource, transitives)

    private class MockRepositoryHandler(override val settings: MockRepositorySettings) :
        RepositoryHandler<MockDescriptor, MockArtifactMeta, MockRepositorySettings> {
        override fun descriptorOf(name: String): MockDescriptor = MockDescriptor(name)

        override fun metaOf(descriptor: MockDescriptor): MockArtifactMeta {
            return MockArtifactMeta(
                descriptor,
                object : CheckedResource {
                    override fun get(): Sequence<Byte> {
                        TODO("Not yet implemented")
                    }
                },
                listOf(
                    MockTransitive(
                        MockDescriptor(""),
                        listOf(RepositoryReference(SecondMock, MockRepositorySettings()))
                    )
                )
            )
        }
    }

    private class MockResolutionConfig :
        ArtifactResolutionConfig<MockDescriptor, MockArtifactResolutionOptions>(
            MockRepositoryDeReferencer()
        )

    private class MockArtifactResolver(
        config: MockResolutionConfig,
        provider: ResolutionProvider<MockResolutionConfig, *>
    ) :
        ArtifactResolver<MockResolutionConfig, MockDescriptor, MockArtifactMeta, MockRepositorySettings, MockArtifactProcessor>(
            config, provider
        ) {
        override fun processorFor(settings: MockRepositorySettings): MockArtifactProcessor = MockArtifactProcessor(
            MockRepositoryHandler(
                MockRepositorySettings()
            ), config.deReferencer!!
        )

        override fun newSettings(): MockRepositorySettings = MockRepositorySettings()
    }

    private class MockArtifactResolutionOptions : ArtifactResolutionOptions()

    private class MockArtifactProcessor(
        repository: RepositoryHandler<MockDescriptor, MockArtifactMeta, MockRepositorySettings>,
        private val deReferencer: RepositoryDeReferencer<MockDescriptor, MockArtifactResolutionOptions>
    ) : ArtifactResolver.ArtifactProcessor<MockDescriptor, MockArtifactMeta, MockRepositorySettings, MockArtifactResolutionOptions>(
        repository
    ) {
        override fun emptyOptions(): MockArtifactResolutionOptions = MockArtifactResolutionOptions()
        override fun artifactOf(meta: MockArtifactMeta, options: MockArtifactResolutionOptions): Artifact {
            return Artifact(meta, meta.transitives.mapNotNull { t ->
                t.resolutionCandidates.firstNotNullOfOrNull {
                    deReferencer.deReference(it)?.artifactOf(t.desc, options)
                }
            })
        }
    }

    private class MockRepositoryDeReferencer : RepositoryDeReferencer<MockDescriptor, MockArtifactResolutionOptions> {
        override fun deReference(ref: RepositoryReference<*>): MockArtifactProcessor? = null
    }

    private object Mock : ResolutionProvider<MockResolutionConfig, MockArtifactResolver> {
        override val key: ResolutionProvider.Key = MockKey

        override fun provide(config: MockResolutionConfig): MockArtifactResolver = MockArtifactResolver(config, this)

        override fun emptyConfig(): MockResolutionConfig = MockResolutionConfig()

        private object MockKey : ResolutionProvider.Key()
    }

    private object SecondMock : ResolutionProvider<MockResolutionConfig, MockArtifactResolver> {
        override val key: ResolutionProvider.Key = SecondMockKey

        override fun emptyConfig(): MockResolutionConfig = MockResolutionConfig()

        override fun provide(config: MockResolutionConfig): MockArtifactResolver = MockArtifactResolver(config, this)

        private object SecondMockKey : ResolutionProvider.Key()
    }
}