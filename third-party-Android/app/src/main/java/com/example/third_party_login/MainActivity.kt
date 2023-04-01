package com.example.third_party_login

import android.app.Activity
import android.app.Dialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.third_party_login.databinding.ActivityMainBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    lateinit var twitter: Twitter
    lateinit var twitterDialog: Dialog
    private var callBackManager: CallbackManager? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            // use token to login google
            print(task.result.idToken)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        callBackManager = CallbackManager.Factory.create()

        setupButton()
    }

    private fun setupButton() {
        binding.twitterButton.setOnClickListener {
            twitterLogin()
        }

        binding.facebookButton.setOnClickListener {
            facebookLogin()
        }

        binding.googleButton.setOnClickListener {
            googleLogin()
        }
    }

    private fun twitterLogin() {
        lifecycleScope.launch(Dispatchers.Default) {
            val builder = ConfigurationBuilder()
                .setDebugEnabled(true)
                .setOAuthConsumerKey("") // Consume Key
                .setOAuthConsumerSecret("") // Consume Secret

            val config = builder.build()
            val factory = TwitterFactory(config)
            twitter = factory.instance

            try {
                val requestToken = twitter.oAuthRequestToken
                withContext(Dispatchers.Main) {
                    setupTwitterWebDialog(requestToken.authenticationURL)
                }
            } catch (e: IllegalStateException) {
                print("Error")
            }
        }
    }

    private fun setupTwitterWebDialog(url: String) {
        twitterDialog = Dialog(this)
        val webView = WebView(this)

        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.webViewClient = TwitterWebClinent()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)

        twitterDialog.setContentView(webView)
        twitterDialog.show()
        twitterDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    inner class TwitterWebClinent: WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            // check whether to use callback URL
            if (request?.url.toString().startsWith("CALLBACK URL")) {
                handleUrl(request?.url.toString())
                if (request?.url.toString().contains("CALLBACK URL")) {
                    twitterDialog.dismiss()
                }
                return true
            }

            return false
        }
    }

    private fun handleUrl(url: String) {
        val uri = Uri.parse(url)
        val oauthVerifier = uri.getQueryParameter("oauth_verifier") ?: return
        lifecycleScope.launch(Dispatchers.Default) {
            val accessToken = withContext(Dispatchers.IO) {
                twitter.getOAuthAccessToken(oauthVerifier)
            }
            // use access token
            print(accessToken.token)
        }
    }

    // set applicationId and client token in AndroidManifest.xml
    private fun facebookLogin() {
        val loginManager = LoginManager()

        loginManager.registerCallback(callBackManager, object: FacebookCallback<LoginResult> {
            override fun onCancel() {
            }

            override fun onError(error: FacebookException) {
            }

            override fun onSuccess(result: LoginResult) {
                print(result.accessToken.token)
            }
        })

        callBackManager?.let {
            LoginManager.getInstance().logIn(
                activityResultRegistryOwner = this,
                callbackManager = it,
                permissions = listOf("email"))
        }
    }

    private fun googleLogin() {
        val option = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(
                com.google.android.gms.common.api.Scope(Scopes.PROFILE),
                com.google.android.gms.common.api.Scope(Scopes.EMAIL),
                com.google.android.gms.common.api.Scope(Scopes.OPEN_ID)
            )
            .requestServerAuthCode("○○○-○○○○.apps.googleusercontent.com")
            .requestEmail().build()

        val googleSignInClient = GoogleSignIn.getClient(this, option)
        val googleSignInIntent = googleSignInClient.signInIntent
        launcher.launch(googleSignInIntent)
    }
}