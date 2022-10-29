package dev.fabik.bluetoothhid

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.fabik.bluetoothhid.ui.*
import dev.fabik.bluetoothhid.utils.PreferenceStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(12.dp, 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        stringResource(R.string.connection),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    SwitchPreference(
                        title = stringResource(R.string.auto_connect),
                        desc = stringResource(R.string.auto_connect_desc),
                        icon = Icons.Default.Link,
                        preference = PreferenceStore.AUTO_CONNECT
                    )
                }

                item {
                    SwitchPreference(
                        title = stringResource(R.string.show_unnamed),
                        desc = stringResource(R.string.show_unnamed_desc),
                        icon = Icons.Default.DeviceUnknown,
                        preference = PreferenceStore.SHOW_UNNAMED
                    )
                }

                item {
                    SliderPreference(
                        title = stringResource(R.string.send_delay),
                        desc = stringResource(R.string.send_delay_desc),
                        valueFormat = stringResource(R.string.send_delay_template),
                        range = 0f..100f,
                        icon = Icons.Default.Timer,
                        preference = PreferenceStore.SEND_DELAY
                    )
                }

                item {
                    ComboBoxPreference(
                        title = stringResource(R.string.keyboard_layout),
                        desc = stringResource(R.string.keyboard_layout_desc),
                        icon = Icons.Default.Keyboard,
                        values = stringArrayResource(R.array.keyboard_layout_values),
                        preference = PreferenceStore.KEYBOARD_LAYOUT
                    )
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.appearance),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    ComboBoxPreference(
                        title = stringResource(R.string.theme),
                        desc = stringResource(R.string.theme_desc),
                        icon = Icons.Default.AutoFixHigh,
                        values = stringArrayResource(R.array.theme_values),
                        preference = PreferenceStore.THEME
                    )
                }

                item {
                    SwitchPreference(
                        title = stringResource(R.string.dynamic_theme),
                        desc = stringResource(R.string.dynamic_theme_desc),
                        icon = Icons.Default.AutoAwesome,
                        preference = PreferenceStore.DYNAMIC_THEME
                    )
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.scanner),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    CheckBoxPreference(
                        title = stringResource(R.string.code_types),
                        desc = stringResource(R.string.code_types_desc),
                        valueStrings = stringArrayResource(R.array.code_types_values),
                        icon = Icons.Default.QrCode2,
                        preference = PreferenceStore.CODE_TYPES
                    )
                }

                item {
                    SwitchPreference(
                        title = stringResource(R.string.front_camera),
                        desc = stringResource(R.string.front_camera_desc),
                        icon = Icons.Default.FlipCameraAndroid,
                        preference = PreferenceStore.FRONT_CAMERA
                    )
                }

                item {
                    SwitchPreference(
                        title = stringResource(R.string.restrict_area),
                        desc = stringResource(R.string.restrict_area_desc),
                        icon = Icons.Default.CropFree,
                        preference = PreferenceStore.RESTRICT_AREA
                    )
                }

                item {
                    ComboBoxPreference(
                        title = stringResource(R.string.overlay_type),
                        desc = stringResource(R.string.overlay_type_desc),
                        icon = Icons.Default.CenterFocusWeak,
                        values = stringArrayResource(R.array.overlay_values),
                        preference = PreferenceStore.OVERLAY_TYPE
                    )
                }

                item {
                    SwitchPreference(
                        title = stringResource(R.string.full_inside),
                        desc = stringResource(R.string.full_inside_desc),
                        icon = Icons.Default.QrCodeScanner,
                        preference = PreferenceStore.FULL_INSIDE
                    )
                }

                item {
                    ComboBoxPreference(
                        title = stringResource(R.string.scan_freq),
                        desc = stringResource(R.string.scan_freq_desc),
                        icon = Icons.Default.ShutterSpeed,
                        values = stringArrayResource(R.array.scan_freq_values),
                        preference = PreferenceStore.SCAN_FREQUENCY
                    )
                }

                item {
                    ComboBoxPreference(
                        title = stringResource(R.string.scan_res),
                        desc = stringResource(R.string.scan_res_desc),
                        icon = Icons.Default.Hd,
                        values = stringArrayResource(R.array.scan_res_values),
                        preference = PreferenceStore.SCAN_RESOLUTION
                    )
                }

                item {
                    SwitchPreference(
                        title = stringResource(R.string.auto_send),
                        desc = stringResource(R.string.auto_send_desc),
                        icon = Icons.Default.Send,
                        preference = PreferenceStore.AUTO_SEND
                    )
                }

                item {
                    ComboBoxPreference(
                        title = stringResource(R.string.extra_keys),
                        desc = stringResource(R.string.extra_keys_desc),
                        icon = Icons.Default.AddCircle,
                        values = stringArrayResource(R.array.extra_keys_values),
                        preference = PreferenceStore.EXTRA_KEYS
                    )
                }

                item {
                    SwitchPreference(
                        title = stringResource(R.string.play_sound),
                        desc = stringResource(R.string.play_sound_desc),
                        icon = Icons.Default.VolumeUp,
                        preference = PreferenceStore.PLAY_SOUND
                    )
                }

                item {
                    SwitchPreference(
                        title = stringResource(R.string.haptic_feedback),
                        desc = stringResource(R.string.haptic_feedback_desc),
                        icon = Icons.Default.Vibration,
                        preference = PreferenceStore.VIBRATE
                    )
                }

                item {
                    SwitchPreference(
                        title = stringResource(R.string.raw_value),
                        desc = stringResource(R.string.raw_value_desc),
                        icon = Icons.Default.Description,
                        preference = PreferenceStore.RAW_VALUE
                    )
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.about), color = MaterialTheme.colorScheme.primary)
                }

                item {
                    val uriHandler = LocalUriHandler.current

                    ButtonPreference(
                        title = stringResource(R.string.repository),
                        desc = "https://github.com/Fabi019/hid-barcode-scanner",
                        icon = Icons.Default.Code
                    ) {
                        uriHandler.openUri("https://github.com/Fabi019/hid-barcode-scanner")
                    }
                }

                item {
                    ButtonPreference(
                        title = stringResource(R.string.build_version),
                        desc = stringResource(
                            R.string.build_version_desc,
                            BuildConfig.BUILD_TYPE,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.FLAVOR,
                            BuildConfig.VERSION_CODE
                        ),
                        icon = Icons.Default.Info
                    )

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
