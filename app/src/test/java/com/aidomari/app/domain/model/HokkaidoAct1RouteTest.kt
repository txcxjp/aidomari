package com.aidomari.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HokkaidoAct1RouteTest {

    @Test
    fun route_hasExactCuratedNodes() {
        assertEquals(262, HokkaidoAct1Route.totalNodes)
        assertEquals(262, HokkaidoAct1Route.routeNodes.size)
        assertEquals(261, HokkaidoAct1Route.maxStepIndex)
    }

    @Test
    fun startNode_isHakodateStation() {
        val startNode = HokkaidoAct1Route.startNode
        assertEquals("hakodate_station", startNode.id)
        assertEquals(0, startNode.stepIndex)
        assertEquals("函館駅前", startNode.name)
        assertEquals("函館市", startNode.municipality)
        assertEquals(SugorokuNodeType.START_HAKODATE, startNode.type)
        assertEquals(1, startNode.rank)
        assertTrue(startNode.isAnchor)
        assertTrue("Start node should have non-empty description", startNode.description.isNotBlank())
    }

    @Test
    fun goalNode_isAidomari() {
        val goalNode = HokkaidoAct1Route.goalNode
        assertEquals("aidomari_bridge", goalNode.id)
        assertEquals(261, goalNode.stepIndex)
        assertEquals("相泊橋", goalNode.name)
        assertEquals("羅臼町", goalNode.municipality)
        assertEquals(SugorokuNodeType.GOAL_AIDOMARI, goalNode.type)
        assertEquals(1, goalNode.rank)
        assertTrue(goalNode.isAnchor)
        assertTrue("Goal node should have non-empty description", goalNode.description.isNotBlank())
    }

    @Test
    fun allNodes_haveUniqueIdsAndSequentialStepIndices() {
        val ids = HokkaidoAct1Route.routeNodes.map { it.id }.toSet()
        assertEquals(HokkaidoAct1Route.totalNodes, ids.size)

        HokkaidoAct1Route.routeNodes.forEachIndexed { index, node ->
            assertEquals("Node at index $index must have stepIndex $index", index, node.stepIndex)
            assertTrue("Node at index $index should have non-empty name", node.name.isNotBlank())
            assertTrue("Node at index $index should have non-empty municipality", node.municipality.isNotBlank())
            assertTrue("Node at index $index should have non-empty description", node.description.isNotBlank())
            assertTrue("Node at index $index x coordinate should be within 0..2000", node.x in 0f..2000f)
            assertTrue("Node at index $index y coordinate should be within 0..2000", node.y in 0f..2000f)
            assertTrue("All nodes must be anchors", node.isAnchor)
        }
    }

    @Test
    fun getNodeByStepIndex_returnsCorrectNode() {
        val node0 = HokkaidoAct1Route.getNodeByStepIndex(0)
        assertNotNull(node0)
        assertEquals("函館駅前", node0?.name)

        val nodeSoya = HokkaidoAct1Route.nodes.firstOrNull { it.id == "soya_cape_monument" }
        assertNotNull(nodeSoya)
        assertEquals("宗谷岬", nodeSoya?.name)
        assertEquals(SugorokuNodeType.SCENIC_SPOT, nodeSoya?.type)

        val nodeGoal = HokkaidoAct1Route.getNodeByStepIndex(HokkaidoAct1Route.maxStepIndex)
        assertNotNull(nodeGoal)
        assertEquals("相泊橋", nodeGoal?.name)

        assertNull(HokkaidoAct1Route.getNodeByStepIndex(-1))
        assertNull(HokkaidoAct1Route.getNodeByStepIndex(HokkaidoAct1Route.totalNodes))
    }

    @Test
    fun getNodeById_returnsCorrectNode() {
        val nodeSoya = HokkaidoAct1Route.nodes.firstOrNull { it.id == "soya_cape_monument" }
        assertNotNull(nodeSoya)
        val fetchedNode = HokkaidoAct1Route.getNodeById(nodeSoya!!.id)
        assertNotNull(fetchedNode)
        assertEquals(nodeSoya.stepIndex, fetchedNode?.stepIndex)
        assertEquals("宗谷岬", fetchedNode?.name)

        assertNull(HokkaidoAct1Route.getNodeById("non_existent_node"))
    }

    @Test
    fun anchors_haveUniqueSemanticIdsAndValidRanks() {
        val anchors = HokkaidoAct1Route.anchors
        assertEquals(HokkaidoAct1Route.totalNodes, anchors.size)
        assertEquals("hakodate_station", anchors.first().id)
        assertEquals("aidomari_bridge", anchors.last().id)

        anchors.forEach { anchor ->
            assertTrue("Rank must be between 1 and 4", anchor.rank in 1..4)
            assertTrue("Anchor id should not be blank", anchor.id.isNotBlank())
            assertTrue("Anchor name should not be blank", anchor.name.isNotBlank())
            assertTrue("Anchor municipality should not be blank", anchor.municipality.isNotBlank())
            assertTrue("Anchor description should not be blank", anchor.description.isNotBlank())
        }

        val rank1Anchors = anchors.filter { it.rank == 1 }
        assertEquals(67, rank1Anchors.size)
        assertTrue(anchors.any { it.id == "otaru_aquarium" && it.name == "おたる水族館" && it.rank == 1 })
        assertTrue(anchors.any { it.id == "seseki_onsen" && it.name == "セセキ温泉" && it.rank == 1 })

        val rank2Anchors = anchors.filter { it.rank == 2 }
        assertEquals(68, rank2Anchors.size)
        assertTrue(rank2Anchors.any { it.id == "teshio_river_mouth" && it.name == "天塩川河口" })

        val rank3Anchors = anchors.filter { it.rank == 3 }
        assertEquals(80, rank3Anchors.size)
        assertTrue(rank3Anchors.any { it.id == "kawaguchi_ruins_scenic_forest" && it.name == "川口遺跡風景林" })
        assertTrue(rank3Anchors.any { it.id == "esashi_port" && it.name == "枝幸港" })

        val rank4Anchors = anchors.filter { it.rank == 4 }
        assertEquals(47, rank4Anchors.size)
        assertTrue(rank4Anchors.any { it.id == "goryokaku_station" && it.name == "五稜郭駅" })
        assertTrue(rank4Anchors.any { it.id == "nanae_station" && it.name == "七飯駅" })

        val ids = anchors.map { it.id }.toSet()
        assertEquals(anchors.size, ids.size)
    }

    @Test
    fun adjacentAnchors_satisfyMinimumDistanceRule() {
        val anchors = HokkaidoAct1Route.anchors
        for (i in 0 until anchors.size - 1) {
            val a1 = anchors[i]
            val a2 = anchors[i + 1]
            val lat1Rad = Math.toRadians(a1.lat)
            val lat2Rad = Math.toRadians(a2.lat)
            val dLat = Math.toRadians(a2.lat - a1.lat)
            val dLon = Math.toRadians(a2.lon - a1.lon)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            val distKm = 6371.0 * c

            assertTrue(
                "Distance between anchor $i (${a1.name}) and ${i + 1} (${a2.name}) is ${distKm}km, expected >= 1.0km",
                distKm >= 1.0,
            )
        }
    }

    @Test
    fun nextAndPrevAnchor_navigateCorrectly() {
        val start = HokkaidoAct1Route.startAnchor
        val next = HokkaidoAct1Route.getNextAnchor(start.id)
        assertNotNull(next)
        assertEquals("omori_park", next?.id)

        val prev = HokkaidoAct1Route.getPrevAnchor(next!!.id)
        assertNotNull(prev)
        assertEquals(start.id, prev?.id)

        assertNull(HokkaidoAct1Route.getPrevAnchor(start.id))
        assertNull(HokkaidoAct1Route.getNextAnchor(HokkaidoAct1Route.goalAnchor.id))
    }

    @Test
    fun allCapesAndLighthouses_areAnchors() {
        HokkaidoAct1Route.routeNodes.forEach { node ->
            if (node.name.contains("岬") || node.name.contains("灯台")) {
                assertTrue(
                    "Cape or lighthouse node ${node.stepIndex} (${node.name}) must be an anchor",
                    node.isAnchor,
                )
            }
        }
    }

    @Test
    fun removedObsoleteAnchors_areNotPresent() {
        val names = HokkaidoAct1Route.routeNodes.map { it.name }.toSet()
        assertTrue("明元町 should not be present", "明元町" !in names)
        assertTrue("中央町 should not be present", "中央町" !in names)
        assertTrue("小樽ドリームビーチ should not be present", "小樽ドリームビーチ" !in names)
        assertTrue("駒ヶ岳ビューポイント should not be present", "駒ヶ岳ビューポイント" !in names)
        assertTrue("神岬海岸 should not be present", "神岬海岸" !in names)
        assertTrue("望来展望台 should not be present", "望来展望台" !in names)
        assertTrue("力昼夕日展望台 should not be present", "力昼夕日展望台" !in names)
        assertTrue("沢木海岸 should not be present", "沢木海岸" !in names)
        assertTrue("斜内海岸 should not be present", "斜内海岸" !in names)
        assertTrue("浜里海岸 should not be present", "浜里海岸" !in names)
        assertTrue("坂の下高台 should not be present", "坂の下高台" !in names)
        assertTrue("網走帽子岩 should not be present", "網走帽子岩" !in names)
        assertTrue("稚内西海岸 should not be present", "稚内西海岸" !in names)
        assertTrue("茅沼炭鉱跡 should not be present", "茅沼炭鉱跡" !in names)
    }
}
