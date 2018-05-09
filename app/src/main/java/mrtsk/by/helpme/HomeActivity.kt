package mrtsk.by.helpme

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.*
import kotlinx.android.synthetic.main.activity_home.*
import mrtsk.by.helpme.fragments.MySubscriptionsFragment

class HomeActivity : FragmentActivity(), MySubscriptionsFragment.OnFragmentInteractionListener {
    override fun onFragmentInteraction(uri: Uri) {

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val pagerAdapter = MyFragmentPagerAdapter(supportFragmentManager)
        pager.adapter = pagerAdapter
    }

    inner class MyFragmentPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return MySubscriptionsFragment.newInstance()
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence {
            return "hello pipiska"
        }

    }
}
