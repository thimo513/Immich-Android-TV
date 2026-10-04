package nl.giejay.android.tv.immich.plus.places

import android.os.Bundle
import arrow.core.Either
import nl.giejay.android.tv.immich.api.ApiClient
import nl.giejay.android.tv.immich.api.model.Asset
import nl.giejay.android.tv.immich.assets.GenericAssetFragment
import nl.giejay.android.tv.immich.plus.api.PlusApi

/** All assets taken in one city, opened from [PlacesFragment]. */
class PlaceAssetsFragment : GenericAssetFragment() {
    private lateinit var city: String
    private var country: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        city = requireArguments().getString(ARG_CITY).orEmpty()
        country = requireArguments().getString(ARG_COUNTRY)
        super.onCreate(savedInstanceState)
    }

    override suspend fun loadItems(
        apiClient: ApiClient,
        page: Int,
        pageCount: Int
    ): Either<String, List<Asset>> {
        return PlusApi.assetsInCity(city, country, page, pageCount, currentFilter)
    }

    override fun setTitle(response: List<Asset>) {
        title = city
    }

    companion object {
        const val ARG_CITY = "city"
        const val ARG_COUNTRY = "country"
    }
}
