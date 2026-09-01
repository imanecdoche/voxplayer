# Product Requirements Document (PRD)
## Proyek: Minimalist Offline Music Player & Audio Utility (Android Native)

**Dokumen Versi:** 1.0.0  
**Status:** Approved / Ready for Development  
**Platform Target:** Android 10 (API Level 29) – Android 15+ (API Level 35)  
**Bahasa & Framework:** Kotlin, Jetpack Compose, Jetpack Media3  
**Format Dokumen:** Markdown (`PRD.md`)

---

## 1. Executive Summary & Visi Produk

Aplikasi ini adalah pemutar musik offline (*local audio player*) dan utilitas audio canggih berbasis Android Native (Kotlin) yang menggabungkan performa playback profesional, pemrosesan sinyal digital (*Digital Signal Processing* / DSP), pemindaian berkas berbasis folder, analisis audio otomatis (BPM, Key, Chord), serta editor metadata mendalam.

Secara visual, produk ini mengusung filosofi desain **Ultra-Minimalist Monochrome**:
* **Zero Container / Zero Card Elevation**: Tidak menggunakan card elevated, bayangan (drop shadow), maupun container dekoratif berlapis.
* **Pure Monochrome**: Hanya menggunakan spektrum Hitam Murni (`#000000`), Putih Murni (`#FFFFFF`), dan Aksen Abu-abu Netral (`#757575`).
* **Typography-Driven & Whitespace Layout**: Pemisahan konten dan hierarki navigasi sepenuhnya diatur melalui bobot font (*FontWeight*), spasi terukur (*Spacer*), dan garis pembatas ultra-tipis (*Hairline Dividers*).

---

## 2. Arsitektur Teknis & Tech Stack

| Komponen | Pilihan Teknologi | Rasional & Peran |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.0+ (Android Native) | Performa native, memory safety, interop C++ via JNI. |
| **UI Framework** | Jetpack Compose + Material 3 | Desain flat deklaratif tanpa container berat, dynamic scaling. |
| **Audio Playback Engine** | Jetpack Media3 (`ExoPlayer`) | Standard industri playback audio Android, gapless playback. |
| **Background Service** | `MediaSessionService` (Media3) | Background playback anti-kill, MediaSession, Lockscreen/Notif. |
| **DSP, Pitch & Speed** | `SonicAudioProcessor` (Media3) / `Sonic` | Real-time independent pitch shifting & tempo scaling. |
| **Equalizer Engine** | Android `AudioEffect` (DynamicsProcessing / Equalizer) | Multi-band parametric/graphic EQ & Bass Boost. |
| **Audio Analysis (BPM, Key, Chord)** | Native C++ via NDK (`Aubio` / `SoundTouch` / `FFTW`) | Spectral analysis, beat tracking, chroma feature extraction. |
| **Audio Trimming / Clipping** | `FFmpeg-Kit Android` / `MediaExtractor` + `MediaMuxer` | Fast lossless audio trimming (stream copy) tanpa re-encoding. |
| **Metadata Tagging** | `Jaudiotagger` + MediaStore API / SAF | Parsing & penulisan ID3v1, ID3v2.3/2.4, Vorbis, MP4/AAC tags. |
| **Local Database & Cache** | Android Jetpack Room DB | Penyimpanan playlist, tag kustom, cache BPM/Key/Chord, index folder. |
| **Storage Access** | MediaStore API + Storage Access Framework (SAF) | Kompatibel penuh dengan Scoped Storage Android 10 - 15+. |

---

## 3. Desain Sistem & UI/UX Guidelines (Monokrom Flat)

### 3.1. Palet Warna (Strict Monochrome)
* **Background Utama:** `#000000` (Amoled Black) atau `#FFFFFF` (Light Mode)
* **Teks Primer / Aksen Aktif:** `#FFFFFF` (Dark) / `#000000` (Light)
* **Teks Sekunder / Inaktif:** `#757575` (Neutral Gray)
* **Divider / Border:** `#222222` (Dark) / `#E0E0E0` (Light) dengan ketebalan `0.5.dp` - `1.dp`
* **Handle Slider & Playhead:** Circle putih pekat diameter `8.dp` tanpa glow/shadow.

