package com.example.midterm_proj
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.Intent
import android.widget.Button
import android.util.Log
import android.widget.TextView
import com.example.midterm_proj.data.WeatherResponse
import com.example.midterm_proj.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Welcome : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome)

        val button = findViewById<Button>(R.id.getStartedButton)
        button.setOnClickListener {
            val intent = Intent(this, CitySearchActivity::class.java)
            startActivity(intent)
        }
    }
}


