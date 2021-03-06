package com.example.recipes.presentation.ui.registration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.recipes.R
import com.example.recipes.business.utils.CheckConnectionUtils
import com.example.recipes.databinding.ActivityRegistrationBinding
import com.example.recipes.presentation.ui.recipes.RecipesActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity(), ConfirmationListener {

    private var _binding: ActivityRegistrationBinding? = null
    private val binding: ActivityRegistrationBinding
        get() = _binding!!

    private var auth: FirebaseAuth? = null

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) {
        this.onSignInWithSocialResult(it)
    }

    private val viewModel: RegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_registration)
        binding.lifecycleOwner = this
        binding.vm = viewModel

        auth = Firebase.auth

    }

    override fun onStart() {
        super.onStart()

        if (auth?.currentUser != null) moveToRecipeActivity()

        binding.apply {
            tvForgotPassword.setOnClickListener {
                showConfirmationDialog()
            }
        }

        binding.apply {

            ibtnGoogle.setOnClickListener {
                if (isConnected()) {
                    val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
                    signInWithSocial(providers)
                }
            }

            ibtnFacebook.setOnClickListener {
                if (isConnected()) {
                    val providers = arrayListOf(AuthUI.IdpConfig.FacebookBuilder().build())
                    signInWithSocial(providers)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()

        viewModel.messageForUser.observe(this) {
            if (!it.isNullOrEmpty()) {
                showSnackBar(it)
                viewModel.messageForUser.value = null
            }
        }

        viewModel.user.observe(this) {
            openRecipeByUser(it)
        }
    }

    override fun confirmButtonClicked() {
        auth?.let { viewModel.sendPasswordResetEmail(it) }
    }

    override fun cancelButtonClicked() {
        showSnackBar(applicationContext.getString(R.string.cancel_reset_password))
    }

    private fun isConnected(): Boolean {
        return if (!CheckConnectionUtils.isNetConnected(applicationContext)) {
            showSnackBar("No internet connection")
            false
        } else true
    }

    private fun signInWithSocial(providers: ArrayList<AuthUI.IdpConfig>) {

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun onSignInWithSocialResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        Log.d("TAG", "onSignInWithSocialResult: ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            moveToRecipeActivity()
        } else {
            if (response == null) {
                showSnackBar(applicationContext.getString(R.string.cancel_user_registration))
            } else {
                catchError(response)
            }
        }
    }

    private fun moveToRecipeActivity() {
        startActivity(Intent(this, RecipesActivity::class.java))
        finish()
    }

    private fun openRecipeByUser(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, RecipesActivity::class.java))
            viewModel.setFirebaseUser(user)
            finish()
        }
    }

    private fun showSnackBar(message: String) {
        val mySnackBar = Snackbar
            .make(binding.root, message, Snackbar.LENGTH_INDEFINITE)

        mySnackBar.setAction("Ok") {
            if (!binding.btnSignIn.isVisible) {
                viewModel.changeVisibilityAtProgress()
            }
            mySnackBar.dismiss()
        }
        mySnackBar.show()
    }

    private fun catchError(response: IdpResponse) {                                     // ??????????????????
        when (response.error?.errorCode) {
            ErrorCodes.NO_NETWORK -> {
                showSnackBar("Sign in failed due to lack of network connection.")
            }
            ErrorCodes.PLAY_SERVICES_UPDATE_CANCELLED -> {
                showSnackBar("A required update to Play Services was cancelled by the user.")
            }
            ErrorCodes.DEVELOPER_ERROR -> {
                showSnackBar("A sign-in operation couldn't be completed due to a developer error.")
            }
            ErrorCodes.PROVIDER_ERROR -> {
                showSnackBar("An external sign-in provider error occurred.")
            }
            else -> {
                response.error?.localizedMessage?.let {
                    showSnackBar(it)
                }
            }
        }
    }

    private fun showConfirmationDialog() {
        ConfirmationDialogFragment().show(supportFragmentManager, "ConfirmationDialogFragmentTag")
    }

}

//        val test = TestData.DATA_1
//        when(test) {
//            TestData.DATA_1 -> {}
//            TestData.DATA_2-> {}
//            TestData.DATA_3 -> {}
//            TestData.DATA_4 -> {}
//        }.exclusive
//        val <T> T.exclusive: T
//            get() = this