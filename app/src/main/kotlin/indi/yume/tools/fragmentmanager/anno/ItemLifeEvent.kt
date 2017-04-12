package indi.yume.tools.fragmentmanager.anno

/**
 * Created by yume on 16-8-31.

 * ON_PAUSE: 1. when start new activity. 2. program switch to background.(just this activity is top)
 * ON_START_NEW: when start new fragment, always on call hide() and next fragment is created.
 * ON_START_NEW_AFTER_ANIM: when start new fragment and next fragment enter anim is over(if has anim).
 * ON_SWITCH: when switch to other tag.
 */

sealed class OnHideMode {
    object OnPause : OnHideMode()
    object OnStartNew : OnHideMode()
    object OnSwitch : OnHideMode()
    object OnStartNewAfterAnim : OnHideMode()

    companion object {
        @JvmStatic fun toString(hideMode: OnHideMode): String {
            when (hideMode) {
                is OnPause -> return "onPause"
                is OnStartNew -> return "onStartNew"
                is OnStartNewAfterAnim -> return "onStartNewAfterAnim"
                is OnSwitch -> return "onSwitch"
                else -> return "None"
            }
        }
    }
}

sealed class OnShowMode {
    object OnResume : OnShowMode()
    object OnBack : OnShowMode()
    object OnSwitch : OnShowMode()
    object OnCreate : OnShowMode()
    object OnCreateAfterAnim : OnShowMode()

    companion object {
        @JvmStatic fun toString(showMode: OnShowMode): String {
            when (showMode) {
                is OnResume -> return "onResume"
                is OnBack -> return "onBack"
                is OnSwitch -> return "onSwitch"
                is OnCreate -> return "onCreate"
                is OnCreateAfterAnim -> return "onCreateAfterAnim"
                else -> return "None"
            }
        }
    }
}