### 3.2. Layout Rules
* Dilarang menggunakan `Card`, `ElevatedCard`, atau `Surface(shadowElevation > 0)`.
* Navigasi berpindah menggunakan `TabRow` minimalis bergaris bawah tipis atau header teks datar.
* Indikator status (Play, Pause, Loop, Shuffle) menggunakan ikon outline geometris monokrom.
* Cover art album ditampilkan dalam rasio 1:1 tajam (*sharp corners*, no corner radius) atau border persegi tipis 1px.

---

## 4. Rincian Spesifikasi Fitur

### 4.1. Fitur 1: Storage Scanner & Directory Explorer
* **Deskripsi:** Memindai seluruh penyimpanan internal dan microSD untuk mengekstrak berkas audio yang valid.
* **Format Audio yang Didukung:** `.mp3`, `.wav`, `.flac`, `.m4a`, `.aac`, `.ogg`, `.opus`.
* **Mekanisme Folder-Based Display:**
  * Mengelompokkan berkas berdasarkan direktori fisik tempat audio disimpan (misal: `/Music/Rock`, `/Download/TelegramAudio`).
  * Menampilkan jumlah folder yang memiliki minimal 1 berkas audio valid.
  * Menampilkan struktur folder root, nama folder, total durasi isi folder, dan jumlah berkas di dalamnya.
  * Real-time ContentObserver: Memperbarui daftar lagu otomatis saat ada file baru ditambahkan atau dihapus dari penyimpanan.

### 4.2. Fitur 2: Basic Playback Controller
* **Core Playback Functions:**
  * `Play`, `Pause`, `Stop`.
  * `Next` / `Previous` (dengan batas 3 detik untuk restart lagu atau pindah track).
  * `Fast Forward` & `Rewind` (step interval: 5s, 10s, 30s yang dapat dikonfigurasi).
  * `Seekbar / Scrubber` linear ultra-tipis dengan tampilan waktu *elapsed* vs *remaining/total*.
* **Queue & Playback Modes:**
  * Manajemen antrean lagu (*Up Next*), *drag-and-drop reordering*, dan *swipe-to-remove*.
  * **Shuffle Mode:** True random dengan history buffer untuk mencegah pemutaran ulang lagu yang sama sebelum antrean habis.
  * **Loop Modes (3 Mode Toggle):**
    1. `Loop All` (mengulang seluruh antrean).
    2. `Loop Current` / Single Repeat (mengulang lagu yang sedang aktif terus-menerus).
    3. `No Loop` / Stop at Queue End.
* **Volume Integration:** Slider volume internal independen serta sinkronisasi dengan hardware audio stream Android (`STREAM_MUSIC`).

### 4.3. Fitur 3: Advanced Playback & DSP Control
* **Playback Speed:** Pengaturan kecepatan `0.25x` hingga `3.00x` (step `0.05x`) tanpa distorsi nada (time stretching).
* **Pitch Shifter:** Mengubah nada audio dari `-12 semitone` hingga `+12 semitone` (step 1 semitone / 10 cents) terpisah dari kecepatan pemutaran.
* **Equalizer (EQ):**
  * 5-band / 10-band Graphic Equalizer (tergantung kapabilitas hardware DSP perangkat).
  * Presets: *Flat, Bass Boost, Treble Boost, Vocal, Acoustic, Rock, Electronic, Custom*.
  * Virtualizer & Loudness Enhancer slider.
* **A-B Loop (Precision Looper):**
  * Menandai Point A (Start) dan Point B (End) pada milidetik presisi.
  * Pemutar otomatis melompat kembali ke Point A saat mencapai Point B.
  * Fitur Clear A-B Loop untuk kembali ke mode normal.

### 4.4. Fitur 4: File Management & Library Organization
* **Kategori Organisasi:**
  * *Favorites (Sistem Bintang/Heart Monokrom)*.
  * *Custom Playlists* (Buat, ubah nama, urutkan, ekspor/impor `.m3u` / `.m3u8`).
  * *Tags Kustom* (label kustom lokal yang disimpan di Room DB, misal: `#Study`, `#Workout`, `#AcousticVibe`).
