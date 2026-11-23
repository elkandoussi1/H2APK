package com.h2a;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.webkit.JavascriptInterface;
public class ClipboardHelper implements Runnable {
  private Activity activity;
  private volatile String pendingWrite;
  private volatile String readResult;
  private final Object lock = new Object();
  private volatile boolean done;
  private int mode;
  public ClipboardHelper(Activity a) { this.activity = a; }
  @JavascriptInterface
  public String readText() {
    synchronized (lock) {
      mode = 0; done = false; readResult = "";
      activity.runOnUiThread(this);
      long start = System.currentTimeMillis();
      while (!done && System.currentTimeMillis() - start < 1000) {
        try { lock.wait(1000); } catch (InterruptedException e) { break; }
      }
      return readResult;
    }
  }
  @JavascriptInterface
  public void writeText(String text) {
    synchronized (lock) { mode = 1; pendingWrite = text == null ? "" : text; }
    activity.runOnUiThread(this);
  }
  public void run() {
    ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    if (mode == 0) {
      String r = "";
      try {
        if (cm != null && cm.hasPrimaryClip() && cm.getPrimaryClip() != null
            && cm.getPrimaryClip().getItemCount() > 0) {
          CharSequence cs = cm.getPrimaryClip().getItemAt(0).coerceToText(activity);
          r = cs == null ? "" : cs.toString();
        }
      } catch (Exception e) {}
      synchronized (lock) { readResult = r; done = true; lock.notifyAll(); }
    } else {
      try { if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("text", pendingWrite)); } catch (Exception e) {}
    }
  }
}