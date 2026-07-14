# Test Coverage Matrix — Kuklive

**Automated coverage: 0%.** No unit, integration, UI, or E2E tests exist in the repo (`find` for `*Test*.kt` / `test` / `androidTest` returns nothing). All results below are **manual/code-review only**. "Automated" column is MISSING everywhere.

Legend: PASS / FAIL / PARTIAL / NOT TESTED / N/A

| Feature | Smoke | Functional | Negative | Boundary | API/Net | Security | Regression | UI/UX | Automated | Result |
|---|---|---|---|---|---|---|---|---|---|---|
| App launch / splash gate | PASS(manual) | PARTIAL | NOT TESTED | N/A | N/A | NOT TESTED | PARTIAL | PASS | MISSING | PARTIAL |
| Setup (country/language pick) | PASS | PARTIAL | NOT TESTED | NOT TESTED (empty=all) | N/A | N/A | PARTIAL | PASS | MISSING | PARTIAL |
| M3U parsing | PARTIAL (python port only) | PARTIAL | NOT TESTED | NOT TESTED | N/A | NOT TESTED | NOT TESTED | N/A | MISSING | PARTIAL |
| Country/language load + intersection | PARTIAL (curl verified 325∩) | PARTIAL | NOT TESTED | NOT TESTED | PARTIAL | NOT TESTED | FAIL→FIXED (host bug) | N/A | MISSING | PARTIAL |
| Curated bundled list | PASS (shows) | PARTIAL | NOT TESTED | N/A | NOT TESTED (streams) | N/A | N/A | PARTIAL | MISSING | PARTIAL |
| Custom URL override | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED (SSRF-ish) | NOT TESTED | NOT TESTED | MISSING | NOT TESTED |
| Channel grid render | PASS | PARTIAL | N/A | NOT TESTED (10k rows) | N/A | N/A | PASS | PASS | MISSING | PARTIAL |
| Category filter | PASS | PARTIAL | NOT TESTED | NOT TESTED | N/A | N/A | PARTIAL | PASS | MISSING | PARTIAL |
| Search | PARTIAL | PARTIAL | NOT TESTED | NOT TESTED | N/A | N/A | NOT TESTED | PASS | MISSING | PARTIAL |
| Favorites | PARTIAL | NOT TESTED | NOT TESTED | N/A | N/A | N/A | NOT TESTED | PASS | MISSING | PARTIAL |
| Player: play HLS | PASS (NDTV/Aaj Tak) | PARTIAL | PARTIAL (dead→timeout) | NOT TESTED | PARTIAL | NOT TESTED | PASS | PASS | MISSING | PARTIAL |
| Player: zapping | PASS | PARTIAL | NOT TESTED (index bounds handled) | NOT TESTED | N/A | N/A | NOT TESTED | PASS | MISSING | PARTIAL |
| Keep-screen-on | NOT TESTED | NOT TESTED | N/A | N/A | N/A | N/A | NOT TESTED | N/A | MISSING | NOT TESTED |
| webOS client | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED (CORS) | NOT TESTED | FAIL (drift) | NOT TESTED | MISSING | NOT TESTED |
| Offline / network loss | NOT TESTED | NOT TESTED | PARTIAL (error state exists) | N/A | NOT TESTED | N/A | N/A | PARTIAL | MISSING | NOT TESTED |
| Config change / rotation | NOT TESTED | NOT TESTED | N/A | N/A | N/A | N/A | NOT TESTED | NOT TESTED | MISSING | NOT TESTED |

**Executed with evidence this session:** M3U parse logic (python port), country/language URL intersection (curl → 325), Free-TV/iptv-org reachability (curl), CI debug build (green). **Could not execute:** actual stream playback (sandbox proxy blocks CDN hosts), on-device UI/TV/webOS, rotation, offline.
