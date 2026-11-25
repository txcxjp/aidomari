# Aidomari Web Ingest（ブラウザ拡張機能）

Web ブラウザで開いている記事の本文を取り出し、端末やローカル環境で動いている Aidomari アプリ（`http://127.0.0.1:8877`）へ送って、Gemini AI による自動デッキ作成へ繋げるための WebExtension（Manifest V3）です。

Firefox for Android、Firefox Desktop、および Chrome / Chromium 系ブラウザ（PC）で動作します。

---

## 主な機能

- **ワンクリック送信**: 開いているページの記事本文を抽出してローカルの Aidomari へ送信します。
- **本文の整理**: ナビゲーション、広告、SNS ボタン、コメント欄などの不要要素を省き、本文の見出しや段落を取り出します（`extractor.js`）。
- **右クリックメニュー送信**:
  - ページ全体を送信（`このページをAidomariへ送信`）
  - 選択範囲だけを送信（`選択したテキストをAidomariへ送信`）
- **ローカル完結**: 外部の中継サーバーを使わず、ブラウザと Aidomari 間のローカル通信（デフォルト `127.0.0.1:8877`）でやり取りします。

---

## ファイル構成

```text
extensions/aidomari-browser/
├── manifest.json            # Manifest V3 定義（Firefox / Chrome 対応）
├── background.js            # サービスワーカー / 右クリックメニュー / 送信処理
├── content_script.js        # ページ内の抽出スクリプト呼び出し
├── extractor.js             # 本文抽出・不要要素除外ロジック
├── popup/
│   ├── popup.html           # ポップアップ UI
│   ├── popup.css            # スタイル
│   └── popup.js             # ポップアップ制御・設定
├── icons/                   # 拡張機能アイコン
├── test/
│   └── test_extractor.js    # 抽出ロジックの単体テスト（Node.js）
├── generate_icons.py        # アイコン生成スクリプト
└── README.md                # 本ドキュメント
```

---

## 導入手順

### 1. Firefox Desktop（PC）
1. Firefox のアドレスバーに `about:debugging#/runtime/this-firefox` を入力して開きます。
2. 「一時的なアドオンを読み込む...」をクリックします。
3. `extensions/aidomari-browser/manifest.json` を選択します。

### 2. Firefox for Android（スマートフォン）
1. PC と Android 端末を USB デバッグで接続し、PC 側 Firefox の `about:debugging` から対象端末を選び `manifest.json` を読み込みます（一時導入）。
2. 常時利用する場合は、Firefox Nightly / Beta for Android の開発者メニューから AMO のカスタムアドオンコレクションを設定して導入できます。

### 3. Google Chrome / Chromium 系（PC）
1. `chrome://extensions/` を開きます。
2. 右上の「デベロッパーモード」をオンにします。
3. 「パッケージ化されていない拡張機能を読み込む」から `extensions/aidomari-browser` フォルダを選びます。

---

## 使い方

1. **Aidomari アプリを起動する**: ポート `8877` でローカル受信サーバーが待機します。
2. **ブラウザで記事を開く**: 技術記事、解説ページ、ニュースなどを開きます。
3. **拡張機能アイコンから送信**: 接続状態を確認し、送信ボタンを押します。
4. **Aidomari でデッキ化する**: アプリ内の「自動デッキ作成」画面に受信した記事が表示されるので、カードを作成して登録します。

---

## 受信設定

ポップアップ下部の「受信設定 (ローカル連携)」を開くと、送信先を変更できます。

- **Aidomari エンドポイント**:
  - デフォルト: `http://127.0.0.1:8877`
  - PC ブラウザから同一 LAN 内の Android 実機へ送る場合は、実機のローカル IP アドレス（例: `http://192.168.1.100:8877`）を指定できます。
- **接続テスト**: Aidomari アプリへの接続可否と未処理記事件数を確認できます。

---

## 単体テストの実行

Node.js が入っていれば追加インストールなしでテストを実行できます。

```bash
node extensions/aidomari-browser/test/test_extractor.js
```

主な検証項目：
- OGP / Twitter メタタグや Canonical URL からのタイトル・URL 取得
- サイト名サフィックス（` | サイト名` 等）の整形
- ナビゲーション、広告、コメント欄などの不要要素の除外
- 見出し（H1〜H6）、リスト、段落構造の保持
- 文字数カウントとカード作成目安枚数の計算
