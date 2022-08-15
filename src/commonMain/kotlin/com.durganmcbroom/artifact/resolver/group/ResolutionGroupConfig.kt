package com.durganmcbroom.artifact.resolver.group

import com.durganmcbroom.artifact.resolver.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

public class ResolutionGroupConfig : ArtifactGraphConfig<Nothing, Nothing>() {
    // References from resolution providers to meta for those resolvers
    private val _refs: MutableMap<ArtifactGraphProvider.Key, ResolverMeta<*, *>> = HashMap()
    internal val refs: Map<ArtifactGraphProvider.Key, ResolverMeta<*, *>>
        get() = _refs.toMap()

    // Starts the Resolution Builder
    public fun <D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions, C : ArtifactGraphConfig<D, O>> graphOf(
        provider: ArtifactGraphProvider<C, ArtifactGraph<C, *, ArtifactGraph.ArtifactResolver<D, *, *, O>>>,
        descType: KClass<D>,
        optionsType: KClass<O>
    ): ResolutionBuilder<D, O, C> = ResolutionBuilder(provider, descType, optionsType)

    public fun <D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions, C : ArtifactGraphConfig<D, O>> graphOf(
        provider: ArtifactGraphProvider<C, ArtifactGraph<C, *, ArtifactGraph.ArtifactResolver<D, *, *, O>>>,
        descType: Class<D>,
        optionsType: Class<O>
    ): ResolutionBuilder<D, O, C> = ResolutionBuilder(provider, descType.kotlin, optionsType.kotlin)

    public inner class ResolutionBuilder<D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions, C : ArtifactGraphConfig<D, O>> internal constructor(
        private val provider: ArtifactGraphProvider<C, ArtifactGraph<C, *, ArtifactGraph.ArtifactResolver<D, *, *, O>>>,
        private val descType: KClass<D>,
        private val optionsType: KClass<O>
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

        // Registers this as meta
        public fun register() {
            // Overrides the deReferencer from the configuration
            val deRefSet = runCatching { configuration.deReferencer = GroupedDeReferencer() }
            val graphSet = runCatching { configuration.graph = graph }

            check(deRefSet.isSuccess) { "Unable to override the deReferencer." }
            check(graphSet.isSuccess) { "Unable to override the artifact graph." }

            // Locks the configuration
            configuration.lock()

            // Creates and set the resolver meta.
            _refs[provider.key] = ResolverMeta(
                configuration, descriptionTransformers.toList(), resolutionOptionsTransformers.toList(),
                descType, optionsType
            )
        }
    }

    // Meta
    public data class ResolverMeta<D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions> internal constructor(
        val config: ArtifactGraphConfig<D, O>,
        val descTransformers: List<DescriptionTransformer<*, D>>,
        val optionTransformers: List<ResolutionOptionsTransformer<*, O>>,
        val descType: KClass<D>,
        val optionsType: KClass<O>
    )

    // The DeReferencer to use for all resolvers
    private inner class GroupedDeReferencer<D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions> :
        RepositoryDeReferencer<D, O> {
        @Suppress("UNCHECKED_CAST")
        override fun deReference(ref: RepositoryReference<*>): RepositoryProxy<D, O, *, *>? {
            // Gets the meta from the map or returns null if it cannot be found
            val meta: ResolverMeta<*, *> = refs[ref.provider.key] ?: return null

            // Gets a new resolver
            val resolver =
                (ref.provider as ArtifactGraphProvider<ArtifactGraphConfig<*, *>, ArtifactGraph<*, RepositorySettings, *>>).provide(
                    meta.config
                )
            // Gets a new processor
            val processor: ArtifactGraph.ArtifactResolver<*, *, *, *> = resolver.resolverFor(ref.settings)

            // Returns a new repository proxy. This proxy wraps all requests repositories and transforms requests to them (outgoing from the de-references point of view).
            return RepositoryProxy(
                processor as ArtifactGraph.ArtifactResolver<ArtifactMetadata.Descriptor, *, *, ArtifactResolutionOptions>,
                meta as ResolverMeta<ArtifactMetadata.Descriptor, ArtifactResolutionOptions>
            )
        }
    }

    // A proxied Artifact Repository which will transform all incoming requests into recognizable formats. The types D and O represent outgoing requests from the caller(which
    // are transformed) while types DT and OT represent incoming requests to teh parent from types D and O.
    private inner class RepositoryProxy<D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions, DT : ArtifactMetadata.Descriptor, OT : ArtifactResolutionOptions>(
        private val parent: ArtifactRepository<DT, OT>, private val metaTo: ResolverMeta<DT, OT>
    ) : ArtifactRepository<D, O> {
        private fun <T : Any> List<Transformer<*, T>>.transform(toTransform: Any, expected: KClass<T>): T {
            if (expected.isInstance(toTransform)) return toTransform as T

            return firstNotNullOfOrNull {
                if (it.typeIn.isInstance(toTransform)) (it as Transformer<Any, T>).transform(toTransform)
                else null
            } ?: throw Exception(
                "Failed to transform type: " +
                        "'${toTransform::class.qualifiedName}', " +
                        "you must register a type transformer " +
                        "in your group config!")
        }

        override fun artifactOf(desc: D, options: O, trace: ArtifactRepository.ArtifactTrace?): Artifact? {
            val newDesc = metaTo.descTransformers.transform(desc, metaTo.descType)
            val newOptions = metaTo.optionTransformers.transform(options, metaTo.optionsType)

            return parent.artifactOf(newDesc, newOptions, trace)
        }
    }
}

public fun <D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions, C : ArtifactGraphConfig<D, O>> ResolutionGroupConfig.ResolutionBuilder<D, O, C>.configure(
    configuration: C.() -> Unit
): ResolutionGroupConfig.ResolutionBuilder<D, O, C> = also {
    this.configuration = newConfig().apply(configuration)
}

public inline fun <reified D : ArtifactMetadata.Descriptor, reified O : ArtifactResolutionOptions, C : ArtifactGraphConfig<D, O>> ResolutionGroupConfig.graphOf(
    provider: ArtifactGraphProvider<C, ArtifactGraph<C, *, ArtifactGraph.ArtifactResolver<D, *, *, O>>>,
): ResolutionGroupConfig.ResolutionBuilder<D, O, C> = graphOf(provider, D::class, O::class)