/**
 * Aidomari Article Extractor Unit Tests (Pure Node.js, Zero Dependencies)
 */
const assert = require('assert');
const path = require('path');
const extractor = require('../extractor.js');

// === 軽量 HTML パーサー & DOM モック ===
class MiniNode {
  constructor(nodeType, tagName = '', textValue = '') {
    this.nodeType = nodeType; // 1: Element, 3: Text
    this.tagName = tagName.toUpperCase();
    this.nodeValue = textValue;
    this.attributes = {};
    this.childNodes = [];
    this.parentNode = null;
  }

  getAttribute(name) {
    return this.attributes[name.toLowerCase()] ?? null;
  }

  setAttribute(name, value) {
    this.attributes[name.toLowerCase()] = String(value);
  }

  appendChild(child) {
    child.parentNode = this;
    this.childNodes.push(child);
    return child;
  }

  removeChild(child) {
    const idx = this.childNodes.indexOf(child);
    if (idx !== -1) {
      this.childNodes.splice(idx, 1);
      child.parentNode = null;
    }
  }

  remove() {
    if (this.parentNode) {
      this.parentNode.removeChild(this);
    }
  }

  get textContent() {
    if (this.nodeType === 3) return this.nodeValue;
    return this.childNodes.map(c => c.textContent).join('');
  }

  cloneNode(deep = true) {
    const clone = new MiniNode(this.nodeType, this.tagName, this.nodeValue);
    for (const [k, v] of Object.entries(this.attributes)) {
      clone.setAttribute(k, v);
    }
    if (deep) {
      for (const child of this.childNodes) {
        clone.appendChild(child.cloneNode(true));
      }
    }
    return clone;
  }

  matches(sel) {
    sel = sel.trim();
    if (!sel) return false;

    // タグ名のみ
    if (/^[a-zA-Z0-9]+$/.test(sel)) {
      return this.tagName.toLowerCase() === sel.toLowerCase();
    }
    // ID: #id
    if (sel.startsWith('#')) {
      return this.getAttribute('id') === sel.slice(1);
    }
    // クラス: .cls
    if (sel.startsWith('.')) {
      const cls = (this.getAttribute('class') || '').split(/\s+/);
      return cls.includes(sel.slice(1));
    }
    // 属性: [attr="val"]
    const attrMatch = sel.match(/^\[([a-zA-Z0-9_-]+)(?:=["']([^"']*)["'])?\]$/);
    if (attrMatch) {
      const val = this.getAttribute(attrMatch[1]);
      if (val === null) return false;
      if (attrMatch[2] !== undefined) return val === attrMatch[2];
      return true;
    }
    // タグ + 属性: meta[property="og:title"]
    const tagAttrMatch = sel.match(/^([a-zA-Z0-9]+)\[([a-zA-Z0-9_:-]+)=["']([^"']*)["']\]$/);
    if (tagAttrMatch) {
      if (this.tagName.toLowerCase() !== tagAttrMatch[1].toLowerCase()) return false;
      return this.getAttribute(tagAttrMatch[2]) === tagAttrMatch[3];
    }
    return false;
  }

  querySelectorAll(selector) {
    const selectors = selector.split(',').map(s => s.trim()).filter(Boolean);
    const results = [];

    const traverse = (node) => {
      for (const child of node.childNodes) {
        if (child.nodeType === 1) {
          for (const sel of selectors) {
            if (child.matches(sel)) {
              results.push(child);
              break;
            }
          }
          traverse(child);
        }
      }
    };
    traverse(this);
    return results;
  }

  querySelector(selector) {
    const list = this.querySelectorAll(selector);
    return list.length > 0 ? list[0] : null;
  }
}

