package com.example.cse489assignment2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.cse489assignment2.databinding.ActivityMainBinding

/**
 * Main (launcher) activity.
 *
 * Hosts the navigation drawer widget together with Jetpack Navigation:
 * every drawer entry opens its designated fragment:
 *  A. Broadcast Receiver
 *  B. Image Scale
 *  C. Video
 *  D. Audio
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar with the hamburger icon that opens/closes the drawer.
        setSupportActionBar(binding.toolbar)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        // All four top level destinations show the hamburger icon in the toolbar.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.broadcastReceiverFragment,
                R.id.imageScaleFragment,
                R.id.videoFragment,
                R.id.audioFragment
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
