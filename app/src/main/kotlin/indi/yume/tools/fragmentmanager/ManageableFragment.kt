package indi.yume.tools.fragmentmanager

import android.os.Bundle
import android.support.v4.app.Fragment
import indi.yume.tools.fragmentmanager.anno.OnHideMode
import indi.yume.tools.fragmentmanager.anno.OnShowMode
import indi.yume.tools.fragmentmanager.event.EmptyAction
import indi.yume.tools.fragmentmanager.event.ModifyAction
import indi.yume.tools.fragmentmanager.functions.playNull
import indi.yume.tools.fragmentmanager.model.ItemState
import indi.yume.tools.fragmentmanager.model.ManagerState
import io.reactivex.Single
import io.reactivex.functions.Consumer

/**
 * Created by yume on 18-3-12.
 */
interface ManageableFragment {

    val activityItem: ActivityItem?
        get() = (fragment.activity as? ManageableActivity)?.let { ApplicationStore.getActivityData(it) }

    val fragmentItem: FragmentItem?
        get() = ApplicationStore.getFragmentData(this)

    val fragment: Fragment

    val stackKey: StackKey
        get() = playNull {
            val activityKey = activityItem.bind().hashKey
            val fragmentKey = fragmentItem.bind().hashKey
            ApplicationStore.stackManager.bind().blockingFirst()
                    .newState.getState(activityKey)?.getStack(fragmentKey)
        }!!

    val stackState: StateData
        get() = ApplicationStore.stackManager.bind().blockingFirst()

    //<editor-fold desc="Override Fragment lifecycle">
    fun onCreate(savedInstanceState: Bundle?) {
        fragmentItem?.apply { RestoreManager.onCreate(savedInstanceState, hashKey) }
    }

    fun onDestroy() {
        fragmentItem?.apply { RestoreManager.onDestroy(hashKey) }
    }

    fun onSaveInstanceState(outState: Bundle) {
        fragmentItem?.apply { RestoreManager.onSaveInstanceState(hashKey, outState) }
    }
    //</editor-fold>


    //<editor-fold desc="Extra life callback">
    fun preBackResultData() {}

    fun onBackPressed(): Boolean = false

    fun onHide(mode: OnHideMode) {}

    fun onShow(mode: OnShowMode) {}

    fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        fragmentItem?.hashKey?.also { hashTag ->
            RestoreManager.onResult(hashTag, requestCode, resultCode, data)
        }
    }
    //</editor-fold>


    //<editor-fold desc="Addition Function">
    fun start(builder: StartBuilder) = activityItem?.start(builder)?.defaultSubscribe()

    fun startForObservable(rxStartBuilder: RxStartBuilder): Single<Pair<Int, Bundle>> = playNull {
        val fragmentVal = fragmentItem.bind()
        val activityVal = activityItem.bind()

        RestoreManager.startFragmentForRx(fragmentVal.hashKey, activityVal, rxStartBuilder)
    } ?: Single.error<Pair<Int, Bundle>>(RuntimeException("Fragment not found at FragmentManager"))


    fun finish() {
        preBackResultData()
        playNull {
            val valFragmentItem = fragmentItem.bind()
            val valActivityItem = activityItem.bind()

            valActivityItem.deleteItem(stackKey, valFragmentItem.hashKey).defaultSubscribe()
        }
    }

    fun setResult(resultCode: Int, resultData: Bundle) {
        playNull {
            val activityKey = activityItem.bind().hashKey
            val fragmentKey = fragmentItem.bind().hashKey

            ApplicationStore.stackManager.dispatch { state ->
                val itemState = state.itemState(activityKey, fragmentKey)
                        ?: return@dispatch EmptyAction()
                ModifyAction(
                        targetTag = itemState.stackTag,
                        itemTag = itemState.hashTag,
                        animData = itemState.animData,
                        resultCode = resultCode,
                        resultData = resultData)
            }.defaultSubscribe()
        }
    }

    fun restore(state: ManagerState) = activityItem?.restore(state)?.defaultSubscribe()

    fun getManageableActivity(): ManageableActivity = fragment.activity as ManageableActivity
    //</editor-fold>

    companion object {
        fun ManagerState.itemState(key: ActivityKey, fragmentKey: FragmentKey): ItemState? =
            getState(key)?.run {
                stackMap.values.flatMap { it.filter { it.hashTag == fragmentKey } }.firstOrNull()
            }
    }
}