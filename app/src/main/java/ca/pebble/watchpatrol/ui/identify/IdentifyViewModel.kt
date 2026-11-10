package ca.pebble.watchpatrol.ui.identify

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.pebble.watchpatrol.data.repository.IdentifyRepository
import ca.pebble.watchpatrol.di.AppContainer
import ca.pebble.watchpatrol.domain.model.IdentifyMatch
import ca.pebble.watchpatrol.util.Async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IdentifyViewModel(
    private val identifyRepository: IdentifyRepository,
) : ViewModel() {

    constructor(container: AppContainer) : this(container.identifyRepository)

    // null = idle (nothing picked yet)
    private val _state = MutableStateFlow<Async<List<IdentifyMatch>>?>(null)
    val state = _state.asStateFlow()

    private val _selectedImage = MutableStateFlow<Uri?>(null)
    val selectedImage = _selectedImage.asStateFlow()

    fun onImagePicked(context: Context, uri: Uri) {
        _selectedImage.value = uri
        _state.value = Async.Loading
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }.getOrNull()
            }
            if (bytes == null) {
                _state.value = Async.Error("Couldn't read that image")
                return@launch
            }
            _state.value = try {
                Async.Success(identifyRepository.identify(bytes))
            } catch (e: Exception) {
                Async.Error(e.message ?: "Identification failed")
            }
        }
    }

    fun reset() {
        _state.value = null
        _selectedImage.value = null
    }
}
