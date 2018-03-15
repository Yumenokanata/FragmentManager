package indi.yume.tools.fragmentmanager.event

import android.content.Intent
import android.os.Bundle
import indi.yume.tools.fragmentmanager.*
import indi.yume.tools.fragmentmanager.exception.DoEffectException
import indi.yume.tools.fragmentmanager.functions.ActionTrunk
import indi.yume.tools.fragmentmanager.functions.playNull
import indi.yume.tools.fragmentmanager.model.*
import io.reactivex.Completable
import io.reactivex.Single
import java.lang.UnsupportedOperationException
import java.util.*

/**
 * Created by yume on 17-4-10.
 */

abstract class Action {
    abstract fun reduce(oldState: ManagerState): ManagerState

    abstract fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable
}

//<editor-fold desc="Fragment Actions">

abstract class TargetAction(val activityKey: (ManagerState) -> ActivityKey?,
                            val commitFun: CommitFun) : Action() {
    override fun reduce(oldState: ManagerState): ManagerState =
            oldState.doForTargetItem(activityKey(oldState)) { reduce(it) }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable =
            Completable.defer {
                playNull {
                    val topKey = oldState.currentActivity.bind()
                    val activityItem = realWorld.getActivityData(topKey).bind()
                    val oldTopState = oldState.getState(topKey).bind()
                    val newTopState = newState.getState(topKey).bind()

                    effect(realWorld, activityItem, oldTopState, newTopState)
                } ?: Completable.complete()
            }

    abstract fun reduce(oldState: ActivityStackState): ActivityStackState

    abstract fun effect(realWorld: RealWorld, targetActivity: ActivityItem, oldState: ActivityStackState, newState: ActivityStackState): Completable

    companion object {
        val defaultActivityKey: (ManagerState) -> ActivityKey? = { it.currentActivity }

        val defaultCommit: CommitFun = commit
    }
}

class AddAction(val targetStack: StackKey, val foreItem: ItemState,
                activityKey: (ManagerState) -> ActivityKey? = defaultActivityKey,
                commitFun: CommitFun = defaultCommit) : TargetAction(activityKey, commitFun) {

    override fun reduce(oldState: ActivityStackState): ActivityStackState {
        val index = if(foreItem.backItemHashTag != null)
            oldState.getIndex(targetStack, foreItem.backItemHashTag).first
        else -1

        return if(index >= 0)
            oldState.plusAt(targetStack, index + 1, foreItem)
        else
            oldState.plus(targetStack, foreItem)
    }

    override fun effect(realWorld: RealWorld, targetActivity: ActivityItem,
                        oldState: ActivityStackState, newState: ActivityStackState): Completable {
        val currentStack = newState.currentStack
        val backItem = newState.getItem(targetStack, foreItem.backItemHashTag)

        if (newState.getTop(targetStack) === foreItem) {
            if (backItem != null && currentStack == targetStack)
                return commitFun(targetActivity.activity.provideFragmentManager) { transaction ->
                    val transPair = targetActivity to transaction

                    val foreFrag = getItem(foreItem)(realWorld).second(transPair).second
                    val backFrag = getItem(backItem)(realWorld).second(transPair).second

                    showFragmentWithAnim(foreFrag to foreItem, backFrag to backItem)(realWorld)
                            .second(transPair)
                            .second
                }

            //Has back item but target tag is not current tag.
            if (backItem != null)
                return commitFun(targetActivity.activity.provideFragmentManager) { transaction ->
                    val transPair = targetActivity to transaction

                    val foreFrag = getItem(foreItem)(realWorld).second(transPair).second
                    val backFrag = getItem(backItem)(realWorld).second(transPair).second

                    val completable = showFragmentNoAnim(foreFrag, backFrag)(realWorld)
                            .second(transPair)
                            .second
                    if(currentStack != targetStack)
                        transaction.hide(foreFrag.fragment.fragment)

                    completable
                }

            //No back item or target tag is not current tag.
            return commitFun(targetActivity.activity.provideFragmentManager) { transaction ->
                val foreFrag = getItem(foreItem)(realWorld).second(targetActivity to transaction).second

                if(currentStack != targetStack)
                    transaction.hide(foreFrag.fragment.fragment)

                showCallback(foreFrag)
            }
        } else {
            //Add item must at stack top, else do nothing
            return Completable.error(DoEffectException("Add item must at stack top"))
        }
    }
}

