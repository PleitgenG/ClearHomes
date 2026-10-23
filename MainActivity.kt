package com.pleit.clearhome

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var favRow: RecyclerView
    private lateinit var allRow: RecyclerView
    private lateinit var favLabel: TextView
    private lateinit var allLabel: TextView
    private lateinit var focusedName: TextView
    private lateinit var favAdapter: AppAdapter
    private lateinit var allAdapter: AppAdapter

    private var allApps: List<AppItem> = emptyList()
    private var hasInitialFocus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getSharedPreferences("clear_home", Context.MODE_PRIVATE)

        favRow = findViewById(R.id.fav_row)
        allRow = findViewById(R.id.all_row)
        favLabel = findViewById(R.id.fav_label)
        allLabel = findViewById(R.id.all_label)
        focusedName = findViewById(R.id.focused_name)

        favAdapter = AppAdapter(192, ::showName, ::openApp, ::togglePin)
        allAdapter = AppAdapter(144, ::showName, ::openApp, ::togglePin)

        favRow.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        allRow.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        favRow.adapter = favAdapter
        allRow.adapter = allAdapter
    }

    override fun onResume() {
        super.onResume()
        // Reload on every resume so newly installed or removed apps show up.
        Thread {
            val apps = AppRepository.load(packageManager, packageName)
            runOnUiThread {
                allApps = apps
                render()
                if (!hasInitialFocus) {
                    hasInitialFocus = true
                    focusFirst()
                }
            }
        }.start()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Home button pressed while already on the home screen: jump back to the start.
        if (intent?.action == Intent.ACTION_MAIN) {
            favRow.scrollToPosition(0)
            allRow.scrollToPosition(0)
            focusFirst()
        }
    }

    // Back should never leave the launcher.
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        focusFirst()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            (currentFocus?.tag as? AppItem)?.let { togglePin(it) }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // ---- rendering -------------------------------------------------------------------------

    private fun render() {
        val pinned = pinnedList()
        val byPackage = allApps.associateBy { it.packageName }
        val favs = pinned.mapNotNull { byPackage[it] }
        val rest = allApps.filter { it.packageName !in pinned }

        favAdapter.items = favs
        allAdapter.items = rest

        val showFavs = favs.isNotEmpty()
        favRow.visibility = if (showFavs) View.VISIBLE else View.GONE
        favLabel.visibility = favRow.visibility

        val showAll = rest.isNotEmpty()
        allRow.visibility = if (showAll) View.VISIBLE else View.GONE
        allLabel.visibility = allRow.visibility
    }

    private fun focusFirst() {
        val row = if (favRow.visibility == View.VISIBLE) favRow else allRow
        row.postDelayed({ row.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus() }, 80)
    }

    private fun focusPackage(pkg: String) {
        val inFav = favAdapter.indexOf(pkg)
        val row = if (inFav >= 0) favRow else allRow
        val pos = if (inFav >= 0) inFav else allAdapter.indexOf(pkg)
        if (pos < 0) return
        row.scrollToPosition(pos)
        row.postDelayed({ row.findViewHolderForAdapterPosition(pos)?.itemView?.requestFocus() }, 80)
    }

    private fun showName(item: AppItem) {
        focusedName.text = item.label
    }

    // ---- actions ---------------------------------------------------------------------------

    private fun openApp(item: AppItem) {
        try {
            startActivity(item.launchIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Can't open ${item.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePin(item: AppItem) {
        val list = pinnedList().toMutableList()
        if (list.remove(item.packageName)) {
            toast("Removed from favorites")
        } else {
            list.add(item.packageName)
            toast("Pinned to favorites")
        }
        savePinned(list)
        render()
        focusPackage(item.packageName)
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // ---- favorites storage -----------------------------------------------------------------

    private fun pinnedList(): List<String> {
        val saved = prefs.getString("pinned", null)
        if (saved != null) return saved.split(",").filter { it.isNotBlank() }

        // First run: pin well-known apps that are actually installed.
        val installed = allApps.map { it.packageName }.toSet()
        if (installed.isEmpty()) return emptyList()
        val defaults = AppRepository.defaultFavorites.filter { it in installed }.take(6)
        savePinned(defaults)
        return defaults
    }

    private fun savePinned(list: List<String>) {
        prefs.edit().putString("pinned", list.joinToString(",")).apply()
    }
}
