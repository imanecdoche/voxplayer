#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "audio_decoder.h"
#include "signal_processor.h"

#define LOG_TAG "VoxDspJni"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_vox_music_core_native_1dsp_NativeDspEngine_initDspEngine(
    JNIEnv* env,
    jobject /* this */) {
    LOGI("Vox DSP Engine Initialized (C++17)");
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_vox_music_core_native_1dsp_NativeDspEngine_releaseDspEngine(
    JNIEnv* env,
    jobject /* this */) {
    LOGI("Vox DSP Engine Released");
}

JNIEXPORT jlong JNICALL
Java_com_vox_music_core_native_1dsp_NativeDspEngine_processAudioBuffer(
    JNIEnv* env,
    jobject /* this */,
    jfloatArray pcmArray,
    jint sampleRate) {
    if (!pcmArray) return 0;

    jsize length = env->GetArrayLength(pcmArray);
    jfloat* pcm = env->GetFloatArrayElements(pcmArray, nullptr);
    if (!pcm) return 0;

    vox::SignalAnalysisResult result = vox::SignalProcessor::analyzeAudioSignal(pcm, length, sampleRate);
    env->ReleaseFloatArrayElements(pcmArray, pcm, JNI_ABORT);

    return static_cast<jlong>(result.bpm);
}

JNIEXPORT jobject JNICALL
Java_com_vox_music_core_native_1dsp_NativeDspEngine_detectBpmAndKeyNative(
    JNIEnv* env,
    jobject /* this */,
    jfloatArray pcmArray,
    jint sampleRate) {
    if (!pcmArray) return nullptr;

    jsize length = env->GetArrayLength(pcmArray);
    jfloat* pcm = env->GetFloatArrayElements(pcmArray, nullptr);
    if (!pcm) return nullptr;

    vox::SignalAnalysisResult result = vox::SignalProcessor::analyzeAudioSignal(pcm, length, sampleRate);
    env->ReleaseFloatArrayElements(pcmArray, pcm, JNI_ABORT);

    jclass resultClass = env->FindClass("com/vox/music/core/native_dsp/NativeAnalysisResult");
    if (!resultClass) return nullptr;

    jmethodID constructor = env->GetMethodID(resultClass, "<init>", "(FLjava/lang/String;F)V");
    if (!constructor) return nullptr;

    jstring keyString = env->NewStringUTF(result.musicalKey.c_str());
    jobject obj = env->NewObject(resultClass, constructor, result.bpm, keyString, result.confidence);

    return obj;
}

JNIEXPORT jfloatArray JNICALL
Java_com_vox_music_core_native_1dsp_NativeDspEngine_decodeAudioFileNative(
    JNIEnv* env,
    jobject /* this */,
    jstring filePathStr,
    jint maxDurationSec) {
    if (!filePathStr) return nullptr;

    const char* filePath = env->GetStringUTFChars(filePathStr, nullptr);
    if (!filePath) return nullptr;

    vox::DecodedAudio decoded;
    bool success = vox::AudioDecoder::decodeFileToPcm(std::string(filePath), maxDurationSec, decoded);
    env->ReleaseStringUTFChars(filePathStr, filePath);

    if (!success || decoded.pcmSamples.empty()) {
        return nullptr;
    }

    jfloatArray resultArray = env->NewFloatArray(decoded.pcmSamples.size());
    if (!resultArray) return nullptr;

    env->SetFloatArrayRegion(resultArray, 0, decoded.pcmSamples.size(), decoded.pcmSamples.data());
    return resultArray;
}

JNIEXPORT jfloat JNICALL
Java_com_vox_music_core_native_1dsp_NativeDspEngine_detectBpm(
    JNIEnv* env,
    jobject /* this */,
    jstring filePathStr) {
    if (!filePathStr) return 0.0f;

    const char* filePath = env->GetStringUTFChars(filePathStr, nullptr);
    if (!filePath) return 0.0f;

    vox::DecodedAudio decoded;
    bool success = vox::AudioDecoder::decodeFileToPcm(std::string(filePath), 30, decoded);
    env->ReleaseStringUTFChars(filePathStr, filePath);

    if (!success || decoded.pcmSamples.empty()) {
        return 0.0f;
    }

    vox::SignalAnalysisResult result = vox::SignalProcessor::analyzeAudioSignal(
        decoded.pcmSamples.data(), decoded.pcmSamples.size(), decoded.sampleRate
    );

    return result.bpm;
}

JNIEXPORT jstring JNICALL
Java_com_vox_music_core_native_1dsp_NativeDspEngine_detectMusicalKey(
    JNIEnv* env,
    jobject /* this */,
    jstring filePathStr) {
    if (!filePathStr) return env->NewStringUTF("");

    const char* filePath = env->GetStringUTFChars(filePathStr, nullptr);
    if (!filePath) return env->NewStringUTF("");

    vox::DecodedAudio decoded;
    bool success = vox::AudioDecoder::decodeFileToPcm(std::string(filePath), 30, decoded);
    env->ReleaseStringUTFChars(filePathStr, filePath);

    if (!success || decoded.pcmSamples.empty()) {
        return env->NewStringUTF("");
    }

    vox::SignalAnalysisResult result = vox::SignalProcessor::analyzeAudioSignal(
        decoded.pcmSamples.data(), decoded.pcmSamples.size(), decoded.sampleRate
    );

    return env->NewStringUTF(result.musicalKey.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_vox_music_core_native_1dsp_NativeDspEngine_analyzeChordProgression(
    JNIEnv* env,
    jobject /* this */,
    jstring filePathStr) {
    if (!filePathStr) return env->NewStringUTF("[]");

    const char* filePath = env->GetStringUTFChars(filePathStr, nullptr);
    if (!filePath) return env->NewStringUTF("[]");

    vox::DecodedAudio decoded;
    bool success = vox::AudioDecoder::decodeFileToPcm(std::string(filePath), 120, decoded); // analyze up to 2 mins
    env->ReleaseStringUTFChars(filePathStr, filePath);

    if (!success || decoded.pcmSamples.empty()) {
        return env->NewStringUTF("[]");
    }

    std::string chordJson = vox::SignalProcessor::analyzeChordProgressionJson(
        decoded.pcmSamples.data(), decoded.pcmSamples.size(), decoded.sampleRate
    );

    return env->NewStringUTF(chordJson.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_vox_music_core_native_1dsp_NativeDspEngine_analyzeChordProgressionFromPcm(
    JNIEnv* env,
    jobject /* this */,
    jfloatArray pcmArray,
    jint sampleRate) {
    if (!pcmArray) return env->NewStringUTF("[]");

    jsize length = env->GetArrayLength(pcmArray);
    jfloat* pcm = env->GetFloatArrayElements(pcmArray, nullptr);
    if (!pcm) return env->NewStringUTF("[]");

    std::string chordJson = vox::SignalProcessor::analyzeChordProgressionJson(
        pcm, length, sampleRate
    );
    env->ReleaseFloatArrayElements(pcmArray, pcm, JNI_ABORT);

    return env->NewStringUTF(chordJson.c_str());
}

} // extern "C"
