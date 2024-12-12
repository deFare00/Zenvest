package com.example.navigationbarkotlin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.navigationbarkotlin.R
import com.example.zenvest.Fragments.HomeFragment
import com.example.zenvest.News.NewsFragment
import com.example.zenvest.Sizing.SizingFragment
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigation = findViewById<CurvedBottomNavigation>(R.id.bottomNavigation)
        bottomNavigation.add(
            CurvedBottomNavigation.Model(1, "News", R.drawable.news)
        )
        bottomNavigation.add(
            CurvedBottomNavigation.Model(2, "Home", R.drawable.home_icon)
        )
        bottomNavigation.add(
            CurvedBottomNavigation.Model(3, "Sizing", R.drawable.sizing)
        )

        bottomNavigation.setOnClickMenuListener {
            when(it.id) {
                1 -> {
                    replaceFragment(NewsFragment())
                }
                2 -> {
                    replaceFragment(HomeFragment())
                }
                3 -> {
                    replaceFragment(SizingFragment())
                }
            }
        }
        replaceFragment(HomeFragment())
        bottomNavigation.show(2)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_layout, fragment)
            .commit()
    }
}