package com.durganmcbroom.artifact.resolver.test

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.right
import com.durganmcbroom.artifact.resolver.*
import kotlin.test.Test

typealias MockArtifactStub = ArtifactStub<MockTest.MockArtifactRequest, MockTest.MockRepositoryStub>

typealias MockArtifactReference = ArtifactReference<MockTest.MockMetadata, MockArtifactStub>

class MockTest {
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
                MockMetadata.MockDescriptor("definitely-valid"),
                false,
                listOf()
            )
        )

        check(either.isRight())
        println(either)
    }

    @Test
    fun `Test mock artifact resolution`() {
        MockRepositoryFactory.createResolver(MockRepositorySettings("default", "URL"))
        val factory= MockRepositoryFactory
        val context = factory.createResolver(MockRepositorySettings("", ""))

        val either = context.getAndResolve(MockArtifactRequest(MockMetadata.MockDescriptor("definitely-valid"), true, listOf()))

        check(either.isRight())

        println(either)
    }

    data class MockRepositoryStub(val type: String, val url: String) : RepositoryStub

    data class MockRepositorySettings(val type: String, val url: String) : RepositorySettings

    class MockMetadata(
        descriptor: MockDescriptor,
        children: List<MockChildInfo>
    ) : ArtifactMetadata<MockMetadata.MockDescriptor, MockMetadata.MockChildInfo>(
        descriptor, null, children
    ) {
        data class MockDescriptor(override val name: String) : Descriptor
        data class MockChildInfo(
            override val descriptor: MockDescriptor,
            override val candidates: List<MockRepositoryStub>
        ) : ChildInfo<MockDescriptor, MockRepositoryStub>(
            descriptor, candidates
        )
    }

    object MockRepositoryFactory :
        RepositoryFactory<MockRepositorySettings, MockArtifactRequest, MockArtifactStub, MockArtifactReference, MockArtifactRepository> {
        override fun createNew(settings: MockRepositorySettings): MockArtifactRepository = MockArtifactRepository(
            MockMetadataHandler(settings)
        )


    }

    class MockStubResolver : ArtifactStubResolver<MockRepositoryStub, MockArtifactStub, MockArtifactReference> {
        override val factory = MockRepositoryFactory
        override val repositoryResolver: RepositoryStubResolver<MockRepositoryStub, *>
            get() = RepositoryStubResolver {
                MockRepositorySettings(it.type, it.url).right()
            }


        override fun resolve(stub: MockArtifactStub): Either<ArtifactException, MockArtifactReference> =
            MockRepositoryFactory.createNew(MockRepositorySettings("default", "Even more real"))
                .get(
                    MockArtifactRequest(
                        stub.request.descriptor,
                        false,
                        listOf()
                    )
                )

    }

    class MockArtifactRepository(override val handler: MockMetadataHandler) :
        ArtifactRepository<MockArtifactRequest, MockArtifactStub, MockArtifactReference> {
        override val name: String = "mock"
        override val factory = MockRepositoryFactory
        override val stubResolver: MockStubResolver = MockStubResolver()

        override fun get(
            request: MockArtifactRequest
        ): Either<ArtifactException, MockArtifactReference> = either.eager {
            val meta = handler.requestMetadata(request.descriptor).bind()

            MockArtifactReference(meta, listOf())
        }

    }

    class MockMetadataHandler(override val settings: MockRepositorySettings) :
        MetadataHandler<MockRepositorySettings, MockMetadata.MockDescriptor, MockMetadata> {
        override fun parseDescriptor(desc: String): Either<MetadataRequestException.DescriptorParseFailed, MockMetadata.MockDescriptor> =
            either.eager {

                MockMetadata.MockDescriptor(desc)
            }

        override fun requestMetadata(
            desc: MockMetadata.MockDescriptor
        ): Either<MetadataRequestException, MockMetadata> = either.eager {
            ensure(desc.name.contains("definitely-valid")) { MetadataRequestException.DescriptorParseFailed }

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