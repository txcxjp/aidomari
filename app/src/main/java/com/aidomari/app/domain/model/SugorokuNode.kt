package com.aidomari.app.domain.model

data class SugorokuNode(
    val id: String,
    val stepIndex: Int,
    val name: String,
    val municipality: String,
    val type: SugorokuNodeType,
    val x: Float,
    val y: Float,
    val description: String = "",
    val roadName: String = "",
    val isAnchor: Boolean = false,
    val rank: Int = 1,
)
