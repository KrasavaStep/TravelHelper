package com.example.travelhelper

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.travelhelper.databinding.ActivityMainBinding
import com.example.travelhelper.utils.AppBarViewModel
import com.yandex.mapkit.MapKitFactory
import kotlin.getValue
import androidx.core.view.size
import androidx.core.view.get
import com.example.travelhelper.data.network.registration.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    private val appBarViewModel: AppBarViewModel by viewModels()

    interface MenuConfig {
        fun shouldShowMenuItems(menu: Menu): Boolean
    }

    private var currentMenuConfig: MenuConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_KEY)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.map_fragment)

        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_main_map, R.id.nav_liked_places, R.id.nav_settings, R.id.nav_about_app
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_activity, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        updateMenuVisibility(menu)
        searchView.setOnQueryTextListener( object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false;
            }

            private fun updateNavHeader() {
                val navView: NavigationView = binding.navView
                val headerView = navView.getHeaderView(0)
                val navUsername = headerView.findViewById<TextView>(R.id.nav_header_username)
                val navUserEmail = headerView.findViewById<TextView>(R.id.textView)

                val newUsername = intent.getStringExtra("USER_NAME")
                val newEmail = intent.getStringExtra("USER_EMAIL")

                if (newUsername != null && newEmail != null) {
                    navUsername.text = newUsername
                    navUserEmail.text = newEmail
                } else {
                    val user = auth.currentUser
                    if (user != null) {
                        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.uid)
                        dbRef.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val usernameFromDb = snapshot.child("username").getValue(String::class.java)
                                val emailFromDb = snapshot.child("email").getValue(String::class.java)

                                if (usernameFromDb != null) {
                                    navUsername.text = usernameFromDb
                                }
                                if (emailFromDb != null) {
                                    navUserEmail.text = emailFromDb
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false;
            }
        })
        return true
    }

    private fun updateMenuVisibility(menu: Menu) {
        currentMenuConfig?.shouldShowMenuItems(menu)?.let { shouldShow ->
            for (i in 0 until menu.size) {
                menu[i].isVisible = shouldShow
            }
        }
    }

    fun setMenuConfig(config: MenuConfig?) {
        currentMenuConfig = config
        invalidateOptionsMenu()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}