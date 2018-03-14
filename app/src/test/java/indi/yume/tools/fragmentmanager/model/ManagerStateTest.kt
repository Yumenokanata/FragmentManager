package indi.yume.tools.fragmentmanager.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Created by yume on 17-4-12.
 */

val TAGS = arrayOf("tag0", "tag1", "tag2", "tag3")
val ERROR_TAG = "ErrorTag"
val ERROR_HASH_TAG = "ErrorHashTag"

val state: ActivityStackState = ActivityStackState(
        mapOf(
                TAGS[0] to listOf(),
                TAGS[1] to listOf(ItemState.empty(TAGS[1], String::class.java)),
                TAGS[2] to listOf(ItemState.empty(TAGS[2], String::class.java), ItemState.empty(TAGS[2], String::class.java)),
                TAGS[3] to listOf(ItemState.empty(TAGS[3], String::class.java), ItemState.empty(TAGS[3], String::class.java), ItemState.empty(TAGS[3], String::class.java))
        ),
        null
)

val newTag0Item = ItemState.empty(TAGS[0], String::class.java)
val newTag1Item = ItemState.empty(TAGS[1], String::class.java)
val newTag2Item = ItemState.empty(TAGS[2], String::class.java)

class ManagerStateTest {

    @Test
    fun getCurrentStackTest() {
        assertEquals(null, state.getCurrentStack())

        val newState = state.copy(currentStack = TAGS[0])
        assertEquals(newState.stackMap.get(TAGS[0]), newState.getCurrentStack())
    }

    @Test
    fun isCurrentStackEmptyTest() {
        assert(state.isCurrentStackEmpty())

        val tag0State = state.copy(currentStack = TAGS[0])
        assert(tag0State.isCurrentStackEmpty())

        val tag1State = state.copy(currentStack = TAGS[1])
        assertFalse(tag1State.isCurrentStackEmpty())
    }

    @Test
    fun getCurrentTopTest() {
        assertEquals(null, state.getCurrentTop())

        val tag0State = state.copy(currentStack = TAGS[0])
        assertEquals(null, tag0State.getCurrentTop())

        val tag1State = state.copy(currentStack = TAGS[1])
        assertEquals(tag1State.stackMap.get(TAGS[1])!!.last(), tag1State.getCurrentTop())
    }

    @Test
    fun plusTest() {
        assertEquals(state.copy(state.stackMap + (ERROR_TAG to listOf(newTag0Item))),
                state.plus(ERROR_TAG, newTag0Item))

        assertEquals(state.copy(state.stackMap + (TAGS[0] to (state.stackMap[TAGS[0]] ?: emptyList()) + newTag0Item)),
                state.plus(TAGS[0], newTag0Item))

        assertEquals(state.copy(state.stackMap + (TAGS[1] to (state.stackMap[TAGS[1]] ?: emptyList()) + newTag1Item)),
                state.plus(TAGS[1], newTag1Item))
    }

    @Test
    fun plusAtTest() {
        assertEquals(state.copy(state.stackMap + (ERROR_TAG to listOf(newTag0Item))),
                state.plusAt(ERROR_TAG, 0, newTag0Item))

        assertEquals(state.copy(state.stackMap + (TAGS[0] to listOf(newTag0Item))),
                state.plusAt(TAGS[0], -1, newTag0Item))

        val index0_Item = state.stackMap[TAGS[1]]!![0]

        assertEquals(listOf(newTag1Item, index0_Item),
                state.plusAt(TAGS[1], 0, newTag1Item).stackMap[TAGS[1]])
        assertEquals(listOf(index0_Item, newTag1Item),
                state.plusAt(TAGS[1], 1, newTag1Item).stackMap[TAGS[1]])
        assertEquals(listOf(index0_Item, newTag1Item),
                state.plusAt(TAGS[1], 2, newTag1Item).stackMap[TAGS[1]])
    }

    @Test
    fun getIndexTest() {
        val index0Item = state.stackMap[TAGS[1]]!![0]
        val index1Item = state.stackMap[TAGS[2]]!![1]
        val index2Item = state.stackMap[TAGS[3]]!![2]

        assertEquals(0 to index0Item, state.getIndex(TAGS[1], index0Item.hashTag))
        assertEquals(1 to index1Item, state.getIndex(TAGS[2], index1Item.hashTag))
        assertEquals(2 to index2Item, state.getIndex(TAGS[3], index2Item.hashTag))

        assertEquals(-1 to null, state.getIndex(TAGS[3], ERROR_HASH_TAG))
    }

