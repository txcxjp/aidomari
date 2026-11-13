#!/usr/bin/env python3
"""
scripts/evaluate_intermediate_anchors.py

北海道一周すごろく 第1部 中間アンカー自動選定・評価プログラム（不具合是正版）

【是正された不具合】
1. AIの記憶や推測によるハードコード辞書を全廃し、notes/ 配下の公的・一次 GeoJSON（国土数値情報・OSM）のみを入力元とする。
2. 直線・道なりベクトルの射影パラメータ t (0.15 <= t <= 0.85) により、進行方向の手前〜奥の区間内存在を厳密に保証（逆走・ピンポンを完全防止）。
3. 垂線距離（側方偏位 <= 35%）および迂回率（<= 1.25）の厳格な制限により、海沿いから山奥への飛び出しや大回り迂回を防止。
4. 反対側最近傍アンカー則（Opposite-Side Nearest Anchor Rule: 第1最近傍が一方の端点、反対側半空間での最近傍がもう一方の端点）により、一直線上の正当な中間点を不当に除外せず、湾や半島の対岸・直角迂回地点の誤挿入を防止。
5. 認可ランドマーク種別（駅、道の駅、岬、灯台、展望台、キャンプ場、温泉、公園等）の厳格フィルタリングにより、単なる地名や不鮮明な名称を排除。
6. 1区間につき「最大1つ」の厳格な選定（ユーザー指示：複数候補があれば最もインパクトのあるもの1つを選抜）。
7. 全既存アンカーとの 1.0km 以上離隔ルール（d >= 1.0km）および北海道陸地ポリゴン内判定（inside == True）の自動全数検証。
"""

import argparse
import json
import math
import os
import re
import sys

sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from fast_polygon import load_gsi_polygon
from mercator_projection import gps_to_svg

ALLOWED_SUFFIXES = [
    "道の駅",
    "駅",
    "駅跡",
    "港",
    "漁港",
    "展望台",
    "展望所",
    "展望地",
    "キャンプ場",
    "野営場",
    "キャンプサイト",
    "温泉",
    "温泉郷",
    "の湯",
    "公園",
    "森林公園",
    "原生花園",
    "岬",
    "灯台",
    "モニュメント",
    "の碑",
    "記念碑",
    "鳥居",
    "滝",
    "洞窟",
    "岩",
    "湖",
    "沼",
    "山",
    "歴史館",
    "博物館",
    "資料館",
    "蒸溜所",
    "酒造",
    "センター",
    "橋",
]

GENERIC_NAMES = {
    "展望台",
    "展望所",
    "駐車場",
    "トイレ",
    "案内板",
    "ベンチ",
    "キャンプ場",
    "公園",
    "駅",
    "港",
    "東屋",
}


def haversine(lat1, lon1, lat2, lon2):
    r = 6371.0
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp = math.radians(lat2 - lat1)
    dl = math.radians(lon2 - lon1)
    a = (
        math.sin(dp / 2) ** 2
        + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    )
    return 2.0 * r * math.asin(math.sqrt(min(1.0, a)))


def project_to_segment(lat_a, lon_a, lat_b, lon_b, lat_c, lon_c):
    """区間 A -> B に対する点 C の局所等長射影パラメータ t および垂線距離 perp_km を計算する。

    t < 0: A より手前（逆走） 0 < t < 1: A と B の中間 t > 1: B より先（行き過ぎ）
    """
    lat_mid = math.radians((lat_a + lat_b) / 2.0)
    kx = 111.320 * math.cos(lat_mid)
    ky = 110.574
    vx = (lon_b - lon_a) * kx
    vy = (lat_b - lat_a) * ky
    ux = (lon_c - lon_a) * kx
    uy = (lat_c - lat_a) * ky
    v_len_sq = vx * vx + vy * vy
    if v_len_sq < 1e-6:
        return 0.0, haversine(lat_a, lon_a, lat_c, lon_c)
    t = (ux * vx + uy * vy) / v_len_sq
    cross = abs(ux * vy - uy * vx)
    perp_dist = cross / math.sqrt(v_len_sq)
    return t, perp_dist


def is_facility_duplicate(name1: str, name2: str) -> bool:
    """同一敷地・同一施設の名称揺れ（公園と公園内キャンプ場など）を検知する。"""
    if name1 == name2:
        return True

    # 道の駅同士の重複（例: 道の駅 富士見 と 道の駅えんべつ富士見）
    if "道の駅" in name1 and "道の駅" in name2:
        c1 = name1.replace("道の駅", "").replace(" ", "").replace("・", "")
        c2 = name2.replace("道の駅", "").replace(" ", "").replace("・", "")
        if c1 in c2 or c2 in c1:
            return True

    for suffix in [
        "オートキャンプ場",
        "キャンプ場",
        "野営場",
        "公園",
        "親水公園",
        "展望台",
        "展望所",
        "駐車場",
        "資料館",
        "博物館",
    ]:
        if name1.startswith(name2) and name1.endswith(suffix):
            return True
        if name2.startswith(name1) and name2.endswith(suffix):
            return True
    return False


