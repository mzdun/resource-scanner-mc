#!/usr/bin/env python3
# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import math

size = 128
crossline = 6
step = 14
edge = 12
scanner_slope = 80
scanner_slope_drag = 80

center = int(size / 2)
R = center - edge


def circle(radius: float):
    print(f'    <circle r="{radius}" cx="{center}" cy="{center}"/>')


def line(x1: float, y1: float, x2: float, y2: float):
    print(f'    <line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}"/>')


def h(x: float, y: float, len: float):
    line(x, y, x + len, y)


def v(x: float, y: float, len: float):
    line(x, y, x, y + len)


def slope(deg: float):
    rad = deg * math.pi / 180
    dx = math.cos(rad)
    dy = math.sin(rad)
    return [dx, dy]


def coaxial(start: float, stop: float, deg: float):
    [dx, dy] = slope(deg)
    line(
        center + start * dx, center + start * dy, center + stop * dx, center + stop * dy
    )


[slopeX, slopeY] = slope(scanner_slope)
[dragX, dragY] = slope(scanner_slope - scanner_slope_drag)

print(
    f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {size} {
      size}" width="{size}px" height="{size}px" fill="none" stroke="none">'
)
print(
    """  <style>
    .pen, .heavy-pen {
      stroke: var(--color, #daff99);
      stroke-width: 1;
    }
    .heavy-pen {
      stroke-width: 1.7;
    }
    .brush {
      fill: var(--color, #daff99);
    }
  </style>
  <defs>
    <linearGradient id="scan" x1="58%" y1="0%" x2="42%" y2="100%" >
      <stop offset="0%" style="stop-color:rgb(157,255,0);stop-opacity:0.00" />
      <stop offset="100%" style="stop-color:rgb(234,255,200);stop-opacity:0.70" />
    </linearGradient>
    <linearGradient id="bg" x1="0%" y1="3%" x2="100%" y2="97%" >
      <stop offset="0%" style="stop-color:rgb(31,48,0);stop-opacity:1.00" />
      <stop offset="100%" style="stop-color:rgb(14,18,7);stop-opacity:1.00" />
    </linearGradient>
  </defs>
  <rect width="128" height="128" fill="url(#bg)" />"""
)

print(
    f'  <path fill="url(#scan)" d="M{center},{center} l{
      R*slopeX},{R*slopeY} A{R},{R} 0 0 0 {center + R*dragX},{center + R*dragY} z" />'
)

print('  <g class="pen">')
for radius in range(R - step, 0, -step):
    circle(radius)
h(edge + step * 3 / 2, center, R * 2 - step * 3)
v(center, edge + step * 3 / 2, R * 2 - step * 3)

for index in range(math.floor(R / step)):
    v(edge + step / 2 + index * step, center - crossline / 2, crossline)
    v(edge + 2 * R - step / 2 - index * step, center - crossline / 2, crossline)
    h(center - crossline / 2, edge + step / 2 + index * step, crossline)
    h(center - crossline / 2, edge + 2 * R - step / 2 - index * step, crossline)

for arc in range(0, 360, 4):
    len = step * 2 / 3 if arc % 20 == 0 else step / 3
    coaxial(R - len, R, arc)

print(
    """  </g>
  <g class="heavy-pen">"""
)

circle(R)
h(edge, center, step * 3 / 2)
v(center, edge, step * 3 / 2)
h(size - edge, center, -step * 3 / 2)
v(center, size - edge, -step * 3 / 2)
coaxial(0, R, scanner_slope)

print("  </g>")
print("</svg>")
