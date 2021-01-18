package com.jaimegc.covid19tracker.activity.kakao

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.jaimegc.covid19tracker.R
import com.jaimegc.covid19tracker.ui.home.MainActivity
import com.jaimegc.covid19tracker.ui.world.WorldFragment
import com.jaimegc.covid19tracker.utils.matchers.BottomNavigationViewMenuItemMatcher.Companion.bottomNavigationViewHasMenuItemChecked
import com.google.common.truth.Truth.assertThat
import com.jaimegc.covid19tracker.data.room.Covid19TrackerDatabase
import com.jaimegc.covid19tracker.utils.kakao.MainScreen
import com.jaimegc.covid19tracker.ui.country.CountryFragment
import com.jaimegc.covid19tracker.ui.home.MainViewModel
import io.mockk.mockk
import io.mockk.mockkClass
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.mock.MockProviderRule

@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityKakaoTest : KoinTest {

    private lateinit var scenario: ActivityScenario<MainActivity>
    private val viewModel = mockk<MainViewModel>(relaxed = true)
    private lateinit var mockModule: Module

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        mockkClass(clazz)
    }

    @Before
    fun setUp() {
        mockModule = module(createdAtStart = true, override = true) {
            single {
                Covid19TrackerDatabase.buildTest(get())
            }
            single {
                viewModel
            }
        }
    }

    @Test
    fun openActivity_shouldShowCountryFragmentByDefault() {
        onScreen<MainScreen> {
            loadKoinModules(mockModule)

            scenario = ActivityScenario.launch(MainActivity::class.java)

            scenario.onActivity { activity ->
                val fragments =
                    activity.supportFragmentManager.fragments.first().childFragmentManager.fragments
                assertThat(fragments.size).isEqualTo(1)
                assertThat(fragments[0]::class.java).isEqualTo(CountryFragment::class.java)
            }
            navigationView {
                view.check(
                    matches(bottomNavigationViewHasMenuItemChecked(R.id.navigation_country))
                )
            }

            scenario.close()
        }
    }

    @Test
    fun clickOnNavigationWorld_shouldShowWorldFragment() {
        onScreen<MainScreen> {
            loadKoinModules(mockModule)

            scenario = ActivityScenario.launch(MainActivity::class.java)

            navigationView {
                setSelectedItem(R.id.navigation_world)
                scenario.onActivity { activity ->
                    val fragments =
                        activity.supportFragmentManager.fragments.first().childFragmentManager.fragments
                    assertThat(fragments.size).isEqualTo(2)
                    assertThat(fragments[1]::class.java).isEqualTo(WorldFragment::class.java)
                }
                view.check(
                    matches(bottomNavigationViewHasMenuItemChecked(R.id.navigation_world))
                )
            }

            scenario.close()
        }
    }

    @Test
    fun pressBackButtonInNavigationWorld_shouldShowNavigationCountry() {
        onScreen<MainScreen> {
            val screen = this
            loadKoinModules(mockModule)

            scenario = ActivityScenario.launch(MainActivity::class.java)

            navigationView {
                setSelectedItem(R.id.navigation_world)

                inRoot {
                    screen.pressBack()
                }

                view.check(
                    matches(bottomNavigationViewHasMenuItemChecked(R.id.navigation_country))
                )
            }

            scenario.close()
        }
    }
}