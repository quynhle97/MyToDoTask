package group14.finalproject.mytodotask

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import group14.finalproject.mytodotask.fragments.ForgetPasswordFragment
import group14.finalproject.mytodotask.fragments.LoginFragment
import group14.finalproject.mytodotask.fragments.RegisterFragment
import group14.finalproject.mytodotask.sharedpreferences.SharedPreferencesHelper
import android.content.Intent

class FirstActivity : AppCompatActivity(), LoginFragment.Listener,
    RegisterFragment.Listener, ForgetPasswordFragment.Listener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        if (SharedPreferencesHelper.readString(USERNAME_KEY) == "NO_CONTAIN_KEY") {
            addFirstFragment()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun addFirstFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.flContainer, LoginFragment())
        fragmentTransaction.commit()

    }

    override fun openRegisterScreen() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(
            R.anim.enter,
            R.anim.exit,
            R.anim.pop_enter,
            R.anim.pop_exit
        )
        fragmentTransaction.replace(
            R.id.flContainer,
            RegisterFragment()
        )
        fragmentTransaction.addToBackStack(RegisterFragment::class.java.simpleName)
        fragmentTransaction.commit()
    }

    override fun openForgotScreen() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(
            R.anim.enter,
            R.anim.exit,
            R.anim.pop_enter,
            R.anim.pop_exit
        )
        fragmentTransaction.replace(
            R.id.flContainer,
            ForgetPasswordFragment()
        )
        fragmentTransaction.addToBackStack(ForgetPasswordFragment::class.java.simpleName)
        fragmentTransaction.commit()
    }

    override fun openLoginScreen() {
        onBackPressed()
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)
        if (fragment is LoginFragment) {
            val loginfragment = fragment
            loginfragment.setListener(this)

        }
        if (fragment is RegisterFragment) {
            val registerFragment = fragment
            registerFragment.setListener(this)

        }
        if (fragment is ForgetPasswordFragment) {
            val forgetFragment = fragment
            forgetFragment.setListener(this)

        }
    }
}
