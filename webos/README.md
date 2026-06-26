# Kuklive for LG webOS

Kuklive as a native **LG webOS** TV app — same IPTV experience (M3U playlists,
category / country / language filters, HLS live playback) built with plain
HTML/JS and [hls.js](https://github.com/video-dev/hls.js), navigable with the
LG remote (Magic Remote pointer **or** the arrow buttons).

## Files

```
webos/
  appinfo.json    # webOS app manifest
  index.html      # UI + styles
  app.js          # playlist parsing, filters, remote nav, player
  hls.min.js      # bundled HLS engine (fallback when native HLS fails)
  icon.png        # 80×80 launcher icon
  largeIcon.png   # 130×130 launcher icon
```

## Get the .ipk

The **Build webOS IPK** GitHub Actions workflow packages an installable
`com.kuklive.app_1.0.0_all.ipk` on every push and publishes it to the
[`webos-latest`](../../releases/tag/webos-latest) release.

To build it yourself:

```bash
npm install -g @webosose/ares-cli
ares-package webos -o out      # -> out/com.kuklive.app_1.0.0_all.ipk
```

## Install on your LG TV

LG TVs don't allow arbitrary app installs by default — enable Developer Mode
first (free LG account required):

1. On the TV, install **Developer Mode** from the LG Content Store and sign in
   with your LG developer account, then toggle **Dev Mode Status: ON** (the TV
   restarts).
2. On your computer:
   ```bash
   npm install -g @webosose/ares-cli
   ares-setup-device                       # add your TV (IP shown in the Dev Mode app)
   ares-install ./out/com.kuklive.app_1.0.0_all.ipk -d <device-name>
   ```
   Prefer a GUI? Use the **webOS Studio** VS Code extension or the **webOS Dev
   Manager** app — point it at the `.ipk` and your TV.
3. Launch **Kuklive** from the TV's app list.

> Dev Mode sessions expire after ~50 hours; just re-extend them in the
> Developer Mode app. This is an LG restriction, not a Kuklive one.

## Using the app

- **Magic Remote**: point and click tiles; scroll the wheel to browse.
- **Arrow remote**: arrows move between channels, **OK** plays, **Back** exits
  the player. In the player, **◀ / ▶** change channel.
- Filter by **Category / Country / Language** or search by name.

## Notes

- Playback tries the TV's native HLS pipeline first (no CORS limits), then
  falls back to hls.js. Some streams may still be geo-blocked by their provider.
- Default playlist is the iptv-org community index; it loads over the network.
