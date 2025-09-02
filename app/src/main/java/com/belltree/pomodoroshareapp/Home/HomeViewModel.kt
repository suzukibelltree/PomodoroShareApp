package com.belltree.pomodoroshareapp.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.SpaceCreateRequest
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

    fun getSpaces(): List<Space> {
        return spaceRepository.spaces.value
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            spaceRepository.getAll().onSuccess { list ->
                _spaces.value = list
            }.onFailure {
                // TODO: error handling (log/report)
            }
            _isLoading.value = false
        }
    }

    fun refresh() = load()

    fun getAllSpaces() {
        viewModelScope.launch {
            val result = spaceRepository.getAll()
            result.onSuccess { response ->
                println("投稿一覧取得成功: $response")

            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createSpace(space: SpaceCreateRequest) {
        viewModelScope.launch {
            val result = spaceRepository.create(space)
            result.onSuccess { response ->
                println("投稿成功: $response")
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }
}


class SpaceViewModelFactory(private val spaceRepository: SpaceRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpaceViewModel(spaceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

