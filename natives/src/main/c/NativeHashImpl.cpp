#include <stdlib.h>

#include <mbedtls/sha256.h>
#include "com_voxelwind_server_jni_hash_NativeHashImpl.h"

typedef unsigned char byte;

void initializeDigest(mbedtls_sha256_context *mdCtx) {
	mbedtls_sha256_init(mdCtx);
	mbedtls_sha256_starts(mdCtx, 0);
}

jlong JNICALL Java_com_voxelwind_server_jni_hash_NativeHashImpl_init(JNIEnv *env, jobject obj) {
	mbedtls_sha256_context *mdCtx = (mbedtls_sha256_context*) malloc(sizeof (mbedtls_sha256_context));
	initializeDigest(mdCtx);
	return (jlong)mdCtx;
}

void JNICALL Java_com_voxelwind_server_jni_hash_NativeHashImpl_update(JNIEnv *env, jobject obj, jlong ctx, jlong in, jint bytes) {
	mbedtls_sha256_context *mdCtx = (mbedtls_sha256_context*)ctx;
	mbedtls_sha256_update(mdCtx, (byte*) in, bytes);
}

jbyteArray JNICALL Java_com_voxelwind_server_jni_hash_NativeHashImpl_digest(JNIEnv *env, jobject obj, jlong ctx) {
	// SHA-256 produces 32 bytes (256 bits)
	unsigned char output[32];

	// Hash the final output.
	mbedtls_sha256_context *mdCtx = (mbedtls_sha256_context*)ctx;
	mbedtls_sha256_finish(mdCtx, (unsigned char*) output);

	// Reinitialize the context for further handling.
	initializeDigest(mdCtx);

	// Copy array into Java byte array.
	jbyteArray array = env->NewByteArray(32);
	env->SetByteArrayRegion(array, 0, 32, (jbyte*)output);
	return array;
}

JNIEXPORT void JNICALL Java_com_voxelwind_server_jni_hash_NativeHashImpl_free(JNIEnv *env, jobject obj, jlong ctx) {
	mbedtls_sha256_context *mdCtx = (mbedtls_sha256_context*)ctx;
	mbedtls_sha256_free(mdCtx);
}