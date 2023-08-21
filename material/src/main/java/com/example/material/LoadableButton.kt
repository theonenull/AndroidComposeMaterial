package com.example.material

import androidx.compose.animation.core.animateIntSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonBorder
import androidx.wear.compose.material.ButtonColors
import androidx.wear.compose.material.ButtonDefaults
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
    colors: ButtonColors = ButtonDefaults.primaryButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = CircleShape,
    border: ButtonBorder = ButtonDefaults.buttonBorder(),
    normalContent: @Composable BoxScope.()->Unit,
    loadingContent: @Composable BoxScope.()->Unit,
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
        onClick = onClick,
        
        modifier = modifier
            .size(buttonSize.width.dp, buttonSize.height.dp)
            .clip(shape),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        shape = shape,
        border = border,
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