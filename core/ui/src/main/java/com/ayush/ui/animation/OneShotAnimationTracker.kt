package com.ayush.ui.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
class OneShotAnimationTracker {
    private val played = mutableSetOf<Any>()

    fun hasPlayed(key: Any): Boolean = played.contains(key)

    fun markPlayed(key: Any) {
        played.add(key)
    }
}

@Composable
fun rememberOneShotAnimationTracker(): OneShotAnimationTracker =
    remember { OneShotAnimationTracker() }

@Composable
fun rememberOneShotFlag(
    tracker: OneShotAnimationTracker,
    key: Any,
): Boolean {
    val shouldAnimate = !tracker.hasPlayed(key)
    LaunchedEffect(key) { tracker.markPlayed(key) }
    return shouldAnimate
}
