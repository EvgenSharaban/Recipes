package com.example.recipes.presentation.ui.recipes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.recipes.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle the splash screen transition.
        installSplashScreen()

        setContentView(R.layout.activity_recipes)
    }
}