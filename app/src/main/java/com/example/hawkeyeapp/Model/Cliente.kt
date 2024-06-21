package com.example.hawkeyeapp.Model

class Cliente() {
    var id: String = ""
    var numero: String = ""
    var nombre: String = ""
    var correo: String = ""

    constructor(id: String, numero: String, nombre: String, correo: String) : this() {
        this.id = id
        this.numero = numero
        this.nombre = nombre
        this.correo = correo
    }
}
