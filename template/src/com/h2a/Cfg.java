package com.h2a;

import android.content.Context;
import org.json.JSONObject;

/** Runtime settings read once from assets/h2a_config.json (written by the in-browser builder). */
public final class Cfg {
  private Cfg() {}

  public static boolean loaded;
  public static String url = "file:///android_asset/index.html";
  public static int theme = 0xFF1C1C1E;
  public static boolean transparentNav;
  public static boolean hideScrollbars = true;
  public static boolean zoom;
  public static boolean pull;
  public static boolean disableCopy;
  public static boolean blockAds;
  public static boolean adguard;
  public static boolean notif;
  public static boolean geo;
  public static boolean media;
  public static boolean assetLoader;
  public static boolean debug;
  public static boolean splash = true;
  public static int splashColor = 0xFF1C1C1E;
  public static int splashSize = 40;
  public static int splashDuration = 1200;
  public static String splashAnim = "fade";

  public static synchronized void load(Context c) {
    if (loaded) return;
    loaded = true;
    try {
      java.io.InputStream is = c.getAssets().open("h2a_config.json");
      java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
      byte[] buf = new byte[4096];
      int n;
      while ((n = is.read(buf)) > 0) bo.write(buf, 0, n);
      is.close();
      JSONObject o = new JSONObject(new String(bo.toByteArray(), "UTF-8"));
      String u = o.optString("url", "");
      if (u.length() > 0) url = u;
      theme = color(o.optString("theme_color", ""), theme);
      transparentNav = o.optBoolean("transparent_nav", transparentNav);
      hideScrollbars = o.optBoolean("hide_scrollbars", hideScrollbars);
      zoom = o.optBoolean("zoom_enabled", zoom);
      pull = o.optBoolean("pull_refresh", pull);
      disableCopy = o.optBoolean("disable_copy_text", disableCopy);
      adguard = o.optBoolean("adguard_dns", adguard);
      blockAds = o.optBoolean("block_ads", blockAds) || adguard;
      notif = o.optBoolean("notifications", notif);
      geo = o.optBoolean("geolocation", geo);
      media = o.optBoolean("media", media);
      assetLoader = o.optBoolean("asset_loader", assetLoader);
      debug = o.optBoolean("debug", debug);
      splash = o.optBoolean("splash_enabled", splash);
      splashColor = color(o.optString("splash_color", ""), theme);
      splashSize = clamp(o.optInt("splash_image_size", splashSize), 5, 100);
      splashDuration = clamp(o.optInt("splash_duration", splashDuration), 0, 15000);
      splashAnim = o.optString("splash_animation", splashAnim);
    } catch (Exception ignored) {
    }
  }

  /** Decodes a PNG or WebP image from assets; null when missing. */
  public static android.graphics.Bitmap assetBitmap(Context c, String name) {
    try {
      java.io.InputStream is = c.getAssets().open(name);
      android.graphics.Bitmap b = android.graphics.BitmapFactory.decodeStream(is);
      is.close();
      return b;
    } catch (Exception e) {
      return null;
    }
  }

  static int color(String s, int def) {
    if (s == null || s.length() == 0) return def;
    try {
      return android.graphics.Color.parseColor(s) | 0xFF000000;
    } catch (Exception e) {
      return def;
    }
  }

  static int clamp(int v, int lo, int hi) {
    return v < lo ? lo : (v > hi ? hi : v);
  }

  /** True when dark status/navigation bar icons are needed on this background. */
  public static boolean isLight(int c) {
    int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
    return (0.299 * r + 0.587 * g + 0.114 * b) >= 160;
  }
}
