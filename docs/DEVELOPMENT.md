# Aidomari 開発・CI/CD ガイド

本プロジェクトの開発サイクル、コード規約、テスト、CI/CD パイプラインについての解説です。

---

## 1. 開発環境要件

- **JDK**: Java 17 (Temurin 推奨)
- **Android SDK**: compileSdk 35, minSdk 26
- **IDE**: Android Studio Ladybug (2024.2.1) 以降推奨
- **Gradle**: 8.10.2 (Gradle Wrapper 同梱)

---

## 2. 日常の開発サイクル（コマンド集）

### コードの自動整形（Spotless / ktlint）
コミット前にコードスタイルを自動適用します：
```bash
./gradlew spotlessApply
```
コードスタイルの検証のみを行う場合：
```bash
./gradlew spotlessCheck
```

### 単体テストの実行
すべての Unit Test を実行します：
```bash
./gradlew testDebugUnitTest
```
テスト結果レポートは `app/build/reports/tests/testDebugUnitTest/index.html` に出力されます。

### Android Lint の実行
静的解析を実行します：
```bash
./gradlew lintDebug
```
解析結果は `app/build/reports/lint-results-debug.html` に出力されます。

### デバッグ用 APK のビルド
ローカルで APK を生成します：
```bash
./gradlew assembleDebug
```
生成物: `app/build/outputs/apk/debug/app-debug.apk`

---

## 3. GitHub Actions CI/CD パイプライン

`.github/workflows/ci.yml` により、`main` ブランチへのプッシュおよび Pull Request 作成時に自動実行されます。

```mermaid
flowchart TD
    Trigger["git push / PR to main"] --> Quality["Job 1: Code Quality\n- spotlessCheck\n- lintDebug\n(HTMLレポート保存)"]
    Trigger --> Test["Job 2: Unit Tests\n- testDebugUnitTest\n(テスト結果保存)"]
    Quality --> Build["Job 3: Build Debug APK\n- assembleDebug\n(APK成果物アップロード)"]
    Test --> Build
```

### 自動アップロードされる成果物（Artifacts）
- **`aidomari-debug-apk`**: CIでビルドされたインストール可能な実機用 APK。GitHub のアクション実行画面からワンクリックでダウンロード可能です。
- **`lint-report`**: Android Lint の詳細 HTML レポート。
- **`test-results`**: 単体テストの詳細レポート。

---

## 4. 依存関係の管理（Version Catalog）

すべてのライブラリおよびプラグインのバージョンは、**`gradle/libs.versions.toml`** に集約されています。

```toml
[versions]
agp = "8.7.2"
kotlin = "2.0.21"
room = "2.6.1"
composeBom = "2024.10.01"
```

ライブラリの追加・更新は必ずこのファイルを経由して行います。

---

## 5. リポジトリ運用方針（Short-lived Feature Branch 戦略）

GitFlow のような `develop` や `release` などの肥大化しやすい中間層を全廃し、**「複数機能の並行開発」** と **「リリースの取捨選択（この機能は後回しなど）」** を最小の負荷で両立するブランチ戦略を採用します。

```mermaid
gitGraph
   commit id: "Initial setup"
   branch feat/importer
   checkout feat/importer
   commit id: "feat: JSONパース処理"
   checkout main
   branch feat/3d-flip
   checkout feat/3d-flip
   commit id: "ui: 3D回転アニメーション"
   commit id: "ui: スワイプ判定完成"
   checkout main
   merge feat/3d-flip id: "3Dフリップ完成！先にmainへ合流"
   checkout feat/importer
   commit id: "feat: インポートUI完成"
   checkout main
   commit id: "tag: v0.1.0 (3Dフリップ先行リリース)" tag: "v0.1.0"
   merge feat/importer id: "インポーター完成で合流"
   commit id: "tag: v0.2.0 (インポーター追加リリース)" tag: "v0.2.0"
```

### 運用ルール

1. **`main` ブランチ**:
   - 常に「ビルド可能で、テストがパスする」安定版。
   - 完成した機能のみをマージします。
