# قالب التطبيق (لواجهة «مصنع التطبيقات»)

واجهة `docs/index.html` تبني ملف APK داخل المتصفح انطلاقاً من هذا القالب الجاهز:
تطبيق WebView يقرأ إعداداته وقت التشغيل من `assets/h2a_config.json`، فلا يحتاج المتصفح إلى أي مترجم.

عند الضغط على «توليد» تقوم الصفحة بما يلي:
1. تستبدل في `AndroidManifest.xml` المعرّف والاسم والإصدار و`minSdk`/`targetSdk`، وتعطّل الأذونات غير المستعملة.
2. تغيّر في `resources.arsc` اسم الحزمة وألوان الخلفية والتمييز وخلفية الأيقونة.
3. تضع الأيقونات والكود والإعدادات، ثم ترتّب الملف (محاذاة 4 بايت) وتوقّعه بتوقيع APK v2 عبر WebCrypto.

## إعادة بناء القالب بعد تعديل كود Java

```bash
cd template
./build.sh            # يحتاج javac و aapt2 و tools/android.jar و tools/d8.jar
python3 ../scripts/embed_template.py   # يضع القالب الجديد داخل docs/index.html
```

النصوص البديلة (`com.h2a.tpl.placeholder` و`H2A_APP_LABEL_PLACEHOLDER` و`0.0.0-h2a-version-placeholder`)
والألوان البديلة (`#FF0A0B0C` و`#FF0D0E0F` و`#FF101112`) تبحث عنها الصفحة لاستبدالها، فلا تغيّرها.
