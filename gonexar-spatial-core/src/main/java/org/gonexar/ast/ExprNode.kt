package org.gonexar.ast

import jakarta.persistence.criteria.Expression

class ExprNode(val expr: Expression<*>) : SelectNode