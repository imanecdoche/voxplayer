#ifndef VOX_SIGNAL_PROCESSOR_H
#define VOX_SIGNAL_PROCESSOR_H

#include <vector>
#include <string>
#include <cstdint>

namespace vox {

struct SignalAnalysisResult {
    float bpm = 0.0f;
    std::string musicalKey = "";
    float confidence = 0.0f;
};

struct ChordSegment {
    float timeSec = 0.0f;
    std::string chord = "";
};

class SignalProcessor {
public:
    SignalProcessor();
    ~SignalProcessor();

    static SignalAnalysisResult analyzeAudioSignal(const float* pcmSamples, size_t sampleCount, int32_t sampleRate);
    static std::string analyzeChordProgressionJson(const float* pcmSamples, size_t sampleCount, int32_t sampleRate);

private:
    static float estimateBpm(const float* pcmSamples, size_t sampleCount, int32_t sampleRate);
    static std::string estimateKey(const float* pcmSamples, size_t sampleCount, int32_t sampleRate);
    static std::string detectChordFromWindow(const float* pcmSamples, size_t windowSize, int32_t sampleRate);
};

} // namespace vox

#endif // VOX_SIGNAL_PROCESSOR_H
