#!/usr/bin/env python3
"""
يبني تطبيقات APK عبر خادم H2APK المحلي.
يُستعمل تلقائياً في GitHub Actions، ويمكن تشغيله على الجهاز أيضاً:

    ./h2apk &                                   # شغّل الخادم أولاً
    python3 scripts/ci_build.py apps/khaznati   # مجلد تطبيق
    python3 scripts/ci_build.py --url https://example.com --name "اسم التطبيق"

كل مجلد داخل apps/ تطبيق مستقل:
    index.html   الصفحة الرئيسية (إلزامي)
    app.json     الإعدادات (اختياري): الاسم، المعرّف، الإصدار، الألوان، شاشة البداية…
                 وإذا احتوى على "url" صار التطبيق غلافاً لذلك الموقع ولا يلزم index.html
    icon.png     أيقونة التطبيق (اختياري، يفضّل 512×512)
    splash.png   صورة شاشة البداية (اختياري)
    أي ملفات أخرى (css، js، صور، صفحات) تُضاف إلى التطبيق بمساراتها نفسها.

التوقيع: إذا وُجدت الأسرار KEYSTORE_BASE64 وKEYSTORE_PASSWORD وKEY_ALIAS وKEY_PASSWORD
يُبنى التطبيق بوضع الإصدار (AAB لمتجر Play + APK موقّع بمفتاحك)، وإلا بمفتاح التجربة.
"""
import argparse, base64, json, os, pathlib, re, subprocess, sys, time, urllib.parse, urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
APPS = ROOT / 'apps'
DIST = ROOT / 'dist'
HOST = os.environ.get('H2APK_HOST', 'http://localhost:' + os.environ.get('PORT', '8011'))
SPECIAL = {'app.json', 'icon.png', 'splash.png'}
DEFAULTS = {
    'version': '1.0', 'version_code': 1, 'theme_color': '#110D1D',
    'hide_scrollbars': True, 'zoom_enabled': False, 'pull_refresh': False,
}


def b64(path):
    return base64.b64encode(pathlib.Path(path).read_bytes()).decode()


def slug(text, fallback='app'):
    s = re.sub(r'[^a-z0-9]', '', (text or '').lower())
    return (s if s and s[0].isalpha() else 'a' + s) if s else fallback


def wait_for_server():
    for _ in range(240):
        try:
            urllib.request.urlopen(HOST + '/', timeout=2)
            return
        except Exception:
            time.sleep(0.5)
    sys.exit('✗ خادم H2APK لا يستجيب على ' + HOST)


def apply_signing(req):
    ks = os.environ.get('KEYSTORE_BASE64', '').strip()
    if not ks:
        req.setdefault('build_mode', 'debug')
        return
    req.update({
        'build_mode': 'release',
        'keystore': ks,
        'ks_pass': os.environ.get('KEYSTORE_PASSWORD', ''),
        'key_alias': os.environ.get('KEY_ALIAS', ''),
        'key_pass': os.environ.get('KEY_PASSWORD', '') or os.environ.get('KEYSTORE_PASSWORD', ''),
    })


def request_for_dir(app_dir):
    app_dir = pathlib.Path(app_dir).resolve()
    cfg = {}
    if (app_dir / 'app.json').exists():
        cfg = json.loads((app_dir / 'app.json').read_text(encoding='utf-8'))
    main_html = app_dir / cfg.pop('html_file', 'index.html')
    is_url = bool(str(cfg.get('url', '')).strip())
    if not is_url and not main_html.exists():
        raise SystemExit(f'✗ لا يوجد {main_html.name} ولا رابط url في {app_dir}')
    req = dict(DEFAULTS)
    req['app_name'] = app_dir.name
    req['package_name'] = 'com.elkandoussi.' + slug(app_dir.name)
    if is_url:
        req.update({'block_ads': True, 'pull_refresh': True})
    req.update(cfg)
    if is_url:
        req['url'] = str(cfg['url']).strip()
        req.pop('html', None)
    else:
        req['html'] = main_html.read_text(encoding='utf-8')
    if (app_dir / 'icon.png').exists():
        req['icon'] = b64(app_dir / 'icon.png')
    if req.get('splash_enabled') and (app_dir / 'splash.png').exists() and not req.get('splash_use_icon'):
        req['splash_image'] = b64(app_dir / 'splash.png')
    assets = {}
    for f in ([] if is_url else sorted(app_dir.rglob('*'))):
        rel = f.relative_to(app_dir).as_posix()
        if f.is_file() and f != main_html and rel not in SPECIAL and not rel.startswith('.'):
            assets[rel] = b64(f)
    if assets:
        req['asset_files'] = assets
    apply_signing(req)
    return req


