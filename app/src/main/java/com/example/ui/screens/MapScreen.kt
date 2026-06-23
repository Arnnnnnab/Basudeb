package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RathaYatraViewModel
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.GoldAccent

data class RathTrackerState(
    val id: String,
    val name: String,
    val deity: String,
    val color: Color,
    val accentColor: Color,
    val initial: Char,
    val flagEmoji: String,
    val description: String,
    val specHeight: String,
    val specWheels: Int,
    var routeIndex: Int,
    var battery: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: RathaYatraViewModel) {
    val context = LocalContext.current
    
    // Zoom and Pan states for the interactive canvas viewport
    var scale by remember { mutableFloatStateOf(1.2f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Simulation nodes along GT Road
    val routeNodes = remember {
        listOf(
            Pair(23.6821, 87.0112), // Start: Budha ISKCON Temple
            Pair(23.6845, 87.0145), // GT Road Crossing Intersection
            Pair(23.6872, 87.0180), // Police Chowki Station
            Pair(23.6901, 87.0211), // Grand Food Halt
            Pair(23.6934, 87.0249), // Finish: Polo Ground Event Location
        )
    }

    val nodeLabels = listOf(
        "Budha ISKCON Temple (Start)",
        "GT Road Crossing",
        "Police Chowki",
        "Grand Food Halt",
        "Polo Ground (Resting Pandal)"
    )

    // Three majestic chariots trackers setup
    var rath1Index by remember { mutableIntStateOf(2) } // Lord Jagannatha
    var rath2Index by remember { mutableIntStateOf(1) } // Lord Baladeva
    var rath3Index by remember { mutableIntStateOf(0) } // Lady Subhadra

    var rath1Battery by remember { mutableIntStateOf(95) }
    var rath2Battery by remember { mutableIntStateOf(88) }
    var rath3Battery by remember { mutableIntStateOf(91) }

    val jagannathaRath = RathTrackerState(
        id = "rath_j",
        name = "Nandighosa",
        deity = "Lord Jagannatha",
        color = Color(0xFFFFC107), // Saffron gold
        accentColor = SaffronDark,
        initial = 'J',
        flagEmoji = "💛",
        description = "Adorned with gold & yellow wrap, representing the cosmic master of loving service.",
        specHeight = "45 feet",
        specWheels = 16,
        routeIndex = rath1Index,
        battery = rath1Battery
    )

    val baladevaRath = RathTrackerState(
        id = "rath_b",
        name = "Taladhwaja",
        deity = "Sri Baladeva",
        color = Color(0xFF4CAF50), // Majestic Forest Green
        accentColor = Color(0xFF2E7D32),
        initial = 'B',
        flagEmoji = "💚",
        description = "Carries Lord Balarama. Blue and green canopy represent immense strength & cosmic safety.",
        specHeight = "44 feet",
        specWheels = 14,
        routeIndex = rath2Index,
        battery = rath2Battery
    )

    val subhadraRath = RathTrackerState(
        id = "rath_s",
        name = "Devadalana",
        deity = "Srimati Subhadra",
        color = Color(0xFFE91E63), // Royal Pinkish Crimson
        accentColor = Color(0xFFC2185B),
        initial = 'S',
        flagEmoji = "💗",
        description = "Subhadra Devi's chariot. Black and red shields representing dynamic shielding and protection.",
        specHeight = "43 feet",
        specWheels = 12,
        routeIndex = rath3Index,
        battery = rath3Battery
    )

    val raths = listOf(jagannathaRath, baladevaRath, subhadraRath)
    var selectedRathId by remember { mutableStateOf("rath_j") }
    val activeRath = raths.find { it.id == selectedRathId } ?: jagannathaRath

    // Helper functions: Map coordinates to canvas pixels
    // We choose bounding coordinates for Asansol section tightly encompassing the spots:
    // Latitude: 23.6740 to 23.6960 (span ~0.022)
    // Longitude: 87.0080 to 87.0280 (span ~0.020)
    val mapMinLat = 23.6730
    val mapMaxLat = 23.6970
    val mapMinLon = 87.0060
    val mapMaxLon = 87.0300

    fun projectToCanvas(lat: Double, lon: Double, width: Float, height: Float): Offset {
        val pctX = (lon - mapMinLon) / (mapMaxLon - mapMinLon)
        val pctY = 1.0 - (lat - mapMinLat) / (mapMaxLat - mapMinLat) // top to bottom flip

        val lx = (pctX.toFloat() * (width - 100f) + 50f) * scale + offsetX
        val ly = (pctY.toFloat() * (height - 100f) + 50f) * scale + offsetY
        return Offset(lx, ly)
    }

    // Function to center map on certain lat/lon
    fun centerOn(lat: Double, lon: Double, canvasWidth: Float, canvasHeight: Float) {
        scale = 1.5f
        val pctX = (lon - mapMinLon) / (mapMaxLon - mapMinLon)
        val pctY = 1.0 - (lat - mapMinLat) / (mapMaxLat - mapMinLat)

        val targetPixelX = (pctX.toFloat() * (canvasWidth - 100f) + 50f) * scale
        val targetPixelY = (pctY.toFloat() * (canvasHeight - 100f) + 50f) * scale

        offsetX = (canvasWidth / 2f) - targetPixelX
        offsetY = (canvasHeight / 2f) - targetPixelY
    }

    // Measure tool for vector text overlays
    val textMeasurer = rememberTextMeasurer()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Interactive Ratha Map",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Text(
                            text = "Gaining divine coordinates on Grand Trunk Road",
                            style = MaterialTheme.typography.bodySmall,
                            color = SaffronDark.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                // Focus / Reset button
                FloatingActionButton(
                    onClick = {
                        // Focus on active rath
                        val activeNode = routeNodes[activeRath.routeIndex]
                        centerOn(activeNode.first, activeNode.second, 800f, 1000f)
                        Toast.makeText(context, "Centered viewport on ${activeRath.name}!", Toast.LENGTH_SHORT).show()
                    },
                    containerColor = SaffronPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("locate_chariot_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Center on Chariot"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFFFDF9)) // Parchment styling
                .testTag("interactive_map_screen")
        ) {
            // Live Raths Quick Tabs
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "THE 3 DIVINE CHARIOTS (LIVE TRACK)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = SaffronPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        raths.forEach { rath ->
                            val isSelected = rath.id == selectedRathId
                            val borderCol = if (isSelected) rath.color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            val bgCol = if (isSelected) rath.color.copy(alpha = 0.08f) else Color.Transparent

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(bgCol)
                                    .border(if (isSelected) 2.dp else 1.dp, borderCol, RoundedCornerShape(16.dp))
                                    .clickable { selectedRathId = rath.id }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${rath.flagEmoji} ${rath.initial}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = rath.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = nodeLabels[rath.routeIndex].split(" ").first(),
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Dynamic Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFF7EA)) // Warm background
            ) {
                // Interactive grid instructions overlay
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Drag/Pinch Map",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.6f, 4f)
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        }
                        .testTag("interactive_chariot_map")
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // 1. Draw Map grid lines for rich coordinate aesthetics
                    val gridCols = 8
                    val gridRows = 8
                    val stepX = canvasWidth / gridCols
                    val stepY = canvasHeight / gridRows
                    for (i in 1 until gridCols) {
                        drawLine(
                            color = Color(0xFFFFE8CC),
                            start = Offset(i * stepX, 0f),
                            end = Offset(i * stepX, canvasHeight),
                            strokeWidth = 1f
                        )
                    }
                    for (j in 1 until gridRows) {
                        drawLine(
                            color = Color(0xFFFFE8CC),
                            start = Offset(0f, j * stepY),
                            end = Offset(canvasWidth, j * stepY),
                            strokeWidth = 1f
                        )
                    }

                    // 2. Draw Connection Link Path from ISKCON Asansol Temple to GT Road Crossing
                    val templeCoords = Pair(23.6780, 87.0150)
                    val crossingCoords = Pair(23.6845, 87.0145)

                    val templeOffset = projectToCanvas(templeCoords.first, templeCoords.second, canvasWidth, canvasHeight)
                    val crossingOffset = projectToCanvas(crossingCoords.first, crossingCoords.second, canvasWidth, canvasHeight)

                    drawLine(
                        color = Color(0xFF4CAF50).copy(alpha = 0.5f),
                        start = templeOffset,
                        end = crossingOffset,
                        strokeWidth = 4f * scale,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )

                    // 3. Draw GT Road Path with nice thick gold and orange layouts
                    val roadPath = Path().apply {
                        routeNodes.forEachIndexed { idx, node ->
                            val pt = projectToCanvas(node.first, node.second, canvasWidth, canvasHeight)
                            if (idx == 0) {
                                moveTo(pt.x, pt.y)
                            } else {
                                lineTo(pt.x, pt.y)
                            }
                        }
                    }

                    // Draw outer border road
                    drawPath(
                        path = roadPath,
                        color = Color(0xFFFFCC80),
                        style = Stroke(width = 24f * scale, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    // Draw inner yellow central line
                    drawPath(
                        path = roadPath,
                        color = SaffronPrimary.copy(alpha = 0.5f),
                        style = Stroke(width = 4f * scale, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )

                    // 4. Draw Landmarks (ISKCON Temple, Budha ISKCON Temple, Polo Ground)
                    // Polo Ground Node
                    val poloCoords = routeNodes.last()
                    val poloOffset = projectToCanvas(poloCoords.first, poloCoords.second, canvasWidth, canvasHeight)
                    
                    // Budha ISKCON node
                    val budhaCoords = routeNodes.first()
                    val budhaOffset = projectToCanvas(budhaCoords.first, budhaCoords.second, canvasWidth, canvasHeight)

                    // DRAW LANDMARK DOTS & LABELS
                    val landmarksList = listOf(
                        Triple(templeOffset, "ISKCON Asansol", Color(0xFFE65100)),
                        Triple(budhaOffset, "Starting Point (Budha Temple)", Color(0xFF1565C0)),
                        Triple(poloOffset, "Festival Pandal (Polo Ground)", SaffronDark)
                    )

                    landmarksList.forEach { (offset, label, color) ->
                        // Outer glowing circle
                        drawCircle(
                            color = color.copy(alpha = 0.15f),
                            radius = 24f * scale,
                            center = offset
                        )
                        // Inner ring
                        drawCircle(
                            color = color,
                            radius = 8f * scale,
                            center = offset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3f * scale,
                            center = offset
                        )

                        // Draw neat labels above pins
                        val measuredText = textMeasurer.measure(
                            text = label,
                            style = TextStyle(
                                fontSize = (11 * scale).coerceIn(10f, 13f).sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark,
                                fontFamily = FontFamily.SansSerif
                            )
                        )
                        drawText(
                            textLayoutResult = measuredText,
                            topLeft = Offset(offset.x - measuredText.size.width / 2f, offset.y - 32f * scale - measuredText.size.height)
                        )
                    }

                    // 5. Draw the 3 Raths
                    val rathPlacements = listOf(
                        Pair(jagannathaRath, rath1Index),
                        Pair(baladevaRath, rath2Index),
                        Pair(subhadraRath, rath3Index)
                    )

                    rathPlacements.forEach { placement ->
                        val rath = placement.first
                        val index = placement.second
                        val coords = routeNodes[index]
                        val rathOffset = projectToCanvas(coords.first, coords.second, canvasWidth, canvasHeight)

                        // Outer glowing aura
                        drawCircle(
                            color = rath.color.copy(alpha = 0.25f),
                            radius = (18f + (10f * scale).coerceAtMost(25f)) * scale,
                            center = rathOffset
                        )

                        // Chariot marker circle
                        drawCircle(
                            color = rath.color,
                            radius = 12f * scale,
                            center = rathOffset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 9f * scale,
                            center = rathOffset
                        )
                        drawCircle(
                            color = rath.accentColor,
                            radius = 7f * scale,
                            center = rathOffset
                        )

                        // Print initial inside marker
                        val initialTxt = textMeasurer.measure(
                            text = rath.initial.toString(),
                            style = TextStyle(
                                fontSize = (10 * scale).coerceIn(9f, 13f).sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                        drawText(
                            textLayoutResult = initialTxt,
                            topLeft = Offset(rathOffset.x - initialTxt.size.width / 2f, rathOffset.y - initialTxt.size.height / 2f)
                        )

                        // Quick tiny ratha title flag label
                        val titleTxt = textMeasurer.measure(
                            text = rath.name,
                            style = TextStyle(
                                fontSize = (9 * scale).coerceIn(8f, 11f).sp,
                                fontWeight = FontWeight.Bold,
                                color = rath.accentColor,
                                background = Color.White.copy(alpha = 0.8f)
                            )
                        )
                        drawText(
                            textLayoutResult = titleTxt,
                            topLeft = Offset(rathOffset.x - titleTxt.size.width / 2f, rathOffset.y + 14f * scale)
                        )
                    }
                }
            }

            // Selected Rath Information & Simulation Controls Pane
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(activeRath.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${activeRath.name} Chariot",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronDark
                                )
                            }
                            Text(
                                text = "Deity: ${activeRath.deity}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Battery Telemetry
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.BatteryStd,
                                contentDescription = null,
                                tint = if (activeRath.battery < 20) Color.Red else Color(0xFF2E7D32),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                    text = "${activeRath.battery}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = activeRath.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chariot specifications & stats row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Ratha Height", fontSize = 10.sp, color = Color.Gray)
                            Text(text = activeRath.specHeight, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Wooden Wheels", fontSize = 10.sp, color = Color.Gray)
                            Text(text = "${activeRath.specWheels} Wheels", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Current Station", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = nodeLabels[activeRath.routeIndex].take(16) + if (nodeLabels[activeRath.routeIndex].length > 16) ".." else "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simulated Pull action row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // Reset to temple / start
                                when (activeRath.id) {
                                    "rath_j" -> { rath1Index = 0; rath1Battery = 99 }
                                    "rath_b" -> { rath2Index = 0; rath2Battery = 99 }
                                    "rath_s" -> { rath3Index = 0; rath3Battery = 99 }
                                }
                                // Center map on start node
                                centerOn(routeNodes[0].first, routeNodes[0].second, 1000f, 1000f)
                                Toast.makeText(context, "Returned ${activeRath.name} home to starting point!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset Start", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                // Advance along route index
                                val currentIndex = when (activeRath.id) {
                                    "rath_j" -> rath1Index
                                    "rath_b" -> rath2Index
                                    "rath_s" -> rath3Index
                                    else -> 0
                                }
                                
                                val nextIndex = (currentIndex + 1) % routeNodes.size
                                when (activeRath.id) {
                                    "rath_j" -> {
                                        rath1Index = nextIndex
                                        rath1Battery -= 3
                                        // Update central VM as well to keep in sync
                                        viewModel.moveChariotSimulated()
                                    }
                                    "rath_b" -> {
                                        rath2Index = nextIndex
                                        rath2Battery -= 2
                                    }
                                    "rath_s" -> {
                                        rath3Index = nextIndex
                                        rath3Battery -= 2
                                    }
                                }

                                // Center map coordinates on next node
                                val nextNodeCoords = routeNodes[nextIndex]
                                centerOn(nextNodeCoords.first, nextNodeCoords.second, 800f, 1000f)

                                Toast.makeText(
                                    context, 
                                    "Harinama! Devotees pulled ${activeRath.name} chariot forward to ${nodeLabels[nextIndex]}! 🪔🚩", 
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(2.5f)
                                .height(42.dp)
                                .testTag("sim_pull_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pull Chariot (Advance)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
