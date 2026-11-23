package com.h2a;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SplashActivity extends Activity implements Runnable {
  private boolean done;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Cfg.load(this);
    if (!Cfg.splash) {
      startActivity(new Intent(this, WebViewActivity.class));
      overridePendingTransition(0, 0);
      finish();
      return;
    }
    int bg = Cfg.splashColor;
    getWindow().getDecorView().setBackgroundColor(bg);
    getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    getWindow().setStatusBarColor(bg);
    getWindow().setNavigationBarColor(bg);
    if (Cfg.isLight(bg)) {
      int f = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
      if (android.os.Build.VERSION.SDK_INT >= 26) f |= android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
      getWindow().getDecorView().setSystemUiVisibility(f);
    }
    RelativeLayout layout = new RelativeLayout(this);
    layout.setBackgroundColor(bg);
    android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
    int screenW = Math.min(dm.widthPixels, dm.heightPixels);
    int imgSize = (int) (screenW * Cfg.splashSize / 100f);
    ImageView iv = new ImageView(this);
    Bitmap bm = Cfg.assetBitmap(this, "h2a_splash.img");
    if (bm == null) bm = Cfg.assetBitmap(this, "h2a_icon.img");
    if (bm != null) iv.setImageBitmap(bm);
    iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(imgSize, imgSize);
    lp.addRule(RelativeLayout.CENTER_IN_PARENT);
    layout.addView(iv, lp);
    setContentView(layout);
    if ("slide".equals(Cfg.splashAnim)) {
      TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.3f, Animation.RELATIVE_TO_SELF, 0f);
      ta.setDuration(500);
      AlphaAnimation fa = new AlphaAnimation(0f, 1f);
      fa.setDuration(500);
      AnimationSet as = new AnimationSet(true);
      as.addAnimation(ta);
      as.addAnimation(fa);
      iv.startAnimation(as);
    } else {
      AlphaAnimation fa = new AlphaAnimation(0f, 1f);
      fa.setDuration(600);
      iv.startAnimation(fa);
    }
    new Handler(Looper.getMainLooper()).postDelayed(this, Cfg.splashDuration);
  }

  @Override
  public void onBackPressed() {
    done = true;
    super.onBackPressed();
  }

  public void run() {
    if (done || isFinishing()) return;
    done = true;
    startActivity(new Intent(this, WebViewActivity.class));
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    finish();
  }
}
