#!/usr/bin/env python3
"""
[DEPRECATED / 廃止]
このスクリプトは、旧世代のハードコードされたラウンド3用挿入スクリプトです。
現在は是正版の scripts/evaluate_intermediate_anchors.py (--apply オプション) に完全統合されています。
"""
import sys
import subprocess
import os

print("[警告] generate_round3_route.py は廃止されました。是正版 scripts/evaluate_intermediate_anchors.py --apply を実行します。")
script_dir = os.path.dirname(os.path.abspath(__file__))
target = os.path.join(script_dir, "evaluate_intermediate_anchors.py")
sys.exit(subprocess.call([sys.executable, target, "--apply"] + sys.argv[1:]))
