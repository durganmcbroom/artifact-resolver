package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.CheckedResource

public expect class LocalResource(
    path: String
) : CheckedResource

public expect fun localResourceOrNull(path: String) : CheckedResource?