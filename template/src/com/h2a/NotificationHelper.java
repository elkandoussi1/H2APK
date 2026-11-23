package com.h2a;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.webkit.JavascriptInterface;

public class NotificationHelper {
  private Activity activity;
  private android.graphics.Bitmap appIcon;
  private int iconResId;

  public NotificationHelper(Activity a) {
    this.activity = a;
    iconResId = a.getResources().getIdentifier("icon", "mipmap", a.getPackageName());
    if (iconResId == 0) iconResId = android.R.drawable.ic_dialog_info;
    appIcon = Cfg.assetBitmap(a, "h2a_icon.img");
    if (Build.VERSION.SDK_INT >= 26) {
      NotificationChannel ch = new NotificationChannel(
        "h2a_notifs", "Notifications", NotificationManager.IMPORTANCE_DEFAULT);
      activity.getSystemService(NotificationManager.class).createNotificationChannel(ch);
    }
  }

  @JavascriptInterface
  public void showNotification(String title, String body) {
    if (Build.VERSION.SDK_INT >= 33 &&
        activity.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
        != android.content.pm.PackageManager.PERMISSION_GRANTED) return;
    Notification.Builder b = new Notification.Builder(activity, "h2a_notifs")
      .setContentTitle(title)
      .setContentText(body)
      .setSmallIcon(iconResId)
      .setAutoCancel(true);
    if (appIcon != null) b.setLargeIcon(appIcon);
    activity.getSystemService(NotificationManager.class)
      .notify((int)(System.currentTimeMillis() % Integer.MAX_VALUE), b.build());
  }

  @JavascriptInterface
  public String getNotificationPermission() {
    if (Build.VERSION.SDK_INT >= 33) {
      return activity.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
        == android.content.pm.PackageManager.PERMISSION_GRANTED ? "granted" : "denied";
    }
    return "granted";
  }

  @JavascriptInterface
  public void requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= 33) {
      activity.requestPermissions(
        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 2001);
    }
  }
}