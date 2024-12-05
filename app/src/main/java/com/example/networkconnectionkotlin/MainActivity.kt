package com.example.networkconnectionkotlin

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var btnFetchData: Button
    private lateinit var tvResult: TextView

    val url = URL("https://jsonplaceholder.typicode.com/todos")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnFetchData = findViewById(R.id.btnFetchData)
        tvResult = findViewById(R.id.tvResult)

        btnFetchData.setOnClickListener {
            if (isInternetAvailable()) {
                fetchData()
            } else {
                tvResult.text = "No Internet Connection"
                Toast.makeText(this, "No internet connection available.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = makeNetworkRequest()
                withContext(Dispatchers.Main) {
                    tvResult.text = result
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    handleException(e)
                }
            }
        }
    }

    private fun makeNetworkRequest(): String {

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        return try {
            if (connection.responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                throw IOException("Unable to fetch data: ${connection.responseMessage}")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun handleException(e: IOException) {
        tvResult.text = "Failed to fetch data"
        Toast.makeText(this, e.message ?: "An error occurred", Toast.LENGTH_SHORT).show()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}