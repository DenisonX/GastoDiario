package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val label: String) {
    SYSTEM("Sistema (Auto)"),
    LIGHT("Modo Claro"),
    DARK("Modo Escuro")
}

enum class AppThemePreset(
    val id: String,
    val title: String,
    val description: String,
    val previewPrimary: Color,
    val previewSecondary: Color,
    val previewBackground: Color,
    val previewSurface: Color
) {
    EMERALD(
        id = "EMERALD",
        title = "Esmeralda Verde",
        description = "Clássico financeiro moderno e limpo",
        previewPrimary = Color(0xFF00875A),
        previewSecondary = Color(0xFF36B37E),
        previewBackground = Color(0xFFF4F6F5),
        previewSurface = Color(0xFFFFFFFF)
    ),
    OCEAN_BLUE(
        id = "OCEAN_BLUE",
        title = "Safira Azul",
        description = "Azul oceano corporativo e elegante",
        previewPrimary = Color(0xFF0052CC),
        previewSecondary = Color(0xFF00B8D9),
        previewBackground = Color(0xFFF4F7FC),
        previewSurface = Color(0xFFFFFFFF)
    ),
    AMETHYST_PURPLE(
        id = "AMETHYST_PURPLE",
        title = "Ametista Roxa",
        description = "Estilo fintech roxo vibrante e moderno",
        previewPrimary = Color(0xFF7C3AED),
        previewSecondary = Color(0xFFC084FC),
        previewBackground = Color(0xFFFAF5FF),
        previewSurface = Color(0xFFFFFFFF)
    ),
    SUNSET_GOLD(
        id = "SUNSET_GOLD",
        title = "Pôr do Sol Dourado",
        description = "Tons quentes de âmbar, terracota e ouro",
        previewPrimary = Color(0xFFD97706),
        previewSecondary = Color(0xFFF97316),
        previewBackground = Color(0xFFFFFBEB),
        previewSurface = Color(0xFFFFFFFF)
    ),
    BERRY_ROSE(
        id = "BERRY_ROSE",
        title = "Rosa Framboesa",
        description = "Vibrante, elegante e acolhedor",
        previewPrimary = Color(0xFFE11D48),
        previewSecondary = Color(0xFFF472B6),
        previewBackground = Color(0xFFFFF1F2),
        previewSurface = Color(0xFFFFFFFF)
    ),
    GRAPHITE_DARK(
        id = "GRAPHITE_DARK",
        title = "Grafite Minimalista",
        description = "Monocromático, neutro e sofisticado",
        previewPrimary = Color(0xFF334155),
        previewSecondary = Color(0xFF64748B),
        previewBackground = Color(0xFFF8FAFC),
        previewSurface = Color(0xFFFFFFFF)
    ),
    FOREST_PINE(
        id = "FOREST_PINE",
        title = "Floresta & Lima",
        description = "Tons naturais de pinho e lima orgânica",
        previewPrimary = Color(0xFF2E7D32),
        previewSecondary = Color(0xFF81C784),
        previewBackground = Color(0xFFF1F8E9),
        previewSurface = Color(0xFFFFFFFF)
    );

    val lightColorScheme: ColorScheme
        get() = when (this) {
            EMERALD -> lightColorScheme(
                primary = Color(0xFF00875A),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE3FCEF),
                onPrimaryContainer = Color(0xFF004D40),
                secondary = Color(0xFF36B37E),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFE3FCEF),
                tertiary = Color(0xFF00B8D9),
                background = Color(0xFFF4F6F5),
                onBackground = Color(0xFF172B4D),
                surface = Color(0xFFF9FBFA),
                onSurface = Color(0xFF172B4D),
                surfaceVariant = Color(0xFFFFFFFF),
                onSurfaceVariant = Color(0xFF5E6C84),
                outline = Color(0xFFDFE1E6)
            )
            OCEAN_BLUE -> lightColorScheme(
                primary = Color(0xFF0052CC),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFDEEBFF),
                onPrimaryContainer = Color(0xFF0747A6),
                secondary = Color(0xFF00B8D9),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFE6FCFF),
                tertiary = Color(0xFF6554C0),
                background = Color(0xFFF4F7FC),
                onBackground = Color(0xFF091E42),
                surface = Color(0xFFFAFBFC),
                onSurface = Color(0xFF091E42),
                surfaceVariant = Color(0xFFFFFFFF),
                onSurfaceVariant = Color(0xFF505F79),
                outline = Color(0xFFDFE1E6)
            )
            AMETHYST_PURPLE -> lightColorScheme(
                primary = Color(0xFF7C3AED),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEDE9FE),
                onPrimaryContainer = Color(0xFF5B21B6),
                secondary = Color(0xFF9333EA),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFF3E8FF),
                tertiary = Color(0xFF06B6D4),
                background = Color(0xFFFAF5FF),
                onBackground = Color(0xFF1E1B4B),
                surface = Color(0xFFFCFAFF),
                onSurface = Color(0xFF1E1B4B),
                surfaceVariant = Color(0xFFFFFFFF),
                onSurfaceVariant = Color(0xFF6B7280),
                outline = Color(0xFFE5E7EB)
            )
            SUNSET_GOLD -> lightColorScheme(
                primary = Color(0xFFD97706),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFEF3C7),
                onPrimaryContainer = Color(0xFF92400E),
                secondary = Color(0xFFEA580C),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFFEDD5),
                tertiary = Color(0xFFB45309),
                background = Color(0xFFFFFBEB),
                onBackground = Color(0xFF292524),
                surface = Color(0xFFFFFDF5),
                onSurface = Color(0xFF292524),
                surfaceVariant = Color(0xFFFFFFFF),
                onSurfaceVariant = Color(0xFF78716C),
                outline = Color(0xFFE7E5E4)
            )
            BERRY_ROSE -> lightColorScheme(
                primary = Color(0xFFE11D48),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFE4E6),
                onPrimaryContainer = Color(0xFF9F1239),
                secondary = Color(0xFFDB2777),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFCE7F3),
                tertiary = Color(0xFF9333EA),
                background = Color(0xFFFFF1F2),
                onBackground = Color(0xFF1C1917),
                surface = Color(0xFFFFF8F9),
                onSurface = Color(0xFF1C1917),
                surfaceVariant = Color(0xFFFFFFFF),
                onSurfaceVariant = Color(0xFF71717A),
                outline = Color(0xFFE4E4E7)
            )
            GRAPHITE_DARK -> lightColorScheme(
                primary = Color(0xFF334155),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE2E8F0),
                onPrimaryContainer = Color(0xFF0F172A),
                secondary = Color(0xFF475569),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFF1F5F9),
                tertiary = Color(0xFF0284C7),
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF64748B),
                outline = Color(0xFFCBD5E1)
            )
            FOREST_PINE -> lightColorScheme(
                primary = Color(0xFF2E7D32),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE8F5E9),
                onPrimaryContainer = Color(0xFF1B5E20),
                secondary = Color(0xFF558B2F),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFF1F8E9),
                tertiary = Color(0xFF00796B),
                background = Color(0xFFF1F8E9),
                onBackground = Color(0xFF1B2A1E),
                surface = Color(0xFFF9FDF9),
                onSurface = Color(0xFF1B2A1E),
                surfaceVariant = Color(0xFFFFFFFF),
                onSurfaceVariant = Color(0xFF556B58),
                outline = Color(0xFFD6E3D8)
            )
        }

    val darkColorScheme: ColorScheme
        get() = when (this) {
            EMERALD -> darkColorScheme(
                primary = Color(0xFF36B37E),
                onPrimary = Color(0xFF003822),
                primaryContainer = Color(0xFF0B382A),
                onPrimaryContainer = Color(0xFFE3FCEF),
                secondary = Color(0xFF00B8D9),
                onSecondary = Color(0xFF003640),
                background = Color(0xFF0D1412),
                onBackground = Color(0xFFE8ECEB),
                surface = Color(0xFF121A17),
                onSurface = Color(0xFFE8ECEB),
                surfaceVariant = Color(0xFF182320),
                onSurfaceVariant = Color(0xFFA0AFA9),
                outline = Color(0xFF2D3C37)
            )
            OCEAN_BLUE -> darkColorScheme(
                primary = Color(0xFF4C9AFF),
                onPrimary = Color(0xFF002359),
                primaryContainer = Color(0xFF0747A6),
                onPrimaryContainer = Color(0xFFDEEBFF),
                secondary = Color(0xFF00B8D9),
                onSecondary = Color(0xFF003640),
                background = Color(0xFF0A111E),
                onBackground = Color(0xFFE6EFFC),
                surface = Color(0xFF111A2C),
                onSurface = Color(0xFFE6EFFC),
                surfaceVariant = Color(0xFF18243A),
                onSurfaceVariant = Color(0xFF98A6C0),
                outline = Color(0xFF2C3E5E)
            )
            AMETHYST_PURPLE -> darkColorScheme(
                primary = Color(0xFFA78BFA),
                onPrimary = Color(0xFF2E1065),
                primaryContainer = Color(0xFF5B21B6),
                onPrimaryContainer = Color(0xFFEDE9FE),
                secondary = Color(0xFFC084FC),
                onSecondary = Color(0xFF3B0764),
                background = Color(0xFF0F0B18),
                onBackground = Color(0xFFF3E8FF),
                surface = Color(0xFF181326),
                onSurface = Color(0xFFF3E8FF),
                surfaceVariant = Color(0xFF221A36),
                onSurfaceVariant = Color(0xFFA79BBB),
                outline = Color(0xFF3A2D58)
            )
            SUNSET_GOLD -> darkColorScheme(
                primary = Color(0xFFFBBF24),
                onPrimary = Color(0xFF451A03),
                primaryContainer = Color(0xFF92400E),
                onPrimaryContainer = Color(0xFFFEF3C7),
                secondary = Color(0xFFFB923C),
                onSecondary = Color(0xFF431407),
                background = Color(0xFF18120B),
                onBackground = Color(0xFFFEF3C7),
                surface = Color(0xFF231A10),
                onSurface = Color(0xFFFEF3C7),
                surfaceVariant = Color(0xFF2F2417),
                onSurfaceVariant = Color(0xFFC7B7A3),
                outline = Color(0xFF4D3B26)
            )
            BERRY_ROSE -> darkColorScheme(
                primary = Color(0xFFFB7185),
                onPrimary = Color(0xFF4C0519),
                primaryContainer = Color(0xFF9F1239),
                onPrimaryContainer = Color(0xFFFFE4E6),
                secondary = Color(0xFFF472B6),
                onSecondary = Color(0xFF500724),
                background = Color(0xFF1C0B11),
                onBackground = Color(0xFFFFE4E6),
                surface = Color(0xFF261018),
                onSurface = Color(0xFFFFE4E6),
                surfaceVariant = Color(0xFF341721),
                onSurfaceVariant = Color(0xFFC4A5B1),
                outline = Color(0xFF542435)
            )
            GRAPHITE_DARK -> darkColorScheme(
                primary = Color(0xFF94A3B8),
                onPrimary = Color(0xFF0F172A),
                primaryContainer = Color(0xFF334155),
                onPrimaryContainer = Color(0xFFF1F5F9),
                secondary = Color(0xFFCBD5E1),
                onSecondary = Color(0xFF1E293B),
                background = Color(0xFF0B0F17),
                onBackground = Color(0xFFF8FAFC),
                surface = Color(0xFF131924),
                onSurface = Color(0xFFF8FAFC),
                surfaceVariant = Color(0xFF1C2536),
                onSurfaceVariant = Color(0xFF94A3B8),
                outline = Color(0xFF334155)
            )
            FOREST_PINE -> darkColorScheme(
                primary = Color(0xFF81C784),
                onPrimary = Color(0xFF052E09),
                primaryContainer = Color(0xFF1B5E20),
                onPrimaryContainer = Color(0xFFE8F5E9),
                secondary = Color(0xFFAED581),
                onSecondary = Color(0xFF1A330B),
                background = Color(0xFF0B160C),
                onBackground = Color(0xFFE8F5E9),
                surface = Color(0xFF132215),
                onSurface = Color(0xFFE8F5E9),
                surfaceVariant = Color(0xFF1C301F),
                onSurfaceVariant = Color(0xFFA5BFA8),
                outline = Color(0xFF2D4D32)
            )
        }
}
