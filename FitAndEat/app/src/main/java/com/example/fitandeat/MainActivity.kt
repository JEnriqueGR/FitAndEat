package com.example.fitandeat

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.fitandeat.exercise.ExercisesFragment
import com.example.fitandeat.exercise.RoutineFragment
import com.example.fitandeat.food.FoodFragment
import com.example.fitandeat.stats.StatsFragment
import com.example.fitandeat.timer.TimerFragment
import com.example.fitandeat.user.UserFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        bottomNav = findViewById(R.id.bottomNavigationView)

        // Mostrar fragmento por defecto
        loadFragment(StatsFragment())

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_stats -> loadFragment(StatsFragment()) // Stats
                R.id.nav_exercises -> loadFragment(ExercisesFragment()) // Exercises
                R.id.nav_timer -> loadFragment(TimerFragment()) // Chronometer
                R.id.nav_routine -> loadFragment(RoutineFragment()) // Routine
                //R.id.nav_profile -> loadFragment(UserFragment()) // Profile
                R.id.nav_food -> loadFragment(FoodFragment()) // Food

            }
            true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_user_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                loadFragment(UserFragment()) // o abre UserActivity si es actividad
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .commit()
    }
}
