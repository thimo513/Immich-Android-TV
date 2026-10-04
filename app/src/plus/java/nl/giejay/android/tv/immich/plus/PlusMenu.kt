package nl.giejay.android.tv.immich.plus

import nl.giejay.android.tv.immich.ImmichApplication
import nl.giejay.android.tv.immich.R
import nl.giejay.android.tv.immich.home.Header
import nl.giejay.android.tv.immich.plus.favorites.FavoritesFragment
import nl.giejay.android.tv.immich.plus.places.PlacesFragment
import nl.giejay.android.tv.immich.plus.search.SmartSearchFragment

/**
 * Menu entries added by Immich TV Plus. HomeFragment spreads them into its HEADERS list with a
 * single line, so new pages are added here instead of in the upstream file.
 */
object PlusMenu {
    fun headers(): Array<Header> {
        val context = ImmichApplication.appContext!!
        return arrayOf(
            Header(context.getString(R.string.plus_search)) { SmartSearchFragment() },
            Header(context.getString(R.string.plus_favorites)) { FavoritesFragment() },
            Header(context.getString(R.string.plus_places)) { PlacesFragment() },
        )
    }
}
