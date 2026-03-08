package com.ayush.home.presentation

import android.content.Context
import androidx.compose.runtime.Stable
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : BaseMviViewModel<HomeUiEvent, HomeState, HomeSideEffect>(
    initialState = HomeState()
) {
    override fun onEvent(event: HomeUiEvent) {
        when (event) {
            else -> {}
        }
    }
}

@Stable
data class HomeState(
    val name: String = ""
)

sealed interface HomeUiEvent {

}

sealed interface HomeSideEffect {

}