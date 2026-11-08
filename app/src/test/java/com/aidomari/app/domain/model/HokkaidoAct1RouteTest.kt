package com.aidomari.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HokkaidoAct1RouteTest {

    @Test
    fun route_hasExact113Nodes() {
        assertEquals(113, HokkaidoAct1Route.totalNodes)
        assertEquals(113, HokkaidoAct1Route.routeNodes.size)
        assertEquals(112, HokkaidoAct1Route.maxStepIndex)
    }

    @Test
    fun startNode_isHakodateStation() {
        val startNode = HokkaidoAct1Route.startNode
        assertEquals("node_000", startNode.id)
        assertEquals(0, startNode.stepIndex)
        assertEquals("函館駅前", startNode.name)
        assertEquals(SugorokuNodeType.START_HAKODATE, startNode.type)
    }

    @Test
    fun goalNode_isAidomari() {
        val goalNode = HokkaidoAct1Route.goalNode
        assertEquals("node_112", goalNode.id)
        assertEquals(112, goalNode.stepIndex)
        assertEquals("相泊（最果ての終着点・相泊温泉）", goalNode.name)
        assertEquals(SugorokuNodeType.GOAL_AIDOMARI, goalNode.type)
    }

    @Test
    fun allNodes_haveUniqueIdsAndSequentialStepIndices() {
        val ids = HokkaidoAct1Route.routeNodes.map { it.id }.toSet()
        assertEquals(113, ids.size)

        HokkaidoAct1Route.routeNodes.forEachIndexed { index, node ->
            assertEquals("Node at index $index must have stepIndex $index", index, node.stepIndex)
            assertTrue("Node at index $index should have non-empty name", node.name.isNotBlank())
            assertTrue("Node at index $index x coordinate should be within 0..2000", node.x in 0f..2000f)
            assertTrue("Node at index $index y coordinate should be within 0..2000", node.y in 0f..2000f)
        }
    }

    @Test
    fun getNodeByStepIndex_returnsCorrectNode() {
        val node0 = HokkaidoAct1Route.getNodeByStepIndex(0)
        assertNotNull(node0)
        assertEquals("函館駅前", node0?.name)

        val node65 = HokkaidoAct1Route.getNodeByStepIndex(65)
        assertNotNull(node65)
        assertEquals("宗谷岬（日本最北端の碑）", node65?.name)
        assertEquals(SugorokuNodeType.SCENIC_SPOT, node65?.type)

        val node112 = HokkaidoAct1Route.getNodeByStepIndex(112)
        assertNotNull(node112)
        assertEquals("相泊（最果ての終着点・相泊温泉）", node112?.name)

        assertNull(HokkaidoAct1Route.getNodeByStepIndex(-1))
        assertNull(HokkaidoAct1Route.getNodeByStepIndex(113))
    }

    @Test
    fun getNodeById_returnsCorrectNode() {
        val node = HokkaidoAct1Route.getNodeById("node_065")
        assertNotNull(node)
        assertEquals(65, node?.stepIndex)
        assertEquals("宗谷岬（日本最北端の碑）", node?.name)

        assertNull(HokkaidoAct1Route.getNodeById("non_existent_node"))
    }
}
