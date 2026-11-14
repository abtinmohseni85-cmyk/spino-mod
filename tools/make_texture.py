#!/usr/bin/env python3
"""Paints spinosaurus.png + spinosaurus_glow.png (256x256) for the JP3 Spinosaurus model.

Every box below uses the SAME (u, v, w, h, d) values as SpinosaurusModel.java,
so the UV unwrap always lines up. Needs: pip install pillow
Run:  python3 tools/make_texture.py
"""
import math
import os
import random

from PIL import Image

random.seed(1337)
SIZE = 256
img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
glow = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
px = img.load()
gx = glow.load()


def clamp(v):
    return max(0, min(255, int(v)))


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return (a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t)


def noisy(c, amt=9):
    n = random.randint(-amt, amt)
    return (clamp(c[0] + n), clamp(c[1] + n), clamp(c[2] + n), 255)


# palette (taken from the reference art: grey-brown hide, red-orange head/tail, blue sail stripes)
GRAY = (122, 112, 104)
GRAY_D = (88, 80, 76)
CREAM = (198, 184, 160)
WHITE = (238, 230, 212)
RED = (178, 70, 44)
RED_D = (128, 42, 30)
ORANGE = (206, 104, 54)
BLUE = (86, 118, 164)
CLAW = (32, 28, 26)
PINK = (152, 66, 72)


def paint_box(u, v, w, h, d, fn):
    faces = {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "left": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "right": (u + d + w, v + d, d, h),
        "back": (u + 2 * d + w, v + d, w, h),
    }
    for name, (fu, fv, fw, fh) in faces.items():
        for y in range(fh):
            for x in range(fw):
                c = fn(name, x, y, fw, fh)
                if c:
                    px[fu + x, fv + y] = c


def make_fn(top, side_top, side_bot, bottom, streak=0.0, noise=9, spot=0.04):
    def f(face, x, y, fw, fh):
        if face == "top":
            base = top
        elif face == "bottom":
            base = bottom
        else:
            t = y / max(1, fh - 1)
            base = mix(side_top, side_bot, t)
            if streak and math.sin((x + 0.2 * y) * 0.9) > 0.94:
                base = mix(base, WHITE, streak)
        if random.random() < spot:
            base = mix(base, (60, 40, 34), 0.35)
        return noisy(base, noise)

    return f


def sail_fn(face, x, y, fw, fh):
    if face in ("left", "right"):
        tt = y / 26.0
        base = mix(BLUE, RED, (tt - 0.05) / 0.75)
        if x % 2 == 0:
            base = mix(base, RED_D, 0.25)
        if (x * 5 + y * 3) % 13 < 2:
            base = mix(base, ORANGE, 0.5)
    elif face in ("top", "bottom"):
        base = RED_D
    else:
        base = RED
    return noisy(base, 8)


def teeth_fn(upper):
    def f(face, x, y, fw, fh):
        if face in ("left", "right"):
            visible = (y == 0 or x % 3 == 1) if upper else (y == fh - 1 or x % 3 == 1)
            if not visible:
                return (0, 0, 0, 0)
        return noisy(WHITE, 6)

    return f


BODY_TOP = mix(GRAY_D, RED_D, 0.3)

