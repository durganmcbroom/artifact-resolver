# Artifact Resolver

*An API for parsing, loading, and resolving artifacts and dependencies from remote repositories. Comes with optional
extra modules to support loading from maven repositories.*

## Basic Usage

The Artifact Resolution API contains these main components:

- **ArtifactGraphProvider** : The main entry point for creating and configuring artifact graphs.
- **ArtifactGraph** : A cache of artifacts that have already been loaded. This class also contains options for
  configuring Artifact resolvers.
- **ArtifactResolver** : As the name suggests, this resolves artifacts. More specifically it creates metadata about
  artifacts and then is able to return a tree of artifacts and their transitive dependencies.
- **ArtifactMeta** : Metadata about an artifact including: links to resources(jar etc.), information about transitive
  dependencies and other repository specific data.
    - **Descriptor** : Information about where to find an artifact.
    - **TransitiveInfo** : Information about where to find transitive artifacts.
- **Artifact** : A resolved artifact containing metadata and children.

### Examples

#### Kotlin

First, we need to create a `ArtifactGraph`:

*Assuming we are using the simple maven library, this is a working example*

```kotlin
val graph = ArtifactGraph(SimpleMaven) {
    // ... configure the graph ...
    // eg. add a de-referencer
}

// Then, we can use the `ArtifactGraph` to obtain a `ArtifactGraph$ArtifactResolver`:
val resolver = graph.resolverFor {
    // ... configure the processor ...
    // eg. add a repository
    useMavenCentral()
}

// Lastly we can use our resolve to resolve an artifact:

val name = "<NAME>" // Artifact name, eg. `groupId:artifactId:version`

// Resolve a descriptor (format depends on underlying implementation)
val desc = resolver.descriptorOf(name)
val meta = resolver.metaOf(desc)

val artifact = resolver.artifactOf(meta, /* options */)

// ~~ OR ~~

// Using the convenience methods
val artifact = resolver.artifactOf(name) {
    // ... configure the artifact ...
    // Eg. Exclude artifacts, filter scopes etc.
}
```

#### Java

Using Java is slightly more verbose but is still fully supported.

*All type information excluded for the sake of brevity, if directly copied you will need to fill in that info.*

```java
public class MyArtifactTests {
    public ArtifactGraph getGraph() {
        final var config = SimpleMaven.INSTANCE.emptyConfig();

        // Configure config

        return ArtifactGraphs.newGraph(SimpleMaven.INSTANCE, config);

        // ~~ OR ~~

        // You can also configure the config using the following convenience
        // method. This technique is less helpful here, but slightly easier when
        // using groupings.

        return ArtifactGraphs.newGraph(SimpleMaven.INSTANCE, JavaResolutionConfig.config(SimpleMaven.INSTANCE, (config) -> {
            // Configure
        }));
    }

    // Then we get our Processor
    public ArtifactResolver getResolver() {
        final var graph = getGraph();

        final var settings = graph.newSettings();
        // configure settings
        // ...
        settings.useMavenCentral();

        return graph.resolverFor(settings);
    }

    // Then resolve an artifact
    @Nullable
    public Artifact getArtifact(String name) {
        final var resolver = getResolver();

        // Going straight to convenience methods

        final var options = resolver.emptyOptions();
        // Configure options
        // ...

        return resolver.artifactOf(name, options);
    }
}
```

### Repository Grouping

There are times when we need more than 1 type of repository to resolve an artifact. For example, custom implementations
that use their own repository system as well as using maven. In this case we can use the Grouping API.

The Grouping API is an implementation of an artifact resolution system except that it provides utilities to safely allow
access between multiple different repositories, ensuring no type exceptions occur.

#### A Basic Example

Unfortunately, to properly function there is a bit more setup needed, two pieces of information have to be transformed
between resolvers: One, the artifact descriptor; and two the artifact resolution options. These are both types that are
specific to each
different type of repository and so cannot be safely passed between them with no transformation. The solution is to
provide transformers so that as information is passed between repositories, a proxy can intercept and
transform them.

