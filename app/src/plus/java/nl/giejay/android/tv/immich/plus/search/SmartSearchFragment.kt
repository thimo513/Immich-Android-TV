package nl.giejay.android.tv.immich.plus.search

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import arrow.core.Either
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import nl.giejay.android.tv.immich.R
import nl.giejay.android.tv.immich.api.ApiClient
import nl.giejay.android.tv.immich.api.model.Asset
import nl.giejay.android.tv.immich.assets.GenericAssetFragment
import nl.giejay.android.tv.immich.plus.api.PlusApi

/**
 * Smart search (CLIP) over the whole library, e.g. "mountains" or "sailboat".
 *
 * The query is entered through the search orb in the title, or by pressing right when the grid
 * is empty / at its right edge (the spot where other pages open their settings).
 */
class SmartSearchFragment : GenericAssetFragment() {
    private var dialogOpen = false

    private val voiceInput = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { search(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnSearchClickedListener { showSearchDialog() }
        updateTitle()
    }

    override suspend fun loadItems(
        apiClient: ApiClient,
        page: Int,
        pageCount: Int
    ): Either<String, List<Asset>> {
        val query = lastQuery ?: return Either.Right(emptyList())
        return PlusApi.smartSearch(query, page, pageCount, currentFilter)
    }

    // results are ordered by relevance, the user's photo sorting would scramble them
    override fun sortItems(items: List<Asset>): List<Asset> = items

    override fun setTitle(response: List<Asset>) {
        updateTitle()
    }

    override fun openPopUpMenu() {
        showSearchDialog()
    }

    private fun updateTitle() {
        title = lastQuery?.let { getString(R.string.plus_search_title_with_query, it) }
            ?: getString(R.string.plus_search_title_empty)
    }

    private fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty() || !isAdded) return
        lastQuery = trimmed
        ioScope.coroutineContext[Job]?.cancelChildren()
        clearState()
        allPagesLoaded = false
        progressBar?.visibility = View.VISIBLE
        updateTitle()
        fetchInitialItems()
    }

    private fun showSearchDialog() {
        if (dialogOpen || !isAdded) return
        val context = requireContext()
        val input = EditText(context).apply {
            setSingleLine()
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            hint = getString(R.string.plus_search_hint)
            setText(lastQuery.orEmpty())
            selectAll()
        }
        val padding = (24 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(context).apply {
            setPadding(padding, padding / 2, padding, 0)
            addView(input)
        }

        val voiceIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.plus_search_voice_prompt))

        val builder = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(R.string.plus_search_dialog_title)
            .setView(container)
            .setPositiveButton(R.string.plus_search_action) { _, _ -> search(input.text.toString()) }
            .setNegativeButton(android.R.string.cancel, null)
            .setOnDismissListener { dialogOpen = false }
        if (voiceIntent.resolveActivity(context.packageManager) != null) {
            builder.setNeutralButton(R.string.plus_search_voice) { _, _ -> voiceInput.launch(voiceIntent) }
        }
        val dialog = builder.create()
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                search(input.text.toString())
                dialog.dismiss()
                true
            } else {
                false
            }
        }
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialogOpen = true
        dialog.show()
        input.requestFocus()
    }

    companion object {
        // survives switching pages and returning from the photo slider
        private var lastQuery: String? = null
    }
}