2. **フィーチャーブランチ（`feat/*`, `fix/*`, `ui/*`）**:
   - 機能ごとに `main` からブランチを作成（例: `feat/room-db`, `feat/volume-key`, `feat/importer`）。
   - **並行開発**: ブランチを切り替えることで、複数の機能を互いに干渉せず並行して開発可能。
   - **リリースの取捨選択**: 「この機能は後回しにしよう」と判断した場合、マージせずブランチのまま寝かせておく。`main` は一切汚れません。
3. **Issue 連携とブランチ命名**:
   - 作業開始時はまず GitHub で Issue を起票（例: `#1: Room Database 実装`）。
   - ブランチ名に Issue 番号を含める: `feat/1-room-database`
   - コミットメッセージに Issue を記載: `feat: Subject, Deck Entity を追加 (refs #1)`
   - PR の説明文に `Closes #1` と記載 ➔ **マージ時に GitHub が Issue を自動で Closed にします**。
4. **PR（プルリクエスト）は「実機テスト用 APK を焼くスイッチ」**:
   - ブランチを push して GitHub で PR を開くと、GitHub Actions が自動で検証＆実機用 APK を生成。
   - スマホで APK をテストして問題なければ、GitHub 上で **[Squash and merge]** ボタンを押してマージ（ブランチは削除）。
5. **リリースとタグ打法**:
   - 節目となるバージョン（例: `v0.1.0`, `v0.2.0`）に達した際、`main` 上でタグを打って push：
     ```bash
     git tag v0.1.0
     git push --tags
     ```

---

## 6. シナリオテスト拡充方針（End-to-End・統合テスト）

### 背景と目的
単体テスト（Unit Test）では、ViewModel や Screen を高速にテストするために「Fake Repository」を使用しています。しかし、画面から発行されたデータ操作が、本物のデータベース（Room / SQLite）の外部キー制約（`CASCADE` 等）やスキーマとどのように結合するかまでは検証できません。
そのため、画面（Compose）、ナビゲーション（NavHost）、実データベース（In-Memory SQLite / Room）を一気通貫で結合した**統合シナリオテスト**（`app/src/test/java/com/aidomari/app/scenario/`）を導入しています。

### 機能追加・仕様変更時のテスト運用ルール
1. **想定シナリオの検証**:
   - 新機能の追加や仕様変更の設計段階で、「ユーザーの一連の利用シナリオが増減・変化するか」を必ず点検する。
2. **シナリオテストへの追加・更新**:
   - ユーザー操作の主要フローが増える場合、または重要な状態変化（アーカイブ、インポート、セッション復帰など）を伴う場合、`scenario/` 配下にシナリオテストを追加または更新することを必須とする。
3. **実DBの結合検証**:
   - 単に画面上の変化を確認するだけでなく、一連の操作後に本物の SQLite テーブル上でデータが欠損・意図しない連鎖削除を起こしていないか（整合性検証）までテスト内に含める。

---

## 7. Compose Preview 設計方針

### 目的
Android Studio 上でアプリを起動することなく UI の確認やデザイン参照が行えるよう、主要画面およびコンポーネントに Compose Preview を整備しています。

### 設計・実装ガイドライン
1. **ステートフル Screen とステートレス Content の分離**:
   - ViewModel や Activity Result Launcher に依存する Screen から、表示状態とコールバックのみを受け取るステートレスな Content コンポーザブル（例: `SubjectListContent`, `DeckListContent`）を切り出す。
2. **`@ThemePreviews` の活用**:
   - `com.aidomari.app.ui.theme.ThemePreviews` アノテーションを使用することで、Light モードと Dark モードを Android Studio プレビュー上で並べて同時に確認可能。
3. **`PreviewSampleData` の再利用**:
   - `com.aidomari.app.ui.preview.PreviewSampleData` に共通のプレビュー用モデルインスタンスを集約し、各プレビューで一貫性のある現実的なサンプルデータを使用する。
4. **初回起動用サンプルデータ（`SampleData`）**:
   - アプリを新規インストールした初回起動時、`AidomariDatabase` のコールバックにより自動的に基本サンプル教科・デッキ・カード（ルビ・タグ・TTS・Markdown対応）が初期投入され、初回ユーザーがすぐに操作感を体験できるようにしている。



