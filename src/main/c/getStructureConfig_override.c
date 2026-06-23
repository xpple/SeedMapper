#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include <jni.h>

static const jint JNI_VERSION = JNI_VERSION_24;

static JavaVM *g_vm = NULL;

static jclass g_structureConfigOverrideCls = NULL;

static JNIEnv* getEnv(int *didAttach) {
    *didAttach = 0;
    JNIEnv *env;
    jint state = (*g_vm)->GetEnv(g_vm, (void**)&env, JNI_VERSION);
    if (state == JNI_EVERSION) {
        fprintf(stderr, "[seedmapper] JNI version not supported!\n");
        return NULL;
    }
    if (state == JNI_EDETACHED) {
        if ((*g_vm)->AttachCurrentThread(g_vm, (void**)&env, NULL) != JNI_OK) {
            fprintf(stderr, "[seedmapper] Could not attach current thread to JVM!\n");
            return NULL;
        }
        *didAttach = 1;
        return env;
    }
    assert(state == JNI_OK);
    return env;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_vm = vm;
    fprintf(stderr, "[seedmapper] Cached JVM pointer! This is not an error.\n");

    JNIEnv *env;
    jint state = (*vm)->GetEnv(vm, (void**)&env, JNI_VERSION);
    if (state != JNI_OK) {
        return JNI_ERR;
    }
    jclass cls = (*env)->FindClass(env, "dev/xpple/seedmapper/jni/StructureConfigOverride");
    if (cls == NULL) {
        fprintf(stderr, "[seedmapper] Class 'StructureConfigOverride' not found!\n");
        return JNI_ERR;
    } else {
        g_structureConfigOverrideCls = (jclass)(*env)->NewGlobalRef(env, cls);
    }

    return JNI_VERSION;
}

JNIEXPORT int getStructureConfig_override(int stype, int mc, void *sconf) {
    int didAttach;
    JNIEnv *env = getEnv(&didAttach);
    if (env == NULL) {
        return 0;
    }

    jmethodID mid = (*env)->GetStaticMethodID(env, g_structureConfigOverrideCls, "getStructureConfig_override", "(IIJ)I");
    if (mid == NULL) {
        fprintf(stderr, "[seedmapper] Method 'getStructureConfig_override' not found!\n");
        if (didAttach) {
            (*g_vm)->DetachCurrentThread(g_vm);
        }
        return 0;
    }

    jint ret = (*env)->CallStaticIntMethod(env, g_structureConfigOverrideCls, mid, stype, mc, (jlong)sconf);
    if (didAttach) {
        (*g_vm)->DetachCurrentThread(g_vm);
    }
    return ret;
}
