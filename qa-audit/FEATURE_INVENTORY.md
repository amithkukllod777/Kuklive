# Feature Inventory — Kuklive

Verified against source at commit `93861b6`. Roles: the app has **one role** (anonymous local user); no auth, no admin.

| Module | Feature | Status | Test | Docs | Risk |
|---|---|---|---|---|---|
| Onboarding | First-run Setup screen (country + language multi-select) | ✅ Implemented (`SetupScreen.kt`, gated in `MainActivity`) | ❌ none | README/NEXT_VERSION | Med |
| Data | M3U/M3U8 parsing (name clean, categories, country, language) | ✅ (`M3UParser.kt`) — validated by hand only | ❌ | inline | Med |
| Data | Multi-country / multi-language load | ✅ (`PlaylistRepository.kt`) | ❌ | inline | Med |
| Data | Curated bundled India list (5 verified streams) | ✅ (`CuratedPlaylists.kt`) | ❌ | inline | Med |
| Data | Free-TV merge + YouTube exclusion + dedupe | ✅ | ❌ | inline | Med |
| Data | Custom playlist URL override | ✅ (`SettingsStore`, repo, `SettingsScreen`) | ❌ | in-app hint | Med |
| Data | Language enrichment via URL intersection | ✅ (with non-empty guard) | ❌ | inline | Med |
| Browse | Channel grid (adaptive columns, logos via Coil) | ✅ (`ChannelsScreen.kt`) | ❌ | — | Low |
| Browse | Category chips (normalised genres, emoji, ordered) | ✅ (`CategoryTaxonomy.kt`) | ❌ | — | Low |
| Browse | Search by name | ✅ | ❌ | — | Low |
| Browse | Favorites (star, persisted) + Favorites tab | ✅ (DataStore string-set) | ❌ | — | Low |
| Browse | Region bar (shows selection, opens Settings) | ✅ | ❌ | — | Low |
| Player | HLS playback (Media3, custom UA, cross-protocol redirect, HLS mime) | ✅ (`PlayerScreen.kt`) | ❌ | — | Med |
| Player | Channel zapping (prev/next within filtered list) | ✅ | ❌ | — | Low |
| Player | Buffering spinner + 25s "not responding" timeout | ✅ | ❌ | — | Low |
| Player | Keep screen awake while playing (FLAG_KEEP_SCREEN_ON) | ✅ | ❌ | — | Low |
| Player | Picture-in-Picture | ⚠️ Declared in manifest, **not implemented** in code | ❌ | — | Low |
| Settings | Change country/language/custom URL + reload | ✅ | ❌ | — | Low |
| Platform | Android phone + Android TV (Leanback) | ✅ declared; TV UX not verified on device | ❌ | README | Med |
| Platform | LG webOS client | ⚠️ Implemented but **behind Android** (github.io, full index, no curated/FreeTV/multi-select) | ❌ | webOS/README | Major |
| Build/Release | CI debug APK + webOS IPK, published to releases | ✅ (GitHub Actions) | n/a | README | Low |
| Build/Release | Signed release build / AAB | ❌ Missing | n/a | — | Major |

## Explicitly NOT APPLICABLE (no such subsystem)
Signup, login/logout, OTP/password reset, profile, roles/permissions, payments/subscriptions/invoices, push/email/SMS notifications, reports/exports, admin panel, account/data deletion flows, server database, backups/DR, analytics/monitoring. These are marked NOT APPLICABLE throughout the audit — the app is a stateless on-device player.

## Undocumented / partial
- **PiP**: `supportsPictureInPicture="true"` declared but no `enterPictureInPictureMode` call → dead declaration.
- **EPG (`x-tvg-url`)**: parsed sources contain EPG URLs; app ignores them (no program guide).
- **Radio / Creator TV / Kuk Originals / OTT Apps**: listed in `NEXT_VERSION.md`, not built.
