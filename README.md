# 相泊（Aidomari）

自分なりの工夫やメモを手入れしながら使い続けられる、Android・Windows 向けの単語帳・学習アプリです。  
以前の Words アプリ（Elm + Cordova）を置き換えるかたちで、Kotlin と Compose Multiplatform を使って作り直しています。

アプリ名の「相泊（あいどまり）」は、知床半島・羅臼側の道路の終点にある地名に由来します。

---

## このアプリが大切にしていること

暗記アプリの中には、復習の順番やスケジュールをすべてシステム側で決めてしまうものもありますが、相泊では「どう覚えるかを自分で工夫すること」や「その日の気分や生活リズムに合わせて学び方を選べること」を大切にしています。

- **その日の自分のペースで切り口を選べる**: パート単位の演習、ブックマークしたカードの復習、昨日・先週・先月のカード、ランダム出題などから、やりたい学び方を選べます。
- **自分のメモを書き加えて育てられる**: 「ことば」と「説明」だけでなく、自分向けの問いかけや補足メモを残せます。
- **日常の導線にそっと馴染む**: スマホでは片手操作や満員電車での音声・音量キー演習、画面点灯時のロック画面学習を、PC ではキーボードを使った落ち着いた演習や Web 記事の整理を行えます。
- **自動処理はあくまで下ごしらえ**: Web 記事からカード候補を作る機能もありますが、何を取り込み、どう書き直して覚えるかを決めるのは常に使い手自身です。

---

## 主な機能

### 1. 教科・デッキ・カード管理（Android / Desktop）
- **パート分割**: 枚数の多いデッキも、設定した枚数（デフォルト 50 枚）ごとのパートに分けて無理なく進められます。
- **出題の向き**: 「ことば → 説明」と「説明 → ことば」を対等に切り替えられます。
- **学習の切り口と休眠**: ブックマーク集約、過去の復習デッキ（昨日・先週・先月）、ランダム出題、一時的に非表示にするスヌーズ（「休眠」）、学習カレンダーを備えています。
- **続きから再開**: 途中の学習キューややり直し対象はセッションとして保存され、中断した箇所から再開できます。

### 2. カード演習と操作（Android / Desktop）
- **フリップと振り分け**: カードをめくり、「覚えた」「もう一度」に振り分けます（モバイルではタップ／スワイプと音量キー、PC では `→` / `←` / `Space` / `Enter` のキーボード操作に対応）。
- **関連項目リンク**: 説明文の中に同じ教科の別カードの「ことば」が含まれていると自動でリンクになり、学習画面を離れずにたどって確認できます。
- **音声読み上げ（TTS）**: 教科ごとの言語設定に合わせた自動・手動の読み上げと速度調整に対応しています（Android TTS / Windows SAPI）。

### 3. Web 記事からの自動デッキ作成（Android / Desktop）
- **共有メニュー・ブラウザ拡張からの持ち帰り**: Android の共有メニューや、同梱のブラウザ拡張機能（[extensions/aidomari-browser](extensions/aidomari-browser/README.md)）から、読んだ Web 記事をアプリ内のローカル受信サーバー（`127.0.0.1:8877`）へ送れます。
- **カード候補の作成と編集**: 設定した Gemini API キーを使い、記事から「ことば」「説明」「補足メモ」の候補を作成します（自動判定 / 要旨重視 / 用語網羅）。登録前に内容を見直して取捨選択・編集できます。

### 4. ロック画面学習・テーマ・すごろくマップ
- **カラーテーマ（Android / Desktop）**: ライト／ダーク表示と 5 種類の配色テーマ（藍・常磐・桜・琥珀・墨）を選べます。
- **ロック画面学習（Android）**: 画面を点けた際に、一度学習した復習対象のカードを 1 問表示できます。上方向へスワイプするとすぐ閉じられます。
- **すごろくマップ（Android）**: 学習した時間に応じて北海道のルート上を少しずつ進み、各地点への到着を楽しめます（Desktop 版は今後の課題として #105 で計画中）。

### 5. インポート / エクスポート（Android / Desktop）
- Aidomari 独自の JSON 形式のほか、旧 Words アプリ（Elm 版 V1 / V2、CSV 変換 JSON）の読み込み・バックアップ書き出しに対応しています。

---

## ビルド成果物

- **Android**: [`release/app-debug.apk`](release/app-debug.apk) にデバッグ APK を置いています（最新ビルドは GitHub Actions の `aidomari-debug-apk` からも取得できます）。
- **Windows (Desktop)**: GitHub Actions の `build-desktop-windows` ジョブで生成される `aidomari-windows-desktop` アーティファクト（展開して `Aidomari.exe` を実行）から利用できます。

---

## 技術構成

- **言語・UI**: Kotlin 2.0.21 / Compose Multiplatform 1.7.3（Android & Desktop JVM）
- **モジュール構成**:
  - `:shared-core`: ドメインモデル、計算ロジック、Gemini API クライアント、インポーター／エクスポーター、`KeywordEngine`、`LocalIngestServer` 等の純粋 Kotlin/JVM モジュール
  - `:shared-ui`: 教科・デッキ・カード管理、学習画面、自動デッキ作成、カレンダー、テーマ設定などの共有 Compose Multiplatform UI / ViewModel（Android / Desktop）
  - `:app`: Android エントリポイント、Room DB、Android TTS、ロック画面学習、共有受信、すごろくマップ
  - `:desktopApp`: Windows / デスクトップ向け JVM エントリポイント、SQLite 永続化、デスクトップファイル選択／音声読み上げ
  - `extensions/aidomari-browser`: Chrome / Firefox 向けブラウザ拡張機能（Manifest V3）
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

# フォーマットの適用・検証
./gradlew spotlessApply spotlessCheck --no-daemon --max-workers=1

# 単体テストの実行（共通・デスクトップ・Android）
./gradlew :shared-core:test :desktopApp:test :app:testDebugUnitTest --no-daemon --max-workers=1

# デスクトップアプリを起動（PC ローカル動作確認）
./gradlew :desktopApp:run --no-daemon --max-workers=1

# デバッグ APK / デスクトップ配布パッケージのビルド
./gradlew assembleDebug --no-daemon --max-workers=1
./gradlew :desktopApp:createDistributable --no-daemon --max-workers=1
```

---

## ドキュメント

- [アーキテクチャ設計書 (docs/ARCHITECTURE.md)](docs/ARCHITECTURE.md)
- [詳細設計書 (docs/DETAILED_DESIGN.md)](docs/DETAILED_DESIGN.md)
- [開発・CI/CD ガイド (docs/DEVELOPMENT.md)](docs/DEVELOPMENT.md)
- [ブラウザ拡張機能 README (extensions/aidomari-browser/README.md)](extensions/aidomari-browser/README.md)
