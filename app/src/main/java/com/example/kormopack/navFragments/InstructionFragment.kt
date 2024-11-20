package com.example.kormopack.navFragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.kormopack.activityCallbackInterfaces.ToolbarCallback
import com.example.kormopack.databinding.FragmentInstructionBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class InstructionFragment : Fragment() {

    private var _binding: FragmentInstructionBinding? = null
    private val binding get() = _binding!!
    private lateinit var pdfImageView: ImageView
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private var totalPages: Int = 0
    private var displayPage: Int = 0

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInstructionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarCallback?.changeToolbarColor(0)

        pdfImageView = binding.img
        val pdfFile = getFileFromAssets("leepackPDF.pdf")

        binding.nextPage.setOnClickListener {
            if (displayPage < (totalPages - 1)) {
                displayPage++
                showPage(displayPage)
            }
        }
        binding.previousPage.setOnClickListener {
            if (displayPage > 0) {
                displayPage--
                showPage(displayPage)
            }
        }

        openRenderer(pdfFile)
    }

    override fun onDestroy() {
        super.onDestroy()
        closeRenderer()
        _binding = null
    }

    private fun openRenderer(file: File) {
        try {
            val fileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
            totalPages = pdfRenderer!!.pageCount
            showPage(0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun closeRenderer() {
        currentPage?.close()
        pdfRenderer?.close()
    }

    private fun showPage(index: Int) {
        pdfRenderer?.let { renderer ->
            currentPage?.close()
            currentPage = renderer.openPage(index)
            displayPage = index
            currentPage?.let { page ->
                val bitmap = Bitmap.createBitmap(currentPage!!.width, currentPage!!.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                pdfImageView.setImageBitmap(bitmap)
            }
            binding.pagetxt.text = "${index+1}/$totalPages"
        }
    }

    private fun getFileFromAssets(fileName: String): File {
        val file = File(context?.cacheDir, fileName)
        try {
            context?.assets?.open(fileName)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }
}