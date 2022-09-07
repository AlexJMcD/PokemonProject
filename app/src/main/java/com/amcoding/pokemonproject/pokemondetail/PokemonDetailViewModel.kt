package com.amcoding.pokemonproject.pokemondetail

import androidx.lifecycle.ViewModel
import com.amcoding.pokemonproject.data.remote.responses.Pokemon
import com.amcoding.pokemonproject.repository.PokemonRepository
import com.amcoding.pokemonproject.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


//The view model contains a function to retrieve the Pokemon specific information.
@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val repository: PokemonRepository
): ViewModel() {

    suspend fun getPokemonInfo(pokemonName: String): Resource<Pokemon>{
        return repository.getPokemonInfo(pokemonName)
    }
}