package com.aidomari.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HokkaidoAct1RouteTest {

    @Test
    fun route_hasExact2000Nodes() {
        assertEquals(2000, HokkaidoAct1Route.totalNodes)
        assertEquals(2000, HokkaidoAct1Route.routeNodes.size)
        assertEquals(1999, HokkaidoAct1Route.maxStepIndex)
    }

    @Test
    fun startNode_isHakodateStation() {
        val startNode = HokkaidoAct1Route.startNode
        assertEquals("node_0000", startNode.id)
        assertEquals(0, startNode.stepIndex)
        assertEquals("函館駅前", startNode.name)
        assertEquals(SugorokuNodeType.START_HAKODATE, startNode.type)
    }

    @Test
    fun goalNode_isAidomari() {
        val goalNode = HokkaidoAct1Route.goalNode
        assertEquals("node_1999", goalNode.id)
        assertEquals(1999, goalNode.stepIndex)
        assertEquals("相泊（最果ての終着点・相泊温泉）", goalNode.name)
        assertEquals(SugorokuNodeType.GOAL_AIDOMARI, goalNode.type)
    }

    @Test
    fun allNodes_haveUniqueIdsAndSequentialStepIndices() {
        val ids = HokkaidoAct1Route.routeNodes.map { it.id }.toSet()
        assertEquals(2000, ids.size)

        HokkaidoAct1Route.routeNodes.forEachIndexed { index, node ->
            assertEquals("Node at index $index must have stepIndex $index", index, node.stepIndex)
            if (node.isAnchor) {
                assertTrue("Anchor node at index $index should have non-empty name", node.name.isNotBlank())
            }
            assertTrue("Node at index $index x coordinate should be within 0..2000", node.x in 0f..2000f)
            assertTrue("Node at index $index y coordinate should be within 0..2000", node.y in 0f..2000f)
        }
    }

    @Test
    fun getNodeByStepIndex_returnsCorrectNode() {
        val node0 = HokkaidoAct1Route.getNodeByStepIndex(0)
        assertNotNull(node0)
        assertEquals("函館駅前", node0?.name)

        val nodeSoya = HokkaidoAct1Route.getNodeById("node_1218")
        assertNotNull(nodeSoya)
        assertEquals("日本最北端の地の碑（宗谷岬）", nodeSoya?.name)
        assertEquals(SugorokuNodeType.SCENIC_SPOT, nodeSoya?.type)

        val nodeShosanbetsu = HokkaidoAct1Route.getNodeById("node_0966")
        assertNotNull(nodeShosanbetsu)
        assertEquals("みさき台公園", nodeShosanbetsu?.name)
        assertTrue(nodeShosanbetsu?.isAnchor == true)

        val node1999 = HokkaidoAct1Route.getNodeByStepIndex(1999)
        assertNotNull(node1999)
        assertEquals("相泊（最果ての終着点・相泊温泉）", node1999?.name)

        assertNull(HokkaidoAct1Route.getNodeByStepIndex(-1))
        assertNull(HokkaidoAct1Route.getNodeByStepIndex(2000))
    }

    @Test
    fun getNodeById_returnsCorrectNode() {
        val node = HokkaidoAct1Route.getNodeById("node_1218")
        assertNotNull(node)
        assertEquals(1218, node?.stepIndex)
        assertEquals("日本最北端の地の碑（宗谷岬）", node?.name)

        assertNull(HokkaidoAct1Route.getNodeById("non_existent_node"))
    }

    @Test
    fun anchors_areCorrectlyDefinedAndMonotonic() {
        val anchors = HokkaidoAct1Route.anchors
        assertEquals(221, anchors.size)
        assertEquals(0, anchors.first().stepIndex)
        assertEquals(1999, anchors.last().stepIndex)

        for (i in 0 until anchors.size - 1) {
            assertTrue(
                "Anchors must be strictly monotonic: ${anchors[i].stepIndex} < ${anchors[i+1].stepIndex}",
                anchors[i].stepIndex < anchors[i + 1].stepIndex
            )
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
    fun removedObsoleteAnchors_areNotAnchors() {
        val anchorStepSet = HokkaidoAct1Route.anchors.map { it.stepIndex }.toSet()
        // Step 785 (明元町) and Step 820 (中央町) must not be anchors
        assertTrue("Step 785 (明元町) should not be an anchor", 785 !in anchorStepSet)
        assertTrue("Step 820 (中央町) should not be an anchor", 820 !in anchorStepSet)
        assertTrue("Step 566 (小樽ドリームビーチ) should not be an anchor", 566 !in anchorStepSet)
    }

    @Test
    fun anchorNodes_haveIsAnchorFlagCorrectlySet() {
        val anchorStepSet = HokkaidoAct1Route.anchors.map { it.stepIndex }.toSet()
        HokkaidoAct1Route.routeNodes.forEach { node ->
            if (node.stepIndex in anchorStepSet) {
                assertTrue("Node ${node.stepIndex} should have isAnchor == true", node.isAnchor)
            } else {
                assertTrue("Node ${node.stepIndex} should have isAnchor == false", !node.isAnchor)
            }
        }
    }

    @Test
    fun interpolatedNodes_areCollinearBetweenAnchors() {
        val anchors = HokkaidoAct1Route.anchors
        for (i in 0 until anchors.size - 1) {
            val a1 = anchors[i]
            val a2 = anchors[i + 1]
            val count = a2.stepIndex - a1.stepIndex

            for (offset in 0..count) {
                val step = a1.stepIndex + offset
                val node = HokkaidoAct1Route.getNodeByStepIndex(step)
                assertNotNull(node)

                val t = offset.toFloat() / count.toFloat()
                val expectedX = a1.x + t * (a2.x - a1.x)
                val expectedY = a1.y + t * (a2.y - a1.y)

                assertEquals("X for step $step should match lerp", expectedX, node!!.x, 0.01f)
                assertEquals("Y for step $step should match lerp", expectedY, node.y, 0.01f)
            }
        }
    }
}
