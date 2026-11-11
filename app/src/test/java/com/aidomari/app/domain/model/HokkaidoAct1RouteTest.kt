package com.aidomari.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HokkaidoAct1RouteTest {

    @Test
    fun route_hasExactCuratedNodes() {
        assertEquals(67, HokkaidoAct1Route.totalNodes)
        assertEquals(67, HokkaidoAct1Route.routeNodes.size)
        assertEquals(66, HokkaidoAct1Route.maxStepIndex)
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
    }

    @Test
    fun goalNode_isAidomari() {
        val goalNode = HokkaidoAct1Route.goalNode
        assertEquals("aidomari_bridge", goalNode.id)
        assertEquals(66, goalNode.stepIndex)
        assertEquals("相泊橋（最果ての終着点・車道終点）", goalNode.name)
        assertEquals("羅臼町", goalNode.municipality)
        assertEquals(SugorokuNodeType.GOAL_AIDOMARI, goalNode.type)
        assertEquals(1, goalNode.rank)
        assertTrue(goalNode.isAnchor)
    }

    @Test
    fun allNodes_haveUniqueIdsAndSequentialStepIndices() {
        val ids = HokkaidoAct1Route.routeNodes.map { it.id }.toSet()
        assertEquals(HokkaidoAct1Route.totalNodes, ids.size)

        HokkaidoAct1Route.routeNodes.forEachIndexed { index, node ->
            assertEquals("Node at index $index must have stepIndex $index", index, node.stepIndex)
            assertTrue("Node at index $index should have non-empty name", node.name.isNotBlank())
            assertTrue("Node at index $index should have non-empty municipality", node.municipality.isNotBlank())
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

        val nodeSoya = HokkaidoAct1Route.nodes.firstOrNull { it.name.contains("宗谷岬") && it.name.contains("碑") }
        assertNotNull(nodeSoya)
        assertEquals("日本最北端の地の碑（宗谷岬）", nodeSoya?.name)
        assertEquals(SugorokuNodeType.SCENIC_SPOT, nodeSoya?.type)

        val nodeGoal = HokkaidoAct1Route.getNodeByStepIndex(HokkaidoAct1Route.maxStepIndex)
        assertNotNull(nodeGoal)
        assertEquals("相泊橋（最果ての終着点・車道終点）", nodeGoal?.name)

        assertNull(HokkaidoAct1Route.getNodeByStepIndex(-1))
        assertNull(HokkaidoAct1Route.getNodeByStepIndex(HokkaidoAct1Route.totalNodes))
    }

    @Test
    fun getNodeById_returnsCorrectNode() {
        val nodeSoya = HokkaidoAct1Route.nodes.firstOrNull { it.name.contains("宗谷岬") && it.name.contains("碑") }
        assertNotNull(nodeSoya)
        val fetchedNode = HokkaidoAct1Route.getNodeById(nodeSoya!!.id)
        assertNotNull(fetchedNode)
        assertEquals(nodeSoya.stepIndex, fetchedNode?.stepIndex)
        assertEquals("日本最北端の地の碑（宗谷岬）", fetchedNode?.name)

        assertNull(HokkaidoAct1Route.getNodeById("non_existent_node"))
    }

    @Test
    fun anchors_areRank1AndHaveUniqueSemanticIds() {
        val anchors = HokkaidoAct1Route.anchors
        assertEquals(HokkaidoAct1Route.totalNodes, anchors.size)
        assertEquals("hakodate_station", anchors.first().id)
        assertEquals("aidomari_bridge", anchors.last().id)

        anchors.forEach { anchor ->
            assertEquals(1, anchor.rank)
            assertTrue("Anchor id should not be blank", anchor.id.isNotBlank())
            assertTrue("Anchor name should not be blank", anchor.name.isNotBlank())
            assertTrue("Anchor municipality should not be blank", anchor.municipality.isNotBlank())
        }
        val ids = anchors.map { it.id }.toSet()
        assertEquals(anchors.size, ids.size)
    }

    @Test
    fun nextAndPrevAnchor_navigateCorrectly() {
        val start = HokkaidoAct1Route.startAnchor
        val next = HokkaidoAct1Route.getNextAnchor(start.id)
        assertNotNull(next)
        assertEquals("goryokaku_park", next?.id)

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
                    node.isAnchor
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
    }
}
