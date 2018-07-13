/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <stdio.h>
#include <string.h>
/* Header for class com_okay_testjni_jni_JNICall */

#ifndef _Included_com_okay_testjni_jni_JNICall
#define _Included_com_okay_testjni_jni_JNICall
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_okay_testjni_jni_JNICall
 * Method:    getStringFromJNI
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_okay_testjni_jni_JNICall_getStringFromJNI
        (JNIEnv *env, jclass jobj) {
    char buff[128] = {0};
    sprintf(buff, "hello from jni(c)");
    return (*env)->NewStringUTF(env, buff);
}

JNIEXPORT jstring JNICALL Java_com_okay_testjni_jni_JNICall_encrypt
        (JNIEnv *env, jclass jobj, jstring jstr) {
    
    //1.将传入的string转换成byte数组
    jclass clsstring = (*env)->FindClass(env, "java/lang/String");
    jstring strencode = (*env)->NewStringUTF(env, "GB2312");
    jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) (*env)->CallObjectMethod(env,jstr, mid, strencode);
    
    //2.获取长度
    jsize alen = (*env)->GetArrayLength(env, barr);
    //3.获取数组指针
    jbyteArray *b_arr = (*env)->GetByteArrayElements(env,barr, NULL);
    printf("alen %d", alen);
    printf("diyige %c",b_arr[0]);
    
    //4.定义key
    jint key = 0x12;
    jint i = 0;

    //5.新建长度len的数组
    jbyteArray jarr = (*env)->NewByteArray(env, alen);
    //6.获取数组指针
    jbyteArray *j_arr = (*env)->GetByteArrayElements(env, jarr, NULL);
    //7.赋值
    for (i = 0; i < alen; i++) {
        j_arr[i] = (jbyte) ((jbyte)(b_arr[i])^key);
        key = b_arr[i];
    }

    jstring result2 = (*env)->NewStringUTF(env, jarr);
    //8.释放资源
   /* (*env)->ReleaseByteArrayElements(env,jarr, j_arr, 0);
    (*env)->ReleaseByteArrayElements(env,barr, b_arr, 0);*/
    return (*env)->NewStringUTF(env, "111");
}

JNIEXPORT void JNICALL Java_com_okay_testjni_jni_JNICall_decrypt
        (JNIEnv *env, jclass jobj, jbyteArray jbarray) {

}

#ifdef __cplusplus
}
#endif
#endif