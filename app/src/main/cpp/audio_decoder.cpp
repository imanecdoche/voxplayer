#include "audio_decoder.h"
#include <media/NdkMediaExtractor.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaFormat.h>
#include <android/log.h>
#include <cmath>
#include <algorithm>

#define LOG_TAG "VoxAudioDecoder"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

namespace vox {

bool AudioDecoder::decodeFileToPcm(const std::string& filePath, int32_t maxDurationSec, DecodedAudio& outAudio) {
    AMediaExtractor* extractor = AMediaExtractor_new();
    if (!extractor) {
        LOGE("Failed to create AMediaExtractor");
        return false;
    }

    media_status_t status = AMediaExtractor_setDataSource(extractor, filePath.c_str());
    if (status != AMEDIA_OK) {
        LOGE("Failed to set data source: %s", filePath.c_str());
        AMediaExtractor_delete(extractor);
        return false;
    }

    int trackCount = AMediaExtractor_getTrackCount(extractor);
    int audioTrackIndex = -1;
    AMediaFormat* format = nullptr;
    const char* mime = nullptr;

    for (int i = 0; i < trackCount; ++i) {
        AMediaFormat* trackFormat = AMediaExtractor_getTrackFormat(extractor, i);
        if (AMediaFormat_getString(trackFormat, AMEDIAFORMAT_KEY_MIME, &mime)) {
            if (std::string(mime).find("audio/") == 0) {
                audioTrackIndex = i;
                format = trackFormat;
                break;
            }
        }
        AMediaFormat_delete(trackFormat);
    }

    if (audioTrackIndex < 0 || !format) {
        LOGE("No audio track found in: %s", filePath.c_str());
        AMediaExtractor_delete(extractor);
        return false;
    }

    AMediaExtractor_selectTrack(extractor, audioTrackIndex);

    int32_t sampleRate = 44100;
    int32_t channelCount = 2;
    int64_t durationUs = 0;

    AMediaFormat_getInt32(format, AMEDIAFORMAT_KEY_SAMPLE_RATE, &sampleRate);
    AMediaFormat_getInt32(format, AMEDIAFORMAT_KEY_CHANNEL_COUNT, &channelCount);
    AMediaFormat_getInt64(format, AMEDIAFORMAT_KEY_DURATION, &durationUs);

    outAudio.sampleRate = sampleRate;
    outAudio.channels = channelCount;
    outAudio.durationUs = durationUs;

    AMediaCodec* codec = AMediaCodec_createDecoderByType(mime);
    if (!codec) {
        LOGE("Failed to create codec for mime: %s", mime);
        AMediaFormat_delete(format);
        AMediaExtractor_delete(extractor);
        return false;
    }

    AMediaCodec_configure(codec, format, nullptr, nullptr, 0);
    AMediaCodec_start(codec);
    AMediaFormat_delete(format);

    int64_t maxDurationUs = (maxDurationSec > 0) ? (static_cast<int64_t>(maxDurationSec) * 1000000LL) : INT64_MAX;
    bool sawInputEOS = false;
    bool sawOutputEOS = false;

    std::vector<float> monoSamples;
    monoSamples.reserve(sampleRate * std::min(maxDurationSec, 60));

    const int64_t TIMEOUT_US = 5000;

    while (!sawOutputEOS) {
        if (!sawInputEOS) {
            ssize_t inputIndex = AMediaCodec_dequeueInputBuffer(codec, TIMEOUT_US);
            if (inputIndex >= 0) {
                size_t bufferSize = 0;
                uint8_t* inputBuffer = AMediaCodec_getInputBuffer(codec, inputIndex, &bufferSize);
                if (inputBuffer) {
                    ssize_t sampleSize = AMediaExtractor_readSampleData(extractor, inputBuffer, bufferSize);
                    int64_t presentationTimeUs = AMediaExtractor_getSampleTime(extractor);

                    if (sampleSize < 0 || presentationTimeUs > maxDurationUs) {
                        sawInputEOS = true;
                        AMediaCodec_queueInputBuffer(codec, inputIndex, 0, 0, 0, AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        AMediaCodec_queueInputBuffer(codec, inputIndex, 0, sampleSize, presentationTimeUs, 0);
                        AMediaExtractor_advance(extractor);
                    }
                }
            }
        }

        AMediaCodecBufferInfo info;
        ssize_t outputIndex = AMediaCodec_dequeueOutputBuffer(codec, &info, TIMEOUT_US);

        if (outputIndex >= 0) {
            if (info.flags & AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM) {
                sawOutputEOS = true;
            }

            size_t outputBufferSize = 0;
            uint8_t* outputBuffer = AMediaCodec_getOutputBuffer(codec, outputIndex, &outputBufferSize);

            if (outputBuffer && info.size > 0) {
                const int16_t* pcm16 = reinterpret_cast<const int16_t*>(outputBuffer + info.offset);
                size_t sampleCount = info.size / sizeof(int16_t);

                if (channelCount >= 2) {
                    // Mix down stereo to mono float
                    for (size_t i = 0; i + 1 < sampleCount; i += channelCount) {
                        float left = static_cast<float>(pcm16[i]) / 32768.0f;
                        float right = static_cast<float>(pcm16[i + 1]) / 32768.0f;
                        monoSamples.push_back((left + right) * 0.5f);
                    }
                } else {
                    for (size_t i = 0; i < sampleCount; ++i) {
                        monoSamples.push_back(static_cast<float>(pcm16[i]) / 32768.0f);
                    }
                }
            }

            AMediaCodec_releaseOutputBuffer(codec, outputIndex, false);

            if (info.presentationTimeUs >= maxDurationUs) {
                sawOutputEOS = true;
            }
        } else if (outputIndex == AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED) {
            AMediaFormat* newFormat = AMediaCodec_getOutputFormat(codec);
            AMediaFormat_getInt32(newFormat, AMEDIAFORMAT_KEY_SAMPLE_RATE, &outAudio.sampleRate);
            AMediaFormat_getInt32(newFormat, AMEDIAFORMAT_KEY_CHANNEL_COUNT, &outAudio.channels);
            AMediaFormat_delete(newFormat);
        }
    }

    AMediaCodec_stop(codec);
    AMediaCodec_delete(codec);
    AMediaExtractor_delete(extractor);

    outAudio.pcmSamples = std::move(monoSamples);
    return !outAudio.pcmSamples.empty();
}

} // namespace vox
