package indi.yume.tools.fragmentmanager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import indi.yume.tools.fragmentmanager.anno.OnHideMode
import indi.yume.tools.fragmentmanager.anno.OnShowMode
import indi.yume.tools.fragmentmanager.event.ModifyAction
import indi.yume.tools.fragmentmanager.model.FragmentItem
import indi.yume.tools.fragmentmanager.model.ItemState
import indi.yume.tools.fragmentmanager.model.ManagerState
import io.reactivex.Single
import io.reactivex.functions.Consumer
import org.slf4j.LoggerFactory

/**
 * Created by yume on 17-4-12.
 */

abstract class BaseManagerFragment : Fragment() {
    private val logger = LoggerFactory.getLogger(javaClass)

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
        logger.debug("Fragment onCreate: ${this}")
        super.onCreate(savedInstanceState)
        controller.onCreate()
        RestoreManager.onCreate(savedInstanceState, fragmentItem.hashTag)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        logger.debug("Fragment onViewCreated: ${this}")
        super.onViewCreated(view, savedInstanceState)
        controller.onViewCreated(view)
    }

    override fun onPause() {
        logger.debug("Fragment onPause: ${this}")
        controller.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        logger.debug("Fragment onDestroy: ${this}")
        controller.onDestroy()
        RestoreManager.onDestroy(fragmentItem.hashTag)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        RestoreManager.onSaveInstanceState(fragmentItem.hashTag, outState)
    }

    open fun preBackResultData() {}

    open fun onBackPressed(): Boolean = false

    open fun onHide(mode: OnHideMode) {}

    open fun onShow(mode: OnShowMode) {}

    open fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        RestoreManager.onResult(fragmentItem.hashTag, requestCode, resultCode, data)
    }

    fun start(builder: StartBuilder) = stackManager.start(builder)

    fun startForObservable(rxStartBuilder: RxStartBuilder): Single<Pair<Int, Bundle>> =
            RestoreManager.startFragmentForRx(fragmentItem.hashTag, stackManager, rxStartBuilder)

    fun ManagerState.itemState(item: FragmentItem): ItemState = getItem(item.stackTag, item.hashTag)!!

    fun finish() {
        preBackResultData()
        stackManager.deleteItem(fragmentItem.stackTag, fragmentItem.hashTag)
    }

    fun setResult(resultCode: Int, resultData: Bundle) {
        stackManager.dispatch { state ->
            ModifyAction(state.itemState(fragmentItem))
                    .copy(resultCode = resultCode, resultData = resultData)
        }
    }

    fun restore(state: ManagerState) = stackManager.restore(state)

    fun getState(func: Consumer<ManagerState>) = stackManager.getState(func)
}