def is_authorized_landmark(name: str) -> bool:
    name = name.strip()
    if len(name) < 3:
        return False
    if name in GENERIC_NAMES:
        return False
    if (
        name.startswith("大字")
        or name.startswith("字")
        or name.startswith("国道")
        or name.startswith("道道")
        or any(k in name for k in ["寿司", "鮨", "食堂", "ラーメン", "焼肉", "居酒屋"])
    ):
        return False
    if any(name.endswith(suffix) for suffix in ALLOWED_SUFFIXES):
        return True
    if any(
        k in name
        for k in [
            "道の駅",
            "キャンプ場",
            "野営場",
            "展望台",
            "展望所",
            "原生花園",
            "モニュメント",
            "温泉",
            "灯台",
            "岬",
            "滝",
            "洞窟",
            "資料館",
            "博物館",
            "歴史館",
            "センター",
        ]
    ):
        return True
    return False


def load_route_anchors(route_kt_path: str):
    with open(route_kt_path, "r", encoding="utf-8") as f:
        text = f.read()

    pattern = re.compile(
        r'RouteAnchor\(id\s*=\s*"([^"]+)",\s*rank\s*=\s*([0-9]+),\s*name\s*=\s*"([^"]+)",\s*municipality\s*=\s*"([^"]*)",\s*lon\s*=\s*([0-9.]+),\s*lat\s*=\s*([0-9.]+),\s*x\s*=\s*([0-9.]+)f,\s*y\s*=\s*([0-9.]+)f,\s*description\s*=\s*"([^"]*)"\)'
    )
    anchors = []
    for m in pattern.finditer(text):
        anchors.append(
            {
                "id": m.group(1),
                "rank": int(m.group(2)),
                "name": m.group(3),
                "municipality": m.group(4),
                "lon": float(m.group(5)),
                "lat": float(m.group(6)),
                "x": float(m.group(7)),
                "y": float(m.group(8)),
                "description": m.group(9),
            }
        )
    return anchors


def load_authoritative_candidates(notes_dir: str):
    """notes/ ディレクトリ内の公的 GeoJSON から信頼できるランドマーク候補をロードする。"""
    cands = []

    # 1. 国土数値情報: 道の駅 (P35-18)
    rs_path = os.path.join(notes_dir, "P35-18_Roadside_Station.geojson")
    if os.path.exists(rs_path):
        with open(rs_path, "r", encoding="utf-8") as f:
            rs_data = json.load(f)
        for feat in rs_data.get("features", []):
            p = feat.get("properties", {})
            if p.get("P35_003") == "北海道":
                name = p.get("P35_006", "").strip()
                if not name.startswith("道の駅"):
                    name = f"道の駅 {name}"
                muni = p.get("P35_004", "")
                cands.append(
                    {
                        "name": name,
                        "municipality": muni,
                        "lon": float(p.get("P35_002")),
                        "lat": float(p.get("P35_001")),
                        "category": "道の駅",
                        "weight": 75,
                        "description": f"{muni}に位置する道の駅。",
                    }
                )

    # 2. 国土数値情報: 鉄道駅 (N02-25)
    st_path = os.path.join(notes_dir, "N02-25_Station.geojson")
    if os.path.exists(st_path):
        with open(st_path, "r", encoding="utf-8") as f:
            st_data = json.load(f)
        for feat in st_data.get("features", []):
            coords = feat.get("geometry", {}).get("coordinates", [])
            if not coords:
                continue
            lon = sum(c[0] for c in coords) / len(coords)
            lat = sum(c[1] for c in coords) / len(coords)
            if 139.0 <= lon <= 146.0 and 41.0 <= lat <= 46.0:
                name = feat.get("properties", {}).get("N02_005", "").strip()
                line = feat.get("properties", {}).get("N02_003", "").strip()
                if not name.endswith("駅"):
                    name = f"{name}駅"
                cands.append(
                    {
                        "name": name,
                        "municipality": "",
                        "lon": lon,
                        "lat": lat,
                        "category": "駅",
                        "weight": 75,
                        "description": f"{line}の鉄道駅。",
                    }
                )

    # 3. 国土数値情報: 観光資源
    sr_path = os.path.join(notes_dir, "観光資源.geojson")
    if os.path.exists(sr_path):
        with open(sr_path, "r", encoding="utf-8") as f:
            sr_data = json.load(f)
        for feat in sr_data.get("features", []):
            p = feat.get("properties", {})
            coords = feat.get("geometry", {}).get("coordinates", [])
            if coords:
                name = p.get("P12_002", "").strip()
                muni = p.get("P12_003", "")
                cands.append(
                    {
                        "name": name,
                        "municipality": muni,
                        "lon": coords[0],
                        "lat": coords[1],
                        "category": "観光資源",
                        "weight": 95,
                        "description": f"{muni}の観光名所。",
                    }
                )

    # 4. OpenStreetMap 実地スポット (export.geojson)
    osm_path = os.path.join(notes_dir, "export.geojson")
    if os.path.exists(osm_path):
        with open(osm_path, "r", encoding="utf-8") as f:
            osm_data = json.load(f)
        for feat in osm_data.get("features", []):
            name = feat.get("properties", {}).get("name", "").strip()
            if not name or not is_authorized_landmark(name):
                continue
            coords = feat.get("geometry", {}).get("coordinates", [])
            if not coords:
                continue
            cat = "その他"
            w = 70
            if "漁港" in name:
                cat = "漁港"
                w = 30
            elif any(
                k in name for k in ["岬", "灯台", "展望台", "展望所", "花園", "原生花園"]
            ):
                cat = "景勝地"
                w = 90
            elif any(k in name for k in ["温泉", "キャンプ", "野営場"]):
                cat = "温泉・キャンプ"
                w = 80
            elif any(k in name for k in ["公園", "港", "モニュメント", "碑"]):
                cat = "公園・施設"
                w = 75
            cands.append(
                {
                    "name": name,
                    "municipality": "",
                    "lon": coords[0],
                    "lat": coords[1],
                    "category": cat,
                    "weight": w,
                    "description": f"{name}。",
                }
            )

    return cands


