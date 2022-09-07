package com.amcoding.pokemonproject.pokemonlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.amcoding.pokemonproject.R
import com.amcoding.pokemonproject.data.models.PokedexListEntry
import com.amcoding.pokemonproject.ui.theme.RobotoCondensed
import kotlinx.coroutines.launch


//The composable functions for the Pokemon List screen. As this is a relatively basic screen I did not separate
//compose elements into their own files.
//The Screen has its background set to the them background so this will display the background color as the light theme
//or dark theme depending on user settings.
@Composable
fun PokemonListScreen(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel()
){
 Surface(
     color = MaterialTheme.colors.background,
     modifier = Modifier.fillMaxSize()
 ){
    Column{
        Spacer(modifier = Modifier.height(20.dp))
        Image(painter = painterResource(
            id = R.drawable.ic_international_pokemon_logo),
            contentDescription = "Pokemon Logo",
            modifier = Modifier
                .fillMaxWidth()
                .align(CenterHorizontally)
            )
        SearchBar(
            hint = "Search...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ){
            viewModel.searchPokemonList(it)
        }
        Spacer(modifier = Modifier.height(16.dp))
        PokemonList(navController = navController)
    }
 }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    //Will trigger an on search function once the user has entered text.
    onSearch: (String) -> Unit = {}
){
    var text by remember {
        mutableStateOf("")
    }
    var isHintDisplayed by remember {
        mutableStateOf(hint != "")
    }

    Box(modifier = modifier){
        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                //The function is triggered as the user enters text
                onSearch(it)
            },
            //Keeps any text entered into the search bar on one tine
            maxLines = 1,
            singleLine = true,
            //Standard search bar style choice, white background, black text, internal padding.
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(5.dp, CircleShape)
                .background(Color.White, CircleShape)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                //Disables the search bar hint if it is in focus.
                .onFocusChanged { focusState ->
                    isHintDisplayed = when {
                        focusState.isFocused -> false
                        text.isNotEmpty() -> false
                        else -> true
                    }
                }
        )
        if(isHintDisplayed){
            Text(
                text = hint,
                color = Color.LightGray,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }

}

@Composable
fun PokemonList(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel()
){
    val pokemonList by remember { viewModel.pokemonList }
    val endReached by remember { viewModel.endReached }
    val loadError by remember { viewModel.loadError }
    val isLoading by remember { viewModel.isLoading }
    val isSearching by remember { viewModel.isSearching }

    LazyColumn(contentPadding = PaddingValues(16.dp)){
        val itemCount = if(pokemonList.size %2 == 0){
            pokemonList.size/2
        }else{
            pokemonList.size/2 + 1
        }

        items(itemCount){
            if(it >= itemCount - 1 && !endReached && !isLoading && !isSearching){
                viewModel.loadPokemonPaginated()
            }
            PokedexRow(rowIndex = it, entries = pokemonList, navController = navController)
        }
    }
    Box(
        contentAlignment = Center,
        modifier = Modifier.fillMaxSize()){
        if(isLoading){
            CircularProgressIndicator(color = MaterialTheme.colors.primary)
        }
        if(loadError.isNotEmpty()){
            RetrySection(error = loadError) {
                viewModel.loadPokemonPaginated()
            }
        }
    }
}




//The composable for individual pokedex entries that will be display on the Pokemon list screen.
//The background color for the pokemon entry will be a color gradient.
@Composable
fun PokedexEntry(
    entry: PokedexListEntry,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel()
) {

    val defaultDominantColor = MaterialTheme.colors.surface
    var dominantColor by remember {
        mutableStateOf(defaultDominantColor)
    }

    Box(
        contentAlignment = Center,
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1f)
            .background(
                Brush.verticalGradient(
                    listOf(
                        dominantColor,
                        defaultDominantColor
                    )
                )
            )
            .clickable {
                navController.navigate(
                    "pokemon_detail_screen/${dominantColor.toArgb()}/${entry.pokemonName}"
                )
            }
    ) {
        Column {
            //Get the reference to the image
            val painter = rememberAsyncImagePainter(
                model = entry.imageUrl
            )
            val painterState = painter.state
            Image(
                painter = painter,
                contentDescription = entry.pokemonName,
                modifier = Modifier
                    .size(120.dp)
                    .align(CenterHorizontally),
            )
            //Loading wheel shows while the image is loading.
            if (painterState is AsyncImagePainter.State.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .scale(0.5f)
                        .align(CenterHorizontally)
                )
            }
            //Once the image has been loaded the calculate dominant color function is used to set the background.
            else if (painterState is AsyncImagePainter.State.Success) {
                LaunchedEffect(key1 = painter) {
                    launch {
                        val image = painter.imageLoader.execute(painter.request).drawable
                        viewModel.calcDominantColor(image!!) {
                            dominantColor = it
                        }
                    }
                }
            }

            Text(
                text = entry.pokemonName,
                fontFamily = RobotoCondensed,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
//A row within the Pokedex list screen that will Pokedex entries.
@Composable
fun PokedexRow(
    rowIndex: Int,
    entries: List<PokedexListEntry>,
    navController: NavController
) {
    Column {
        Row {
            PokedexEntry(
                entry = entries[rowIndex * 2],
                navController = navController,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            if(entries.size >= rowIndex * 2 + 2) {
                PokedexEntry(
                    entry = entries[rowIndex * 2 + 1],
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
//If the Pokemon fail to load (due to not being connected to Wifi for example), a retry button will load that will
//allow the user to retry the loading Pokemon process.
//This error handling is currently very general, but specific error handling could be added later.
fun RetrySection(
    error: String,
    onRetry: () -> Unit
){
    Column {
        Text(text = error, color = Color.Red, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {onRetry()},
            modifier = Modifier.align(CenterHorizontally)
        ) {
            Text(text = "Retry")
        }
    }
}