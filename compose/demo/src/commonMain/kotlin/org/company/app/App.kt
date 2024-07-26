package org.company.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.microsoft.design.compose.icons.Res
import com.microsoft.design.compose.icons.allDrawableResources
import com.microsoft.design.compose.icons.ic_fluent_dismiss_32_light
import com.microsoft.design.compose.icons.ic_fluent_dismiss_circle_32_light
import com.microsoft.design.compose.icons.ic_fluent_search_48_regular
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import my.nanihadesuka.compose.LazyVerticalGridScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.company.app.theme.AppTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun App() = AppTheme {
    val allItems = remember { Res.allDrawableResources.entries.toList() }
    val (filteredItems, setFilteredItems) = remember { mutableStateOf(allItems) }
    val (searchText, setSearchText) = remember { mutableStateOf("") }

    LaunchedEffect(searchText) {
        delay(300)
        if (searchText.isEmpty()) {
            setFilteredItems(allItems)
        } else {
            val new = async(Dispatchers.Default) {
                allItems.sortedByDescending { (key, res) ->
                    FuzzySearch.ratio(
                        searchText.lowercase(),
                        key.lowercase().removePrefix("ic_fluent_")
                    )
                }
            }
            setFilteredItems(new.await())
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            OutlinedTextField(
                modifier = Modifier.widthIn(min = 500.dp),
                shape = RoundedCornerShape(50),
                value = searchText,
                onValueChange = { setSearchText(it) },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(Res.drawable.ic_fluent_search_48_regular),
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = { setSearchText("") }
                    ) {
                        Icon(painterResource(Res.drawable.ic_fluent_dismiss_circle_32_light), null)
                    }
                }
            )
        }

        val state = rememberLazyGridState()
        LazyVerticalGridScrollbar(
            state = state,
            settings = ScrollbarSettings.Default.copy(alwaysShowScrollbar = true)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(300.dp),
                state = state,
                contentPadding = PaddingValues(
                    start = 12.dp,
                    top = 16.dp,
                    end = 12.dp,
                    bottom = 16.dp
                ),
                content = {
                    items(filteredItems) { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                painterResource(item.value),
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).padding(bottom = 16.dp)
                            )
                            SelectionContainer {
                                Text(item.key)
                            }
                        }
                    }
                }
            )
        }
    }
}
