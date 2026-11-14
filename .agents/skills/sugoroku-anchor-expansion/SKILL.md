---
name: sugoroku-anchor-expansion
description: >-
  Use this skill whenever adding, evaluating, or updating intermediate anchors
  for the Hokkaido Sugoroku Act 1 route in Aidomari. Enforces user-defined selection
  rules, authoritative data source (notes/ GeoJSON), opposite-side nearest anchor
  rule, 70% lens ratio with 100% fallback, 500m spacing, lateral penalty, scoring,
  and rank determination without simplified polygon check.
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
   - **★ 反対側最近傍アンカー則（Opposite-Side Nearest Anchor Rule）**:
     1. **第1最近傍判定**: 新アンカー候補地 $C$ から見て、全既存アンカーの中で最も近いアンカー $P_1$ を探索する。$P_1$ は必ず元となった区間の端点（$A$ または $B$）のいずれかでなければならない。
     2. **反対側半空間での最近傍判定**: $C$ を挟んで $P_1$ の反対側（ベクトル内積 $\vec{CP_1} \cdot \vec{CQ} < 0$ となる半空間）に存在する既存アンカー群の中で、最も近いアンカー $P_2$ を探索する。$P_2$ は必ず元となった区間のもう一方の端点でなければならない。
     ※ 旧「最寄り2地点ルール」では一直線上に並ぶ中間点（桔梗駅など）が後続区間の短さゆえに不当に失格していた幾何学的欠陥を解消。同時に、鋭角・直角に脱線する迂回候補（$\angle ACB \le 90^\circ$）や湾を跨いだショートカットを数学的に厳密に排除する。
   - **★ 離隔ルール**: その端点および全既存アンカーとは、それぞれ〇キロ（例: 0.5km）以上離れていなければならない。
   - **★ 距離比率制約（70%レンズ則 ＆ 候補不在時のフォールバック100%）**:
     - 基本は **70%レンズ則**（$AC \le 0.70 \times AB$ かつ $BC \le 0.70 \times AB$）。区間の中央寄り（30%〜70%の範囲内）に絞り込み、山奥や対岸・端点偏在スポットを排除する。
     - 70%ルールで適切な候補が得られない区間に限って、**最大100%まで緩和**（$AC \le 1.00 \times AB$ かつ $BC \le 1.00 \times AB$）して探索し、偏在している実在拠点（道の駅や駅跡等）を救出・補完する。
   - **★ 陸地判定（ポリゴン判定撤廃）**: 
     新アンカー候補はすべて `notes/*.geojson` の実在する公的ランドマークから直接取得し、「反対側最近傍アンカー則」と「側方偏位（回廊）ペナルティ」によって海峡越え等の幾何学的逸脱が防がれているため、SVGポリゴン内外判定は撤廃する（軽量化SVGの簡略化誤差によって、実在する沿岸の岬や海沿いの道の駅・キャンプ場が誤って除外されるのを防止するため。※地図SVG自体の高精度化は後日実施）。

4. **複数候補がある場合の選定（重みづけと中間地点）**:
   - できるだけ中間地点に近い部分が都合が良いが、中間地点に無い場合はその限りではない。
   - 複数候補がある場合の総合スコアリング（カテゴリ魅力度 + 中間地点らしさ + 側方偏位ペナルティ）：
     - 観光資源: **95点**
     - 景勝地（岬、灯台、展望台、原生花園など）: **90点**
     - 温泉・キャンプ場: **80点**
     - 道の駅・鉄道駅: **75点**
     - 公園・記念碑: **75点**
     - その他公的スポット: **70点**
     - 漁港: **30点**
     - **スコア計算式**:
       $$\text{Score} = \text{カテゴリ点} - 25.0 \times |t - 0.5| - 5.0 \times \left(\frac{\text{側方偏位(km)}}{\max(1.0, \text{区間長(km)})}\right) \times 10.0$$
       - 区間中央（$t=0.5$）に近いほど加点。
       - ルートの進行直線からの側方偏位（横ブレ）が大きいほど減点し、道路回廊に沿った正当なスポットを最優先。

5. **新アンカーのランク決定**:
   - 新しく追加するアンカーのランクは、**もとになったアンカーのうち大きいほうに1を足した数字**になる。
   - $\text{新ランク} = \max(\text{元アンカー1のランク}, \text{元アンカー2のランク}) + 1$

---

## 運用ワークフローとタグ基準点

### 基準タグ
- `route-v0-empty`: アンカー0個の完全白紙状態（コミット `ecb6864`）
- `route-v1-initial-41`: ユーザー指定の初期41アンカー（Rank 1）確定状態（コミット `4feb080`）

### ルール変更時のリビルド手順（確定ワークフロー）
選定ルールや幾何アルゴリズム、重みづけに変更が生じた場合は、既存の拡張ノードの上に継ぎ足すのではなく、**必ず初期アンカー基準タグに戻ってクリーンに再構築**します：

1. **初期アンカー基準タグからの新ブランチ作成**:
   ```bash
   git checkout route-v1-initial-41 -b feat/issue-68-clean-anchor-rebuild-vX
   ```
2. **ルール・評価プログラム・スキルの変更コミット**:
   `scripts/evaluate_intermediate_anchors.py`、`test_evaluate_intermediate_anchors.py`、`SKILL.md` を更新してコミット。
3. **ラウンド制による順次拡張**:
   Round 1（Rank 2）、Round 2（Rank 3）……と段階的に `--apply` してテストを通過させ、各ラウンドごとにコミット。

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
