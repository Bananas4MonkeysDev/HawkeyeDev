package com.example.hawkeyeapp.Model

class Viaje {
    var id: String = ""
    var ubi_ini: String = ""
    var ubi_desti: String = ""
    lateinit var estadovia: MutableList<Estado>

    constructor(id: String, ubi_ini: String, ubi_desti: String, estadovia: MutableList<Estado>) : this() {
        this.id = id
        this.ubi_ini = ubi_ini
        this.ubi_desti = ubi_desti
        this.estadovia = estadovia
    }

    constructor()
}