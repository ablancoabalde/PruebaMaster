package com.alberto.pruebamaster

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.alberto.pruebamaster.dummy.DummyContent
import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import kotlinx.android.synthetic.main.item_list.*

/**
 * Añadido a más
 */
import android.util.Log
import com.alberto.pruebamaster.dummy.DummyItem
import kotlinx.android.synthetic.main.item_list_content.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import java.net.URL

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ItemDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ItemListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (item_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
            // realizamos la conexión rest http

        }
        peticionPosts()
    }

    private fun peticionPosts() {
        // corutina en un hilo diferente
        doAsync {
            // capturamos los errores de la peticion
            try {
                // peticion a un servidor rest que devuelve un json generico
                val respuesta = URL("http://18.223.212.231/WordPress/?rest_route=/wp/v2/posts").readText()
                // parsing data
                // sabemos que recibimos un array de objetos JSON
                val miJSONArray = JSONArray(respuesta)
                // recorremos el Array

                DummyContent.ITEMS.clear()
                DummyContent.ITEM_MAP.clear()
                for (jsonIndex in 0 .. (miJSONArray.length()-1)) {
                    val idpost = miJSONArray.getJSONObject(jsonIndex).getString("id")
                    val titulo = miJSONArray.getJSONObject(jsonIndex).getJSONObject("title").getString("rendered")
                    val resumen = miJSONArray.getJSONObject(jsonIndex).getJSONObject("excerpt").getString("rendered")
                    val quitarTexto= resumen.substring(3,resumen.length-5)
                    // asignamos los valores en el constructor de la data class 'DummyItem'
                    // añadimos al array list
                    DummyContent.addItem(DummyItem(idpost, titulo, quitarTexto))
                }
                // una vez que cargamos los posts, recargamos las vistas en el layout
                uiThread {
                    setupRecyclerView(item_list)
                }
            } catch (e: Exception) { // Si algo va mal lo capturamos
                Log.d(DummyContent.LOGTAG, "Algo va mal: $e")
            }

        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, twoPane)
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: ItemListActivity,
        private val values: List<DummyItem>,
        private val twoPane: Boolean
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as DummyItem
                if (twoPane) {
                    val fragment = ItemDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(ItemDetailFragment.ARG_ITEM_ID, item.id)
                        }
                    }
                    parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.ARG_ITEM_ID, item.id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = item.id
            holder.contentView.text = item.title

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }
    }
}
