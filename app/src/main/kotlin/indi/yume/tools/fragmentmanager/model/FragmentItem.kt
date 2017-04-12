package indi.yume.tools.fragmentmanager.model

import android.os.Bundle
import indi.yume.tools.fragmentmanager.BaseManagerFragment
import indi.yume.tools.fragmentmanager.FragmentController
import indi.yume.tools.fragmentmanager.anno.OnHideMode
import indi.yume.tools.fragmentmanager.anno.OnShowMode

/**
 * Created by yume on 17-4-11.
 */

data class FragmentItem (
        val fragment: BaseManagerFragment,
        val stackTag: String,
        val hashTag: String,
        val controller: FragmentController
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