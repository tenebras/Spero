package com.tenebras.spero

import java.sql.Connection
import java.sql.DriverManager

open class DbConnectionManager(val connectionString: String) {

    var connection: Connection? = null

    fun connection(): Connection {
        if(connection == null || connection!!.isClosed) {
            connection = DriverManager.getConnection(connectionString)
        }

        return connection!!
    }
}