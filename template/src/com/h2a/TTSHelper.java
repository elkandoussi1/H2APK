package com.h2a;
import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.webkit.JavascriptInterface;
import java.util.HashMap;
import java.util.Locale;

public class TTSHelper implements TextToSpeech.OnInitListener {
  private Activity activity;
  private TextToSpeech tts;
  private boolean ready = false;
  private String pendingText = null;

  public TTSHelper(Activity a) {
    this.activity = a;
    tts = new TextToSpeech(a, this);
  }

  @Override
  public void onInit(int status) {
    if (status == TextToSpeech.SUCCESS) {
      tts.setLanguage(Locale.getDefault());
      ready = true;
      if (pendingText != null) { doSpeak(pendingText); pendingText = null; }
    }
  }

  @JavascriptInterface
  public void speak(String text) {
    if (ready) { doSpeak(text); } else { pendingText = text; }
  }

  @JavascriptInterface
  public boolean isReady() { return ready; }

  @JavascriptInterface
  public void stop() { if (tts != null) tts.stop(); }

  private void doSpeak(String text) {
    if (android.os.Build.VERSION.SDK_INT >= 21) {
      tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "h2a_" + System.currentTimeMillis());
    } else {
      HashMap<String,String> params = new HashMap<>();
      params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "h2a");
      tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }
  }
}