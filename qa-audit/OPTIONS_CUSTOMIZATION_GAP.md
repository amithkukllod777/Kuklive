# Options & Customization Gap — Kuklive

**Date:** 2026-07-14. Competitor support **INFERRED**. Current support read from source.

| Option | Kuklive | Competitors (inferred) | Gap | Target user | Business value | Cx | Recommendation |
|---|---|---|---|---|---|---|---|
| Playlist source (custom URL) | Available (Settings) | Available | Parity | All | High | — | Keep; add validation + host display |
| Xtream Codes login | Missing | Available | Table-stakes gap | Provider users | High | L | Add host/user/pass source type |
| Multiple playlists | Missing (one URL) | Available | Table-stakes gap | Power users | High | L | Named source list, switchable |
| EPG source (`x-tvg-url`) | Ignored | Configurable | Table-stakes gap | All | High | L | Parse + assignable EPG per source |
| Parental PIN / hide adult | Missing (adult hidden from chips only) | Available | Table-stakes | Families | Med | M | PIN-lock categories |
| Audio / subtitle track | Missing | Available | Table-stakes | Multilingual | Med | M | Media3 track selector UI |
| Player buffer / decoder (HW/SW) | Missing | Available | Parity | Troubleshooters | Low-Med | M | Advanced settings |
| Default startup (last channel/section) | Missing | Available | Parity | Frequent users | Med | S | "Resume last channel" toggle |
| Favorites grouping / recents | Flat favorites only | Grouped + recents | Parity | Heavy users | Med | M | Add "Recently watched" |
| Theme (dark/light/accent) | Dark-first, no toggle | Themes | Parity | Preference | Low | S | Optional light/theme toggle |
| UI language (localization) | English only | Multiple | Parity (India) | Regional users | Med | L | Hindi + majors; RTL later |
| Grid density / layout | Fixed adaptive grid | List/grid options | Parity | Preference | Low | S | Optional list view |
| Timeout / auto-skip behaviour | Fixed 25s, manual next | Configurable | Parity | All | Med | S | Setting + auto-skip |
| Custom-URL mode indicator/label | Missing | n/a | UX | Custom users | Low | S | Show "Custom playlist" |
| Data saver / stream quality | Missing (adaptive HLS handles it) | Some | Low value | Mobile-data users | Low | M | Optional cap |
| Clear cache / reset app | Missing | Available | Parity | Support | Low | S | "Reset" in Settings |
| Export/import settings & favorites | Missing | Some | Low value | Power users | Low | M | Later |

## Judgement
- **Build (table-stakes):** Xtream login, multiple playlists, EPG, parental PIN, audio/subtitle tracks — these unlock "real provider" usage and family-safety.
- **Cheap wins (S):** resume-last, auto-skip/timeout setting, theme toggle, custom-URL label, reset/clear-cache.
- **Where fewer options is better:** keep the **onboarding minimal** (2 pickers). Push all advanced config into Settings so the first run stays a 2-tap experience — that simplicity is a competitive strength, not a gap to "fix."
- **Avoid overbuild:** recording/timeshift and export/import are low-priority for the free, casual audience; only pursue if targeting power/TV users.
