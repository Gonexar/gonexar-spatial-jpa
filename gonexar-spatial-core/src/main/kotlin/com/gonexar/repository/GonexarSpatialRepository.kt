package com.gonexar.repository

import com.gonexar.spatial.SpatialContext

interface GonexarSpatialRepository<T : Any> {
    val ctx: SpatialContext<T>
}