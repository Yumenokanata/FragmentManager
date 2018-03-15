package indi.yume.tools.fragmentmanager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import indi.yume.tools.fragmentmanager.anno.OnHideMode
import indi.yume.tools.fragmentmanager.anno.OnShowMode
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by yume on 18-3-12.
 */

typealias StringKey = String

typealias ActivityKey = StringKey
typealias StackKey = StringKey
typealias FragmentKey = StringKey

typealias RealWorld = ApplicationStore

private val Any.createKey get() = hashCode().toString()

fun Single<*>.defaultSubscribe() = subscribe({  }, { Log.e("FragmentManagerUtil", "Single has error", it) })

fun Observable<*>.defaultSubscribe() = subscribe({  }, { Log.e("FragmentManagerUtil", "Observable has error", it) })

fun Completable.defaultSubscribe() = subscribe({  }, { Log.e("FragmentManagerUtil", "Completable has error", it) })

fun Flowable<*>.defaultSubscribe() = subscribe({  }, { Log.e("FragmentManagerUtil", "Flowable has error", it) })


@SuppressLint("StaticFieldLeak")
object ApplicationStore {
    val stackManager = StackManager()

    val activityStore = ActivityStore()

    fun getActivityData(key: ActivityKey): ActivityItem? =
            activityStore.getData(key)

    fun getActivityData(activity: ManageableActivity): ActivityItem? =
            activityStore.getData(activity)


    fun getFragmentData(activity: ManageableActivity, fragment: ManageableFragment): FragmentItem? =
            getActivityData(activity)?.fragmentCollection?.getData(fragment)

    fun getFragmentData(fragment: ManageableFragment): FragmentItem? =
            (fragment as? Fragment)?.let { getFragmentData(it) }

    fun getFragmentData(fragment: Fragment): FragmentItem? =
            (fragment.activity as? ManageableActivity)?.let { activity ->
                (fragment as? ManageableFragment)?.let { fragment ->
                    getFragmentData(activity, fragment)
                }
            }
}

class ActivityStore : MapStore<ManageableActivity, ActivityLifecycleOwner, ActivityItem>(
        { key, activity, lifecycleOwner ->
            ActivityItem(
                    activity = activity,
                    fragmentId = activity.fragmentId,
                    hashKey = key,
                    controller = lifecycleOwner)
        }
) {
    override fun putData(key: String, data: ActivityItem) = putData(key, data.activity, data)
}

class FragmentStore : MapStore<ManageableFragment, FragmentLifecycleOwner, FragmentItem>(
        { key, fragment, lifecycleOwner -> FragmentItem(fragment, key, lifecycleOwner) }
) {
    override fun putData(key: String, data: FragmentItem) = putData(key, data.fragment, data)
}

private const val activityKeyBundleTag = "activity_key__bundle_key"

abstract class MapStore<M, P, Data>(val create: (String, M, P) -> Data,
                                    val storeMap: MutableMap<String, Data> = Hashtable<String, Data>())
    : Map<String, Data> by storeMap {

    val keyMap: MutableMap<M, String> = HashMap()

    fun unsafeGetData(key: String): Data = storeMap[key]!!

    fun getData(key: String): Data? = storeMap[key]

    fun unsafeGetData(keyItem: M): Data = getData(keyItem)!!

    fun getData(keyItem: M): Data? = keyMap[keyItem]?.let { storeMap[it] }

    abstract fun putData(key: String, data: Data)

    fun putData(key: String, keyItem: M, data: Data) {
        keyMap[keyItem] = key
        storeMap[key] = data
    }

    fun createData(key: String, keyItem: M, param: P): Data =
            create(key, keyItem, param).also {
                keyMap[keyItem] = key
                storeMap[key] = it
            }

    fun getOrCreateData(key: String, keyItem: M, param: P): Data =
            getData(keyItem) ?: createData(key, keyItem, param)

    internal fun onCreate(keyItem: M, param: P, originKey: String?): Data =
            getOrCreateData(originKey ?: keyItem!!.createKey, keyItem, param)

    fun onCreate(keyItem: M, param: P, savedInstanceState: Bundle?): Data =
            onCreate(keyItem, param, savedInstanceState?.getString(activityKeyBundleTag))

    fun onSaveInstanceState(keyItem: M, outState: Bundle?) {
        val key = getKey(keyItem)
        if(key != null)
            outState?.putString(activityKeyBundleTag, key)
    }

    internal fun replaceData(key: String, newKeyItem: M) {
        val oldKeyItem = keyMap.entries.firstOrNull { it.value == key }?.key
        if(oldKeyItem != null)
            keyMap -= oldKeyItem
        keyMap[newKeyItem] = key
    }

    fun destoryData(keyItem: M) {
        val key = keyMap[keyItem]
        if(key != null) {
            storeMap -= key
            keyMap -= keyItem
        }
    }

    fun destoryData(key: String) {
        val keyItem = keyMap.entries.firstOrNull { it.value == key }?.key
        if(keyItem != null) {
            storeMap -= key
            keyMap -= keyItem
        }
    }

    fun getKey(keyItem: M): String? = keyMap[keyItem]
}


data class ActivityItem(
        val activity: ManageableActivity,
        val fragmentId: Int,
        val hashKey: ActivityKey,
        val fragmentCollection: FragmentStore = FragmentStore(),
        val controller: ActivityLifecycleOwner
)

data class FragmentItem (
        val fragment: ManageableFragment,
        val hashKey: FragmentKey,
        val controller: FragmentLifecycleOwner
) {

    fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        fragment.onFragmentResult(requestCode, resultCode, data)
    }

    fun preBackResultData() {
        fragment.preBackResultData()
    }

    fun onHide(mode: OnHideMode) {
        fragment.onHide(mode)
    }

    fun onShow(mode: OnShowMode) {
        fragment.onShow(mode)
    }
}