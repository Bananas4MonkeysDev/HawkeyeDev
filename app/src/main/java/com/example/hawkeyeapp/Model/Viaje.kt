package com.example.hawkeyeapp.Model

class Viaje {
    var id: String? = null
    var pasajeroId: String? = null
    var ubi_ini: String? = null
    var ubi_desti: String? = null
    var placa: String? = null
    var aplicativo: String? = null
    var nombreCondu:String? = null

    constructor()

    constructor(id: String, pasajeroId: String, ubi_ini: String, ubi_desti: String, placa: String, aplicativo: String, nombreCondu:String) {
        this.id = id
        this.pasajeroId = pasajeroId
        this.ubi_ini = ubi_ini
        this.ubi_desti = ubi_desti
        this.placa = placa
        this.aplicativo = aplicativo
        this.nombreCondu = nombreCondu
    }
}
