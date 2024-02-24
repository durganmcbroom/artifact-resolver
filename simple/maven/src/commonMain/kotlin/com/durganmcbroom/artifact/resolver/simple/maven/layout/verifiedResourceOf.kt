package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.jobs.JobResult
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceAlgorithm

internal expect suspend fun verifiedResourceOf(
    location: String,
    checksumLocation: String,
    algorithm: ResourceAlgorithm,

    requireVerification: Boolean,
) : JobResult<Resource, ResourceRetrievalException>