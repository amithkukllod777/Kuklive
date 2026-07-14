# Security Audit — Kuklive

Scope: on-device Android/webOS player, no backend, no auth, no PII, no payments. Attack surface is small. Benchmarked against OWASP Mobile Top 10 (2024) where relevant. **No destructive testing performed.**

## Summary
No Critical/Blocker security issues found. No secrets in the repo. The main items are **defensive hardening** and **privacy/transport** posture appropriate to a content player.

| ID | Finding | Severity | Component | OWASP MASVS/MTop |
|---|---|---|---|---|
| SEC-01 | Cleartext traffic globally enabled | Low-Med | `AndroidManifest` `usesCleartextTraffic="true"` | M5 Insecure Comms |
| SEC-02 | Arbitrary URL fetch (custom playlist + playlist-controlled stream/logo URLs) | Low | `PlaylistRepository`, Coil, ExoPlayer | M4/M5 (SSRF-style on-device) |
| SEC-03 | `allowBackup=true`, no `dataExtractionRules` | Low | manifest | M9 Insecure Data Storage |
| SEC-04 | No dependency vulnerability scanning in CI | Low-Med | pipeline | M2 Supply Chain |
| SEC-05 | Release build unsigned / debug-signed; `assembleRelease` unverified | Med (integrity) | `build.gradle.kts` | M8 |
| SEC-06 | No transport integrity for playlists (no pinning) | Low | repo | M5 |
| SEC-07 | Third-party content execution (HLS) trust | Low | player | M4 |

## Details

### SEC-01 Cleartext traffic
- **Evidence:** `usesCleartextTraffic="true"`. **Why it exists:** many IPTV streams are plain `http://`. **Risk:** MITM can inject/replace a stream (impact low — public TV, no credentials). **Remediation:** keep cleartext but scope it with a `network_security_config.xml` (allow cleartext only where required, disallow for the app's own future endpoints), and document the tradeoff. **Verify:** manifest references config; https-only hosts rejected on cleartext.

### SEC-02 Arbitrary URL fetch (SSRF-style, on device)
- **Scenario:** a malicious/compromised playlist (or a custom URL the user pastes) can make the app fetch arbitrary hosts (stream URLs, `tvg-logo` images). Because there's no server, this is **client-side only** — no internal-network pivot beyond the user's own device/LAN. A crafted playlist could point at LAN IPs (e.g. `http://192.168.x.x`) and the app would GET them.
- **Severity:** Low (user-initiated, on-device). **Remediation:** treat playlists as untrusted (already do — parse defensively); optionally block obviously-private ranges for logos, and show the custom-URL host to the user. **Do not** claim SSRF protection is needed server-side — there is no server.

### SEC-03 Backup rules
- `allowBackup="true"` with no `fullBackupContent`/`dataExtractionRules`. Backed-up data = favorites + selected country/language + custom URL. **No secrets/PII.** **Risk:** low. **Remediation:** add explicit `dataExtractionRules` (Android 12+) and `fullBackupContent`, or set `allowBackup="false"` if backup isn't desired.

### SEC-04 Dependency scanning
- No `dependency-check` / `gradle versions` / Dependabot for the Android deps (Media3 1.5.1, OkHttp 4.12.0, Coil 2.7.0, Compose BOM 2024.12.01, Navigation 2.8.5). None are known-vulnerable as bundled, but there's no ongoing check. **Remediation:** add Dependabot (Gradle ecosystem) + optional OWASP dependency-check in CI. (Note: the earlier Next.js scaffold that had a critical advisory was removed; current stack is Android-only.)

### SEC-05 Release signing / build
- Only `assembleDebug` runs in CI; `buildTypes.release` enables R8/shrink but there is **no `signingConfig`** → release artifact would be unsigned or debug-signed. **Risk:** integrity/distribution. **Remediation:** add a release `signingConfig` sourced from CI secrets (keystore not in repo — confirmed `.gitignore` excludes `*.jks`/`*.keystore`), verify `assembleRelease` in CI.

### SEC-06 / SEC-07 Transport & content trust
- No certificate pinning (acceptable — public content, pinning would break rotating CDNs). HLS is decoded by Media3/hls.js; both are current. Keep dependencies patched.

## What was checked and is CLEAN
- **No hardcoded secrets / API keys / tokens** in `app/src` or `webos` (grep clean; only `hls.min.js` minified library matched generic words).
- **No committed keystores / `local.properties`** (`.gitignore` covers them).
- Permissions are **minimal and justified**: INTERNET, ACCESS_NETWORK_STATE, WAKE_LOCK. No location/contacts/storage/camera/mic.
- `MainActivity exported="true"` is **correct and required** (LAUNCHER/LEANBACK intent); no other exported components; no custom `intent-filter` deep links to abuse.
- No SQL (no DB) → no SQLi. No WebView in Android client → no JS-bridge/XSS on Android. (webOS is a web app — see below.)

## webOS-specific
- `webos/index.html` + `app.js`: builds channel tiles with `textContent`/`createElement` (no `innerHTML` with remote data) → low DOM-XSS risk. Playlist names are set via `textContent`. **Keep it that way** — do not switch channel names/logos to `innerHTML`.
- `usesCleartextTraffic` equivalent: webOS app loads `http` streams; same MITM tradeoff as SEC-01.

## Not tested / needs live verification
- Behaviour with a maliciously huge/malformed playlist (DoS via memory) — NOT TESTED. Recommend a size cap on fetched playlists.
- webOS CORS / mixed-content on real TVs — NOT TESTED.
