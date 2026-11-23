package com.h2a;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.webkit.PermissionRequest;

public class H2AChromeClient extends WebChromeClient implements android.content.DialogInterface.OnClickListener {
  private View customView;
  private CustomViewCallback callback;
  private FrameLayout container;
  private WebView webView;
  private PermissionRequest pendingPermission;
  private WebViewActivity activity;
  private boolean cameraEnabled = true;
  private boolean micEnabled = true;
  private static final int REQ_CAMERA = 1001;
  private static final int REQ_MIC = 1002;
  private static final int REQ_GEO = 1003;
  private int themeColor = (int)4279383126L;
  private String pendingGeoOrigin;
  private android.webkit.GeolocationPermissions.Callback pendingGeoCallback;
  private android.webkit.JsResult pendingJsResult;
  private android.webkit.JsPromptResult pendingPromptResult;
  private android.widget.EditText promptInput;

  public H2AChromeClient(FrameLayout container, WebView webView, int themeColor) {
    this.container = container;
    this.webView = webView;
    this.activity = (WebViewActivity) webView.getContext();
    this.themeColor = themeColor;
  }

  @Override
  public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
    return false;
  }

  @Override
  public void onShowCustomView(View view, CustomViewCallback callback) {
    if (this.customView != null) {
      callback.onCustomViewHidden();
      return;
    }
    this.customView = view;
    this.callback = callback;
    webView.setVisibility(View.GONE);
    container.addView(view, new FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT
    ));
  }

  @Override
  public void onHideCustomView() {
    if (customView == null) return;
    webView.setVisibility(View.VISIBLE);
    container.removeView(customView);
    customView = null;
    if (callback != null) {
      callback.onCustomViewHidden();
      callback = null;
    }
  }

  public boolean dismissCustomView() {
    if (customView == null) return false;
    onHideCustomView();
    return true;
  }

  @Override
  public void onPermissionRequest(PermissionRequest request) {
    boolean needCam = false, needMic = false;
    for (String r : request.getResources()) {
      if (r.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) needCam = true;
      if (r.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) needMic = true;
    }
    boolean camOK = true, micOK = true;
    if (android.os.Build.VERSION.SDK_INT >= 23) {
      if (needCam && cameraEnabled) {
        camOK = activity.checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED;
      }
      if (needMic && micEnabled) {
        micOK = activity.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED;
      }
    }
    if (camOK && micOK) {
      request.grant(request.getResources());
      return;
    }
    pendingPermission = request;
    if (android.os.Build.VERSION.SDK_INT >= 23) {
      if (needCam && !camOK) activity.reRequestPermission(android.Manifest.permission.CAMERA, REQ_CAMERA);
      if (needMic && !micOK) activity.reRequestPermission(android.Manifest.permission.RECORD_AUDIO, REQ_MIC);
    } else {
      request.deny();
      pendingPermission = null;
    }
  }

  public void onPermissionResult(int requestCode, boolean granted) {
    if (pendingPermission == null) return;
    if (granted) {
      pendingPermission.grant(pendingPermission.getResources());
    } else {
      pendingPermission.deny();
    }
    pendingPermission = null;
  }

  @Override
  public boolean onShowFileChooser(WebView webView,
      android.webkit.ValueCallback<android.net.Uri[]> filePathCallback,
      WebChromeClient.FileChooserParams fileChooserParams) {
    String[] accept = null;
    try { accept = fileChooserParams.getAcceptTypes(); } catch (Exception e) {}
    if (activity == null) { try { activity = (WebViewActivity) webView.getContext(); } catch (Exception e) {} }
    if (activity == null) { filePathCallback.onReceiveValue(null); return true; }
    activity.launchFileChooser(filePathCallback, accept);
    return true;
  }

  @Override
  public void onClick(android.content.DialogInterface dialog, int which) {
    if (which == android.content.DialogInterface.BUTTON_POSITIVE) {
      if (pendingPromptResult != null) {
        String val = promptInput != null ? promptInput.getText().toString() : "";
        pendingPromptResult.confirm(val);
      } else if (pendingJsResult != null) {
        pendingJsResult.confirm();
      }
    } else {
      if (pendingPromptResult != null) pendingPromptResult.cancel();
      else if (pendingJsResult != null) pendingJsResult.cancel();
    }
    pendingJsResult = null;
    pendingPromptResult = null;
    promptInput = null;
  }

  private android.app.AlertDialog buildDialog(android.content.Context ctx, String title, String msg) {
    android.view.ContextThemeWrapper themed = new android.view.ContextThemeWrapper(ctx, android.R.style.Theme_Material_Dialog);
    android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(themed);
    if (title != null && title.length() > 0) b.setTitle(title);
    if (msg != null) b.setMessage(msg);
    b.setCancelable(false);
    return b.create();
  }

  @Override
  public boolean onJsAlert(WebView view, String url, String message, android.webkit.JsResult result) {
    pendingJsResult = result;
    android.app.AlertDialog d = buildDialog(view.getContext(), null, message);
    d.setButton(android.content.DialogInterface.BUTTON_POSITIVE, "OK", this);
    d.show();
    d.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setTextColor(themeColor);
    return true;
  }

  @Override
  public boolean onJsConfirm(WebView view, String url, String message, android.webkit.JsResult result) {
    pendingJsResult = result;
    android.app.AlertDialog d = buildDialog(view.getContext(), null, message);
    d.setButton(android.content.DialogInterface.BUTTON_POSITIVE, "OK", this);
    d.setButton(android.content.DialogInterface.BUTTON_NEGATIVE, "Cancel", this);
    d.show();
    d.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setTextColor(themeColor);
    d.getButton(android.content.DialogInterface.BUTTON_NEGATIVE).setTextColor(themeColor);
    return true;
  }

  @Override
  public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, android.webkit.JsPromptResult result) {
    pendingPromptResult = result;
    android.widget.EditText input = new android.widget.EditText(view.getContext());
    input.setText(defaultValue != null ? defaultValue : "");
    promptInput = input;
    android.app.AlertDialog d = buildDialog(view.getContext(), message, null);
    d.setView(input);
    d.setButton(android.content.DialogInterface.BUTTON_POSITIVE, "OK", this);
    d.setButton(android.content.DialogInterface.BUTTON_NEGATIVE, "Cancel", this);
    d.show();
    d.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setTextColor(themeColor);
    d.getButton(android.content.DialogInterface.BUTTON_NEGATIVE).setTextColor(themeColor);
    return true;
  }

  @Override
  public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
    if (android.os.Build.VERSION.SDK_INT >= 23 &&
        activity.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
          != android.content.pm.PackageManager.PERMISSION_GRANTED) {
      pendingGeoOrigin = origin;
      pendingGeoCallback = callback;
      activity.reRequestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, REQ_GEO);
    } else {
      callback.invoke(origin, true, false);
    }
  }

  public void onGeoPermissionResult(boolean granted) {
    if (pendingGeoCallback != null) {
      pendingGeoCallback.invoke(pendingGeoOrigin, granted, false);
      pendingGeoCallback = null;
      pendingGeoOrigin = null;
    }
  }
}