* **Operasi File Fisik (Scoped Storage Compliant):**
  * **Rename:** Mengubah nama berkas fisik dan menyinkronkan kembali dengan MediaStore.
  * **Delete:** Menghapus berkas fisik permanen dari penyimpanan lokal (menggunakan `MediaStore.createDeleteRequest` di Android 11+).
  * **Move / Copy:** Memindahkan file antar direktori melalui SAF / DocumentFile.

### 4.5. Fitur 5: Metadata Listener / Audio Inspector
* Ekstraksi dan penayangan metadata lengkap dari header berkas audio:
  * Nama Berkas (*Filename*) & Ekstensi.
  * Title, Artist, Contributing Artists / Composers, Album, Album Artist.
  * Durasi, Bitrate (kbps), Sample Rate (Hz), Channels (Stereo/Mono), Format Codec.
  * Embedded Cover Art (mengekstrak gambar album beresolusi penuh).
  * Lirik Lagu (*Synchronized LRC* dan *Unsynced Plain Text*).
  * Genre & Release Year / Date.

### 4.6. Fitur 6: Background Playback & Battery Optimization Lifecycle
* **Background Service (`MediaSessionService`):**
  * Berjalan secara persisten di latar belakang sebagai *Foreground Service* dengan `NotificationCompat.MediaStyle`.
  * Kontrol notifikasi: Play, Pause, Next, Prev, Favorite, Seekbar, dan Cover Art.
  * Integrasi penuh dengan Android MediaSession (kompatibel dengan Bluetooth Headset, Android Auto, Lockscreen, Smartwatch).
  * Audio Focus Handling: Otomatis pause saat telepon masuk, ducking saat ada navigasi/notifikasi, resume saat panggilan berakhir.
* **Battery Optimization Management:**
  * Dialog eksplisit untuk meminta izin pengecualian optimasi baterai (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) agar service tidak dimatikan oleh OEM custom OS (MIUI/HyperOS, ColorOS, OneUI).

### 4.7. Fitur 7: Audio Signal Analysis (BPM, Key, Chord Detection)
* **BPM / Tempo Detector:**
  * Menghitung nilai tempo lagu (*Beats Per Minute*) secara otomatis menggunakan algoritma onset detection / peak energy filtering via C++ (Aubio/SoundTouch).
  * Menampilkan angka BPM pada detail lagu dan player view.
* **Musical Key & Scale Detector:**
  * Ekstraksi pitch chroma profile untuk mendeteksi tangga nada dasar (misal: `C Major`, `A Minor`, `F# Minor`).
* **Chord Progression Detector:**
  * Analisis real-time atau pre-computed harmonic pitch class profiles (HPCP) untuk memprediksi progresi akor yang sedang dimainkan pada rentang waktu tertentu.
* **Hasil Analisis:** Disimpan ke Room DB agar proses kalkulasi FFT/DSP berat hanya berjalan satu kali per berkas.

### 4.8. Fitur 8: Audio Clipper & Trimmer
* **Visual Trimming UI:**
  * Waveform viewer monokrom dengan dual draggable handles (Marker Start & Marker End).
  * Preview audio khusus pada rentang waktu yang di-klip.
  * Input waktu manual dalam format `MM:SS.mmm`.
* **Exporting / Saving:**
  * Opsi 1: Lossless Stream Copy (ekspor instan tanpa re-encoding menggunakan `MediaMuxer` / `FFmpeg`).
  * Opsi 2: Encode kustom (.mp3, .wav, .m4a) dengan pilihan bitrate (128k, 192k, 320k).
  * Menyimpan salinan baru ke folder tujuan (`/Music/Clipped/`) tanpa merusak file asli.

