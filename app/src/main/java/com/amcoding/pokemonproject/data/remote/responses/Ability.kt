package com.amcoding.pokemonproject.data.remote.responses

data class Ability(
    val ability: AbilityX,
    val isHidden: Boolean,
    val slot: Int
)