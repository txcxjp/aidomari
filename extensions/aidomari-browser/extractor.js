/**
 * Aidomari Article Extractor
 * 軽量・スタンドアロンな記事本文抽出エンジン。
 * ブラウザ (DOM) および Node.js (JSDOM または 軽量DOM) の両方で動作します。
 */
(function (root, factory) {
  if (typeof module === 'object' && module.exports) {
    module.exports = factory();
  } else {
    root.AidomariExtractor = factory();
  }
}(typeof self !== 'undefined' ? self : this, function () {
  'use strict';

  // 検索エンジンドメイン
  const SEARCH_ENGINE_DOMAINS = [
    'google.com', 'google.co.jp', 'bing.com', 'yahoo.co.jp', 'search.yahoo.co.jp',
    'yahoo.com', 'duckduckgo.com', 'baidu.com', 'ecosia.org', 'yandex.com', 'ask.com'
  ];

  // 認証・ログイン系ホストプレフィックス
  const EXCLUDED_HOST_PREFIXES = [
    'login.', 'auth.', 'signin.', 'accounts.', 'account.', 'id.', 'admin.', 'sso.'
  ];

  /**
   * プライバシー除外チェック
   */
  function checkPrivacyExclusion(rawUrl) {
    if (!rawUrl) return { isExcluded: false, reason: null };
    try {
      const parsed = new URL(rawUrl);
      const host = parsed.hostname.toLowerCase();
      const path = parsed.pathname.toLowerCase();

      // ローカル / 内部ホスト
      if (['localhost', '127.0.0.1', '0.0.0.0', '::1'].includes(host) || host.endsWith('.local') || host.endsWith('.internal') || host.endsWith('.lan')) {
        return { isExcluded: true, reason: 'ローカルホストまたは内部ページの除外' };
      }

      // 検索エンジン
      if (SEARCH_ENGINE_DOMAINS.some(d => host === d || host === `www.${d}` || host.endsWith(`.${d}`))) {
        return { isExcluded: true, reason: '検索結果ページは取り込み対象外です' };
      }

      // 認証・ログインサブドメイン
      if (EXCLUDED_HOST_PREFIXES.some(p => host.startsWith(p))) {
        return { isExcluded: true, reason: 'ログイン・アカウント管理ページはセキュリティ保護のため除外されます' };
      }

      // 認証・ログインパス
      if (path.includes('/login') || path.includes('/signin') || path.includes('/auth/') || path.includes('/oauth')) {
        return { isExcluded: true, reason: 'ログイン・認証関連ページのため除外されます' };
      }

      return { isExcluded: false, reason: null };
    } catch (_) {
      return { isExcluded: false, reason: null };
    }
  }

  // 不要要素セレクタ
  const UNWANTED_SELECTORS = [
    'script', 'style', 'noscript', 'iframe', 'svg', 'canvas', 'audio', 'video',
    'nav', 'header', 'footer', 'aside',
    '[role="navigation"]', '[role="banner"]', '[role="contentinfo"]', '[role="complementary"]',
    '[aria-hidden="true"]',
    '.ad', '.ads', '.advertisement', '#ad', '#ads',
    '.social-share', '.sns-share', '.share-buttons', '.sns-buttons',
    '.comments', '#comments', '.comment-list', '.related-posts', '.recommended-posts',
    '.sidebar', '#sidebar', '.breadcrumb', '.breadcrumbs',
    '.footer', '#footer', '.menu', '#menu', '.header', '#header', '.nav', '#nav',
    '.author-profile', '.author-bio', '.widget', '.popup', '.modal'
  ];

  // 本文コンテナの候補セレクタ（優先順）
  const CANDIDATE_SELECTORS = [
    'article',
    'main',
    '[role="main"]',
    '.entry-content',
    '.post-content',
    '.article-body',
    '.article-content',
    '.story-body',
    '.main-content',
    '#content',
    '#main'
  ];

  /**
   * タイトルを抽出
   */
  function extractTitle(doc) {
    // 1. og:title
    const ogTitle = doc.querySelector('meta[property="og:title"]')?.getAttribute('content');
    if (ogTitle && ogTitle.trim()) {
      return cleanTitle(ogTitle.trim());
    }

    // 2. twitter:title
    const twitterTitle = doc.querySelector('meta[name="twitter:title"]')?.getAttribute('content');
    if (twitterTitle && twitterTitle.trim()) {
      return cleanTitle(twitterTitle.trim());
    }

    // 3. h1
    const h1 = doc.querySelector('h1');
    if (h1 && h1.textContent && h1.textContent.trim()) {
      return cleanTitle(h1.textContent.trim());
    }

    // 4. document.title
    if (doc.title && doc.title.trim()) {
      return cleanTitle(doc.title.trim());
    }

    return '無題の記事';
  }

  /**
   * サイト名サフィックス等をトリムしたクリーンなタイトル
   */
  function cleanTitle(raw) {
    return raw
      .replace(/[\r\n\t]+/g, ' ')
      .replace(/\s+/g, ' ')
      .replace(/\s*([|・\-_–—]|::|\/\/)\s*[^|・\-_–—]+$/, '') // 「記事タイトル | サイト名」の末尾除去
      .trim();
  }

  /**
   * URL を取得
   */
  function extractUrl(doc, fallbackUrl) {
    const canonical = doc.querySelector('link[rel="canonical"]')?.getAttribute('href');
    if (canonical && canonical.startsWith('http')) {
      return canonical.trim();
    }
    const ogUrl = doc.querySelector('meta[property="og:url"]')?.getAttribute('content');
    if (ogUrl && ogUrl.startsWith('http')) {
      return ogUrl.trim();
    }
    return fallbackUrl || '';
  }

  /**
   * 要素から不要ノードを除去
   */
  function stripUnwanted(container) {
    for (const sel of UNWANTED_SELECTORS) {
      const elements = container.querySelectorAll(sel);
      for (let i = 0; i < elements.length; i++) {
        elements[i].remove();
      }
    }
  }

  /**
   * DOMノードを再帰的に走査して構造化テキストを生成
   */
  function nodeToStructuredText(node) {
    if (!node) return '';

    // テキストノード
    if (node.nodeType === 3) {
      return node.nodeValue || '';
    }

    // 要素ノード以外はスキップ
    if (node.nodeType !== 1) {
      return '';
    }

    const tagName = node.tagName.toLowerCase();

    // 不要タグ
    if (['script', 'style', 'noscript', 'iframe', 'svg', 'canvas', 'button', 'input', 'select', 'textarea'].includes(tagName)) {
      return '';
    }

    // 見出しタグ
    if (/^h[1-6]$/.test(tagName)) {
      const level = tagName[1];
      const prefix = '#'.repeat(parseInt(level, 10)) + ' ';
      const text = getChildrenText(node).trim();
      return text ? `\n\n${prefix}${text}\n\n` : '';
    }

    // 段落
    if (tagName === 'p') {
      const text = getChildrenText(node).trim();
      return text ? `\n\n${text}\n\n` : '';
    }

    // リスト項目
    if (tagName === 'li') {
      const text = getChildrenText(node).trim();
      return text ? `\n- ${text}` : '';
    }

    // 順序なし・順序ありリストコンテナ
    if (tagName === 'ul' || tagName === 'ol') {
      const text = getChildrenText(node).trim();
      return text ? `\n\n${text}\n\n` : '';
    }

    // 引用
    if (tagName === 'blockquote') {
      const text = getChildrenText(node).trim();
      if (!text) return '';
      const lines = text.split('\n').map(line => `> ${line}`).join('\n');
      return `\n\n${lines}\n\n`;
    }

    // コードブロック
    if (tagName === 'pre') {
      const text = node.textContent || '';
      return text.trim() ? `\n\n\`\`\`\n${text.trim()}\n\`\`\`\n\n` : '';
    }

    // 改行タグ
    if (tagName === 'br') {
      return '\n';
    }

    // テーブル
    if (tagName === 'tr') {
      const text = getChildrenText(node).trim();
      return text ? `\n| ${text} |` : '';
    }
    if (tagName === 'td' || tagName === 'th') {
      const text = getChildrenText(node).trim();
      return `${text} | `;
    }

    // その他汎用コンテナ（div, section, span, strong, em, aなど）
    return getChildrenText(node);
  }

  function getChildrenText(node) {
    let result = '';
    const children = node.childNodes;
    for (let i = 0; i < children.length; i++) {
      result += nodeToStructuredText(children[i]);
    }
    return result;
  }

  /**
   * テキストの正規化
   */
  function normalizeText(text) {
    return text
      .replace(/\r\n/g, '\n')
      .replace(/\r/g, '\n')
      .replace(/[ \t]+/g, ' ')            // 連続スペースの圧縮
      .replace(/\n[ \t]+/g, '\n')         // 行頭空白の除去
      .replace(/[ \t]+\n/g, '\n')         // 行末空白の除去
      .replace(/\n{3,}/g, '\n\n')         // 3つ以上の改行を2つに正規化
      .trim();
  }

  /**
   * 最適な本文要素をスコアリングして探索
   */
  function findBestContentElement(doc) {
    // 1. セレクタ候補を順に探す
    for (const sel of CANDIDATE_SELECTORS) {
      const el = doc.querySelector(sel);
      if (el) {
        const textLen = (el.textContent || '').trim().length;
        if (textLen >= 150) {
          return el;
        }
      }
    }

    // 2. なければ body 内の div, section を走査して、段落・テキスト密度の高いコンテナを見つける
    const body = doc.body;
    if (!body) return null;

    let bestElement = body;
    let maxScore = 0;

    const containers = body.querySelectorAll('div, section, article, main');
    for (let i = 0; i < containers.length; i++) {
      const container = containers[i];
      const pCount = container.querySelectorAll('p').length;
      const text = (container.textContent || '').trim();
      const textLen = text.length;

      if (textLen < 100) continue;

      // リンク密度を算出 (リンク内のテキスト長 / 全テキスト長)
      const links = container.querySelectorAll('a');
      let linkTextLen = 0;
      for (let j = 0; j < links.length; j++) {
        linkTextLen += (links[j].textContent || '').trim().length;
      }
      const linkDensity = textLen > 0 ? (linkTextLen / textLen) : 0;

      // ナビゲーションやフッターなどの高リンク密度ブロックを除外
      if (linkDensity > 0.5) continue;

      // スコア = (段落数 * 50 + テキスト文字数) * (1 - リンク密度)
      const score = (pCount * 50 + textLen) * (1 - linkDensity);

      if (score > maxScore) {
        maxScore = score;
        bestElement = container;
      }
    }

    return bestElement;
  }

  /**
   * メイン抽出関数
   * @param {Document} doc - HTML Document オブジェクト
   * @param {string} fallbackUrl - オプションのフォールバックURL
   * @returns {Object} { title, url, content, charCount, estimatedCards, excerpt }
   */
  function extractArticle(doc, fallbackUrl = '') {
    if (!doc) {
      return {
        title: '',
        url: fallbackUrl || '',
        content: '',
        charCount: 0,
        estimatedCards: 0,
        excerpt: ''
      };
    }

    const title = extractTitle(doc);
    const url = extractUrl(doc, fallbackUrl);

    // 作業用クローンを作成して不要要素を除去
    const targetElement = findBestContentElement(doc);
    let content = '';

    if (targetElement) {
      const clone = targetElement.cloneNode(true);
      stripUnwanted(clone);
      content = normalizeText(nodeToStructuredText(clone));
    } else if (doc.body) {
      const clone = doc.body.cloneNode(true);
      stripUnwanted(clone);
      content = normalizeText(nodeToStructuredText(clone));
    }

    const charCount = content.replace(/\s+/g, '').length;
    // 400文字ごとに1問、最小1問〜最大10問の概算カード枚数
    const estimatedCards = Math.max(1, Math.min(10, Math.round(charCount / 400)));
    const excerpt = content.slice(0, 150).replace(/\n+/g, ' ') + (content.length > 150 ? '...' : '');

    const privacyCheck = checkPrivacyExclusion(url);

    return {
      title,
      url,
      content,
      charCount,
      estimatedCards,
      excerpt,
      isExcluded: privacyCheck.isExcluded,
      exclusionReason: privacyCheck.reason
    };
  }

  return {
    extractArticle,
    extractTitle,
    extractUrl,
    cleanTitle,
    normalizeText,
    checkPrivacyExclusion
  };
}));
