#!/usr/bin/env python3
"""
scripts/test_evaluate_intermediate_anchors.py

是正版中間アンカー選定プログラム（evaluate_intermediate_anchors.py）の網羅的妥当性検証テストスイート
"""

import math
import os
import sys
import unittest

sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from evaluate_intermediate_anchors import (
    evaluate_intervals,
    haversine,
    is_authorized_landmark,
    load_authoritative_candidates,
    load_route_anchors,
    project_to_segment,
)
from fast_polygon import load_gsi_polygon


class TestGeometryMath(unittest.TestCase):
    """幾何学・測地計算の妥当性テスト"""

    def test_haversine_accuracy(self):
        # 札幌駅 (43.0686, 141.3508) 〜 函館駅 (41.7738, 140.7264)
        # 国土地理院測地線長はおよそ 151.8km
        dist = haversine(43.0686, 141.3508, 41.7738, 140.7264)
        self.assertTrue(
            150.0 < dist < 155.0, f"Expected ~152km between Sapporo and Hakodate, got {dist:.2f}km"
        )

    def test_project_to_segment_midpoint(self):
        # 経度差 1.0度の東西線分の中点
        lat_a, lon_a = 44.0, 142.0
        lat_b, lon_b = 44.0, 143.0
        lat_c, lon_c = 44.0, 142.5
        t, perp = project_to_segment(lat_a, lon_a, lat_b, lon_b, lat_c, lon_c)
        self.assertAlmostEqual(t, 0.5, places=3)
        self.assertAlmostEqual(perp, 0.0, places=2)

    def test_project_to_segment_backward_reversal(self):
        # 点Cが始点Aより手前にある（逆走）
        lat_a, lon_a = 44.0, 142.0
        lat_b, lon_b = 44.0, 143.0
        lat_c, lon_c = 44.0, 141.8
        t, perp = project_to_segment(lat_a, lon_a, lat_b, lon_b, lat_c, lon_c)
        self.assertLess(t, 0.0, f"Expected t < 0 for backward point, got {t}")

    def test_project_to_segment_forward_overshoot(self):
        # 点Cが終点Bより先にある（行き過ぎ）
        lat_a, lon_a = 44.0, 142.0
        lat_b, lon_b = 44.0, 143.0
        lat_c, lon_c = 44.0, 143.2
        t, perp = project_to_segment(lat_a, lon_a, lat_b, lon_b, lat_c, lon_c)
        self.assertGreater(t, 1.0, f"Expected t > 1 for overshot point, got {t}")

    def test_project_to_segment_perpendicular_distance(self):
        # 線分から北へ緯度0.05度（約5.5km）オフセット
        lat_a, lon_a = 44.0, 142.0
        lat_b, lon_b = 44.0, 143.0
        lat_c, lon_c = 44.05, 142.5
        t, perp = project_to_segment(lat_a, lon_a, lat_b, lon_b, lat_c, lon_c)
        self.assertAlmostEqual(t, 0.5, places=3)
        self.assertTrue(
            5.0 < perp < 6.0, f"Expected perpendicular distance ~5.5km, got {perp:.2f}km"
        )


class TestLandmarkFilter(unittest.TestCase):
    """認可ランドマーク種別判定の妥当性テスト"""

    def test_authorized_landmarks(self):
        valid_landmarks = [
            "道の駅 てしお",
            "藻琴駅",
            "旧大椴駅跡",
            "宗谷岬",
            "稚内灯台",
            "小平町望洋台キャンプ場",
            "羅臼温泉野営場",
            "黒松内温泉ぶなの森",
            "昭和公園",
            "小清水原生花園",
            "セタカムイ岩",
            "オシンコシンの滝",
            "マッカウス洞窟",
        ]
        for name in valid_landmarks:
            self.assertTrue(
                is_authorized_landmark(name), f"Should be accepted: {name}"
            )

    def test_unauthorized_and_bogus_names(self):
        invalid_names = [
            "大字更岸",
            "字美岬",
            "海岸町",
            "展望台",
            "展望所",
            "駐車場",
            "トイレ",
            "案内板",
            "ベンチ",
            "国道229号",
            "道道106号",
            "ab",
        ]
        for name in invalid_names:
            self.assertFalse(
                is_authorized_landmark(name), f"Should be rejected: {name}"
            )


