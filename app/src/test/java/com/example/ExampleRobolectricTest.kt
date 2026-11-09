package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Google Form", appName)
  }

  @Test
  fun `verify screen bottomNavItems are initialized and non null`() {
    val items = com.example.navigation.Screen.bottomNavItems
    assertEquals(5, items.size)
    items.forEach { screen ->
      org.junit.Assert.assertNotNull("Screen in bottomNavItems should not be null", screen)
      org.junit.Assert.assertNotNull("Screen route should not be null", screen.route)
    }
    assertEquals("today", items[0].route)
    assertEquals("programme", items[1].route)
    assertEquals("transformation", items[2].route)
    assertEquals("analytics", items[3].route)
    assertEquals("coach", items[4].route)
  }
}
