# BudinLauncher - Aplikasi Launcher Android

## Ringkasan Proyek

BudinLauncher adalah aplikasi launcher kustom untuk Android yang dibangun dengan Kotlin dan AndroidX. Ini adalah launcher yang minimalis dan ringan yang dirancang untuk menggantikan layar beranda default Android dengan antarmuka yang sederhana dan bersih yang berfokus pada fungsionalitas peluncuran aplikasi.

## Fitur Utama

- 🏠 **Layar Beranda Kustom** - Menggantikan launcher default Android
- 📱 **Daftar Aplikasi** - Menampilkan dan meluncurkan semua aplikasi terinstal
- 🔍 **Pencarian Aplikasi** - Cari aplikasi dengan cepat
- ⏱️ **Pelacakan Waktu Layar** - Monitor penggunaan aplikasi
- ⚙️ **Pengaturan** - Kelola preferensi launcher
- 👆 **Gesture Swipe** - Navigasi dengan gerakan swipe
- ⏰ **Integrasi Alarm** - Akses cepat ke aplikasi alarm

## Teknologi

- **Bahasa**: Kotlin
- **Platform**: Android (API 23-34)
- **Sistem Build**: Gradle dengan Android Gradle Plugin 8.2.0
- **UI Framework**: Android Views (Material Design)
- **Arsitektur**: Single-activity architecture dengan kelas helper

## Persyaratan Sistem

- **Android**: Minimum API 23 (Android 6.0) hingga API 34 (Android 14)
- **RAM**: Minimal 2GB direkomendasikan
- **Penyimpanan**: 50MB ruang kosong

## Instalasi

### Metode 1: Instalasi Manual