class DeleteAction(val targetTag: String,
                   val targetHashTag: String,
                   activityKey: (ManagerState) -> ActivityKey? = defaultActivityKey,
                   commitFun: CommitFun = defaultCommit) : TargetAction(activityKey, commitFun) {

    override fun reduce(oldState: ActivityStackState): ActivityStackState {
        return oldState.minus(targetTag, targetHashTag).first
    }

    override fun effect(realWorld: RealWorld, targetActivity: ActivityItem, oldState: ActivityStackState, newState: ActivityStackState): Completable {
        if(oldState == newState)
            return Completable.complete()

        val currentTag = newState.currentStack
        val foreItem = oldState.getItem(targetTag, targetHashTag) ?: return Completable.complete()

        val backItem = newState.getItem(targetTag, foreItem.backItemHashTag) ?: newState.getTop(targetTag)

        if (foreItem.isBackItem(backItem) && currentTag == targetTag) {
            return commitFun(targetActivity.activity.provideFragmentManager) { transaction ->
                val transPair = targetActivity to transaction

                val foreFrag = getItem(foreItem)(realWorld).second(transPair).second
                val backFrag = if(backItem != null) getItem(backItem)(realWorld).second(transPair).second else null
                val backData = if(backFrag != null && backItem != null) backFrag to backItem else null

                removeFragmentWithAnim(foreFrag to foreItem, backData)(realWorld)
                        .second(transPair)
                        .second
            }
        } else {
            return commitFun(targetActivity.activity.provideFragmentManager) { transaction ->
                val transPair = targetActivity to transaction

                val foreFrag = getItem(foreItem)(realWorld).second(transPair).second
                val backFrag = if(backItem != null) getItem(backItem)(realWorld).second(transPair).second else null
                val backData = if(backFrag != null && backItem != null) backFrag to backItem else null

                removeFragmentNoAnim(foreFrag to foreItem, backData)(realWorld)
                        .second(transPair)
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

class ModifyAction(
        val targetTag: String,
        val itemTag: String,
        val animData: AnimData?,
        val resultCode: Int,
        val resultData: Bundle?,

        activityKey: (ManagerState) -> ActivityKey? = defaultActivityKey,
        commitFun: CommitFun = defaultCommit) : TargetAction(activityKey, commitFun) {

    constructor(state: ItemState): this(
            targetTag = state.stackTag,
            itemTag = state.hashTag,
            animData = state.animData,
            resultCode = state.resultCode,
            resultData = state.resultData
    )

    override fun reduce(oldState: ActivityStackState): ActivityStackState =
        oldState.modifyItem(targetTag, itemTag) { oldItem ->
            oldItem.copy(animData = animData,
                    resultCode = resultCode,
                    resultData = resultData)
        }

    override fun effect(realWorld: RealWorld, targetActivity: ActivityItem, oldState: ActivityStackState, newState: ActivityStackState): Completable =
            Completable.complete()
}

class SwitchAction(
        val backTag: String? = null,
        val targetStack: StackKey?,
        val defaultItem: ItemState? = null,

        activityKey: (ManagerState) -> ActivityKey? = defaultActivityKey,
        commitFun: CommitFun = defaultCommit) : TargetAction(activityKey, commitFun) {

    override fun reduce(oldState: ActivityStackState): ActivityStackState {
        val newState = oldState.copy(currentStack = targetStack)
        if (targetStack != null && defaultItem != null && newState.isCurrentStackEmpty())
            return newState.plus(targetStack, defaultItem)
        return newState
    }

    override fun effect(realWorld: RealWorld, targetActivity: ActivityItem, oldState: ActivityStackState, newState: ActivityStackState): Completable {
        val backItem = backTag?.let { newState.getTop(it) }
        val foreItem = targetStack?.let { newState.getTop(it) }

        if (backItem != null || foreItem != null)
            return commitFun(targetActivity.activity.provideFragmentManager) { transaction ->
                val transPair = targetActivity to transaction

                val foreFrag = foreItem?.let { getItem(it)(realWorld).second(transPair).second }
                val backFrag = backItem?.let { getItem(it)(realWorld).second(transPair).second }

                switchTag(foreFrag, backFrag)(realWorld)
                        .second(transPair)
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
                oldState.currentActivity?.let { realWorld.getActivityData(it) }
                        ?.activity?.activity?.finish()
                e.onComplete()
            }
}

class BackAction : Action() {

    override fun reduce(oldState: ManagerState): ManagerState {
        return oldState
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable {
        return playNull {
            val currentActivityKey = oldState.currentActivity.bind()
            val currentActivity = oldState.getState(currentActivityKey).bind()
            val currentActivityItem = realWorld.getActivityData(currentActivityKey).bind()

            val list = currentActivity.getCurrentStack()

            if (list == null || list.isEmpty() || list.size == 1) {
                return@playNull if(!currentActivityItem.activity.onBackPressed(list?.size ?: 0))
                    ApplicationStore.stackManager.dispatch { FinishAction() }.toCompletable()
                else
                    null
            }

            val (clazz, fromIntent, animData, stackTag, hashTag) = list.last()
            val topFragment = currentActivityItem.fragmentCollection[hashTag].bind()
            if (topFragment.fragment.onBackPressed() || currentActivityItem.activity.onBackPressed(list.size))
                return@playNull null

            return@playNull ApplicationStore.stackManager.dispatch { DeleteAction(stackTag, hashTag) }
                    .toCompletable()
        } ?: Completable.complete()
    }
}
//</editor-fold>

//<editor-fold desc="Activity Lifecycle Actions">
internal class OnCreateAction private constructor(val activityKey: ActivityKey) : Action() {
    constructor(activity: ManageableActivity): this(ApplicationStore.activityStore.getKey(activity)
            ?: throw NullPointerException("Must call ApplicationStore.activityStore.onCreate() first"))

    override fun reduce(oldState: ManagerState): ManagerState {
        return oldState.onCreate(activityKey)
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable {
        return Completable.complete()
    }
}

internal class OnResumeAction(val activityKey: ActivityKey) : Action() {

    override fun reduce(oldState: ManagerState): ManagerState {
        return oldState
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable {
        return playNull {
            val targetActivity = oldState.getState(activityKey).bind()
            val targetActivityItem = realWorld.getActivityData(activityKey).bind()

            val list = targetActivity.getCurrentStack().bind()

            if (!list.isEmpty()) {
                val foreItem = list.last()
                commit(targetActivityItem.activity.provideFragmentManager) { transaction ->
                    val foreFrag = foreItem.let { getItem(it)(realWorld).second(targetActivityItem to transaction).second }

                    onResume()(realWorld).second(foreFrag)

                    Completable.complete()
                }
            } else null
        } ?: Completable.complete()
    }
}

internal class OnPauseAction(val activityKey: ActivityKey) : Action() {

    override fun reduce(oldState: ManagerState): ManagerState {
        return oldState
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable {
        return playNull {
            val targetActivity = oldState.getState(activityKey).bind()
            val targetActivityItem = realWorld.getActivityData(activityKey).bind()

            val list = targetActivity.getCurrentStack()

            if (list != null && !list.isEmpty()) {
                val foreItem = list.last()
                commit(targetActivityItem.activity.provideFragmentManager) { transaction ->
                    val foreFrag = foreItem.let { getItem(it)(realWorld).second(targetActivityItem to transaction).second }

                    onPause()(realWorld).second(foreFrag)

                    Completable.complete()
                }
            } else null
        } ?: Completable.complete()
    }
}

internal class OnDestroyAction(val activityKey: ActivityKey) : Action() {

    override fun reduce(oldState: ManagerState): ManagerState {
        return oldState.onDestroy(activityKey)
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable {
        return Completable.complete()
    }
}
//</editor-fold>

//<editor-fold desc="Base Actions">
class TransactionAction(val list: List<ActionTrunk>) : Action() {

    override fun reduce(oldState: ManagerState): ManagerState {
        throw UnsupportedOperationException("TransactionAction can not be real reduce")
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable {
        throw UnsupportedOperationException("TransactionAction can not be real effect")
    }
}

class GenAction(val initAction: ActionTrunk = { EmptyAction() },
                val generator: (ManagerState, Action) -> Action?) : Action() {

    override fun reduce(oldState: ManagerState): ManagerState {
        throw UnsupportedOperationException("TransactionAction can not be real reduce")
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable {
        throw UnsupportedOperationException("TransactionAction can not be real effect")
    }
}

class CallbackAction(val callback: (ManagerState) -> Unit) : Action() {

    override fun reduce(oldState: ManagerState): ManagerState {
        callback(oldState)
        return oldState
    }

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable {
        return Completable.complete()
    }
}

class LambdaAction(val reducer: (ManagerState) -> ManagerState = { it },
                   val effectFun: (realWorld: RealWorld, oldState: ManagerState, newState: ManagerState) -> Completable =
                           { _, _, _ -> Completable.complete() }) : Action() {

    override fun reduce(oldState: ManagerState): ManagerState = reducer(oldState)

    override fun effect(realWorld: RealWorld, oldState: ManagerState, newState: ManagerState): Completable =
            effectFun(realWorld, oldState, newState)
}
//</editor-fold>