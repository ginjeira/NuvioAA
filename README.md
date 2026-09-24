<div align="center">

  <img src="https://nuvio.tv/assets/nuvio-app-logo-wordmark.webp" alt="Nuvio" width="320" />

  <p>
    A free, open-source media app for your phone, your desktop, your TV, and Android Auto.
    <br />
    Bring your own sources. Nuvio turns them into a library with artwork, ratings, subtitles, and your place saved on every screen.
  </p>

  [Website](https://nuvio.tv) · [GitHub releases](https://github.com/NuvioMedia/NuvioMobile/releases/latest) · [Support Nuvio](https://nuvio.tv/support)

</div>

## Nuvio Auto (NuvioAA)

Nuvio includes full Android Auto support built with Jetpack Car App Library 1.7.0 and Media3.

### Features
- **Addon Catalog Browsing**: Browse categories, movies, series, and addons directly on the car display.
- **Continue Watching / Resume**: Automatically resumes video playback from saved position.
- **Subtitles Support**: Multi-language subtitle rendering in the car display.
- **Media Session Integration**: Full media bar controls (Play, Pause, Fast-Forward, Rewind) and Audio Focus management.

## Get Nuvio Mobile & Android Auto

- [Android on Google Play](https://play.google.com/store/apps/details?id=com.nuvio.app)
- [Android APK (`NuvioAA.apk`)](https://github.com/NuvioMedia/NuvioMobile/releases/latest)
- iOS via AltStore or SideStore: add [this source URL](https://raw.githubusercontent.com/NuvioMedia/NuvioMobile/cmp-rewrite/store.json) in the app's Sources section, then install Nuvio.

## Build from source

```bash
git clone https://github.com/NuvioMedia/NuvioMobile.git
cd NuvioMobile
```

### Android & Android Auto (NuvioAA)

Android development requires Android Studio and the Android SDK.

```bash
./gradlew :androidApp:assembleFullDebug
```

The compiled APK will be generated at `NuvioAA.apk` in the root project folder.

### iOS

iOS development requires macOS and Xcode.

```bash
env NUVIO_IOS_DISTRIBUTION=full xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -derivedDataPath build/ios-derived-full-simulator \
  CODE_SIGNING_ALLOWED=NO \
  build
```

The shared app is built with Kotlin Multiplatform and Compose Multiplatform.

## License

[GNU General Public License v3.0](./LICENSE)
