package com.example.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CategoryFood
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryHousing
import com.example.ui.theme.CategoryLeisure
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategorySupermarket
import com.example.ui.theme.CategoryTransport

enum class ExpenseCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    SUPERMERCADO(
        displayName = "Supermercado",
        icon = Icons.Default.ShoppingCart,
        color = CategorySupermarket
    ),
    TRANSPORTE(
        displayName = "Transporte",
        icon = Icons.Default.DirectionsCar,
        color = CategoryTransport
    ),
    LAZER(
        displayName = "Lazer",
        icon = Icons.Default.SportsEsports,
        color = CategoryLeisure
    ),
    ALIMENTACAO(
        displayName = "Alimentação",
        icon = Icons.Default.Fastfood,
        color = CategoryFood
    ),
    MORADIA(
        displayName = "Moradia & Contas",
        icon = Icons.Default.Home,
        color = CategoryHousing
    ),
    SAUDE(
        displayName = "Saúde",
        icon = Icons.Default.LocalPharmacy,
        color = CategoryHealth
    ),
    OUTROS(
        displayName = "Outros",
        icon = Icons.Default.MoreHoriz,
        color = CategoryOther
    );

    companion object {
        fun fromString(value: String): ExpenseCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OUTROS
        }
    }
}
