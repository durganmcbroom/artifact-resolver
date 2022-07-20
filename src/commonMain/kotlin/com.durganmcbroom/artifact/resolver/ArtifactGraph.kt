package com.durganmcbroom.artifact.resolver

public abstract class ArtifactGraph<
        C : ArtifactGraphConfig<*, *>,
        R : RepositorySettings,
        out P : ArtifactGraph.ArtifactResolver<*, *, R, *>>(
    protected val config: C,
    protected val provider: ArtifactGraphProvider<C, *>
) {
    private val controller = config.graph
    public val graph: Map<ArtifactMetadata.Descriptor, Artifact>
        get() = HashMap(controller.graph)

    public abstract fun newRepoSettings(): R

    public abstract fun resolverFor(settings: R): P

    public interface GraphController {
        public val graph: MutableMap<ArtifactMetadata.Descriptor, Artifact>
        public fun put(artifact: Artifact) {
            graph[artifact.metadata.desc] = artifact
        }
    }

    public abstract class ArtifactResolver<D : ArtifactMetadata.Descriptor, M : ArtifactMetadata<D, *>, R : RepositorySettings, O : ArtifactResolutionOptions>(
        public val repository: RepositoryHandler<D, M, R>,
        private val graphController: GraphController
    ) : ArtifactRepository<D, O> {
        private val graph by graphController::graph

        public abstract fun emptyOptions(): O

        public open fun descriptorOf(name: String): D? = repository.descriptorOf(name)

        public open fun metaOf(descriptor: D): M? = repository.metaOf(descriptor)

        protected abstract fun resolve(meta: M, options: O, trace: ArtifactRepository.ArtifactTrace?): Artifact?

        final override fun artifactOf(desc: D, options: O, trace: ArtifactRepository.ArtifactTrace?): Artifact? =
            graph[desc] ?: metaOf(desc)?.let { resolve(it, options, trace) }?.also(graphController::put)

        public fun artifactOf(name: String, options: O): Artifact? = descriptorOf(name)?.let { desc ->
            graph[desc] ?: metaOf(desc)?.let { artifactOf(it, options) }?.also(graphController::put)
        }

        public fun artifactOf(desc: D, options: O): Artifact? =
            graph[desc] ?: metaOf(desc)?.let { artifactOf(it, options) }?.also(graphController::put)

        public fun artifactOf(meta: M, options: O): Artifact? =
            graph[meta.desc] ?: resolve(meta, options, null)?.also(graphController::put)
    }
}

public inline fun <R : RepositorySettings, P : ArtifactGraph.ArtifactResolver<*, *, R, *>> ArtifactGraph<*, R, P>.resolverFor(
    settings: R.() -> Unit
): P = resolverFor(newRepoSettings().apply(settings))


public inline fun <D : ArtifactMetadata.Descriptor, O : ArtifactResolutionOptions> ArtifactGraph.ArtifactResolver<D, *, *, O>.artifactOf(
    name: String,
    block: O.() -> Unit = {}
): Artifact? {
    val desc = descriptorOf(name) ?: return null

    val options = emptyOptions().apply(block)

    return artifactOf(desc, options, null)
}

