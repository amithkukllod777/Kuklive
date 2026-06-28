# Kuklive — Next version notes

Pending changes to pick up in the next version (not yet implemented).

## Rename
- [ ] **Rename app to "Kuk IPTV"** (requested 2026-06-26).
  - Android: `app/src/main/res/values/strings.xml` → `app_name`; update any
    "Kuklive" UI strings (top bar title in `ChannelsScreen.kt`, headings in
    `SetupScreen.kt`, version line in `SettingsScreen.kt`).
  - webOS: `webos/appinfo.json` → `title`; `webos/index.html` header.
  - Keep the package id / applicationId (`com.kuklive.app`) the same to avoid
    breaking installs unless a full rebrand is intended.

## Future ideas discussed
- [ ] **Radio** — needs a separate radio playlist source (not in the iptv-org
  TV index).
- [ ] **Creator TV / Kuk Originals / OTT Apps** — own / licensed content; add a
  way to plug in custom M3U / streams (CMS or per-section playlists).
- [ ] Optional: per-section structure (Home / Live TV / Movies / Series / …) to
  match commercial IPTV layout once licensed/FAST channels are added.
