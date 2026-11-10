package ca.pebble.watchpatrol.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import ca.pebble.watchpatrol.data.repository.CatalogRepository
import ca.pebble.watchpatrol.di.AppContainer

class BrowseViewModel(
    catalogRepository: CatalogRepository,
) : ViewModel() {

    constructor(container: AppContainer) : this(container.catalogRepository)

    val brands = catalogRepository.brands().cachedIn(viewModelScope)
}
