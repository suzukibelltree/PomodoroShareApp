package com.belltree.pomodoroshareapp.Record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.Comment
import com.belltree.pomodoroshareapp.domain.models.Record
import com.belltree.pomodoroshareapp.domain.repository.CommentRepository
import com.belltree.pomodoroshareapp.domain.repository.RecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {
    private val _records = MutableStateFlow<List<Record>>(emptyList())
    val records: StateFlow<List<Record>> = _records

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun updateRecord(recordId: String, updatedData: Record) {
        viewModelScope.launch {
            recordRepository.updateRecord(recordId, updatedData)
        }
    }

    fun getAllRecords(userId: String) {
        viewModelScope.launch {
            _records.value = recordRepository.getAllRecords(userId)
        }
    }

    fun addRecord(record: Record) {
        viewModelScope.launch {
            recordRepository.addRecord(record)
        }
    }
}