# Artifact Resolver

*An artifact resolution API for working with repoositories. Comes with out-of-the-box support for a mocked version of
Maven*

## Basic Usage

The Artifact Resolution API contains these main components:

- **ResolutionProvider** : The main entry point for creating and configuring resolvers.
- **ArtifactResolver** : The main entry point for creating and configuring processors.
- **ArtifactProcessor** : A processor use to resolve artifacts.
- **ArtifactMeta** : Metadata about an artifact, specific to different repository types.
    - **Descriptor** : Information about where to find an artifact.
    - **Transitive** : Information about where to find transitive artifacts.
- **Artifact** : A resolved artifact containing information about its meta and children.
- **RepositoryReference** : Information about a repository.
- **RepositoryDeReferencer** : An interface to turn references into usable repositories.

### Examples

#### Kotlin

First, to configure the resolver, we need to create a `ArtifactResolver`:

*Assuming we are using the mock maven library*

```kotlin
val resolver = ArtifactResolver(MockMaven) {
    // ... configure the resolver ...
    // eg. add a de-referencer
}
```

Then, we can use the `ArtifactResolver` to obtain a `ArtifactResolver$ArtifactProcessor`:

```kotlin
val processor = resolver.processorFor {
    // ... configure the processor ...
    // eg. add a repository
}
```

Lastly we can use our repository to resolve an artifact:

```kotlin
// Resolve a descriptor (format depends on underlying implementation)
val desc = processor.descriptorOf("...")
val meta = processor.metaOf(desc)

val artifact = processor.artifactOf(meta, /* options */)

// ~~ OR ~~

// Using the convenience methods
val artifact = procesor.artifactOf("...") {
    // ... configure the artifact ...
    // Eg. Exclude artifacts, filter scopes etc.
}
```

#### Java

Using Java is slightly more verbose but is still fully supported.

*All type information excluded for the sake of brevity*

```java
public class MyArtifactTests {
    public ArtifactResolver getResolver() {
        final var config = MockMaven.INSTANCE.emptyConfig();

        // Configure config

        return Resolvers.newResolver(MockMaven.INSTANCE, config);

        // ~~ OR ~~

        // You can also configure the config using the following convenience
        // method. This technique is less helpful here, but slightly easier when
        // using groupings.

        return Resolvers.newResolver(MockMaven.INSTANCE, JavaResolutionConfig.config(MockMaven.INSTANCE, (config) -> {
            // Configure
        }));
    }

    // ...
}
```

Then getting a processor.

```java
public class MyArtifactTests {
    // ...

    public ArtifactProcessor getProcessor() {
        final var resolver = getResolver();

        final var settings = resolver.newSettings();
        // configure settings
        // ...

        return resolver.processorFor(settings);
    }
}
```

Then resolve an artifact.

```java
public class MyArtifactTests {
    // ...

    @Nullable
    public Artifact getArtifact(String name) {
        final var processor = getProcessor();

        // Going straight to convenience methods

        final var options = processor.emptyOptions();
        // Configure options
        // ...

        return processor.artifactOf(name, options);
    }
}
```

### Repository Grouping

There are times when we need more than 1 type of repository to resolve an artifact. For example, custom implementations
that use their own system as well as using maven. In this case we can use the ArtifactResolver Grouping API.

The Grouping API is an implementation of an artifact resolution system except that it provides utilities to safely allow
access to multiple different repositories, ensuring no type exceptions occur.

#### A Basic Example

Unfortunately, there are some issues with the Grouping Api, one of which is that there are two things that need to be
provided to a processor. A descriptor, and artifact resolution options. These are both types that are specific to each
different type of repository and so cannot be safely passed between them with no transformation. The solution that is
used is to provide transformers so that as information is passed between repositories, a proxy can intercept and
transform them.

*Assuming we are using a custom implementation and the mock maven library*

##### Kotlin

