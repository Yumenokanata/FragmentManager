package indi.yume.tools.fragmentmanager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import indi.yume.tools.fragmentmanager.anno.OnHideMode
import indi.yume.tools.fragmentmanager.anno.OnShowMode
import indi.yume.tools.fragmentmanager.event.AddAction
import indi.yume.tools.fragmentmanager.event.DeleteAction
import indi.yume.tools.fragmentmanager.event.ModifyAction
import indi.yume.tools.fragmentmanager.exception.DoEffectException
import indi.yume.tools.fragmentmanager.model.FragmentItem
import indi.yume.tools.fragmentmanager.model.ItemState
import indi.yume.tools.fragmentmanager.model.ManagerState

/**
 * Created by yume on 17-4-12.
 */

abstract class BaseManagerFragment : Fragment() {
    lateinit var stackManager: StackManager
    lateinit var controller: FragmentController
    lateinit var fragmentItem: FragmentItem

    fun init(manager: StackManager,
             controller: FragmentController,
             fragmentItem: FragmentItem) {
        this.stackManager = manager
        this.controller = controller
        this.fragmentItem = fragmentItem
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller.onCreate()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.onViewCreated(view)
    }

    override fun onPause() {
        controller.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        controller.onDestroy()
        super.onDestroy()
    }

    open fun preBackResultData() {}

    open fun onBackPressed(): Boolean = false

    open fun onHide(mode: OnHideMode) {}

    open fun onShow(mode: OnShowMode) {}

    open fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {}

    fun start(builder: StartBuilder) {
        stackManager.dispatch { state ->
            val currentTag = state.currentTag
            val backItem = state.getCurrentTop()
            val backItemHashTag = backItem?.hashTag

            if (currentTag == null)
                throw DoEffectException("start new item must at a stack")

            AddAction(currentTag, ItemState(currentTag, backItemHashTag, builder))
        }
    }

    fun ManagerState.itemState(item: FragmentItem): ItemState = getItem(item.stackTag, item.hashTag)!!

    fun finish() {
        preBackResultData()
        stackManager.dispatch { state ->
            val targetItem = state.itemState(fragmentItem)
            DeleteAction(targetItem.stackTag, targetItem.hashTag)
        }
    }

    fun setResult(resultCode: Int, resultData: Bundle) {
        stackManager.dispatch { state ->
            ModifyAction(state.itemState(fragmentItem))
                    .copy(resultCode = resultCode, resultData = resultData)
        }
    }
}