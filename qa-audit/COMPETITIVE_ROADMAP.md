# Competitive Roadmap — Kuklive

Prioritization inputs (1–5 each): User impact (U), Business (B), Competitive urgency (C), Frequency (F), Evidence confidence (E), Effort (Eff, lower=cheaper), Maintenance (M), Risk (R). Priority score ≈ (U+B+C+F+E) − (Eff+M+R). Categories: P0 blocker · P1 high-value · P2 parity · P3 optional · DO NOT BUILD.

## Immediate improvements (this/next build)
| Item | Problem solved | Evidence | User value | Comp value | Cx | Success metric | Priority |
|---|---|---|---|---|---|---|---|
| Confirm KUK-002/003 on device | "no channels / none play" | user reports | High | — | S | ≥300 ch load, ≥3/5 curated play | P0 |
| Port fixes to webOS | LG build broken/slow | KUK-001 | High (TV) | Diff | M | webOS loads on same network | P1 |
| Coil fallback + custom-URL label + auto-skip/timeout setting | polish + "finds a working one" | BUG/UI audit | Med | Parity | S | no broken tiles; auto-advances | P1 |

## Next release
| Item | Problem | Evidence | U value | Comp value | Cx | Metric | Priority |
|---|---|---|---|---|---|---|---|
| **EPG / now-next** from `x-tvg-url` | grid is info-light; #1 quality signal | matrix | High | Table-stakes | L | now/next shown per channel | P1 |
| **Xtream Codes login** | most providers use it | matrix | High | Table-stakes | L | provider account loads live/VOD | P1 |
| **Multiple playlists** | only one custom URL | options gap | High | Table-stakes | L | manage ≥2 sources | P1 |
| Parental PIN | adult content, families | matrix | Med | Table-stakes | M | PIN locks categories | P2 |
| Audio/subtitle track menu | multilingual streams | matrix | Med | Table-stakes | M | user switches track | P2 |
| P0 unit tests + crash reporting | no safety net | MISSING_TESTS | High(eng) | — | M | CI runs tests; crashes visible | P1 |

## Next quarter
| Item | Problem | U | Comp | Cx | Metric |
|---|---|---|---|---|---|
| VOD / Series (via Xtream) | table-stakes for provider users | Med | Table-stakes | L | VOD browseable |
| Chromecast / external player | living-room use | Med | Parity | M | cast works |
| PiP (implement or drop flag) | declared-not-impl | Low-Med | Parity | M | PiP on home |
| Recently-watched + favorite groups | heavy-user retention | Med | Parity | M | recents list |
| Localization (Hindi + majors) | India audience | Med | Parity | L | UI in Hindi |
| Resume last channel; theme toggle; reset/clear-cache | cheap satisfaction wins | Low-Med | Parity | S | toggles work |

## Long-term opportunities
- Shared core between Android & webOS (kill drift permanently).
- Curated "Kuklive Picks" per country (the working-channels differentiator, expanded & maintained).
- Optional "Kuk Originals / Creator TV / OTT Apps" structure for owned/licensed content (per `NEXT_VERSION.md`) — only with a content pipeline.

## DO NOT BUILD (for now)
- **DVR/recording, timeshift server, export/import, data-saver transcoding** — high maintenance, low value for a free casual audience; revisit only if pivoting to a power-user/TV product.
- **Copying competitors' premium-tier feature breadth wholesale** — would bloat the app and erode the "simple, it-just-works" differentiator.
- **Analytics SDKs that harm the privacy/no-account positioning** — prefer minimal, privacy-respecting crash reporting only.

## Strengths to protect (don't regress)
Fast 2-tap onboarding · bundled curated working channels · fully free / no account · webOS reach (once fixed) · clean modern Compose UI. Every new power feature must live behind Settings so the default experience stays effortless.
