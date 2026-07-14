# Missing Tests — Kuklive (prioritized by risk)

Current automated coverage: **0%.** Recommended additions, highest-value first. Pure-logic units (no device) are cheapest and cover the riskiest code.

## P0 — Pure JVM unit tests (fast, no device, highest ROI)
1. **`M3UParserTest`** — the core parser, currently only hand-verified.
   - parses name/url/logo/group; splits multi-value `group-title` on `;`; `cleanName` strips `(1080p)`/`[Not 24x7]`; `tvg-country`/`tvg-id` suffix → country; skips `#EXTGRP`/`#EXTVLCOPT`; ignores non-http/rtmp; `parseGroupTitlesByUrl` accumulates languages per URL.
2. **`CategoryTaxonomyTest`** — `genreFor` maps known iptv-org keys, hides `xxx`/country-names/`vod*`, title-cases unknowns; `orderIndex` ordering.
3. **`CountryUtilTest`** — `name("in")=="India"`, `flag("in")=="🇮🇳"`, rejects bad codes.
4. **`UiStateTest`** — `visibleChannels` applies tab+category+search; `categories` dedupe+order; favorites membership.
5. **`PlaylistMergeTest`** (extract merge/dedupe/isPlayable/language-guard into a testable pure function) — dedupe by URL keeps first; YouTube excluded; language filter never empties the list (KUK-002 regression guard).

## P1 — Repository / coroutine tests (with a fake OkHttp/dispatcher)
6. `PlaylistRepository` with a stubbed HTTP client: country-only, language-only, both (intersection), custom-URL override, all-empty→Free-TV, network failure → graceful fallback (only curated/Free-TV remain).
7. `SettingsStore` (Robolectric/instrumented): save/restore countries/languages/customUrl/favorites; empty set = "all".

## P2 — Instrumented / UI (Compose test)
8. Setup screen: multi-select toggle + "Show channels" persists and navigates.
9. Channels screen: category chip filters grid; search filters; favorite toggle updates Favorites tab.
10. Player screen (headless assertions): index bounds on prev/next; timeout sets error state; screen-on flag set/cleared on enter/exit.

## P3 — E2E / manual scripts (documented, since infra is limited)
11. Fresh install → setup → play a curated channel → favorite → reopen (state restored).
12. Airplane-mode → open app → error state → recover on reconnect.
13. Custom URL → load → clear → back to country/language.
14. Android TV D-pad navigation + LEANBACK launcher presence.
15. webOS: install IPK, remote navigation, native-vs-hls.js playback, CORS.

## Enablers (not yet present)
- Add `testImplementation("junit:junit:4.13.2")` + `kotlinx-coroutines-test`; `androidTestImplementation` Compose UI test + Espresso.
- Add a CI step `./gradlew testDebugUnitTest` (P0/P1) — cheap, fast, would have caught the host-URL and guessed-URL regressions earlier.
- Add crash reporting (Crashlytics/Sentry) so field failures are visible without user screenshots.
