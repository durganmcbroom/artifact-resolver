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
  dependencies (internally called children) and other repository specific data.
    - **Descriptor** : Information about where to find an artifact.
    - **ChildInfo** : Information about where to find child (transitive) artifact.
- **Artifact** : A resolved artifact containing metadata and children.

### Examples

#### Kotlin

This is a fully working super simple example. Here I choose to exclude any scopes that are not `compile`, `runtime`
or `import`, so we dont have to worry about pulling in all the testing libraries.

*This requires the simple maven library*

```kotlin
val graph = ArtifactGraph(SimpleMaven)

val resolver = graph.resolverFor {
    useMavenCentral()
}

// Dont worry, spring context isnt *too* giant...
val artifact = resolver.artifactOf("org.springframework:spring-context:5.3.22") {
    includeScopes("compile", "runtime", "import")
}
```

#### Java

Using Java is slightly more verbose but is still fully supported.

*All type information excluded for the sake of brevity, if directly copied you will need to fill in that info.*

```java
public class MyArtifactTests {
    public Artifact getSpringContextArtifact() {
        final var resolver = ArtifactGraphs.newGraph(SimpleMaven.INSTANCE);

        final var settings = resolver.newRepoSettings();
        settings.useMavenCentral();

        var resolver = resolver.resolverFor(settings);
        final var options = resolver.emptyOptions();
        options.includeScopes("compile", "runtime", "import");

        var artifactOrNull = resolver.artifactOf("org.springframework:spring-context:5.3.22", options);

        assert artifactOrNull != null;

        return artifactOrNull;
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

val graph = ArtifactGraph(ResolutionGroup) {
    // With resolution groups most of the heavy lifting comes with the resolver configuration.

    // To start creating an actual graph inside this group, we call the #graphOf method 
    // on our configuration. In this case we are using the inline reifed extension function,
    // You could instead call 
    // `#graphOf(SimpleMaven, SimpleMavenDescriptor::class, SimpleMavenArtifactResolutionOptions::class)`
    graphOf(SimpleMaven)
        // Now we have to add transformers, these receive descriptors and output a descriptor that 
        // the given resolver can read. Eg. Maven doesnt know the class MyCustomDesc, so we have 
        // to add a transform which converts this type into something Maven can deal with. For every 
        // repository in the group that outputs a descriptor not usable by *this* graph  (ie. SimpleMaven), 
        // a transformer is needed.
        .addDescriptionTransformer(MyCustomDesc::class, SimpleMavenDescriptor::class) {
            // Do work
        }
        // We have now added a description transformer, but we still need to modify resolution options.
        // We can do this in a similar manner.
        .addResolutionOptionsTransformer(MyCustomOptions::class, SimpleMavenArtifactResolutionOptions::class) {
            // Do work
        }
        // Finally we can (optionally, if it has settings to configure) configure the graph, this can 
        // happen at any time however, I chose to include it at the end.
        .configure {
            // Configure
        }
        // And lastly register this graph
        .register()

    // We can continue adding as many graphs as we like. However, you may have alot 
    // of transformers to implement!

    // I'll include one more example, no comments though.

    graphOf(MyCustomImpl)
        .addDescriptionTransformer(SimpleMavenDescriptor::class, MyCustomDesc::class) {}
        .addResolutionOptionsTransformer(SimpleMavenArtifactResolutionOptions::class, MyCustomOptions::class) {}
        .configure {}
        .register()
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

        // When registering the graph we have to provide our description type and options type. 
        config.graphOf(SimpleMaven.INSTANCE, MyCustomDesc.class, MyCustomOptions.class)
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
        config.graphOf(MyCustomImpl.INSTANCE, SimpleMavenDescriptor.class, SimpleMavenArtifactResolutionOptions.class)
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
