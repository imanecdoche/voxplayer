package com.vox.music.core.audio.chords

import com.vox.music.core.model.ChordEvent
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChordTracker @Inject constructor() {

    fun parseChordJson(jsonStr: String): List<ChordEvent> {
        if (jsonStr.isBlank() || jsonStr == "[]") return emptyList()
        val list = mutableListOf<ChordEvent>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val timeSec = obj.optDouble("time", 0.0)
                val chord = obj.optString("chord", "")
                if (chord.isNotBlank()) {
                    list.add(ChordEvent(timestampMs = (timeSec * 1000).toLong(), chord = chord))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedBy { it.timestampMs }
    }

    fun findActiveChord(chords: List<ChordEvent>, positionMs: Long): ChordEvent? {
        if (chords.isEmpty()) return null
        if (positionMs < chords.first().timestampMs) return chords.first()

        var low = 0
        var high = chords.size - 1
        var bestIndex = 0

        while (low <= high) {
            val mid = (low + high) ushr 1
            val chordTime = chords[mid].timestampMs

            if (chordTime <= positionMs) {
                bestIndex = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        return chords[bestIndex]
    }

    fun getUpcomingChords(chords: List<ChordEvent>, positionMs: Long, maxCount: Int = 4): List<ChordEvent> {
        if (chords.isEmpty()) return emptyList()
        val activeIndex = chords.indexOfLast { it.timestampMs <= positionMs }
        val startIndex = if (activeIndex >= 0) activeIndex + 1 else 0
        return chords.drop(startIndex).take(maxCount)
    }
}
