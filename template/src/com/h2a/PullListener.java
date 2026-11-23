package com.h2a;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
public class PullListener implements View.OnTouchListener, PaddingClient.PullCallback, Runnable {
  private WebView wv;
  private PullIndicator indicator;
  private float startY;
  private float pullDist;
  private boolean dragging;
  private boolean loading;
  private float indicatorH;
  private float threshold;
  private float maxSlide;
  private Handler handler;
  private Runnable forceHide;

  public PullListener(WebView wv, PullIndicator indicator) {
    this.wv = wv;
    this.indicator = indicator;
    float d = indicator.getContext().getResources().getDisplayMetrics().density;
    this.indicatorH = 56 * d;
    this.threshold = 115 * d;
    this.maxSlide = indicatorH * 2;
    this.handler = new Handler(Looper.getMainLooper());
    this.forceHide = this;
    indicator.setTranslationY(-this.indicatorH);
  }

  @Override
  public void run() {
    loading = false;
    indicator.setVisibility(View.GONE);
    indicator.setTranslationY(-indicatorH);
    indicator.setLoading(false);
    indicator.setPullProgress(0);
    indicator.setSpin(0);
    indicator.setSpinBoost(0);
  }

  @Override
  public void onPageFinished() {
    handler.removeCallbacks(forceHide);
    loading = false;
    indicator.setVisibility(View.GONE);
    indicator.setTranslationY(-indicatorH);
    indicator.setLoading(false);
    indicator.setPullProgress(0);
    indicator.setSpin(0);
    indicator.setSpinBoost(0);
  }

  @Override
  public boolean onTouch(View v, MotionEvent e) {
    if (e.getPointerCount() > 1) { dragging = false; return false; }
    switch (e.getAction()) {
      case MotionEvent.ACTION_DOWN:
        if (!loading) {
          startY = e.getY();
          pullDist = 0;
          dragging = true;
        }
        break;
      case MotionEvent.ACTION_MOVE:
        if (!dragging || loading) break;
        if (wv.getScrollY() > 0) { dragging = false; break; }
        pullDist = e.getY() - startY;
        if (pullDist <= 0) break;
        wv.evaluateJavascript("document.body.style.webkitUserSelect='none';document.body.style.userSelect='none'", null);
        indicator.setVisibility(View.VISIBLE);
        float resisted = Math.min((float) Math.pow(pullDist, 0.85), maxSlide);
        indicator.setTranslationY(resisted - indicatorH);
        indicator.setPullProgress(pullDist / threshold);
        indicator.setSpin(resisted / maxSlide);
        float maxPull = (float) Math.pow(maxSlide, 1.0 / 0.85);
        float over = Math.max(0, pullDist - maxPull);
        indicator.setSpinBoost(Math.min(over * 0.5f, 45f));
        return true;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        dragging = false;
        wv.evaluateJavascript("document.body.style.webkitUserSelect='';document.body.style.userSelect=''", null);
        if (loading) break;
        if (pullDist >= threshold) {
          loading = true;
          indicator.setTranslationY(0);
          indicator.setLoading(true);
          handler.postDelayed(forceHide, 10000);
          wv.reload();
          return true;
        }
        indicator.setVisibility(View.GONE);
        indicator.setTranslationY(-indicatorH);
        indicator.setPullProgress(0);
        indicator.setSpin(0);
        indicator.setSpinBoost(0);
        break;
    }
    return false;
  }
}