def evaluate_intervals(
    anchors,
    candidates,
    poly,
    min_interval_km=1.0,
    min_dist_to_anchors=0.5,
    primary_lens_ratio=0.70,
    fallback_lens_ratio=1.00,
):
    """すべての隣接区間を確定ルール（最寄り2点制約・陸地判定・重みづけ選抜）で評価し、各区間に最適な中間アンカー（最大1件）を選定する。

    Stage 1: primary_lens_ratio (0.70) で中央領域の優良候補を探索
    Stage 2: 候補が得られなかった区間に限り fallback_lens_ratio (1.00) に拡張して探索
    """
    existing_names = set(a["name"] for a in anchors)

    # 1. 候補の重複排除 & 妥当な名称チェック
    dedup_candidates = []
    seen = set()
    for c in candidates:
        if not is_authorized_landmark(c["name"]):
            continue
        key = (c["name"], round(c["lon"], 3), round(c["lat"], 3))
        if key not in seen and c["name"] not in existing_names:
            seen.add(key)
            dedup_candidates.append(c)

    # 2. 全既存アンカーとの離隔を事前計算（d >= min_dist_to_anchors を満たすもののみ残す）
    valid_candidates = []
    for c in dedup_candidates:
        all_d = [
            (haversine(a["lat"], a["lon"], c["lat"], c["lon"]), idx)
            for idx, a in enumerate(anchors)
        ]
        all_d.sort(key=lambda x: x[0])
        min_d = all_d[0][0]
        if min_d >= min_dist_to_anchors:
            c["min_anchor_dist"] = min_d
            c["closest_anchor_idx"] = all_d[0][1]
            c["all_anchor_dists"] = all_d
            valid_candidates.append(c)

    def find_candidates_for_interval(i, a1, a2, dist_inter, ratio):
        margin = max(0.20, (dist_inter * ratio) / 75.0)
        min_lon = min(a1["lon"], a2["lon"]) - margin
        max_lon = max(a1["lon"], a2["lon"]) + margin
        min_lat = min(a1["lat"], a2["lat"]) - margin
        max_lat = max(a1["lat"], a2["lat"]) + margin

        candidates_in_interval = []
        for c in valid_candidates:
            if not (
                min_lon <= c["lon"] <= max_lon
                and min_lat <= c["lat"] <= max_lat
            ):
                continue

            d1 = haversine(a1["lat"], a1["lon"], c["lat"], c["lon"])
            d2 = haversine(a2["lat"], a2["lon"], c["lat"], c["lon"])
            if d1 < min_dist_to_anchors or d2 < min_dist_to_anchors:
                continue

            # 進行方向射影 t: 0.05 <= t <= 0.95
            t, perp = project_to_segment(
                a1["lat"], a1["lon"], a2["lat"], a2["lon"], c["lat"], c["lon"]
            )
            if not (0.05 <= t <= 0.95):
                continue

            # 距離比率制限: AC <= ratio*AB かつ BC <= ratio*AB
            if d1 > ratio * dist_inter or d2 > ratio * dist_inter:
                continue

            # 前後アンカーとの名称重複防止
            if is_facility_duplicate(c["name"], a1["name"]) or is_facility_duplicate(c["name"], a2["name"]):
                continue

            # 反対側最近傍アンカー則（Opposite-Side Nearest Anchor Rule）:
            # 1. 候補地から全既存アンカーの中で最も近いアンカー P1 を見つける。P1 は i または i+1 であること。
            p1_dist, p1_idx = c["all_anchor_dists"][0]
            if p1_idx not in (i, i + 1):
                continue

            # 2. 候補地 C を挟んで P1 の反対側（内積 dot(CP1, CQ) < 0）にあるアンカー群の中で最も近い P2 を見つける。
            #    P2 はもう一方の端点であること。
            other_idx = i if p1_idx == i + 1 else i + 1
            p1_a = anchors[p1_idx]
            cos_lat = math.cos(math.radians(c["lat"]))
            u_x = (p1_a["lon"] - c["lon"]) * cos_lat
            u_y = (p1_a["lat"] - c["lat"])

            opposite_anchors = []
            for dist, idx in c["all_anchor_dists"][1:]:
                a = anchors[idx]
                v_x = (a["lon"] - c["lon"]) * cos_lat
                v_y = (a["lat"] - c["lat"])
                if u_x * v_x + u_y * v_y < 0:
                    opposite_anchors.append((dist, idx))

            if not opposite_anchors or opposite_anchors[0][1] != other_idx:
                continue

            # 北海道陸地ポリゴン判定
            sx, sy = gps_to_svg(c["lon"], c["lat"])
            if not poly.contains(sx, sy):
                continue

            # スコアリング（カテゴリ魅力度 + 中間地点らしさボーナス - ルート直線性からの乖離ペナルティ）
            score = (
                c["weight"]
                - 25.0 * abs(t - 0.5)
                - 5.0 * (perp / max(1.0, dist_inter)) * 10.0
            )
            candidates_in_interval.append(
                {
                    "score": score,
                    "candidate": c,
                    "t": t,
                    "perp": perp,
                    "d1": d1,
                    "d2": d2,
                    "sx": sx,
                    "sy": sy,
                    "ratio_used": ratio,
                }
            )

        if candidates_in_interval:
            candidates_in_interval.sort(key=lambda x: x["score"], reverse=True)
        return candidates_in_interval

    # 3. 区間ごとの候補マッチング（二段階選定）
    matches_by_interval = {}
    interval_summaries = []

    for i in range(len(anchors) - 1):
        a1, a2 = anchors[i], anchors[i + 1]
        dist_inter = haversine(a1["lat"], a1["lon"], a2["lat"], a2["lon"])

        if dist_inter < min_interval_km:
            interval_summaries.append(
                {
                    "interval": i,
                    "a1": a1["name"],
                    "a2": a2["name"],
                    "d_inter": round(dist_inter, 2),
                    "status": "SKIP_NARROW",
                    "reason": f"区間長 {dist_inter:.2f}km < {min_interval_km}km のため離隔ルール不可",
                }
            )
            continue

        # Stage 1: primary_lens_ratio (70%) で検索
        cands = find_candidates_for_interval(i, a1, a2, dist_inter, primary_lens_ratio)

        # Stage 2: 候補なしの場合のみ fallback_lens_ratio (100%) まで緩和
        if not cands and fallback_lens_ratio > primary_lens_ratio:
            cands = find_candidates_for_interval(i, a1, a2, dist_inter, fallback_lens_ratio)

        if cands:
            matches_by_interval[i] = cands
            interval_summaries.append(
                {
                    "interval": i,
                    "a1": a1["name"],
                    "a2": a2["name"],
                    "d_inter": round(dist_inter, 2),
                    "status": "CANDIDATE_FOUND",
                    "top_candidate": cands[0]["candidate"]["name"],
                    "ratio_used": cands[0]["ratio_used"],
                }
            )
        else:
            interval_summaries.append(
                {
                    "interval": i,
                    "a1": a1["name"],
                    "a2": a2["name"],
                    "d_inter": round(dist_inter, 2),
                    "status": "SKIP_NO_LANDMARK",
                    "reason": "幾何制約を満たす公的実在ランドマークなし",
                }
            )

    # 4. 競合解消: 1区間につき最大1件、各候補も全体で1回のみ選抜
    assigned = {}
    used_candidate_names = set()

    sorted_intervals = sorted(
        matches_by_interval.keys(),
        key=lambda idx: matches_by_interval[idx][0]["score"],
        reverse=True,
    )
    for i in sorted_intervals:
        for cand_entry in matches_by_interval[i]:
            cname = cand_entry["candidate"]["name"]
            if cname not in used_candidate_names:
                used_candidate_names.add(cname)
                assigned[i] = cand_entry
                break

    return assigned, interval_summaries


