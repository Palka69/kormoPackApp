package com.example.kormopack.navFragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.example.kormopack.R
import com.example.kormopack.activityCallbackInterfaces.ToolbarCallback
import com.example.kormopack.databinding.FragmentAboutBinding
import com.example.kormopack.databinding.FragmentSettingsBinding

class AboutFragment : Fragment(), OnClickListener {
    private var _binding: FragmentAboutBinding? = null
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
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarCallback?.changeToolbarColor(0)


        binding.devInstTxtLink.setOnClickListener(this)
        binding.devGithubTxtLink.setOnClickListener(this)
        binding.kormotechInstTxtLink.setOnClickListener(this)
        binding.kormotechSTxtLink.setOnClickListener(this)
    }



    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClick(v: View?) {
        var url = ""
        Log.d("MyLinkKKK", "${v?.id}")
        Log.d("MyLinkKKK", "${v?.id == binding.devInstTxtLink.id}")
        when (v?.id) {
            binding.devInstTxtLink.id -> url = "https://www.instagram.com/aezakmi___34?igsh=MXYzZnMxdmJlNzlybg=="
            binding.devGithubTxtLink.id -> url = "https://github.com/Palka69/kormoPackApp"
            binding.kormotechInstTxtLink.id -> url = "https://www.instagram.com/kormotech.global/"
            binding.kormotechSTxtLink.id -> url = "https://kormotech.com/uk"
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}