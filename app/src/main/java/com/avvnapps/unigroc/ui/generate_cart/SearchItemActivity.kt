package com.avvnapps.unigroc.ui.generate_cart

import android.content.Intent
import android.graphics.Color
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
import com.avvnapps.unigroc.R
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

        setupClickListeners()

    }

    private fun setupClickListeners() {

        cant_find_item_btn.setOnClickListener {
            if (cant_find_item_btn?.isSelected == true) {
                cant_find_item_btn?.isSelected = false
                cant_find_item_btn.setBackgroundResource(R.drawable.rectangle_strock)
                cant_find_item_btn.setTextColor(Color.parseColor("#455A64"))
                submit_problem_edit_text.visibility = View.GONE
                submit_problem_button.isClickable = false


            } else {
                cant_find_item_btn?.isSelected = true
                cant_find_item_btn.setBackgroundResource(R.drawable.red_btn_bg)
                cant_find_item_btn.setTextColor(Color.parseColor("#E57373"))
                submit_problem_edit_text.visibility = View.VISIBLE
                submit_problem_edit_text.hint = "Which Item could you not find?"
                submit_problem_button.isClickable = true


                prices_high_btn.setBackgroundResource(R.drawable.rectangle_strock)
                prices_high_btn.setTextColor(Color.parseColor("#455A64"))
                prices_high_btn.isSelected = false

                too_less_info_btn.setBackgroundResource(R.drawable.rectangle_strock)
                too_less_info_btn.setTextColor(Color.parseColor("#455A64"))
                too_less_info_btn.isSelected = false

                others_btn.setBackgroundResource(R.drawable.rectangle_strock)
                others_btn.setTextColor(Color.parseColor("#455A64"))
                others_btn.isSelected = false
            }

        }

        prices_high_btn.setOnClickListener {
            if (prices_high_btn?.isSelected == true) {
                prices_high_btn?.isSelected = false
                prices_high_btn.setBackgroundResource(R.drawable.rectangle_strock)
                prices_high_btn.setTextColor(Color.parseColor("#455A64"))
                submit_problem_edit_text.visibility = View.GONE
                submit_problem_button.isClickable = false


            } else {
                prices_high_btn?.isSelected = true
                prices_high_btn.setBackgroundResource(R.drawable.red_btn_bg)
                prices_high_btn.setTextColor(Color.parseColor("#E57373"))
                submit_problem_edit_text.visibility = View.VISIBLE
                submit_problem_edit_text.hint = "Which items are priced high?"
                submit_problem_button.isClickable = true


                cant_find_item_btn.setBackgroundResource(R.drawable.rectangle_strock)
                cant_find_item_btn.setTextColor(Color.parseColor("#455A64"))
                cant_find_item_btn.isSelected = false

                too_less_info_btn.setBackgroundResource(R.drawable.rectangle_strock)
                too_less_info_btn.setTextColor(Color.parseColor("#455A64"))
                too_less_info_btn.isSelected = false

                others_btn.setBackgroundResource(R.drawable.rectangle_strock)
                others_btn.setTextColor(Color.parseColor("#455A64"))
                others_btn.isSelected = false

            }
        }

        too_less_info_btn.setOnClickListener {
            if (too_less_info_btn?.isSelected == true) {
                too_less_info_btn?.isSelected = false
                too_less_info_btn.setBackgroundResource(R.drawable.rectangle_strock)
                too_less_info_btn.setTextColor(Color.parseColor("#455A64"))
                submit_problem_edit_text.visibility = View.GONE
                submit_problem_button.isClickable = false


            } else {
                too_less_info_btn?.isSelected = true
                too_less_info_btn.setBackgroundResource(R.drawable.red_btn_bg)
                too_less_info_btn.setTextColor(Color.parseColor("#E57373"))
                submit_problem_edit_text.visibility = View.VISIBLE
                submit_problem_edit_text.hint = "Which items information is missing?"
                submit_problem_button.isClickable = true


                cant_find_item_btn.setBackgroundResource(R.drawable.rectangle_strock)
                cant_find_item_btn.setTextColor(Color.parseColor("#455A64"))
                cant_find_item_btn.isSelected = false

                prices_high_btn.setBackgroundResource(R.drawable.rectangle_strock)
                prices_high_btn.setTextColor(Color.parseColor("#455A64"))
                prices_high_btn.isSelected = false

                others_btn.setBackgroundResource(R.drawable.rectangle_strock)
                others_btn.setTextColor(Color.parseColor("#455A64"))
                others_btn.isSelected = false

            }
        }

        others_btn.setOnClickListener {
            if (others_btn?.isSelected == true) {
                others_btn?.isSelected = false
                others_btn.setBackgroundResource(R.drawable.rectangle_strock)
                others_btn.setTextColor(Color.parseColor("#455A64"))
                submit_problem_edit_text.visibility = View.GONE
                submit_problem_button.isClickable = false

            } else {
                others_btn?.isSelected = true
                others_btn.setBackgroundResource(R.drawable.red_btn_bg)
                others_btn.setTextColor(Color.parseColor("#E57373"))
                submit_problem_edit_text.visibility = View.VISIBLE
                submit_problem_edit_text.hint = "Please describe the issue"
                submit_problem_button.isClickable = true


                cant_find_item_btn.setBackgroundResource(R.drawable.rectangle_strock)
                cant_find_item_btn.setTextColor(Color.parseColor("#455A64"))
                cant_find_item_btn.isSelected = false

                prices_high_btn.setBackgroundResource(R.drawable.rectangle_strock)
                prices_high_btn.setTextColor(Color.parseColor("#455A64"))
                prices_high_btn.isSelected = false

                too_less_info_btn.setBackgroundResource(R.drawable.rectangle_strock)
                too_less_info_btn.setTextColor(Color.parseColor("#455A64"))
                too_less_info_btn.isSelected = false

            }
        }
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
