# Vox — Workspace Rules & Project Instructions

Dokumen ini merupakan acuan utama dan mutlak untuk proyek **Vox (Minimalist Offline Music Player & Audio Utility)**.

## 📌 Absolute Rules & Directives
1. **Aturan Mutlak**: Setiap ada instruksi tambahan baru, langsung catat ke `.agents/RULES.md` dan jadikan file instruksi ini sebagai acuan utama dan mutlak selalu benar.
2. **Platform & Stack**:
   - Platform: Android Native (Kotlin 2.0+, Kotlin DSL `build.gradle.kts`).
   - Target SDK: Android 10 (API 29) s/d Android 15 (API 35).
   - UI: Jetpack Compose + Material 3 (Monokrom murni).
   - Audio Core: Jetpack Media3 (ExoPlayer + MediaSessionService).
   - Database: AndroidX Room.
   - DI: Dagger Hilt.
3. **Design System & Visual Assets (Ultra-Minimalist Monochrome)**:
   - **Zero Container / Zero Card Elevation**: Dilarang menggunakan card elevated, drop shadows, atau container berlapis.
   - **Pure Monochrome**: Hanya menggunakan `#000000` (Amoled Black), `#FFFFFF` (White), `#757575` (Neutral Gray), `#222222` (Dark Divider), `#E0E0E0` (Light Divider).
   - **Typography-Driven & Whitespace Layout**: Hirarki dibangun menggunakan bobot font (`FontWeight`), `Spacer`, dan hairline dividers (`0.5.dp` - `1.dp`).
   - **Sharp Corners**: Cover art 1:1 tajam tanpa corner radius melengkung.
   - **Branding Assets**:
     - App Icon: `ic_launcher` & `ic_launcher_round` (`res/mipmap-*`).
     - Vector Logomark: `res/drawable/vox_main_logomark.xml` (`VoxLogomark` composable).
     - Vector Logotype: `res/drawable/vox_main_logotype.xml` (`VoxLogotype` composable).
     - Brand Header: `VoxHeader` composable monokrom flat tanpa container.
4. **Tahap Pengembangan**:
   - **Phase 1 Step 1**: Inisialisasi Proyek, Gradle Kotlin DSL, Version Catalog, Manifest Permissions & Foreground Service, Pure Monochrome Design Tokens. (DONE)
   - **Phase 1 Step 2**: Visual Assets Integration, Room Database Entity/DAO/Database, MediaStore Audio Scanner & ContentObserver, Clean Architecture Repository, Library Screen UI (Folder & Track Views) with MVI, Runtime Permissions Handling. (DONE)
   - **Phase 1 Step 3**: Jetpack Media3 (ExoPlayer + MediaSessionService), SonicAudioProcessor DSP Pipeline, Precision A-B Looping, PlayerController StateFlow, PlayerScreen & MiniPlayer UI. (DONE)
   - **Phase 2 Step 4**: SonicAudioProcessor Independent Speed (0.25x-3.00x) & Pitch Shifter (-12 s.t to +12 s.t) Engine, State/ViewModel Reducer, Precision Step Controls, Preset Chips, Flat Monochrome DSP Controller Sheet. (DONE)
   - **Phase 2 Step 5**: Multi-band Graphic Equalizer (Android AudioEffect), BassBoost, Virtualizer, LoudnessEnhancer, Preset Engine, Precision A-B Looper Visual Marker, EqualizerScreen Flat Monochrome. (DONE)
   - **Phase 2 Step 6**: Custom Playlists (Create/Add/Reorder/Delete), M3U/M3U8 Export-Import, Local Custom Tags, Scoped Storage File Operations (Rename & System Delete Request API 30+), Track Actions BottomSheet. (DONE)
   - **Phase 3 Step 7**: Metadata Inspector & ID3 Tag Editor (Jaudiotagger Engine, Title/Artist/Album/Year/Genre/Lyrics/Artwork binary read-write, Scoped Storage Write Request API 30+, MetadataEditorScreen & AudioInspectorBottomSheet). (DONE)
   - **Phase 3 Step 8**: Audio Clipper & Trimmer Engine (Visual Waveform Trimmer, Fast Lossless Trimming via MediaExtractor/MediaMuxer, Custom Bitrate Exporter, AudioClipperScreen Flat Monochrome). (DONE)
   - **Phase 3 Step 9**: Synced Lyrics Viewer & LRC Parser Engine (.lrc external parser, embedded USLT/SYLT extractor, real-time index synchronization, seek-by-lyric, smooth auto-scrolling, LyricsScreen Flat Monochrome). (DONE)
   - **Phase 4 Step 10**: NDK C++ Integration & Native Audio Signal Processing Bridge (CMake, libvox-dsp.so, AMediaExtractor/AMediaCodec PCM decoding, JNI bridge, Threading & Memory safety). (DONE)
   - **Phase 4 Step 11**: BPM / Tempo & Musical Key Detection Signal Processing Engine (C++ Autocorrelation Onset Detection, Chromagram Harmonic Profiles, AudioAnalysisRepository caching, UI badges & inspector integration). (DONE)
   - **Phase 4 Step 12**: Chord Progression Analyzer Engine (HPCP Windowed Chromagram, Real-time Chord Tracker, ChordVisualizer Flat Monochrome, Room DB Cache). (DONE)
   - **Phase 4 Step 13**: Final UI Polish, Memory & Battery Benchmark, Bug Fixing, and Build Production APK (`Vox_stable_v1.0.0.apk`). (DONE)
   - **Extension Step 14**: Lucide Icons for Compose integration (`com.composables:icons-lucide-android:2.2.1` / `com.composables:icons-lucide:2.2.1`), full icon set refactor (Player, Scrubber, Looper, Library, Equalizer, Clipper, Tag Editor, Lyrics, Analysis, Actions) with pure monochrome styling. (DONE)
   - **Instruction / Remote Sync**: Remote Git repository `git@github.com:imanecdoche/voxplayer.git` configured with root APK (`Vox_stable_v1.0.0.apk`) tracking and staging. (DONE)
