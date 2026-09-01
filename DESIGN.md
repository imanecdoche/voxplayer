# **Technical Design Document (DESIGN.md)**  
## **Proyek: Minimalist Offline Music Player & Audio Utility**  
**Dokumen Versi:** 1.0.0  
   
**Status:** Architecture & UI/UX Ready  
   
**Platform Target:** Android Native (API 29 - API 35+)   
   
**Tech Stack:** Kotlin 2.0+, Jetpack Compose, Jetpack Media3, Room, NDK (C++)   
   
## **1. High-Level Architecture Pattern**  
Aplikasi ini menggunakan **Clean Architecture** yang dikombinasikan dengan arsitektur UI  **MVI (Model-View-Intent) / Unidirectional Data Flow (UDF)**.   
   
                ┌────────────────────────────────────────────────────────┐  
                │                 Jetpack Compose UI                     │  
                │   (Monochrome Flat Design, Screen, Components, Theme)  │  
                └───────────────────────────▲────────────────────────────┘  
                                            │  
                             StateFlow<UiState> / Events  
                                            │  
                ┌───────────────────────────▼────────────────────────────┐  
                │                    ViewModel (MVI)                     │  
                │         (State Reducer, Single Source of Truth)        │  
                └───────────────────────────▲────────────────────────────┘  
                                            │  
                                  Use Cases / Domain  
                                            │  
            ┌───────────────────────────────┴───────────────────────────────┐  
            │                                                               │  
┌───────────▼────────────┐     ┌────────────────────────┐      ┌────────────▼───────────┐  
│     Audio Core         │     │     Storage & Data     │      │     NDK / DSP Engine   │  
│ ∙ MediaSessionService  │     │ ∙ MediaStore Scanner   │      │ ∙ Aubio C++ via JNI    │  
│ ∙ Jetpack Media3       │     │ ∙ Room Local DB        │      │ ∙ BPM & Key Extraction │  
│ ∙ SonicAudioProcessor  │     │ ∙ Jaudiotagger Engine  │      │ ∙ Chroma Chord Parsing │  
│ ∙ Hardware Equalizer   │     │ ∙ FFmpeg / MediaMuxer  │      │ ∙ Waveform Generator   │  
└────────────────────────┘     └────────────────────────┘      └────────────────────────┘  
   
## **2. Multi-Module Project Structure**  
Untuk memisahkan tanggung jawab kode secara modular, proyek dibagi menjadi beberapa layer modul Gradle:  
   
├── app/                                 # Inisialisasi Hilt DI, App Entry Point, Single Activity  
├── core/  
│   ├── common/                          # Result wrappers, Dispatcher providers, Extensions  
│   ├── model/                           # Pure Kotlin data classes & domain models  
│   ├── database/                        # Room Database, DAOs, Entities, TypeConverters  
│   ├── storage/                         # MediaStore resolver, SAF utilities, ContentObserver  
│   ├── audio/                           # Jetpack Media3 ExoPlayer, Service, DSP Processors  
│   ├── dsp-native/                      # C++ source (Aubio/SoundTouch), CMakeLists, JNI bridge  
│   └── tagger/                          # Jaudiotagger wrapper, ID3/Vorbis parsing, FFmpeg clipper  
└── feature/  
    ├── library/                         # Directory folder view, track lists, custom tags  
    ├── player/                          # Scrubber, A-B loop, speed/pitch sheet, EQ screen  
    ├── analysis/                        # BPM/Key inspector, live chord visualization  
    ├── clipper/                         # Lossless visual audio waveform trimmer  
    └── metadata-editor/                 # Tag editing, lyric editor, cover art picker  
   
## **3. UI/UX Design System (Ultra-Minimalist Monochrome)**  
### **3.1. Design Tokens & Color Palette**  
Visual interface dirancang menggunakan styling monokrom murni tanpa drop shadow atau container bergradien:   
   
