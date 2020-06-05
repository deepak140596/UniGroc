package com.avvnapps.unigroc.ui.generate_cart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.avvnapps.unigroc.models.CartEntity
import com.avvnapps.unigroc.ui.Activity.IndividualProduct
import com.avvnapps.unigroc.viewmodel.CartViewModel
import com.avvnapps.unigroc.viewmodel.FirestoreViewModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_search_item.*


class SearchItemActivity : AppCompatActivity() {

    val TAG = "SEARCH_ITEM_ACTIVITY"
    lateinit var firestoreViewModel: FirestoreViewModel
    lateinit var cartItemViewModel: CartViewModel
    lateinit var availableCartItems: List<CartEntity>
    lateinit var filteredCartItems: List<CartEntity>
    lateinit var savedCartItems: List<CartEntity>
    lateinit var cartItemAdapter: CartItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.avvnapps.unigroc.R.layout.activity_search_item)

        activity_search_progress_bar.visibility = View.VISIBLE
        activity_search_item_rv.layoutManager = LinearLayoutManager(this)
        activity_search_item_rv.addItemDecoration(
            DividerItemDecoration(
                activity_search_item_rv.context, DividerItemDecoration.VERTICAL
            )
        )
        // get cart items from local database
        cartItemViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        /*cartItemViewModel.cartList.observe(this, Observer {
            savedCartItems = it!!
            if(cartItemAdapter != null){
                runOnUiThread {
                    cartItemAdapter.notifyDataSetChanged()
                }

            }

        })*/

        // get available products from firebase
        firestoreViewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)
        firestoreViewModel.getAvailableCartItems().observe(this, Observer {
            availableCartItems = it!!
            setSearchBar()
            activity_search_progress_bar.visibility = View.GONE

        })


    }


    private fun setSearchBar() {
        activity_search_floating_search_view.setOnQueryChangeListener { oldQuery, newQuery ->

            if (oldQuery != "" && newQuery == "")
                activity_search_floating_search_view.clearSuggestions()
            else {
                activity_search_floating_search_view.showProgress()
                // search action
                filteredCartItems = filterCartItems(availableCartItems, newQuery)
                activity_search_floating_search_view.swapSuggestions(filteredCartItems)
                activity_search_floating_search_view.hideProgress()
                updateRecylerView(filteredCartItems)

            }
        }

        activity_search_floating_search_view.setOnSearchListener(object :
            FloatingSearchView.OnSearchListener {
            override fun onSuggestionClicked(searchSuggestion: SearchSuggestion?) {

                val cartItem = searchSuggestion as CartEntity
                Log.i(TAG, "Item Clicked: ${cartItem.body}")
                filteredCartItems = cartItem.name?.let { filterCartItems(availableCartItems, it) }!!
                updateRecylerView(filteredCartItems)
                activity_search_floating_search_view.clearSearchFocus()
            }

            override fun onSearchAction(currentQuery: String?) {
                activity_search_floating_search_view.showProgress()
                // search action
                filteredCartItems = filterCartItems(availableCartItems, currentQuery!!)
                activity_search_floating_search_view.swapSuggestions(filteredCartItems)
                activity_search_floating_search_view.hideProgress()
                updateRecylerView(filteredCartItems)
            }
        })


        activity_search_floating_search_view.setOnBindSuggestionCallback { suggestionView, leftIcon, textView, item, itemPosition ->
            val cartItem = item as CartEntity
            textView!!.text = cartItem.body
            leftIcon.scaleType = ImageView.ScaleType.CENTER_CROP
            if (cartItem.photoUrl != null)
                Glide.with(this@SearchItemActivity).load(cartItem.photoUrl).into(leftIcon!!)

        }
    }

    fun filterCartItems(cartList: List<CartEntity>, searchQuery: String): List<CartEntity> {


        val filteredValues = ArrayList<CartEntity>(cartList)
        for (cartItem in cartList) {
            val searchText = "${cartItem.name}  ${cartItem.category}  ${cartItem.clubbedCategory}"
            if (!(searchText.toLowerCase()).contains(searchQuery.toLowerCase()))
                filteredValues.remove(cartItem)

        }

        return filteredValues

    }

    fun updateRecylerView(cartList: List<CartEntity>) {
        cartItemAdapter = CartItemAdapter(
            this,
            cartList,
            cartItemViewModel
        )
        activity_search_item_rv.adapter = cartItemAdapter

        cartItemAdapter.setOnItemClickListener(object :
            CartItemAdapter.ClickListener {
            override fun onClick(pos: Int, aView: View) {
                val cartItem: CartEntity = cartItemAdapter.getItem(pos) as CartEntity
                val intent = Intent(this@SearchItemActivity, IndividualProduct::class.java)
                intent.putExtra("product", cartItem)
                startActivity(intent)

            }
        })
    }
}
