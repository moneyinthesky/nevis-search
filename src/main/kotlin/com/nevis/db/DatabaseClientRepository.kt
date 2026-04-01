package com.nevis.db

import com.fasterxml.jackson.module.kotlin.readValue
import com.nevis.config.Json
import com.nevis.model.ClientResponse
import com.nevis.model.ClientResult
import com.nevis.model.CreateClientRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

private val mapper = Json.mapper

internal object ClientsTable : Table("clients") {
    val id = uuid("id").autoGenerate()
    val firstName = varchar("first_name", 255)
    val lastName = varchar("last_name", 255)
    val email = varchar("email", 255)
    val description = text("description").nullable()
    val socialLinks = jsonb<List<String>>(
        "social_links",
        { mapper.writeValueAsString(it) },
        { mapper.readValue(it) }
    )
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}

class DatabaseClientRepository : ClientRepository {

    private val table = ClientsTable

    override fun create(request: CreateClientRequest): ClientResponse = transaction {
        table.insertReturning {
            it[table.firstName] = request.firstName
            it[table.lastName] = request.lastName
            it[table.email] = request.email
            it[table.description] = request.description
            it[table.socialLinks] = request.socialLinks
        }.single().let(::toResponse)
    }

    override fun existsById(clientId: UUID): Boolean = transaction {
        table.selectAll().where { table.id eq clientId }.count() > 0
    }

    override fun search(query: String): List<ClientResult> = transaction {
        val escaped = query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
        val pattern = "%$escaped%"
        table.selectAll().where {
            (table.firstName.lowerCase() like pattern) or
            (table.lastName.lowerCase() like pattern) or
            (table.email.lowerCase() like pattern) or
            (table.description.lowerCase() like pattern)
        }.map(::toResult)
    }

    private fun toResponse(row: ResultRow) = ClientResponse(
        id = row[table.id],
        firstName = row[table.firstName],
        lastName = row[table.lastName],
        email = row[table.email],
        description = row[table.description],
        socialLinks = row[table.socialLinks],
        createdAt = row[table.createdAt],
        updatedAt = row[table.updatedAt],
    )

    private fun toResult(row: ResultRow) = ClientResult(
        id = row[table.id],
        firstName = row[table.firstName],
        lastName = row[table.lastName],
        email = row[table.email],
        description = row[table.description],
        socialLinks = row[table.socialLinks],
    )
}
