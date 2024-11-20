package com.example.kormopack.navFragments

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kormopack.activityCallbackInterfaces.DrawerCallback
import com.example.kormopack.activityCallbackInterfaces.ToolbarCallback
import com.example.kormopack.databinding.FragmentPayCalcBinding
import com.example.kormopack.utils.NetworkChangeReceiver


class PayCalcFragment : Fragment() {
    private var _binding: FragmentPayCalcBinding? = null
    private val binding get() = _binding!!

    private var toolbarCallback: ToolbarCallback? = null

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

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
        _binding = FragmentPayCalcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarCallback?.changeToolbarColor(1)

        val webView = binding.webView
        val progressBar = binding.progress

        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView?, errorCode: Int, description: String?, failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Error loading page", Toast.LENGTH_SHORT).show()
            }

            override fun onReceivedHttpError(
                view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                progressBar.visibility = View.GONE
                Toast.makeText(context, "HTTP error loading page", Toast.LENGTH_SHORT).show()
            }
        }

        networkChangeReceiver = NetworkChangeReceiver (requireContext()) {
            if (networkChangeReceiver.isNetworkAvailable()) {
                webView.loadUrl("https://salarycalculator-72940.web.app/")
            }
        }

        if (!networkChangeReceiver.isNetworkAvailable()) {
            Toast.makeText(requireContext(), "Відсутнє інтернет - з'єднання.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireContext().registerReceiver(networkChangeReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(networkChangeReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}