# (u, v, w, h, d, painter)  -- keep identical to SpinosaurusModel.java
paint_box(0, 0, 18, 22, 40, make_fn(BODY_TOP, mix(GRAY_D, GRAY, 0.7), mix(GRAY, CREAM, 0.85), CREAM, streak=0.35, spot=0.06))
paint_box(0, 64, 10, 22, 14, make_fn(GRAY, GRAY, mix(GRAY, CREAM, 0.3), GRAY_D, streak=0.3))
paint_box(48, 64, 7, 26, 7, make_fn(GRAY, GRAY, GRAY_D, GRAY_D, streak=0.2))
paint_box(0, 102, 10, 5, 14, make_fn((96, 88, 82), (96, 88, 82), (70, 62, 58), (60, 54, 50)))
paint_box(0, 124, 10, 3, 3, make_fn(CLAW, CLAW, CLAW, CLAW, noise=4, spot=0))
paint_box(116, 0, 12, 14, 24, make_fn(mix(GRAY, RED, 0.35), mix(GRAY, RED, 0.25), mix(CREAM, GRAY, 0.4), CREAM, streak=0.3))
paint_box(116, 40, 9, 10, 26, make_fn(mix(GRAY, RED, 0.65), mix(GRAY, RED, 0.55), mix(CREAM, RED, 0.35), mix(CREAM, RED, 0.2), streak=0.25))
paint_box(116, 78, 6, 7, 28, make_fn(mix(GRAY, RED, 0.9), mix(RED, ORANGE, 0.3), mix(ORANGE, CREAM, 0.3), mix(ORANGE, CREAM, 0.4)))
paint_box(190, 0, 2, 26, 6, sail_fn)
paint_box(0, 132, 10, 11, 16, make_fn(RED, mix(RED, GRAY, 0.35), mix(CREAM, GRAY, 0.3), CREAM, streak=0.3))
paint_box(52, 132, 9, 9, 12, make_fn(RED, mix(RED, ORANGE, 0.3), mix(RED, GRAY, 0.4), CREAM, streak=0.2))
paint_box(96, 132, 6, 5, 20, make_fn(mix(RED, ORANGE, 0.15), mix(RED, ORANGE, 0.35), mix(ORANGE, CREAM, 0.55), CREAM))
paint_box(150, 132, 8, 7, 5, make_fn(RED_D, RED_D, mix(RED_D, RED, 0.5), RED_D, noise=7))
paint_box(178, 132, 2, 4, 10, make_fn(RED_D, RED_D, RED_D, RED_D, noise=8))
paint_box(0, 162, 6, 3, 28, make_fn(PINK, mix(ORANGE, CREAM, 0.5), CREAM, CREAM))
paint_box(70, 162, 7, 2, 19, teeth_fn(True))
paint_box(124, 162, 7, 2, 22, teeth_fn(False))
paint_box(0, 196, 5, 12, 5, make_fn(GRAY, GRAY, GRAY, GRAY_D, streak=0.2))
paint_box(22, 196, 4, 11, 4, make_fn(GRAY, GRAY, GRAY_D, GRAY_D))
paint_box(40, 196, 5, 3, 5, make_fn(GRAY_D, GRAY_D, (60, 52, 48), (60, 52, 48)))
paint_box(62, 196, 1, 5, 1, make_fn(CLAW, CLAW, CLAW, CLAW, noise=3, spot=0))

# nostrils on the snout tip (top face starts at x=155,y=132 and is 8x5)
for nx in (157, 160):
    px[nx, 134] = (50, 16, 12, 255)

# eyes: skull side faces. left face u0=52, right face u0=73, both start at v=144; skull d=12, h=9
for side, u0 in (("left", 52), ("right", 73)):
    fw = 12
    for off in range(2, 7):  # dark brow ridge
        x = u0 + (fw - 1 - off) if side == "left" else u0 + off
        px[x, 145] = (92, 26, 20, 255)
    for off in (3, 4, 5):
        for row in (2, 3, 4):
            x = u0 + (fw - 1 - off) if side == "left" else u0 + off
            y = 144 + row
            if off == 4:  # vertical slit pupil
                px[x, y] = (14, 14, 8, 255)
                gx[x, y] = (0, 0, 0, 255)
            else:
                px[x, y] = (205, 228, 62, 255)
                gx[x, y] = (150, 235, 40, 255)

out = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "..", "src", "main", "resources", "assets", "jp3_spinosaurus", "textures", "entity",
)
os.makedirs(out, exist_ok=True)
img.save(os.path.join(out, "spinosaurus.png"))
glow.save(os.path.join(out, "spinosaurus_glow.png"))
print("textures written to", os.path.abspath(out))
