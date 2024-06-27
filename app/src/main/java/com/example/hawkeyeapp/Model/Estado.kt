package com.example.hawkeyeapp.Model

import java.sql.Timestamp

class Estado {
    var id: String=""
    var viajeid: String = ""
    var estadoCambio: String = ""
    var fecha: Timestamp = Timestamp(System.currentTimeMillis())

    constructor(id:String ,viajeid: String, estadoCambio: String, fecha: Timestamp): this() {
        this.id =id
        this.viajeid = viajeid
        this.estadoCambio = estadoCambio
        this.fecha = fecha
    }

    constructor()
}
