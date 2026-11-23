package com.h2a;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import android.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
public class FileHelper implements Runnable {
  private static final String CHANNEL_ID = "h2a_downloads";
  private static final AtomicInteger notifIdCounter = new AtomicInteger(3000);
  private Activity activity;
  private volatile String pendingBase64;
  private volatile String pendingFilename;
  private volatile String pendingMime;
  public FileHelper(Activity a) { this.activity = a; }
  @JavascriptInterface
  public void saveBase64File(String base64, String filename, String mimeType) {
    pendingBase64 = base64;
    pendingFilename = filename;
    pendingMime = mimeType == null || mimeType.isEmpty() ? "application/octet-stream" : mimeType;
    new Thread(this).start();
  }
  private NotificationManager getNotifManager() {
    return (NotificationManager) activity.getSystemService(NotificationManager.class);
  }
  private void ensureChannel() {
    if (Build.VERSION.SDK_INT >= 26) {
      NotificationManager nm = getNotifManager();
      if (nm.getNotificationChannel(CHANNEL_ID) == null) {
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_LOW);
        ch.setSound(null, null);
        nm.createNotificationChannel(ch);
      }
    }
  }
  static class ToastRunner implements Runnable {
    private Activity activity;
    private String msg;
    private boolean long_;
    ToastRunner(Activity a, String m, boolean l) { activity=a; msg=m; long_=l; }
    public void run() {
      Toast.makeText(activity, msg, long_ ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }
  }
  public void run() {
    final String filename = pendingFilename;
    final String mime = pendingMime;
    final byte[] data;
    try {
      data = Base64.decode(pendingBase64, Base64.DEFAULT);
    } catch (Exception e) {
      activity.runOnUiThread(new ToastRunner(activity, "Download failed: " + e.getMessage(), true));
      return;
    }
    ensureChannel();
    final int notifId = notifIdCounter.getAndIncrement();
    final NotificationManager nm = getNotifManager();
    int iconRes = activity.getResources().getIdentifier("icon", "mipmap", activity.getPackageName());
    Notification.Builder builder;
    if (Build.VERSION.SDK_INT >= 26) {
      builder = new Notification.Builder(activity, CHANNEL_ID);
    } else {
      builder = new Notification.Builder(activity);
    }
    builder.setContentTitle(filename)
           .setContentText("Downloading...")
           .setSmallIcon(android.R.drawable.stat_sys_download)
           .setOngoing(true)
           .setProgress(100, 0, true);
    if (iconRes != 0) builder.setLargeIcon(android.graphics.BitmapFactory.decodeResource(activity.getResources(), iconRes));
    nm.notify(notifId, builder.build());
    try {
      if (Build.VERSION.SDK_INT >= 29) {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Downloads.DISPLAY_NAME, filename);
        cv.put(MediaStore.Downloads.MIME_TYPE, mime);
        cv.put(MediaStore.Downloads.IS_PENDING, 1);
        android.net.Uri uri = activity.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
        if (uri != null) {
          OutputStream os = activity.getContentResolver().openOutputStream(uri);
          if (os != null) { os.write(data); os.close(); }
          cv.clear();
          cv.put(MediaStore.Downloads.IS_PENDING, 0);
          activity.getContentResolver().update(uri, cv, null, null);
        }
      } else {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        dir.mkdirs();
        File f = new File(dir, filename);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(data);
        fos.close();
      }
      Notification.Builder doneBuilder;
      if (Build.VERSION.SDK_INT >= 26) {
        doneBuilder = new Notification.Builder(activity, CHANNEL_ID);
      } else {
        doneBuilder = new Notification.Builder(activity);
      }
      long bytes = data.length;
      String sizeStr;
      if (bytes >= 1048576) sizeStr = String.format("%.1f MB", bytes / 1048576.0);
      else if (bytes >= 1024) sizeStr = String.format("%.1f KB", bytes / 1024.0);
      else sizeStr = bytes + " B";
      doneBuilder.setContentTitle(filename)
                 .setContentText("Download complete · " + sizeStr)
                 .setSmallIcon(android.R.drawable.stat_sys_download_done)
                 .setOngoing(false)
                 .setProgress(0, 0, false)
                 .setAutoCancel(true);
      if (iconRes != 0) doneBuilder.setLargeIcon(android.graphics.BitmapFactory.decodeResource(activity.getResources(), iconRes));
      if (Build.VERSION.SDK_INT >= 21) doneBuilder.setStyle(new Notification.BigTextStyle().bigText("Download complete · " + sizeStr));
      nm.notify(notifId, doneBuilder.build());
      activity.runOnUiThread(new ToastRunner(activity, "Saved: " + filename, false));
    } catch (Exception e) {
      nm.cancel(notifId);
      activity.runOnUiThread(new ToastRunner(activity, "Save failed: " + e.getMessage(), true));
    }
  }
}