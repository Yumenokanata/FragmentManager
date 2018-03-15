package indi.yume.tools.fragmentmanager

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import indi.yume.tools.fragmentmanager.anno.OnHideMode
import indi.yume.tools.fragmentmanager.anno.OnShowMode
import io.reactivex.subjects.Subject
import org.slf4j.LoggerFactory

/**
 * Created by yume on 17-4-12.
 */

abstract class DefaultManagerFragment : Fragment(), ManageableFragment, FragmentLifecycleOwner {
    private val logger = LoggerFactory.getLogger(javaClass)

    override val lifeSubject: Subject<FragmentLifeEvent> = FragmentLifecycleOwner.defaultLifeSubject()

    override val fragment: Fragment = this

    override val fragmentLifeCycle: FragmentLifecycleOwner = this

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.debug("Fragment onCreate: ${this}")
        super<Fragment>.onCreate(savedInstanceState)
        super<ManageableFragment>.onCreate(savedInstanceState)
        makeState(FragmentLifeEvent.OnCreate(this, savedInstanceState))
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        makeState(FragmentLifeEvent.OnCreateView(this, inflater, container, savedInstanceState))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logger.debug("Fragment onViewCreated: ${this}")
        super.onViewCreated(view, savedInstanceState)
        makeState(FragmentLifeEvent.OnViewCreated(this, view, savedInstanceState))
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        makeState(FragmentLifeEvent.OnStart(this))
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        makeState(FragmentLifeEvent.OnResume(this))
    }

    @CallSuper
    override fun onShow(mode: OnShowMode) {
        super.onShow(mode)
        makeState(FragmentLifeEvent.OnShow(this, mode))
    }

    @CallSuper
    override fun onHide(mode: OnHideMode) {
        super.onHide(mode)
        makeState(FragmentLifeEvent.OnHide(this, mode))
    }

    @CallSuper
    override fun onDestroy() {
        logger.debug("Fragment onDestroy: ${this}")
        makeState(FragmentLifeEvent.OnDestroy(this))
        destroyLifecycle()
        super<ManageableFragment>.onDestroy()
        super<Fragment>.onDestroy()
    }

    @CallSuper
    override fun onPause() {
        logger.debug("Fragment onPause: ${this}")
        makeState(FragmentLifeEvent.OnPause(this))
        super.onPause()
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super<Fragment>.onSaveInstanceState(outState)
        super<ManageableFragment>.onSaveInstanceState(outState)
        fragmentItem?.apply { RestoreManager.onSaveInstanceState(hashKey, outState) }
    }
}