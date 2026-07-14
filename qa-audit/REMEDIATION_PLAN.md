# Remediation Plan — Kuklive

Complexity: S (<½ day) · M (~1–2 days) · L (~1 week) · XL (>1 week). Owner types: Eng, Product, Legal, Design.

## Immediate — before any wider release
| Action | Ref | Owner | Cx | Depends | Verify |
|---|---|---|---|---|---|
| Confirm KUK-002/003 fixes on device (channels return; curated 5 play) | KUK-002/003 | Eng+User | S | new APK | user tests India+Hindi; ≥3/5 curated play |
| Port Android source strategy to webOS (`app.js`) | KUK-001 | Eng | M | — | webOS loads channels on same network; parity checklist |
| Add release `signingConfig` (CI secret keystore) + run `assembleRelease` in CI | SEC-05, PRC-4/5 | Eng | S | keystore | signed release artifact built green |
| Privacy policy + content/rights disclaimer + first-run notice | PRC-14/15 | Product+Legal | M | — | pages linked in app/settings; consent shown once |
| Add crash reporting (Sentry/Crashlytics) | KUK-006, PRC-11 | Eng | S | — | test crash appears in dashboard |

## Short term — next sprint
| Action | Ref | Owner | Cx | Verify |
|---|---|---|---|---|
| P0 JVM unit tests (M3UParser, taxonomy, merge/dedupe, UiState) + CI `testDebugUnitTest` | MISSING_TESTS P0 | Eng | M | tests run in CI; parser/host regressions covered |
| Precompute derived lists in ViewModel (`derivedStateOf`) | KUK-007 | Eng | S | no jank scrolling/typing at ~800 ch |
| Cache last playlist (TTL) for warm start + offline resilience | KUK-008 | Eng | M | 2nd launch instant; airplane-mode shows cached list |
| Coil error/fallback placeholder; region bar shows "Custom playlist" | KUK-004/010 | Eng+Design | S | broken logos → TV icon; custom mode labeled |
| Audio focus + becoming-noisy handling | KUK-009 | Eng | S | call pauses playback; headphone unplug pauses |
| `network_security_config.xml` scoping cleartext + explicit backup rules | SEC-01/03 | Eng | S | manifest references configs |
| Debounce search | KUK-007 | Eng | S | smooth typing |

## Medium term
| Action | Ref | Owner | Cx | Verify |
|---|---|---|---|---|
| EPG / program guide from `x-tvg-url` (now ignored) | Feature gap | Eng | L | current programme shown per channel |
| Implement PiP (or remove the flag) | KUK-005 | Eng | M | player enters PiP on home |
| Auto-skip/retry option on dead stream | KUK-011 | Eng+Design | S | optional next-on-fail |
| Multi-playlist management (add/name/switch several sources) | Feature gap | Eng+Design | L | user manages >1 source |
| Xtream Codes API login (provider host+user+pass) | Feature gap | Eng | L | provider account loads VOD/live/EPG |
| Compose UI tests + Android TV D-pad pass | MISSING_TESTS P2 | Eng | M | key flows automated |
| Dependabot + dependency-check in CI | SEC-04 | Eng | S | PRs for updates |

## Long term / tech debt
- webOS: extract shared parsing/source logic to avoid future drift (single source of truth).
- Subtitles/audio-track selection, Chromecast, recording/timeshift (see roadmap).
- Localization framework (currently English-only UI) + RTL.
- Baseline profile for startup/scroll.
- Formal rollback/runbook + support channel.
