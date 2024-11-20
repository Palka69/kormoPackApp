package com.example.kormopack.navFragments

import DatabaseHelper
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kormopack.PREF_PIB
import com.example.kormopack.R
import com.example.kormopack.activityCallbackInterfaces.DrawerCallback
import com.example.kormopack.activityCallbackInterfaces.ToolbarCallback
import com.example.kormopack.databinding.FragmentSpecsBinding
import com.example.kormopack.brandRecycleClasses.FeedBrand
import com.example.kormopack.brandRecycleClasses.FeedRecycleAdapter
import com.example.kormopack.feedRecycleClasses.FeedSpecs
import java.io.IOException

class SpecsFragment : Fragment() {
    private var _binding: FragmentSpecsBinding? = null
    private val binding get() = _binding!!

    private var drawerCallback: DrawerCallback? = null
    private var toolbarCallback: ToolbarCallback? = null

    lateinit var myAdapter: FeedRecycleAdapter

    private lateinit var dbHelper: DatabaseHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DrawerCallback) {
            drawerCallback = context
        } else {
            throw RuntimeException("$context must implement DrawerCallback")
        }
        if (context is ToolbarCallback) {
            toolbarCallback = context
        } else {
            throw RuntimeException("$context must implement ToolbarCallback")
        }
    }

    override fun onDetach() {
        super.onDetach()
        drawerCallback = null
        toolbarCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpecsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        Log.d("dbbb", dbHelper.toString())

        drawerCallback?.unlockDrawer()
        toolbarCallback?.changeToolbarColor(0)
        toolbarCallback?.showToolbar()

        val user = drawerCallback?.getSharPref()?.getString(PREF_PIB, "NULL")
        drawerCallback?.renameDrawerUser(user ?: "NULL")

        saveBrandsToDatabase()

        val listOfFeeds = genBrandList()

        val onBrandClickListener: FeedRecycleAdapter.OnBrandClickListener =
            object : FeedRecycleAdapter.OnBrandClickListener {
                override fun onBrandClick(text: String) {
                    val bundle = Bundle()
                    bundle.putString("brand", text)
                    findNavController().navigate(R.id.action_nav_specs_to_feedListFragment, bundle)
                }
            }

        myAdapter = FeedRecycleAdapter(requireContext(), listOfFeeds, onBrandClickListener)
        binding.recycle.adapter = myAdapter
    }

    private fun genBrandList(): List<FeedBrand> {
        val list = mutableListOf<FeedBrand>()

        try {
            dbHelper.openDatabase()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var sqlrequest = "feed_brands"

        val cursor: Cursor = dbHelper.readableDatabase.rawQuery("SELECT * FROM $sqlrequest", null)
        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(1)
                val brandLogo: Drawable = when(cursor.getInt(2)) {
                    1 -> ResourcesCompat.getDrawable(resources, R.drawable.c4p_logo, context?.theme)!!
                    2 -> ResourcesCompat.getDrawable(resources, R.drawable.myau_logo, context?.theme)!!
                    3 -> ResourcesCompat.getDrawable(resources, R.drawable.opti_meal_white_logo, context?.theme)!!
                    4 -> ResourcesCompat.getDrawable(resources, R.drawable.smart_choice_white_logo, context?.theme)!!
                    5 -> ResourcesCompat.getDrawable(resources, R.drawable.sjobogarden_logo, context?.theme)!!

                    else -> {ResourcesCompat.getDrawable(resources, R.drawable.kormo_pets_paw, context?.theme)!!}
                }
                val cardBackColor = cursor.getInt(3)
                val textColor = cursor.getInt(4)

                val feedBrand = FeedBrand(name, brandLogo, cardBackColor, textColor)
                Log.d("MyFeed", feedBrand.toString())

                list.add(feedBrand)
            } while (cursor.moveToNext())
        }
        cursor.close()
        dbHelper.close()

        Log.d("ListOfBrands2", "${list}")

        return list
    }

    private fun saveBrandsToDatabase() {
        try {
            dbHelper.openDatabase()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var count: Int = 0
        val countCursor: Cursor = dbHelper.readableDatabase.rawQuery("SELECT COUNT(*) FROM feed_brands", null)
        if (countCursor.moveToFirst()) {
            count = countCursor.getInt(0)
            Log.d("DatabaseCheck", "Number of rows in feed_brands: $count")
        }
        countCursor.close()

        var brands = mutableListOf<FeedBrand>()
        Log.d("CountB", count.toString())
        if (count < 5) {
            brands = mutableListOf(
                FeedBrand(
                    resources.getString(R.string.brand_club_4_paws),
                    ResourcesCompat.getDrawable(resources, R.drawable.c4p_logo, context?.theme)!!,
                    resources.getColor(R.color.kormoTech_dark_orange, context?.theme),
                    resources.getColor(R.color.white, context?.theme),
                ), FeedBrand(
                    resources.getString(R.string.brand_myau),
                    ResourcesCompat.getDrawable(resources, R.drawable.myau_logo, context?.theme)!!,
                    resources.getColor(R.color.myau_red, context?.theme),
                    resources.getColor(R.color.white, context?.theme),
                ), FeedBrand(
                    resources.getString(R.string.brand_opti_meal),
                    ResourcesCompat.getDrawable(resources, R.drawable.opti_meal_white_logo, context?.theme)!!,
                    resources.getColor(R.color.opti_green, context?.theme),
                    resources.getColor(R.color.white, context?.theme),
                ), FeedBrand(
                    resources.getString(R.string.brand_smart_choice),
                    ResourcesCompat.getDrawable(resources, R.drawable.smart_choice_white_logo, context?.theme)!!,
                    resources.getColor(R.color.smart_choice_blue, context?.theme),
                    resources.getColor(R.color.white, context?.theme),
                ), FeedBrand(
                    resources.getString(R.string.brand_sjobogarden),
                    ResourcesCompat.getDrawable(resources, R.drawable.sjobogarden_logo, context?.theme)!!,
                    resources.getColor(R.color.white, context?.theme),
                    resources.getColor(R.color.sjobogarden_blue, context?.theme),
                ), )
        }
        Log.d("ListOfBrands1", "${brands}")

        brands.forEach { brand ->
            dbHelper.insertFeedBrand(brand)
        }

        dbHelper.close()
    }
}