#!/usr/bin/env python3
"""
Generate and update HokkaidoAct1Route.kt with 266 curated anchors.
Preserves existing anchor lines exactly byte-for-byte, inserting the 98 new anchors between them.
"""

import re
import sys

sys.path.append('/home/txjp/dev/aidomari/scripts')
from mercator_projection import gps_to_svg
from evaluate_intermediate_anchors import CANDIDATES

# Explicit IDs for the 98 new intermediate candidates (keyed by interval 0..166)
CANDIDATE_IDS = {
    3: "goryokaku_station",
    4: "nanae_station",
    6: "junsainuma",
    7: "akaigawa_station",
    8: "oniushi_park",
    9: "ishikura_station",
    10: "nodaoi_station",
    14: "yamakoshi_station_site",
    15: "yamazaki_station",
    16: "nakanosawa_station",
    17: "fumatamata_station",
    18: "kuromatsunai_station",
    19: "utasai_beech_forest",
    20: "yubetsu_onsen",
    25: "hommoku_post_office",
    27: "chihase_fishing_port",
    29: "sukki_fishing_port",
    32: "sambonsugi_rocks",
    33: "minato_fishing_port",
    34: "benten_cape",
    35: "tachiokake_rock",
    38: "tomarinkan",
    41: "kamoenai_fishing_port",
    44: "sannai_fishing_port",
    45: "mazemon_arch",
    46: "yobetsu_fishing_port",
    48: "shakotan_town_office",
    53: "bikuni_port",
    54: "toyohama_tunnel_monument",
    57: "toyohama_fishing_port",
    58: "moire_cape",
    59: "michinoeki_space_apple_yoichi",
    60: "nitori_fruit_park",
    63: "shioya_station",
    64: "otaru_park",
    68: "minami_otaru_station",
    69: "asarigawa_onsen",
    70: "hariusu_station_site",
    71: "hoshioki_green_space",
    72: "zenibako_central_park",
    73: "ishikari_bentensha",
    74: "kotan_fishing_port",
    75: "goubishi_trailhead",
    76: "hamamasu_fishing_port_poro",
    77: "okumou_coast_observatory",
    81: "mashike_riverside_park",
    82: "hashibetsu_station_site",
    83: "afun_station_site",
    85: "obira_central_park",
    87: "onishika_port",
    88: "rikiru_port",
    89: "tomamae_town_office",
    90: "kotambetsu_river_park",
    91: "tomamae_yuuhigaoka_campground",
    94: "asahi_park_campground",
    95: "teshio_ariake_station_site",
    96: "shosanbetsu_port",
    97: "toyomisaki_station_site",
    98: "kyosei_fishing_port",
    104: "horonobe_hamasato_parking",
    105: "sarobetsu_wetland_center",
    106: "toyotomi_onsen",
    107: "sarobetsu_resthouse",
    108: "bakkai_rock",
    113: "minami_wakkanai_station",
    115: "soya_history_park",
    118: "soya_port",
    120: "mokeuni_marsh",
    121: "hamatonbetsu_onsen",
    122: "tonbetsu_fishing_port",
    123: "menashidomari_fishing_port",
    126: "otohyo_fishing_port",
    127: "motoineppu_fishing_port",
    128: "sawaki_fishing_port",
    129: "okkoppe_park",
    130: "rurochi_fishing_port",
    131: "shokotsu_station_site",
    134: "motomombetsu_station_site",
    135: "shibunotsunai_lake",
    136: "nakayubetsu_station_memorial",
    137: "gokayama_park",
    138: "toppushi_fishing_port",
    140: "saroma_glasswort_habitat",
    141: "saromako_ikoinomori",
    143: "tokoro_port",
    144: "notoro_fishing_port",
    145: "notoro_lakeside_park",
    146: "misaki_fishing_port",
    148: "abashiri_station",
    149: "masuura_station",
    150: "mokoto_station",
    151: "yamubetsu_station",
    152: "koshimizu_town_office",
    164: "makkausu_cave",
}

def update_file():
    kt_path = '/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt'
    with open(kt_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    # Find the anchors section
    anchor_line_indices = []
    anchor_ranks = []
    for idx, line in enumerate(lines):
        m = re.search(r'^\s*RouteAnchor\(id = "([^"]+)", rank = (\d+), name = "([^"]+)"', line)
        if m:
            anchor_line_indices.append(idx)
            anchor_ranks.append(int(m.group(2)))

    assert len(anchor_line_indices) == 168, f"Expected 168 existing anchors, found {len(anchor_line_indices)}"

    # We will build output lines
    new_lines = []
    current_existing_idx = 0

    for idx, line in enumerate(lines):
        if current_existing_idx < len(anchor_line_indices) and idx == anchor_line_indices[current_existing_idx]:
            new_lines.append(line)
            # Check if an intermediate anchor should be inserted after this existing anchor
            if current_existing_idx in CANDIDATES and CANDIDATES[current_existing_idx] is not None:
                cname, cmuni, ctype, clon, clat, cdesc = CANDIDATES[current_existing_idx]
                cid = CANDIDATE_IDS[current_existing_idx]
                prev_rank = anchor_ranks[current_existing_idx]
                next_rank = anchor_ranks[current_existing_idx + 1]
                rank = max(prev_rank, next_rank) + 1
                sx, sy = gps_to_svg(clon, clat)
                new_anchor_line = f'        RouteAnchor(id = "{cid}", rank = {rank}, name = "{cname}", municipality = "{cmuni}", lon = {clon}, lat = {clat}, x = {sx:.2f}f, y = {sy:.2f}f, description = "{cdesc}"),\n'
                new_lines.append(new_anchor_line)
            current_existing_idx += 1
        else:
            # Update comment
            if '/** 公式一次測地データに基づくアンカーポイント' in line:
                line = '    /** 公式一次測地データに基づくアンカーポイント（Rank 1主要拠点: 67, Rank 2中間拠点: 68, Rank 3詳細拠点: 80, Rank 4追加拠点: 47：全262箇所） */\n'
            new_lines.append(line)

    with open(kt_path, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)

    print(f"Updated {kt_path} successfully. Total anchors: {168 + len(CANDIDATE_IDS)}.")

if __name__ == '__main__':
    update_file()