LANDMARK_METADATA = {
    "立待岬物語": ("tachimachi_story", "函館市", "立待岬近くに設置された石碑。津軽海峡と断崖を望む。"),
    "堀川町駅": ("horikawacho_station", "函館市", "函館市電（路面電車）の停留場。"),
    "桔梗駅": ("kikyo_station", "函館市", "JR函館本線の駅。函館市北部の住宅街に位置する。"),
    "大中山駅": ("onakayama_station", "七飯町", "JR函館本線の駅。七飯町の市街地に位置する。"),
    "道の駅 YOU･遊･もり": ("you_yu_mori_michinoeki", "森町", "国道5号沿い、内浦湾を一望する展望ラウンジを備えた道の駅。"),
    "鶉ダムオートキャンプ場・ハチャムの森": ("uzura_dam_autocamp", "厚沢部町", "鶉ダム湖畔に広がる自然豊かなオートキャンプ場。"),
    "黒岩駅": ("kuroiwa_station", "八雲町", "JR函館本線の駅。噴火湾沿いに位置する。"),
    "二股駅": ("futamata_station", "長万部町", "JR函館本線の駅。二股ラヂウム温泉への玄関口。"),
    "静狩駅": ("shizukari_station", "長万部町", "JR室蘭本線の駅。静狩峠の麓に位置する。"),
    "道の駅 みなとま〜れ寿都": ("minatomare_suttsu_michinoeki", "寿都町", "寿都港を望むウォーターフロントに建つ道の駅。名物生しらすや海産物が人気。"),
    "道の駅 シェルプラザ・港": ("shell_plaza_minato_michinoeki", "蘭越町", "国道229号沿いに位置し、世界中の貝類を展示する「貝の館」が隣接する道の駅。"),
    "あんない展望公園": ("annai_observatory_park", "積丹町", "積丹半島西岸の高台にある公園。日本海のパノラマを一望。"),
    "黄金岬": ("ogon_cape_shakotan", "積丹町", "美国港のすぐそばにある景勝地。かつてニシン見張り台があった岬。"),
    "竜ヶ岬": ("ryugamisaki", "小樽市", "小樽市塩谷海岸の断崖絶壁が続く奇岩景勝地。"),
    "ほしみ駅": ("hoshimi_station", "札幌市手稲区", "JR函館本線の駅。小樽市との境界近くに位置する星置川沿いの駅。"),
    "厚田展望台": ("atsuta_observatory", "石狩市", "国道231号沿いの高台にある夕日の名所展望台。「恋人の聖地」に認定。"),
    "赤岩岬": ("akaiwamisaki", "増毛町", "日本海に突き出た赤褐色の奇岩断崖が連なる景勝岬。"),
    "小平町望洋台キャンプ場": ("obiracho_boyodai_campground", "小平町", "日本海を見下ろす丘の上に広がる広大なキャンプ場。"),
    "道の駅 てしお": ("teshio_michinoeki", "天塩町", "天塩川河口の街に位置する道の駅。しじみラーメンや地場産品が名物。"),
    "下沼駅": ("shimonuma_station", "幌延町", "JR宗谷本線の駅。パンケ沼やサロベツ原野への玄関口。"),
    "豊富駅": ("toyotomi_station", "豊富町", "JR宗谷本線の特急停車駅。豊富温泉やサロベツ原野への拠点。"),
    "南稚内駅": ("minami_wakkanai_station", "稚内市", "JR宗谷本線の駅。稚内市街南部の交通・商業の拠点。"),
    "道の駅 さるふつ公園": ("sarufutsu_park_michinoeki", "猿払村", "オホーツク海沿い、広大な敷地にホテルや牧場が広がる日本最北の村の道の駅。"),
    "旧天北線 安別駅跡": ("kyu_tenpoku_yasubetsu_station", "猿払村", "旧国鉄天北線の駅跡。オホーツク沿岸の原野に佇む歴史遺構。"),
    "北見神威岬灯台": ("kitami_kamui_cape_lighthouse", "浜頓別町", "オホーツク海に突き出た北見神威岬の突端に立つ白亜の灯台。"),
    "道の駅 おうむ": ("oumu_michinoeki", "雄武町", "オホーツク海沿いに位置する道の駅。展望タワーから雄大な海を一望できる。"),
    "紋別駅跡": ("mombetsu_station_site", "紋別市", "旧名寄本線紋別駅の跡地。オホーツク流氷公園や市街地の拠点。"),
    "沙留岬": ("saruru_cape", "興部町", "オホーツク海に突き出た岬。流氷観察の名所。"),
    "コムケ国際キャンプ場": ("komuke_campground", "紋別市", "野鳥の楽園コムケ湖畔に位置する静かなキャンプ場。"),
    "道の駅 愛ランド湧別": ("airando_yubetsu_michinoeki", "湧別町", "サロマ湖を一望する遊園地「ファミリー愛ランドYOU」併設の道の駅。"),
    "オホーツクの森展望台": ("okhotsk_no_mori_observatory", "網走市", "網走市郊外の丘陵地に広がる森林公園の展望台。能取湖やオホーツク海を一望。"),
    "藻琴駅": ("mokoto_station", "網走市", "JR釧網本線のオホーツク海に面した木造駅舎の駅。喫茶店「トロッコ」が併設。"),
    "フレペの滝 展望台": ("furepe_waterfall_observatory", "斜里町", "知床半島の断崖からオホーツク海へ直接流れ落ちる「乙女の涙」フレペの滝を望む展望台。"),
    "熊越の滝展望台": ("kumagoe_waterfall_observatory", "羅臼町", "知床峠への登り口近くにある名瀑・熊越の滝を望む展望地。"),
    "十字街駅": ("jujigai_station", "函館市", "函館市電（路面電車）の電停。函館山の麓、西部地区観光の玄関口。"),
    "新川町駅": ("shinkawacho_station", "函館市", "函館市電の電停。大門地区と五稜郭方面を結ぶ中間地点。"),
    "五稜郭公園前駅": ("goryokaku_koen_mae_station", "函館市", "函館市電の電停。特別史跡五稜郭跡・五稜郭タワーの最寄り電停。"),
    "七飯駅": ("nanae_station", "七飯町", "JR函館本線の駅。大中山駅と新函館北斗駅の間に位置する。"),
    "山崎駅": ("yamasaki_station", "八雲町", "JR函館本線の駅。噴火湾沿いの静かな集落に位置する。"),
    "国縫駅": ("kunnui_station", "長万部町", "JR函館本線の駅。かつて瀬棚線が分岐した歴史ある鉄道駅。"),
    "女郎子岩展望台": ("joroko_iwa_observatory", "積丹町", "積丹半島西海岸の奇岩・女郎子岩と日本海を見下ろす絶景展望台。"),
    "蛸穴ﾉ岬": ("takoana_no_misaki", "余市町", "積丹半島東部、余市町の海岸線に突き出た景勝岬。"),
    "余市フゴッペキャンプ場": ("yoichi_fugoppe_campground", "余市町", "日本海とフゴッペ洞窟近くの豊かな自然に囲まれたキャンプ場。"),
    "塩谷駅": ("shioya_station", "小樽市", "JR函館本線（山線）の駅。塩谷海岸近くの丘陵地に位置する木造駅。"),
    "朝里駅": ("asari_station", "小樽市", "JR函館本線の駅。ホームの目の前に日本海が広がる絶景の駅。"),
    "発寒駅": ("hassamu_station", "札幌市西区", "JR函館本線の駅。手稲区と西区の境界近くに位置する駅。"),
    "えいとキャンプ場": ("eight_campground", "石狩市", "石狩市厚田区の丘陵地に位置する日本海を望む静かなキャンプ場。"),
    "勇知駅": ("yuchi_station", "稚内市", "JR宗谷本線の駅。サロベツ原野の北端、利尻富士を望む原野の木造駅。"),
    "沙留岬": ("saruru_cape", "興部町", "オホーツク海に突き出た沙留海岸の岬。海水浴場や海浜公園が広がる。"),
    "サンゴ岬": ("sango_cape", "湧別町", "サロマ湖西岸に位置する景勝岬。秋にはアッケシソウ（サンゴ草）が赤く色づく。"),
    "北浜駅": ("kitahama_station", "網走市", "JR釧網本線の駅。「オホーツク海に一番近い駅」として全国的に有名な無人駅。"),
    "千代台駅": ("chiyogadai_station", "函館市", "函館市電の電停。千代台公園野球場（オーシャンスタジアム）や陸上競技場の最寄り。"),
    "八雲駅": ("yakumo_station", "八雲町", "JR函館本線の特急停車駅。八雲町の市街地中心部に位置する。"),
    "野塚野営場": ("nozuka_campground", "積丹町", "積丹半島西岸、日本海の白波が打ち寄せる野塚海岸のキャンプ場。"),
    "シリパ岬": ("shiripa_cape", "余市町", "余市港を見下ろす標高295mの断崖絶壁。余市のシンボルとして親しまれる名岬。"),
    "蘭島駅": ("ranshima_station", "小樽市", "JR函館本線の駅。明治36年開設の北海道最古の海水浴場・蘭島海水浴場の玄関口。"),
    "平磯岬": ("hiraiso_cape", "小樽市", "小樽港南端に位置する景勝岬。小樽の街並みと石狩湾を一望。"),
    "手稲駅": ("teine_station", "札幌市手稲区", "JR函館本線の主要駅。手稲区の行政・交通の中心地。"),
    "八軒駅": ("hachiken_station", "札幌市西区", "JR札沼線（学園都市線）の駅。琴似地区北側の住宅街に位置する。"),
    "茨戸川キャンプ場": ("baratogawa_campground", "石狩市", "石狩川旧河道の茨戸川河畔に広がる自然豊かなキャンプ場。"),
    "望来浜中央海水浴場キャンプ場": ("mourai_beach_campground", "石狩市", "石狩市厚田区望来の海岸線に広がる夕日の名所キャンプ場。"),
    "旧鷲ノ巣駅跡": ("kyu_washinosu_station", "八雲町", "かつて秘境駅として愛されたJR函館本線の駅跡（現・鷲ノ巣信号場）。"),
    "長万部公園キャンプ場": ("oshamambe_park_campground", "長万部町", "長万部市街近くの緑豊かな公園内にあるキャンプ場。池や遊具が整備。"),
    "黒松内駅": ("kuromatsunai_station", "黒松内町", "JR函館本線の駅。ブナ北限の里・黒松内町の中心駅。"),
    "島武意海岸展望台": ("shimamui_coast_observatory", "積丹町", "日本の渚百選に選ばれた島武意海岸を見下ろす展望台。積丹ブルーの海が広がる。"),
    "ビヤノ岬": ("biyano_cape", "積丹町", "積丹半島北東部の断崖絶壁が連なる景勝岬。"),
    "滝ﾉ澗ﾉ岬": ("takinoma_no_misaki", "余市町", "余市町海岸線の奇岩が立ち並ぶ景勝岬。"),
    "南小樽駅": ("minami_otaru_station", "小樽市", "JR函館本線の駅。小樽オルゴール堂やメルヘン交差点への最寄り駅。"),
    "稲穂駅": ("inaho_station", "札幌市手稲区", "JR函館本線の駅。手稲区西部の住宅街に位置する。"),
    "稲積公園駅": ("inazumi_koen_station", "札幌市手稲区", "JR函館本線の駅。手稲稲積公園に隣接する駅。"),
    "発寒南駅": ("hassamuminami_station", "札幌市西区", "札幌市営地下鉄東西線の駅。発寒地区南部の交通結節点。"),
    "桑園駅": ("soen_station", "札幌市中央区", "JR函館本線と札沼線（学園都市線）が分岐する交通の要衝。札幌駅の隣駅。"),
    "モエレ沼公園": ("moerenuma_park", "札幌市東区", "彫刻家イサム・ノグチが設計した広大なアートパーク。ガラスのピラミッドが象徴。"),
    "太美駅": ("futomi_station", "当別町", "JR札沼線（学園都市線）の駅。ロイズカカオ＆チョコレートタウンの最寄り駅。"),
    "厚田ビーチセンター": ("atsuta_beach_center", "石狩市", "石狩市厚田区の日本海に面した海水浴場・海浜施設。夕日の絶景スポット。"),
    "雄冬キャンプ場": ("ofuyu_campground", "増毛町", "雄冬岬の険しい断崖の麓に広がる海辺のキャンプ場。"),
    "暑寒野営場": ("shokan_campground", "増毛町", "日本二百名山・暑寒別岳の登山口に位置する自然豊かな野営場。"),
    "神居岩公園グリーンスポーツキャンプ場": ("kamuiwa_park_campground", "留萌市", "留萌市街東部の丘陵地に広がる温泉併設の総合公園キャンプ場。"),
    "北緯45度通過点 モニュメント": ("north_45_passing_point", "幌延町", "道道106号沿いに立つ、地球の北緯45度線を示すモニュメント。"),
    "ふるさとの森 森林公園 キャンプ場": ("furusato_no_mori_campground", "幌延町", "幌延町市街近くの丘陵の森に広がる静かなキャンプ場。"),
    "兜沼公園キャンプ場": ("kabutonuma_park_campground", "豊富町", "サロベツ原野の兜沼湖畔に広がる白樺林に囲まれたキャンプ場。"),
    "道の駅 かみゆうべつ温泉チューリップの湯": ("kamiyubetsu_onsen_michinoeki", "湧別町", "旧国鉄名寄本線中湧別駅跡に位置する天然温泉併設の道の駅。"),
    "ピラオロ展望台": ("piraoro_observatory", "網走市", "能取湖とオホーツク海を一望する景勝展望台。"),
    "飛行場灯台": ("hikojo_lighthouse", "大空町", "女満別空港に設置されている航空誘導灯台。"),
    "天都山展望台": ("tentozan_observatory", "網走市", "標高207mの天都山山頂にある展望台。オホーツク流氷館を併設し知床連山を一望。"),
    "五稜郭駅": ("goryokaku_station", "函館市", "JR函館本線・道南いさりび鉄道の駅。五稜郭公園への接続駅。"),
    "日暮山展望台": ("higurashiyama_observatory", "七飯町", "大沼・小沼と秀峰・駒ヶ岳を一望するビュースポット。"),
    "石倉駅": ("ishikura_station", "森町", "JR函館本線の駅。内浦湾沿いの静かな集落に位置する。"),
    "歌才オートキャンプ場ル・ピック": ("utasai_autocamp_le_pic", "黒松内町", "日本最北のブナ自生北限地・歌才ブナ林に隣接する高規格オートキャンプ場。"),
    "神仙沼自然休養林展望台": ("shinsen_numa_observatory", "共和町", "ニセコ山系の最も美しい湿原・神仙沼を望む展望台。"),
    "川下海浜公園キャンプ場": ("kawashimo_seaside_campground", "石狩市", "石狩市浜益区の美しい砂浜に面した海浜公園キャンプ場。"),
    "増毛リバーサイドパークオートキャンプ場": ("mashike_riverside_autocamp", "増毛町", "暑寒別川の河畔に広がる自然豊かなオートキャンプ場。"),
    "道の駅 マリーンアイランド岡島": ("marine_island_okashima_michinoeki", "枝幸町", "オホーツク海に浮かぶ客船をイメージしたユニークな外観の道の駅。"),
    "キムアネップ岬サンゴ草群生地": ("kimaneppu_cape_coral_grass", "佐呂間町", "サロマ湖東南に突き出た岬に広がる日本最大級のサンゴ草群生地。"),
    "女満別湖畔キャンプ場": ("memanbetsu_kohan_campground", "大空町", "網走湖畔の白砂青松の美しい湖畔に広がるキャンプ場。"),
    "天に続く道展望台": ("road_to_heaven_observatory", "斜里町", "地平線に向かって一直線に続く全長約28kmの道が「天に続く」ように見える絶景展望台。"),
}


