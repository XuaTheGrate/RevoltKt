package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.Image

class ImageImpl internal constructor(
    data: JsonObject,
    val state: State
): Image {
    override val width: Int
    override val height: Int

    init {
        val meta = data["metadata"].jsonObject
        width = meta["width"].int
        height = meta["height"].int
    }

    override fun getUrl(maxSize: Int): String {
        return "${state.cdn}/$tag/$filename?max_side=$maxSize"
    }

    override fun getUrl(): String {
        return getUrl(1024)
    }

    override val tag: String = data["tag"].string
    override val id: String = data["_id"].string
    override val size: Int = data["size"].int
    override val filename: String = data["filename"].string
    override val contentType: String = data["content_type"].string

}