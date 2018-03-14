package indi.yume.tools.fragmentmanager.event

import indi.yume.tools.fragmentmanager.ClassCreator
import indi.yume.tools.fragmentmanager.StackManager
import indi.yume.tools.fragmentmanager.defaultSubscribe
import indi.yume.tools.fragmentmanager.functions.toTrunk
import indi.yume.tools.fragmentmanager.model.ActivityStackState
import org.junit.Test

import indi.yume.tools.fragmentmanager.model.ItemState
import indi.yume.tools.fragmentmanager.model.ManagerState
import io.reactivex.Completable

import org.junit.Assert.*

/**
 * Created by yume on 17-4-6.
 */
class ActionTest {
    @Test
    @Throws(Exception::class)
    fun addReduceTest() {
        val state = ActivityStackState.empty()
        val addState1 = ItemState.empty("tag1", ClassCreator(String::class.java))
        val add1Action = AddAction("tag1", addState1)
        val newState = add1Action.reduce(state)

        assertTrue(newState.stackMap.containsKey("tag1"))
        assertTrue(newState.stackMap.size == 1)
        assertTrue(newState.stackMap["tag1"]?.size == 1)
        assertTrue(newState.stackMap["tag1"]?.last() === addState1)

        val addState2 = ItemState.empty("tag1", addState1.hashTag, ClassCreator(String::class.java))
        val add2Action = AddAction("tag1", addState2)
        val newState2 = add2Action.reduce(newState)

        assertTrue(newState2.stackMap.containsKey("tag1"))
        assertTrue(newState2.stackMap.size == 1)
        assertTrue(newState2.stackMap["tag1"]?.size == 2)
        assertTrue(newState2.toString(), newState2.stackMap["tag1"]?.last() === addState2)

        val addState3 = ItemState.empty("tag1", addState1.hashTag, ClassCreator(String::class.java))
        val add3Action = AddAction("tag1", addState3)
        val newState3 = add3Action.reduce(newState2)

        assertTrue(newState3.stackMap.containsKey("tag1"))
        assertTrue(newState3.stackMap.size == 1)
        assertTrue(newState3.stackMap["tag1"]?.size == 3)
        assertTrue(newState3.stackMap["tag1"]?.last() === addState2)
        assertTrue(newState3.stackMap["tag1"]?.get(1) === addState3)
    }

    @Test
    @Throws(Exception::class)
    fun normalActionTest() {
        val stackManager = StackManager()

        stackManager.dispatch(LambdaAction(reducer = { it.copy(activityMap = it.activityMap + ("0" to ActivityStackState())) }))
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertValue { it.newState.activityMap.size == 1 }
    }

    @Test
    @Throws(Exception::class)
    fun listActionTest() {
        val stackManager = StackManager()

        var count = 0
        val action = LambdaAction(reducer = { it.copy(activityMap = it.activityMap + (count.toString() to ActivityStackState())) },
                effectFun = { _, _, _ -> Completable.fromCallable { count++ } }).toTrunk()

        stackManager.dispatch(TransactionAction(List(100, { _ -> action })))
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertValue { it.newState.activityMap.size == 100 }
    }

    @Test
    @Throws(Exception::class)
    fun genActionTest() {
        val stackManager = StackManager()

        var count = 0
        stackManager.dispatch(GenAction(
                initAction = { state -> LambdaAction(reducer = { it.copy(activityMap = it.activityMap + (count.toString() to ActivityStackState())) }) },
                generator = { state, action ->
                    count++
                    if (count >= 10000)
                        null
                    else
                        LambdaAction(reducer = { it.copy(activityMap = it.activityMap + (count.toString() to ActivityStackState())) })
                }
        ))
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertValue {
                    println("result size=${it.newState.activityMap.size}")
                    it.newState.activityMap.size == 10000
                }
    }
}