| | | | |  
|-|-|-|-|  
| **Token** | **Dark Mode (Default)** | **Light Mode** | **Penggunaan** |   
| colorBackground | #000000   | #FFFFFF   | Kanvas layar penuh |   
| colorOnBackground | #FFFFFF   | #000000   | Teks utama, ikon aktif, playhead |   
| colorSubtle | #757575   | #757575   | Metadata teks sekunder, ikon inaktif |   
| colorDivider | #222222   | #E0E0E0   | Hairline horizontal separator (0.5.dp)   |   
| colorMarkerAB | #FFFFFF   | #000000   | Penanda Point A & Point B (garis vertikal 1px) |   
### **3.2. Layout Rules & Component Guidelines**  
- **Zero-Container Rule:** Dilarang membungkus item list atau kontrol dalam Card, ElevatedCard, atau Surface yang memiliki border tebal / bayangan.   
-    
- **Separation via Typography & Whitespace:** Hirarki visual dibangun menggunakan kontras ketebalan teks (FontWeight.Bold vs FontWeight.Light), ukuran font, dan Spacer.   
-    
- **Cover Art:** Rasio 1:1 tajam (*sharp edge*, shape = RectangleShape) tanpa sudut melengkung ( *zero corner radius*).   
-    
- **Scrubber Slider:** Garis track horizontal tipis 1.5.dp dengan thumb lingkaran solid diameter 8.dp.   
-    
### **3.3. ASCII UI Layout Mockups**  
#### ***A. Main Library (Folder & Directory View)***  
┌────────────────────────────────────────────────────────┐  
│ DIRECTORIES                               [SEARCH] [=] │  
├────────────────────────────────────────────────────────┤  
│                                                        │  
│ /Storage/Music/Acoustic Sessions                       │  
│ 14 tracks  •  48m 22s                                  │  
│ ────────────────────────────────────────────────────── │  
│ /Storage/Download/Mastering Export                     │  
│ 6 tracks  •  18m 05s                                   │  
│ ────────────────────────────────────────────────────── │  
│ /Storage/Music/Synthwave Collection                    │  
│ 32 tracks  •  2h 10m                                   │  
│                                                        │  
├────────────────────────────────────────────────────────┤  
│ [■] Track Title Here                 [PREV] [▶] [NEXT] │  
│     Artist Name  •  120 BPM  •  A Minor                │  
└────────────────────────────────────────────────────────┘  
   
