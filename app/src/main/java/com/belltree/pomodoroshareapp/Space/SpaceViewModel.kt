package com.belltree.pomodoroshareapp.Space

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.Record
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.repository.RecordRepositoryImpl
import com.belltree.pomodoroshareapp.domain.repository.SpaceRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
class SpaceViewModel(
    private val recordRepository: RecordRepositoryImpl,
    private val spaceRepository: SpaceRepositoryImpl
) : ViewModel() {
    private val _records = MutableStateFlow<List<Record>>(emptyList())
    val records: StateFlow<List<Record>> = _records
    private val _spaces = MutableStateFlow<List<Space>>(emptyList())
    val spaces: StateFlow<List<Space>> = _spaces

    private val _space = MutableStateFlow<Space?>(null)
    val space: StateFlow<Space?> = _space

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    fun getUnfinishedSpaces() {
        viewModelScope.launch {
            _spaces.value = spaceRepository.getUnfinishedSpaces()
        }
    }

    fun getSpaceById(spaceId: String) {
        viewModelScope.launch {
            _space.value = spaceRepository.getSpaceById(spaceId)
        }
    }

    fun createSpace(space: Space){
        viewModelScope.launch {
            spaceRepository.createSpace(space)
        }
    }

    fun addMyUserInfoToSpace(spaceId: String, userId: String){
        viewModelScope.launch {
            spaceRepository.addMyUserInfoToSpace(spaceId, userId)
        }
    }

    fun observeSpace(spaceId: String){
        // 既存の監視をキャンセルしてから新しい Flow を収集
        viewModelScope.launch {
            spaceRepository.observeSpace(spaceId)
                .collect { latest ->
                    _space.value = latest
                }
        }
    }
}


class SpaceViewModelFactory(private val spaceRepository: SpaceRepositoryImpl, private val recordRepository: RecordRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpaceViewModel(recordRepository,spaceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}