package com.quadlogixs.debugtool.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quadlogixs.debugtool.api.DebugEnvironment
import com.quadlogixs.debugtool.api.DebugToolRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnvironmentViewModel @Inject constructor() : ViewModel() {

    private val _environments = MutableStateFlow<List<DebugEnvironment>>(emptyList())
    val environments: StateFlow<List<DebugEnvironment>> = _environments.asStateFlow()

    private val _selectedEnvironmentUrl = MutableStateFlow("")
    val selectedEnvironmentUrl: StateFlow<String> = _selectedEnvironmentUrl.asStateFlow()

    fun loadEnvironmentScreen() {
        viewModelScope.launch {
            _environments.value = DebugToolRegistry.host.environments()
            val saved = DebugToolRegistry.host.currentEnvironment()
            _selectedEnvironmentUrl.value = resolveSelectedEnvironmentUrl(saved, _environments.value)
        }
    }

    fun getSelectedEnvironment(selectedEnvironment: (String) -> Unit) {
        viewModelScope.launch {
            selectedEnvironment(DebugToolRegistry.host.currentEnvironment())
        }
    }

    fun getEnvironments() {
        _environments.value = DebugToolRegistry.host.environments()
    }

    fun addCustomEnvironment(title: String, url: String) {
        viewModelScope.launch {
            val newEnv = DebugEnvironment(title = title, url = url)
            _environments.value = _environments.value + newEnv
            DebugToolRegistry.host.applyEnvironment(url)
            _selectedEnvironmentUrl.value = url
        }
    }

    fun changeEnvironment(env: String) {
        viewModelScope.launch {
            DebugToolRegistry.host.applyEnvironment(env)
            _selectedEnvironmentUrl.value = env
        }
    }

    private fun resolveSelectedEnvironmentUrl(
        saved: String,
        environmentList: List<DebugEnvironment>,
    ): String {
        if (environmentList.isEmpty()) return saved
        if (saved.isBlank() || saved.equals("default", ignoreCase = true)) {
            return environmentList.first().url
        }
        val normalizedSaved = saved.trimEnd('/')
        environmentList.firstOrNull { it.url.trimEnd('/') == normalizedSaved }?.url?.let { return it }
        return saved
    }
}