#### ***B. Full Player Screen (DSP & Scrubber)***  
┌────────────────────────────────────────────────────────┐  
│ [∨]                   NOW PLAYING                  [:] │  
│                                                        │  
│ ┌────────────────────────────────────────────────────┐ │  
│ │                                                    │ │  
│ │                                                    │ │  
│ │                 [ 1:1 COVER ART ]                  │ │  
│ │                                                    │ │  
│ │                                                    │ │  
│ └────────────────────────────────────────────────────┘ │  
│                                                        │  
│ Track Title Long Name                                  │  
│ Artist Name — Album Title (2024)                       │  
│                                                        │  
│ [ 128 BPM ]      [ KEY: C# MINOR ]      [ 44.1kHz/24b] │  
│                                                        │  
│ 01:24 ────[A]─────────●──────────────[B]──────── 03:45 │  
│                                                        │  
│   [🔀]       [⏮]        [ ▶ ]        [⏭]       [🔁]   │  
│                                                        │  
│ ────────────────────────────────────────────────────── │  
│ SPEED: 1.00x   │   PITCH: +2 st   │   A-B: ACTIVE      │  
└────────────────────────────────────────────────────────┘  
   
## **4. Subsystem Design & Technical Implementation**  
### **4.1. Audio Core Engine & MediaSession Architecture**  
Playback berjalan pada **Foreground Service** berbasis MediaSessionService milik Jetpack Media3.   
   
                 ┌────────────────────────────────┐  
                 │       MediaController          │  
                 │   (UI / Compose ViewModel)     │  
                 └───────────────▲────────────────┘  
                                 │ Binder IPC  
                 ┌───────────────▼────────────────┐  
                 │     MediaSessionService        │  
                 │ ∙ Audio Focus Request Listener │  
                 │ ∙ Notification Provider        │  
                 │ ∙ MediaSessionCallback         │  
                 └───────────────▲────────────────┘  
                                 │  
                 ┌───────────────▼────────────────┐  
                 │           ExoPlayer            │  
                 └───────────────▲────────────────┘  
                                 │  
    ┌────────────────────────────┴────────────────────────────┐  
    │                                                         │  
┌───▼──────────────────────────┐           ┌──────────────────▼───────────────────┐  
│     SonicAudioProcessor      │           │      DynamicsProcessing / EQ         │  
│ ∙ Speed: 0.25x – 3.00x       │           │ ∙ Graphic Equalizer (5/10 Bands)     │  
│ ∙ Pitch: -12 to +12 Semitone │           │ ∙ Bass Boost & Loudness Enhancer     │  
└──────────────────────────────┘           └──────────────────────────────────────┘  
   
- **Pitch & Speed Decoupling:** SonicAudioProcessor diinjeksikan langsung ke dalam DefaultRenderersFactory melalui pipeline audio processor Media3.   
-    
- **A-B Loop Logic:** Implementasi custom Player.Listener yang memantau nilai exoPlayer.currentPosition. Saat playback melewati Point_B, eksekusi exoPlayer.seekTo(Point_A) secara instan tanpa memutus audio stream.   
-    
- **Battery Optimization Handler:** Memanggil intent Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS saat inisialisasi pertama untuk mencegah sistem membunuh background service.   
-    
### **4.2. Storage Scanner & MediaStore Synchronization**  
Pemindaian direktori dilakukan menggunakan ContentResolver yang dikelompokkan berdasarkan direktori fisik induk:   
   
Kotlin  
// Ekstraksi Folder Path dari Relative Path / Data Columnval projection = arrayOf(     MediaStore.Audio.Media._ID,     MediaStore.Audio.Media.DATA,     MediaStore.Audio.Media.TITLE,     MediaStore.Audio.Media.ARTIST,     MediaStore.Audio.Media.ALBUM,     MediaStore.Audio.Media.DURATION,     MediaStore.Audio.Media.MIME_TYPE,     MediaStore.Audio.Media.DATE_MODIFIED )  val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"  
- **ContentObserver:** Mendaftarkan observer pada MediaStore.Audio.Media.EXTERNAL_CONTENT_URI untuk sinkronisasi otomatis jika ada file baru diunduh/dihapus oleh aplikasi lain.   
-    
- **Directory Aggregation:** Hasil query dipetakan ke objek DirectoryGroup berdasarkan substring path direktori sebelum disimpan ke Room DB untuk caching cepat.   
-    
### **4.3. NDK Audio Signal Analysis Engine (BPM, Key, Chord)**  
Pemrosesan DSP tingkat lanjut dijalankan di layer C++ menggunakan library native Aubio/SoundTouch yang dihubungkan melalui JNI.   
   
[ Audio File (.mp3/.wav) ]  
            │  
            ▼ (Decoded to PCM 16-bit Mono via MediaCodec)  
[ Native JNI Interface: Java_com_app_dsp_NativeAudioAnalyzer ]  
            │  
    ┌───────┴───────────────────────────────┐  
    │                                       │  
    ▼ (Aubio Tempo Extraction)              ▼ (Chroma Pitch Class Extraction)  
[ Peak Energy Spectral Filter ]         [ 12-Bin Pitch Chroma Calculation ]  
    │                                       │  
    ▼                                       ▼  
[ Output: BPM Float ]                   [ Musical Key & Chord Matcher ]  
   
- **Thread Safety:** Seluruh komputasi DSP dijalankan di thread background berprioritas rendah (Dispatchers.Default atau WorkManager) agar tidak mengganggu rendering 60/120 FPS pada UI Compose.   
-    
- **Result Caching:** Nilai BPM, Musical Key, dan serialisasi JSON progresi akor langsung disimpan ke tabel audio_analysis_cache di Room DB. Analisis hanya diproses ulang jika dateModified pada berkas berubah.   
-    
### **4.4. Audio Clipper & Trimmer Engine**  
Fitur pemotongan audio mendukung dua metode ekspor:   
   
                                [ Audio Source File ]  
                                         │  
                         ┌───────────────┴───────────────┐  
                         │                               │  
            (Opsi A: Fast Lossless)            (Opsi B: Custom Encoding)  
                         │                               │  
                         ▼                               ▼  
               [ MediaExtractor ]                [ FFmpeg-Kit ]  
                 (Seek Keyframe)                         │  
                         │                   (Bitrate / Codec Conversion)  
                         ▼                               │  
                 [ MediaMuxer ]                          │  
              (Direct Sample Copy)                       │  
                         │                               │  
                         └───────────────┬───────────────┘  
                                         ▼  
                         [ /Music/Clipped/output.mp3 ]  
                                         │  
                         [ MediaScannerConnection Sync ]  
   
- **Lossless Clipping:** Menggunakan MediaExtractor dan MediaMuxer untuk menyalin packet stream tanpa decoding/encoding ulang, menghasilkan waktu pemotongan < 1 detik.   
-    
- **MediaStore Register:** File hasil klip langsung didaftarkan ke MediaStore agar terdeteksi seketika di library.   
-    
### **4.5. Metadata & ID3 Tag Editor Flow**  
Untuk mematuhi Android Scoped Storage (API 29+):   
   
[ User Changes Tag in UI ]  
            │  
            ▼  
[ Android Version Check ]  
    ├── API < 30 : Langsung tulis berkas fisik via Jaudiotagger  
    └── API >= 30: Request Write Permission via MediaStore.createWriteRequest(listOf(uri))  
            │  
            ▼ (User Grants Permission via System Dialog)  
[ Jaudiotagger Engine: Update ID3v2.4 / Vorbis / MP4 Metadata ]  
            │  
            ▼  
[ Force MediaScannerConnection.scanFile() ]  
            │  
            ▼  
[ Update Room Database Entity ]  
   
## **5. Unidirectional Data Flow (MVI) Specification**  
Setiap layar mengimplementasikan tiga komponen state:  
   
Kotlin  
// 1. Immutable UI Statedata class PlayerUiState(     val currentTrack: Track? = null,     val isPlaying: Boolean = false,     val playbackPositionMs: Long = 0L,     val durationMs: Long = 0L,     val playbackSpeed: Float = 1.0f,     val pitchSemitones: Int = 0,     val loopMode: LoopMode = LoopMode.NONE,     val isShuffleEnabled: Boolean = false,     val abLoop: Pair<Long?, Long?> = Pair(null, null),     val bpm: Float? = null,     val musicalKey: String? = null )  // 2. User Intent / Eventssealed interface PlayerIntent {     data object TogglePlayPause : PlayerIntent     data object SkipNext : PlayerIntent     data object SkipPrevious : PlayerIntent     data class SeekTo(val positionMs: Long) : PlayerIntent     data class SetSpeed(val speed: Float) : PlayerIntent     data class SetPitch(val semitones: Int) : PlayerIntent     data object ToggleLoopMode : PlayerIntent     data object SetPointA : PlayerIntent     data object SetPointB : PlayerIntent     data object ClearABLoop : PlayerIntent }  // 3. Side Effects (One-time actions)sealed interface PlayerSideEffect {     data class ShowToast(val message: String) : PlayerSideEffect     data object RequestBatteryOptimization : PlayerSideEffect }   
## **6. Permissions & Manifest Configuration**  
Konfigurasi permission yang wajib dideklarasikan di AndroidManifest.xml:  
   
XML  
<manifest xmlns:android="http://schemas.android.com/apk/res/android">      <!-- Audio & Storage Permissions -->     <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />     <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="29" />      <!-- Foreground Service & MediaSession -->     <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />     <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />     <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />     <uses-permission android:name="android.permission.WAKE_LOCK" />      <!-- Battery Optimization Exemption -->     <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />      <application         android:name=".MusicApplication"         android:theme="@style/Theme.MonochromeMusic">                  <service             android:name=".core.audio.service.MusicPlaybackService"             android:foregroundServiceType="mediaPlayback"             android:exported="true">             <intent-filter>                 <action android:name="androidx.media3.session.MediaSessionService" />             </intent-filter>         </service>     </application></manifest>  
## **7. Testing & Quality Assurance Strategy**  
- **Unit Testing (JUnit 5 + Mockk):**  
-    
  - Pengujian state transitions pada ViewModel (MVI reducer verification).  
  -    
  - Pengujian parsing file lirik .lrc dan ekstraksi tag metadata.  
  -    
  - Logika A-B Loop dan time format converters (formatMsToTimestamp).  
  -    
- **Audio Pipeline Verification:**  
-    
  - Pengujian integrasi SonicAudioProcessor memastikan perubahan pitch tidak mengubah durasi track saat playback.  
  -    
  - Uji coba gapless transition antar antrean audio.  
  -    
- **Storage Edge Cases:**  
-    
  - Pengujian pemindaian MediaStore pada folder bersarang (*nested directories*).  
  -    
  - Pengujian penanganan Scoped Storage write permission pada Android 11, 12, 13, dan 14+.  
   
