package com.example.material
/**
    大部分代码来自掘金大佬 米奇律师
    仅稍作修改
    https://juejin.cn/post/7229678870703702053
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


@OptIn(ExperimentalTextApi::class)
@Composable
fun ScrollSelection(
    modifier: Modifier = Modifier,
    textList : List<String>,
    textStyle : TextStyle = MaterialTheme.typography.titleLarge,
    padding : PaddingValues = PaddingValues(top = 14.dp, bottom = 14.dp),
    state: LazyListState = rememberLazyListState(),
    unselectedScale: Float = 0.70F,
    selectedScale: Float = 1.0F,
    onItemSelected: (index: Int, content: String) -> Unit = { _, _ -> },
    backgroundContent:@Composable (BoxScope.()->Unit)?= {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(217, 217, 238))
        )
    }
){
    val currentUnselectedScale by rememberUpdatedState(newValue = unselectedScale)
    val currentSelectedScale by rememberUpdatedState(newValue = selectedScale)
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val contentHeight = remember(textStyle, padding) {
        val textContentHeight = textMeasurer.measure(AnnotatedString(""), textStyle).size.height
        val topPadding = padding.calculateTopPadding()
        val bottomPadding = padding.calculateBottomPadding()
        with(density) { (topPadding + bottomPadding).toPx() } + textContentHeight
    }
    val scrollingOutScale = remember { mutableStateOf(selectedScale) }
    val scrollingInScale = remember { mutableStateOf(unselectedScale) }
    var firstVisibleItemIndex by remember { mutableStateOf(state.firstVisibleItemIndex) }
//    val firstVisibleItemIndex by remember {
//        derivedStateOf {
//            state.firstVisibleItemIndex
//        }
//    }

    LaunchedEffect(state){
        snapshotFlow{state.firstVisibleItemScrollOffset}
            .onEach { firstVisibleItemScrollOffset ->
                val progress = firstVisibleItemScrollOffset.toFloat() / contentHeight
                val disparity = (currentSelectedScale - currentUnselectedScale) * progress
                scrollingOutScale.value = currentSelectedScale - disparity
                scrollingInScale.value = currentUnselectedScale + disparity
            }
            .launchIn(this)
        snapshotFlow { state.firstVisibleItemIndex }
            .filter { it != firstVisibleItemIndex }
            .onEach { firstVisibleItemIndex = it }
            .launchIn(this)

        launch {
            var lastInteraction: Interaction? = null
            state.interactionSource.interactions.mapNotNull {
                it as? DragInteraction
            }.map { interaction ->
                // 滑动结束或取消时，判断是否需要复位
                val currentStart = (interaction as? DragInteraction.Stop)?.start
                    ?: (interaction as? DragInteraction.Cancel)?.start
                val needReset = currentStart == lastInteraction
                lastInteraction = interaction
                needReset
            }.combine( snapshotFlow { state.isScrollInProgress } ) { needReset, isScrollInProgress ->
                needReset && !isScrollInProgress
            }.filter {
                it
            }.collectLatest {
                val halfHeight = contentHeight / 2
                val selectedIndex = if (state.firstVisibleItemScrollOffset < halfHeight) {
                    // 若滑动距离小于一半，则回滚到上一个item
                    firstVisibleItemIndex
                } else {
                    // 若滑动距离大于一半，则滚动到下一个item
                    firstVisibleItemIndex + 1
                }
                if (selectedIndex < textList.size) {
                    onItemSelected(selectedIndex, textList[selectedIndex])
                }
                state.animateScrollToItem(selectedIndex)
            }
        }
    }

    Box(modifier = modifier){
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        ){
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(
                        vertical = with(density) {
                            (contentHeight * 1).toDp()
                        }
                    )
                    .align(Alignment.Center)

            ){
                if (backgroundContent != null) {
                    backgroundContent()
                }
            }
            LazyColumn(
                modifier = Modifier
                    .height(
                        with(density) {
                            (contentHeight * 3).toDp()
                        }
                    )
                    .fillMaxWidth(),
                state = state,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                with(density) {
                                    contentHeight.toDp()
                                }
                            )
                    )
                }
                items(
                    textList.size,
                    key = { textList[it] }
                ) { index ->
                    Box(
                        modifier = Modifier.defaultMinSize(minWidth = 42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = textList[index],
                            style = textStyle,
                            modifier = Modifier
                                .scale(
                                    when (index) {
                                        firstVisibleItemIndex -> scrollingOutScale.value
                                        firstVisibleItemIndex + 1 -> scrollingInScale.value
                                        else -> currentUnselectedScale
                                    }
                                )
                                .padding(padding),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                with(density) {
                                    contentHeight.toDp()
                                }
                            )
                    )
                }
            }

        }
    }

}
