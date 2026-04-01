package com.nevis.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.CustomOperator
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.FloatColumnType
import org.jetbrains.exposed.sql.QueryParameter
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

class VectorColumnType(private val dimensions: Int) : ColumnType<FloatArray>() {

    override fun sqlType(): String = "vector($dimensions)"

    override fun valueFromDB(value: Any): FloatArray = when (value) {
        is PGobject -> (value.value ?: error("PGobject value was null for vector column"))
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.toFloat() }
            .toFloatArray()
        else -> error("Unexpected value type for vector column: ${value::class}")
    }

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        stmt[index] = when (value) {
            is PGobject -> value
            else -> PGobject().apply {
                type = "vector"
                this.value = when (value) {
                    is FloatArray -> value.joinToString(",", "[", "]")
                    null -> null
                    else -> error("Unexpected parameter type for vector column: ${value::class}")
                }
            }
        }
    }

    override fun notNullValueToDB(value: FloatArray): Any = PGobject().apply {
        type = "vector"
        this.value = value.joinToString(",", "[", "]")
    }
}

fun Table.vector(name: String, dimensions: Int): Column<FloatArray> =
    registerColumn(name, VectorColumnType(dimensions))

/** pgvector cosine distance operator: `column <=> vector` — returns a float in [0, 2]. */
fun cosineDistance(column: Column<FloatArray?>, embedding: FloatArray): CustomOperator<Float> =
    CustomOperator(
        operatorName = "<=>",
        columnType = FloatColumnType(),
        expr1 = column,
        expr2 = QueryParameter(embedding, VectorColumnType(embedding.size)),
    )
