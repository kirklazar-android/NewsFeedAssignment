package com.newsfeedassignment

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeNavigatesToSearch() {
        if (onAllNodesWithText("Not now").fetchSemanticsNodes().isNotEmpty()) {
            onNodeWithText("Not now").performClick()
        }
        onNodeWithText("Home").assertExists()
        onNodeWithText("Search").performClick()
        onNodeWithText("Search news").assertExists()
    }
}
