package com.mianu.paymentapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mianu.paymentapp.data.ApiResult
import com.mianu.paymentapp.data.MianuRepository
import com.mianu.paymentapp.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean
        get() = username.isNotBlank() && password.isNotBlank() && !isSubmitting
}

class AuthViewModel(
    private val repo: MianuRepository = MianuRepository.instance,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onUsernameChange(value: String) = _state.update { it.copy(username = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    /**
     * Authenticates with conference credentials.
     */
    fun submit(onSuccess: () -> Unit) {
        val current = _state.value
        if (!current.canSubmit) return

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }

            val result = repo.login(current.username.trim(), current.password)

            when (result) {
                is ApiResult.Success -> {
                    SessionManager.signIn(result.data)
                    _state.update { it.copy(isSubmitting = false) }
                    onSuccess()
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(isSubmitting = false, error = result.error.message)
                }
            }
        }
    }

    fun signOut() {
        SessionManager.signOut()
        _state.value = AuthUiState()
    }
}
