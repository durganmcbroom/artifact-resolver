package com.durganmcbroom.artifact.resolver.mock.maven

import com.durganmcbroom.artifact.resolver.ArtifactResolutionConfig
import com.durganmcbroom.artifact.resolver.RepositoryDeReferencer

public class MockMavenResolutionConfig(
//    public val layoutProvider: MavenLayoutProvider = DelegatingLayoutProvider(ArrayList()),
) : ArtifactResolutionConfig<MavenDescriptor, MockMavenArtifactResolutionOptions>() {
    init {
        deReferencer = RepositoryDeReferencer { ref ->
            val resolver = if (ref.provider is MockMaven)
                (ref.provider as MockMaven).provide(this)
            else return@RepositoryDeReferencer null

            check(ref.settings is MockMavenRepositorySettings) { "Invalid Repository settings." }
            resolver.processorFor(ref.settings as MockMavenRepositorySettings)
        }
    }

//    public fun installMaven() {
//        installLayouts {
//            if (it !is MavenRepositorySettings) return@installLayouts null
//
//            when (it.layout.type) {
//                "default" -> DefaultMavenLayout(it)
//                "snapshot" -> SnapshotRepositoryLayout(it)
//                "local" -> MavenLocalLayout
//                else -> null
//            }
//        }
//    }

//    public fun installLayouts(provider: MavenLayoutProvider) {
//        if (!isLocked && layoutProvider is DelegatingLayoutProvider) layoutProvider.layouts.add(provider)
//        else throw IllegalArgumentException("Cannot install layouts in this context.")
//    }

//    private class DelegatingLayoutProvider(
//        val layouts: MutableList<MavenLayoutProvider>
//    ) : MavenLayoutProvider {
//
//        override fun provide(settings: RepositorySettings): MavenRepositoryLayout? =
//            layouts.firstNotNullOfOrNull { it.provide(settings) }
//    }
}