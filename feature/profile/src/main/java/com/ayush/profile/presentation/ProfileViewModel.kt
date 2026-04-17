package com.ayush.profile.presentation

import androidx.lifecycle.viewModelScope
import com.ayush.datastore.domain.usecase.GetThemeModeUseCase
import com.ayush.datastore.domain.usecase.SetThemeModeUseCase
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getThemeModeUseCase: GetThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase
) : BaseMviViewModel<ProfileEvent, ProfileState, ProfileSideEffect>(
    initialState = ProfileState()
) {

    init {
        viewModelScope.launch {
            getThemeModeUseCase().collect { mode ->
                setState { copy(themeMode = mode) }
            }
        }
    }

    override fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.ThemeModeChanged -> {
                viewModelScope.launch { setThemeModeUseCase(event.mode) }
            }
        }
    }
}