    @Test
    fun modifyItemTest() {
        val index1_OldItem = state.stackMap[TAGS[2]]!![1]
        val newState = state.modifyItem(TAGS[2], index1_OldItem.hashTag, { newTag2Item })
        assertEquals(newState.stackMap[TAGS[2]]!![1], newTag2Item)

        assertEquals(state, state.modifyItem(ERROR_TAG, index1_OldItem.hashTag, { newTag2Item }))
        assertEquals(state, state.modifyItem(TAGS[2], ERROR_HASH_TAG, { newTag2Item }))
    }

    @Test
    fun minusTopTest() {
        val index0_Item = state.stackMap[TAGS[3]]!![0]
        val index1_Item = state.stackMap[TAGS[3]]!![1]
        val index2_Item = state.stackMap[TAGS[3]]!![2]
        val rightState = state.copy(state.stackMap + (TAGS[3] to state.stackMap[TAGS[3]]!!.dropLast(1)))
        val testStatePair = state.minusTop(TAGS[3])
        assertEquals(rightState to index2_Item, testStatePair)
        assertEquals(testStatePair.first.stackMap[TAGS[3]]!![0], index0_Item)
        assertEquals(testStatePair.first.stackMap[TAGS[3]]!![1], index1_Item)

        assertEquals(state to null, state.minusTop(ERROR_TAG))
        assertEquals(state to null, state.minusTop(TAGS[0]))
    }

    @Test
    fun minusTest() {
        val index0_Item = state.stackMap[TAGS[3]]!![0]
        val index1_Item = state.stackMap[TAGS[3]]!![1]
        val index2_Item = state.stackMap[TAGS[3]]!![2]
        val rightState = state.copy(state.stackMap + (TAGS[3] to state.stackMap[TAGS[3]]!!.filterIndexed { index, _ -> index != 1 }))
        val testStatePair = state.minus(TAGS[3], index1_Item.hashTag)
        assertEquals(rightState to index1_Item, testStatePair)
        assertEquals(testStatePair.first.stackMap[TAGS[3]]!![0], index0_Item)
        assertEquals(testStatePair.first.stackMap[TAGS[3]]!![1], index2_Item)

        assertEquals(state to null, state.minus(TAGS[3], ERROR_HASH_TAG))
        assertEquals(state to null, state.minus(TAGS[0], index1_Item.hashTag))
    }

    @Test
    fun clearTest() {
        assert(state.clear(TAGS[0]).stackMap[TAGS[0]]?.isEmpty() ?: true)
        assert(state.clear(TAGS[1]).stackMap[TAGS[1]]?.isEmpty() ?: true)
        assert(state.clear(TAGS[3]).stackMap[TAGS[3]]?.isEmpty() ?: true)
        assertEquals(state.copy(state.stackMap - TAGS[3]), state.clear(TAGS[3]))
        assertEquals(state, state.clear(ERROR_TAG))
    }

    @Test
    fun getTopTest() {
        val tag1_TopItem = state.stackMap[TAGS[1]]!!.last()
        val tag2_TopItem = state.stackMap[TAGS[2]]!!.last()
        val tag3_TopItem = state.stackMap[TAGS[3]]!!.last()

        assertEquals(tag1_TopItem, state.getTop(TAGS[1]))
        assertEquals(tag2_TopItem, state.getTop(TAGS[2]))
        assertEquals(tag3_TopItem, state.getTop(TAGS[3]))

        assertEquals(null, state.getTop(TAGS[0]))
        assertEquals(null, state.getTop(ERROR_TAG))
    }

    @Test
    fun getItemTest() {
        val index0_Item = state.stackMap[TAGS[3]]!![0]
        val index1_Item = state.stackMap[TAGS[3]]!![1]
        val index2_Item = state.stackMap[TAGS[3]]!![2]

        assertEquals(index0_Item, state.getItem(TAGS[3], index0_Item.hashTag))
        assertEquals(index1_Item, state.getItem(TAGS[3], index1_Item.hashTag))
        assertEquals(index2_Item, state.getItem(TAGS[3], index2_Item.hashTag))

        assertEquals(null, state.getItem(TAGS[3], ERROR_HASH_TAG))
        assertEquals(null, state.getItem(ERROR_TAG, index2_Item.hashTag))
    }

    @Test
    fun getBackTest() {
        val index0_Item = state.stackMap[TAGS[3]]!![0]
        val index1_Item = state.stackMap[TAGS[3]]!![1]
        val index2_Item = state.stackMap[TAGS[3]]!![2]

        assertEquals(null, state.getBack(TAGS[3], index0_Item.hashTag))
        assertEquals(index0_Item, state.getBack(TAGS[3], index1_Item.hashTag))
        assertEquals(index1_Item, state.getBack(TAGS[3], index2_Item.hashTag))

        assertEquals(null, state.getBack(TAGS[3], ERROR_HASH_TAG))
        assertEquals(null, state.getBack(ERROR_TAG, index2_Item.hashTag))
    }
}