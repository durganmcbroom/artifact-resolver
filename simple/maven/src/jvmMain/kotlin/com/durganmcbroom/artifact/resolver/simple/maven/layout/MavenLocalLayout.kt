package com.durganmcbroom.artifact.resolver.simple.maven.layout

import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

public actual val mavenLocal: String = Path(System.getProperty("user.home")).resolve(".m2").resolve("repository").toAbsolutePath().toString()

public actual val pathSeparator: String = File.separator