*Assuming we are using a custom implementation and the simple maven library*

##### Kotlin

```kotlin
// As usual we first create our graph, this time using the `ResolutionGroup` provider

val graph = ArtifactGraph(ResolutionGrouping) {
    // With resolution groupings most of the heavy lifting comes with the resolver configuration.

    // To start creating an actual graph for us to use we call the #graphOf method on our configuration
    graphOf(SimpleMaven)
        // Now we have to add transformers, these receive descriptors and output a descriptor that 
        // the given resolver can read. For every repository in the group that outputs a descriptor
        // not usable by this graph, a transformer is needed.
        .addDescriptionTransformer(MyCustomDesc::class, SimpleMavenDescriptor::class) {
            // Do work
        }
        // We have now added a description transformer, but we still need to modify resolution options.
        // We can do this in a similar manner.
        .addResolutionOptionsTransformer(MyCustomOptions::class, SimpleMavenArtifactResolutionOptions::class) {
            // Do work
        }
        // Finally we can configure the graph, this can happen at any time however I choose to
        // include it at the end.
        .configure {
            // Configure
        }
        // And lastly register this graph
        .register()

    // We can continue adding as many graphs as we like. However, you may have alot 
    // of transformers to implement!

    // I'll include one more example, no comments though.

    graphOf(MyCustomImpl)
        .addDescriptionTransformer(SimpleMavenDescriptor::class, MyCustomDesc::class) {
            // Do work
        }
        .addResolutionOptionsTransformer(SimpleMavenArtifactResolutionOptions::class, MyCustomOptions::class) {
            // Do work
        }
        .configure {
            // Configure
        }.register()
}

// Great! We have setup a grouping graph! Now we have to retrieve an actual implementation of a graph from it to use.

val customGraph = graph[MyCustomImpl]!! // We know its in there, dont have to check.
val customResolver = customGraph.resolverFor {} // Get a processor from the resolver
val artifact =
    customResolver.artifactOf("...") {} // And finally get our artifact! The grouping system will take care of all calls between our two repositories.
```

##### Java

*For a detailed explanation please see the kotlin example.*

```java
class MyGroupingTests {
    public void createResolver() {
        val config = ResolutionGrouping.INSTANCE;

        config.resolver(SimpleMaven.INSTANCE)
                // We add a description transformer in the same way as before, however now we also add in the call `JavaTransformers#descTransformer` 
                // with the types we are going to transform and a transformer.
                .addDescriptionTransformer(JavaTransformers.descTransformer(MyCustomDesc.class, SimpleMavenDescriptor.class, (in) -> {
                    // Do work
                })).addResolutionOptionsTransformer(JavaTransformers.resolutionOptionsTransformer(MyCustomOptions.class, SimpleMavenArtifactResolutionOptions.class, (in) -> {
                    // Do work
                }))
                // When configuring we can use the `JavaResolutionConfig#config` convenience method to keep
                // things slightly tighter, this is completely optional however.
                .configure(JavaResolutionConfig.config(SimpleMaven.INSTANCE, (config) -> {
                    // Configure
                })).register();

        // Configure second graph
        config.resolver(MyCustomImpl.INSTANCE)
                .addDescriptionTransformer(JavaTransformers.descTransformer(SimpleMavenDescriptor.class, MyCustomDesc.class, (in) -> {
                    // Do work
                })).addResolutionOptionsTransformer(JavaTransformers.resolutionOptionsTransformer(SimpleMavenArtifactResolutionOptions.class, MyCustomOptions.class, (in) -> {
                    // Do work
                }))
                .configure(JavaResolutionConfig.config(MyCustomImpl.INSTANCE, (config) -> {
                    // Configure
                })).register();


        val group = ArtifactGraphs.newGraph(ResolutionGrouping.INSTANCE, config);

        // Now we setup our initial graph and we can use the grouping! 
        val graph = group.get(MyCustomImpl.INSTANCE);

        // Resolve artifacts...
    }
}

```

## Implementations

- [Simple Maven](./simple/maven/README.md)