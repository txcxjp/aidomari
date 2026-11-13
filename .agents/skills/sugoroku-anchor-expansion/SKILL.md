---
name: sugoroku-anchor-expansion
description: >-
  Use this skill whenever adding, evaluating, or updating intermediate anchors
  for the Hokkaido Sugoroku Act 1 route in Aidomari. Enforces user-defined selection
  rules, authoritative data source (notes/ GeoJSON), closest-two-anchors isolation,
  spacing, land polygon check, scoring, and rank determination.
---

# 北海道一周すごろく 第1部 アンカー追加手順（公式スキル）

北海道一周すごろく（Aidomari Act 1）のルートに対して、新しい中間アンカーを追加・選定・反映するための公式手順書です。

## 基本方針とゴールデンルール

ユーザー様によって定義された確定仕様です。一切の例外を認めず遵守してください。

1. **データソース**:
   - `notes/` 配下の公的・実在 GeoJSON（道の駅、鉄道駅、観光資源、OSM実地スポット）から直接取得する。
   - 外部検索やAIの推測による地名・座標の捏造（ハルシネーション）は絶対に禁止。

2. **対象区間とラウンド制（多重挿入の厳禁）**:
   - あるアンカーと隣のアンカーの間が〇キロ以上（例: 500m離隔を保つため 1.0km 以上）ある区間を対象とする。
   - その2点に近い別のアンカー候補地を、**1区間につき1つだけ**選ぶ。
   - **あくまで既存アンカーと既存アンカーの間に選ぶこと**。
   - 同一ラウンド内で、既存アンカーとアンカー候補地の間、あるいはアンカー候補地とアンカー候補地の間を重ねて選ぶのは禁止。
   - 既存アンカー2点間の間に適切なスポットがない場合は、**その間にはアンカーを作らない（スキップ）**。

3. **位置・幾何条件**:
   - **★ 最寄り2地点ルール**: 新アンカーの候補地から見て、全既存アンカーの中で一番近いものと2番目に近いそれは、必ず元となった2地点であること（Uターン逆走や対岸への飛び地を防止）。
   - **★ 離隔ルール**: その一番近い2地点とは、それぞれ〇キロ（例: 0.5km）以上離れていなければならない。
   - **★ 距離比率制約（70%レンズ則 ＆ 候補不在時のフォールバック100%）**:
     - 基本は **70%レンズ則**（$AC \le 0.70 \times AB$ かつ $BC \le 0.70 \times AB$）。区間の中央寄り（30%〜70%の範囲内）に絞り込み、山奥や対岸・端点偏在スポットを排除する。
     - 70%ルールで適切な候補が得られない区間に限って、**最大100%まで緩和**（$AC \le 1.00 \times AB$ かつ $BC \le 1.00 \times AB$）して探索し、偏在している実在拠点（道の駅や駅跡等）を救出・補完する。
   - **陸地判定**: 新たにつくるアンカーは必ず陸地（国土地理院ポリゴン内 `inside = True`）であること。

4. **複数候補がある場合の選定（重みづけと中間地点）**:
   - できるだけ中間地点に近い部分が都合が良いが、中間地点に無い場合はその限りではない。
   - 複数候補がある場合は、旅行で行きたくなる魅力度・インパクトを考慮した重みづけを参考にする：
     - 観光資源: **95点**
     - 景勝地（岬、灯台、展望台、原生花園など）: **90点**
     - 温泉・キャンプ場: **80点**
     - 道の駅・鉄道駅: **75点**
     - 公園・記念碑: **75点**
     - その他公的スポット: **70点**
     - 漁港: **30点**

5. **新アンカーのランク決定**:
   - 新しく追加するアンカーのランクは、**もとになったアンカーのうち大きいほうに1を足した数字**になる。
   - $\text{新ランク} = \max(\text{元アンカー1のランク}, \text{元アンカー2のランク}) + 1$

---

## 実行手順とコマンド

### 1. 評価スクリプトのドライラン
```bash
python3 scripts/evaluate_intermediate_anchors.py
```
出力された [anchor_evaluation_report.json](file:///home/txjp/dev/aidomari/scripts/anchor_evaluation_report.json) を確認し、選定結果をレビューする。

### 2. ルートへの反映
```bash
python3 scripts/evaluate_intermediate_anchors.py --apply
```
[HokkaidoAct1Route.kt](file:///home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt) に該当する新アンカーが挿入される。

### 3. 品質検証パイプラインの実行
```bash
./gradlew spotlessApply && ./gradlew testDebugUnitTest && ./gradlew recordRoborazziDebug && ./gradlew lintDebug assembleDebug
```
すべてのテスト・スナップショット・静的解析・ビルドが成功することを確認する。

### 4. コミット & プッシュ
Git に変更をコミットし、リモートへプッシュして GitHub Actions CI の成功を確認する。
