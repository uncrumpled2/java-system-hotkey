/* system_hotkey_jni.c - JNI bridge to Jai system_hotkey library */

#include "system_hotkey_jni.h"
#include <stdlib.h>
#include <stdint.h>

/*
 * Jai library types and function declarations.
 * These must match the exports from the Jai library.
 */

typedef struct {
    uint32_t modifiers;
    uint32_t key;
} C_Hotkey;

/* Jai array slice representation */
typedef struct {
    int64_t count;
    C_Hotkey *data;
} C_Hotkey_Array;

/* Opaque context pointer */
typedef void* System_Hotkey_Context;

/* Jai library functions (must be exported with #program_export) */
extern System_Hotkey_Context system_hotkey_init(void);
extern void system_hotkey_shutdown(System_Hotkey_Context ctx);
extern int system_hotkey_register(System_Hotkey_Context ctx, C_Hotkey hotkey);
extern int system_hotkey_unregister(System_Hotkey_Context ctx, C_Hotkey hotkey);
extern void system_hotkey_poll_events(System_Hotkey_Context ctx);
extern C_Hotkey_Array system_hotkey_get_triggered_hotkeys(System_Hotkey_Context ctx);

/* JNI Implementations */

JNIEXPORT jlong JNICALL Java_app_uncrumpled_systemhotkey_SystemHotkey_nativeInit
  (JNIEnv *env, jclass cls)
{
    (void)env;
    (void)cls;
    System_Hotkey_Context ctx = system_hotkey_init();
    return (jlong)(intptr_t)ctx;
}

JNIEXPORT void JNICALL Java_app_uncrumpled_systemhotkey_SystemHotkey_nativeShutdown
  (JNIEnv *env, jclass cls, jlong ptr)
{
    (void)env;
    (void)cls;
    if (ptr != 0) {
        system_hotkey_shutdown((System_Hotkey_Context)(intptr_t)ptr);
    }
}

JNIEXPORT jboolean JNICALL Java_app_uncrumpled_systemhotkey_SystemHotkey_nativeRegister
  (JNIEnv *env, jclass cls, jlong ptr, jint modifiers, jint key)
{
    (void)env;
    (void)cls;
    if (ptr == 0) return JNI_FALSE;

    C_Hotkey hotkey;
    hotkey.modifiers = (uint32_t)modifiers;
    hotkey.key = (uint32_t)key;

    int result = system_hotkey_register(
        (System_Hotkey_Context)(intptr_t)ptr,
        hotkey
    );
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_app_uncrumpled_systemhotkey_SystemHotkey_nativeUnregister
  (JNIEnv *env, jclass cls, jlong ptr, jint modifiers, jint key)
{
    (void)env;
    (void)cls;
    if (ptr == 0) return JNI_FALSE;

    C_Hotkey hotkey;
    hotkey.modifiers = (uint32_t)modifiers;
    hotkey.key = (uint32_t)key;

    int result = system_hotkey_unregister(
        (System_Hotkey_Context)(intptr_t)ptr,
        hotkey
    );
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jintArray JNICALL Java_app_uncrumpled_systemhotkey_SystemHotkey_nativePoll
  (JNIEnv *env, jclass cls, jlong ptr)
{
    (void)cls;
    if (ptr == 0) {
        return (*env)->NewIntArray(env, 0);
    }

    System_Hotkey_Context ctx = (System_Hotkey_Context)(intptr_t)ptr;

    /* Poll for events */
    system_hotkey_poll_events(ctx);

    /* Get triggered hotkeys */
    C_Hotkey_Array hotkeys = system_hotkey_get_triggered_hotkeys(ctx);

    /* Create Java int array: [mod0, key0, mod1, key1, ...] */
    jsize array_len = (jsize)(hotkeys.count * 2);
    jintArray result = (*env)->NewIntArray(env, array_len);

    if (result == NULL || hotkeys.count == 0) {
        return result;
    }

    /* Copy data */
    jint *elements = (*env)->GetIntArrayElements(env, result, NULL);
    if (elements != NULL) {
        for (int64_t i = 0; i < hotkeys.count; i++) {
            elements[i * 2] = (jint)hotkeys.data[i].modifiers;
            elements[i * 2 + 1] = (jint)hotkeys.data[i].key;
        }
        (*env)->ReleaseIntArrayElements(env, result, elements, 0);
    }

    return result;
}
