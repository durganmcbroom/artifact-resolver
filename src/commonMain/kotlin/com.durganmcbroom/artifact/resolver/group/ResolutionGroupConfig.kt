package com.durganmcbroom.artifact.resolver.group

import com.durganmcbroom.artifact.resolver.*

public class ResolutionGroupConfig : ArtifactGraphConfig<Nothing, Nothing>() {
    // References from resolution providers to metadata for those resolvers
    private val _refs: MutableMap<ArtifactGraphProvider.Key, ResolverMetadata<*, *>> = HashMap()
    internal val refs: Map<ArtifactGraphProvider.Key, ResolverMetadata<*, *>>
        get() = _refs.toMap()

    // Starts the Resolution Builder
    public fun <D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions, C : ArtifactGraphConfig<D, O>> graphOf(
        provider: ArtifactGraphProvider<C, ArtifactGraph<C, *, ArtifactGraph.ArtifactResolver<D, *, *, O>>>,
    ): ResolutionBuilder<D, O, C> = ResolutionBuilder(provider)

    public inner class ResolutionBuilder<D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions, C : ArtifactGraphConfig<D, O>> internal constructor(
        private val provider: ArtifactGraphProvider<C, ArtifactGraph<C, *, ArtifactGraph.ArtifactResolver<D, *, *, O>>>
    ) {
        // Initialize configuration to be empty
        internal var configuration: C = newConfig()

        // Transformers to ensure compatibility between different resolvers
        private val descriptionTransformers: MutableList<DescriptionTransformer<*, D>> = ArrayList()

        // Same as above but for resolution options.
        private val resolutionOptionsTransformers: MutableList<ResolutionOptionsTransformer<*, O>> = ArrayList()

        // Creates a new config
        public fun newConfig(): C = provider.emptyConfig()

        // Sets a configuration
        public fun configure(configuration: C): ResolutionBuilder<D, O, C> = also {
            this.configuration = configuration
        }

        // Configures and sets a new configuration

        public fun addDescriptionTransformer(dt: DescriptionTransformer<*, D>): ResolutionBuilder<D, O, C> = also {
            descriptionTransformers.add(dt)
        }

        public fun addResolutionOptionsTransformer(dt: ResolutionOptionsTransformer<*, O>): ResolutionBuilder<D, O, C> =
            also {
                resolutionOptionsTransformers.add(dt)
            }

        // Registers this as metadata
        public fun register() {
            // Overrides the deReferencer from the configuration
            val deRefSet = runCatching { configuration.deReferencer = GroupedDeReferencer() }
            val graphSet = runCatching { configuration.graph = graph }

            check(deRefSet.isSuccess) { "Unable to override the deReferencer." }
            check(graphSet.isSuccess) { "Unable to override the artifact graph." }

            // Locks the configuration
            configuration.lock()

            // Creates and set the resolver metadata.
            _refs[provider.key] = ResolverMetadata(
                configuration,
                descriptionTransformers.toList(),
                resolutionOptionsTransformers.toList()
            )
        }
    }

    // Metadata
    public data class ResolverMetadata<D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions> internal constructor(
        val config: ArtifactGraphConfig<D, O>,
        val descTransformers: List<DescriptionTransformer<*, D>>,
        val optionTransformers: List<ResolutionOptionsTransformer<*, O>>
    )

    // The DeReferencer to use for all resolvers
    private inner class GroupedDeReferencer<D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions> :
        RepositoryDeReferencer<D, O> {
        @Suppress("UNCHECKED_CAST")
        override fun deReference(ref: RepositoryReference<*>): RepositoryProxy<D, O, *, *>? {
            // Gets the metadata from the map or returns null if it cannot be found
            val metadata: ResolverMetadata<*, *> = refs[ref.provider.key] ?: return null

            // Gets a new resolver
            val resolver =
                (ref.provider as ArtifactGraphProvider<ArtifactGraphConfig<*, *>, ArtifactGraph<*, RepositorySettings, *>>).provide(
                    metadata.config
                )
            // Gets a new processor
            val processor: ArtifactGraph.ArtifactResolver<*, *, *, *> = resolver.resolverFor(ref.settings)

            // Returns a new repository proxy. This proxy wraps all requests repositories and transforms requests to them (outgoing from the de-references point of view).
            return RepositoryProxy(
                processor as ArtifactGraph.ArtifactResolver<ArtifactMetadata.Descriptor, *, *, ArtifactResolutionOptions>,
                metadata as ResolverMetadata<ArtifactMetadata.Descriptor, ArtifactResolutionOptions>
            )
        }
    }

    // A proxied Artifact Repository which will transform all incoming requests into recognizable formats. The types D and O represent outgoing requests from the caller(which
    // are transformed) while types DT and OT represent incoming requests to teh parent from types D and O.
    private inner class RepositoryProxy<D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions, DT : ArtifactMetadata.Descriptor, OT : ArtifactResolutionOptions>(
        private val parent: ArtifactRepository<DT, OT>,
        private val metadataTo: ResolverMetadata<DT, OT>
    ) : ArtifactRepository<D, O> {
        private fun <T : Any> List<Transformer<*, T>>.transform(toTransform: Any): T? = firstNotNullOfOrNull {
            if (it.typeIn.isInstance(toTransform)) (it as Transformer<Any, T>).transform(toTransform)
            else null
        }

        override fun artifactOf(desc: D, options: O, trace: ArtifactRepository.ArtifactTrace?): Artifact? {
            val newDesc = metadataTo.descTransformers.transform(desc) ?: return null
            val newOptions = metadataTo.optionTransformers.transform(options) ?: return null

            return parent.artifactOf(newDesc, newOptions, trace)
        }
    }
}

public fun <D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions, C : ArtifactGraphConfig<D, O>> ResolutionGroupConfig.ResolutionBuilder<D, O, C>.configure(
    configuration: C.() -> Unit
): ResolutionGroupConfig.ResolutionBuilder<D, O, C> = also {
    this.configuration = newConfig().apply(configuration)
}