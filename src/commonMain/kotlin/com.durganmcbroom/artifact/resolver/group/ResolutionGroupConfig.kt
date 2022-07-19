package com.durganmcbroom.artifact.resolver.group

import com.durganmcbroom.artifact.resolver.*

public class ResolutionGroupConfig : ArtifactResolutionConfig<Nothing, Nothing>(
) {
    // References from resolution providers to meta for those resolvers
    private val _refs: MutableMap<ResolutionProvider.Key, ResolverMeta<*, *>> = HashMap()
    internal val refs: Map<ResolutionProvider.Key, ResolverMeta<*, *>>
        get() = _refs.toMap()

    // Starts the Resolution Builder
    public fun <D : ArtifactMeta.Descriptor, O : ArtifactResolutionOptions, C : ArtifactResolutionConfig<D, O>> resolver(
        provider: ResolutionProvider<C, ArtifactResolver<C, *, ArtifactResolver.ArtifactProcessor<D, *, *, O>>>,
    ): ResolutionBuilder<D, O, C> = ResolutionBuilder(provider)

    public inner class ResolutionBuilder<D : ArtifactMeta.Descriptor, O : ArtifactResolutionOptions, C : ArtifactResolutionConfig<D, O>> internal constructor(
        private val provider: ResolutionProvider<C, ArtifactResolver<C, *, ArtifactResolver.ArtifactProcessor<D, *, *, O>>>
    ) {
        // Initialize configuration to be empty
        private var configuration: C = newConfig()

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
        public fun configure(configuration: C.() -> Unit): ResolutionBuilder<D, O, C> = also {
            this.configuration = newConfig().apply(configuration)
        }

        public fun addDescriptionTransformer(dt: DescriptionTransformer<*, D>): ResolutionBuilder<D, O, C> = also {
            descriptionTransformers.add(dt)
        }

        public fun addResolutionOptionsTransformer(dt: ResolutionOptionsTransformer<*, O>): ResolutionBuilder<D, O, C> =
            also {
                resolutionOptionsTransformers.add(dt)
            }

        // Registers this as meta
        public fun register() {
            // Overrides the deReferencer from the configuration
            val deRefSet = runCatching { configuration.deReferencer = GroupedDeReferencer() }
            check(deRefSet.isSuccess) { "Unable to override the deReferencer!" }

            // Locks the configuration
            configuration.lock()

            // Creates and set the resolver meta.
            _refs[provider.key] = ResolverMeta(
                configuration,
                descriptionTransformers.toList(),
                resolutionOptionsTransformers.toList()
            )
        }
    }

    // Meta
    public data class ResolverMeta<D : ArtifactMeta.Descriptor, O : ArtifactResolutionOptions> internal constructor(
        val config: ArtifactResolutionConfig<D, O>,
        val descTransformers: List<DescriptionTransformer<*, D>>,
        val optionTransformers: List<ResolutionOptionsTransformer<*, O>>
    )

    // The DeReferencer to use for all resolvers
    private inner class GroupedDeReferencer<D : ArtifactMeta.Descriptor, O : ArtifactResolutionOptions>(
//        private val metaFrom: ResolverMeta<D, O>
    ) : RepositoryDeReferencer<D, O> {
        @Suppress("UNCHECKED_CAST")
        override fun deReference(ref: RepositoryReference<*>): RepositoryProxy<D, O, *, *>? {
            // Gets the meta from the map or returns null if it cannot be found
            val meta: ResolverMeta<*, *> = refs[ref.provider.key] ?: return null

            // Gets a new resolver
            val resolver =
                (ref.provider as ResolutionProvider<ArtifactResolutionConfig<*, *>, ArtifactResolver<*, RepositorySettings, *>>).provide(
                    meta.config
                )
            // Gets a new processor
            val processor: ArtifactResolver.ArtifactProcessor<*, *, *, *> = resolver.processorFor(ref.settings)

            // Returns a new repository proxy. This proxy wraps all requests repositories and transforms requests to them (outgoing from the de-references point of view).
            return RepositoryProxy(
                processor as ArtifactResolver.ArtifactProcessor<ArtifactMeta.Descriptor, *, *, ArtifactResolutionOptions>,
                meta as ResolverMeta<ArtifactMeta.Descriptor, ArtifactResolutionOptions>
            )
        }
    }

    // A proxied Artifact Repository which will transform all incoming requests into recognizable formats. The types D and O represent outgoing requests from the caller(which
    // are transformed) while types DT and OT represent incoming requests to teh parent from types D and O.
    private inner class RepositoryProxy<D : ArtifactMeta.Descriptor, O : ArtifactResolutionOptions, DT : ArtifactMeta.Descriptor, OT : ArtifactResolutionOptions>(
        private val parent: ArtifactRepository<DT, OT>,
        private val metaTo: ResolverMeta<DT, OT>
    ) : ArtifactRepository<D, O> {
        private fun <T : Any> List<Transformer<*, T>>.transform(toTransform: Any): T? = firstNotNullOfOrNull {
            if (it.typeIn.isInstance(toTransform)) (it as Transformer<Any, T>).transform(toTransform)
            else null
        }

        override fun artifactOf(desc: D, options: O, trace: ArtifactRepository.ArtifactTrace?): Artifact? {
            val newDesc = metaTo.descTransformers.transform(desc) ?: return null
            val newOptions = metaTo.optionTransformers.transform(options) ?: return null

            return parent.artifactOf(newDesc, newOptions, trace)
        }
    }
}