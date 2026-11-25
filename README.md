# 相泊（Aidomari）

自分なりの工夫やメモを手入れしながら使い続けられる、Android・Windows 向けの単語帳アプリです。

アプリ名の「相泊（あいどまり）」は、知床半島・羅臼側の道路の終点にある地名に由来します。

---

## このアプリが大切にしていること

相泊は、「どう覚えたら身につくか」を自分で考えながら使い続けられる単語帳アプリです。  
その日の気分や生活リズムに合わせて、自分のペースで学び方を選べます。

---

## 主な特徴

### 満員電車でも、画面を見ずに進められる（Android）
音声の読み上げを聞きながら、スマホの音量ボタンだけでカードをめくれます。  
「覚えた」「もう一度」の振り分けも音量ボタンで行えます。  
画面を覗き込めない満員電車の中でも、片手のままテンポよく復習できます。

### 画面を点けた瞬間に、1問だけ振り返る（Android）
わざわざアプリを開かなくても、スマホの画面を点けたときに、かつて覚えたカードを1問だけ表示できます。  
急いでいるときは、上にスワイプすればすぐ閉じられます。

### PC ではキーボードで落ち着いて進められる（Windows）
Windows 版では、キーボードの左右キーだけでカードをめくって振り分けられます。  
大きな画面で長い解説を読んだり、手持ちのカードを整理したりしながら、自分のペースで進められます。

### 一息で回せる分量に区切り、切り口を選べる
収録枚数が多い単語帳でも、自分が無理なく一周できる枚数ごとに区切って進められます。  
中断しても、次はその続きからすぐに再開できます。  
「苦手なカードだけ」「少し前に学んだカード」「ランダム」など、その日の状況に合う出し方を選べます。  
しばらく見直さなくてよいカードは、一時的に手元から外しておくこともできます。

### 自分なりの覚え方を書き込み、言葉どうしをたどる
問いと答えのどちらを先に表示するか、自分のやり方に合わせて切り替えられます。  
カードには、自分なりのヒントや語呂合わせのメモを自由に書き残せます。  
解説の中に同じ教科の別の言葉が登場したときは、学習を止めずにそのまま関連先のカードを確認できます。

### 読んだ Web 記事を自分の単語帳に持ち帰る（Android / Windows）
普段読んでいる記事の中で手元に残したいページがあれば、スマホの共有メニューやブラウザ拡張機能（[extensions/aidomari-browser](extensions/aidomari-browser/README.md)）からアプリへ送れます。  
送った記事をもとに、あとから単語カードの候補を作れます。  
登録する前に候補を見直し、必要なものだけを選んで手直ししながら単語帳に加えられます。

### 積み重ねを振り返り、無理なく続ける
学習の記録をカレンダーで振り返ることができます。  
Android 版では、学習に応じて北海道の地図の上をキャラクターが歩きます。  
画面の配色テーマは好みに合わせて切り替えられます。  
バックアップ用のファイルで、データを書き出したり読み込んだりもできます。

---

## 入手・ビルド成果物

- **Android**: [`release/app-debug.apk`](release/app-debug.apk) にデバッグ APK を置いています（最新ビルドは GitHub Actions の `aidomari-debug-apk` からも取得できます）。
- **Windows (Desktop)**: GitHub Actions の `build-desktop-windows` ジョブで生成される `aidomari-windows-desktop` アーティファクト（展開して `Aidomari.exe` を実行）から利用できます。

---

## 技術構成（開発者向け）

- **言語・UI**: Kotlin 2.0.21 / Compose Multiplatform 1.7.3（Android & Desktop JVM）
- **モジュール構成**:
  - `:shared-core`: ドメインモデル、計算ロジック、データ入出力、Gemini API クライアント等の純粋 Kotlin/JVM モジュール
  - `:shared-ui`: Android / Desktop で共有する Compose Multiplatform UI と ViewModel
  - `:app`: Android エントリポイント、Room DB、音声読み上げ、ロック画面学習、共有受信、北海道マップ
  - `:desktopApp`: Windows / デスクトップ向け JVM エントリポイント、SQLite 永続化、ファイル選択、音声読み上げ
  - `extensions/aidomari-browser`: Chrome / Firefox 向けブラウザ拡張機能（Manifest V3）
- **データベース**: Room 2.6.1（Android）、sqlite-jdbc（Desktop）
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
