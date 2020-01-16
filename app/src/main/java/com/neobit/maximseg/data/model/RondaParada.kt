package com.neobit.maximseg.data.model

data class RondaParada(
    val id_ronda_parada: Int,
    val id_ronda: Int,
    val tareas: Boolean,
    val codigo: String,
    val nombre: String,
    val hora: String
    )