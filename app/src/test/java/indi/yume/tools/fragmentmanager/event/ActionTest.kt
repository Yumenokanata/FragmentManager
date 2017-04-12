package indi.yume.tools.fragmentmanager.event

import org.junit.Test

import indi.yume.tools.fragmentmanager.model.ItemState
import indi.yume.tools.fragmentmanager.model.ManagerState

import org.junit.Assert.*

/**
 * Created by yume on 17-4-6.
 */
class ActionTest {
    @Test
    @Throws(Exception::class)
    fun addReduceTest() {
        val state = ManagerState.empty()
        val addState1 = ItemState.empty("tag1", String::class.java)
        val add1Action = AddAction("tag1", addState1)
        val newState = add1Action.reduce(state)

        assertTrue(newState.stackMap.containsKey("tag1"))
        assertTrue(newState.stackMap.size == 1)
        assertTrue(newState.stackMap["tag1"]?.size == 1)
        assertTrue(newState.stackMap["tag1"]?.last() === addState1)

        val addState2 = ItemState.empty("tag1", addState1.hashTag, String::class.java)
        val add2Action = AddAction("tag1", addState2)
        val newState2 = add2Action.reduce(newState)

        assertTrue(newState2.stackMap.containsKey("tag1"))
        assertTrue(newState2.stackMap.size == 1)
        assertTrue(newState2.stackMap["tag1"]?.size == 2)
        assertTrue(newState2.toString(), newState2.stackMap["tag1"]?.last() === addState2)

        val addState3 = ItemState.empty("tag1", addState1.hashTag, String::class.java)
        val add3Action = AddAction("tag1", addState3)
        val newState3 = add3Action.reduce(newState2)

        assertTrue(newState3.stackMap.containsKey("tag1"))
        assertTrue(newState3.stackMap.size == 1)
        assertTrue(newState3.stackMap["tag1"]?.size == 3)
        assertTrue(newState3.stackMap["tag1"]?.last() === addState2)
        assertTrue(newState3.stackMap["tag1"]?.get(1) === addState3)
    }

}