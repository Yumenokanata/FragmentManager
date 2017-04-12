package indi.yume.tools.fragmentmanager.event

import android.os.Bundle
import indi.yume.tools.fragmentmanager.*
import indi.yume.tools.fragmentmanager.exception.DoEffectException
import indi.yume.tools.fragmentmanager.model.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import lombok.experimental.Wither

/**
 * Created by yume on 17-4-10.
 */

sealed class Action {
    abstract fun reduce(oldState: ManagerState): ManagerState

    abstract fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable
}

data class AddAction(val targetTag: String, val foreItem: ItemState) : Action() {

    override fun reduce(oldState: ManagerState): ManagerState {
        val index = if(foreItem.backItemHashTag != null)
            oldState.getIndex(targetTag, foreItem.backItemHashTag).first
        else -1

        return if(index >= 0)
            oldState.plusAt(targetTag, index + 1, foreItem)
        else
            oldState.plus(targetTag, foreItem)
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable {
        val currentTag = newState.currentTag
        val backItem = newState.getItem(targetTag, foreItem.backItemHashTag)

        if (newState.getTop(targetTag) === foreItem) {
            if (backItem != null && currentTag === targetTag)
                return commit(realWorld.fragmentManager) { transaction ->
                    val foreFrag = getItem(foreItem)(realWorld).second(transaction).second
                    val backFrag = getItem(backItem)(realWorld).second(transaction).second

                    showFragmentWithAnim(foreFrag to foreItem, backFrag to backItem)(realWorld)
                            .second(transaction)
                            .second
                }

            //Has back item but target tag is not current tag.
            if (backItem != null)
                return commit(realWorld.fragmentManager) { transaction ->
                    val foreFrag = getItem(foreItem)(realWorld).second(transaction).second
                    val backFrag = getItem(backItem)(realWorld).second(transaction).second

                    showFragmentNoAnim(foreFrag, backFrag)(realWorld)
                            .second(transaction)
                            .second
                }

            //No back item or target tag is not current tag.
            return commit(realWorld.fragmentManager) { transaction ->
                val foreFrag = getItem(foreItem)(realWorld).second(transaction).second

                showFragmentNoAnim(foreFrag)(realWorld)
                        .second(transaction)
                        .second
            }
        } else {
            //Add item must at stack top, else do nothing
            return Completable.error(DoEffectException("Add item must at stack top"))
        }
    }
}

data class DeleteAction(val targetTag: String,
                        val targetHashTag: String) : Action() {

    override fun reduce(oldState: ManagerState): ManagerState {
        return oldState.minus(targetTag, targetHashTag).first
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable {
        val currentTag = newState.currentTag
        val foreItem = oldState.getItem(targetTag, targetHashTag) ?: return Completable.complete()

        val backItem = newState.getItem(targetTag, foreItem.backItemHashTag) ?: newState.getTop(targetTag)

        if (foreItem.isBackItem(backItem) && currentTag == targetTag) {
            return commit(realWorld.fragmentManager) { transaction ->
                val foreFrag = getItem(foreItem)(realWorld).second(transaction).second
                val backFrag = if(backItem != null) getItem(backItem)(realWorld).second(transaction).second else null
                val backData = if(backFrag != null && backItem != null) backFrag to backItem else null

                removeFragmentWithAnim(foreFrag to foreItem, backData)(realWorld)
                        .second(transaction)
                        .second
                        .subscribeOn(AndroidSchedulers.mainThread())
            }
        } else {
            return commit(realWorld.fragmentManager) { transaction ->
                val foreFrag = getItem(foreItem)(realWorld).second(transaction).second
                val backFrag = if(backItem != null) getItem(backItem)(realWorld).second(transaction).second else null
                val backData = if(backFrag != null && backItem != null) backFrag to backItem else null

                removeFragmentNoAnim(foreFrag to foreItem, backData)(realWorld)
                        .second(transaction)
                        .second
            }
        }
    }
}

class EmptyAction : Action() {
    override fun reduce(oldState: ManagerState): ManagerState = oldState

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable =
            Completable.complete()
}

data class ModifyAction(
        val targetTag: String,
        val itemTag: String,
        val animData: AnimData?,
        val resultCode: Int,
        val resultData: Bundle?
) : Action() {

    constructor(state: ItemState): this(
            targetTag = state.stackTag,
            itemTag = state.hashTag,
            animData = state.animData,
            resultCode = state.resultCode,
            resultData = state.resultData
    )

    override fun reduce(oldState: ManagerState): ManagerState =
        oldState.modifyItem(targetTag, itemTag) { oldItem ->
            oldItem.copy(animData = animData,
                    resultCode = resultCode,
                    resultData = resultData)
        }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable =
            Completable.complete()
}

data class SwitchAction(
        val backTag: String? = null,
        val targetTag: String,
        val defaultItem: ItemState? = null
) : Action() {

    override fun reduce(oldState: ManagerState): ManagerState {
        val newState = oldState.copy(currentTag = targetTag)
        if (defaultItem != null && newState.isCurrentStackEmpty())
            return newState.plus(targetTag, defaultItem)
        return newState
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable {
        val backItem = backTag?.run { newState.getTop(this) }
        val foreItem = newState.getTop(targetTag)

        if (backItem != null || foreItem != null)
            return commit(realWorld.fragmentManager) { transaction ->
                val foreFrag = foreItem?.let { getItem(it)(realWorld).second(transaction).second }
                val backFrag = backItem?.let { getItem(it)(realWorld).second(transaction).second }

                switchTag(foreFrag, backFrag)(realWorld)
                        .second(transaction)
                        .second
            }

        return Completable.complete()
    }
}

class FinishAction : Action() {
    override fun reduce(oldState: ManagerState): ManagerState {
        return oldState
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable =
            Completable.create { e ->
                realWorld.activity.finish()
                e.onComplete()
            }
}

class BackAction : Action() {

    override fun reduce(oldState: ManagerState): ManagerState {
        return oldState
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable {
        val list = oldState.getCurrentStack()

        if (list == null || list.isEmpty() || list.size == 1) {
            if(!realWorld.activity.onBackPressed(list?.size ?: 0))
                realWorld.activity.stackManager.dispatch { FinishAction() }
            return Completable.complete()
        }

        val (clazz, fromIntent, animData, stackTag, hashTag) = list.last()
        val topFragment = realWorld.fragmentCollection[hashTag]
        if (topFragment?.fragment?.onBackPressed() ?: false || realWorld.activity.onBackPressed(list.size))
            return Completable.complete()

        realWorld.activity.stackManager.dispatch { DeleteAction(stackTag, hashTag) }

        return Completable.complete()
    }
}