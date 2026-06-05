package com.hanshow.mapExample.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.hanshow.mapExample.data.model.map.MapDataResponse
import com.hanshow.mapExample.data.model.map.MapEdge
import com.hanshow.mapExample.data.model.map.MapNode

@Composable
fun MapCanvas(
    mapData: MapDataResponse,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        val scaleFactor = minOf(
            size.width / mapData.width,
            size.height / mapData.height
        ) * 0.85f // 留点边距

        val offsetX = (size.width - mapData.width * scaleFactor) / 2f
        val offsetY = (size.height - mapData.height * scaleFactor) / 2f

        // 绘制连线（先画线再画节点，线在节点下面）
        for (edge in mapData.edges) {
            val fromNode = mapData.nodes.find { it.id == edge.fromId }
            val toNode = mapData.nodes.find { it.id == edge.toId }
            if (fromNode != null && toNode != null) {
                val from = scalePoint(fromNode, scaleFactor, offsetX, offsetY)
                val to = scalePoint(toNode, scaleFactor, offsetX, offsetY)

                val lineColor = when (edge.style) {
                    "dashed" -> Color.Gray
                    else -> Color(0xFF90CAF9) // 浅蓝色连线
                }

                val pathEffect = when (edge.style) {
                    "dashed" -> PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    else -> null
                }

                drawLine(
                    color = lineColor,
                    start = from,
                    end = to,
                    strokeWidth = 3f,
                    pathEffect = pathEffect
                )
            }
        }

        // 绘制节点
        for (node in mapData.nodes) {
            val center = scalePoint(node, scaleFactor, offsetX, offsetY)
            val nodeRadius = 24f * scaleFactor.coerceAtLeast(0.5f)

            val nodeColor = when (node.type) {
                "highlight" -> Color(0xFF4CAF50) // 绿色 - 入口/出口
                "warning" -> Color(0xFFFF9800)  // 橙色 - 警告
                else -> Color(0xFF2196F3)       // 蓝色 - 默认
            }

            // 节点圆形
            drawCircle(
                color = nodeColor,
                radius = nodeRadius,
                center = center
            )

            // 白色边框
            drawCircle(
                color = Color.White,
                radius = nodeRadius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // 节点标签文字
            drawText(
                textMeasurer = textMeasurer,
                text = node.label,
                topLeft = Offset(
                    center.x - textMeasurer.measure(node.label).size.width / 2f,
                    center.y + nodeRadius + 6f
                ),
                style = androidx.compose.ui.text.TextStyle(
                    color = Color.DarkGray,
                    fontSize = 12.sp
                )
            )
        }
    }
}

private fun DrawScope.scalePoint(
    node: MapNode,
    scaleFactor: Float,
    offsetX: Float,
    offsetY: Float
): Offset = Offset(
    x = node.x * scaleFactor + offsetX,
    y = node.y * scaleFactor + offsetY
)