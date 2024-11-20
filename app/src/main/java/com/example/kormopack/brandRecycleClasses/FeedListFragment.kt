package com.example.kormopack.brandRecycleClasses

import DatabaseHelper
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kormopack.activityCallbackInterfaces.ToolbarCallback
import com.example.kormopack.databinding.FragmentFeedListBinding
import com.example.kormopack.feedRecycleClasses.FeedSpecRecycleAdapter
import com.example.kormopack.feedRecycleClasses.FeedSpecs
import java.io.IOException

class FeedListFragment : Fragment() {

    private var _binding: FragmentFeedListBinding? = null
    private val binding get() = _binding!!
    private var toolbarCallback: ToolbarCallback? = null
    lateinit var myAdapter: FeedSpecRecycleAdapter
    private lateinit var dbHelper: DatabaseHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ToolbarCallback) {
            toolbarCallback = context
        } else {
            throw RuntimeException("$context must implement ToolbarCallback")
        }
    }

    override fun onDetach() {
        super.onDetach()
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
    ): View? {
        _binding = FragmentFeedListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val brand = arguments?.getString("brand")
        toolbarCallback?.renameToolbar(brand!!)

        val feedSpecList = brand?.let { getFeedSpecListFromSQL(it) }

        myAdapter = FeedSpecRecycleAdapter(requireContext(), feedSpecList!!)
        binding.recycle.adapter = myAdapter

        binding.feedSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchQuery = s.toString()
                val searchResult = getFeedSpecListFromSQL(brand, searchQuery)

                if (searchResult.size == 0) {
                    binding.recycle.visibility = View.GONE
                    binding.emptyImage.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Специфікації не знайдені.", Toast.LENGTH_SHORT).show()
                } else {
                    binding.recycle.visibility = View.VISIBLE
                    binding.emptyImage.visibility = View.GONE
                }

                myAdapter.updateData(searchResult)
            }
        })
    }

    private fun getFeedSpecListFromSQL(brand: String, keyWord: String? = null): MutableList<FeedSpecs> {
        val list = mutableListOf<FeedSpecs>()
        val feedSpecMap = mutableMapOf<String, FeedSpecs>()

        dbHelper = DatabaseHelper(requireContext())
        try {
            dbHelper.openDatabase()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val sqlrequest = when (brand) {
            "Club 4 Paws" -> "specifications_clubs_four_paws"
            "Мяу" -> "specifications_myay"
            "OptiMeal" -> "specifications_opti_meal"
            "Розумний Вибір/АТБ" -> "specifications_smart_choice"
            "Sjobogardens" -> "specifications_sjobogardens"
            else -> "specifications_${brand.split(" ").joinToString("_").lowercase()}"
        }

        val cursor:Cursor = when(keyWord) {
            null -> dbHelper.readableDatabase.rawQuery("SELECT * FROM $sqlrequest", null)
            else -> { dbHelper.readableDatabase.rawQuery("SELECT * FROM $sqlrequest WHERE feed_name LIKE ? OR spec_num LIKE ?",
                arrayOf("%$keyWord%", "%$keyWord%"))
            }
        }

        if (cursor.moveToFirst()) {
            do {
                val spec_num = cursor.getString(1)
                val recipe_num = cursor.getInt(2)
                val feed_name = cursor.getString(3)
                val total_weight = cursor.getInt(4)
                val pieces_perc = cursor.getInt(5)
                val sauce_perc = cursor.getInt(6)
                val addition_one_perc = cursor.getInt(7)
                val addition_two_perc = cursor.getInt(8)
                val matrix_type = cursor.getString(9)
                val reg_data = cursor.getString(10)
                    if (feedSpecMap.containsKey(spec_num)) {
                        feedSpecMap[spec_num]?.additional_reg_data?.add(reg_data)
                    } else {
                        val feedSpec = FeedSpecs(spec_num, recipe_num, feed_name, total_weight, pieces_perc, sauce_perc, addition_one_perc, addition_two_perc, matrix_type, reg_data)
                        feedSpecMap[spec_num] = feedSpec
                    }
                } while (cursor.moveToNext())
            }
        cursor.close()

        dbHelper.close()

        list.addAll(feedSpecMap.values)
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
