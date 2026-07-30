package com.dragannovakovic.bblauncher.ui.apps

internal fun matchesAppQuery(
    label: String,
    packageName: String,
    query: String,
): Boolean {
    val normalizedQuery = query.trim()
    return normalizedQuery.isEmpty() ||
        label.contains(normalizedQuery, ignoreCase = true) ||
        packageName.contains(normalizedQuery, ignoreCase = true)
}
