package com.kevin1031.simplesheet.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp),
    small = RoundedCornerShape(0.dp),
    medium = CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)
