package com.tenebras.spero.db

import java.sql.ResultSet
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.*

fun ResultSet.getUUID(columnLabel: String): UUID = UUID.fromString(getString(columnLabel))
fun ResultSet.getZonedDateTime(columnLabel: String): ZonedDateTime = getObject(columnLabel, OffsetDateTime::class.java).toZonedDateTime()
fun ResultSet.getOffsetDateTime(columnLabel: String): OffsetDateTime = getObject(columnLabel, OffsetDateTime::class.java)