package com.belltree.pomodoroshareapp.MakeSpace

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.Login.AuthViewModel
import com.belltree.pomodoroshareapp.Space.SpaceViewModel
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.repository.AuthRepositoryImpl
import com.belltree.pomodoroshareapp.domain.repository.SpaceRepositoryImpl
import com.belltree.pomodoroshareapp.domain.repository.UserRepositoryImpl
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MakeSpaceViewModel(
    private val spaceRepository: SpaceRepositoryImpl
) : ViewModel() {
    fun createSpace(space: Space) {
        viewModelScope.launch {
            spaceRepository.createSpace(space)
        }
    }
}

class MakeSpaceViewModelFactory(private val makeSpaceRepository: SpaceRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MakeSpaceViewModel(makeSpaceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}