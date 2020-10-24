//
// Created by Leo Liu on 2020/10/23.
// 来源：https://www.jb51.net/article/109652.htm
//
#include "jni.h"
#include<android/log.h>
#include <cstdio>
#include "sstream"
#include <lame/lame.h>

#define LOG_TAG "System.out.c"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)


static lame_global_flags *glf = NULL;

extern "C" void Java_com_baiyuas_record_Lame_close(
        JNIEnv *env, jclass type) {
    LOGD("lpc-->回收资源");
    lame_close(glf);
    glf = NULL;
}

extern "C" jint Java_com_baiyuas_record_Lame_encode(
        JNIEnv *env, jclass type,
        jshortArray buffer_l_,
        jshortArray buffer_r_,
        jint samples,
        jbyteArray mp3buf_) {
    LOGD("lpc-->开始转换");
    jshort *buffer_l = env->GetShortArrayElements(buffer_l_, NULL);
    jshort *buffer_r = env->GetShortArrayElements(buffer_r_, NULL);
    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);

    const jsize mp3buf_size = env->GetArrayLength(mp3buf_);

    int result = lame_encode_buffer(glf, buffer_l, buffer_r, samples, (u_char *) mp3buf,
                                    mp3buf_size);

    env->ReleaseShortArrayElements(buffer_l_, buffer_l, 0);
    env->ReleaseShortArrayElements(buffer_r_, buffer_r, 0);
    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);
    LOGD("lpc-->转换完成");
    return result;
}

extern "C" jint Java_com_baiyuas_record_Lame_flush(
        JNIEnv *env, jclass type,
        jbyteArray mp3buf_) {
    LOGD("lpc-->刷新缓存区");
    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);
    const jsize mp3buf_size = env->GetArrayLength(mp3buf_);
    int result = lame_encode_flush(glf, (u_char *) mp3buf, mp3buf_size);
    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);
    return result;
}

extern "C" void
Java_com_baiyuas_record_Lame_init(
        JNIEnv *env, jclass type,
        jint inSampleRate,
        jint outChannel,
        jint outSampleRate,
        jint outBitrate,
        jint quality) {

    std::stringstream temp;
    temp << "lpc-->初始化Lame, 当前版本: " << get_lame_version();
    LOGD("%s", temp.str().c_str());

    if (glf != NULL) {
        lame_close(glf);
        glf = NULL;
    }
    glf = lame_init();
    lame_set_in_samplerate(glf, inSampleRate);
    lame_set_num_channels(glf, outChannel);
    lame_set_out_samplerate(glf, outSampleRate);
    lame_set_brate(glf, outBitrate);
    lame_set_quality(glf, quality);
    lame_init_params(glf);
}