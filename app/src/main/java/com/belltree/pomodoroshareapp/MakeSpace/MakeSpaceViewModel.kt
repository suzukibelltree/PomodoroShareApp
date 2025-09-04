package com.belltree.pomodoroshareapp.MakeSpace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.repository.SpaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MakeSpaceViewModel @Inject constructor(
    private val spaceRepository: SpaceRepository
) : ViewModel() {
    fun createSpace(space: Space) {
        viewModelScope.launch {
            spaceRepository.createSpace(space)
        }
    }
}