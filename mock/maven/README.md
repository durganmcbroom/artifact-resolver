# Mock Maven Artifact Resolver

An Implementation of the Artifact Resolver API for maven. Note that this is a completely mocked version and contains no
official maven code, so while very lightweight there are edge cases it cannot handle.

## Usage

### Basic Example

```kotlin
val resolver = ArtifactResolver(MockMaven) {
    // Nothing to configure besides the graph and de-referencer.
}

val processor = resolver.processorFor {
    useMavenCentral()
}

val artifact = processor.artifactOf("...") {
    exclude("...", "...")
    includeScopes("compile", "test")
    processAsync = false
    // Etc...
}
```

### The Maven Resolution Config

The Mocked Maven Resolution Config contains no special information or configurations besides what is inherited from its
parent.

**Eg.**

```kotlin
val resolver = ArtifactResolver(Mockmaven) {
    // Nothing to configure specific to Maven
}
```

### The Maven repository settings

The Mocked Maven repository settings have some more options to configure:

#### Fields:

- `preferredHash` : What Hash type you would like to be used when verifying artifacts/external files.
- `pluginProvider` : A provider of plugins, used when parsing poms for injecting extra properties.
- `repositoryReferencer` : Takes repositories from pom files and parses them into `RepositoryReference`s.
- `layout` : The Maven repository layout to use.

#### Methods:

- `#installPluginProvider(MockPluginProvider)` : If the current plugin provider is able to delegate, this convenience
  method installs the given provider into it.
- `#installPomRepoReferencer(PomRepositoryReferencer)` : If the current POM referencer is able to delegate, this
  convenience method installs the given referencer into it.
- `#useBasicRepoReferencer()` : Installs a basic Maven Repo referencer capable of supplying layouts `default`
  and `snapshot`
- `#useMavenCentral()` : Sets Maven Central as the current layout.
- `#useMavenLocal()` : Sets Maven Local as the current layout.
- `#useDefaultLayout(String)` : Sets the current layout to default with the given String as the URL.

### Maven Artifact Resolution Options

Resolution options allow you to specify specifics about how a certain artifact should be resolved. The options provided
by Mock Maven are:

- `#exclude(vararg names)` : Excludes the given artifacts from resolution. A variable amount of Artifact IDs should be
  passed to this method.
- `#includeScopes(vararg scopes)` : ONLY includes the given transitive dependencies with the given scopes when resolving
  artifacts. If no scopes are set here then all scopes will be included in resolution.
- `processAsync` : Whether the given artifact should be processed asynchronously with kotlin coroutines.