package com.softwavegamess.smgrealestate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.softwavegamess.smgrealestate.domain.model.Property
import com.softwavegamess.smgrealestate.ui.details.DetailsRoute
import com.softwavegamess.smgrealestate.ui.listings.ListingsRoute
import com.softwavegamess.smgrealestate.ui.listings.ListingsViewModel

private const val RouteListings = "listings"
private const val RouteDetails = "details"
private const val ArgPropertyId = "propertyId"

private const val KeySelectedProperty = "selected_property"

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = RouteListings,
    ) {
        composable(RouteListings) { backStackEntry ->
            val listingsViewModel: ListingsViewModel = hiltViewModel(backStackEntry)

            ListingsRoute(
                viewModel = listingsViewModel,
                onOpenDetails = { property ->
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        KeySelectedProperty,
                        property,
                    )
                    navController.navigate("$RouteDetails/${property.id}")
                },
            )
        }

        composable("$RouteDetails/{$ArgPropertyId}") { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString(ArgPropertyId).orEmpty()
            val initialProperty =
                navController.previousBackStackEntry?.savedStateHandle?.get<Property>(KeySelectedProperty)

            DetailsRoute(
                propertyId = propertyId,
                initialProperty = initialProperty,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}

