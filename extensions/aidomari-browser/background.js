/**
 * Aidomari Background Script / Service Worker
 * - コンテキストメニューによるワンクリック送信
 * - Aidomariローカルサーバー (127.0.0.1:8877) へのデータ送信
 * - 接続ステータスチェック & アイコンバッジ通知
 */

const DEFAULT_ENDPOINT = 'http://127.0.0.1:8877';

// 設定からエンドポイントを取得
async function getEndpoint() {
  return new Promise((resolve) => {
    chrome.storage.local.get({ endpointUrl: DEFAULT_ENDPOINT }, (items) => {
      let url = items.endpointUrl || DEFAULT_ENDPOINT;
      if (url.endsWith('/')) {
        url = url.slice(0, -1);
      }
      resolve(url);
    });
  });
}

// Aidomariサーバー接続確認
async function checkServerStatus() {
  try {
    const endpoint = await getEndpoint();
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 2000);

    const resp = await fetch(`${endpoint}/api/status`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' },
      signal: controller.signal
    });
    clearTimeout(timeoutId);

    if (resp.ok) {
      const data = await resp.json();
      return { ok: true, data };
    }
    return { ok: false, error: `HTTP ${resp.status}` };
  } catch (err) {
    return { ok: false, error: err.message || '接続できませんでした' };
  }
}

// 記事データをAidomariサーバーに送信
async function sendArticleToAidomari({ url, title, content, source = 'browser_extension' }) {
  try {
    const endpoint = await getEndpoint();
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 5000);

    const resp = await fetch(`${endpoint}/api/ingest`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json; charset=utf-8',
        'Accept': 'application/json'
      },
      body: JSON.stringify({ url, title, content, source }),
      signal: controller.signal
    });
    clearTimeout(timeoutId);

    if (resp.ok) {
      const data = await resp.json();
      showBadge('OK', '#4CAF50'); // 緑
      return { success: true, data };
    } else {
      const errorText = await resp.text();
      showBadge('ERR', '#F44336'); // 赤
      return { success: false, error: `サーバーエラー (${resp.status}): ${errorText}` };
    }
  } catch (err) {
    showBadge('ERR', '#F44336'); // 赤
    return { success: false, error: `送信に失敗しました: ${err.message}` };
  }
}

// アイコンバッジの一時表示
function showBadge(text, color) {
  try {
    chrome.action.setBadgeText({ text });
    chrome.action.setBadgeBackgroundColor({ color });
    setTimeout(() => {
      chrome.action.setBadgeText({ text: '' });
    }, 3500);
  } catch (e) {
    console.warn('[Aidomari] Badge update error:', e);
  }
}

// 初期化: コンテキストメニューの作成
chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.removeAll(() => {
    // ページ全体送信
    chrome.contextMenus.create({
      id: 'aidomari-send-page',
      title: '📥 このページをAidomariへ送信',
      contexts: ['page']
    });

    // 選択テキスト送信
    chrome.contextMenus.create({
      id: 'aidomari-send-selection',
      title: '📥 選択したテキストをAidomariへ送信',
      contexts: ['selection']
    });
  });
});

// コンテキストメニューのクリックハンドラー
chrome.contextMenus.onClicked.addListener(async (info, tab) => {
  if (!tab || !tab.id) return;

  if (info.menuItemId === 'aidomari-send-selection') {
    const selectedText = (info.selectionText || '').trim();
    if (!selectedText) return;

    await sendArticleToAidomari({
      url: tab.url || '',
      title: tab.title || '選択テキスト',
      content: selectedText,
      source: 'selection'
    });
  } else if (info.menuItemId === 'aidomari-send-page') {
    // ページから本文抽出
    try {
      // extractor.js と content_script.js を実行（先頭スラッシュで拡張機能ルートからのパスを明示）
      await chrome.scripting.executeScript({
        target: { tabId: tab.id },
        files: ['/extractor.js', '/content_script.js']
      });

      chrome.tabs.sendMessage(tab.id, { action: 'extract' }, async (response) => {
        if (chrome.runtime.lastError || !response || !response.success) {
          console.error('[Aidomari] Extract message error:', chrome.runtime.lastError || response?.error);
          showBadge('ERR', '#F44336');
          return;
        }

        const article = response.article;
        await sendArticleToAidomari({
          url: article.url || tab.url,
          title: article.title || tab.title,
          content: article.content,
          source: 'context_menu'
        });
      });
    } catch (e) {
      console.error('[Aidomari] Context menu injection failed:', e);
      showBadge('ERR', '#F44336');
    }
  }
});

// ポップアップ等からのメッセージリレー
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === 'checkStatus') {
    checkServerStatus().then(sendResponse);
    return true; // 非同期レスポンス
  }

  if (request.action === 'sendArticle') {
    sendArticleToAidomari(request.payload).then(sendResponse);
    return true; // 非同期レスポンス
  }
});
