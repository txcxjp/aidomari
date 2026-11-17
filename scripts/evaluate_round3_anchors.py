#!/usr/bin/env python3
"""
[DEPRECATED / 廃止]
このスクリプトは、AIの推測記憶に基づくハードコード辞書と単調性チェックの欠落により、
誤った座標（力昼港、鬼鹿港、止別駅など）や進行方向の逆走を引き起こした旧世代のスクリプトです。

現在は是正版の scripts/evaluate_intermediate_anchors.py に完全統合されています。
そちらをご使用ください。
"""
import sys
import subprocess
import os

print("[警告] evaluate_round3_anchors.py は廃止されました。是正版 scripts/evaluate_intermediate_anchors.py を実行します。")
script_dir = os.path.dirname(os.path.abspath(__file__))
target = os.path.join(script_dir, "evaluate_intermediate_anchors.py")
sys.exit(subprocess.call([sys.executable, target] + sys.argv[1:]))
