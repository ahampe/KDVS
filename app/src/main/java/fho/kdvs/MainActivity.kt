package fho.kdvs

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var viewModel: KdvsViewModel? = null
    private val navController: NavController get() = findNavController(R.id.nav_host_fragment)

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                navController.navigate(R.id.homeFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_schedule_grid -> {
                navController.navigate(R.id.scheduleFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                // Settings / other stuff
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(KdvsViewModel::class.java)

        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        Log.d("DAVISCA","Test Foobar")
    }

    override fun onSupportNavigateUp() = navController.navigateUp()
}
