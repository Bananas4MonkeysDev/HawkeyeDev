package com.example.hawkeyeapp.Model

import java.sql.Timestamp

class Viaje {
    var id: String? = null
    var pasajeroId: String? = null
    var ubi_ini: String? = null
    var ubi_desti: String? = null
    var placa: String? = null
    var aplicativo: String? = null
    var nombreCondu:String? = null
    var fecha: Long = System.currentTimeMillis()
    var estados: HashMap<String, Boolean> = hashMapOf()
    var estadoViaje:String?=null

    constructor()

    constructor(id: String, pasajeroId: String, ubi_ini: String, ubi_desti: String, placa: String, aplicativo: String, nombreCondu:String,estados:HashMap<String,Boolean>,fecha: Long, estadoViaje:String) {
        this.id = id
        this.pasajeroId = pasajeroId
        this.ubi_ini = ubi_ini
        this.ubi_desti = ubi_desti
        this.placa = placa
        this.aplicativo = aplicativo
        this.nombreCondu = nombreCondu
        this.estados = estados
        this.fecha=fecha
        this.estadoViaje=estadoViaje
    }
}
