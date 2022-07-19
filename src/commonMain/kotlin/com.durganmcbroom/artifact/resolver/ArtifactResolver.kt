package com.durganmcbroom.artifact.resolver

public abstract class ArtifactResolver<
        C : ArtifactResolutionConfig<*, *>,
        R : RepositorySettings,
        out P : ArtifactResolver.ArtifactProcessor<*, *, R, *>>(
    protected val config: C,
    protected val provider: ResolutionProvider<C, *>
) {
    public abstract fun newSettings(): R

    public abstract fun processorFor(settings: R): P

    public abstract class ArtifactProcessor<D : ArtifactMeta.Descriptor, M : ArtifactMeta<D, *>, in R : RepositorySettings, O : ArtifactResolutionOptions>(
        private val repository: RepositoryHandler<D, M, R>,
        private val graphController: ArtifactGraph.GraphController
    ) : ArtifactRepository<D, O> {
        private val graph by graphController::graph

        public abstract fun emptyOptions(): O

        public open fun descriptorOf(name: String): D? = repository.descriptorOf(name)

        public open fun metaOf(descriptor: D): M? = repository.metaOf(descriptor)

        protected abstract fun processArtifact(meta: M, options: O, trace: ArtifactRepository.ArtifactTrace?): Artifact?

        final override fun artifactOf(desc: D, options: O, trace: ArtifactRepository.ArtifactTrace?): Artifact? =
            graph[desc] ?: metaOf(desc)?.let { processArtifact(it, options, trace) }?.also(graphController::put)

        public fun artifactOf(name: String, options: O): Artifact? = descriptorOf(name)?.let { desc ->
            graph[desc] ?: metaOf(desc)?.let { artifactOf(it, options) }?.also(graphController::put)
        }

        public fun artifactOf(desc: D, options: O): Artifact? =
            graph[desc] ?: metaOf(desc)?.let { artifactOf(it, options) }?.also(graphController::put)

        public fun artifactOf(meta: M, options: O): Artifact? =
            graph[meta.desc] ?: processArtifact(meta, options, null)?.also(graphController::put)
    }
}

public inline fun <R : RepositorySettings, P : ArtifactResolver.ArtifactProcessor<*, *, R, *>> ArtifactResolver<*, R, P>.processorFor(
    settings: R.() -> Unit
): P = processorFor(newSettings().apply(settings))


public inline fun <D : ArtifactMeta.Descriptor, O : ArtifactResolutionOptions> ArtifactResolver.ArtifactProcessor<D, *, *, O>.artifactOf(
    name: String,
    block: O.() -> Unit = {}
): Artifact? {
    val desc = descriptorOf(name) ?: return null

    val options = emptyOptions().apply(block)

    return artifactOf(desc, options, null)
}

