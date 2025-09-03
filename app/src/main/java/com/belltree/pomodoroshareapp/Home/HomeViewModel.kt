package com.belltree.pomodoroshareapp.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.repository.SpaceRepositoryImpl
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(
    private val spaceRepository: SpaceRepositoryImpl
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

class HomeViewModelFactory(private val spaceRepository: SpaceRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(spaceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

