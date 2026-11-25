# 相泊（Aidomari）

自分なりの工夫やメモを残しながら使い続けられる、Android 向けの単語帳・学習アプリです。  
以前の Words アプリ（Elm + Cordova）を置き換えるかたちで、Kotlin と Jetpack Compose を使って作り直しています。

アプリ名の「相泊（あいどまり）」は、知床半島の道路の終点にある地名に由来します。

---

## このアプリが大切にしていること

暗記カードアプリの中には、復習スケジュールをすべてシステム側で決めてしまうものもありますが、相泊では「自分でどう覚えるか工夫すること」や「その日の気分や状況に合わせて学び方を選べること」を重視しています。

- **その日に合った学び方を選べる**: パート単位の演習、ブックマークしたカードの復習、昨日や先週のカード、ランダム出題などから選んで学習できます。
- **カードに自分のメモを書き加えられる**: 「ことば」と「説明」だけでなく、出題時の自分向けクイズや補足メモを残せます。
- **無理なく続けられる仕組み**: 画面点灯時に1問だけ確認するロック画面学習や、学習時間に応じて少しずつ地図を進めるすごろく要素を用意しています。

---

## 主な機能

### 1. 教科・デッキ・カード管理
- **自動パート分割**: 枚数の多いデッキも、設定した枚数（デフォルト50枚）ごとのパートに自動で区切って進められます。
- **出題の向き**: 「ことば → 説明」と「説明 → ことば」を対等に切り替えられます。
- **各種学習モード**: 苦手カードのブックマーク集約、過去の復習デッキ（昨日・先週など）、ランダム出題、一時期休ませるスヌーズ（「休眠」）、学習カレンダーを備えています。
- **中断からの再開**: 途中の学習キューややり直し対象はセッションとして保存され、あとから続きに戻れます。

### 2. カード演習と操作
- **フリップとスワイプ**: タップまたは横ドラッグで裏返し、右スワイプで「覚えた」、左スワイプで「もう一度」に振り分けます。
- **音量キーでの操作**: 表面では音量上下でカードをめくり、裏面では音量下で「覚えた」、音量上で「もう一度」を選べます。片手持ちや移動中でも操作できます。
- **関連項目リンク**: 説明文の中に同じ教科の別カードの「ことば」が含まれていると自動でリンクになり、学習画面を閉じずに詳細シートをたどって確認できます。
- **音声読み上げ（TTS）**: 教科ごとの言語設定に合わせた自動・手動の読み上げと速度調整に対応しています。

### 3. 自動デッキ作成（Web 記事 × Gemini AI）
- **共有メニュー・ブラウザ拡張からの取り込み**: Android の共有メニューや、同梱のブラウザ拡張機能（[extensions/aidomari-browser](extensions/aidomari-browser/README.md)）から読んだ Web 記事をアプリ内のローカル受信サーバー（`127.0.0.1:8877`）へ送れます。
- **AI によるカード候補の作成**: 設定した Gemini API キーを使って、記事から「ことば」「説明」「補足メモ」の候補を作成します（自動判定 / 要旨重視 / 用語網羅）。
- **確認と編集**: 作成された候補を取捨選択・編集し、出題の向き（デフォルトは「説明 → ことば」）や保存先デッキを指定して登録できます。

### 4. ロック画面学習・テーマ・すごろくマップ
- **ロック画面学習**: 画面を点けた際に復習対象のカードを1問表示できます。上方向へスワイプするとそのまま閉じられます。
- **カラーテーマ**: 5種類の配色テーマから好みに合わせて選べます。
- **すごろくマップ**: 学習した時間に応じて北海道のルート上を少しずつ進み、各地点への到着を楽しめます。

### 5. インポート / エクスポート
- Aidomari 独自の JSON 形式のほか、旧 Words アプリ（Elm 版 V1 / V2、CSV 変換 JSON）の読み込み・バックアップ書き出しに対応しています。

---

## デバッグ版 APK

[`release/app-debug.apk`](release/app-debug.apk) に最新ビルドのデバッグ APK を置いています。

---

## 技術構成

- **言語・UI**: Kotlin 2.0.21 / Compose Multiplatform 1.7.3（Android & Desktop JVM）
- **モジュール構成**:
  - `:shared-core`: ドメインモデル、計算ロジック、Gemini API クライアント、インポーター/エクスポーター、関連項目エンジン、ローカル受信サーバー等の純粋 Kotlin/JVM モジュール
  - `:shared-ui`: 教科・デッキ・カード管理、学習画面、自動デッキ作成、カレンダー、テーマ設定などの共有 Compose Multiplatform UI / ViewModel（Android / Desktop）
  - `:app`: Android エントリポイント、Room DB、Android TTS、ロック画面学習、共有受信、すごろくマップ
  - `:desktopApp`: Windows / デスクトップ向け JVM エントリポイント、SQLite 永続化、デスクトップファイル選択／音声読み上げ
- **データベース**: Room 2.6.1（Android / スキーマバージョン 10）、sqlite-jdbc（Desktop / 同一スキーマ）
- **非同期処理**: Kotlin Coroutines & Flow
- **品質・テスト**: Spotless (ktlint), Android Lint, JUnit4, Robolectric, Compose UI Test
- **CI**: GitHub Actions（フォーマット検証・Android Lint・単体テスト・デバッグ APK / Windows デスクトップビルド）

---

## 開発者向けクイックスタート

```bash
# リポジトリの取得
git clone https://github.com/txcxjp/aidomari.git
cd aidomari

# フォーマットの適用
./gradlew spotlessApply

# 単体テストの実行（共通・デスクトップ・Android）
./gradlew :shared-core:test :desktopApp:test testDebugUnitTest

# デスクトップアプリを起動（PC ローカル動作確認）
./gradlew :desktopApp:run

# デバッグ APK / デスクトップ配布パッケージのビルド
./gradlew assembleDebug
./gradlew :desktopApp:createDistributable
```

---

## ドキュメント

- [アーキテクチャ設計書 (docs/ARCHITECTURE.md)](docs/ARCHITECTURE.md)
- [詳細設計書 (docs/DETAILED_DESIGN.md)](docs/DETAILED_DESIGN.md)
- [開発・CI/CD ガイド (docs/DEVELOPMENT.md)](docs/DEVELOPMENT.md)
- [ブラウザ拡張機能 README (extensions/aidomari-browser/README.md)](extensions/aidomari-browser/README.md)
