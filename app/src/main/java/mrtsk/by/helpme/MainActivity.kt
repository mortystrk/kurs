package mrtsk.by.helpme

/*import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import kotlinx.android.synthetic.main.activity_main.*
import mrtsk.by.helpme.adapters.ViewPagerAdapter
import mrtsk.by.helpme.fragments.MainPageFragment
import mrtsk.by.helpme.fragments.SimpleFragment

class MainActivity : AppCompatActivity() {

    private lateinit var id: String
    private lateinit var mainPageFragment: MainPageFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        id = intent.getStringExtra("_id")

        mainPageFragment = MainPageFragment.newInstance(id)

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        setupViewPager(viewpager)

        tablayout.setupWithViewPager(viewpager)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(mainPageFragment, "Профиль")
        adapter.addFragment(SimpleFragment(), "лоло")
        viewPager.adapter = adapter
    }
}*/
