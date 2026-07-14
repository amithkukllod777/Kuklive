# Bug Report — Kuklive

Confirmed defects (evidence-backed) and suspected risks are separated. Severity: Blocker / Critical / Major / Minor / Cosmetic.

---

## CONFIRMED

### KUK-001 — webOS client is out of sync with Android
- **Module:** webOS (`webos/app.js`) · **Env:** LG webOS
- **Steps:** Open webOS app → it loads `DEFAULT_PLAYLIST = https://iptv-org.github.io/iptv/index.m3u`.
- **Expected:** Same reliable behaviour as Android (raw.githubusercontent host, country/language, curated, Free-TV, YouTube exclusion, timeout).
- **Actual:** Uses `iptv-org.github.io` (the host that was unreachable for the user on Android), loads the full ~10k index, no curated/Free-TV/country-language/host fixes.
- **Severity:** Major · **Priority:** High · **Evidence:** `webos/app.js:5,142,166`.
- **Root cause:** Android improvements (last several commits) were not ported to webOS.
- **Fix:** Port the source strategy to `app.js` (raw host, per-country/language, curated, YouTube filter). **Regression risk:** Med (single-file, no shared code).

### KUK-002 — Vanishing channels via github.io host (FIXED, verify)
- **Module:** `PlaylistRepository` · **Steps:** India+Hindi on a network where `iptv-org.github.io` is blocked → only 9 bundled channels showed.
- **Expected:** ~300+ channels. **Actual (pre-fix):** 9. **Severity:** Major (was) · Now mitigated by switch to `raw.githubusercontent.com/.../gh-pages`. **Evidence:** user screenshot; curl confirmed raw host serves 765 IN / 350 HIN. **Regression risk:** Low. **Status:** Needs user re-confirmation on-device.

### KUK-003 — Curated list shipped guessed/dead URLs
- **Module:** `CuratedPlaylists` · **Steps:** open ABP Asmita → `ERROR_CODE_IO_NETWORK_CONNECTION_FAILED`.
- **Cause:** URLs for ABP Majha/Asmita, NDTV 24x7, Good News Today were guessed by analogy, not verified. **Severity:** Major (was) → trimmed to 5 verified streams. **Evidence:** user screenshot. **Status:** FIXED (pending confirmation).

### KUK-004 — Broken logo images render provider error graphic
- **Module:** `ChannelsScreen` (Coil) · **Steps:** any channel whose `tvg-logo` 404s (e.g. dead imgur) → tile shows the host's "image does not exist" picture instead of a placeholder.
- **Expected:** fall back to the app's TV icon. **Actual:** Coil renders whatever bytes the URL returns (imgur returns a real error image → displayed).
- **Severity:** Cosmetic · **Evidence:** user screenshot. **Fix:** set Coil `error(placeholder)`/`fallback`; already removed bad bundled logos. **Regression risk:** Low.

### KUK-005 — PiP declared but not implemented
- **Module:** manifest vs `MainActivity` · **Evidence:** `android:supportsPictureInPicture="true"` present; no `enterPictureInPictureMode` in code. **Severity:** Minor. **Fix:** implement PiP on the player, or remove the flag.

### KUK-006 — No test suite / no crash reporting
- **Module:** project · **Evidence:** 0 test files; no Crashlytics/Sentry. Every regression this session was found only by manual user testing. **Severity:** Major (process) · **Fix:** see `MISSING_TESTS.md`.

---

## SUSPECTED (needs on-device verification)

### KUK-007 — Category/visibleChannels recomputed each access (perf)
- **Module:** `MainViewModel.UiState` · `categories`, `visibleChannels`, `countries`… are computed getters (flatMap/distinct/sorted/filter) recomputed on every Compose read over up to ~800 channels. **Impact:** possible scroll/typing jank on low-end devices. **Severity:** Minor (Major on 10k "all" set). **Fix:** `derivedStateOf` / precompute in VM. **Confidence:** Med (code-based).

### KUK-008 — No playlist cache; re-fetches every launch/reload
- Streams list is fetched fresh each cold start (2 large HTTP fetches for country+language). **Impact:** slow start on poor networks, wasted data. **Severity:** Minor. **Fix:** cache last playlist (DataStore/file) with TTL.

### KUK-009 — Player: no audio-focus / no transient-loss handling
- ExoPlayer built without `AudioAttributes`/`handleAudioBecomingNoisy`/audio focus. Calls/other media won't pause/duck; unplugging headphones won't pause. **Severity:** Minor. **Fix:** `setAudioAttributes(..., handleAudioFocus=true)`, `setHandleAudioBecomingNoisy(true)`.

### KUK-010 — Region bar misleads when custom URL active
- With a custom playlist set, the region bar still shows "India · Hindi" though those aren't used. **Severity:** Minor/Cosmetic. **Fix:** show "Custom playlist" when `customUrl` non-blank.

### KUK-011 — No auto-skip/retry on dead stream
- On timeout/error the user must manually press ▶. **Severity:** Minor (UX). **Fix:** optional auto-advance to next channel after failure.

### KUK-012 — "All countries + any language" loads Free-TV main only
- That path returns Free-TV's 1895 (Italy-heavy) set, not a true global list; may surprise users expecting "everything". **Severity:** Minor. **Fix:** document, or merge iptv-org index too (heavier).

### KUK-013 — webOS playlist/segment CORS (unverified)
- webOS `fetch()` of iptv-org + hls.js segment XHR may hit CORS on some TVs/streams. **Severity:** Major-if-real / NOT TESTED. **Fix:** verify on device; prefer native `<video src>` (already tries native-first) and document.

### KUK-014 — Cosmetic strings hardcoded ("Kuklive v1.0", "Kuklive")
- Version string and app title hardcoded; rename to "Kuk IPTV" pending (`NEXT_VERSION.md`). **Severity:** Cosmetic.
