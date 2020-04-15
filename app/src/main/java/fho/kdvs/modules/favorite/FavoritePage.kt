package fho.kdvs.modules.favorite

interface FavoritePage<T> {
    fun subscribeToViewModel()
    fun processFavorites(joins: List<T>?)
    fun setSectionHeaders()
    fun clearSectionHeaders()
    fun initializeClickListeners()
    fun initializeSearchBar()
}