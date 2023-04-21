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
import androidx.compose.material.icons.filled.*
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
            MaterialTheme {
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
            TopShotBottomNavigation(
                onTabAllTrue = { tabAll = true }, onTabAllFalse = { tabAll = false },
                playerTab = tabAll,
            )
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

    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val playerDetails = detailsViewModelState.player
    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            PlayerDetailsView(
                modifier = modifier.padding(start = 16.dp, top = 32.dp),
                playerDetails = playerDetails,
                isFavorite = viewModelState.favorites.contains(playerDetails?.id),
                onClickLike = {
                    playerDetails?.apply { viewModel.likeUnLike(this.id) }
                }
            )
        },
        gesturesEnabled = true,
        drawerBackgroundColor = MaterialTheme.colors.background
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val all = viewModelState.playerlist.allPlayers
            val playersLiked = viewModelState.favorites.map { all.find { player -> player.id == it } }.filterNotNull()
            items(
                items = if (tabAll) all else playersLiked
            ) { player ->
                PlayerListPlayerView(
                    player,
                    isFavorite = viewModelState.favorites.contains(player.id),
                    onDetailsClick = {
                        coroutineScope.launch {
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
    modifier: Modifier = Modifier, playerDetails: Player?, isFavorite: Boolean,
    onClickLike: () -> Unit,
) {
    Column(modifier = modifier) {
        if (playerDetails != null) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(playerDetails.firstName ?: "", style = MaterialTheme.typography.h3)
                Text(playerDetails.lastName ?: "", style = MaterialTheme.typography.h3)
                Spacer(Modifier.size(8.dp))
                Text(playerDetails.jerseyNumber?.let { "Jersey Number: $it" } ?: "")
                Text(playerDetails.currentTeamName?.let { "Team Name: $it" } ?: "")
                Text(playerDetails.position?.let { "Position: $it" } ?: "")
            }
            Spacer(Modifier.size(98.dp))
            Divider()
            Spacer(Modifier.size(16.dp))
            Button(onClick = onClickLike) {
                // Inner content including an icon and a text label
                Icon(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like button",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(if (isFavorite) "Liked" else "Like")
            }
        } else {
            Text("Details not available")
        }
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
                style = MaterialTheme.typography.body1,
            )
            if (isFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Liked Player"
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
    onTabAllTrue: () -> Unit, onTabAllFalse: () -> Unit,
    playerTab: Boolean,
) {
    BottomNavigation(modifier) {
        BottomNavigationItem(selected = playerTab,
            onClick = onTabAllTrue,
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Players"
                )
            },
            label = { Text(stringResource(R.string.bottom_players)) }
        )
        BottomNavigationItem(selected = !playerTab,
            onClick = onTabAllFalse,
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite Players"
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
            onTabAllTrue = {}, onTabAllFalse = {},
            playerTab = true
        )
    }
}
