package com.example.c001apk.compose.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.c001apk.compose.ui.component.SlideTransition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTransitionTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun toolbarBackDrawsIntermediateFrames() = verifyReturnAnimation(systemBack = false)

    @Test
    fun systemBackDrawsIntermediateFrames() = verifyReturnAnimation(systemBack = true)

    private fun verifyReturnAnimation(systemBack: Boolean) {
        lateinit var controller: NavHostController
        lateinit var activity: ComponentActivity
        var mainCreated = 0
        var mainDisposed = 0
        compose.setContent {
            activity = LocalContext.current as ComponentActivity
            controller = rememberNavController()
            MaterialTheme {
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    FeedSplitLayout(
                        showDetail = false,
                        detailContent = {},
                        mainBackground = {
                            RetainedMainScreen(controller) {
                                DisposableEffect(Unit) {
                                    mainCreated++
                                    onDispose { mainDisposed++ }
                                }
                                Box(Modifier.fillMaxSize().background(Color.Red))
                            }
                        },
                    ) {
                        NavHost(
                            navController = controller,
                            startDestination = Router.MAIN.name,
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = { SlideTransition.slideLeft.enterTransition() },
                            exitTransition = { SlideTransition.slideLeft.exitTransition() },
                            popEnterTransition = { SlideTransition.slideRight.enterTransition() },
                            popExitTransition = { SlideTransition.slideRight.exitTransition() },
                        ) {
                            composable(Router.MAIN.name) {}
                            composable("detail") {
                                Box(Modifier.fillMaxSize().background(Color.Blue))
                            }
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.waitUntil(timeoutMillis = 10_000) { activity.hasWindowFocus() }
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { controller.navigate("detail") }
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(1, mainCreated)
            assertEquals(0, mainDisposed)
            if (systemBack) activity.onBackPressedDispatcher.onBackPressed()
            else controller.popBackStack()
        }
        compose.mainClock.advanceTimeBy(120)
        compose.waitForIdle()
        val frame = compose.onRoot().captureToImage().toPixelMap()
        val center = frame[frame.width / 2, frame.height / 2]
        assertTrue(
            "Return animation must blend the outgoing and incoming pages: $center",
            center.red > 0.02f && center.blue > 0.02f && center.green < 0.02f,
        )
        compose.mainClock.advanceTimeBy(1_000)
        compose.runOnIdle {
            assertEquals(Router.MAIN.name, controller.currentDestination?.route)
            assertEquals(1, mainCreated)
            assertEquals(0, mainDisposed)
        }
    }
}
