package com.durganmcbroom.artifact.resolver.mock.maven.layout

import java.io.File
import java.nio.file.Path

public actual val mavenLocal: String = Path.of(System.getProperty("user.home")).resolve(".m2").resolve("repository").toAbsolutePath().toString()

public actual val pathSeparator: String = File.separator
