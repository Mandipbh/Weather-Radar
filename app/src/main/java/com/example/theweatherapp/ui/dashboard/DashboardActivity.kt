package com.example.theweatherapp.ui.dashboard

import android.os.Bundle
import android.view.Menu
import android.widget.ImageButton
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.theweatherapp.R
import com.example.theweatherapp.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

//    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.appBarDashboard.toolbar)

        val drawerLayout = binding.drawerLayout
        val openDrawerBtn = findViewById<ImageButton>(R.id.btn_open_drawer)

        openDrawerBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        val navView: NavigationView = binding.navView
//        val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
//            ), drawerLayout
//        )
////        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.dashboard, menu)
        return true
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }
}