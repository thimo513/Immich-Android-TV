package nl.giejay.android.tv.immich.plus.search

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognizerIntent
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import arrow.core.Either
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import nl.giejay.android.tv.immich.R
import nl.giejay.android.tv.immich.api.ApiClient
import nl.giejay.android.tv.immich.api.model.Asset
import nl.giejay.android.tv.immich.assets.GenericAssetFragment
import nl.giejay.android.tv.immich.plus.api.PlusApi
import nl.giejay.android.tv.immich.shared.viewmodel.KeyEventsViewModel

/**
 * Smart search (CLIP) over the whole library, e.g. "mountains" or "sailboat".
 *
 * The query is entered in a dialog: OK while the page is empty, or right at the grid's right edge
 * (the spot where other pages open their settings).
 */
class SmartSearchFragment : GenericAssetFragment() {
    private var dialogOpen = false

    private val voiceInput = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { search(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateTitle()
        openDialogOnOkWhileEmpty()
    }

    /**
     * On an empty search page there is nothing to focus, so OK on the remote (also the press that
     * enters the page from the menu) opens the search dialog. The key state is replayed to new
     * collectors, hence the age check: only a fresh press counts.
     */
    private fun openDialogOnOkWhileEmpty() {
        val keyEvents = ViewModelProvider(requireActivity())[KeyEventsViewModel::class.java]
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                keyEvents.state.collect { event ->
                    val isOk = event?.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        event?.keyCode == KeyEvent.KEYCODE_ENTER ||
                        event?.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
                    val fresh = event != null && SystemClock.uptimeMillis() - event.eventTime < FRESH_KEY_MS
                    if (isOk && fresh && lastQuery == null && assets.isEmpty()) {
                        showSearchDialog()
                    }
                }
            }
        }
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
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val dialog = Dialog(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
        val input = EditText(context).apply {
            setSingleLine()
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            hint = getString(R.string.plus_search_hint)
            setText(lastQuery.orEmpty())
            setSelection(text.length)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(Color.WHITE)
            setHintTextColor(0x99FFFFFF.toInt())
        }
        fun submit() {
            dialog.dismiss()
            search(input.text.toString())
        }
        fun button(label: String, onClick: () -> Unit) = Button(context).apply {
            text = label
            isAllCaps = false
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setOnClickListener { onClick() }
        }

        val voiceIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.plus_search_voice_prompt))
        val buttons = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            if (voiceIntent.resolveActivity(context.packageManager) != null) {
                addView(button(getString(R.string.plus_search_voice)) {
                    dialog.dismiss()
                    voiceInput.launch(voiceIntent)
                })
            }
            addView(button(getString(android.R.string.cancel)) { dialog.dismiss() })
            addView(button(getString(R.string.plus_search_action)) { submit() })
        }
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(20), dp(24), dp(16))
            background = GradientDrawable().apply {
                setColor(0xF2202124.toInt())
                cornerRadius = dp(12).toFloat()
            }
            addView(TextView(context).apply {
                text = getString(R.string.plus_search_dialog_title)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                setTextColor(Color.WHITE)
            })
            addView(input, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(12)
                bottomMargin = dp(8)
            })
            addView(buttons)
        }

        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }
        dialog.setContentView(content)
        dialog.setOnDismissListener { dialogOpen = false }
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // compact box in the upper half, the on-screen keyboard needs the lower part
            setLayout(minOf(dp(560), (resources.displayMetrics.widthPixels * 0.7f).toInt()), WindowManager.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
            attributes = attributes.apply { y = dp(48) }
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        }
        dialogOpen = true
        dialog.show()
        input.requestFocus()
    }

    companion object {
        // survives switching pages and returning from the photo slider
        private var lastQuery: String? = null
        private const val FRESH_KEY_MS = 500L
    }
}
