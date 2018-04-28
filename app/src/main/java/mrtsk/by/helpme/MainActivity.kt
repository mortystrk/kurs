package mrtsk.by.helpme

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import kotlinx.android.synthetic.main.activity_main.*
import mrtsk.by.helpme.adapters.ViewPagerAdapter
import mrtsk.by.helpme.fragments.SimpleFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        setupViewPager(viewpager)

        tablayout.setupWithViewPager(viewpager)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(SimpleFragment(), "лала")
        adapter.addFragment(SimpleFragment(), "лоло")
        adapter.addFragment(SimpleFragment(), "лулу")
        viewPager.adapter = adapter
    }
}
