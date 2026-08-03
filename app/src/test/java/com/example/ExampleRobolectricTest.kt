package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Rahman Fast Food", appName)
  }

  @Test
  fun `verify profile update and rewards balance`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.DatabaseService(context)
    
    // Check initial user values
    assertEquals("Hamza Rahman", db.currentUserName.value)
    assertEquals("03217654321", db.currentUserPhone.value)
    
    // Perform update
    db.updateProfile("Muhammad Ali", "03001234567", "Gulberg III, Lahore")
    assertEquals("Muhammad Ali", db.currentUserName.value)
    assertEquals("03001234567", db.currentUserPhone.value)
    assertEquals("Gulberg III, Lahore", db.currentUserAddress.value)
    
    // Check loyalty points starting balance
    assertEquals(350, db.currentUserLoyaltyPoints.value)
    
    // Earn loyalty points
    db.earnLoyaltyPoints(50)
    assertEquals(400, db.currentUserLoyaltyPoints.value)
  }

  @Test
  fun `verify FCM token update and payload simulation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.DatabaseService.getInstance(context)

    // Token default checks
    assertEquals("fcm_sandbox_token_rahman_fast_food_786", db.fcmToken.value)

    // Update Token
    db.updateFcmToken("fcm_token_test_abc123")
    assertEquals("fcm_token_test_abc123", db.fcmToken.value)

    // Verify inbound simulation payload logs
    assertEquals(0, db.fcmPayloads.value.size)

    db.simulateIncomingFcmMessage(context, "Promo Alert Code", "Get 20% off!", "channel_promotional")
    
    assertEquals(1, db.fcmPayloads.value.size)
    assertEquals("Promo Alert Code", db.fcmPayloads.value[0].title)
    assertEquals("Get 20% off!", db.fcmPayloads.value[0].body)
    assertEquals("Exclusive Promo", db.fcmPayloads.value[0].type)
  }
}
