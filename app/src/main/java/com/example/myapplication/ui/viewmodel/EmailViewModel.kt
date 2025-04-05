// Create file: app/src/main/java/com/example/myapplication/ui/viewmodel/EmailViewModel.kt
package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.entity.EmailEntity
import com.example.myapplication.data.repository.EmailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class EmailViewModel(private val repository: EmailRepository) : ViewModel() {
    private val _emails = MutableStateFlow<List<EmailEntity>>(emptyList())
    val emails: StateFlow<List<EmailEntity>> = _emails

    private val _selectedEmail = MutableStateFlow<EmailEntity?>(null)
    val selectedEmail: StateFlow<EmailEntity?> = _selectedEmail

    init {
        loadEmails()
    }

    fun loadEmails(pageSize: Int = 20, pageOffset: Int = 0) {
        viewModelScope.launch {
            repository.getEmailsPaged(pageSize, pageOffset).collect { emailList ->
                _emails.value = emailList
            }
        }
    }

    fun selectEmail(email: EmailEntity) {
        _selectedEmail.value = email
    }

    class Factory(private val repository: EmailRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EmailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EmailViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}