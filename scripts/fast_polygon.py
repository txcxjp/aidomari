import re

class FastPolygon:
    def __init__(self, svg_d_string):
        poly = []
        for part in svg_d_string.replace('M', '').replace('Z', '').split('L'):
            part = part.strip()
            if part:
                x, y = part.split(',')
                poly.append((float(x), float(y)))
        self.poly = poly
        self.n = len(poly)
        
        # Build Y-buckets for fast intersection tests
        # Grid bucket size: 5 pixels
        self.bucket_size = 5.0
        self.min_y = min(p[1] for p in poly)
        self.max_y = max(p[1] for p in poly)
        self.min_x = min(p[0] for p in poly)
        self.max_x = max(p[0] for p in poly)
        
        num_buckets = int((self.max_y - self.min_y) / self.bucket_size) + 2
        self.buckets = [[] for _ in range(num_buckets)]
        
        for i in range(self.n):
            p1x, p1y = poly[i]
            p2x, p2y = poly[(i + 1) % self.n]
            low_y, high_y = min(p1y, p2y), max(p1y, p2y)
            b_start = max(0, int((low_y - self.min_y) / self.bucket_size))
            b_end = min(num_buckets - 1, int((high_y - self.min_y) / self.bucket_size))
            for b in range(b_start, b_end + 1):
                self.buckets[b].append((p1x, p1y, p2x, p2y))

    def contains(self, x, y):
        if x < self.min_x or x > self.max_x or y < self.min_y or y > self.max_y:
            return False
        b_idx = int((y - self.min_y) / self.bucket_size)
        if b_idx < 0 or b_idx >= len(self.buckets):
            return False
        
        inside = False
        for p1x, p1y, p2x, p2y in self.buckets[b_idx]:
            if y > min(p1y, p2y) and y <= max(p1y, p2y):
                if x <= max(p1x, p2x):
                    if p1y != p2y:
                        xinters = (y - p1y) * (p2x - p1x) / (p2y - p1y) + p1x
                    if p1x == p2x or x <= xinters:
                        inside = not inside
        return inside

    def dist_to_border(self, px, py):
        min_d = 1e9
        for i in range(self.n):
            x1, y1 = self.poly[i]
            x2, y2 = self.poly[(i + 1) % self.n]
            dx = x2 - x1
            dy = y2 - y1
            if dx == 0 and dy == 0:
                d = ((px - x1)**2 + (py - y1)**2)**0.5
            else:
                t = max(0.0, min(1.0, ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy)))
                proj_x = x1 + t * dx
                proj_y = y1 + t * dy
                d = ((px - proj_x)**2 + (py - proj_y)**2)**0.5
            if d < min_d:
                min_d = d
        return min_d

def load_gsi_polygon():
    with open('/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/ui/sugoroku/HokkaidoSvgPaths.kt') as f:
        text = f.read()
    m = re.search('MAINLAND_D = \"([^\"]+)\"', text)
    return FastPolygon(m.group(1))

if __name__ == '__main__':
    poly = load_gsi_polygon()
    print(f"Loaded GSI mainland polygon with {poly.n} vertices.")
    print("Testing Hakodate (105.0, 331.0):", poly.contains(105.0, 331.0))
    print("Testing Soya Cape (186.0, 33.0):", poly.contains(186.0, 33.0))
    print("Testing Aidomari (379.85, 124.65):", poly.contains(379.85, 124.65))
    print("Testing Old Hakodate bay (105.0, 328.0):", poly.contains(105.0, 328.0))