```kotlin
// As usual we first create our resolver, this time using the `ResolutionGroup` rovider

val resolver = ArtifactResolver(ResolutionGrouping) {
    // With resolution groupings most of the heavy lifting comes with the resolver configuration.

    // To start creating an actual resolver for us to use we call the #resolver method on our configuration
    resolver(MockMaven)
        // Now we have to add transformers, these receive descriptors and output a descriptor that 
        // the given resolver can read. For every repository in the group that outputs a descriptor
        // not usable by this resolver, a transformer is needed.
        .addDescriptionTransformer(MyCustomDesc::class, MockMavenDescriptor::class) {
            // Do work
        }
        // We have now added a description transformer, but we still need to modify resolution options.
        // We can do this in a similar manner.
        .addResolutionOptionsTransformer(MyCustomOptions::class, MockMavenArtifactResolutionOptions::class) {
            // Do work
        }
        // Finally we can configure the resolver, this can happen at any time however i choose to
        // include it at the end.
        .configure {
            // Configure
        }
        // And lastly register this resolver
        .register()

    // We can continue adding as many resolvers as we like. However, note you may have alot 
    // of transformers to implement!

    // I'll include one more example, no comments though.

    resolver(MyCustomImpl)
        .addDescriptionTransformer(MockMavenDescriptor::class, MyCustomDesc::class) {
            // Do work
        }
        .addResolutionOptionsTransformer(MockMavenArtifactResolutionOptions::class, MyCustomOptions::class) {
            // Do work
        }
        .configure {
            // Configure
        }.register()
}

// Great! We have setup a resolver! Now we have to retrieve an actual implementation of a resolver from it to use.

val customResolver = resolver[MyCustomImpl]!! // We know its in there, dont have to check.
val customProcessor = customResolver.processorFor {} // Get a processor from the resolver
val artifact =
    customProcessor.artifactOf("...") {} // And finally get our artifact! The grouping system will take care of all calls between our two repositories.
```

##### Java

*For a detailed explanation please see the kotlin example.*

```java
class MyGroupingTests {
    public void createResolver() {
        val config = ResolutionGrouping.INSTANCE;

        config.resolver(MockMaven.INSTANCE)
                // We add a description transformer in the same way as before, however now we also add in the call `JavaTransformers#descTransformer` 
                // with the types we are going to transform and a transformer.
                .addDescriptionTransformer(JavaTransformers.descTransformer(MyCustomDesc.class, MockMavenDescriptor.class, (in) -> {
                    // Do work
                })).addResolutionOptionsTransformer(JavaTransformers.resolutionOptionsTransformer(MyCustomOptions.class, MockMavenArtifactResolutionOptions.class, (in) -> {
                    // Do work
                }))
                // When configuring we can use the `JavaResolutionConfig#config` convenience method to keep
                // things slightly tighter, this is completely optional however.
                .configure(JavaResolutionConfig.config(MockMaven.INSTANCE, (config) -> {
                    // Configure
                })).register();

        // Configure second resolver
        config.resolver(MyCustomImpl.INSTANCE)
                .addDescriptionTransformer(JavaTransformers.descTransformer(MockMavenDescriptor.class, MyCustomDesc.class, (in) -> {
                    // Do work
                })).addResolutionOptionsTransformer(JavaTransformers.resolutionOptionsTransformer(MockMavenArtifactResolutionOptions.class, MyCustomOptions.class, (in) -> {
                    // Do work
                }))
                .configure(JavaResolutionConfig.config(MyCustomImpl.INSTANCE, (config) -> {
                    // Configure
                })).register();


        val group = Resolvers.newResolver(ResolutionGrouping.INSTANCE, config);
        
        // Now we setup our initial resolver and we can use the grouping! 
        val resolver = group.get(MyCustomImpl.INSTANCE);
        
        // Resolve artifacts...
    }
}

```

## Implementations

 - [Mock Maven](./mock/maven/README.md)