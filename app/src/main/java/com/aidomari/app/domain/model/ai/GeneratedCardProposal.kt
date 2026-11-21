package com.aidomari.app.domain.model.ai

import kotlinx.serialization.Serializable

/**
 * AIによってWeb記事から抽出・生成されたカードの提案データモデル。
 */
@Serializable
data class GeneratedCardProposal(
    val front: String,
    val back: String,
    val userSupplement: String = "",
    val sourceUrl: String = "",
    val sourceTitle: String = "",
)
