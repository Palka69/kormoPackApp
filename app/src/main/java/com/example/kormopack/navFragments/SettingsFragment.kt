package com.example.kormopack.navFragments

import DatabaseHelper
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.kormopack.R
import com.example.kormopack.activityCallbackInterfaces.ToolbarCallback
import com.example.kormopack.brandRecycleClasses.FeedBrand
import com.example.kormopack.databinding.FragmentPersonalCabinetBinding
import com.example.kormopack.databinding.FragmentSettingsBinding
import com.example.kormopack.feedRecycleClasses.FeedSpecs
import com.google.android.material.textfield.TextInputEditText

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var toolbarCallback: ToolbarCallback? = null

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
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarCallback?.changeToolbarColor(0)

        binding.addNewBrandButton.setOnClickListener {
            showAddBrandDialog()
        }
        binding.addNewSpec.setOnClickListener {
            showAddSpecDialog()
        }
    }

    private fun showAddSpecDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_spec, null)

        val dbHelper = DatabaseHelper(requireContext())
        val list = dbHelper.getAllBrands()
        val brandSpinner = dialogLayout.findViewById<Spinner>(R.id.brand_spinner)
        val matrixSpinner = dialogLayout.findViewById<Spinner>(R.id.matrix_spinner)
        val brandImage = dialogLayout.findViewById<ImageView>(R.id.brand_image)
        val matrixImage = dialogLayout.findViewById<ImageView>(R.id.matrix_image)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        brandSpinner.adapter = adapter

        brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                itemSelected: View?, selectedItemPosition: Int, selectedId: Long
            ) {
                val xArray = arrayOf(R.drawable.c4p_logo, R.drawable.myau_logo, R.drawable.opti_meal_logo,
                    R.drawable.smart_choice_logo, R.drawable.sjobogarden_logo)
                if (selectedItemPosition < 5) {
                    brandImage.setImageResource(xArray[selectedItemPosition])
                } else {
                    brandImage.setImageResource(R.drawable.kormo_pets_paw)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        matrixSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                itemSelected: View?, selectedItemPosition: Int, selectedId: Long
            ) {
                val xArray = arrayOf(R.drawable.bk_1_matrix, R.drawable.bk_2_matrix, R.drawable.bk_3_matrix)
                matrixImage.setImageResource(xArray[selectedItemPosition])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        with(builder) {
            setTitle(getString(R.string.add_new_spec))
            setPositiveButton("OK") { dialog, which ->
                addNewSpec(dialogLayout)
            }
            setNegativeButton("Назад") { dialog, which ->
                dialog.dismiss()
            }
            setIcon(R.drawable.blue_k_logo)
            setView(dialogLayout)
            show()
        }
    }

    private fun showAddBrandDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_brand, null)
        val editTextBrandName = dialogLayout.findViewById<TextInputEditText>(R.id.edit_text_brand_name)

        with(builder) {
            setTitle(getString(R.string.add_new_brand))
            setPositiveButton("OK") { dialog, which ->
                val brandName = editTextBrandName.text.toString()
                if (brandName.isNotEmpty()) {
                    addNewBrand(brandName)
                }
            }
            setNegativeButton("Назад") { dialog, which ->
                dialog.dismiss()
            }
            setIcon(R.drawable.blue_k_logo)
            setView(dialogLayout)
            show()
        }
    }

    private fun addNewBrand(brandName: String) {
        // Створіть новий об'єкт FeedBrand з необхідними значеннями
        val newBrand = FeedBrand(
            name = brandName,
            brandLogo = ContextCompat.getDrawable(requireContext(), R.drawable.kormo_pets_paw)!!,
            cardBackColor = Color.WHITE, // Ви можете задати потрібний колір
            textColor = Color.BLACK      // Ви можете задати потрібний колір
        )

        // Вставте новий бренд у базу даних
        val dbHelper = DatabaseHelper(requireContext())
        dbHelper.insertFeedBrand(newBrand)
        dbHelper.createNewFeedBrandTable(newBrand.name)
        Toast.makeText(requireContext(), "Додано новий бренд - ${newBrand.name}", Toast.LENGTH_SHORT).show()
    }

    private fun addNewSpec(view: View) {
        val spec_num = view.findViewById<TextInputEditText>(R.id.edit_text_spec_num)
        val recipe_num = view.findViewById<TextInputEditText>(R.id.edit_text_recipe_num)
        val name = view.findViewById<TextInputEditText>(R.id.edit_text_feed_name)
        val total_weight = view.findViewById<TextInputEditText>(R.id.edit_text_total_weight)
        val pieces_perc = view.findViewById<TextInputEditText>(R.id.edit_text_pieces_perc)
        val sauce_perc = view.findViewById<TextInputEditText>(R.id.edit_text_sauce_perc)
        val addition_one = view.findViewById<TextInputEditText>(R.id.edit_text_addition_one_perc)
        val addition_two = view.findViewById<TextInputEditText>(R.id.edit_text_addition_two_perc)
        val reg_data = view.findViewById<TextInputEditText>(R.id.edit_text_reg_data)

        val matrix = view.findViewById<Spinner>(R.id.matrix_spinner)
        val brand = view.findViewById<Spinner>(R.id.brand_spinner)

        if (spec_num.text?.isNotEmpty() == true && recipe_num.text?.isNotEmpty() == true && name.text?.isNotEmpty() == true
            && total_weight.text?.isNotEmpty() == true && pieces_perc.text?.isNotEmpty() == true
            && sauce_perc.text?.isNotEmpty() == true && addition_one.text?.isNotEmpty() == true
            && addition_two.text?.isNotEmpty() == true && reg_data.text?.isNotEmpty() == true) {

            val dbHelper = DatabaseHelper(requireContext())
            dbHelper.insertFeedSpec(FeedSpecs(spec_num.text.toString(), recipe_num.text.toString().toInt(),
                name.text.toString(), total_weight.text.toString().toInt(), pieces_perc.text.toString().toInt(),
                sauce_perc.text.toString().toInt(), addition_one.text.toString().toInt(),
                addition_two.text.toString().toInt(), matrix.selectedItem.toString(), reg_data.text.toString()),
                brand.selectedItem.toString())

        } else {
            Toast.makeText(requireContext(), "Заповніть усі поля",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}