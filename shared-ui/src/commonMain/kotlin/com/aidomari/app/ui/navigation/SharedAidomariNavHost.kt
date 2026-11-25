package com.aidomari.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aidomari.app.AidomariVersion
import com.aidomari.app.core.ai.GeminiApiClient
import com.aidomari.app.core.ai.GeminiApiClientImpl
import com.aidomari.app.core.ai.GeminiConfigStore
import com.aidomari.app.core.debug.SugorokuSpeedProvider
import com.aidomari.app.core.tts.TtsSpeaker
import com.aidomari.app.domain.repository.SourceArticleRepository
import com.aidomari.app.domain.repository.WordsRepository
import com.aidomari.app.ui.calendar.CalendarScreen
import com.aidomari.app.ui.calendar.CalendarViewModel
import com.aidomari.app.ui.card.CardListScreen
import com.aidomari.app.ui.card.CardListViewModel
import com.aidomari.app.ui.deck.DeckListScreen
import com.aidomari.app.ui.deck.DeckViewModel
import com.aidomari.app.ui.digest.BrowsingDigestScreen
import com.aidomari.app.ui.digest.BrowsingDigestViewModel
import com.aidomari.app.ui.study.StudyScreen
import com.aidomari.app.ui.study.StudyViewModel
import com.aidomari.app.ui.subject.SharedSubjectListScreen
import com.aidomari.app.ui.subject.SubjectViewModel
import com.aidomari.app.ui.theme.AidomariTheme
import com.aidomari.app.ui.theme.AppColorTheme
import com.aidomari.app.ui.theme.LocalThemeConfig
import com.aidomari.app.ui.theme.ThemeConfigStore

private const val TRANSITION_DURATION_MS = 280

/**
 * Shared cross-platform navigation host supporting mobile and desktop screens.
 */
