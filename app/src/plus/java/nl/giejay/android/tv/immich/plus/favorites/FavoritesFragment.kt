package nl.giejay.android.tv.immich.plus.favorites

import arrow.core.Either
import nl.giejay.android.tv.immich.api.ApiClient
import nl.giejay.android.tv.immich.api.model.Asset
import nl.giejay.android.tv.immich.assets.GenericAssetFragment
import nl.giejay.android.tv.immich.card.Card
import nl.giejay.android.tv.immich.plus.api.PlusApi
import nl.giejay.android.tv.immich.plus.toDateCard

/** All assets marked as favorite (heart) in Immich. */
class FavoritesFragment : GenericAssetFragment() {

    override suspend fun loadItems(
        apiClient: ApiClient,
        page: Int,
        pageCount: Int
    ): Either<String, List<Asset>> {
        return PlusApi.favorites(page, pageCount, currentFilter)
    }

    override fun createCard(a: Asset): Card = a.toDateCard()
}
