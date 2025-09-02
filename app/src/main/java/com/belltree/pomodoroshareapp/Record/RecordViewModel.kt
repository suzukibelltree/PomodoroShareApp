package com.belltree.pomodoroshareapp.Record

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.Record
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecordViewModel(
    private val recordRepository: RecordRepositoryImpl
) : ViewModel() {
    private val _records = MutableStateFlow<List<Record>>(emptyList())
    val records: StateFlow<List<Record>> = _records

    fun getRecords(): List<Record> {
        return recordRepository.records.value
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            recordRepository.getAll().onSuccess { list ->
                _records.value = list
            }.onFailure {
                // TODO: error handling (log/report)
            }
            _isLoading.value = false
        }
    }

    fun refresh() = load()

    fun getAllRecords() {
        viewModelScope.launch {
            val result = recordRepository.getAll()
            result.onSuccess { response ->
                println("投稿一覧取得成功: $response")

            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createRecord(record: RecordCreateRequest) {
        viewModelScope.launch {
            val result = recordRepository.create(record)
            result.onSuccess { response ->
                println("投稿成功: $response")
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }
}


class RecordViewModelFactory(private val recordRepository: RecordRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordViewModel(recordRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}