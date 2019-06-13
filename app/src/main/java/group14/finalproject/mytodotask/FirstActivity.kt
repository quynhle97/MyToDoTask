package group14.finalproject.mytodotask

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import group14.finalproject.mytodotask.login.ForgetPasswordFragment
import group14.finalproject.mytodotask.login.LoginFragment
import group14.finalproject.mytodotask.login.RegisterFragment

class FirstActivity : AppCompatActivity(), LoginFragment.Listener,
    RegisterFragment.Listener, ForgetPasswordFragment.Listener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        addFirstFragment()
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
