package com.gastro.nameet.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gastro.nameet.ui.theme.PrimaryOrange

/**
 * OutlinedTextField pre-styled for the dark auth background. White typed
 * text + a translucent black container so the value doesn't disappear into
 * the photo backdrop on the Login / Register screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = PrimaryOrange,
            focusedBorderColor = PrimaryOrange,
            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
            focusedLabelColor = PrimaryOrange,
            unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
            focusedContainerColor = Color.Black.copy(alpha = 0.35f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.25f)
        )
    )
}
