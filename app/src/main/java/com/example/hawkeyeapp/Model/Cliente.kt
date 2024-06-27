package com.example.hawkeyeapp.Model

class Cliente {
    var id: String? = null
    var numero: String? = null
    var nombre: String? = null
    var correo: String? = null
    var latitud: Double? = null
    var longitud: Double? = null
    var viajes: HashMap<String, Boolean> = hashMapOf() // Usa un HashMap para almacenar referencias a viajes

    constructor()

    constructor(id: String, numero: String, nombre: String, correo: String) {
        this.id = id
        this.numero = numero
        this.nombre = nombre
        this.correo = correo
    }
}
