package dev.fabik.bluetoothhid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import dev.fabik.bluetoothhid.bt.BluetoothController
import dev.fabik.bluetoothhid.ui.NavGraph
import dev.fabik.bluetoothhid.ui.RequiresBluetoothPermission
import dev.fabik.bluetoothhid.ui.theme.BluetoothHIDTheme
import dev.fabik.bluetoothhid.utils.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val bluetoothController by lazy { BluetoothController(this) }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val theme by rememberPreferenceNull(PreferenceStore.THEME)
            val useDynTheme by rememberPreferenceDefault(PreferenceStore.DYNAMIC_THEME)

            if (theme == null)
                return@setContent

            BluetoothHIDTheme(
                darkTheme = when (theme) {
                    1 -> false
                    2 -> true
                    else -> isSystemInDarkTheme()
                },
                dynamicColor = useDynTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RequiresBluetoothPermission {
                        val context = LocalContext.current
                        val scope = rememberCoroutineScope()

                        NavGraph(bluetoothController)

                        ComposableLifecycle { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_START -> {
                                    if (!bluetoothController.bluetoothEnabled) {
                                        startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                                    }

                                    scope.launch {
                                        if (!bluetoothController.register()) {
                                            Toast.makeText(
                                                context,
                                                getString(R.string.bt_proxy_error),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                                Lifecycle.Event.ON_STOP -> bluetoothController.unregister()
                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}
