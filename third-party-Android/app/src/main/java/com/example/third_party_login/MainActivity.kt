package com.example.third_party_login

import android.app.Dialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
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
import com.google.android.gms.common.api.Scope
import jp.co.yahoo.yconnect.yjloginsdk.core.LoginError
import jp.co.yahoo.yconnect.yjloginsdk.core.LoginListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.lang.IllegalStateException
import java.security.MessageDigest
import java.util.*

class MainActivity : AppCompatActivity(), LoginListener {
    lateinit var binding: ActivityMainBinding

    lateinit var twitter: Twitter
    lateinit var twitterDialog: Dialog
    private var callBackManager: CallbackManager? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
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
        jp.co.yahoo.yconnect.yjloginsdk.core.LoginManager.setup("CLIENT_ID", "REDIRECT_URL".toUri())
        jp.co.yahoo.yconnect.yjloginsdk.core.LoginManager.setLoginListener(this)

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

        binding.yahooButton.setOnClickListener {
            yahooLogin()
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
                Scope(Scopes.PROFILE),
                Scope(Scopes.EMAIL),
                Scope(Scopes.OPEN_ID)
            )
            .requestServerAuthCode("○○○-○○○○.apps.googleusercontent.com")
            .requestEmail().build()

        val googleSignInClient = GoogleSignIn.getClient(this, option)
        val googleSignInIntent = googleSignInClient.signInIntent
        launcher.launch(googleSignInIntent)
    }

    private fun yahooLogin() {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(byteArrayOf())
        val codeChallenge = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val codeChallengeFormatted = codeChallenge.replace("=", "").replace("/", "_").replace("+", "-")
        jp.co.yahoo.yconnect.yjloginsdk.core.LoginManager.login(this, setOf(jp.co.yahoo.yconnect.yjloginsdk.core.Scope.OPENID), UUID.randomUUID().toString(), codeChallengeFormatted)
    }

    override fun onLoginFailure(loginError: LoginError) {
        print(loginError)
    }

    override fun onLoginSuccess(loginResult: jp.co.yahoo.yconnect.yjloginsdk.core.LoginResult) {
        print(loginResult.authorizationCode)
    }
}