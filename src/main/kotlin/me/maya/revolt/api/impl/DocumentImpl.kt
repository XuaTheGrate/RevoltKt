package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.Document

class DocumentImpl internal constructor(
    data: JsonObject,
    val state: State
): Document {
    override val tag: String = data["tag"].string
    override val id: String = data["_id"].string
    override val size: Int = data["size"].int
    override val filename: String = data["filename"].string
    override val contentType: String = data["content_type"].string

    override fun getUrl(): String {
        return "${state.cdn}/$tag/$filename"
    }
}