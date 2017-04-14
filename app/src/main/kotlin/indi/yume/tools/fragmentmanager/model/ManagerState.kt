package indi.yume.tools.fragmentmanager.model

import android.support.annotation.IntRange

/**
 * Created by yume on 17-4-11.
 *
 */

data class ManagerState(
        val stackMap : Map<String, List<ItemState>>,
        val currentTag : String?
) {
    fun getCurrentStack(): List<ItemState>? = currentTag?.let { stackMap.get(it) }

    fun isCurrentStackEmpty(): Boolean = currentTag?.let { stackMap.get(it)?.isEmpty() ?: true } ?: false

    fun getCurrentTop(): ItemState? = currentTag?.let { getTop(it) }

    fun plus(tag: String, item: ItemState): ManagerState =
            copy(stackMap + (tag to (stackMap.get(tag) ?: emptyList()) + item))

    fun plusAt(tag: String, index: Int, item: ItemState): ManagerState {
        val stack = stackMap.get(tag) ?: emptyList()
        if(index < 0 || index >= stack.size)
            return plus(tag, item)
        else
            return copy(stackMap + (tag to (stack.take(index) + item + stack.takeLast(stack.size - index))))
    }

    fun getIndex(tag: String, itemTag: String): Pair<Int, ItemState?> =
            stackMap.get(tag)?.withIndex()?.find { it.value.hashTag == itemTag }?.run { index to value } ?: -1 to null

    fun modifyItem(tag: String, itemTag: String, func: (ItemState) -> ItemState): ManagerState {
        val newStack = stackMap.get(tag)?.map { if (it.hashTag == itemTag) func(it) else it } ?: emptyList()
        return if(newStack.isEmpty())
            this
        else
            copy(stackMap + (tag to newStack))
    }

    fun minusTop(tag: String): Pair<ManagerState, ItemState?> {
        val stack = stackMap.get(tag) ?: emptyList()
        if (stack.isEmpty()) return this to null

        return copy(stackMap + (tag to stack.dropLast(1))) to stack.last()
    }

    fun minus(tag: String, itemTag: String): Pair<ManagerState, ItemState?> {
        val stack = stackMap.get(tag) ?: emptyList()
        if (stack.isEmpty()) return this to null

        val minusItem = stack.find { it.hashTag == itemTag }

        return if (minusItem == null)
            this to null
        else
            copy(stackMap + (tag to stack - minusItem)) to minusItem
    }

    fun clear(tag: String): ManagerState = copy(stackMap - tag)

    fun getTop(tag: String): ItemState? {
        val stack = stackMap.get(tag) ?: emptyList()
        if (stack.isEmpty()) return null

        return stack.last()
    }

    fun getItem(tag: String, itemTag: String?): ItemState? {
        val stack = stackMap.get(tag) ?: emptyList()
        if (stack.isEmpty()) return null

        return stack.find { it.hashTag == itemTag }
    }

    fun getBack(tag: String, itemTag: String): ItemState? {
        val stack = stackMap.get(tag) ?: emptyList()
        if (stack.isEmpty()) return null

        val index = stack.indexOfFirst { it.hashTag == itemTag }
        return if(index <= 0) null else stack[index - 1]
    }

    companion object {
        @JvmStatic fun empty(): ManagerState = ManagerState(emptyMap<String, List<ItemState>>(), null)
    }
}