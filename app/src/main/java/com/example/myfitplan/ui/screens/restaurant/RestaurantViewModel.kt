package com.example.myfitplan.ui.screens.restaurant

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RestaurantViewModel : ViewModel() {
    private val _radius = MutableStateFlow(1)
    val radius: StateFlow<Int> = _radius

    fun setRadius(value: Int) {
        _radius.value = value
    }
}