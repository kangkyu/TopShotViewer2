package com.example.topshotviewer2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.topshotviewer2.data.players.PlayersRepository
import com.example.topshotviewer2.model.Player
import com.example.topshotviewer2.model.PlayerListPlayer
import com.example.topshotviewer2.ui.players.PlayerListViewModel
import com.example.topshotviewer2.ui.players.PlayerViewModel
import com.example.topshotviewer2.ui.theme.TopShotViewer2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TopShotViewer2Theme {
                TopShotApp()
            }
        }
    }
}

@Composable
fun TopShotApp(
    modifier: Modifier = Modifier,
) {
    val scaffoldState = rememberScaffoldState()
    val playersRepository = PlayersRepository()
    var tabAll by remember { mutableStateOf(true) }

    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = {
            TopShotBottomNavigation(onTabAllTrue = { tabAll = true }, onTabAllFalse = { tabAll = false })
        }
    ) { padding ->
        PlayerListView(
            modifier = modifier.padding(padding),
            viewModel = viewModel(
                factory = PlayerListViewModel.provideFactory(playersRepository)
            ),
            detailsViewModel = viewModel(
                factory = PlayerViewModel.provideFactory(playersRepository)
            ),
            tabAll = tabAll
        )
    }
}

@Composable
fun PlayerListView(
    modifier: Modifier = Modifier, viewModel: PlayerListViewModel = viewModel(),
    detailsViewModel: PlayerViewModel = viewModel(), tabAll: Boolean = false
) {
    val viewModelState by viewModel.viewModelStatePublic.collectAsState()
    val detailsViewModelState by detailsViewModel.viewModelStatePublic.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshPlayers()
        viewModel.refreshFavorites()
    }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val playerDetails = detailsViewModelState.player
    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(modifier = modifier) {
                PlayerDetailsView(
                    playerDetails,
                    onToggleFavorite = {
                        playerDetails?.apply { viewModel.likeUnLike(this.id) }
                    },
                    isFavorite = viewModelState.favorites.contains(playerDetails?.id)
                )

                OutlinedButton(
                    onClick = {
                        scope.launch { drawerState.close() }
                    }
                ) {
                    Text(stringResource(R.string.button_back), style = MaterialTheme.typography.body2)
                }
            }
        },
        gesturesEnabled = true,
        drawerBackgroundColor = MaterialTheme.colors.background
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val all = viewModelState.playerlist.allPlayers
            val playersLiked = viewModelState.favorites.map {
                all.find { player -> player.id == it }!!
            }
            items(
                items = if (tabAll) {
                            all
                        } else {
                            playersLiked
                        }
            ) { player ->
                PlayerListPlayerView(
                    player,
                    isFavorite = viewModelState.favorites.contains(player.id),
                    onDetailsClick = {
                        scope.launch {
                            drawerState.open()
                            detailsViewModel.loadPlayer(player.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlayerDetailsView(
    playerDetails: Player?, onToggleFavorite: () -> Unit,
    isFavorite: Boolean = false,
) {
    return if (playerDetails != null) {
        Row {
            Column {
                Text(playerDetails.firstName ?: "", style = MaterialTheme.typography.h1)
                Text(playerDetails.lastName ?: "", style = MaterialTheme.typography.h1)
                Text(playerDetails.jerseyNumber?.let { "Jersey Number: $it" }
                    ?: "details not available")
                Text(playerDetails.currentTeamName?.let { "Team Name: $it" } ?: "")
                Text(playerDetails.position?.let { "Position: $it" } ?: "")
            }
            TextButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    contentDescription = null
                )
            }
        }
    } else {
        Text("Details not available")
    }
}

@Composable
fun PlayerListPlayerView(
    player: PlayerListPlayer, onDetailsClick: () -> Unit, modifier: Modifier = Modifier,
    isFavorite: Boolean = false
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        modifier = modifier.clickable(onClick = { onDetailsClick() })
    ) {
        Row(modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = player.name,
                modifier = Modifier.padding(all = 4.dp),
                style = MaterialTheme.typography.h3,
            )
            if (isFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TopShotViewer2Theme {
        PlayerListPlayerView(
            PlayerListPlayer(id="1630462", name="Aari McDonald"),
            modifier = Modifier.padding(8.dp),
            onDetailsClick = {}
        )
    }
}

@Composable
fun TopShotBottomNavigation(
    modifier: Modifier = Modifier,
    onTabAllTrue: () -> Unit, onTabAllFalse: () -> Unit
) {
    BottomNavigation(modifier) {
        BottomNavigationItem(selected = true,
            onClick = onTabAllTrue,
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.bottom_players)) }
        )
        BottomNavigationItem(selected = false,
            onClick = onTabAllFalse,
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.bottom_favorites)) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationPreview() {
    TopShotViewer2Theme {
        TopShotBottomNavigation(
            Modifier.padding(top = 24.dp),
            onTabAllTrue = {}, onTabAllFalse = {}
        )
    }
}
