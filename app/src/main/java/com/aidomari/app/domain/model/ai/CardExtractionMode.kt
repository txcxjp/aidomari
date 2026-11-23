package com.aidomari.app.domain.model.ai

/**
 * AI単語帳カード生成における抽出モード。
 * 記事タイプに応じた自動判定（スマート）、要旨・論点中心（コア）、体系的知識網羅（ナレッジ）を定義する。
 */
enum class CardExtractionMode(val label: String, val description: String) {
    SMART(label = "スマート", description = "記事タイプをAIが自動判別して最適化"),
    CORE(label = "コア", description = "要旨・論点・中心概念を凝縮"),
    KNOWLEDGE(label = "ナレッジ", description = "体系的知識・専門用語を徹底網羅"),
}
