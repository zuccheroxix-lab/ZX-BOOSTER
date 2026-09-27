package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DeviceHardwareInfo
import com.example.model.OptimizationPreset
import com.example.ui.GamingLauncherState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ZX Game Booster", appName)
  }

  @Test
  fun `verify gaming launcher states`() {
    assertEquals("READY", GamingLauncherState.READY.label)
    assertEquals("BOOSTING", GamingLauncherState.BOOSTING.label)
    assertEquals("PLAYING", GamingLauncherState.PLAYING.label)
    assertEquals("SESSION ENDED", GamingLauncherState.SESSION_ENDED.label)
  }

  @Test
  fun `verify optimization presets exist`() {
    val presets = OptimizationPreset.entries
    assertTrue(presets.contains(OptimizationPreset.BALANCED))
    assertTrue(presets.contains(OptimizationPreset.PERFORMANCE))
    assertTrue(presets.contains(OptimizationPreset.BATTERY_SAVER))
  }

  @Test
  fun `verify device hardware info defaults`() {
    val info = DeviceHardwareInfo(
      ramTotalBytes = 8L * 1024L * 1024L * 1024L,
      ramAvailableBytes = 4L * 1024L * 1024L * 1024L,
      ramUsedPercentage = 50,
      batteryPercentage = 80,
      batteryTemperatureCelsius = 32.5f
    )
    assertEquals(50, info.ramUsedPercentage)
    assertNotNull(info.healthSummary)
  }
}

