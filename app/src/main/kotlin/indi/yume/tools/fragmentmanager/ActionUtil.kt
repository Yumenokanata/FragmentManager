package indi.yume.tools.fragmentmanager

import indi.yume.tools.fragmentmanager.event.*
import indi.yume.tools.fragmentmanager.exception.DoEffectException
import indi.yume.tools.fragmentmanager.functions.ActionTrunk
import indi.yume.tools.fragmentmanager.functions.toTrunk
import indi.yume.tools.fragmentmanager.model.ItemState
import indi.yume.tools.fragmentmanager.model.ManagerState
import io.reactivex.functions.Consumer

/**
 * Created by yume on 17-4-13.
 */

fun StackManager.start(startBuilder: StartBuilder): Unit {
    dispatch { state ->
        val currentTag = state.currentTag
        val backItem = state.getCurrentTop()
        val backItemHashTag = backItem?.hashTag

        if (currentTag == null)
            throw DoEffectException("start new item must at a stack")

        AddAction(currentTag, ItemState(currentTag, backItemHashTag, startBuilder))
    }
}

fun StackManager.deleteItem(targetTag: String, hashTag: String) {
    dispatch { state ->
        val targetItem = state.getItem(targetTag, hashTag)
        targetItem?.run { DeleteAction(targetTag, this.hashTag) } ?: EmptyAction()
    }
}

fun StackManager.switchToStackByTag(tag: String, defaultClass: Class<out BaseManagerFragment>?) {
    dispatch { state ->
        val currentTag = state.currentTag

        SwitchAction(currentTag, tag,
                if(defaultClass != null) ItemState.empty(tag, defaultClass) else null)
    }
}

fun StackManager.restore(state: ManagerState) {
    dispatch(TransactionAction(listOf(
            SwitchAction(targetTag = null).toTrunk(),
            GenAction { s, _ ->
                s.stackMap.nextItem()?.let { DeleteAction(it.stackTag, it.hashTag) }
            }.toTrunk(),
            TransactionAction(state.stackMap.allValue().map { AddAction(it.stackTag, it).toTrunk() }).toTrunk(),
            { if(state.currentTag != null) SwitchAction(targetTag = state.currentTag) else EmptyAction() }
    )))
}

fun StackManager.getState(func: Consumer<ManagerState>) = dispatch(CallbackAction { func.accept(it) })

private fun <K, V> Map<K, List<V>>.allValue(): List<V> {
    val valueLists = values
    if(valueLists.isEmpty())
        return emptyList()

    var sumList = emptyList<V>()
    for(list in valueLists)
        sumList += list

    return sumList
}

private fun <K, V> Map<K, List<V>>.nextItem(): V? {
    val valueLists = values
    if(valueLists.isEmpty())
        return null

    for(list in valueLists)
        if(list.isNotEmpty())
            return list.last()

    return null
}