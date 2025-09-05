package com.belltree.pomodoroshareapp.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.repository.SpaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val spaceRepository: SpaceRepository
) : ViewModel() {
    private val _spaces = MutableStateFlow<List<Space>>(emptyList())
    val spaces: StateFlow<List<Space>> = _spaces

    // 個別取得用の選択中スペース
    private val _selectedSpace = MutableStateFlow<Space?>(null)
    val selectedSpace: StateFlow<Space?> = _selectedSpace

    suspend fun getUnfinishedSpaces(): List<Space> {
        _spaces.value = spaceRepository.getUnfinishedSpaces()
        return _spaces.value
    }

    suspend fun getSpaceById(spaceId: String): Space? {
        val space = spaceRepository.getSpaceById(spaceId)
        _selectedSpace.value = space
        return space
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _spaces.value = spaceRepository.getUnfinishedSpaces()
            _isLoading.value = false
        }
    }

    fun refresh() = load()


    fun createSpace(space: Space) {
        viewModelScope.launch {
            spaceRepository.createSpace(space)
        }
    }
}

