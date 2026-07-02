package com.kevin.secretsanta.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun SecretSantaApp(factory: ViewModelFactory) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "groupList") {

        composable("groupList") {
            GroupListScreen(
                factory = factory,
                onGroupClick = { id -> navController.navigate("groupDetail/$id") }
            )
        }

        composable(
            "groupDetail/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: -1L
            GroupDetailScreen(
                groupId = groupId,
                factory = factory,
                onEditParticipant = { pid -> navController.navigate("editParticipant/$pid") },
                onManageExclusions = { navController.navigate("exclusions/$groupId") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "editParticipant/{participantId}",
            arguments = listOf(navArgument("participantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val participantId = backStackEntry.arguments?.getLong("participantId") ?: -1L
            EditParticipantScreen(
                participantId = participantId,
                factory = factory,
                onDone = { navController.popBackStack() }
            )
        }

        composable(
            "exclusions/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: -1L
            ExclusionsScreen(
                groupId = groupId,
                factory = factory,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
