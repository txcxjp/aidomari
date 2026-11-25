/**
 * Unit tests for Aidomari WebExtension script injection paths and content script idempotency.
 * Run with Node.js: node test/test_extension_scripts.js
 */
const fs = require('fs');
const path = require('path');
const vm = require('vm');
const assert = require('assert');

const extRoot = path.resolve(__dirname, '..');

function extractExecuteScriptFileArrays(jsSource) {
  const regex = /scripting\.executeScript\([\s\S]*?files:\s*\[([^\]]+)\]/g;
  const results = [];
  let match;
  while ((match = regex.exec(jsSource)) !== null) {
    const items = match[1]
      .split(',')
      .map((s) => s.trim().replace(/^['"]|['"]$/g, ''))
      .filter(Boolean);
    results.push(items);
  }
  return results;
}

console.log('--- Aidomari Extension Script Tests ---');

// Test 1: All executeScript files use extension-root-leading paths and exist on disk
{
  const targets = ['popup/popup.js', 'background.js'];
  for (const relPath of targets) {
    const src = fs.readFileSync(path.join(extRoot, relPath), 'utf8');
    const fileArrays = extractExecuteScriptFileArrays(src);
    assert(fileArrays.length > 0, `${relPath} に executeScript の files 指定があること`);
    for (const files of fileArrays) {
      for (const f of files) {
        assert(
          f.startsWith('/'),
          `${relPath} の files パス "${f}" は Firefox 対策として先頭 "/" から始まること`,
        );
        const resolved = path.join(extRoot, f.slice(1));
        assert(fs.existsSync(resolved), `注入対象ファイルが存在すること: ${resolved}`);
      }
    }
  }
  console.log('✓ Test 1: executeScript files use root-relative paths ("/...") and resolve cleanly');
}

// Test 2: content_script.js avoids duplicate onMessage listener registration on re-injection
{
  const contentScriptSrc = fs.readFileSync(path.join(extRoot, 'content_script.js'), 'utf8');
  const listeners = [];
  const sandbox = {
    globalThis: {},
    chrome: {
      runtime: {
        onMessage: {
          addListener: (fn) => listeners.push(fn),
        },
      },
    },
    window: {
      location: { href: 'https://example.com/article' },
      getSelection: () => ({ toString: () => '' }),
    },
    document: {},
    AidomariExtractor: {
      extractArticle: () => ({ title: 'Sample', content: 'Body text', url: 'https://example.com/article' }),
    },
    console,
  };
  sandbox.globalThis = sandbox;
  vm.createContext(sandbox);

  vm.runInContext(contentScriptSrc, sandbox);
  vm.runInContext(contentScriptSrc, sandbox);

  assert.strictEqual(listeners.length, 1, '再注入時もリスナーは1回だけ登録されること');

  let received = null;
  listeners[0]({ action: 'extract' }, {}, (resp) => {
    received = resp;
  });
  assert(received && received.success === true, 'extract アクションが成功レスポンスを返すこと');
  assert.strictEqual(received.article.title, 'Sample');
  console.log('✓ Test 2: content_script.js guards against duplicate listener registration');
}

console.log('\nAll Extension Script Tests Passed Successfully!');
