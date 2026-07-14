# UI / UX Competitor Audit — Kuklive

**Date:** 2026-07-14. Competitor benchmarks are **INFERRED** (verify before acting). Recommendations extract principles, not visual copies.

| Screen / workflow | Current Kuklive | Competitor benchmark (inferred) | Problem | User impact | Recommendation | Type | Priority |
|---|---|---|---|---|---|---|---|
| **First-run onboarding** | Country + language multi-select, one "Show channels" button | TiViMate: add-playlist wizard (URL/Xtream); Smarters: login form | Kuklive is *simpler and faster* — a strength; but no "paste playlist/Xtream" path on first run | Positive; power users must dig into Settings for custom URL | Keep the fast path; add an optional "I have a playlist/Xtream" link on setup | Both | P2 |
| **Channel grid** | Adaptive logo grid, genre chips, search | Competitors default to TV-style rows/lists with EPG "now/next" | No EPG/now-next; grid is clean but info-light | Med — users can't see what's on | Add now/next line under name when EPG exists | Both | P1 |
| **Category navigation** | Emoji genre chips (normalised) | Left-rail categories / group list | Chips are modern & readable — a strength; horizontal scroll can hide chips | Low | Keep; consider a "more" overflow | Visual | P3 |
| **Player** | Fullscreen, top title, prev/next, buffering spinner, 25s timeout error | EPG overlay, channel list drawer, audio/subtitle menus, PiP | No in-player channel list / no track menus | Med | Add a channel drawer + track selection | Functional | P2 |
| **Error / dead stream** | Clear "not responding… try next ▶" | Auto-retry / auto-skip common | Manual only | Med | Optional auto-skip; shorten timeout to ~12–15s | Functional | P2 |
| **Empty / loading states** | Spinner + "Loading channels…"; "No channels found" | Skeletons, contextual empties | Adequate, generic | Low | Add "try changing region / add playlist" CTA in empty state | Both | P3 |
| **Logos** | Coil; broken URLs show provider error image | Placeholder on failure | Broken tiles look buggy | Low-Med | Coil `error()`/`fallback()` to TV icon | Visual | P1(quick) |
| **Region indicator** | Region bar summarises selection | — | Ignores custom-URL mode | Low | Show "Custom playlist" when active | Visual | P2 |
| **Dark/light mode** | Dark theme; Material3 dynamic light exists but app is dark-first | Both, often theme options | Light mode untested; forced dark is fine for TV | Low | Verify light mode or commit to dark | Visual | P3 |
| **Android TV / D-pad** | Compose focus not verified on TV; grid tiles clickable | First-class D-pad + focus states | Focus/scroll on D-pad NOT VERIFIED | High (TV is a target) | Test & add explicit `focusable` + focus visuals; leanback-friendly nav | Functional | P1 |
| **Accessibility** | Some `contentDescription`s; icon buttons labelled | Varies | No font-scaling/screen-reader pass; touch targets ~28dp in spots | Med | 48dp targets, TalkBack pass, large-font test | Both | P2 |
| **Terminology** | "Region", "Any language", genre emojis | Familiar IPTV terms | Clear; "Region" vs "Country" minor | Low | fine | — | — |

## Principles to adopt (not copy)
1. **Now/Next context** beats a bare grid — even a single EPG line raises perceived quality most.
2. **In-player channel switching** (drawer) is expected on TV; zapping-only feels limited.
3. **Fail gracefully and quickly** — short timeout + auto-skip turns "nothing works" into "it finds one that works."
4. **Protect simplicity** — Kuklive's onboarding is a genuine UX win vs competitors' heavier setup; don't regress it while adding power features (gate advanced options behind Settings).
