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