### 4.9. Fitur 9: Metadata & ID3 Tag Editor
* **Field yang Dapat Diedit:**
  * Title, Artist, Album, Genre, Year, Track Number, Disc Number, Composer, Comments.
  * Lirik lagu (Input lirik manual atau load file `.lrc` eksternal).
  * Cover Art: Mengganti gambar album dengan foto dari galeri atau menghapus cover art bawaan.
* **Penyimpanan:**
  * Menulis langsung tag ID3v2.4 / Vorbis Comment / MP4 Atom ke dalam biner berkas audio melalui library native.
  * Memperbarui index Android `MediaStore` pasca-penulisan agar perubahan langsung terbaca di seluruh sistem.

---

## 5. Skema Basis Data Lokal (Room Database)

### 5.1. Entity: `AudioTrackEntity`
* `id`: Long (Primary Key, MediaStore ID)
* `filePath`: String (Absolute Path)
* `folderPath`: String (Directory Path)
* `fileName`: String
* `title`: String
* `artist`: String
* `album`: String
* `durationMs`: Long
* `mimeType`: String
* `bitrate`: Int
* `sampleRate`: Int
* `genre`: String?
* `year`: Int?
* `hasEmbeddedLyrics`: Boolean
* `bpm`: Float? (Cached Analysis)
* `musicalKey`: String? (Cached Analysis)
* `isFavorite`: Boolean (Default: false)
* `customTags`: String (JSON Array / Comma Separated)
* `dateAdded`: Long
* `dateModified`: Long

### 5.2. Entity: `PlaylistEntity` & `PlaylistTrackCrossRef`
* `playlistId`: Long (PK, Auto-Generate)
* `playlistName`: String
* `createdAt`: Long
* `trackOrder`: Int (di cross-ref table)

### 5.3. Entity: `AudioAnalysisCacheEntity`
* `trackId`: Long (PK, Foreign Key to AudioTrackEntity)
* `chordDataJson`: String (Timestamped Chords: `[{"time": 0.0, "chord": "C"}, ...]`)
* `waveformSamples`: ByteArray (Pre-calculated peak samples for quick waveform rendering)

---

## 6. Persyaratan Non-Fungsional (NFR)

* **Latency Audio:** Seek latency < 50ms untuk file lokal; DSP effect switching latency < 20ms.
* **Performa CPU & Baterai:** Analisis BPM/Chord dijalankan di Background Dispatcher (`Dispatchers.Default` / WorkManager) agar UI thread tetap stabil di 60/120 FPS.
* **Memory Management:** Thumbnail cover art dan waveform di-cache menggunakan *LRU Cache* dan *Coil Compose* untuk mencegah `OutOfMemoryError` (OOM) saat membuka folder dengan ribuan lagu.
* **Kompatibilitas Android:** Mendukung Android 10 (Q) hingga Android 15 (Vanilla Ice Cream) dengan kepatuhan penuh terhadap MediaStore Scoped Storage dan runtime permissions (`READ_MEDIA_AUDIO`, `POST_NOTIFICATIONS`).

---

## 7. Roadmap Implementasi

```
Phase 1: Pondasi & Basic Engine
├── Inisialisasi Project (Jetpack Compose + Monokrom Theme Setup)
├── MediaStore Scanner & Room Database Sync (Folder-based Grouping)
└── Jetpack Media3 (ExoPlayer + MediaSessionService Background Playback)

Phase 2: Advanced DSP & File Operations
├── SonicAudioProcessor Integration (Speed & Pitch Shifting)
├── Equalizer & A-B Looping Controller
└── File Operations (Favorite, Playlist, Tags, Scoped Storage SAF Rename/Delete)

Phase 3: Metadata & Utility
├── Jaudiotagger Integration (Metadata Inspector & Tag Editor)
├── Audio Trimming Engine (FFmpeg-Kit / MediaMuxer Lossless Clipper)
└── Lyrics Viewer (Synced LRC parser)

Phase 4: DSP Analysis (BPM, Key, Chord) & Polish
├── NDK C++ Aubio/SoundTouch Integration via JNI
├── BPM & Musical Key Detection Engine
├── Chord Progression Analyzer
└── UI Clean-up, Monokrom Polish, & Performance Benchmark
```
