package com.gastro.nameet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gastro.nameet.model.BookmarkItem
import com.gastro.nameet.model.BookmarkSortOption
import com.gastro.nameet.model.BookmarkType
import com.gastro.nameet.ui.theme.PrimaryOrange
import com.gastro.nameet.util.RouteConstants
import com.gastro.nameet.viewmodel.BookmarkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    navController: NavController,
    viewModel: BookmarkViewModel = hiltViewModel()
) {
    val state by viewModel.bookmarkState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf<BookmarkType?>(null) }

    // Bookmarks now load reactively from the active user — no manual trigger.

    val filtered = remember(state.bookmarks, filter) {
        if (filter == null) state.bookmarks else state.bookmarks.filter { it.type == filter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Bookmarks", fontWeight = FontWeight.Bold) },
                actions = {
                    if (state.bookmarks.isNotEmpty()) {
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.Sort, contentDescription = "Sort")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                BookmarkSortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (state.sortOption == option) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }
                                                Text(option.displayName)
                                            }
                                        },
                                        onClick = {
                                            viewModel.setSortOption(option)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryOrange
                )
            } else if (state.bookmarks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No bookmarks yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bookmark places or Ilonggo specialties to save them here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterPill(
                                label = "All (${state.bookmarks.size})",
                                selected = filter == null,
                                onClick = { filter = null }
                            )
                            val placeCount = state.bookmarks.count { it.type == BookmarkType.PLACE }
                            val specCount = state.bookmarks.count { it.type == BookmarkType.SPECIALTY }
                            FilterPill(
                                label = "Places ($placeCount)",
                                selected = filter == BookmarkType.PLACE,
                                onClick = { filter = BookmarkType.PLACE }
                            )
                            FilterPill(
                                label = "Specialties ($specCount)",
                                selected = filter == BookmarkType.SPECIALTY,
                                onClick = { filter = BookmarkType.SPECIALTY }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sorted by: ${state.sortOption.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(filtered, key = { it.key }) { item ->
                        BookmarkRow(
                            item = item,
                            onClick = {
                                when (item.type) {
                                    BookmarkType.PLACE -> item.placeId?.let {
                                        navController.navigate("${RouteConstants.PLACE_DETAILS}/$it")
                                    }
                                    BookmarkType.SPECIALTY -> item.specialtyId?.let {
                                        navController.navigate("${RouteConstants.FOOD_DETAIL}/$it")
                                    }
                                }
                            },
                            onLocateClick = if (item.type == BookmarkType.PLACE && item.placeId != null) {
                                {
                                    navController.navigate(
                                        "${RouteConstants.MAPS}?${RouteConstants.MAP_QUERY_ARG}=&${RouteConstants.LOCATE_PLACE_ID_ARG}=${item.placeId}"
                                    ) {
                                        popUpTo(RouteConstants.HOME)
                                        launchSingleTop = true
                                    }
                                }
                            } else null,
                            onRemoveClick = {
                                viewModel.removeBookmark(item)
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(text = state.error!!)
                }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) PrimaryOrange else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun BookmarkRow(
    item: BookmarkItem,
    onClick: () -> Unit,
    onLocateClick: (() -> Unit)?,
    onRemoveClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ) {
            if (item.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (item.type == BookmarkType.PLACE) Icons.Default.Restaurant
                            else Icons.Default.LocalDining,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryOrange.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (item.type == BookmarkType.PLACE) "Place" else "Specialty",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryOrange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.rating > 0f) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { i ->
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (i < item.rating) PrimaryOrange else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", item.rating),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (onLocateClick != null) {
                    IconButton(onClick = onLocateClick) {
                        Icon(Icons.Default.Map, contentDescription = "Locate", tint = PrimaryOrange)
                    }
                }
                IconButton(onClick = onRemoveClick) {
                    Icon(
                        Icons.Default.BookmarkRemove,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
