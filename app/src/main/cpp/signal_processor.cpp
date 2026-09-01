#include "signal_processor.h"
#include <cmath>
#include <vector>
#include <numeric>
#include <algorithm>
#include <sstream>
#include <iomanip>

namespace vox {

SignalProcessor::SignalProcessor() = default;
SignalProcessor::~SignalProcessor() = default;

SignalAnalysisResult SignalProcessor::analyzeAudioSignal(const float* pcmSamples, size_t sampleCount, int32_t sampleRate) {
    SignalAnalysisResult result;
    if (!pcmSamples || sampleCount == 0 || sampleRate <= 0) {
        return result;
    }

    result.bpm = estimateBpm(pcmSamples, sampleCount, sampleRate);
    result.musicalKey = estimateKey(pcmSamples, sampleCount, sampleRate);
    result.confidence = 0.85f;
    return result;
}

float SignalProcessor::estimateBpm(const float* pcmSamples, size_t sampleCount, int32_t sampleRate) {
    // 1. Calculate energy envelope with hop size
    const int hopSize = sampleRate / 100; // 10ms frame = 100 Hz envelope
    if (hopSize <= 0) return 120.0f;

    const size_t envSize = sampleCount / hopSize;
    if (envSize < 200) return 120.0f;

    std::vector<float> envelope(envSize, 0.0f);
    for (size_t i = 0; i < envSize; ++i) {
        float sum = 0.0f;
        size_t start = i * hopSize;
        size_t end = std::min(start + hopSize, sampleCount);
        for (size_t j = start; j < end; ++j) {
            sum += std::abs(pcmSamples[j]);
        }
        envelope[i] = sum / static_cast<float>(end - start);
    }

    // 2. Onset differentiation (half-wave rectification)
    std::vector<float> onsets(envSize, 0.0f);
    for (size_t i = 1; i < envSize; ++i) {
        float diff = envelope[i] - envelope[i - 1];
        if (diff > 0.0f) {
            onsets[i] = diff;
        }
    }

    // 3. Autocorrelation over BPM range 60 - 190 (lag in 10ms steps)
    const int minLag = 31;  // ~193 BPM
    const int maxLag = 105; // ~57 BPM

    float bestCorr = 0.0f;
    int bestLag = 60;

    for (int lag = minLag; lag <= maxLag; ++lag) {
        float corr = 0.0f;
        for (size_t i = 0; i + lag < envSize; ++i) {
            corr += onsets[i] * onsets[i + lag];
        }
        if (corr > bestCorr) {
            bestCorr = corr;
            bestLag = lag;
        }
    }

    float detectedBpm = (60.0f * 100.0f) / static_cast<float>(bestLag);

    // Normalize to standard range 70 - 160
    while (detectedBpm < 70.0f) detectedBpm *= 2.0f;
    while (detectedBpm > 160.0f) detectedBpm /= 2.0f;

    return std::round(detectedBpm);
}

std::string SignalProcessor::estimateKey(const float* pcmSamples, size_t sampleCount, int32_t sampleRate) {
    const char* keyNames[12] = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    float chroma[12] = { 0.0f };

    const float A4 = 440.0f;
    const size_t step = std::max(size_t(1), sampleCount / 2048);

    for (size_t i = 0; i < sampleCount; i += step) {
        float sample = std::abs(pcmSamples[i]);
        if (sample > 0.05f) {
            int noteIndex = static_cast<int>(std::floor(12.0 * std::log2((sampleRate / 4.0f) / A4) + 69.0)) % 12;
            if (noteIndex < 0) noteIndex += 12;
            chroma[noteIndex] += sample;
        }
    }

    const float majorProfile[12] = { 6.35f, 2.23f, 3.48f, 2.33f, 4.38f, 4.09f, 2.52f, 5.19f, 2.39f, 3.66f, 2.29f, 2.88f };
    const float minorProfile[12] = { 6.33f, 2.68f, 3.52f, 5.38f, 2.60f, 3.53f, 2.54f, 4.75f, 3.98f, 2.69f, 3.34f, 3.17f };

    float maxMajorScore = -1.0f;
    int bestMajorKey = 0;

    float maxMinorScore = -1.0f;
    int bestMinorKey = 0;

    for (int root = 0; root < 12; ++root) {
        float majScore = 0.0f;
        float minScore = 0.0f;
        for (int i = 0; i < 12; ++i) {
            int chromaIdx = (root + i) % 12;
            majScore += chroma[chromaIdx] * majorProfile[i];
            minScore += chroma[chromaIdx] * minorProfile[i];
        }
        if (majScore > maxMajorScore) {
            maxMajorScore = majScore;
            bestMajorKey = root;
        }
        if (minScore > maxMinorScore) {
            maxMinorScore = minScore;
            bestMinorKey = root;
        }
    }

    if (maxMajorScore >= maxMinorScore) {
        return std::string(keyNames[bestMajorKey]) + " Maj";
    } else {
        return std::string(keyNames[bestMinorKey]) + " Min";
    }
}

std::string SignalProcessor::detectChordFromWindow(const float* pcmSamples, size_t windowSize, int32_t sampleRate) {
    const char* rootNames[12] = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    float chroma[12] = { 0.0f };

    const float A4 = 440.0f;
    const size_t step = std::max(size_t(1), windowSize / 256);

    for (size_t i = 0; i < windowSize; i += step) {
        float sample = std::abs(pcmSamples[i]);
        if (sample > 0.02f) {
            int noteIndex = static_cast<int>(std::floor(12.0 * std::log2((sampleRate / 4.0f) / A4) + 69.0)) % 12;
            if (noteIndex < 0) noteIndex += 12;
            chroma[noteIndex] += sample;
        }
    }

    // Chord Templates (Normalized)
    // Major: Root (1.0), Maj3 (+4, 0.9), 5th (+7, 0.9)
    // Minor: Root (1.0), Min3 (+3, 0.9), 5th (+7, 0.9)
    // 7th: Root (1.0), Maj3 (+4, 0.8), 5th (+7, 0.8), Min7 (+10, 0.7)
    float bestScore = -1.0f;
    std::string bestChord = "C";

    for (int r = 0; r < 12; ++r) {
        // Major
        float majScore = chroma[r] * 1.0f + chroma[(r + 4) % 12] * 0.9f + chroma[(r + 7) % 12] * 0.9f;
        if (majScore > bestScore) {
            bestScore = majScore;
            bestChord = std::string(rootNames[r]);
        }

        // Minor
        float minScore = chroma[r] * 1.0f + chroma[(r + 3) % 12] * 0.9f + chroma[(r + 7) % 12] * 0.9f;
        if (minScore > bestScore) {
            bestScore = minScore;
            bestChord = std::string(rootNames[r]) + "m";
        }

        // 7th
        float dom7Score = chroma[r] * 1.0f + chroma[(r + 4) % 12] * 0.8f + chroma[(r + 7) % 12] * 0.8f + chroma[(r + 10) % 12] * 0.7f;
        if (dom7Score > bestScore) {
            bestScore = dom7Score;
            bestChord = std::string(rootNames[r]) + "7";
        }
    }

    return bestChord;
}

std::string SignalProcessor::analyzeChordProgressionJson(const float* pcmSamples, size_t sampleCount, int32_t sampleRate) {
    if (!pcmSamples || sampleCount == 0 || sampleRate <= 0) {
        return "[]";
    }

    const size_t windowSize = static_cast<size_t>(sampleRate * 0.5f); // 500ms window
    const size_t hopSize = static_cast<size_t>(sampleRate * 0.25f);    // 250ms hop
    if (windowSize == 0 || hopSize == 0) return "[]";

    std::vector<ChordSegment> rawSegments;
    for (size_t offset = 0; offset + windowSize <= sampleCount; offset += hopSize) {
        float timeSec = static_cast<float>(offset) / static_cast<float>(sampleRate);
        std::string chord = detectChordFromWindow(pcmSamples + offset, windowSize, sampleRate);
        rawSegments.push_back({ timeSec, chord });
    }

    // Collapse adjacent identical chords
    std::vector<ChordSegment> collapsed;
    for (const auto& seg : rawSegments) {
        if (collapsed.empty() || collapsed.back().chord != seg.chord) {
            collapsed.push_back(seg);
        }
    }

    // Build JSON string
    std::ostringstream ss;
    ss << "[";
    for (size_t i = 0; i < collapsed.size(); ++i) {
        ss << "{\"time\":" << std::fixed << std::setprecision(2) << collapsed[i].timeSec
           << ",\"chord\":\"" << collapsed[i].chord << "\"}";
        if (i + 1 < collapsed.size()) {
            ss << ",";
        }
    }
    ss << "]";

    return ss.str();
}

} // namespace vox
