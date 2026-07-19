package com.sangeetsetu.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val prefs = application.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedState = MutableStateFlow("")
    val selectedState: StateFlow<String> = _selectedState.asStateFlow()

    private val _selectedDistrict = MutableStateFlow("")
    val selectedDistrict: StateFlow<String> = _selectedDistrict.asStateFlow()

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchResults = MutableStateFlow<SearchUIState>(SearchUIState.Empty)
    val searchResults: StateFlow<SearchUIState> = _searchResults.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _popularArtists = MutableStateFlow<List<User>>(emptyList())
    val popularArtists: StateFlow<List<User>> = _popularArtists.asStateFlow()

    private val _popularCategories = MutableStateFlow<List<Category>>(emptyList())
    val popularCategories: StateFlow<List<Category>> = _popularCategories.asStateFlow()

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    val allCategories: StateFlow<List<Category>> = _allCategories.asStateFlow()

    init {
        loadRecentSearches()
        fetchPopularData()
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        performSearchDebounced()
    }

    private fun performSearchDebounced() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (_query.value.isNotEmpty()) {
                kotlinx.coroutines.delay(500) // 500ms debounce
            }
            performSearch()
        }
    }

    fun onStateChange(state: String) {
        _selectedState.value = state
        _selectedDistrict.value = "" // Reset district when state changes
        performSearchDebounced()
    }

    fun onDistrictChange(district: String) {
        _selectedDistrict.value = district
        performSearchDebounced()
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
        performSearchDebounced()
    }

    fun onLocationChange(state: String, district: String) {
        _selectedState.value = state
        _selectedDistrict.value = district
        performSearchDebounced()
    }

    private fun performSearch() {
        val q = _query.value
        val stateFilter = _selectedState.value
        val districtFilter = _selectedDistrict.value
        val categoryFilter = _selectedCategory.value

        if (q.isBlank() && stateFilter.isBlank() && districtFilter.isBlank() && categoryFilter.isBlank()) {
            _searchResults.value = SearchUIState.Empty
            return
        }

        viewModelScope.launch {
            _searchResults.value = SearchUIState.Loading
            try {
                // Fetch all artists and filter client-side for better reliability with inconsistent data
                val artistsSnap = db.collection("users")
                    .whereEqualTo("userType", "Artist")
                    .get().await()
                
                val allArtists = artistsSnap.toObjects(User::class.java)
                
                val filteredArtists = allArtists.filter { artist ->
                    // Standardize status checks
                    val isStatusActive = artist.status.equals("ACTIVE", ignoreCase = true) || artist.status.equals("active", ignoreCase = true)
                    val isAccountActive = artist.accountStatus.equals("ACTIVE", ignoreCase = true) || artist.accountStatus.equals("active", ignoreCase = true)
                    val isApproved = artist.approvalStatus.equals("APPROVED", ignoreCase = true) || artist.isApproved
                    
                    if (!isStatusActive || !isAccountActive || !isApproved) return@filter false

                    // Apply Location Filters
                    if (stateFilter.isNotEmpty() && !artist.state.equals(stateFilter, ignoreCase = true)) return@filter false
                    if (districtFilter.isNotEmpty() && !artist.district.equals(districtFilter, ignoreCase = true)) return@filter false
                    
                    // Apply Category Filter
                    if (categoryFilter.isNotEmpty()) {
                        val isCatMatch = artist.categoryId.trim().equals(categoryFilter.trim(), ignoreCase = true)
                        if (!isCatMatch) return@filter false
                    }

                    // Apply Text Query
                    if (q.isNotEmpty()) {
                        val matchesQuery = artist.name.contains(q, ignoreCase = true) || 
                                           artist.category.contains(q, ignoreCase = true) ||
                                           artist.categoryId.contains(q, ignoreCase = true) ||
                                           artist.city.contains(q, ignoreCase = true) ||
                                           artist.skill.contains(q, ignoreCase = true) ||
                                           artist.artistId.contains(q, ignoreCase = true)
                        if (!matchesQuery) return@filter false
                    }
                    
                    true
                }

                val categoriesSnap = db.collection("categories").get().await()
                val allCategoriesList = categoriesSnap.documents.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                }
                
                val filteredCategories = if (q.isBlank()) emptyList() else allCategoriesList
                    .filter { it.name.contains(q, ignoreCase = true) }

                val locations = allArtists
                    .map { if (it.district.isNotEmpty()) "${it.district}, ${it.state}" else it.state }
                    .filter { it.contains(q, ignoreCase = true) }
                    .distinct()

                if (filteredArtists.isEmpty() && filteredCategories.isEmpty() && locations.isEmpty()) {
                    _searchResults.value = SearchUIState.NoResults
                } else {
                    _searchResults.value = SearchUIState.Success(
                        artists = filteredArtists,
                        categories = filteredCategories,
                        locations = locations
                    )
                }
            } catch (e: Exception) {
                _searchResults.value = SearchUIState.Error(e.localizedMessage ?: "Search failed")
            }
        }
    }

    fun addRecentSearch(q: String) {
        if (q.isBlank()) return
        val current = _recentSearches.value.toMutableList()
        current.remove(q)
        current.add(0, q)
        val updated = current.take(10)
        _recentSearches.value = updated
        prefs.edit().putStringSet("recent_searches", updated.toSet()).apply()
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        prefs.edit().remove("recent_searches").apply()
    }

    private fun loadRecentSearches() {
        val set = prefs.getStringSet("recent_searches", emptySet()) ?: emptySet()
        _recentSearches.value = set.toList()
    }

    private fun fetchPopularData() {
        viewModelScope.launch {
            try {
                val artistsSnap = db.collection("users")
                    .whereEqualTo("userType", "Artist")
                    .whereEqualTo("status", "active")
                    .limit(5)
                    .get().await()
                _popularArtists.value = artistsSnap.toObjects(User::class.java)
                
                val catSnap = db.collection("categories").get().await()
                val categories = catSnap.documents.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                }
                _allCategories.value = categories
                _popularCategories.value = categories.take(6)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

sealed class SearchUIState {
    object Empty : SearchUIState()
    object Loading : SearchUIState()
    data class Success(
        val artists: List<User>,
        val categories: List<Category>,
        val locations: List<String>
    ) : SearchUIState()
    object NoResults : SearchUIState()
    data class Error(val message: String) : SearchUIState()
}
