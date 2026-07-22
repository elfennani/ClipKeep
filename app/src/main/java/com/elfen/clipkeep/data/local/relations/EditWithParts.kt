package com.elfen.clipkeep.data.local.relations

import androidx.room3.Embedded
import androidx.room3.Relation
import com.elfen.clipkeep.data.local.model.EditingClipEntity
import com.elfen.clipkeep.data.local.model.EditingClipPartEntity
import com.elfen.clipkeep.data.local.model.asAppModel

data class EditWithParts(
    @Embedded
    val edit: EditingClipEntity,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["editId"]
    )
    val parts: List<EditingClipPartEntity>
)

fun EditWithParts.asAppModel() = edit.asAppModel(parts.map { it.asAppModel() })