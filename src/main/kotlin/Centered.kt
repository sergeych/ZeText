package net.sergeych.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed


@Composable
fun Centered(modifier: Modifier = Modifier,block: @Composable ()->Unit) {
    Column(modifier = modifier.composed {  fillMaxWidth().fillMaxHeight() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        block()
    }
}
