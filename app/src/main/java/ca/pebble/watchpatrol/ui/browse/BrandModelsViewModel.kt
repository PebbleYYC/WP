package ca.pebble.watchpatrol.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import ca.pebble.watchpatrol.data.repository.CatalogRepository

class BrandModelsViewModel(
    catalogRepository: CatalogRepository,
    brandSlug: String,
) : ViewModel() {

    val models = catalogRepository
        .models(brandSlug = brandSlug)
        .cachedIn(viewModelScope)
}
