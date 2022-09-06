package com.durganmcbroom.artifact.resolver.simple.maven

import arrow.core.Either
import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.simple.maven.layout.ResourceRetrievalException

public expect class HashedResource(
    hashType: String,
    resourceUrl: String,
    check: ByteArray
) : CheckedResource

public expect fun hashedResourceOf(
    hashType: HashType,
    resourceUrl: String,
    checkUrl: String
) : Either<ResourceRetrievalException, HashedResource>

// All used hash types in maven
public enum class HashType {
    SHA1,
    SHA256,
    SHA512,
    MD5,
}