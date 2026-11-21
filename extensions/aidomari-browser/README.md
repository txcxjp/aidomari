# Aidomari Web Ingest (ブラウザ拡張機能)

Aidomari公式のブラウザ拡張機能（WebExtension Manifest V3）です。  
ブラウザで閲覧しているWebページの記事本文を抽出し、端末内で動作するAidomariアプリ（`http://127.0.0.1:8877`）へワンクリックで送信して、Gemini AIによる自動単語デッキ化を行います。

Firefox for Android (スマートフォン)、Firefox Desktop、および Chrome / Chromium系ブラウザ（PC）に完全対応しています。

---

## 主な特徴

- 🚀 **ワンクリック高速送信**: 閲覧中の記事を瞬時に抽出し、ローカルのAidomariへ転送
- 🧹 **クリーンな本文抽出**: 広告、SNSシェアボタン、ナビゲーション、コメント欄を自動除外する軽量Readabilityエンジンを内蔵
- 🖱️ **コンテキストメニュー対応**:
  - ページ上で右クリック → `📥 このページをAidomariへ送信`
  - 選択した文章のみを右クリック → `📥 選択したテキストをAidomariへ送信`（記事の一部分だけを暗記したい時に便利）
- 🔒 **100% プライバシー & サーバーレス**: クラウド中継サーバーなし。ブラウザとAidomariアプリ間の `127.0.0.1:8877` ローカル通信のみで完結

---

## ディレクトリ構成

```text
extensions/aidomari-browser/
├── manifest.json            # Manifest V3 定義 (Firefox / Chrome 両対応)
├── background.js           # サービスワーカー / 右クリックメニュー / 送信制御
├── content_script.js       # ページ内コンテンツスクリプト
├── extractor.js            # スタンドアロン本文抽出・ノイズ除去エンジン
├── popup/
│   ├── popup.html          # モバイル・PC両対応 ポップアップUI
│   ├── popup.css           # Material / Aidomari Indigo スタイル
│   └── popup.js            # ポップアップ制御 & 設定管理
├── icons/                  # 拡張機能アイコン (16x16, 48x48, 128x128, SVG)
├── test/
│   └── test_extractor.js   # 抽出ロジックの単体テスト (Node.js)
├── generate_icons.py       # アイコン生成スクリプト
└── README.md               # 本ドキュメント
```

---

## インストール手順

### 1. Firefox Desktop (PC)
1. Firefox のアドレスバーに `about:debugging#/runtime/this-firefox` を入力して開きます。
2. **「一時的なアドオンを読み込む...」** ボタンをクリックします。
3. `extensions/aidomari-browser/manifest.json` を選択します。
4. ツールバーに Aidomari アイコンが表示されます。

### 2. Firefox for Android (スマートフォン)
Firefox for Android (Fenix) で拡張機能を読み込む方法は2種類あります：

#### 方法 A: USBデバッグ経由 (開発・即時確認用)
1. PCとAndroid端末をUSBケーブルで接続し、開発者向けオプションの「USBデバッグ」を有効にします。
2. PCのFirefoxで `about:debugging` を開き、左サイドバーの「USBデバイス」から接続中のAndroidを選択します。
3. Android版Firefoxの「一時的なアドオンを読み込む...」から本拡張機能の `manifest.json` を指定してロードします。

#### 方法 B: カスタムアドオンコレクション (常用向け)
1. [addons.mozilla.org (AMO)](https://addons.mozilla.org/) でアカウントを作成し、マイコレクションを作成します。
2. Firefox Nightly または Firefox Beta for Android の「設定」→「Firefoxについて」でロゴを複数回タップして開発者メニューを解放します。
3. 「カスタムアドオンコレクション」にAMOのユーザーIDとコレクション名を入力することで、Android上のFirefoxで常時利用可能になります。

### 3. Google Chrome / Chromium 系 (PC: Chrome, Brave, Edge, Vivaldi)
1. アドレスバーに `chrome://extensions/` を入力して開きます。
2. 画面右上の **「デベロッパーモード」** をONにします。
3. 画面左上の **「パッケージ化されていない拡張機能を読み込む」** をクリックします。
4. `extensions/aidomari-browser` フォルダを選択します。

---

## 使い方

1. **Aidomari アプリを起動する**:
   - Android端末で Aidomari アプリを起動します（バックグラウンドまたはフォアグラウンド）。
   - ポート `8877` でローカル受信サーバーが自動待機します。
2. **ブラウザで読みたい記事を開く**:
   - ニュース記事、技術ブログ、論文、解説記事などを閲覧します。
3. **拡張機能アイコンをクリック**:
   - ポップアップが開き、記事タイトル、ドメイン、文字数、AIカード想定枚数が表示されます。
   - ステータスが `🟢 接続中` になっていることを確認し、**「📥 Aidomariへ送信」** をタップします。
4. **Aidomari アプリでデッキ化**:
   - Aidomari アプリの「本日の閲覧デッキ化」を開くと、受信した記事がリストアップされています。
   - 「AIカード生成」を実行すると、Gemini APIにより自動的に問題と解説のフラッシュカードが生成されます。

---

## 設定のカスタマイズ

ポップアップ下部の **「⚙️ 受信設定 (ローカル連携)」** をタップして展開すると、以下の設定が行えます：

- **Aidomari エンドポイント**:
  - デフォルト: `http://127.0.0.1:8877`
  - PCブラウザから別端末のAndroid実機へ送信したい場合は、Android実機のローカルIPアドレス（例: `http://192.168.1.100:8877`）を指定することも可能です。
- **接続テスト**:
  - ボタンをタップして、Aidomariアプリとの疎通状態や未処理の記事数を確認できます。

---

## 単体テストの実行

Node.js（追加ライブラリのインストール不要）で即座にテストを実行できます：

```bash
node extensions/aidomari-browser/test/test_extractor.js
```

以下のテストが自動検証されます：
- ✅ OGPタイトル、Twitterタイトル、Canonical URLの正確な抽出
- ✅ サイト名サフィックス（` | サイト名` 等）の自動トリム
- ✅ ナビゲーション、広告、SNS共有ボタン、コメント欄、サイドバー、フッターの確実な除外
- ✅ 見出し（H1〜H6）、リスト、段落のMarkdown構造化保持
- ✅ 文字数カウントおよび想定AIカード生成枚数の計算
