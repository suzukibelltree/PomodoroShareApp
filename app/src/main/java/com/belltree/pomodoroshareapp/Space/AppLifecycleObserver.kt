package com.belltree.pomodoroshareapp.Space

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.belltree.pomodoroshareapp.domain.models.SpaceState

class AppLifecycleObserver(private val spaceViewModel: SpaceViewModel) : DefaultLifecycleObserver {

    var isSpaceScreenActive = false
    val isSpaceStateWORKING = (spaceViewModel.spaceState.value == SpaceState.WORKING)

    fun updateSpaceScreenActive(isActive: Boolean) {
        isSpaceScreenActive = isActive
    }

    override fun onStop(owner: LifecycleOwner) {
        // Space画面がアクティブで、かつSpaceStateがWORKINGの場合にのみバックグラウンド処理を実行する
        if (isSpaceScreenActive && isSpaceStateWORKING) {
            spaceViewModel.onScreenBackgrounded()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (isSpaceScreenActive) {
            spaceViewModel.onScreenForegrounded()
        }
    }
}
