package org.gonexar.expression

import jakarta.persistence.criteria.Expression
import org.gonexar.spatial.SpatialDslContext

/**
 * Representa uma expressão numérica em consultas espaciais.
 *
 * O NumericExpr é um wrapper tipado para qualquer expressão SQL que resulte
 * em um valor numérico (INT, DOUBLE, FLOAT, BIGINT, etc.). Ele funciona de forma
 * semelhante ao SpatialExpr, mas voltado para operações matemáticas, estatísticas
 * e funções PostGIS que retornam números.
 *
 * Finalidade:
 * -----------
 * O objetivo deste wrapper é permitir que operações numéricas possam ser
 * manipuladas, combinadas e projetadas de forma lazy (somente no SQL), sem que
 * nenhum cálculo seja executado no Kotlin. Assim, todo o processamento permanece
 * no banco de dados, garantindo performance e consistência.
 *
 * Use NumericExpr nas seguintes situações:
 *  - ST_Length, ST_Area, ST_Perimeter ...
 *  - ST_Value (retorna Double)
 *  - generate_series (retorna Int)
 *  - cálculos matemáticos: +, -, *, /
 *  - FLOOR(), CEIL(), POWER(), etc.
 *  - projeção de valores numéricos em DTOs
 *
 * Importante:
 * -----------
 * NumericExpr não contém o valor numérico em si — ele representa *a expressão SQL*
 * que produzirá o valor quando a query for executada. Portanto, operações como
 * toInt(), toDouble(), etc., NÃO são realizadas em tempo de execução do Kotlin,
 * e sim no banco de dados.
 *
 * Propriedades:
 * --------------
 * @property name  Nome (alias lógico) associado a esta expressão.
 *                 - Não altera o SQL gerado pela Expression<T>, mas é usado
 *                   internamente pelo DSL para registrar, identificar e reutilizar
 *                   expressões complexas.
 *                 - Útil para debug, logs e projeções.
 *
 * @property expr  A expressão JPA CriteriaBuilder subjacente.
 *                 - É essa expressão que será transformada em SQL.
 *                 - Sempre lazy — nunca avaliada no lado do cliente.
 *
 * Métodos:
 * --------
 *
 * alias(newName: String): NumericExpr<T>
 *     Cria uma nova instância de NumericExpr mantendo a mesma expressão SQL,
 *     mas usando um novo alias lógico.
 *
 *     Usos típicos:
 *       - quando a mesma expressão é reutilizada em várias partes do DSL
 *       - quando a mesma expressão aparece em projections diferentes
 *       - quando queremos que o SQL final seja mais legível
 *
 * Exemplo:
 * --------
 *     // Calcula o comprimento de uma rota
 *     val length: NumericExpr<Double> = route.stLengthGeography(ctx)
 *         .alias("route_length")
 *
 *     // Número de amostras
 *     val samples = NumericExpr(
 *         name = "num_samples",
 *         expr = cb.function("FLOOR", Int::class.java,
 *             cb.quot(length.expr, cb.literal(100.0))
 *         )
 *     )
 *
 * Integração com o DSL:
 * ----------------------
 * NumericExpr é essencial para implementar:
 *  - perfil altimétrico
 *  - NDVI along route
 *  - slope ahead real
 *  - interpolação de rota
 *  - amostragem
 *  - cálculos matemáticos em SQL
 *
 */
class NumericExpr<T: Number>(
    val name: String,
    val expr: Expression<*>
) {
    fun alias(newName: String): NumericExpr<T> =
        NumericExpr(newName, expr)
}