def request_for_url(url, name):
    host = urllib.parse.urlparse(url).hostname or 'site'
    req = dict(DEFAULTS)
    req.update({
        'app_name': name or host,
        'package_name': 'com.elkandoussi.web.' + slug(host.replace('www.', '')),
        'url': url,
        'block_ads': True,
        'pull_refresh': True,
    })
    apply_signing(req)
    return req


def build(req):
    label = req['app_name']
    print(f'→ بناء «{label}» ({req["package_name"]}) بوضع {req["build_mode"]}…', flush=True)
    body = json.dumps(req).encode()
    resp = json.load(urllib.request.urlopen(urllib.request.Request(
        HOST + '/api/build', body, {'Content-Type': 'application/json'}), timeout=60))
    bid = resp.get('build_id')
    if not bid:
        raise SystemExit('✗ رفض الخادم الطلب: ' + json.dumps(resp, ensure_ascii=False))
    status = {}
    for _ in range(1200):
        status = json.load(urllib.request.urlopen(HOST + '/api/status/' + bid, timeout=30))
        if status.get('error') or status.get('apk_name'):
            break
        time.sleep(0.5)
    if status.get('error') or not status.get('apk_name'):
        print(status.get('log', '')[-4000:])
        raise SystemExit('✗ فشل البناء: ' + str(status.get('error', 'انتهت المهلة')))
    DIST.mkdir(exist_ok=True)
    version = str(req.get('version', '1.0'))
    base = f'{slug(req["package_name"].split(".")[-1])}-v{version}'
    saved = []
    for art in status.get('artifacts') or [{'name': status['apk_name'], 'kind': 'apk'}]:
        url = f'{HOST}/api/download-file/{bid}/{urllib.parse.quote(art["name"])}'
        suffix = '-release' if art['name'].endswith('-release.apk') else ''
        target = DIST / f'{base}{suffix}.{art["kind"]}'
        target.write_bytes(urllib.request.urlopen(url, timeout=120).read())
        saved.append(target)
        print(f'  ✓ {target.relative_to(ROOT)} ({target.stat().st_size // 1024} KB)', flush=True)
    return label, version, saved


def changed_app_dirs(before):
    every = sorted(p.parent for p in APPS.glob('*/index.html'))
    every += sorted(p.parent for p in APPS.glob('*/app.json') if p.parent not in every)
    if not before or set(before) == {'0'}:
        return every
    if subprocess.run(['git', 'cat-file', '-e', before + '^{commit}'], cwd=ROOT).returncode != 0:
        return every
    out = subprocess.run(['git', 'diff', '--name-only', before, 'HEAD', '--', 'apps'],
                         cwd=ROOT, capture_output=True, text=True).stdout.split()
    names = {pathlib.PurePosixPath(p).parts[1] for p in out if len(pathlib.PurePosixPath(p).parts) > 2}
    return [d for d in every if d.name in names]


def main():
    ap = argparse.ArgumentParser(description='H2APK — بناء تطبيقات من مجلدات apps/ أو من رابط')
    ap.add_argument('dirs', nargs='*', help='مجلدات التطبيقات (افتراضياً حسب حدث GitHub)')
    ap.add_argument('--url', default=os.environ.get('IN_URL', ''))
    ap.add_argument('--name', default=os.environ.get('IN_NAME', ''))
    a = ap.parse_args()

    if a.url:
        jobs = [request_for_url(a.url.strip(), a.name.strip())]
    elif a.dirs:
        jobs = [request_for_dir(d) for d in a.dirs]
    elif os.environ.get('IN_APP', '').strip():
        jobs = [request_for_dir(APPS / os.environ['IN_APP'].strip())]
    else:
        dirs = changed_app_dirs(os.environ.get('BEFORE', ''))
        if not dirs:
            print('لا توجد تطبيقات متغيّرة لبنائها.')
            return
        jobs = [request_for_dir(d) for d in dirs]

    wait_for_server()
    results = [build(r) for r in jobs]

    DIST.mkdir(exist_ok=True)
    title = ' · '.join(f'{n} {v}' for n, v, _ in results)
    lines = ['## التطبيقات', '']
    for n, v, files in results:
        lines.append(f'- **{n}** — الإصدار {v}: ' + '، '.join(f'`{f.name}`' for f in files))
    lines += ['', '## التثبيت على الهاتف', '',
              '1. حمّل ملف `.apk` من الأسفل على هاتف أندرويد.',
              '2. افتحه، واسمح بالتثبيت من هذا المصدر إذا طلب الهاتف ذلك.',
              '3. التحديث يحافظ على البيانات ما دام التوقيع نفسه.']
    (DIST / 'title.txt').write_text(title, encoding='utf-8')
    (DIST / 'notes.md').write_text('\n'.join(lines) + '\n', encoding='utf-8')


if __name__ == '__main__':
    main()
