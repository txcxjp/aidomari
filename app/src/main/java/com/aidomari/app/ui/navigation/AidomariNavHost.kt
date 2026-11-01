package com.aidomari.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aidomari.app.core.tts.TtsManager
import com.aidomari.app.domain.repository.WordsRepository
import com.aidomari.app.ui.deck.DeckListScreen
import com.aidomari.app.ui.deck.DeckViewModel
import com.aidomari.app.ui.study.StudyScreen
import com.aidomari.app.ui.study.StudyViewModel
import com.aidomari.app.ui.subject.SubjectListScreen
import com.aidomari.app.ui.subject.SubjectViewModel

@Composable
fun AidomariNavHost(
    repository: WordsRepository,
    ttsManager: TtsManager,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = "subjects",
        modifier = modifier,
    ) {
        composable("subjects") {
            val subjectViewModel = remember { SubjectViewModel(repository) }
            SubjectListScreen(
                viewModel = subjectViewModel,
                onSubjectClick = { subjectId, subjectName ->
                    navController.navigate("decks/$subjectId/$subjectName")
                },
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

            DeckListScreen(
                subjectName = subjectName,
                viewModel = deckViewModel,
                onBack = { navController.popBackStack() },
                onPartClick = { deckId, partIndex ->
                    navController.navigate("study/$deckId/$partIndex")
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
            val studyViewModel = remember(deckId) { StudyViewModel(repository) }

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
                    val lang = studyViewModel.uiState.value.deck?.let { "auto" } ?: "auto"
                    ttsManager.speak(text, lang)
                },
            )
        }
    }
}
