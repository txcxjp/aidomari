/**
 * Aidomari Content Script
 * ページ内で実行され、記事の抽出や選択テキストの取得を行います。
 */
(function () {
  'use strict';

  // メッセージリスナー
  chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === 'extract') {
      try {
        const selection = window.getSelection() ? window.getSelection().toString().trim() : '';
        const article = AidomariExtractor.extractArticle(document, window.location.href);

        sendResponse({
          success: true,
          article: {
            ...article,
            selection: selection || null
          }
        });
      } catch (err) {
        console.error('[Aidomari] Extraction failed:', err);
        sendResponse({
          success: false,
          error: err.message || '記事の抽出に失敗しました'
        });
      }
      return true; // 非同期レスポンス有効化
    }

    if (request.action === 'ping') {
      sendResponse({ status: 'alive' });
      return true;
    }
  });
})();
