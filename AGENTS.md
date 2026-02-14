# AGENTS.md - timeto.me Development Guide

## Project Overview

timeto.me is a Kotlin Multiplatform Mobile (KMM) app with:
- **iOS/watchOS**: Swift/SwiftUI (`apple/` directory)
- **Android**: Kotlin (`android/` directory)
- **Shared**: Kotlin code in `shared/` module

The app is a goals tracker with 24/7 timers, checklists, and calendar features.

---

## Build Commands

### Gradle (Android & KMM)

```bash
# Set environment variables (add to ~/.zshrc for permanence)
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=~/android-sdk

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build debug iOS framework (requires macOS)
./gradlew :shared:compileKotlinIosArm64

# Run Android app
./gradlew :android:installDebug

# Clean build
./gradlew clean
```

### Xcode (iOS/watchOS)

Open `apple/timeto.me.xcodeproj` in Xcode and run on simulator or device.

---

## Code Style

### Kotlin (shared/, android/)

- **Style**: Official Kotlin code style (`kotlin.code.style=official` in gradle.properties)
- **Formatting**: Uses default IntelliJ/Android Studio formatting
- **Naming**:
  - Classes/Types: PascalCase (`AppVm`, `TaskDb`)
  - Functions/Properties: camelCase (`launchEx`, `state`)
  - Private functions: prefix with `_` if internal helper (e.g., `_buildUi()`)
- **Package**: `me.timeto.shared` for shared module
- **Coroutines**: Use `launchEx()` for fire-and-forget with error reporting
- **Error Handling**: Wrap async code with try/catch, use `reportApi()` to log errors

### Swift (apple/)

- **Style**: SwiftUI standard conventions
- **Imports**: 
  - `import SwiftUI` for UI framework
  - `import shared` to access Kotlin shared module
- **View Models**: Follow `Vm` pattern with StateFlow from shared Kotlin layer
- **Naming**: PascalCase for types, camelCase for properties/functions
- **UI Components**: Custom components in `apple/Common/UI/` (e.g., `fillMax`, `VStack`, `HStack`)

---

## Architecture

### Shared Module Patterns

```
vm/          # ViewModels (StateFlow-based)
db/          # Database layer (SQLDelight)
backups/     # Backup functionality
```

- ViewModels extend `Vm<T>` base class with `state: StateFlow<T>`
- Database uses SQLDelight with queries in `*.sq` files
- Use `ioScope()` for background work, `Dispatchers.Main` for UI

### iOS Patterns

- SwiftUI views observe Kotlin StateFlow via `@StateObject` wrapping
- Use `VmView` helper for view-VM binding
- Kotlin classes accessed via `shared` module import

---

## Important Files

- `shared/build.gradle.kts` - KMM configuration, dependencies
- `gradle.properties` - Kotlin code style, JVM args
- `apple/Common/` - SwiftUI utilities and extensions
- `shared/src/commonMain/kotlin/me/timeto/shared/db/` - Database layer

---

## Dependencies

### Kotlin/Shared
- Ktor Client 3.1.3
- SQLDelight 2.1.0
- Kotlinx DateTime 0.6.2
- Kotlinx Serialization 1.8.1

### iOS
- SwiftUI
- WidgetKit (for home screen widgets)
- UserNotifications

---

## Notes

- No test suite exists in this repository
- No CI/CD workflows configured
- Android minSdk: 26, compileSdk: 36
- Uses custom logging via `zlog()` and `reportApi()`
