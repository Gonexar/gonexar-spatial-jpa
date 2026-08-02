package org.gonexar.repository

import org.gonexar.spatial.SpatialContext

interface GonexarSpatialRepository<T : Any> {
    val ctx: SpatialContext<T>
}