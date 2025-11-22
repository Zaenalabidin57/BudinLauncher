# BudinLauncher - Android Launcher Project

## Project Overview

BudinLauncher is a custom Android launcher application built with Kotlin and AndroidX. This is a minimal, lightweight launcher designed to replace the default Android home screen with a simple, clean interface focused on app launching functionality.

### Key Technologies
- **Language**: Kotlin
- **Platform**: Android (API 23-34)
- **Build System**: Gradle with Android Gradle Plugin 8.2.0
- **UI Framework**: Android Views (Material Design components)
- **Architecture**: Single-activity architecture with helper classes

### Main Features
- Custom home screen replacement
- App listing and launching
- Search functionality for apps
- Screen time tracking capabilities
- Settings management
- Swipe gesture support
- Alarm integration

## Project Structure

```
budin-launcher/
├── app/
│   ├── src/main/
│   │   ├── java/app/budinlauncher/
│   │   │   ├── MainActivity.kt          # Main launcher activity
│   │   │   ├── SettingsActivity.kt      # Settings screen
│   │   │   ├── FakeHomeActivity.kt      # Backup home activity
│   │   │   ├── AppMenuHelper.kt         # App menu utilities
│   │   │   ├── ScreenTimeHelper.kt      # Screen time tracking
│   │   │   ├── OnSwipeTouchListener.kt  # Gesture handling
│   │   │   └── Prefs.kt                 # SharedPreferences management
│   │   ├── res/                         # Android resources
│   │   └── AndroidManifest.xml          # App permissions and configuration
│   ├── build.gradle                     # App-level build configuration
│   └── proguard-rules.pro              # Code obfuscation rules
├── build.gradle                         # Project-level build configuration
├── settings.gradle                      # Gradle settings
├── gradle.properties                    # Gradle properties
└── gradlew                              # Gradle wrapper script
```

## Building and Running

### Prerequisites
- Android Studio or Android SDK command-line tools
- Java 8 or higher
- Gradle 6.5+ (included via wrapper)

### Build Commands

```bash
# Clean the project
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug version to connected device
./gradlew installDebug

# Run tests
./gradlew test

# Run lint checks
./gradlew lint
```

### Installation
1. Connect an Android device or start an emulator
2. Run `./gradlew installDebug` to install the debug version
3. Set BudinLauncher as the default home app when prompted
4. Alternatively, manually set it as the default launcher in Settings → Apps → Home

## Development Conventions

### Code Style
- Follows official Kotlin coding style (`kotlin.code.style=official`)
- Uses AndroidX libraries instead of legacy support libraries
- Implements Material Design principles
- Java 8 compatibility for both source and target

### Architecture Patterns
- **Single Activity Pattern**: Main launcher functionality in `MainActivity`
- **Helper Classes**: Separation of concerns with dedicated helper classes
- **SharedPreferences**: Simple key-value storage for settings via `Prefs.kt`
- **Gesture Handling**: Custom swipe touch listener for navigation

### Key Components

#### MainActivity
- Core launcher interface
- App listing and search functionality
- Handles app launching and user interactions

#### SettingsActivity  
- Configuration options for the launcher
- User preferences management

#### Helper Classes
- `AppMenuHelper`: App menu creation and management
- `ScreenTimeHelper`: Usage statistics tracking
- `OnSwipeTouchListener`: Gesture recognition
- `Prefs`: Centralized settings storage

### Permissions Required
- `EXPAND_STATUS_BAR`: Status bar expansion
- `QUERY_ALL_PACKAGES`: App enumeration
- `SET_ALARM`: Alarm app integration
- `PACKAGE_USAGE_STATS`: Screen time functionality

### Build Variants
- **Debug**: Debuggable, with `.debug` suffix, no minification
- **Release**: Minified with ProGuard, optimized for distribution

## Testing

### Unit Tests
Run unit tests with:
```bash
./gradlew test
```

### UI Testing
The project uses AndroidJUnit for instrumentation tests. Run with:
```bash
./gradlew connectedAndroidTest
```

### Lint
Android lint checks are configured with a baseline file:
```bash
./gradlew lint
```

## Configuration Notes

### Gradle Properties
- JVM heap size: 2048MB
- AndroidX migration enabled
- Jetifier enabled for legacy library support
- Non-transitive R-class generation enabled

### Build Configuration
- Target SDK: 34 (Android 14)
- Minimum SDK: 23 (Android 6.0)
- Application ID: `app.budinlauncher`
- Version: 1.0 (versionCode 1)

## Development Workflow

1. Make changes to source files in `app/src/main/java/app/budinlauncher/`
2. Update resources in `app/src/main/res/` as needed
3. Run `./gradlew assembleDebug` to build
4. Install and test on device/emulator
5. Run lint checks before committing: `./gradlew lint`
6. Run tests: `./gradlew test`

## Common Tasks

### Adding New Features
1. Implement functionality in appropriate Kotlin files
2. Add required permissions to `AndroidManifest.xml`
3. Update resources if UI changes are needed
4. Test on multiple Android versions if applicable

### Updating Dependencies
Modify dependencies in `app/build.gradle`:
```kotlin
dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
```

### Release Preparation
1. Update versionCode and versionName in `app/build.gradle`
2. Review ProGuard rules in `proguard-rules.pro`
3. Run `./gradlew assembleRelease`
4. Test the release APK thoroughly
5. Sign the APK for distribution