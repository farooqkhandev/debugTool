package com.quadlogixs.debugtool.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class SavedMockResponse(
    val id: String = UUID.randomUUID().toString(),
    val urlPattern: String,
    val httpMethod: String = "POST",
    val statusCode: Int = 200,
    val responseBody: String = "{}",
    val enabled: Boolean = true,
    val label: String = "",
    val simulateTimeout: Boolean = false,
)

object MockApiResponseStore {
    const val PRESET_EMPTY_BODY = "{}"
    const val PRESET_401_BODY =
        """{"message":"Unauthorized","statusCode":401,"success":false}"""
    const val PRESET_500_BODY =
        """{"message":"Internal Server Error","statusCode":500,"success":false}"""

    private val _globalEnabled = MutableStateFlow(true)
    val globalEnabledFlow: StateFlow<Boolean> = _globalEnabled.asStateFlow()

    val globalEnabled: Boolean
        get() = _globalEnabled.value

    private val _mocks = MutableStateFlow<List<SavedMockResponse>>(emptyList())
    val mocks: StateFlow<List<SavedMockResponse>> = _mocks.asStateFlow()

    private var persistSnapshot: ((globalEnabled: Boolean, mocks: List<SavedMockResponse>) -> Unit)? = null

    fun attachPersistence(
        onPersist: (globalEnabled: Boolean, mocks: List<SavedMockResponse>) -> Unit,
    ) {
        persistSnapshot = onPersist
    }

    fun restore(globalEnabled: Boolean, mocks: List<SavedMockResponse>) {
        _globalEnabled.value = globalEnabled
        _mocks.value = mocks
    }

    private fun persistNow() {
        persistSnapshot?.invoke(_globalEnabled.value, _mocks.value)
    }

    fun setGlobalEnabled(enabled: Boolean) {
        _globalEnabled.value = enabled
        persistNow()
    }

    fun save(mock: SavedMockResponse) {
        _mocks.update { list ->
            val filtered = list.filterNot {
                it.urlPattern == mock.urlPattern &&
                    it.httpMethod.equals(mock.httpMethod, ignoreCase = true)
            }
            filtered + mock.copy(id = mock.id.ifBlank { UUID.randomUUID().toString() })
        }
        persistNow()
    }

    fun update(mock: SavedMockResponse) {
        _mocks.update { list ->
            val withoutCurrent = list.filterNot { it.id == mock.id }
            val withoutDuplicate = withoutCurrent.filterNot {
                it.urlPattern == mock.urlPattern &&
                    it.httpMethod.equals(mock.httpMethod, ignoreCase = true)
            }
            withoutDuplicate + mock
        }
        persistNow()
    }

    fun remove(id: String) {
        _mocks.update { list -> list.filterNot { it.id == id } }
        persistNow()
    }

    fun setEnabled(id: String, enabled: Boolean) {
        _mocks.update { list ->
            list.map { if (it.id == id) it.copy(enabled = enabled) else it }
        }
        persistNow()
    }

    fun clearAll() {
        _mocks.value = emptyList()
        persistNow()
    }

    fun findMatch(url: String, encodedPath: String, method: String): SavedMockResponse? {
        if (!globalEnabled) return null
        return _mocks.value
            .filter { it.enabled && matches(url, encodedPath, method, it) }
            .maxByOrNull { matchScore(url, encodedPath, it) }
    }

    fun createPreset(
        urlPattern: String,
        httpMethod: String,
        preset: MockResponsePreset,
    ): SavedMockResponse {
        return when (preset) {
            MockResponsePreset.EMPTY -> SavedMockResponse(
                urlPattern = urlPattern,
                httpMethod = httpMethod,
                statusCode = 200,
                responseBody = PRESET_EMPTY_BODY,
                label = "Empty {}",
            )

            MockResponsePreset.UNAUTHORIZED -> SavedMockResponse(
                urlPattern = urlPattern,
                httpMethod = httpMethod,
                statusCode = 401,
                responseBody = PRESET_401_BODY,
                label = "401 Unauthorized",
            )

            MockResponsePreset.SERVER_ERROR -> SavedMockResponse(
                urlPattern = urlPattern,
                httpMethod = httpMethod,
                statusCode = 500,
                responseBody = PRESET_500_BODY,
                label = "500 Server Error",
            )

            MockResponsePreset.TIMEOUT -> SavedMockResponse(
                urlPattern = urlPattern,
                httpMethod = httpMethod,
                statusCode = 504,
                responseBody = PRESET_EMPTY_BODY,
                label = "Timeout",
                simulateTimeout = true,
            )
        }
    }

    private fun matches(
        url: String,
        encodedPath: String,
        method: String,
        mock: SavedMockResponse,
    ): Boolean {
        if (mock.httpMethod != "*" &&
            !mock.httpMethod.equals(method, ignoreCase = true)
        ) {
            return false
        }
        val pattern = mock.urlPattern.trim()
        if (pattern.isEmpty()) return false
        return url == pattern ||
            encodedPath == pattern ||
            url.endsWith(pattern) ||
            encodedPath.endsWith(pattern)
    }

    private fun matchScore(url: String, encodedPath: String, mock: SavedMockResponse): Int {
        val pattern = mock.urlPattern.trim()
        return when {
            url == pattern -> 4
            encodedPath == pattern -> 3
            url.endsWith(pattern) -> 2
            encodedPath.endsWith(pattern) -> 1
            else -> 0
        }
    }
}

enum class MockResponsePreset {
    EMPTY,
    UNAUTHORIZED,
    SERVER_ERROR,
    TIMEOUT,
}
