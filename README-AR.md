# H2APK — تحويل أي كود أو موقع إلى تطبيق أندرويد

نسخة خاصة من مشروع [H2APK](https://github.com/HashShin/H2APK) لصاحبه HashShin (رخصة MIT)، بعد توقف موقعه على الإنترنت.
أُضيف إليها بناء تلقائي عبر GitHub Actions، فلا تحتاج لتثبيت أي شيء على جهازك: تضع الكود في المستودع، ويصلك ملف APK جاهز.

---

## الطريقة الأسهل: واجهة «مصنع التطبيقات»

**الرابط:** https://elkandoussi1.github.io/H2APK/

تلصق الكود أو تضع رابط موقع، وتضبط الاسم والأيقونة والألوان وشاشة البداية، ثم تضغط «توليد التطبيق». تتابع خطوات البناء مباشرة، ويظهر زر التحميل خلال دقيقة تقريباً. وفي قسم «تطبيقاتي» تجد كل تطبيقاتك السابقة للتحميل أو التعديل.

**الإعداد مرة واحدة:**

1. **تفعيل الصفحة:** في المستودع افتح **Settings** ← **Pages**، ثم في **Build and deployment** اختر **Deploy from a branch**، والفرع **main** والمجلد **/docs**، واضغط **Save**. تظهر الصفحة بعد دقيقة أو دقيقتين.
2. **رمز الوصول:** افتح [إنشاء رمز دقيق](https://github.com/settings/personal-access-tokens/new):
   - **Repository access** ← **Only select repositories** ← **H2APK** وحده.
   - **Permissions** ← **Contents**: Read and write، و**Actions**: Read-only.
   - **Generate token**، ثم الصقه في الصفحة من زر «ربط حساب GitHub».

> الرمز يبقى في متصفحك ولا يُرسل إلا إلى GitHub. فعّل «تذكّر الرمز» على جهازك الشخصي فقط.
> كيف تعمل: الصفحة ترفع الكود والإعدادات إلى `apps/<اسم>/` بإيداع واحد، فيبني GitHub Actions التطبيق، ثم تقرأ الصفحة النتيجة من Releases.

---

## الطريقة 1: تحويل كود (HTML/CSS/JS) إلى تطبيق

1. أنشئ مجلداً جديداً داخل `apps/` باسم تطبيقك بالحروف اللاتينية، مثل `apps/rabiti/`.
2. ضع فيه:

| الملف | ما هو | إلزامي؟ |
|---|---|---|
| `index.html` | الصفحة الرئيسية للتطبيق | نعم |
| `app.json` | الاسم والمعرّف والإصدار والألوان (انظر الجدول بالأسفل) | لا |
| `icon.png` | أيقونة التطبيق، يفضّل 512×512 بخلفية ممتلئة | لا |
| `splash.png` | صورة شاشة البداية | لا |
| أي ملفات أخرى | `css/`، `js/`، صور، صفحات إضافية… تُضاف بمساراتها نفسها | لا |

3. احفظ التغيير (Commit) في فرع `main`.
4. افتح تبويب **Actions**: ستجد «بناء تطبيق APK» يعمل. بعد دقيقتين تقريباً يظهر الملف في صفحة **Releases**.
5. حمّل ملف `.apk` على هاتفك وثبّته.

> كل تعديل لاحق على مجلد التطبيق يبني إصداراً جديداً تلقائياً. ارفع رقم `version` و`version_code` في `app.json` عند كل تحديث.

## الطريقة 2: تحويل موقع (رابط) إلى تطبيق

1. **Actions** ← **بناء تطبيق APK** ← **Run workflow**.
2. ضع الرابط في خانة «رابط موقع» واسم التطبيق في خانته.
3. **Run workflow**، ثم حمّل الملف من **Releases**.

يُفعَّل تلقائياً في وضع الرابط: حجب الإعلانات والسحب للتحديث.

## الطريقة 3: إعادة بناء تطبيق موجود

**Actions** ← **بناء تطبيق APK** ← **Run workflow**، واكتب اسم المجلد (مثل `myapp`) واترك خانة الرابط فارغة.

---

## إعدادات `app.json`

```json
{
  "app_name": "تطبيقي",
  "package_name": "com.elkandoussi.myapp",
  "version": "1.0",
  "version_code": 1,
  "theme_color": "#110D1D",
  "splash_enabled": true,
  "splash_color": "#110D1D",
  "splash_duration": 1200,
  "splash_image_size": 42,
  "splash_animation": "fade",
  "hide_scrollbars": true,
  "zoom_enabled": false,
  "pull_refresh": false,
  "disable_copy_text": false
}
```

| المفتاح | المعنى |
|---|---|
| `app_name` | الاسم الظاهر تحت الأيقونة |
| `package_name` | المعرّف الفريد للتطبيق؛ لا تغيّره بعد النشر وإلا صار تطبيقاً آخر |
| `version` / `version_code` | رقم الإصدار النصي والرقم الصحيح (يجب أن يزيد عند كل تحديث) |
| `theme_color` | لون شريط الحالة |
| `splash_*` | شاشة البداية: تفعيلها، لون الخلفية، المدة بالملّي ثانية، حجم الصورة بالنسبة المئوية، الحركة (`fade` أو `slide`) |
| `hide_scrollbars` · `zoom_enabled` · `pull_refresh` · `disable_copy_text` | إخفاء أشرطة التمرير · التكبير باللمس · السحب للتحديث · منع النسخ |
| `transparent_nav` | شريط تنقّل شفاف |
| `min_sdk` / `target_sdk` | أقل وأعلى إصدار أندرويد (الافتراضي 21 و34) |
| `html_file` | اسم الصفحة الرئيسية إن لم تكن `index.html` |

الكاميرا والميكروفون والإشعارات تُطلب تلقائياً إن وُجد استعمالها في الكود.

---

## التوقيع: مفتاح التجربة أو مفتاحك الخاص

دون إعداد إضافي، تُوقَّع التطبيقات **بمفتاح التجربة** المضمَّن في المشروع. هذا يكفي للتجربة والاستعمال الشخصي، لكنه مفتاح عام يملكه الجميع.

**لتطبيقات حساسة (مثل مدير كلمات المرور) أو للنشر في متجر Play، استعمل مفتاحاً خاصاً بك:**

1. أنشئ المفتاح مرة واحدة على جهازك (يتطلب Java):
   ```bash
   keytool -genkeypair -keystore elkandoussi.jks -alias elkandoussi -keyalg RSA -keysize 2048 -validity 10000
   base64 -w0 elkandoussi.jks > keystore.txt
   ```
2. في المستودع: **Settings** ← **Secrets and variables** ← **Actions** ← **New repository secret**، وأضف:
   - `KEYSTORE_BASE64`: محتوى `keystore.txt`
   - `KEYSTORE_PASSWORD`: كلمة مرور المفتاح
   - `KEY_ALIAS`: `elkandoussi`
   - `KEY_PASSWORD`: كلمة مرور المفتاح (نفسها غالباً)
3. من الآن، كل بناء ينتج ملف `.aab` لمتجر Play وملف `-release.apk` موقّعاً بمفتاحك.

> احتفظ بملف `elkandoussi.jks` وكلمة مروره في مكان آمن، ولا ترفعه إلى المستودع أبداً. ضياعه يعني عدم القدرة على تحديث تطبيقاتك.
> تغيير المفتاح لتطبيق مثبّت يتطلب حذفه وإعادة تثبيته، فصدّر نسخة احتياطية من بياناتك أولاً.

---

## التشغيل على جهازك بواجهة الموقع الأصلية

على Linux أو WSL في ويندوز أو Termux في الهاتف:

```bash
git clone --depth=1 https://github.com/elkandoussi1/H2APK
cd H2APK
./setup.sh
go build -o h2apk main.go
./h2apk
```

ثم افتح `http://localhost:8080`.

إن لم يتوفر `aapt2` أو `zipalign` على جهازك:

```bash
./scripts/get-aapt2.sh                       # يستخرج aapt2 من tools/bundletool.jar
cp scripts/zipalign.py tools/bin/zipalign    # بديل بسيط لـ zipalign
export PATH="$PWD/tools/bin:$PATH"
```

ويمكن البناء من سطر الأوامر دون الواجهة (والخادم يعمل):

```bash
python3 scripts/ci_build.py apps/myapp
python3 scripts/ci_build.py --url https://elkandoussi.com --name "القندوسي"
```

الملفات الناتجة في مجلد `dist/`.

---

## ملاحظات عن التطبيقات الناتجة

- الصفحة تُحمَّل من داخل التطبيق، فتعمل دون إنترنت ما لم تطلب ملفات من الخارج (مثل خطوط Google).
- `localStorage` يعمل ويُحفظ داخل التطبيق. حذف التطبيق يحذف بياناته، فاعتمد التصدير والنسخ الاحتياطي.
- تنزيل الملفات (روابط `download`) واختيار الملفات (`<input type="file">`) والحافظة كلها مدعومة عبر جسور المشروع.
