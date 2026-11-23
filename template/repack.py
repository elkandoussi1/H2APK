"""Repack the aapt2 output + classes.dex into the template zip the browser builder reads.
Entries the builder patches or replaces (manifest, resources.arsc, PNGs) are STORED so the
browser can slice them without inflating; everything else is deflated."""
import sys, zipfile
src, dex, out = sys.argv[1:4]
STORE = ('AndroidManifest.xml', 'resources.arsc')
with zipfile.ZipFile(src) as zin, zipfile.ZipFile(out, 'w') as zout:
    names = [i.filename for i in zin.infolist() if not i.filename.startswith('META-INF/')]
    names = ['AndroidManifest.xml', 'resources.arsc'] + [n for n in sorted(names) if n not in STORE]
    for n in names:
        data = zin.read(n)
        ct = zipfile.ZIP_STORED if (n in STORE or n.endswith('.png')) else zipfile.ZIP_DEFLATED
        zi = zipfile.ZipInfo(n, (1981, 1, 1, 1, 1, 2)); zi.compress_type = ct
        zout.writestr(zi, data, compresslevel=9)
    zi = zipfile.ZipInfo('classes.dex', (1981, 1, 1, 1, 1, 2)); zi.compress_type = zipfile.ZIP_DEFLATED
    zout.writestr(zi, open(dex, 'rb').read(), compresslevel=9)
for i in zipfile.ZipFile(out).infolist(): print(f'{i.compress_type} {i.file_size:>7} {i.compress_size:>7} {i.filename}')