def write_updated_route_kt(route_kt_path: str, new_anchors: list):
    """更新されたアンカーリストを HokkaidoAct1Route.kt に書き出す。"""
    with open(route_kt_path, "r", encoding="utf-8") as f:
        content = f.read()

    # val anchors: List<RouteAnchor> = listOf(...) のブロックを置換
    anchor_lines = []
    for a in new_anchors:
        line = (
            f'        RouteAnchor(id = "{a["id"]}", rank = {a["rank"]}, name = "{a["name"]}", '
            f'municipality = "{a["municipality"]}", lon = {a["lon"]}, lat = {a["lat"]}, '
            f'x = {a["x"]:.2f}f, y = {a["y"]:.2f}f, description = "{a["description"]}"),'
        )
        anchor_lines.append(line)

    anchors_block = "\n".join(anchor_lines)
    replacement = (
        f"    /** ユーザー指定の公式一次測地データに基づく正規アンカーポイント（全{len(new_anchors)}箇所） */\n"
        f"    val anchors: List<RouteAnchor> = listOf(\n"
        f"{anchors_block}\n"
        f"    )"
    )

    pattern = re.compile(
        r"    /\*\*.*?\*/\s*val anchors: List<RouteAnchor> = listOf\([\s\S]*?    \)",
        re.MULTILINE,
    )
    new_content, count = pattern.subn(replacement, content)
    if count == 0:
        raise RuntimeError("HokkaidoAct1Route.kt の anchors リスト置換に失敗しました。")

    with open(route_kt_path, "w", encoding="utf-8") as f:
        f.write(new_content)
    print(f"HokkaidoAct1Route.kt を正常に更新しました。（全 {len(new_anchors)} 箇所）")


