package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.CheckedResource

public expect class HashedResource(
    hashType: HashType,
    resourceURI: String,
    check: ByteArray
) : CheckedResource

public expect fun hashedResourceOrNull(
    hashType: HashType,
    resourceURI: String,
    checkURI: String
) : HashedResource?

// All used hash types in maven
public enum class HashType {
    SHA1,
    SHA256,
    SHA512,
    MD5,
}