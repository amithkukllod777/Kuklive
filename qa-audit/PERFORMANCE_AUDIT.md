# Performance Audit — Kuklive

**Measured vs assumed:** no on-device profiling was possible in this environment (sandbox blocks stream/CDN hosts and has no device/emulator). All rows are **code-based assumptions** unless marked MEASURED. Network facts below are MEASURED via curl.

| Metric | Observed / Assumed | Expected threshold | Status | Bottleneck | Recommendation |
|---|---|---|---|---|---|
| Cold start to Setup | Assumed <1s (Compose + DataStore) | <1.5s | LIKELY PASS | — | Add a baseline profile |
| Playlist load (India+Hindi) | MEASURED sizes: countries/in.m3u ≈ 765 ch, languages/hin.m3u ≈ 350 ch; 2 parallel HTTP fetches | <4s on 4G | AT RISK on slow nets | 2× network fetch + regex parse each launch | Cache last playlist; parse off main thread (already IO) |
| Playlist load ("all countries") | MEASURED Free-TV main ≈ 1895 ch / 518 KB | <4s | LIKELY PASS | single fetch | ok |
| Parse throughput | Assumed fast (regex, ~1–2k entries) | <300ms | LIKELY PASS | regex per line | fine at current sizes; watch full-index 10k path |
| Grid render | Assumed OK (LazyVerticalGrid, keyed) | 60fps | AT RISK | see below | `derivedStateOf` for derived lists |
| Derived state recompute | CODE SMELL: `categories`/`countries`/`languages`/`visibleChannels` recomputed on every access | O(n) amortized | AT RISK | `UiState` computed getters over ≤~800 items, re-run per recomposition & per keystroke | precompute in ViewModel or wrap in `derivedStateOf` |
| Search typing | Assumed OK ≤800 items; janky at 10k | smooth | AT RISK at scale | filter over full list per keystroke | debounce + precomputed lowercase names |
| Player start (working stream) | User-confirmed plays (Aaj Tak/NDTV) | <5s to first frame | PASS (manual) | network/CDN | ok |
| Dead-stream wait | 25s timeout before error | ≤ acceptable | PARTIAL | fixed timeout | consider 12–15s; add auto-skip option |
| Memory (image logos) | Coil default cache | no leak | NOT MEASURED | many remote logos | Coil handles caching; verify no OOM on 800 tiles |
| Battery (WAKE_LOCK + screen-on) | Screen kept on during playback (intended) | expected | PASS by design | screen-on | correct for a TV app |
| APK size | MEASURED ≈ 21.5 MB debug | <30 MB | PASS | Compose + Media3 | release R8 will shrink |
| Config-cache / build | Gradle config-cache on; CI build ~1.5–3 min | — | PASS | — | ok |

## Top performance actions
1. **Precompute derived lists** in `MainViewModel` (emit `categories`, `visibleChannels` as state) instead of recomputing in `UiState` getters — removes the biggest jank risk. (Effort S)
2. **Cache the last successful playlist** to disk with a short TTL → instant warm starts, resilience to network blips. (Effort M)
3. **Debounce search** (~250ms) and store lowercased names once. (Effort S)
4. **Virtualization is already OK** (LazyVerticalGrid) but avoid re-deriving the source list each recomposition.
5. Optional: cap parsed playlist size (e.g. 20k entries) to bound memory on hostile inputs.
