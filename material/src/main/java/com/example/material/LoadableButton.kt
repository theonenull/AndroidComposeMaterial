package com.example.material

import androidx.compose.animation.core.animateIntSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoadableButton(
    modifier: Modifier = Modifier,
    buttonState: ButtonState =  rememberSaveable {
        ButtonState.Normal
    },
    normalIntSize : AnimationEffectData = AnimationEffectData(200,50),
    loadingIntSize: AnimationEffectData = AnimationEffectData(50,50),
    onStateChange : (ButtonState)->Unit = {},
    onClick: () -> Unit,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: androidx.compose.material3.ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    normalContent: @Composable RowScope.()->Unit,
    loadingContent: @Composable RowScope.()->Unit,
){

    val translateAnimation = updateTransition(targetState = buttonState, label = "")
    val buttonSize : IntSize by translateAnimation.animateIntSize(
        label = "",
        transitionSpec = { tween(500) }
    ){
        when(it){
            ButtonState.Normal-> normalIntSize.toIntSize()
            ButtonState.Loading -> loadingIntSize.toIntSize()
        }
    }
    LaunchedEffect(buttonState){
        snapshotFlow { buttonState }
            .collectLatest {
                onStateChange.invoke(it)
            }
    }
    Button(
        modifier = modifier
            .size(
                width = buttonSize.width.dp,
                height = buttonSize.height.dp
            ),
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation =elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        when(buttonState){
            ButtonState.Normal-> {
                normalContent()
            }
            ButtonState.Loading -> {
                loadingContent()
            }
        }
    }
}

enum class ButtonState{
    Normal,
    Loading
}
data class AnimationEffectData(
    val width:Int,
    val height:Int
)
fun  AnimationEffectData.toIntSize():IntSize{
    return IntSize(this.width,this.height)
}


@Preview
@Composable
fun LoadButtonPreview(){
    var state by remember {
        mutableStateOf(ButtonState.Normal)
    }
    LoadableButton(
        onClick = {
            state = when(state){
                ButtonState.Loading -> ButtonState.Normal
                ButtonState.Normal->ButtonState.Loading
            }
        },
        buttonState = state,
        normalContent = {
            Text(text = "Normal")
        },
        loadingContent = {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    )
}