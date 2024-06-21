package com.example.hawkeyeapp.Model

import java.sql.Timestamp

class Estado {
    var id: String = ""
    var estadoCambio: String = "" 
    lateinit var fecha: Timestamp


    constructor(id: String, estadoCambio: String, fecha: Timestamp) : this() {
        this.id = id
        this.estadoCambio = estadoCambio
        this.fecha = fecha
    }

    constructor()

}