// 簡易 HTML 文字列パーサー
function parseHTML(html) {
  const root = new MiniNode(1, 'DOCUMENT');
  const stack = [root];

  // タグとテキストの正規表現
  const tagRegex = /<!--[\s\S]*?-->|<(\/)?([a-zA-Z0-9:-]+)((?:\s+[^=>\s]+(?:=(?:"[^"]*"|'[^']*'|[^>\s]+))?)*)\s*(\/)?>|([^<]+)/g;
  let match;

  const voidTags = new Set(['area', 'base', 'br', 'col', 'embed', 'hr', 'img', 'input', 'link', 'meta', 'param', 'source', 'track', 'wbr']);

  while ((match = tagRegex.exec(html)) !== null) {
    const [full, isClose, tagName, attrString, isSelfClose, text] = match;

    if (full.startsWith('<!--')) continue; // コメント無視

    if (text) {
      const textNode = new MiniNode(3, '', text);
      stack[stack.length - 1].appendChild(textNode);
      continue;
    }

    if (isClose) {
      // 終了タグ
      if (stack.length > 1 && stack[stack.length - 1].tagName.toLowerCase() === tagName.toLowerCase()) {
        stack.pop();
      }
      continue;
    }

    // 開始タグ
    const elem = new MiniNode(1, tagName);

    // 属性パース
    if (attrString) {
      const attrRegex = /([a-zA-Z0-9_:-]+)(?:=(?:"([^"]*)"|'([^']*)'|([^>\s]+)))?/g;
      let aMatch;
      while ((aMatch = attrRegex.exec(attrString)) !== null) {
        const val = aMatch[2] ?? aMatch[3] ?? aMatch[4] ?? '';
        elem.setAttribute(aMatch[1], val);
      }
    }

    stack[stack.length - 1].appendChild(elem);

    if (!voidTags.has(tagName.toLowerCase()) && !isSelfClose) {
      stack.push(elem);
    }
  }

  // document mock
  const doc = {
    body: root.querySelector('body') || root,
    title: root.querySelector('title')?.textContent || '',
    querySelector: (s) => root.querySelector(s),
    querySelectorAll: (s) => root.querySelectorAll(s)
  };

  return doc;
}

// === テストケース実行 ===
console.log('--- Aidomari Extractor Unit Tests ---');

// Test 1: タイトル抽出 & サフィックス除去
{
  const html = `
    <html>
      <head>
        <title>北海道の歴史と文化 | 北国アーカイブス</title>
        <meta property="og:title" content="北海道の歴史と文化 - 詳細ガイド | 北国アーカイブス">
        <link rel="canonical" href="https://example.com/hokkaido-history">
      </head>
      <body>
        <h1>北海道の歴史と文化</h1>
        <article>
          <p>北海道は豊かな自然と独自の歴史を持つ地域です。</p>
        </article>
      </body>
    </html>
  `;
  const doc = parseHTML(html);
  const article = extractor.extractArticle(doc, 'https://example.com/fallback');

  assert.strictEqual(article.title, '北海道の歴史と文化 - 詳細ガイド', 'OGPタイトルがサフィックス除去されて抽出されること');
  assert.strictEqual(article.url, 'https://example.com/hokkaido-history', 'Canonical URLが正しく抽出されること');
  assert(article.content.includes('北海道は豊かな自然と独自の歴史を持つ地域です'), '本文段落が抽出されること');
  console.log('✓ Test 1: OGP Title, cleanTitle suffix stripping & Canonical URL');
}

// Test 2: 不要タグ (header, footer, nav, aside, ad, social, comments) の完全除外
{
  const html = `
    <html>
      <head><title>技術記事</title></head>
      <body>
        <header>
          <nav><ul><li><a href="/">ホーム</a></li><li><a href="/about">概要</a></li></ul></nav>
        </header>
        <article>
          <h1>Kotlin Coroutines 入門</h1>
          <p>コルーチンは非同期処理を同期的に記述できる強力な仕組みです。</p>
          <div class="ad">広告: 最新のスマートフォンが今なら半額！</div>
          <p>ディスパッチャには Dispatchers.Default や Dispatchers.IO などがあります。</p>
          <div class="social-share">Twitterでシェア Facebookでシェア</div>
          <div id="comments">
            <p>読者コメント: 分かりやすかったです！</p>
          </div>
        </article>
        <aside>
          <div class="sidebar">関連記事リンク集</div>
        </aside>
        <footer>
          <p>&copy; 2026 Tech Notes</p>
        </footer>
      </body>
    </html>
  `;
  const doc = parseHTML(html);
  const article = extractor.extractArticle(doc);

  assert(!article.content.includes('ホーム'), 'ナビゲーションは除外されること');
  assert(!article.content.includes('広告:'), '広告ブロックは除外されること');
  assert(!article.content.includes('Twitterでシェア'), 'SNSシェアは除外されること');
  assert(!article.content.includes('読者コメント'), 'コメント欄は除外されること');
  assert(!article.content.includes('関連記事リンク集'), 'サイドバーは除外されること');
  assert(!article.content.includes('Tech Notes'), 'フッターは除外されること');
  assert(article.content.includes('コルーチンは非同期処理を同期的に記述できる強力な仕組みです。'), '本文段落1が含まれること');
  assert(article.content.includes('ディスパッチャには Dispatchers.Default'), '本文段落2が含まれること');
  console.log('✓ Test 2: Unwanted elements (nav, header, footer, ads, social, comments) stripped');
}

// Test 3: Markdown風の構造化テキスト (見出し、リスト、段落)
{
  const html = `
    <html>
      <body>
        <main>
          <h1>Aidomari 学習機能</h1>
          <p>Aidomariは忘却曲線に基づいた単語学習アプリです。</p>
          <h2>主な特徴</h2>
          <ul>
            <li>スワイプで覚えた/復習を即判定</li>
            <li>すごろく連動マップ</li>
            <li>ブラウザ連携による自動デッキ生成</li>
          </ul>
        </main>
      </body>
    </html>
  `;
  const doc = parseHTML(html);
  const article = extractor.extractArticle(doc);

  assert(article.content.includes('# Aidomari 学習機能'), 'H1が見出し構文になること');
  assert(article.content.includes('## 主な特徴'), 'H2が見出し構文になること');
  assert(article.content.includes('- スワイプで覚えた/復習を即判定'), 'リスト項目1が保持されること');
  assert(article.content.includes('- すごろく連動マップ'), 'リスト項目2が保持されること');
  assert(article.content.includes('- ブラウザ連携による自動デッキ生成'), 'リスト項目3が保持されること');
  assert(article.charCount > 50, '文字数が正しく計算されること');
  assert(article.estimatedCards >= 1, '想定カード枚数が1以上であること');
  console.log('✓ Test 3: Structured Markdown output (Headings, lists, paragraphs)');
}

// Test 4: フォールバックURLと文字数・概算カード数
{
  const html = `
    <html>
      <body>
        <div class="entry-content">
          <p>${'これは約1200文字の本文テスト用テキストです。'.repeat(40)}</p>
        </div>
      </body>
    </html>
  `;
  const doc = parseHTML(html);
  const article = extractor.extractArticle(doc, 'https://fallback.aidomari.test/article-1');

  assert.strictEqual(article.url, 'https://fallback.aidomari.test/article-1', 'フォールバックURLが適用されること');
  assert(article.charCount >= 800, `文字数 (${article.charCount}) が正常にカウントされること`);
  assert(article.estimatedCards >= 2 && article.estimatedCards <= 5, `概算カード枚数 (${article.estimatedCards}) が計算されること`);
  console.log('✓ Test 4: Fallback URL & estimated card count scaling');
}

// Test 5: プライバシー除外フィルター（検索エンジン、ログイン、ローカルホスト）
{
  assert.strictEqual(extractor.checkPrivacyExclusion('https://www.google.com/search?q=kotlin').isExcluded, true, 'Google検索は除外されること');
  assert.strictEqual(extractor.checkPrivacyExclusion('https://search.yahoo.co.jp/search?p=test').isExcluded, true, 'Yahoo検索は除外されること');
  assert.strictEqual(extractor.checkPrivacyExclusion('https://accounts.google.com/signin').isExcluded, true, 'Googleログインは除外されること');
  assert.strictEqual(extractor.checkPrivacyExclusion('https://example.com/login').isExcluded, true, 'ログインパスは除外されること');
  assert.strictEqual(extractor.checkPrivacyExclusion('http://localhost:3000/dashboard').isExcluded, true, 'localhostは除外されること');
  assert.strictEqual(extractor.checkPrivacyExclusion('https://qiita.com/developer/items/12345').isExcluded, false, '一般技術記事は除外されないこと');
  console.log('✓ Test 5: Privacy exclusion filter for search engines, auth pages, and localhost');
}

console.log('\nAll 5 Extractor Unit Tests Passed Successfully!');