1. Unduh file APK dari [Releases](https://github.com/username/budinlauncher/releases)
2. Aktifkan "Sumber tidak dikenal" di Pengaturan → Keamanan
3. Instal file APK yang diunduh
4. Saat diminta, pilih BudinLauncher sebagai launcher default

### Metode 2: Build dari Source

```bash
# Clone repository
git clone https://github.com/username/budinlauncher.git
cd budinlauncher

# Build APK debug
./gradlew assembleDebug

# Install ke perangkat
./gradlew installDebug
```

## Penggunaan

### Menjadikan Launcher Default

1. Buka **Pengaturan** → **Aplikasi** → **Aplikasi Beranda**
2. Pilih **BudinLauncher** sebagai launcher default
3. Atau, setelah instalasi pertama, pilih BudinLauncher saat diminta

### Navigasi Dasar

- **Swipe ke Bawah**: Buka pencarian aplikasi
- **Swipe ke Atas**: Akses pengaturan
- **Tap pada Aplikasi**: Luncurkan aplikasi
- **Tap dan Tahan**: Menu opsi tambahan

### Pencarian Aplikasi

1. Swipe ke bawah untuk membuka pencarian
2. Ketik nama aplikasi yang dicari
3. Tap pada hasil untuk meluncurkan

## Struktur Proyek

```
budin-launcher/
├── app/
│   ├── src/main/
│   │   ├── java/app/budinlauncher/
│   │   │   ├── MainActivity.kt          # Aktivitas launcher utama
│   │   │   ├── SettingsActivity.kt      # Layar pengaturan
│   │   │   ├── FakeHomeActivity.kt      # Aktivitas beranda cadangan
│   │   │   ├── AppMenuHelper.kt         # Utilitas menu aplikasi
│   │   │   ├── ScreenTimeHelper.kt      # Pelacakan waktu layar
│   │   │   ├── OnSwipeTouchListener.kt  # Penanganan gesture
│   │   │   ├── Prefs.kt                 # Manajemen SharedPreferences
│   │   │   └── Constants.kt             # Konstanta aplikasi
│   │   ├── res/                         # Sumber daya Android
│   │   └── AndroidManifest.xml          # Izin dan konfigurasi aplikasi
│   ├── build.gradle                     # Konfigurasi build level aplikasi
│   └── proguard-rules.pro              # Aturan obfuscasi kode
├── build.gradle                         # Konfigurasi build level proyek
├── settings.gradle                      # Pengaturan Gradle
├── gradle.properties                    # Properti Gradle
└── README.md                            # Dokumentasi proyek
```

## Build dan Development

### Prasyarat

- Android Studio atau Android SDK command-line tools
- Java 8 atau lebih tinggi
- Gradle 6.5+ (tersedia via wrapper)

### Perintah Build

```bash
# Membersihkan proyek
./gradlew clean

# Build APK debug
./gradlew assembleDebug

# Build APK release
./gradlew assembleRelease

# Install versi debug ke perangkat terhubung
./gradlew installDebug

# Jalankan tes
./gradlew test

# Jalankan pemeriksaan lint
./gradlew lint
```

### Development

1. Buka proyek di Android Studio
2. Hubungkan perangkat Android atau mulai emulator
3. Jalankan `./gradlew assembleDebug` untuk build
4. Install dan test pada perangkat/emulator
5. Jalankan pemeriksaan lint sebelum commit: `./gradlew lint`

## Konfigurasi dan Pengaturan

### Preferensi Pengguna

- **Tema**: Gelap/Terang (otomatis mengikuti sistem)
- **Ukuran Ikon**: Kecil/Sedang/Besar
- **Tampilan Grid**: Jumlah kolom aplikasi
- **Animasi**: Aktif/nonaktifkan animasi transisi

### Pengaturan Lanjutan

- **Screen Time**: Aktifkan pelacakan penggunaan aplikasi
- **Gesture**: Kustomisasi gerakan swipe
- **Hidden Apps**: Sembunyikan aplikasi tertentu dari daftar

## Izin yang Diperlukan

Aplikasi memerlukan izin berikut:

- `EXPAND_STATUS_BAR`: Mengembangkan status bar
- `QUERY_ALL_PACKAGES`: Enumerasi aplikasi terinstal
- `SET_ALARM`: Integrasi aplikasi alarm
- `PACKAGE_USAGE_STATS`: Fungsionalitas waktu layar

## Troubleshooting

### Launcher Tidak Muncul di Opsi

1. Pastikan instalasi berhasil
2. Restart perangkat
3. Buka **Pengaturan** → **Aplikasi** → **Aplikasi Beranda**
4. Pilih BudinLauncher dari daftar

### Aplikasi Tidak Muncul

1. Buka **Pengaturan** → **Aplikasi** → **BudinLauncher**
2. Berikan izin yang diperlukan
3. Restart launcher

### Performa Lambat

1. Hapus cache aplikasi
2. Nonaktifkan animasi jika tidak diperlukan
3. Kurangi jumlah aplikasi yang ditampilkan

## Kontribusi

Kami menyambut kontribusi dari komunitas! Untuk berkontribusi:

1. Fork repository ini
2. Buat branch fitur baru (`git checkout -b fitur-baru`)
3. Commit perubahan Anda (`git commit -am 'Tambah fitur baru'`)
4. Push ke branch (`git push origin fitur-baru`)
5. Buat Pull Request

### Panduan Kontribusi

- Ikuti gaya kode Kotlin official
- Tambahkan tes untuk fitur baru
- Pastikan semua tes lulus sebelum submit
- Dokumentasikan perubahan signifikan

## Lisensi

Proyek ini dilisensikan under [MIT License](LICENSE).

## Versi

- **Versi Saat Ini**: 1.0
- **Version Code**: 1

## Dukungan

Jika Anda mengalami masalah atau memiliki pertanyaan:

- 🐛 **Bug Report**: [Issues](https://github.com/username/budinlauncher/issues)
- 💡 **Fitur Request**: [Discussions](https://github.com/username/budinlauncher/discussions)
- 📧 **Kontak**: budinlauncher@example.com

## Changelog

### v1.0.0
- Rilis awal BudinLauncher
- Fitur launcher dasar
- Pencarian aplikasi
- Pelacakan waktu layar
- Pengaturan kustomisasi

## Kredit

- **Material Design**: Google Material Design Team
- **AndroidX**: Android Jetpack Team
- **Kotlin**: JetBrains Team

---

**Terima kasih telah menggunakan BudinLauncher!** 🚀