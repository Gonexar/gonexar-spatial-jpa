package org.gonexar.repository

import org.gonexar.spatial.SpatialContext

interface SpatialRepository<T : Any> {
    val ctx: SpatialContext
}