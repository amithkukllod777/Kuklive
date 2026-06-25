# Kuklive 📺

**Live TV, anywhere.** A modern, open Android IPTV player built with Kotlin,
Jetpack Compose, and Google's Media3 / ExoPlayer.

Kuklive loads any standard **M3U / M3U8 playlist** — the format used by every
IPTV provider — and lets you browse, search, favorite, and watch live channels.

## Features

- 📡 **Load any M3U playlist** — paste your provider's URL in Settings, or use
  the bundled community playlist out of the box.
- 🗂 **Categories & search** — channels are grouped by `group-title`, with a
  live search box and category filter chips.
- ⭐ **Favorites** — star channels for quick access; saved on-device with
  Jetpack DataStore.
- ▶️ **HLS live playback** — powered by Media3 ExoPlayer, with on-screen
  channel zapping (previous / next).
- 🖼 **Channel logos** — `tvg-logo` artwork loaded via Coil.
- 📱 **Phone & Android TV** — declares Leanback support so it appears on the TV
  launcher too.

## Tech stack

| Layer       | Choice                                         |
| ----------- | ---------------------------------------------- |
| Language    | Kotlin                                         |
| UI          | Jetpack Compose + Material 3                   |
| Playback    | AndroidX Media3 (ExoPlayer + HLS)              |
| Networking  | OkHttp                                         |
| Images      | Coil                                           |
| Persistence | DataStore Preferences                          |
| Min / Target SDK | 26 / 35                                   |

## Project structure

```
app/src/main/java/com/kuklive/app/
  KukliveApplication.kt        # service-locator singletons
  MainActivity.kt              # Compose nav host
  data/
    model/Channel.kt
    M3UParser.kt               # extended-M3U playlist parser
    PlaylistRepository.kt      # fetch + parse over OkHttp
    SettingsStore.kt           # playlist URL + favorites (DataStore)
  ui/
    MainViewModel.kt           # state, filtering, playback selection
    theme/Theme.kt
    screen/
      ChannelsScreen.kt        # grid, tabs, search, categories
      PlayerScreen.kt          # ExoPlayer + zapping
      SettingsScreen.kt        # playlist URL management
```

## Building

> **Requires the Android SDK** (via Android Studio or `sdkmanager`). This repo
> ships the Gradle wrapper, so no separate Gradle install is needed.

```bash
# 1. Point Gradle at your Android SDK (or open the project in Android Studio,
#    which writes this for you):
echo "sdk.dir=/path/to/Android/sdk" > local.properties

# 2. Build a debug APK:
./gradlew assembleDebug

# 3. Install on a connected device / emulator:
./gradlew installDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

### Open in Android Studio

`File → Open` and select this directory. Android Studio will sync Gradle,
download the dependencies, and let you Run ▶ on a device or emulator.

## Changing the playlist

The app ships with the [iptv-org](https://github.com/iptv-org/iptv)
community index of publicly available streams as a default. To use your own
provider, open **Settings** in the app and paste their M3U URL, then
**Save & Reload**.

## Legal note

Kuklive is a **player** only — it ships with no proprietary streams. You are
responsible for the playlists you load and for having the rights to view their
content.

## License

MIT
