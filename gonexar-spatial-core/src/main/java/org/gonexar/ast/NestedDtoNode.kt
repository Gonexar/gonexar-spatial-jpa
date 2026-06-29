package org.gonexar.ast

class NestedDtoNode(
    val dtoClass: Class<*>,
    val args: MutableList<SelectNode> = mutableListOf()
) : SelectNode