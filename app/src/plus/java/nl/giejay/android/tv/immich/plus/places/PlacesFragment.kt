package nl.giejay.android.tv.immich.plus.places

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import nl.giejay.android.tv.immich.R
import nl.giejay.android.tv.immich.api.ApiClient
import nl.giejay.android.tv.immich.api.model.Asset
import nl.giejay.android.tv.immich.api.util.ApiUtil
import nl.giejay.android.tv.immich.card.Card
import nl.giejay.android.tv.immich.plus.api.PlusApi
import nl.giejay.android.tv.immich.shared.fragment.VerticalCardGridFragment

/** One card per city, opening [PlaceAssetsFragment] for the selected city. */
class PlacesFragment : VerticalCardGridFragment<Asset>() {

    override fun sortItems(items: List<Asset>): List<Asset> {
        return items.sortedBy { it.exifInfo?.city?.lowercase() }
    }

    override suspend fun loadItems(
        apiClient: ApiClient,
        page: Int,
        pageCount: Int
    ): Either<String, List<Asset>> {
        if (page == startPage) {
            // the server returns all cities at once, there is no pagination
            return PlusApi.cities()
        }
        return Either.Right(emptyList())
    }

    override fun onItemSelected(card: Card, indexOf: Int) {
        // no use case yet
    }

    override fun onItemClicked(card: Card) {
        val asset = assets.firstOrNull { it.id == card.id } ?: return
        val args = Bundle().apply {
            putString(PlaceAssetsFragment.ARG_CITY, asset.exifInfo?.city)
            putString(PlaceAssetsFragment.ARG_COUNTRY, asset.exifInfo?.country)
        }
        findNavController().navigate(R.id.plusPlaceAssetsFragment, args)
    }

    override fun getBackgroundPicture(it: Asset): String? {
        return ApiUtil.getThumbnailUrl(it.id, "preview")
    }

    override fun createCard(a: Asset): Card {
        return Card(
            a.exifInfo?.city ?: "",
            a.exifInfo?.country ?: "",
            a.id,
            ApiUtil.getThumbnailUrl(a.id, "thumbnail"),
            ApiUtil.getThumbnailUrl(a.id, "preview")
        )
    }
}
