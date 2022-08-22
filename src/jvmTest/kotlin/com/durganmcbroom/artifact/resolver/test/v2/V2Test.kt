package com.durganmcbroom.artifact.resolver.test.v2

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.right
import com.durganmcbroom.artifact.resolver.v2.*
import kotlin.test.Test

typealias MockArtifactStub = ArtifactStub<V2Test.MockMetadata.MockDescriptor, V2Test.MockRepositoryStub>

typealias MockArtifact = Artifact

typealias MockArtifactReference = ArtifactReference<V2Test.MockMetadata, MockArtifactStub>

class V2Test {
    @Test
    fun `Test Exceptional case`() {
        val repo = MockRepositoryFactory.createNew(MockRepositorySettings("default", "a very real url"))
        val either = repo.get(
            MockArtifactRequest(
                MockMetadata.MockDescriptor("A very real descriptor!"),
                false,
                listOf()
            )
        )

        assert(either.isLeft())
        println(either)
    }

    @Test
    fun `Test mock artifact repository`() {
        val repo = MockRepositoryFactory.createNew(MockRepositorySettings("default", "Even more real"))
        val either = repo.get(
            MockArtifactRequest(
                MockMetadata.MockDescriptor("#defintely-valid"),
                false,
                listOf()
            )
        )

        assert(either.isRight())
        println(either)
    }

    data class MockRepositoryStub(val type: String) : RepositoryStub

    data class MockRepositorySettings(val type: String, val url: String) : RepositorySettings

    class MockMetadata(
        descriptor: MockDescriptor,
        children: List<MockChildInfo>
    ) : ArtifactMetadata<MockMetadata.MockDescriptor, MockMetadata.MockChildInfo>(
        descriptor, children
    ) {
        data class MockDescriptor(override val name: String) : Descriptor
        data class MockChildInfo(
            override val descriptor: MockDescriptor,
            override val candidates: List<Either<MockRepositoryStub, MockRepositorySettings>>
        ) : ChildInfo<MockDescriptor, MockRepositorySettings, MockRepositoryStub>(
            descriptor, candidates
        )
    }

    object MockRepositoryFactory : RepositoryFactory<MockRepositorySettings, MockArtifactReference, MockArtifactRepository> {
        override fun createNew(settings: MockRepositorySettings): MockArtifactRepository = MockArtifactRepository(
            MockMetadataHandler(settings)
        )

        override val stubResolver: StubResolver<*, MockArtifactReference> = MockStubResolver()
    }

    class MockStubResolver : StubResolver<MockArtifactStub, MockArtifactReference> {
        override val factory = MockRepositoryFactory

        override fun resolve(stub: MockArtifactStub): Either<ArtifactException, MockArtifactReference> =
            factory.createNew(MockRepositorySettings("default", "Even more real"))
                .get(
                    MockArtifactRequest(
                        stub.descriptor,
                        false,
                        listOf()
                    )
                )

    }

    class MockArtifactRepository(override val handler: MockMetadataHandler) :
        ArtifactRepository<MockArtifactRequest, MockArtifactReference> {
        override val factory: RepositoryFactory<*, *, *> = MockRepositoryFactory
        override fun get(request: MockArtifactRequest): Either<ArtifactException, MockArtifactReference> = either.eager {
            val meta = handler.requestMetadata(request.descriptor).bind()

            MockArtifactReference(meta, listOf())
        }

    }

    class MockMetadataHandler(override val settings: MockRepositorySettings) :
        MetadataHandler<MockRepositorySettings, MockMetadata.MockDescriptor, MockMetadata> {
        override fun parseDescriptor(desc: String): Either<MetadataRequestException.DescriptorParseFailed, MockMetadata.MockDescriptor> =
            either.eager {
                ensure(!desc.contains("definitely-valid")) { MetadataRequestException.DescriptorParseFailed }

                MockMetadata.MockDescriptor(desc)
            }

        override fun requestMetadata(desc: MockMetadata.MockDescriptor): Either<MetadataRequestException, MockMetadata> =
            either.eager {
                MockMetadata(
                    desc,
                    listOf()
                )
            }

    }


    data class MockArtifactRequest(
        override val descriptor: MockMetadata.MockDescriptor,
        val processTransitive: Boolean,
        val takeScopes: List<String>
    ) : ArtifactRequest
}