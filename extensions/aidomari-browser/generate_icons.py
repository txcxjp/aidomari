#!/usr/bin/env python3
"""Generates crisp extension icons in 16, 48, and 128px sizes using Pillow."""
from PIL import Image, ImageDraw

def create_icon(size):
    # Render at 4x for high-quality supersampled anti-aliasing
    scale = 4
    w = size * scale
    img = Image.new("RGBA", (w, w), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 1. Background rounded rectangle
    rx = int(w * 0.22)
    # Draw rounded rect
    draw.rounded_rectangle([0, 0, w - 1, w - 1], radius=rx, fill=(63, 81, 181, 255))
    
    # Gradient overlay hint (darker bottom)
    for y in range(int(w * 0.5), w):
        alpha = int(120 * ((y - w * 0.5) / (w * 0.5)))
        draw.line([(0, y), (w, y)], fill=(26, 35, 126, alpha))

    # 2. Back tilted card
    back_card = Image.new("RGBA", (w, w), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(back_card)
    bx0, by0 = int(w * 0.24), int(w * 0.20)
    bx1, by1 = int(w * 0.78), int(w * 0.82)
    b_draw.rounded_rectangle([bx0, by0, bx1, by1], radius=int(w * 0.08), fill=(121, 134, 203, 200))
    # Rotate around center
    back_card_rot = back_card.rotate(-10, resample=Image.Resampling.BICUBIC, center=(w * 0.5, w * 0.5))
    img.alpha_composite(back_card_rot)

    # 3. Main front card
    fx0, fy0 = int(w * 0.22), int(w * 0.19)
    fx1, fy1 = int(w * 0.76), int(w * 0.81)
    
    # Shadow under main card
    shadow = Image.new("RGBA", (w, w), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow)
    s_draw.rounded_rectangle([fx0 + int(w*0.02), fy0 + int(w*0.04), fx1 + int(w*0.02), fy1 + int(w*0.04)], radius=int(w * 0.08), fill=(10, 15, 60, 90))
    img.alpha_composite(shadow)

    # Front card body
    c_img = Image.new("RGBA", (w, w), (0, 0, 0, 0))
    c_draw = ImageDraw.Draw(c_img)
    c_draw.rounded_rectangle([fx0, fy0, fx1, fy1], radius=int(w * 0.08), fill=(255, 255, 255, 255))
    
    # Card horizontal lines
    pad_x = int(w * 0.10)
    line_x0 = fx0 + pad_x
    line_x1 = fx1 - pad_x
    line_h = max(2, int(w * 0.045))
    
    # Header bar (indigo)
    c_draw.rounded_rectangle([line_x0, fy0 + int(w * 0.12), line_x1, fy0 + int(w * 0.12) + line_h], radius=line_h//2, fill=(63, 81, 181, 255))
    # Content bars
    c_draw.rounded_rectangle([line_x0, fy0 + int(w * 0.22), line_x0 + int((line_x1 - line_x0) * 0.75), fy0 + int(w * 0.22) + int(line_h * 0.75)], radius=line_h//3, fill=(159, 168, 218, 255))
    c_draw.rounded_rectangle([line_x0, fy0 + int(w * 0.30), line_x0 + int((line_x1 - line_x0) * 0.90), fy0 + int(w * 0.30) + int(line_h * 0.75)], radius=line_h//3, fill=(197, 202, 233, 255))
    c_draw.rounded_rectangle([line_x0, fy0 + int(w * 0.38), line_x0 + int((line_x1 - line_x0) * 0.55), fy0 + int(w * 0.38) + int(line_h * 0.75)], radius=line_h//3, fill=(197, 202, 233, 255))
    img.alpha_composite(c_img)

    # 4. AI Sparkle Badge (Amber circle with white 4-point star)
    badge_cx, badge_cy = int(w * 0.72), int(w * 0.72)
    badge_r = int(w * 0.18)
    # Badge shadow
    b_shadow = Image.new("RGBA", (w, w), (0, 0, 0, 0))
    bs_draw = ImageDraw.Draw(b_shadow)
    bs_draw.ellipse([badge_cx - badge_r + int(w*0.02), badge_cy - badge_r + int(w*0.03), badge_cx + badge_r + int(w*0.02), badge_cy + badge_r + int(w*0.03)], fill=(10, 15, 60, 80))
    img.alpha_composite(b_shadow)

    # Amber circle
    b_img = Image.new("RGBA", (w, w), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(b_img)
    b_draw.ellipse([badge_cx - badge_r, badge_cy - badge_r, badge_cx + badge_r, badge_cy + badge_r], fill=(255, 179, 0, 255))
    
    # 4-point star inside circle
    sr = int(badge_r * 0.65)
    sw = int(badge_r * 0.22)
    star_points = [
        (badge_cx, badge_cy - sr),
        (badge_cx + sw, badge_cy - sw),
        (badge_cx + sr, badge_cy),
        (badge_cx + sw, badge_cy + sw),
        (badge_cx, badge_cy + sr),
        (badge_cx - sw, badge_cy + sw),
        (badge_cx - sr, badge_cy),
        (badge_cx - sw, badge_cy - sw),
    ]
    b_draw.polygon(star_points, fill=(255, 255, 255, 255))
    img.alpha_composite(b_img)

    # Resize down with high-quality Lanczos resampling
    final_img = img.resize((size, size), resample=Image.Resampling.LANCZOS)
    return final_img

if __name__ == "__main__":
    for s in [16, 48, 128]:
        icon = create_icon(s)
        path = f"extensions/aidomari-browser/icons/icon-{s}.png"
        icon.save(path, "PNG")
        print(f"Generated {path} ({s}x{s})")