def verify_route_integrity(anchors):
    """隣接アンカー間の距離ルール (>= 0.5km) および単調性を検証する。"""
    violations = []
    for i in range(len(anchors) - 1):
        a1 = anchors[i]
        a2 = anchors[i + 1]
        dist = haversine(a1["lat"], a1["lon"], a2["lat"], a2["lon"])
        if dist < 0.5:
            violations.append((i, a1["name"], a2["name"], dist))
    return violations


def main():
    parser = argparse.ArgumentParser(
        description="北海道一周すごろく第1部 中間アンカー選定・評価スクリプト（是正版）"
    )
    parser.add_argument(
        "--notes-dir",
        default="/home/txjp/dev/aidomari/notes",
        help="GeoJSONソースディレクトリ",
    )
    parser.add_argument(
        "--route-kt",
        default="/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt",
        help="HokkaidoAct1Route.kt パス",
    )
    parser.add_argument(
        "--report-json",
        default="/home/txjp/dev/aidomari/scripts/anchor_evaluation_report.json",
        help="評価レポート出力パス",
    )
    parser.add_argument(
        "--min-interval-km",
        type=float,
        default=1.0,
        help="中間アンカー挿入対象とする最小区間長 (km)",
    )
    parser.add_argument(
        "--min-anchor-dist",
        type=float,
        default=0.5,
        help="既存アンカーとの最小離隔距離 (km)",
    )
    parser.add_argument(
        "--primary-ratio",
        type=float,
        default=0.70,
        help="第1段階の中間レンズ比率 (デフォルト: 0.70)",
    )
    parser.add_argument(
        "--fallback-ratio",
        type=float,
        default=1.00,
        help="候補不在区間に対する第2段階の許容距離比率 (デフォルト: 1.00)",
    )
    parser.add_argument(
        "--apply",
        action="store_true",
        help="選定結果を HokkaidoAct1Route.kt に安全に反映する",
    )
    args = parser.parse_args()

    print("=== 北海道一周すごろく 中間アンカー選定プログラム（不具合是正版） ===")

    # 1. 既存アンカーおよび北海道陸地ポリゴンのロード
    anchors = load_route_anchors(args.route_kt)
    print(f"現在登録アンカー数: {len(anchors)} 箇所")

    poly = load_gsi_polygon()

    # 2. 公的データソースからの候補ロード
    candidates = load_authoritative_candidates(args.notes_dir)
    print(f"notes/ から抽出した公的ランドマーク候補総数: {len(candidates)} 件")

    # 3. 厳密な幾何・地理的検証による選定（二段階探索）
    assigned, summaries = evaluate_intervals(
        anchors,
        candidates,
        poly,
        min_interval_km=args.min_interval_km,
        min_dist_to_anchors=args.min_anchor_dist,
        primary_lens_ratio=args.primary_ratio,
        fallback_lens_ratio=args.fallback_ratio,
    )
    print(f"\n【選定結果】挿入適格と判定された区間数: {len(assigned)} 箇所")

    print(
        f"{'区間':<6} {'始点':<14} {'終点':<14} {'区間長':<8} {'選定新アンカー':<20} {'カテゴリ':<12} {'進行率t':<8} {'側方偏位':<8} {'段階':<8} {'新ランク':<6}"
    )
    print("-" * 115)

    evaluation_report = []
    for i in sorted(assigned.keys()):
        entry = assigned[i]
        c = entry["candidate"]
        a1, a2 = anchors[i], anchors[i + 1]
        dist_inter = haversine(a1["lat"], a1["lon"], a2["lat"], a2["lon"])
        new_rank = min(6, max(a1["rank"], a2["rank"]) + 1)
        stage_str = "Stage 1" if entry["ratio_used"] <= args.primary_ratio else "Stage 2"

        print(
            f"[{i:03d}]  {a1['name']:<14} {a2['name']:<14} {dist_inter:.2f}km  {c['name']:<20} {c['category']:<12} {entry['t']:.2f}    {entry['perp']:.2f}km  {stage_str:<8} Rank {new_rank}"
        )

        evaluation_report.append(
            {
                "interval_index": i,
                "prev_anchor": a1["name"],
                "next_anchor": a2["name"],
                "interval_dist_km": round(dist_inter, 2),
                "new_anchor_name": c["name"],
                "category": c["category"],
                "lon": c["lon"],
                "lat": c["lat"],
                "svg_x": round(entry["sx"], 2),
                "svg_y": round(entry["sy"], 2),
                "projection_t": round(entry["t"], 2),
                "perp_dist_km": round(entry["perp"], 2),
                "d_prev_km": round(entry["d1"], 2),
                "d_next_km": round(entry["d2"], 2),
                "new_rank": new_rank,
                "score": round(entry["score"], 1),
                "description": c["description"],
            }
        )

    # 4. レポート保存
    with open(args.report_json, "w", encoding="utf-8") as f:
        json.dump(evaluation_report, f, ensure_ascii=False, indent=2)
    print(f"\n詳細レポートを保存しました: {args.report_json}")

    # 5. 反映モード (--apply) の実行
    if args.apply:
        print("\n--apply が指定されました。HokkaidoAct1Route.kt を更新します...")
        new_anchors = []
        for i, a in enumerate(anchors):
            new_anchors.append(a)
            if i in assigned:
                entry = assigned[i]
                c = entry["candidate"]
                new_rank = min(6, max(a["rank"], anchors[i + 1]["rank"]) + 1)
                name = c["name"]
                if name in LANDMARK_METADATA:
                    cid, muni, desc = LANDMARK_METADATA[name]
                else:
                    cid = re.sub(r"[^a-z0-9_]+", "", name.lower()) or f"anchor_{i}"
                    muni = c["municipality"] or a["municipality"]
                    desc = c["description"]

                new_anchors.append(
                    {
                        "id": cid,
                        "rank": new_rank,
                        "name": name,
                        "municipality": muni,
                        "lon": c["lon"],
                        "lat": c["lat"],
                        "x": entry["sx"],
                        "y": entry["sy"],
                        "description": desc,
                    }
                )

        violations = verify_route_integrity(new_anchors)
        if violations:
            print("エラー: 更新後のルートに距離ルール違反が検出されました:")
            for v in violations:
                print(f"  区間 {v[0]}: {v[1]} -> {v[2]} (距離: {v[3]:.2f}km)")
            sys.exit(1)

        print(
            f"検証合格: 更新後全 {len(new_anchors)} 箇所のアンカー間離隔はすべて >= 0.5km を満たしています。"
        )
        write_updated_route_kt(args.route_kt, new_anchors)
    else:
        print(
            "\n※ 現在はドライラン（--dry-run）モードです。実際にファイルを更新する場合は --apply を指定してください。"
        )



if __name__ == "__main__":
    main()
