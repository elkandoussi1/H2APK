package com.h2a;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
public class PullIndicator extends View {
  private float progress;
  private float spin;
  private float extraDeg;
  private boolean loading;
  private float spinAngle;
  private Paint arcPaint, arrowPaint, cardBg;
  private RectF ringOval, cardOval;
  private Path arrowPath;
  private float R, strokeW, aTip, aInset, aWing, cardR;

  public PullIndicator(Context ctx, int accent) {
    super(ctx);
    init(accent);
  }

  private void init(int accent) {
    R = dp(9);
    strokeW = dp(2);
    aTip = dp(2.6f);
    aInset = dp(1.6f);
    aWing = dp(2);

    arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    arcPaint.setStyle(Paint.Style.STROKE);
    arcPaint.setStrokeWidth(strokeW);
    arcPaint.setStrokeCap(Paint.Cap.ROUND);
    arcPaint.setColor(0xFF222222);

    cardBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    cardBg.setStyle(Paint.Style.FILL);
    cardBg.setColor(0xFFFFFFFF);
    cardR = dp(15);

    arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    arrowPaint.setStyle(Paint.Style.FILL);
    arrowPaint.setColor(0xFF222222);

    ringOval = new RectF();
    cardOval = new RectF();
    arrowPath = new Path();

    setVisibility(View.GONE);
    setScaleX(0.6f);
    setScaleY(0.6f);
  }

  public void setPullProgress(float p) {
    progress = Math.min(p, 1f);
    setAlpha(0.4f + 0.6f * progress);
    setScaleX(0.6f + 0.4f * progress);
    setScaleY(0.6f + 0.4f * progress);
    invalidate();
  }

  public void setSpin(float s) {
    spin = s;
  }

  public void setSpinBoost(float deg) {
    extraDeg = deg;
  }

  public void setLoading(boolean l) {
    loading = l;
    if (l) {
      setAlpha(1f);
      setScaleX(1f);
      setScaleY(1f);
      spinAngle = 0;
      postInvalidateDelayed(16);
    }
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int w = getWidth(), h = getHeight();
    int cx = w / 2, cy = h / 2;

    cardOval.set(cx - cardR, cy - cardR, cx + cardR, cy + cardR);
    ringOval.set(cx - R, cy - R, cx + R, cy + R);

    canvas.drawOval(cardOval, cardBg);

    if (loading) {
      spinAngle += 5;
      if (spinAngle >= 360) spinAngle -= 360;
      int segs = 14;
      float segSweep = 12;
      for (int i = 0; i < segs; i++) {
        float segStart = spinAngle - i * (segSweep + 6);
        int alpha = 255 - i * (255 / segs);
        arcPaint.setAlpha(alpha);
        canvas.drawArc(ringOval, segStart, segSweep, false, arcPaint);
      }
      arcPaint.setAlpha(255);
      postInvalidateDelayed(16);
    } else {
      float sweep = progress * 290f;
      float startA = 125;
      float endA = startA + sweep;
      canvas.save();
      canvas.rotate(spin * 720 + extraDeg, cx, cy);
      canvas.drawArc(ringOval, startA, sweep, false, arcPaint);

      if (sweep > 0) {
        float rad = (float) Math.toRadians(endA);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        float tx = -sin;
        float ty = cos;
        float nx = cos;
        float ny = sin;

        float px = cx + R * cos;
        float py = cy + R * sin;

        float tipX = px + tx * aTip;
        float tipY = py + ty * aTip;
        float blX = px - tx * aInset + nx * aWing;
        float blY = py - ty * aInset + ny * aWing;
        float brX = px - tx * aInset - nx * aWing;
        float brY = py - ty * aInset - ny * aWing;

        arrowPath.reset();
        arrowPath.moveTo(tipX, tipY);
        arrowPath.lineTo(blX, blY);
        arrowPath.lineTo(brX, brY);
        arrowPath.close();
        canvas.drawPath(arrowPath, arrowPaint);
      }
      canvas.restore();
    }
  }

  private float dp(float px) { return px * getResources().getDisplayMetrics().density; }
}