@Composable
fun SharedAidomariNavHost(
    repository: WordsRepository,
    ttsSpeaker: TtsSpeaker,
    themeConfigStore: ThemeConfigStore,
    geminiConfigStore: GeminiConfigStore,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sugorokuSpeedProvider: SugorokuSpeedProvider? = null,
    sourceArticleRepository: SourceArticleRepository? = null,
    browsingDigestViewModel: BrowsingDigestViewModel? = null,
    geminiApiClient: GeminiApiClient = GeminiApiClientImpl(),
    initialRoute: String? = null,
    onInitialRouteHandled: () -> Unit = {},
    appVersion: String = AidomariVersion.DEFAULT_NAME,
    onSugorokuMapClick: (() -> Unit)? = null,
    lockScreenDialogContent: (@Composable (onDismiss: () -> Unit) -> Unit)? = null,
    extraRoutes: NavGraphBuilder.(NavHostController) -> Unit = {},
) {
    LaunchedEffect(initialRoute) {
        if (!initialRoute.isNullOrBlank()) {
            if (navController.currentDestination?.route != initialRoute) {
                navController.navigate(initialRoute) {
                    launchSingleTop = true
                }
            }
            onInitialRouteHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = "subjects",
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION_MS))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION_MS))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
        },
    ) {
        composable("subjects") {
            val subjectViewModel = remember { SubjectViewModel(repository, sourceArticleRepository = sourceArticleRepository) }
            SharedSubjectListScreen(
                viewModel = subjectViewModel,
                onSubjectClick = { subjectId, subjectName ->
                    navController.navigate("decks/$subjectId/$subjectName")
                },
                themeConfigStore = themeConfigStore,
                geminiConfigStore = geminiConfigStore,
                onCalendarClick = {
                    navController.navigate("calendar")
                },
                onSugorokuMapClick = onSugorokuMapClick,
                onBrowsingDigestClick = {
                    navController.navigate("browsing_digest")
                },
                lockScreenDialogContent = lockScreenDialogContent,
                geminiApiClient = geminiApiClient,
                appVersion = appVersion,
            )
        }

        composable("browsing_digest") {
            val digestViewModel = remember(browsingDigestViewModel) {
                browsingDigestViewModel ?: BrowsingDigestViewModel(
                    sourceArticleRepository = sourceArticleRepository
                        ?: error("SourceArticleRepository is required"),
                    wordsRepository = repository,
                    geminiApiClient = geminiApiClient,
                    geminiPreferences = geminiConfigStore,
                )
            }
            BrowsingDigestScreen(
                viewModel = digestViewModel,
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate("subjects") {
                            popUpTo("browsing_digest") { inclusive = true }
                        }
                    }
                },
                geminiConfigStore = geminiConfigStore,
                geminiApiClient = geminiApiClient,
            )
        }

        composable("calendar") {
            val calendarViewModel = remember { CalendarViewModel(repository) }
            CalendarScreen(
                viewModel = calendarViewModel,
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(
            route = "decks/{subjectId}/{subjectName}",
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("subjectName") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: return@composable
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: "単語帳"
            val deckViewModel = remember(subjectId) { DeckViewModel(subjectId, repository) }
            val subject by deckViewModel.subject.collectAsState()
            val currentConfig = LocalThemeConfig.current
            val subjectColorTheme = subject?.colorTheme?.let { runCatching { AppColorTheme.valueOf(it) }.getOrNull() }
            val effectiveConfig = if (subjectColorTheme != null) {
                currentConfig.copy(colorTheme = subjectColorTheme)
            } else {
                currentConfig
            }

            AidomariTheme(themeConfig = effectiveConfig) {
                DeckListScreen(
                    subjectName = subjectName,
                    viewModel = deckViewModel,
                    onBack = { navController.popBackStack() },
                    onPartClick = { deckId, partIndex ->
                        navController.navigate("study/$deckId/$partIndex")
                    },
                    onSpeakText = { text ->
                        ttsSpeaker.speak(text)
                    },
                    onManageCardsClick = { deckId ->
                        navController.navigate("deck/$deckId/cards")
                    },
                )
            }
        }

        composable(
            route = "deck/{deckId}/cards",
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: return@composable
            val cardListViewModel = remember(deckId) { CardListViewModel(deckId, repository) }

            CardListScreen(
                viewModel = cardListViewModel,
                onBack = { navController.popBackStack() },
                onSpeakText = { text ->
                    val deck = cardListViewModel.uiState.value.deck
                    val rate = deck?.speechRate ?: 1.0f
                    ttsSpeaker.setSpeechRate(rate)
                    ttsSpeaker.speak(text, "auto")
                },
            )
        }

        composable(
            route = "study/{deckId}/{partIndex}",
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
                navArgument("partIndex") { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: return@composable
            val partIndex = backStackEntry.arguments?.getInt("partIndex") ?: 0
            val studyViewModel = remember(deckId) { StudyViewModel(repository, sugorokuSpeedProvider) }
            val state by studyViewModel.uiState.collectAsState()
            val currentConfig = LocalThemeConfig.current
            val subjectColorTheme = state.subject?.colorTheme?.let { runCatching { AppColorTheme.valueOf(it) }.getOrNull() }
            val effectiveConfig = if (subjectColorTheme != null) {
                currentConfig.copy(colorTheme = subjectColorTheme)
            } else {
                currentConfig
            }

            AidomariTheme(themeConfig = effectiveConfig) {
                StudyScreen(
                    deckId = deckId,
                    partIndex = partIndex,
                    viewModel = studyViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPart = { nextPartIndex ->
                        navController.navigate("study/$deckId/$nextPartIndex") {
                            popUpTo("study/$deckId/$partIndex") { inclusive = true }
                        }
                    },
                    onSpeakText = { text ->
                        val deck = studyViewModel.uiState.value.deck
                        val rate = deck?.speechRate ?: 1.0f
                        ttsSpeaker.setSpeechRate(rate)
                        ttsSpeaker.speak(text, "auto")
                    },
                )
            }
        }

        extraRoutes(navController)
    }
}
