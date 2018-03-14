package indi.yume.tools.fragmentmanager

import android.support.annotation.CheckResult
import android.support.v4.app.Fragment
import indi.yume.tools.fragmentmanager.event.*
import indi.yume.tools.fragmentmanager.exception.DoEffectException
import indi.yume.tools.fragmentmanager.functions.ActionTrunk
import indi.yume.tools.fragmentmanager.functions.toTrunk
import indi.yume.tools.fragmentmanager.model.ItemState
import indi.yume.tools.fragmentmanager.model.ManagerState
import io.reactivex.Single
import io.reactivex.functions.Consumer

/**
 * Created by yume on 17-4-13.
 */

sealed class FragmentCreator {
    abstract fun create(): Fragment
}

data class ClassCreator(val clazz: Class<*>) : FragmentCreator() {
    override fun create(): Fragment = clazz.newInstance() as Fragment
}

data class InstantsCreator(val fragment: Fragment) : FragmentCreator() {
    override fun create(): Fragment = fragment
}


@CheckResult
fun ActivityItem.start(startBuilder: StartBuilder): Single<StateData> {
    return ApplicationStore.stackManager.dispatch { state ->
        val activityState = state.getState(hashKey) ?: return@dispatch EmptyAction()

        val currentStack = activityState.currentStack
        val backItem = activityState.getCurrentTop()
        val backItemHashTag = backItem?.hashTag

        if (currentStack == null)
            throw DoEffectException("start new item must at a stack")

        AddAction(currentStack, ItemState(currentStack, backItemHashTag, startBuilder))
    }
}

@CheckResult
fun ActivityItem.deleteItem(targetTag: String, hashTag: String): Single<StateData> {
    return ApplicationStore.stackManager.dispatch { state ->
        val activityState = state.getState(hashKey) ?: return@dispatch EmptyAction()
        val targetItem = activityState.getItem(targetTag, hashTag)

        targetItem?.run { DeleteAction(targetTag, this.hashTag) } ?: EmptyAction()
    }
}

@CheckResult
fun ActivityItem.switchToStackByTag(tag: String, defaultFragment: FragmentCreator?): Single<StateData> {
    return ApplicationStore.stackManager.dispatch { state ->
        val activityState = state.getState(hashKey) ?: return@dispatch EmptyAction()
        val currentTag = activityState.currentStack

        SwitchAction(currentTag, tag,
                if(defaultFragment != null) ItemState.empty(tag, defaultFragment) else null)
    }
}

@CheckResult
fun ActivityItem.restore(state: ManagerState): Single<StateData> {
    val activityState = state.getState(hashKey) ?: return ApplicationStore.stackManager.bind().firstOrError()

    return ApplicationStore.stackManager.dispatch(TransactionAction(listOf(
            SwitchAction(targetStack = null).toTrunk(),
            GenAction { s, _ ->
                val middleState = s.getState(hashKey) ?: return@GenAction EmptyAction()
                middleState.stackMap.nextItem()?.let { DeleteAction(it.stackTag, it.hashTag) }
            }.toTrunk(),
            TransactionAction(activityState.stackMap.allValue().map { AddAction(it.stackTag, it).toTrunk() }).toTrunk(),
            { if(activityState.currentStack != null) SwitchAction(targetStack = activityState.currentStack) else EmptyAction() }
    )))
}

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