class TestHistoricalBugRegressions(unittest.TestCase):
    """過去にバグとして混入した誤座標・逆走ケースの回帰防止テスト"""

    def test_yamubetsu_station_rejected_for_koshimizu_segment(self):
        """止別駅が小清水原生花園〜はなやか小清水間に誤挿入されないことの検証"""
        # 原生花園 -> はなやか小清水
        lat_a, lon_a = 43.9419, 144.4134
        lat_b, lon_b = 43.9339, 144.4533
        # 実在の止別駅 (浜小清水より東に位置)
        lat_c, lon_c = 43.9106, 144.5312
        t, perp = project_to_segment(lat_a, lon_a, lat_b, lon_b, lat_c, lon_c)
        # t > 1.0 のため、区間 (A, B) の中間として絶対に選ばれない
        self.assertGreater(t, 1.0)

    def test_bogus_rikiru_port_rejected(self):
        """力昼港（山奥8.1km誤座標）が過大な側方偏位および t < 0 により排除されることの検証"""
        lat_a, lon_a = 44.3090, 141.6568  # とままえ温泉
        lat_b, lon_b = 44.3680, 141.7010  # 羽幌港
        lat_c, lon_c = 44.1500, 141.7800  # 旧・山奥誤座標
        t, perp = project_to_segment(lat_a, lon_a, lat_b, lon_b, lat_c, lon_c)
        d_inter = haversine(lat_a, lon_a, lat_b, lon_b)
        max_allowed_perp = min(5.0, 0.35 * d_inter)
        self.assertGreater(
            perp,
            max_allowed_perp,
            f"Perp {perp}km should exceed limit {max_allowed_perp}km",
        )

    def test_bogus_onishika_port_rejected(self):
        """鬼鹿港（山奥7.1km誤座標）が過大な側方偏位により排除されることの検証"""
        lat_a, lon_a = 44.0535, 141.6668  # 旧大椴駅跡
        lat_b, lon_b = 44.1578, 141.6600  # 旧鬼鹿駅跡
        lat_c, lon_c = 44.0800, 141.7500  # 旧・山奥誤座標
        t, perp = project_to_segment(lat_a, lon_a, lat_b, lon_b, lat_c, lon_c)
        d_inter = haversine(lat_a, lon_a, lat_b, lon_b)
        max_allowed_perp = min(5.0, 0.35 * d_inter)
        self.assertGreater(
            perp,
            max_allowed_perp,
            f"Perp {perp}km should exceed limit {max_allowed_perp}km",
        )


class TestOverallSelectionIntegrity(unittest.TestCase):
    """現行 319 アンカーに対する選定処理全体の妥当性・整合性テスト"""

    def setUp(self):
        base_dir = "/home/txjp/dev/aidomari"
        self.route_kt = os.path.join(
            base_dir,
            "app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt",
        )
        self.notes_dir = os.path.join(base_dir, "notes")
        self.anchors = load_route_anchors(self.route_kt)
        self.candidates = load_authoritative_candidates(self.notes_dir)
        self.poly = load_gsi_polygon()

    def test_candidates_source_is_authentic(self):
        self.assertGreater(len(self.candidates), 1000)

    def test_evaluation_produces_zero_distance_violations(self):
        assigned, summaries = evaluate_intervals(
            self.anchors, self.candidates, self.poly
        )

        # 1区間最大1件であること
        self.assertTrue(len(assigned) <= len(self.anchors) - 1)

        # 仮想挿入した全区間の離隔をチェック
        simulated_anchors = []
        for i, a in enumerate(self.anchors):
            simulated_anchors.append(a)
            if i in assigned:
                c = assigned[i]["candidate"]
                simulated_anchors.append(c)

        for i in range(len(simulated_anchors) - 1):
            a1 = simulated_anchors[i]
            a2 = simulated_anchors[i + 1]
            dist = haversine(a1["lat"], a1["lon"], a2["lat"], a2["lon"])
            self.assertGreaterEqual(
                dist,
                0.5,
                f"Distance violation between {a1['name']} and {a2['name']}: {dist:.2f}km < 0.5km",
            )

    def test_selected_candidates_authentic_and_non_empty(self):
        assigned, _ = evaluate_intervals(
            self.anchors, self.candidates, self.poly
        )
        self.assertGreater(len(assigned), 0)
        # ポリゴン判定は撤廃（実在の沿岸・岬スポットがSVG簡略化ポリゴン外れで除外されるのを防ぐため）
        for i, entry in assigned.items():
            c = entry["candidate"]
            self.assertTrue(is_authorized_landmark(c["name"]))


if __name__ == "__main__":
    unittest.main(verbosity=2)
