package com.aidomari.app.domain.model.ai

/**
 * AI単語帳カード生成における知識抽出の深さ（解像度）。
 * 件数の数値ノルマではなく、「知識の樹木（幹・枝・葉）」のどの層まで掬い取るかの姿勢を定義する。
 */
enum class CardExtractionDepth(val label: String, val description: String) {
    ESSENCE(label = "エッセンス", description = "記事の根幹・最重要概念のみ厳選"),
    STANDARD(label = "標準", description = "主要な論点・重要用語を網羅"),
    DETAILED(label = "枝葉も含める", description = "固有名詞・付随データまで網羅"),
}
