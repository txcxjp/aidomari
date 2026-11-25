/**
 * Aidomari Popup Script
 */
document.addEventListener('DOMContentLoaded', async () => {
  const connectionStatusEl = document.getElementById('connection-status');
  const statusLabelEl = connectionStatusEl.querySelector('.status-label');

  const extractingView = document.getElementById('extracting-view');
  const previewView = document.getElementById('preview-view');
  const errorView = document.getElementById('error-view');
  const errorMessageEl = document.getElementById('error-message');

  const articleDomainEl = document.getElementById('article-domain');
  const articleCharsEl = document.getElementById('article-chars');
  const articleCardsEl = document.getElementById('article-cards');
  const articleTitleEl = document.getElementById('article-title');
  const articleExcerptEl = document.getElementById('article-excerpt');

  const sendBtn = document.getElementById('send-btn');
  const resultMessageEl = document.getElementById('result-message');
  const retryBtn = document.getElementById('retry-extract-btn');

  const endpointInput = document.getElementById('endpoint-input');
  const saveEndpointBtn = document.getElementById('save-endpoint-btn');
  const pingBtn = document.getElementById('ping-btn');

  let currentArticle = null;
  let activeTab = null;

  // 1. 設定の読み込み
  chrome.storage.local.get({ endpointUrl: 'http://127.0.0.1:8877' }, (items) => {
    endpointInput.value = items.endpointUrl || 'http://127.0.0.1:8877';
  });

  // 2. サーバー接続状態チェック
  async function refreshServerStatus() {
    connectionStatusEl.className = 'status-chip checking';
    statusLabelEl.textContent = '確認中...';

    chrome.runtime.sendMessage({ action: 'checkStatus' }, (response) => {
      if (chrome.runtime.lastError || !response || !response.ok) {
        connectionStatusEl.className = 'status-chip offline';
        statusLabelEl.textContent = '未接続 (Aidomari未起動)';
      } else {
        connectionStatusEl.className = 'status-chip online';
        const pendingCount = response.data?.pendingCount || 0;
        statusLabelEl.textContent = pendingCount > 0 ? `接続中 (${pendingCount}件未処理)` : '接続中';
      }
    });
  }

  // 3. アクティブタブから記事を抽出
  async function extractActiveTabArticle() {
    extractingView.classList.remove('hidden');
    previewView.classList.add('hidden');
    errorView.classList.add('hidden');
    resultMessageEl.classList.add('hidden');

    try {
      const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
      activeTab = tab;

      const internalSchemes = ['chrome://', 'chrome-extension://', 'edge://', 'about:', 'moz-extension://', 'resource://'];
      if (!tab || !tab.id || internalSchemes.some((scheme) => tab.url?.startsWith(scheme))) {
        showError('ブラウザ内部ページからは記事を抽出できません。Webページを開いてください。');
        return;
      }

      // ドメイン名表示
      try {
        const urlObj = new URL(tab.url);
        articleDomainEl.textContent = urlObj.hostname;
      } catch (e) {
        articleDomainEl.textContent = 'Webページ';
      }

      // content_script.js と extractor.js を注入（Firefoxでは先頭スラッシュがないとpopup/配下として解決される）
      await chrome.scripting.executeScript({
        target: { tabId: tab.id },
        files: ['/extractor.js', '/content_script.js']
      });

      chrome.tabs.sendMessage(tab.id, { action: 'extract' }, (response) => {
        if (chrome.runtime.lastError || !response || !response.success) {
          console.error('[Aidomari Popup] Extract response error:', chrome.runtime.lastError || response?.error);
          showError(response?.error || 'ページから本文を抽出できませんでした。');
          return;
        }

        currentArticle = response.article;
        displayArticlePreview(currentArticle);
      });
    } catch (err) {
      console.error('[Aidomari Popup] Injection failed:', err);
      showError('ページスクリプトの実行に失敗しました: ' + err.message);
    }
  }

  // プレビュー表示
  function displayArticlePreview(article) {
    extractingView.classList.add('hidden');
    errorView.classList.add('hidden');
    previewView.classList.remove('hidden');

    articleTitleEl.textContent = article.title || activeTab?.title || '無題の記事';
    articleCharsEl.textContent = `${article.charCount.toLocaleString()}文字`;
    articleCardsEl.textContent = `AIカード: 約${article.estimatedCards}枚`;
    articleExcerptEl.textContent = article.excerpt || article.content.slice(0, 150) || '本文なし';

    // 本文が空または極端に短い場合の警告
    if (article.charCount < 50) {
      articleCardsEl.textContent = '文字数少';
      articleCardsEl.className = 'badge';
    } else {
      articleCardsEl.className = 'badge badge-accent';
    }

    // プライバシー除外チェックの反映
    if (article.isExcluded) {
      sendBtn.disabled = true;
      sendBtn.innerHTML = '<span class="btn-icon">⚠️</span> <span class="btn-label">除外対象ページ</span>';
      resultMessageEl.className = 'result-message error';
      resultMessageEl.innerHTML = `🛡️ <strong>プライバシー除外:</strong> ${article.exclusionReason || '検索結果や認証ページのため送信できません'}`;
      resultMessageEl.classList.remove('hidden');
    } else {
      sendBtn.disabled = false;
      sendBtn.innerHTML = '<span class="btn-icon">📥</span> <span class="btn-label">Aidomariへ送信</span>';
    }
  }

  // エラー表示
  function showError(msg) {
    extractingView.classList.add('hidden');
    previewView.classList.add('hidden');
    errorView.classList.remove('hidden');
    errorMessageEl.textContent = msg;
  }

  // 送信処理
  sendBtn.addEventListener('click', async () => {
    if (!currentArticle) return;

    sendBtn.disabled = true;
    sendBtn.innerHTML = '<span class="spinner" style="width:14px;height:14px;border-width:2px;display:inline-block;vertical-align:middle;"></span> 送信中...';
    resultMessageEl.classList.add('hidden');

    chrome.runtime.sendMessage({
      action: 'sendArticle',
      payload: {
        url: currentArticle.url || activeTab?.url,
        title: currentArticle.title || activeTab?.title,
        content: currentArticle.content,
        source: 'browser_popup'
      }
    }, (response) => {
      sendBtn.disabled = false;
      sendBtn.innerHTML = '<span class="btn-icon">📥</span> <span class="btn-label">Aidomariへ送信</span>';

      if (chrome.runtime.lastError || !response || !response.success) {
        resultMessageEl.className = 'result-message error';
        resultMessageEl.textContent = response?.error || '送信に失敗しました。Aidomariアプリが起動しているか確認してください。';
        resultMessageEl.classList.remove('hidden');
      } else {
        resultMessageEl.className = 'result-message success';
        resultMessageEl.innerHTML = '✅ <strong>送信完了！</strong><br>Aidomariアプリの「本日の閲覧デッキ化」からAIカードを自動生成できます。';
        resultMessageEl.classList.remove('hidden');
        refreshServerStatus();
      }
    });
  });

  // 再試行
  retryBtn.addEventListener('click', () => {
    extractActiveTabArticle();
  });

  // 設定保存
  saveEndpointBtn.addEventListener('click', () => {
    let url = endpointInput.value.trim();
    if (!url) url = 'http://127.0.0.1:8877';
    chrome.storage.local.set({ endpointUrl: url }, () => {
      saveEndpointBtn.textContent = '保存済';
      setTimeout(() => { saveEndpointBtn.textContent = '保存'; }, 1500);
      refreshServerStatus();
    });
  });

  // 接続テスト
  pingBtn.addEventListener('click', () => {
    pingBtn.disabled = true;
    pingBtn.textContent = 'テスト中...';
    chrome.runtime.sendMessage({ action: 'checkStatus' }, (response) => {
      pingBtn.disabled = false;
      pingBtn.textContent = '接続テスト';
      if (chrome.runtime.lastError || !response || !response.ok) {
        alert('Aidomariアプリに接続できませんでした。\n\n・Android端末上でAidomariアプリが開いているか確認してください。\n・URLが正しいか確認してください (' + endpointInput.value + ')');
      } else {
        alert('✅ Aidomariアプリとの通信に成功しました！\nバージョン: ' + (response.data?.version ?? 1) + '\n未処理記事: ' + (response.data?.pendingCount ?? 0) + '件');
        refreshServerStatus();
      }
    });
  });

  // 初期ロード
  refreshServerStatus();
  extractActiveTabArticle();
});
