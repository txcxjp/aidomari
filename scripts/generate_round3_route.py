#!/usr/bin/env python3
"""
Generate and update HokkaidoAct1Route.kt with 342 verified anchors.
Preserves existing 262 anchors byte-for-byte, inserting the 80 verified new anchors.
"""

import re
import sys

sys.path.append('/home/txjp/dev/aidomari/scripts')
from mercator_projection import gps_to_svg
from evaluate_intermediate_anchors import haversine
from evaluate_round3_anchors import CANDIDATES_R3

CANDIDATE_IDS_R3 = {
    4: "showa_park",
    5: "onakayama_station",
    6: "nanae_history_museum",
    8: "higurashiyama_observatory",
    10: "onuma_seminar_house",
    11: "higashi_onuma_campground",
    12: "higashi_mori_station",
    14: "moto_ishikura_station_site",
    15: "otoshibe_fishing_port",
    21: "yakumo_breeding_farm",
    22: "kuroiwa_station",
    26: "oshamambe_town_center",
    29: "kuromatsunai_onsen_bunanomori",
    30: "toit_vert",
    33: "shubuto_river_park",
    34: "suttsu_oiso_fishing_port",
    38: "utajima_fishing_port",
    39: "sakaehama_fishing_port",
    41: "atsuse_fishing_port",
    49: "setana_general_branch_office",
    53: "raiden_onsen_site",
    59: "moiwa_onsen",
    60: "sakazuki_campground",
    74: "bikuni_onsen",
    81: "furuhira_family_travel_village",
    96: "oshoro_stone_circle",
    97: "shioya_beach",
    105: "shinko_central_park",
    106: "asari_dam_memorial",
    109: "hoshimi_station",
    112: "ishikari_discharge_channel",
    115: "mourai_disaster_prevention_station",
    116: "atsuta_port",
    117: "anbase_fishing_port",
    120: "kawashimo_beach_facility",
    121: "hamamasu_horonai_fishing_port",
    125: "shirogane_cascade",
    130: "nobusha_station_site",
    133: "obira_nishin_banya",
    135: "hanaoka_campground",
    137: "onishika_flower_park",
    141: "kotambetsu_station_site",
    145: "tomamae_port",
    149: "tsukibetsu_river_park",
    150: "ariake_dam_park",
    151: "sakaehama_post_office",
    152: "shosanbetsu_nature_center",
    154: "shosanbetsu_village_office",
    156: "embetsugawa_kasen_park",
    158: "embetsu_asahi_onsen",
    165: "horonobe_visitor_center",
    166: "pankenumako_observatory",
    167: "toyotomi_town_office",
    171: "bakkai_post_office",
    178: "onuma_wild_bird_observatory",
    179: "koetoi_post_office",
    180: "soyamisaki_shrine",
    187: "asachino_station_site",
    192: "shanai_fishing_port",
    193: "toitoi_fishing_port",
    194: "usutaibe_campground",
    198: "okajima_fishing_port",
    199: "oumu_horonai_fishing_port",
    201: "hinode_cape_campground",
    203: "saruru_fishing_port",
    206: "saruru_beach",
    207: "tomioka_post_office",
    208: "tokkari_center",
    211: "monbetsu_airport",
    212: "komuke_campground",
    214: "yubetsu_town_office",
    216: "saromako_observatory",
    224: "horoiwayama_observatory",
    231: "ubaranai_traffic_park",
    232: "ubaranai_post_office",
    236: "abashiri_prison_museum",
    244: "kiyosato_fureai_center",
    258: "minehama_fishing_port",
}

def update_route_kt():
    kt_path = '/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt'
    with open(kt_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    anchor_line_indices = []
    anchor_ranks = []
    for idx, line in enumerate(lines):
        m = re.search(r'^\s*RouteAnchor\(id = "([^"]+)", rank = (\d+), name = "([^"]+)"', line)
        if m:
            anchor_line_indices.append(idx)
            anchor_ranks.append(int(m.group(2)))

    assert len(anchor_line_indices) == 262, f"Expected 262 existing anchors, found {len(anchor_line_indices)}"

    new_lines = []
    current_existing_idx = 0

    inserted_count = 0
    rank_counts = {1: 0, 2: 0, 3: 0, 4: 0, 5: 0}

    for idx, line in enumerate(lines):
        if current_existing_idx < len(anchor_line_indices) and idx == anchor_line_indices[current_existing_idx]:
            new_lines.append(line)
            prev_rank = anchor_ranks[current_existing_idx]
            rank_counts[prev_rank] += 1

            if current_existing_idx in CANDIDATE_IDS_R3:
                cand = CANDIDATES_R3[current_existing_idx]
                assert cand is not None
                cname, cmuni, ctype, clon, clat, cdesc = cand
                cid = CANDIDATE_IDS_R3[current_existing_idx]
                next_rank = anchor_ranks[current_existing_idx + 1]
                rank = max(prev_rank, next_rank) + 1
                sx, sy = gps_to_svg(clon, clat)
                new_anchor_line = f'        RouteAnchor(id = "{cid}", rank = {rank}, name = "{cname}", municipality = "{cmuni}", lon = {clon}, lat = {clat}, x = {sx:.2f}f, y = {sy:.2f}f, description = "{cdesc}"),\n'
                new_lines.append(new_anchor_line)
                inserted_count += 1
                rank_counts[rank] += 1

            current_existing_idx += 1
        else:
            if '/** 公式一次測地データに基づくアンカーポイント' in line:
                line = '    /** 公式一次測地データに基づくアンカーポイント（Rank 1主要拠点: 67, Rank 2中間拠点: 68, Rank 3詳細拠点: 96, Rank 4追加拠点: 76, Rank 5詳細拠点: 35：全342箇所） */\n'
            new_lines.append(line)

    assert inserted_count == 78, f"Expected 78 inserted anchors, got {inserted_count}"

    with open(kt_path, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)

    print(f"Updated {kt_path} successfully. Total anchors: {262 + inserted_count}.")
    print(f"Rank distribution: {rank_counts}")

if __name__ == '__main__':
    update_route_kt()
