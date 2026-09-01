#ifndef VOX_AUDIO_DECODER_H
#define VOX_AUDIO_DECODER_H

#include <string>
#include <vector>
#include <cstdint>

namespace vox {

struct DecodedAudio {
    std::vector<float> pcmSamples; // Mono 32-bit Float normalized (-1.0 to +1.0)
    int32_t sampleRate = 44100;
    int32_t channels = 2;
    int64_t durationUs = 0;
};

class AudioDecoder {
public:
    AudioDecoder() = default;
    ~AudioDecoder() = default;

    static bool decodeFileToPcm(const std::string& filePath, int32_t maxDurationSec, DecodedAudio& outAudio);
};

} // namespace vox

#endif // VOX_AUDIO_DECODER_H
