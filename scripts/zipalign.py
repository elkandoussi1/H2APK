#!/usr/bin/env python3
"""Minimal zipalign replacement: `zipalign [-p] [-f] [-v] <align> in.apk out.apk`.
Pads the local extra field so every STORED entry's data starts on an <align>-byte boundary."""
import struct, sys, zipfile
args = [a for a in sys.argv[1:] if not a.startswith('-')]
align, src, dst = int(args[0]), args[1], args[2]
zin = zipfile.ZipFile(src)
raw = open(src, 'rb').read()
out = bytearray(); central = bytearray()
for zi in zin.infolist():
    name = zi.filename.encode('utf-8' if zi.flag_bits & 0x800 else 'cp437')
    h = zi.header_offset
    sig, ver, flags, meth, mtime, mdate, crc, cs, us, nlen, elen = struct.unpack('<IHHHHHIIIHH', raw[h:h + 30])
    assert sig == 0x04034b50
    extra = raw[h + 30 + nlen:h + 30 + nlen + elen]
    data_start = h + 30 + nlen + elen
    data = raw[data_start:data_start + zi.compress_size]
    offset = len(out)
    if zi.compress_type == 0:
        pad = (align - (offset + 30 + len(name) + len(extra)) % align) % align
        extra = extra + b'\0' * pad
    fl = zi.flag_bits & ~0x08
    out += struct.pack('<IHHHHHIIIHH', 0x04034b50, ver, fl, zi.compress_type, mtime, mdate, zi.CRC, zi.compress_size, zi.file_size, len(name), len(extra))
    out += name + extra + data
    cextra = zi.extra
    comment = zi.comment
    central += struct.pack('<IHHHHHHIIIHHHHHII', 0x02014b50, (zi.create_system << 8) | zi.create_version, zi.extract_version, fl, zi.compress_type,
                           mtime, mdate, zi.CRC, zi.compress_size, zi.file_size, len(name), len(cextra), len(comment), 0, zi.internal_attr, zi.external_attr, offset)
    central += name + cextra + comment
cd_off = len(out)
out += central
n = len(zin.infolist())
out += struct.pack('<IHHHHIIH', 0x06054b50, 0, 0, n, n, len(central), cd_off, 0)
open(dst, 'wb').write(out)
