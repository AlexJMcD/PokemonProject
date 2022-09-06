package com.amcoding.pokemonproject.data.remote.responses

data class Move(
    val move: MoveX,
    val versionGroupDetails: List<VersionGroupDetail>
)