/* system_hotkey_jni.h - JNI header for SystemHotkey */
#ifndef SYSTEM_HOTKEY_JNI_H
#define SYSTEM_HOTKEY_JNI_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     app_uncrumpled_systemhotkey_SystemHotkey
 * Method:    nativeInit
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_app_uncrumpled_systemhotkey_SystemHotkey_nativeInit
  (JNIEnv *, jclass);

/*
 * Class:     app_uncrumpled_systemhotkey_SystemHotkey
 * Method:    nativeShutdown
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_app_uncrumpled_systemhotkey_SystemHotkey_nativeShutdown
  (JNIEnv *, jclass, jlong);

/*
 * Class:     app_uncrumpled_systemhotkey_SystemHotkey
 * Method:    nativeRegister
 * Signature: (JII)Z
 */
JNIEXPORT jboolean JNICALL Java_app_uncrumpled_systemhotkey_SystemHotkey_nativeRegister
  (JNIEnv *, jclass, jlong, jint, jint);

/*
 * Class:     app_uncrumpled_systemhotkey_SystemHotkey
 * Method:    nativeUnregister
 * Signature: (JII)Z
 */
JNIEXPORT jboolean JNICALL Java_app_uncrumpled_systemhotkey_SystemHotkey_nativeUnregister
  (JNIEnv *, jclass, jlong, jint, jint);

/*
 * Class:     app_uncrumpled_systemhotkey_SystemHotkey
 * Method:    nativePoll
 * Signature: (J)[I
 */
JNIEXPORT jintArray JNICALL Java_app_uncrumpled_systemhotkey_SystemHotkey_nativePoll
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif

#endif /* SYSTEM_HOTKEY_JNI_H */
