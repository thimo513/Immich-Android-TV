package nl.giejay.android.tv.immich.plus

import nl.giejay.android.tv.immich.api.model.Asset
import nl.giejay.android.tv.immich.api.util.ApiUtil
import nl.giejay.android.tv.immich.card.Card
import java.text.SimpleDateFormat
import java.util.Locale

/** Card for the Plus pages: the date taken (dd.MM.yyyy) as caption instead of the file name. */
fun Asset.toDateCard(): Card {
    val date = exifInfo?.dateTimeOriginal ?: fileCreatedAt ?: fileModifiedAt
    val caption = date?.let { SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(it) } ?: ""
    return Card(
        caption,
        "",
        id,
        ApiUtil.getThumbnailUrl(id, "thumbnail"),
        ApiUtil.getThumbnailUrl(id, "preview")
    )
}
