package com.example.kormopack.authorizationFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class GoogleAccountViewModel : ViewModel() {
    private val _sharedAccount = MutableLiveData<GoogleSignInAccount>()
    val sharedAccount: LiveData<GoogleSignInAccount> get() = _sharedAccount

    fun setSharedAccount(value: GoogleSignInAccount) {
        _sharedAccount.value = value
    }
}