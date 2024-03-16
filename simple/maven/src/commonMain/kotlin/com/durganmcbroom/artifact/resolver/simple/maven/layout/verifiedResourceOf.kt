package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.jobs.Job
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceAlgorithm

internal expect fun verifiedResourceOf(
    location: String,
    checksumLocation: String,
    algorithm: ResourceAlgorithm,

    requireVerification: Boolean,
) : Job<Resource>