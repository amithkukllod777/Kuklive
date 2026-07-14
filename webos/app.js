/* Kuklive for webOS — IPTV live TV player. */
(function () {
  "use strict";

  // Served from GitHub's raw host (reliable where github.io is blocked).
  var DEFAULT_PLAYLIST = "https://raw.githubusercontent.com/iptv-org/iptv/gh-pages/countries/in.m3u";
  var MAX_RENDER = 300; // keep the TV responsive with huge playlists

  // Verified free-to-air streams, shown first (mirrors the Android curated list).
  var CURATED = [
    "#EXTM3U",
    '#EXTINF:-1 tvg-country="IN" group-title="News",Aaj Tak',
    "https://feeds.intoday.in/aajtak/api/aajtakhd/master.m3u8",
    '#EXTINF:-1 tvg-country="IN" group-title="News",India Today',
    "https://indiatodaylive.akamaized.net/hls/live/2014320/indiatoday/indiatodaylive/playlist.m3u8",
    '#EXTINF:-1 tvg-country="IN" group-title="News",NDTV India',
    "https://ndtvindiaelemarchana.akamaized.net/hls/live/2003679/ndtvindia/master.m3u8",
    '#EXTINF:-1 tvg-country="IN" group-title="News",ABP News',
    "https://abplivetv.pc.cdn.bitgravity.com/httppush/abp_livetv/abp_abpnews/master.m3u8",
    '#EXTINF:-1 tvg-country="IN" group-title="News",ABP Ananda',
    "https://abplivetv.pc.cdn.bitgravity.com/httppush/abp_livetv/abp_ananda/master.m3u8",
  ].join("\n");

  function isPlayable(url) {
    var u = url.toLowerCase();
    return u.indexOf("youtube.com") === -1 && u.indexOf("youtu.be") === -1;
  }

  // ----- DOM -----
  var grid = document.getElementById("grid");
  var stateEl = document.getElementById("state");
  var hintEl = document.getElementById("hint");
  var countEl = document.getElementById("count");
  var searchEl = document.getElementById("search");
  var fCategory = document.getElementById("fCategory");
  var fCountry = document.getElementById("fCountry");
  var fLanguage = document.getElementById("fLanguage");
  var reloadBtn = document.getElementById("reload");

  var player = document.getElementById("player");
  var video = document.getElementById("video");
  var pName = document.getElementById("pName");
  var pPos = document.getElementById("pPos");
  var pErr = document.getElementById("pErr");

  // ----- State -----
  var channels = [];
  var rendered = [];
  var playing = [];
  var pIdx = 0;
  var hls = null;
  var triedHlsJs = false;

  // ----- Country helpers -----
  var regionNames = null;
  try { regionNames = new Intl.DisplayNames(["en"], { type: "region" }); } catch (e) {}

  function countryName(code) {
    code = (code || "").toUpperCase();
    if (!/^[A-Z]{2}$/.test(code)) return null;
    if (regionNames) {
      try { var n = regionNames.of(code); if (n && n !== code) return n; } catch (e) {}
    }
    return code;
  }
  function flagEmoji(code) {
    code = (code || "").toUpperCase();
    if (!/^[A-Z]{2}$/.test(code)) return "";
    return String.fromCodePoint(0x1f1e6 + code.charCodeAt(0) - 65, 0x1f1e6 + code.charCodeAt(1) - 65);
  }
  function countryDisplay(code) {
    var n = countryName(code);
    if (!n) return null;
    var f = flagEmoji(code);
    return f ? f + " " + n : n;
  }

  // ----- M3U parsing -----
  var ATTR = /([\w-]+)="([^"]*)"/g;
  var QUALITY = /\s*\((?:\d{3,4}[pi]|[248]K|U?HD|FHD|SD)\)/gi;
  var BRACKET = /\s*\[[^\]]*]/g;

  function cleanName(s) {
    if (!s) return "";
    return s.replace(BRACKET, "").replace(QUALITY, "").replace(/\s{2,}/g, " ").trim();
  }
  function splitList(s, seps) {
    if (!s) return [];
    var re = seps === "lang" ? /[;,]/ : /;/;
    var seen = {}, out = [];
    s.split(re).forEach(function (p) {
      p = p.trim();
      if (p && !seen[p]) { seen[p] = 1; out.push(p); }
    });
    return out;
  }
  function resolveCountry(attrs) {
    var c = attrs["tvg-country"];
    if (c) return c.length === 2 ? (countryDisplay(c) || c.toUpperCase()) : c;
    var id = attrs["tvg-id"] || "";
    var dot = id.lastIndexOf(".");
    var suffix = dot !== -1 ? id.slice(dot + 1) : "";
    if (suffix.length === 2) return countryDisplay(suffix);
    return null;
  }

  function parse(text) {
    var lines = text.split(/\r?\n/);
    var out = [];
    var attrs = {}, name = null, idx = 0, m;
    for (var i = 0; i < lines.length; i++) {
      var raw = lines[i].trim();
      if (!raw) continue;
      if (raw.indexOf("#EXTM3U") === 0) continue;
      if (raw.indexOf("#EXTINF") === 0) {
        attrs = {}; ATTR.lastIndex = 0;
        while ((m = ATTR.exec(raw))) attrs[m[1]] = m[2];
        var c = raw.lastIndexOf(",");
        name = c !== -1 ? raw.slice(c + 1).trim() : (attrs["tvg-name"] || "");
      } else if (raw.charAt(0) === "#") {
        continue;
      } else {
        if (/^(https?|rtmp)/i.test(raw)) {
          out.push({
            id: idx++ + "_" + raw,
            name: cleanName(name) || "Channel " + (out.length + 1),
            url: raw,
            logo: attrs["tvg-logo"] || "",
            categories: splitList(attrs["group-title"], "cat"),
            country: resolveCountry(attrs),
            languages: splitList(attrs["tvg-language"], "lang")
          });
        }
        attrs = {}; name = null;
      }
    }
    return out;
  }

  function parseGroupsByUrl(text) {
    var lines = text.split(/\r?\n/);
    var map = {}, group = null;
    for (var i = 0; i < lines.length; i++) {
      var raw = lines[i].trim();
      if (!raw) continue;
      if (raw.indexOf("#EXTINF") === 0) {
        var m = raw.match(/group-title="([^"]*)"/);
        group = m ? m[1] : null;
      } else if (raw.charAt(0) === "#") {
        continue;
      } else {
        if (group && /^(https?|rtmp)/i.test(raw)) {
          if (!map[raw]) map[raw] = {};
          map[raw][group] = 1;
        }
        group = null;
      }
    }
    return map;
  }

  function languageIndexUrl(url) {
    if (url.indexOf("iptv-org.github.io/iptv") === -1) return null;
    if (url.indexOf("index.language.m3u") !== -1) return null;
    return url.slice(0, url.lastIndexOf("/")) + "/index.language.m3u";
  }

  // ----- Loading -----
  function showState(html) {
    grid.style.display = "none";
    stateEl.style.display = "flex";
    stateEl.innerHTML = html;
  }
  function showGrid() {
    stateEl.style.display = "none";
    grid.style.display = "grid";
  }

  function fetchText(url) {
    return fetch(url, { headers: { "User-Agent": "Kuklive/1.0" } }).then(function (r) {
      if (!r.ok) throw new Error("HTTP " + r.status);
      return r.text();
    });
  }

  function load() {
    var url = DEFAULT_PLAYLIST;
    showState('<div class="spinner"></div><div>Loading channels…</div>');
    fetchText(url)
      .then(function (text) {
        // Curated verified channels first, then the country playlist; drop
        // YouTube links (not playable) and de-duplicate by URL.
        var all = parse(CURATED).concat(parse(text));
        var seen = {};
        channels = all.filter(function (ch) {
          if (!isPlayable(ch.url) || seen[ch.url]) return false;
          seen[ch.url] = 1;
          return true;
        });
        if (!channels.length) throw new Error("No channels found");
        populateFilters();
        render();
      })
      .catch(function (err) {
        showState(
          '<div>Could not load channels.<br>' + String(err.message || err) + "</div>" +
          '<button id="retry">Retry</button>'
        );
        var rb = document.getElementById("retry");
        if (rb) rb.addEventListener("click", load);
        if (rb) rb.focus();
      });
  }

  function enrichLanguages(url) {
    var langUrl = languageIndexUrl(url);
    if (!langUrl) return;
    fetchText(langUrl)
      .then(function (text) {
        var map = parseGroupsByUrl(text);
        var changed = false;
        channels.forEach(function (ch) {
          var g = map[ch.url];
          if (g) { ch.languages = Object.keys(g); changed = true; }
        });
        if (changed) { populateFilters(); render(); }
      })
      .catch(function () { /* best-effort */ });
  }

  // ----- Filters -----
  function distinct(arr) {
    var seen = {}, out = [];
    arr.forEach(function (v) { if (v && !seen[v]) { seen[v] = 1; out.push(v); } });
    return out.sort();
  }
  function fillSelect(sel, label, options) {
    var keep = sel.value;
    sel.innerHTML = "";
    var all = document.createElement("option");
    all.value = ""; all.textContent = "All " + label;
    sel.appendChild(all);
    options.forEach(function (o) {
      var op = document.createElement("option");
      op.value = o; op.textContent = o;
      sel.appendChild(op);
    });
    if (options.indexOf(keep) !== -1) sel.value = keep;
  }
  function populateFilters() {
    var cats = [], countries = [], langs = [];
    channels.forEach(function (ch) {
      cats = cats.concat(ch.categories);
      if (ch.country) countries.push(ch.country);
      langs = langs.concat(ch.languages);
    });
    fillSelect(fCategory, "Categories", distinct(cats));
    fillSelect(fCountry, "Countries", distinct(countries));
    fillSelect(fLanguage, "Languages", distinct(langs));
    // Hide filters with no data.
    fCountry.style.display = countries.length ? "" : "none";
    fLanguage.style.display = langs.length ? "" : "none";
  }

  function filtered() {
    var q = searchEl.value.trim().toLowerCase();
    var cat = fCategory.value, country = fCountry.value, lang = fLanguage.value;
    return channels.filter(function (ch) {
      if (cat && ch.categories.indexOf(cat) === -1) return false;
      if (country && ch.country !== country) return false;
      if (lang && ch.languages.indexOf(lang) === -1) return false;
      if (q && ch.name.toLowerCase().indexOf(q) === -1) return false;
      return true;
    });
  }

  // ----- Rendering -----
  function render() {
    showGrid();
    var list = filtered();
    rendered = list.slice(0, MAX_RENDER);
    grid.innerHTML = "";
    var frag = document.createDocumentFragment();
    rendered.forEach(function (ch, i) {
      var tile = document.createElement("div");
      tile.className = "tile";
      tile.tabIndex = 0;

      var logo = document.createElement("div");
      logo.className = "logo";
      if (ch.logo) {
        var img = document.createElement("img");
        img.src = ch.logo;
        img.onerror = function () { logo.innerHTML = '<span class="ph">▶</span>'; };
        logo.appendChild(img);
      } else {
        logo.innerHTML = '<span class="ph">▶</span>';
      }

      var name = document.createElement("div");
      name.className = "name";
      name.textContent = ch.name;

      tile.appendChild(logo);
      tile.appendChild(name);
      tile.addEventListener("click", function () { openPlayer(rendered, i); });
      frag.appendChild(tile);
    });
    grid.appendChild(frag);

    countEl.textContent = list.length + " channels";
    hintEl.textContent = list.length > MAX_RENDER
      ? "Showing first " + MAX_RENDER + " — use search or filters to narrow down."
      : "";
  }

  // ----- Player -----
  function openPlayer(list, idx) {
    playing = list; pIdx = idx;
    player.classList.add("open");
    playCurrent();
  }
  function closePlayer() {
    player.classList.remove("open");
    stopVideo();
    var t = grid.querySelector(".tile");
    if (rendered[pIdx]) {
      var tiles = grid.querySelectorAll(".tile");
      if (tiles[pIdx]) t = tiles[pIdx];
    }
    if (t) t.focus();
  }
  function stopVideo() {
    if (hls) { try { hls.destroy(); } catch (e) {} hls = null; }
    try { video.pause(); } catch (e) {}
    video.removeAttribute("src");
    try { video.load(); } catch (e) {}
  }
  function showPlayError() {
    pErr.style.display = "flex";
    pErr.textContent = "Can't play this channel. Try another one.";
  }
  function playCurrent() {
    var ch = playing[pIdx];
    if (!ch) return;
    pName.textContent = ch.name;
    pPos.textContent = pIdx + 1 + " / " + playing.length;
    pErr.style.display = "none";
    triedHlsJs = false;
    stopVideo();
    // Native HLS first (no CORS on segment fetch); fall back to hls.js on error.
    video.src = ch.url;
    var p = video.play();
    if (p && p.catch) p.catch(function () {});
  }
  function useHlsJs() {
    var ch = playing[pIdx];
    if (!ch || triedHlsJs) { showPlayError(); return; }
    triedHlsJs = true;
    if (!(window.Hls && Hls.isSupported())) { showPlayError(); return; }
    if (hls) { try { hls.destroy(); } catch (e) {} }
    video.removeAttribute("src");
    hls = new Hls({ maxBufferLength: 30, manifestLoadingTimeOut: 15000 });
    hls.loadSource(ch.url);
    hls.attachMedia(video);
    hls.on(Hls.Events.ERROR, function (evt, data) {
      if (data && data.fatal) showPlayError();
    });
    var p = video.play();
    if (p && p.catch) p.catch(function () {});
  }
  video.addEventListener("error", function () {
    if (player.classList.contains("open")) useHlsJs();
  });
  function nextCh() { if (pIdx < playing.length - 1) { pIdx++; playCurrent(); } }
  function prevCh() { if (pIdx > 0) { pIdx--; playCurrent(); } }

  // ----- Remote / keyboard navigation -----
  function moveFocus(code) {
    var tiles = Array.prototype.slice.call(grid.querySelectorAll(".tile"));
    if (!tiles.length) return;
    var cur = document.activeElement;
    if (!cur || cur.className.indexOf("tile") === -1) { tiles[0].focus(); return; }
    var r = cur.getBoundingClientRect();
    var cx = r.left + r.width / 2, cy = r.top + r.height / 2;
    var best = null, bestD = Infinity;
    for (var i = 0; i < tiles.length; i++) {
      var t = tiles[i];
      if (t === cur) continue;
      var tr = t.getBoundingClientRect();
      var tx = tr.left + tr.width / 2, ty = tr.top + tr.height / 2;
      var dx = tx - cx, dy = ty - cy, ok = false;
      if (code === 37) ok = dx < -5 && Math.abs(dy) < r.height;
      else if (code === 39) ok = dx > 5 && Math.abs(dy) < r.height;
      else if (code === 38) ok = dy < -5;
      else if (code === 40) ok = dy > 5;
      if (!ok) continue;
      var d = (code === 38 || code === 40) ? Math.abs(dy) * 3 + Math.abs(dx) : Math.abs(dx) * 3 + Math.abs(dy);
      if (d < bestD) { bestD = d; best = t; }
    }
    if (best) { best.focus(); best.scrollIntoView({ block: "nearest" }); }
  }

  document.addEventListener("keydown", function (e) {
    var code = e.keyCode;
    if (player.classList.contains("open")) {
      if (code === 461 || code === 27 || code === 8) { e.preventDefault(); closePlayer(); }
      else if (code === 37) { e.preventDefault(); prevCh(); }
      else if (code === 39) { e.preventDefault(); nextCh(); }
      return;
    }
    if (code === 461) return; // Back at root — let the system handle exit.
    var ae = document.activeElement, tag = ae ? ae.tagName : "";
    if (tag === "INPUT" || tag === "SELECT") return; // native handling for fields
    if (code === 37 || code === 38 || code === 39 || code === 40) {
      e.preventDefault(); moveFocus(code);
    } else if (code === 13) {
      if (ae && ae.className && ae.className.indexOf("tile") !== -1) { e.preventDefault(); ae.click(); }
    }
  });

  // ----- Wiring -----
  searchEl.addEventListener("input", render);
  fCategory.addEventListener("change", render);
  fCountry.addEventListener("change", render);
  fLanguage.addEventListener("change", render);
  reloadBtn.addEventListener("click", load);

  load();
})();
