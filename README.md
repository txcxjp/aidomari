# Aidomari（相泊）

> 知床の最果て「相泊」の名を冠した、Android 専用の次世代ネイティブ単語帳アプリ。  
> 旧 Words アプリ（Elm + Cordova）を完全刷新し、Kotlin + Jetpack Compose で再構築されたプロジェクトです。

---

## 📚 ドキュメント

- **[アーキテクチャ設計書 (docs/ARCHITECTURE.md)](docs/ARCHITECTURE.md)**: ドメインモデル、状態遷移、音量キー・スワイプ仕様、関連項目エンジン
- **[開発・CI/CD ガイド (docs/DEVELOPMENT.md)](docs/DEVELOPMENT.md)**: 開発サイクル、コマンド集、Spotless、GitHub Actions パイプライン

---

## 🌟 主な特徴

- **複数教科（Subject）対応**: 英語、資格試験、世界史など、複数科目を切り替えて学習管理。
- **3Dフリップ ＆ Tinder風スワイプ**: 表面から裏面への立体的な裏返りと、裏面からの左右スワイプ（右＝学習済み、左＝あとでもう一度）による軽快な操作感。
- **音量キー操作**:
  - 表面: 音量Up / Down でカード裏返り
  - 裏面: 音量Down で右スワイプ（学習済み）、音量Up で左スワイプ（リトライ）
- **関連項目（Keyword）エンジン**: カードテキスト内の単語から同一教科内の他カードへ自動リンクを生成。辞書のように知識を数珠つなぎで探索可能。
- **50問自動分割 ＆ Auto-Continue**: 大規模なデッキを自動でPart分割し、完了時に自動で次のPartへシームレスに継続。
- **セッション永続化（続きから再開）**: いつ中断しても直前の問題位置とリトライキューを保持。
- **旧データ互換**: 旧 Words アプリのエクスポートデータ（V1, V2, CSV-JSON形式）のインポートに対応。
- **ネイティブ音声読み上げ（TTS）**: 教科ごとの言語設定に応じた自動/手動発音。

---

## 🚀 クイックスタート

```bash
# クローン
git clone https://github.com/txcxjp/aidomari.git
cd aidomari

# コードフォーマット
./gradlew spotlessApply

# 単体テスト実行
./gradlew testDebugUnitTest

# デバッグAPKビルド
./gradlew assembleDebug
```

---

## 🛠 技術スタック

- **言語**: Kotlin 2.0.21
- **UI フレームワーク**: Jetpack Compose (Material 3, Compose BOM 2024.10.01)
- **データベース**: Room 2.6.1 (SQLite + KSP)
- **非同期・状態管理**: Kotlin Coroutines & Flow, ViewModel
- **コード品質**: Spotless + ktlint, Android Lint
- **CI/CD**: GitHub Actions (Lint, Test, Debug APK 自動ビルド・配布)
