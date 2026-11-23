package com.h2a;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.app.DownloadManager;
import android.net.Uri;
import android.content.Context;
import android.widget.Toast;
import android.widget.TextView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.content.res.Configuration;
import android.util.Log;
import android.webkit.ValueCallback;
import android.Manifest;
import android.content.pm.PackageManager;
public class WebViewActivity extends Activity implements DownloadListener, android.view.View.OnLongClickListener, android.view.View.OnApplyWindowInsetsListener {
  private WebView wv;
  private H2AChromeClient chromeClient;
  private ValueCallback<Uri[]> filePathCallback;
  private static final int H2A_FILE_CHOOSER_REQ = 3001;
  private PullIndicator pullIndicator;
  private boolean edgeToEdge;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Cfg.load(this);
    final int theme = Cfg.theme;
    edgeToEdge = android.os.Build.VERSION.SDK_INT >= 35 && getApplicationInfo().targetSdkVersion >= 35;
    getWindow().getDecorView().setBackgroundColor(theme);
    if (Cfg.debug) { try { WebView.setWebContentsDebuggingEnabled(true); } catch (Exception ignored) {} }
    getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    getWindow().setStatusBarColor(theme);
    if (Cfg.transparentNav) {
      getWindow().setNavigationBarColor(0);
      if (android.os.Build.VERSION.SDK_INT >= 29) { getWindow().setNavigationBarContrastEnforced(false); }
    } else {
      getWindow().setNavigationBarColor(theme);
    }
    wv = new WebView(this);
    wv.setBackgroundColor(theme);
    wv.setVerticalScrollBarEnabled(!Cfg.hideScrollbars);
    wv.setHorizontalScrollBarEnabled(!Cfg.hideScrollbars);
    WebSettings ws = wv.getSettings();
    ws.setJavaScriptEnabled(true);
    ws.setDomStorageEnabled(true);
    ws.setGeolocationEnabled(Cfg.geo);
    ws.setSupportMultipleWindows(false);
    if (Cfg.media) ws.setMediaPlaybackRequiresUserGesture(false);
    if (Cfg.zoom) {
      ws.setUseWideViewPort(true);
      ws.setLoadWithOverviewMode(true);
      ws.setSupportZoom(true);
      ws.setBuiltInZoomControls(true);
      ws.setDisplayZoomControls(false);
    }
    if (Cfg.blockAds) {
      android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(wv, false);
      ws.setSavePassword(false);
    }
    FrameLayout fl = new FrameLayout(this);
    chromeClient = new H2AChromeClient(fl, wv, theme);
    wv.setWebChromeClient(chromeClient);
    wv.setDownloadListener(this);
    wv.addJavascriptInterface(new ClipboardHelper(this), "H2AClip");
    wv.addJavascriptInterface(new FileHelper(this), "H2AFile");
    wv.addJavascriptInterface(new ShareHelper(this), "H2AShare");
    wv.addJavascriptInterface(new TTSHelper(this), "H2ATTS");
    if (Cfg.notif) wv.addJavascriptInterface(new NotificationHelper(this), "H2A");
    fl.setBackgroundColor(theme);
    fl.addView(wv);
    if (Cfg.pull) {
      pullIndicator = new PullIndicator(this, theme & 0xFFFFFF);
      int size = (int)(56 * getResources().getDisplayMetrics().density);
      FrameLayout.LayoutParams ilp = new FrameLayout.LayoutParams(size, size);
      ilp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
      ilp.topMargin = 0;
      pullIndicator.setLayoutParams(ilp);
      fl.addView(pullIndicator);
      PullListener pl = new PullListener(wv, pullIndicator);
      wv.setWebViewClient(new PaddingClient(pl, Cfg.blockAds, Cfg.adguard));
      wv.setOnTouchListener(pl);
    } else {
      wv.setWebViewClient(new PaddingClient(Cfg.blockAds, Cfg.adguard));
    }
    wv.loadUrl(Cfg.url);
    if (Cfg.notif && android.os.Build.VERSION.SDK_INT >= 33) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 2001);
      }
    }
    wv.setOnLongClickListener(this);
    if (edgeToEdge) {
      fl.setOnApplyWindowInsetsListener(this);
    } else if (Cfg.transparentNav) {
      int h2aSbh = 0; int h2aSbId = getResources().getIdentifier("status_bar_height", "dimen", "android"); if (h2aSbId > 0) h2aSbh = getResources().getDimensionPixelSize(h2aSbId); fl.setPadding(0, h2aSbh, 0, 0);
    }
    setContentView(fl);
    applySystemUI();
  }

  @Override
  public android.view.WindowInsets onApplyWindowInsets(android.view.View v, android.view.WindowInsets insets) {
    android.graphics.Insets b = insets.getInsets(android.view.WindowInsets.Type.systemBars() | android.view.WindowInsets.Type.displayCutout());
    boolean land = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    v.setPadding(land ? 0 : b.left, land ? 0 : b.top, land ? 0 : b.right, (land || Cfg.transparentNav) ? 0 : b.bottom);
    return insets;
  }

  private void applySystemUI() {
    int flags = android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
    if (Cfg.transparentNav) flags |= android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
    if (Cfg.isLight(Cfg.theme)) {
      flags |= android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
      if (!Cfg.transparentNav && android.os.Build.VERSION.SDK_INT >= 26) flags |= android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
    }
    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      flags |= android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
             | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
             | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
             | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
             | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }
    getWindow().getDecorView().setSystemUiVisibility(flags);
    if (edgeToEdge) getWindow().getDecorView().requestApplyInsets();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    applySystemUI();
  }

  @Override
  public void onBackPressed() {
    if (chromeClient != null && chromeClient.dismissCustomView()) return;
    if (wv.canGoBack()) {
      wv.goBack();
      return;
    }
    super.onBackPressed();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
    if (url != null && url.startsWith("blob:")) {
      final String safeFilename = URLUtil.guessFileName(url, contentDisposition, mimeType).replace("'", "\\'");
      final String safeMime = (mimeType == null ? "" : mimeType).replace("'", "\\'");
      final String safeUrl = url.replace("\\", "\\\\").replace("'", "\\'");
      wv.evaluateJavascript(
        "(function(){" +
        "fetch('" + safeUrl + "')" +
        ".then(function(r){return r.blob();})" +
        ".then(function(b){" +
        "var fr=new FileReader();" +
        "fr.onload=function(){" +
        "var b64=fr.result.indexOf(',')>=0?fr.result.split(',')[1]:fr.result;" +
        "var mt=b.type||'" + safeMime + "';" +
        "if(window.H2AFile)H2AFile.saveBase64File(b64,'" + safeFilename + "',mt);" +
        "};" +
        "fr.readAsDataURL(b);" +
        "}).catch(function(e){console.error('blob dl',e);});" +
        "})()", null);
      return;
    }
    try {
      DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
      req.setMimeType(mimeType);
      req.addRequestHeader("User-Agent", userAgent);
      req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
      String name = URLUtil.guessFileName(url, contentDisposition, mimeType);
      req.setTitle(name);
      req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
      DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
      if (dm != null) dm.enqueue(req);
    } catch (Exception e) {
      Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }
  @Override
  public boolean onLongClick(android.view.View v) {
    if (Cfg.disableCopy) return true;
    WebView.HitTestResult r = ((WebView) v).getHitTestResult();
    return r != null && (r.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE || r.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE);
  }
  
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] perms, int[] grantResults) {
    boolean g = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    if (requestCode == 1003) {
      if (chromeClient != null) chromeClient.onGeoPermissionResult(g);
    } else if (requestCode == 1001) {
      if (chromeClient != null) chromeClient.onPermissionResult(requestCode, g);
    } else if (requestCode == 1002) {
      if (chromeClient != null) chromeClient.onPermissionResult(requestCode, g);
    }
  }

  public void reRequestPermission(String perm, int code) {
    requestPermissions(new String[]{perm}, code);
  }
  
  private String extToMime(String ext) {
    switch (ext.toLowerCase()) {
      case ".txt": return "text/plain";
      case ".pdf": return "application/pdf";
      case ".jpg": case ".jpeg": return "image/jpeg";
      case ".png": return "image/png";
      case ".gif": return "image/gif";
      case ".webp": return "image/webp";
      case ".mp4": return "video/mp4";
      case ".mp3": return "audio/mpeg";
      case ".json": return "application/json";
      case ".csv": return "text/csv";
      case ".zip": return "application/zip";
      case ".doc": return "application/msword";
      case ".docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
      case ".xls": return "application/vnd.ms-excel";
      case ".xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
      case ".htm": case ".html": return "text/html";
      default: return "*/*";
    }
  }

  public void launchFileChooser(android.webkit.ValueCallback<android.net.Uri[]> cb, String[] acceptTypes) {
    if (filePathCallback != null) { filePathCallback.onReceiveValue(null); }
    filePathCallback = cb;
    String mime = "*/*";
    if (acceptTypes != null && acceptTypes.length > 0) {
      for (String a : acceptTypes) {
        if (a == null) continue;
        String t = a.trim();
        if (t.length() == 0) continue;
        if (t.startsWith(".")) { mime = extToMime(t); break; }
        if (t.indexOf('/') >= 0 && t.indexOf(',') < 0) { mime = t; break; }
      }
    }
    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
    intent.setType(mime);
    intent.putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, true);
    try {
      startActivityForResult(intent, H2A_FILE_CHOOSER_REQ);
    } catch (Exception e) {
      if (filePathCallback != null) { filePathCallback.onReceiveValue(null); filePathCallback = null; }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode != H2A_FILE_CHOOSER_REQ) return;
    if (filePathCallback == null) return;
    android.net.Uri[] results = null;
    if (resultCode == Activity.RESULT_OK && data != null) {
      if (data.getClipData() != null) {
        int n = data.getClipData().getItemCount();
        results = new android.net.Uri[n];
        for (int i = 0; i < n; i++) results[i] = data.getClipData().getItemAt(i).getUri();
      } else if (data.getData() != null) {
        results = new android.net.Uri[]{ data.getData() };
      }
    }
    filePathCallback.onReceiveValue(results);
    filePathCallback = null;
  }
}