package group14.finalproject.mytodotask.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import group14.finalproject.mytodotask.USERNAME_DEFAULT
import group14.finalproject.mytodotask.MainActivity
import group14.finalproject.mytodotask.R
import group14.finalproject.mytodotask.USERNAME_KEY
import group14.finalproject.mytodotask.sharedpreferences.SharedPreferencesHelper
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.edPassword
import kotlinx.android.synthetic.main.fragment_login.tvSkip

class LoginFragment : BaseFragment() {

    interface Listener {
        fun openRegisterScreen()
        fun openForgotScreen()
    }

    lateinit var mListener: Listener

    lateinit var mAuth: FirebaseAuth

    override fun getTagName(): String {
        return LoginFragment::class.java.simpleName
    }

    override fun inflateView(): Int {
        return R.layout.fragment_login
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvSkip.setOnClickListener {
            SharedPreferencesHelper.saveString(USERNAME_KEY, USERNAME_DEFAULT)
            startActivity(Intent(activity, MainActivity::class.java))
        }

        initViews()

        initFirebase()
    }

    private fun initFirebase() {
        mAuth = FirebaseAuth.getInstance()
    }

    private fun initViews() {
        tvRegister.setOnClickListener {
            mListener.openRegisterScreen()
        }

        tvReset.setOnClickListener {
            mListener.openForgotScreen()
        }

        btnLogin.setOnClickListener {
            val email = edtmail.text.toString().trim()
            val password = edPassword.text.toString().trim()
            mAuth.signInWithEmailAndPassword(email,password)
                .addOnFailureListener {
                    Toast.makeText(context,"Login Failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }.addOnSuccessListener {
                    SharedPreferencesHelper.saveString(USERNAME_KEY, it.user.email.toString().substringBeforeLast("@"))
                    Toast.makeText(context,"Login success: ${it.user.email}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(activity, MainActivity::class.java))
                }.addOnCanceledListener {
                    Toast.makeText(context,"Login Canceled ", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }


}