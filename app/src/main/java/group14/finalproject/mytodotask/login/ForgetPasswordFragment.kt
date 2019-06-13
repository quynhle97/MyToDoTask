package group14.finalproject.mytodotask.login

import android.os.Bundle
import android.view.View
import group14.finalproject.mytodotask.R
import kotlinx.android.synthetic.main.fragment_forget_password.*

class ForgetPasswordFragment : BaseFragment() {
    interface Listener {
        fun openLoginScreen()
    }

    lateinit var mListener: Listener

    override fun getTagName(): String {
        return ForgetPasswordFragment::class.java.simpleName
    }

    override fun inflateView(): Int {
        return R.layout.fragment_forget_password
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvLogin.setOnClickListener {
            mListener.openLoginScreen()
        }
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

}