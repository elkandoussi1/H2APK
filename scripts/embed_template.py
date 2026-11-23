"""Replace the template APK embedded in docs/index.html with template/build/h2a-template.apk."""
import base64, pathlib, re, sys
root = pathlib.Path(__file__).resolve().parent.parent
apk = root / 'template' / 'build' / 'h2a-template.apk'
page = root / 'docs' / 'index.html'
data = base64.b64encode(apk.read_bytes()).decode()
html = page.read_text(encoding='utf-8')
new, n = re.subn(r'window\.H2_TEMPLATE = "[A-Za-z0-9+/=]*";', 'window.H2_TEMPLATE = "' + data + '";', html, count=1)
if n != 1:
    sys.exit('H2_TEMPLATE not found in docs/index.html')
page.write_text(new, encoding='utf-8')
print('embedded', apk.stat().st_size, 'bytes')
