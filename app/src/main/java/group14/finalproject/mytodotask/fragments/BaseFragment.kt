package group14.finalproject.mytodotask.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class BaseFragment : Fragment() {
    abstract fun getTagName() : String
    abstract fun inflateView(): Int

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.e(getTagName(), "onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(getTagName(), "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.e(getTagName(), "onCreateView")
        return inflater.inflate(inflateView(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.e(getTagName(), "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        Log.e(getTagName(), "onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.e(getTagName(), "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.e(getTagName(), "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.e(getTagName(), "onStop")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.e(getTagName(), "onDestroyView")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.e(getTagName(), "onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.e(getTagName(), "onDetach")
        super.onDetach()
    }
}