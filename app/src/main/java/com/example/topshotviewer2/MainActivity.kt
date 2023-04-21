package com.example.topshotviewer2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import com.example.topshotviewer2.data.players.MintedMomentRepository
import com.example.topshotviewer2.data.players.PlayersRepository
import com.example.topshotviewer2.model.Player
import com.example.topshotviewer2.model.PlayerListPlayer
import com.example.topshotviewer2.ui.players.PlayerListViewModel
import com.example.topshotviewer2.ui.players.PlayerMomentsViewModel
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
    var allPlayersTab by remember { mutableStateOf(true) }
    var usersTab by remember { mutableStateOf(false) }
    var favoritesTab by remember { mutableStateOf(false) }

    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = {
            TopShotBottomNavigation(
                onTabPlayers = {
                    allPlayersTab = true
                    favoritesTab = false
                    usersTab = false
                },
                onTabFavorites = {
                    allPlayersTab = false
                    favoritesTab = true
                    usersTab = false
                },
                onTabUser = {
                    allPlayersTab = false
                    favoritesTab = false
                    usersTab = true
                },
                playerTab = allPlayersTab,
                favoriteTab = favoritesTab,
                userTab = usersTab,
            )
        }
    ) { padding ->
        if (allPlayersTab || favoritesTab) {
            PlayerListView(
                modifier = modifier.padding(padding),
                viewModel = viewModel(
                    factory = PlayerListViewModel.provideFactory(playersRepository)
                ),
                detailsViewModel = viewModel(
                    factory = PlayerViewModel.provideFactory(playersRepository)
                ),
                tabPlayer = allPlayersTab,
                tabUser = usersTab,
                tabFavorites = favoritesTab,
            )
        } else if (usersTab) {
            val playerMomentRepository = MintedMomentRepository()
            UserMomentListView(
                modifier = modifier.padding(padding),
                viewModel = viewModel(
                    factory = PlayerMomentsViewModel.provideFactory(playerMomentRepository)
                ),
            )
        }
    }
}

@Composable
fun PlayerListView(
    modifier: Modifier = Modifier, viewModel: PlayerListViewModel = viewModel(),
    detailsViewModel: PlayerViewModel = viewModel(), tabPlayer: Boolean = false,
    tabUser: Boolean = false, tabFavorites: Boolean = false,
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
                items = if (tabPlayer) {
                    all
                } else {
                    playersLiked
                }
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
        val mintedMomentRepository = MintedMomentRepository()
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
fun UserMomentListView(
    modifier: Modifier = Modifier,
    viewModel: PlayerMomentsViewModel = viewModel()
) {
    val viewModelState by viewModel.viewModelStatePublic.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadMoments()
    }
    LazyColumn(modifier = modifier) {
        val mintedMoments = viewModelState.momentList.moments
        items(items = mintedMoments) { moment ->
            Column {
                Text(moment.playerTitle)
                Text(moment.tierName)
                Text(moment.serialNumber)
                Text(moment.thumbnail)
            }
            Divider()
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
    onTabPlayers: () -> Unit, onTabFavorites: () -> Unit, onTabUser: () -> Unit,
    playerTab: Boolean, favoriteTab: Boolean, userTab: Boolean,
) {
    BottomNavigation(modifier) {
        BottomNavigationItem(selected = playerTab,
            onClick = onTabPlayers,
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Players"
                )
            },
            label = { Text(stringResource(R.string.bottom_players)) }
        )
        BottomNavigationItem(selected = favoriteTab,
            onClick = onTabFavorites,
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite Players"
                )
            },
            label = { Text(stringResource(R.string.bottom_favorites)) }
        )
        BottomNavigationItem(selected = userTab,
            onClick = onTabUser,
            icon = {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "User Moments"
                )
            },
            label = { Text(stringResource(R.string.bottom_user)) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationPreview() {
    TopShotViewer2Theme {
        TopShotBottomNavigation(
            Modifier.padding(top = 24.dp),
            onTabPlayers = {}, onTabFavorites = {}, onTabUser = {},
            playerTab = true, favoriteTab = false, userTab = false,
        )
    }
}
