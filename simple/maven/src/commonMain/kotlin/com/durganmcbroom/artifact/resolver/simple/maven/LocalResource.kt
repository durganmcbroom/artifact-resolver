package com.durganmcbroom.artifact.resolver.simple.maven

import arrow.core.Either
import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.simple.maven.layout.ResourceRetrievalException

public expect class LocalResource(
    path: String
) : CheckedResource

public expect fun localResourceOf(path: String) : Either<ResourceRetrievalException, CheckedResource>