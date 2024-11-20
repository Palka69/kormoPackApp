package com.example.kormopack.authorizationFragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.kormopack.NIGHT
import com.example.kormopack.PREF_NAME
import com.example.kormopack.PREF_PIB
import com.example.kormopack.R
import com.example.kormopack.activityCallbackInterfaces.DrawerCallback
import com.example.kormopack.databinding.FragmentAuthorazationBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.sheets.v4.SheetsScopes

class AuthorizationFragment : Fragment() {

    private var param1: String? = null
    private var _binding: FragmentAuthorazationBinding? = null
    private val binding get() = _binding!!
    private val RC_SIGN_IN = 9001

    private var drawerCallback: DrawerCallback? = null

    private val sharedViewModel: GoogleAccountViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DrawerCallback) {
            drawerCallback = context
        } else {
            throw RuntimeException("$context must implement DrawerCallback")
        }
    }

    override fun onDetach() {
        super.onDetach()
        drawerCallback = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthorazationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("AccountTestAuth", "${GoogleSignIn.getLastSignedInAccount(requireContext())}")

        drawerCallback?.lockDrawer()

        if (drawerCallback?.getSharPref()?.contains(NIGHT) == true) {
            drawerCallback?.getSharPref().also {
                it?.edit { putBoolean(NIGHT, false) }
            }
        }

        param1?.let {
            binding.editTextPIB.setText(it)
        } ?: run {
            binding.editTextPIB.setText("")
        }

        binding.googleSignInButton.isEnabled = false
        binding.googleSignInButton.setOnClickListener { signIn() }

        binding.editTextPIB.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                if (input.isNotEmpty() && input.length >= 12) {
                    val regex = Regex("^[А-ЯҐЄІЇ][а-яґєії]+\\s[А-ЯҐЄІЇ][а-яґєії]+\\s[А-ЯҐЄІЇ][а-яґєії]+")
                    if (!regex.matches(input)) {
                        binding.editTextPIB.error = "Некоректний ПІБ"
                        binding.googleSignInButton.isEnabled = false
                    } else {
                        binding.editTextPIB.error = null
                        binding.googleSignInButton.isEnabled = true
                    }
                } else {
                    binding.editTextPIB.error = null
                    binding.googleSignInButton.isEnabled = false
                }
            }
        })
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS_READONLY))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        googleSignInClient.signOut().addOnCompleteListener {
            Log.d("SignIn", "Signed out of the current account")

            googleSignInClient.revokeAccess().addOnCompleteListener {
                Log.d("SignIn", "Revoked access of the current account")

                startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!

                val allowedEmail = "operatorcvk@gmail.com"
                if (account.email == allowedEmail) {
                    sharedViewModel.setSharedAccount(account)

                    drawerCallback?.getSharPref().also {
                        it?.edit { putString(PREF_PIB, binding.editTextPIB.text.toString().trim()).apply() }
                    }

                    val action = AuthorizationFragmentDirections.actionAuthorizationFragmentToNavSpecs()
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(context, "Вхід дозволений лише з певної електронної пошти.", Toast.LENGTH_SHORT).show()
                    Log.w("Korm28", "SignInResult:failed, unauthorized email")

                    GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                }

            } catch (e: ApiException) {
                Toast.makeText(context, "Відсутнє інтернет - з'єднання.", Toast.LENGTH_SHORT).show()
                Log.w("Korm28", "SignInResult:failed code=" + e.statusCode)
            }
        }
    }
}