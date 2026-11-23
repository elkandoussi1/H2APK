package com.h2a;
import android.app.Activity;
import android.webkit.JavascriptInterface;

public class ShareHelper {
  private Activity activity;
  public ShareHelper(Activity a){ this.activity = a; }

  @JavascriptInterface
  public void share(String title, String text, String url) {
    StringBuilder sb = new StringBuilder();
    if (text != null && text.length() > 0) sb.append(text);
    if (url != null && url.length() > 0) { if (sb.length()>0) sb.append("\n"); sb.append(url); }
    android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_SEND);
    i.setType("text/plain");
    if (title != null && title.length() > 0) i.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
    i.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
    android.content.Intent chooser = android.content.Intent.createChooser(i, title != null ? title : "Share");
    chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
    activity.startActivity(chooser);
  }
}