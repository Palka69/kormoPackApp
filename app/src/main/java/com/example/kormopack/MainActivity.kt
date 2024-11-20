package com.example.kormopack

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kormopack.activityCallbackInterfaces.DrawerCallback
import com.example.kormopack.activityCallbackInterfaces.ToolbarCallback
import com.example.kormopack.brandRecycleClasses.FeedBrand
import com.example.kormopack.databinding.ActivityMainBinding
import com.example.kormopack.navFragments.SpecsFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.util.Timer
import java.util.TimerTask
import kotlin.properties.Delegates

const val PREF_NAME: String = "pib_pref"
const val PREF_PIB: String = "pib"
const val NIGHT: String = "night"

class MainActivity : AppCompatActivity(), DrawerCallback, ToolbarCallback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var pref: SharedPreferences
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()

        setNextFragment()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else if (navController.currentDestination?.id == R.id.authorizationFragment) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (navController.currentDestination?.id == R.id.feedListFragment) {
                    onBackPressed()
                    return true
                }
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun init() {
        setTheme(android.R.style.Theme_DeviceDefault_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        application.setTheme(android.R.style.Theme_Material_Light_NoActionBar)
        setTheme(android.R.style.Theme_Material_Light_NoActionBar)

        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)


        lockDrawer()

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.burger_menu)
        }
        binding.toolbar.visibility = View.GONE
    }


    private fun setNextFragment() {
        val account = GoogleSignIn.getLastSignedInAccount(this)

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    try {
                        binding.introLogo.visibility = View.GONE
                        initializeNavigation(R.navigation.nav_graph)
                        initializeDrawerMenu()
                        if (account == null) {
                            binding.fragmentContainer.visibility = View.VISIBLE
                        } else {
                            val bundle = Bundle()
                            bundle.putParcelable("user", account)
                            navController.navigate(R.id.action_authorizationFragment_to_nav_specs, bundle)
                        }
                    } catch (e: Exception) {
                        Log.e("Korm28", "Error: ${e.message}", e)
                    }
                }
            }
        }, 1000)
    }

    private fun setNavItemSelected() {
        binding.navView.setNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_specs -> {
                    if (navController.currentDestination?.id != R.id.nav_specs) {
                        when (navController.currentDestination?.id) {
                            R.id.nav_cabinet -> navController.navigate(R.id.action_nav_cabinet_to_nav_specs)
                            R.id.nav_pay_calc -> navController.navigate(R.id.action_nav_pay_calc_to_nav_specs)
                            R.id.nav_instruction -> navController.navigate(R.id.action_nav_instruction_to_nav_specs)
                            R.id.nav_settings -> navController.navigate(R.id.action_nav_settings_to_nav_specs)
                            R.id.nav_about -> navController.navigate(R.id.action_nav_about_to_nav_specs)
                        }
                    }
                }
                R.id.nav_cabinet -> {
                    if (navController.currentDestination?.id != R.id.nav_cabinet) {
                        when (navController.currentDestination?.id) {
                            R.id.nav_specs -> navController.navigate(R.id.action_nav_specs_to_nav_cabinet)
                            R.id.nav_pay_calc -> navController.navigate(R.id.action_nav_pay_calc_to_nav_cabinet)
                            R.id.nav_instruction -> navController.navigate(R.id.action_nav_instruction_to_nav_cabinet)
                            R.id.nav_settings -> navController.navigate(R.id.action_nav_settings_to_nav_cabinet)
                            R.id.nav_about -> navController.navigate(R.id.action_nav_about_to_nav_cabinet)
                        }
                    }
                }
                R.id.nav_pay_calc -> {
                    if (navController.currentDestination?.id != R.id.nav_pay_calc) {
                        when (navController.currentDestination?.id) {
                            R.id.nav_specs -> navController.navigate(R.id.action_nav_specs_to_nav_pay_calc)
                            R.id.nav_cabinet -> navController.navigate(R.id.action_nav_cabinet_to_nav_pay_calc)
                            R.id.nav_instruction -> navController.navigate(R.id.action_nav_instruction_to_nav_pay_calc)
                            R.id.nav_settings -> navController.navigate(R.id.action_nav_settings_to_nav_pay_calc)
                            R.id.nav_about -> navController.navigate(R.id.action_nav_about_to_nav_pay_calc)
                        }
                    }
                }
                R.id.nav_instruction -> {
                    if (navController.currentDestination?.id != R.id.nav_instruction) {
                        when (navController.currentDestination?.id) {
                            R.id.nav_specs -> navController.navigate(R.id.action_nav_specs_to_nav_instruction)
                            R.id.nav_cabinet -> navController.navigate(R.id.action_nav_cabinet_to_nav_instruction)
                            R.id.nav_pay_calc -> navController.navigate(R.id.action_nav_pay_calc_to_nav_instruction)
                            R.id.nav_settings -> navController.navigate(R.id.action_nav_settings_to_nav_instruction)
                            R.id.nav_about -> navController.navigate(R.id.action_nav_about_to_nav_instruction)
                        }
                    }
                }
                R.id.nav_settings -> {
                    if (navController.currentDestination?.id != R.id.nav_settings) {
                        when (navController.currentDestination?.id) {
                            R.id.nav_specs -> navController.navigate(R.id.action_nav_specs_to_nav_settings)
                            R.id.nav_cabinet -> navController.navigate(R.id.action_nav_cabinet_to_nav_settings)
                            R.id.nav_pay_calc -> navController.navigate(R.id.action_nav_pay_calc_to_nav_settings)
                            R.id.nav_instruction -> navController.navigate(R.id.action_nav_instruction_to_nav_settings)
                            R.id.nav_about -> navController.navigate(R.id.action_nav_about_to_nav_settings)
                        }
                    }
                }
                R.id.nav_about -> {
                    if (navController.currentDestination?.id != R.id.nav_about) {
                        when (navController.currentDestination?.id) {
                            R.id.nav_specs -> navController.navigate(R.id.action_nav_specs_to_nav_about)
                            R.id.nav_cabinet -> navController.navigate(R.id.action_nav_cabinet_to_nav_about)
                            R.id.nav_pay_calc -> navController.navigate(R.id.action_nav_pay_calc_to_nav_about)
                            R.id.nav_instruction -> navController.navigate(R.id.action_nav_instruction_to_nav_about)
                            R.id.nav_settings -> navController.navigate(R.id.action_nav_settings_to_nav_about)
                        }
                    }
                }
                R.id.nav_exit -> {
                    val googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
                    googleSignInClient.signOut().addOnCompleteListener(this) {
                        Log.d("Korm28", "Signed out successfully")
                    }
                    hideToolbar()
                    navController.popBackStack(navController.graph.startDestinationId, false)
                    navController.navigate(R.id.authorizationFragment)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun initializeNavigation(navGraphId: Int, myStartDestination: Int? = null) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(navGraphId)
        navController.graph = navGraph
        val constraintLayout = binding.constL
        Navigation.setViewNavController(constraintLayout, navController)
    }

    fun initializeDrawerMenu() {
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_specs, R.id.nav_cabinet, R.id.nav_pay_calc, R.id.nav_instruction, R.id.nav_settings, R.id.nav_about),
            binding.drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        setNavItemSelected()
    }

    override fun lockDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun unlockDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    override fun showToolbar() {
        binding.toolbar.visibility = View.VISIBLE
    }

    override fun hideToolbar() {
        binding.toolbar.visibility = View.GONE
    }

    override fun renameToolbar(string: String) {
        binding.toolbar.title = string
    }

    override fun changeToolbarColor(num: Int) {
        when(num) {
            1 -> binding.toolbar.setBackgroundColor(resources.getColor(R.color.kormoTech_calc_blue, theme))
            else -> binding.toolbar.setBackgroundColor(resources.getColor(R.color.white, theme))
        }
    }

    override fun renameDrawerUser(user: String) {
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.header_user).text = user
    }

    override fun getSharPref(): SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}