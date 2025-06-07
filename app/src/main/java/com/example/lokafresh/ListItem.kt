    package com.example.lokafresh

    data class ListItem(
        val icon: Int,
        val title: String,
        val subtitle: String,
        var description: String,
        var isChecked: Boolean = false
    )