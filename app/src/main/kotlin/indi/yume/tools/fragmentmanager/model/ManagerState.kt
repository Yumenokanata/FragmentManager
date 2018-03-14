package indi.yume.tools.fragmentmanager.model

import indi.yume.tools.fragmentmanager.ActivityKey
import indi.yume.tools.fragmentmanager.StackKey
import indi.yume.tools.fragmentmanager.FragmentKey

/**
 * Created by yume on 17-4-11.
 *
 */

data class ManagerState(
        val activityMap: Map<ActivityKey, ActivityStackState> = emptyMap(),
        val activityList : List<ActivityKey> = emptyList()) {

    val currentActivity : ActivityKey?
        get() = activityList.lastOrNull()

    fun getTopState(): ActivityStackState? = currentActivity?.let { activityMap[it] }

    fun getState(key: ActivityKey): ActivityStackState? = activityMap[key]

    fun doForTargetItem(key: ActivityKey?, f: (ActivityStackState) -> ActivityStackState): ManagerState {
        if(key == null) return this
        val item = getState(key)
        return if(item != null)
            copy(activityMap = activityMap + (key to f(item)))
        else
            this
    }

    fun doForTopItem(f: (ActivityKey, ActivityStackState) -> ActivityStackState): ManagerState =
            currentActivity?.let { key -> doForTargetItem(key, { f(key, it) }) } ?: this

    fun onCreate(key: ActivityKey, state: ActivityStackState = ActivityStackState(emptyMap(), null)): ManagerState =
            ManagerState(
                    activityMap = activityMap - key + (key to state),
                    activityList = activityList - key + key
            )

    fun onDestroy(key: ActivityKey): ManagerState =
            ManagerState(
                    activityMap = activityMap - key,
                    activityList = activityList - key
            )

    companion object {
        @JvmStatic fun empty(): ManagerState = ManagerState(emptyMap(), emptyList())
    }
}

data class ActivityStackState(
        val stackMap : Map<StackKey, List<ItemState>> = emptyMap(),
        val currentStack : StackKey? = null
) {
    fun getCurrentStack(): List<ItemState>? = currentStack?.let { stackMap.get(it) }

    fun isCurrentStackEmpty(): Boolean = currentStack?.let { stackMap.get(it)?.isEmpty() ?: true } ?: false

    fun getCurrentTop(): ItemState? = currentStack?.let { getTop(it) }

    fun getStack(fragmentKey: FragmentKey): StackKey? =
            stackMap.entries.firstOrNull { it.value.any { it.hashTag == fragmentKey } }?.key

    fun plus(tag: StackKey, item: ItemState): ActivityStackState =
            copy(stackMap + (tag to (stackMap.get(tag) ?: emptyList()) + item))

    fun plusAt(tag: StackKey, index: Int, item: ItemState): ActivityStackState {
        val stack = stackMap.get(tag) ?: emptyList()
        if(index < 0 || index >= stack.size)
            return plus(tag, item)
        else
            return copy(stackMap + (tag to (stack.take(index) + item + stack.takeLast(stack.size - index))))
    }

    fun getIndex(tag: StackKey, itemTag: FragmentKey): Pair<Int, ItemState?> =
            stackMap.get(tag)?.withIndex()?.find { it.value.hashTag == itemTag }?.run { index to value } ?: -1 to null

    fun modifyItem(tag: StackKey, itemTag: FragmentKey, func: (ItemState) -> ItemState): ActivityStackState {
        val newStack = stackMap.get(tag)?.map { if (it.hashTag == itemTag) func(it) else it } ?: emptyList()
        return if(newStack.isEmpty())
            this
        else
            copy(stackMap + (tag to newStack))
    }

    fun minusTop(tag: StackKey): Pair<ActivityStackState, ItemState?> {
        val stack = stackMap.get(tag) ?: emptyList()
        if (stack.isEmpty()) return this to null

        return copy(stackMap + (tag to stack.dropLast(1))) to stack.last()
    }

    fun minus(tag: StackKey, itemTag: FragmentKey): Pair<ActivityStackState, ItemState?> {
        val stack = stackMap.get(tag) ?: emptyList()
        if (stack.isEmpty()) return this to null

        val minusItem = stack.find { it.hashTag == itemTag }

        return if (minusItem == null)
            this to null
        else
            copy(stackMap + (tag to stack - minusItem)) to minusItem
    }

    fun clear(tag: StackKey): ActivityStackState = copy(stackMap - tag)

    fun getTop(tag: StackKey): ItemState? {
        val stack = stackMap.get(tag) ?: emptyList()
        if (stack.isEmpty()) return null

        return stack.last()
    }

    fun getItem(tag: StackKey, itemTag: FragmentKey?): ItemState? {
        val stack = stackMap.get(tag) ?: emptyList()
        if (stack.isEmpty()) return null

        return stack.find { it.hashTag == itemTag }
    }

    fun getBack(tag: StackKey, itemTag: FragmentKey): ItemState? {
        val stack = stackMap.get(tag) ?: emptyList()
        if (stack.isEmpty()) return null

        val index = stack.indexOfFirst { it.hashTag == itemTag }
        return if(index <= 0) null else stack[index - 1]
    }

    companion object {
        @JvmStatic fun empty(): ActivityStackState = ActivityStackState(emptyMap<String, List<ItemState>>(), null)
    }
}