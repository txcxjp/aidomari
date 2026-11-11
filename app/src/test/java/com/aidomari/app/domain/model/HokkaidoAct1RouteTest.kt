package com.aidomari.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HokkaidoAct1RouteTest {

    @Test
    fun route_hasExact221Nodes() {
        assertEquals(221, HokkaidoAct1Route.totalNodes)
        assertEquals(221, HokkaidoAct1Route.routeNodes.size)
        assertEquals(220, HokkaidoAct1Route.maxStepIndex)
    }

    @Test
    fun startNode_isHakodateStation() {
        val startNode = HokkaidoAct1Route.startNode
        assertEquals("node_0000", startNode.id)
        assertEquals(0, startNode.stepIndex)
        assertEquals("函館駅前", startNode.name)
        assertEquals(SugorokuNodeType.START_HAKODATE, startNode.type)
        assertTrue(startNode.isAnchor)
    }

    @Test
    fun goalNode_isAidomari() {
        val goalNode = HokkaidoAct1Route.goalNode
        assertEquals("node_0220", goalNode.id)
        assertEquals(220, goalNode.stepIndex)
        assertEquals("相泊（最果ての終着点・相泊温泉）", goalNode.name)
        assertEquals(SugorokuNodeType.GOAL_AIDOMARI, goalNode.type)
        assertTrue(goalNode.isAnchor)
    }

    @Test
    fun allNodes_haveUniqueIdsAndSequentialStepIndices() {
        val ids = HokkaidoAct1Route.routeNodes.map { it.id }.toSet()
        assertEquals(221, ids.size)

        HokkaidoAct1Route.routeNodes.forEachIndexed { index, node ->
            assertEquals("Node at index $index must have stepIndex $index", index, node.stepIndex)
            assertTrue("Node at index $index should have non-empty name", node.name.isNotBlank())
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

        val nodeSoya = HokkaidoAct1Route.getNodeById("node_0144")
        assertNotNull(nodeSoya)
        assertEquals("日本最北端の地の碑（宗谷岬）", nodeSoya?.name)
        assertEquals(SugorokuNodeType.SCENIC_SPOT, nodeSoya?.type)

        val nodeShosanbetsu = HokkaidoAct1Route.getNodeById("node_0110")
        assertNotNull(nodeShosanbetsu)
        assertEquals("みさき台公園", nodeShosanbetsu?.name)
        assertTrue(nodeShosanbetsu?.isAnchor == true)

        val nodeGoal = HokkaidoAct1Route.getNodeByStepIndex(220)
        assertNotNull(nodeGoal)
        assertEquals("相泊（最果ての終着点・相泊温泉）", nodeGoal?.name)

        assertNull(HokkaidoAct1Route.getNodeByStepIndex(-1))
        assertNull(HokkaidoAct1Route.getNodeByStepIndex(221))
    }

    @Test
    fun getNodeById_returnsCorrectNode() {
        val node = HokkaidoAct1Route.getNodeById("node_0144")
        assertNotNull(node)
        assertEquals(144, node?.stepIndex)
        assertEquals("日本最北端の地の碑（宗谷岬）", node?.name)

        assertNull(HokkaidoAct1Route.getNodeById("non_existent_node"))
    }

    @Test
    fun anchors_areCorrectlyDefinedAndMonotonic() {
        val anchors = HokkaidoAct1Route.anchors
        assertEquals(221, anchors.size)
        assertEquals(0, anchors.first().stepIndex)
        assertEquals(220, anchors.last().stepIndex)

        for (i in 0 until anchors.size - 1) {
            assertTrue(
                "Anchors must be strictly monotonic: ${anchors[i].stepIndex} < ${anchors[i+1].stepIndex}",
                anchors[i].stepIndex < anchors[i + 1].stepIndex
            )
            assertEquals("Anchors must be sequential 0..220", i, anchors[i].stepIndex)
        }
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
