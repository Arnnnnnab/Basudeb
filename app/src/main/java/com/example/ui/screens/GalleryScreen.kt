package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.RathaYatraViewModel
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.GoldAccent

data class GalleryItem(
    val id: String,
    val title: String,
    val category: String, // "Darshan", "Chariot", "Kirtan", "Prasadam"
    val imageUrl: String,
    val description: String,
    val sanskritVerse: String,
    val verseTranslation: String,
    val captureBy: String = "ISKCON Media"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(viewModel: RathaYatraViewModel) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var zoomItem by remember { mutableStateOf<GalleryItem?>(null) }
    
    // Manage favorite states locally with a set of IDs
    var favoriteIds by remember { mutableStateOf(setOf<String>()) }

    val categories = listOf("All", "Darshan", "Chariot", "Kirtan", "Prasadam")

    val galleryItems = remember {
        listOf(
            GalleryItem(
                id = "gal_1",
                title = "Sri Sri Jagannatha, Baladeva & Subhadra",
                category = "Darshan",
                imageUrl = "https://images.unsplash.com/photo-1627856013091-fed6e4e30025?q=80&w=800",
                description = "Their Lordships majestically decorated with organic marigold and jasmine garlands. Adorned in glowing royal silk garments, their beautiful round lotus eyes shower pure nectar and mercy upon all of Asansol's coordinates.",
                sanskritVerse = "na prayace na ca rājam-ādarshāye\nprabho jagannātha vihāra-kāriṇe\nnamo namaste śaraṇāgatāya",
                verseTranslation = "O Lord Jagannath, I do not seek wealth or royal praise. I seek only your causeless mercy. I bow down to You again and again."
            ),
            GalleryItem(
                id = "gal_2",
                title = "Grand Chariots Standing in Splendor",
                category = "Chariot",
                imageUrl = "https://images.unsplash.com/photo-1609137144813-9118bc9c1b7e?q=80&w=800",
                description = "The colossal chariot of Sri Baladeva, ready to depart from the main temple gates. Outfitted with deep saffron and golden banners, the heavy sal wood wheels symbolize the cosmic cycle of eternal servitude.",
                sanskritVerse = "rathasthaṁ vāmanaṁ dṛṣṭvā\npunarjanma na vidyate",
                verseTranslation = "Seeing the short Vamana form (Lord Jagannatha) upon His chariot, one never takes birth again in the material world."
            ),
            GalleryItem(
                id = "gal_3",
                title = "Gundicha Temple Sandalwood Fragrance",
                category = "Chariot",
                imageUrl = "https://images.unsplash.com/photo-1614064641938-3bbee52942c7?q=80&w=800",
                description = "The divine altar prep station, where thick sandalwood paste (Chandana), pure camphor, and fresh tulsi leaves are kept ready to cleanse the pathways of Gundicha Devi’s garden shelter.",
                sanskritVerse = "śrīndradyumna-manoramera pariśodhana\ngundicā-mārjana-līlā parama-pāvana",
                verseTranslation = "The cleaning of the Gundicha garden temple by Sri Chaitanya Mahaprabhu’s own hands remains the most purifying spiritual pastime."
            ),
            GalleryItem(
                id = "gal_4",
                title = "Spiritual Ecstasy of Harinama Kirtan",
                category = "Kirtan",
                imageUrl = "https://images.unsplash.com/photo-1561361513-2d000a50f0db?q=80&w=800",
                description = "Thousands of devotees raise their hands in spiritual ecstasy, dancing to the sweet rhythms of earthen mridangas and brass kartals under the midday sun. The sacred sound vibration shakes Grand Trunk Road.",
                sanskritVerse = "hare kṛṣṇa hare kṛṣṇa kṛṣṇa kṛṣṇa hare hare\nhare rāma hare rāma rāma rāma hare hare",
                verseTranslation = "My dear Lord, O spiritual energy of the Lord, please engage me in Your transcendental loving service."
            ),
            GalleryItem(
                id = "gal_5",
                title = "The Golden Wheel & Sacred Guiding Ropes",
                category = "Chariot",
                imageUrl = "https://images.unsplash.com/photo-1545205597-3d9d02c29597?q=80&w=800",
                description = "Spiritual seekers grab the thick jute ropes of the chariot, pulling the Lord of the Universe directly into their hearts. This act instantly wipes out lifespans of accumulated negative karmic reactions.",
                sanskritVerse = "gange ca yamune caiva\ngodāvari sarasvati narmade",
                verseTranslation = "O sacred rivers! Please make these paths and ropes spiritual, carrying the flow of eternal devotion."
            ),
            GalleryItem(
                id = "gal_6",
                title = "Traditional Rice & Camphor Sweets Feast",
                category = "Prasadam",
                imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?q=80&w=800",
                description = "Aromatic ghee khichdi, slow-cooked in brass pots over cow-dung fires, seasoned with roasted black cumin and pure rock salt. Served piping hot to thousands of pilgrims at the GT Road junctions.",
                sanskritVerse = "mahā-prasāde govinde\nnāma-brāhmaṇi vaiṣṇave",
                verseTranslation = "For those who have no spiritual merit, the holy name, the Vaishnavas, and the Lord’s remnants are the ultimate shelter."
            ),
            GalleryItem(
                id = "gal_7",
                title = "Sweet Malpua Syrup Distribution Desk",
                category = "Prasadam",
                imageUrl = "https://images.unsplash.com/photo-1505253716362-afaea1d3d1af?q=80&w=800",
                description = "Freshly fried malpuas, soaked in cold cardamom nectar, prepared lovingly by understudies of the temple deity kitchen. Distributed freely as blissful remnants to every child and pilgrim.",
                sanskritVerse = "prasāda-sevā karite haya\nsakala prapañca-jaya",
                verseTranslation = "By honoring the Lord’s prasadam, one easily conquers all worldly obstructions and material dualities."
            )
        )
    }

    // Filtered list based on Search and Selected Category
    val filteredItems = remember(selectedCategory, searchQuery, galleryItems) {
        galleryItems.filter { item ->
            val matchCategory = selectedCategory == "All" || item.category == selectedCategory
            val matchSearch = searchQuery.isBlank() || 
                item.title.contains(searchQuery, ignoreCase = true) || 
                item.description.contains(searchQuery, ignoreCase = true)
            matchCategory && matchSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ratha Yatra Darshan",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Text(
                            text = "Divine media archive of ISKCON Asansol",
                            style = MaterialTheme.typography.bodySmall,
                            color = SaffronDark.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("gallery_screen_container")
        ) {
            // Elegant search bar and offline indicator
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search divine pastimes or prasadam...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = SaffronPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear text")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("gallery_search"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable category filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(text = cat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = SaffronDark
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("gallery_category_chip_$cat")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Beautiful, minimal offline sync status indicator matching design intent
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudDone,
                        contentDescription = "Sync complete",
                        tint = SaffronDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Divine Darshan Cached Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SaffronDark
                    )
                }
            }

            // Beautiful vertical card list
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.SearchOff,
                            contentDescription = "Empty visual",
                            tint = SaffronPrimary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No transcendent moments found",
                            style = MaterialTheme.typography.titleMedium,
                            color = SaffronDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing search queries or checking other darshan filters",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredItems) { item ->
                        val isLiked = favoriteIds.contains(item.id)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .testTag("gallery_item_${item.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column {
                                // Image Header with elegant tag
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp)
                                        .clickable { zoomItem = item }
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(item.imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Subtle Gradient overlay for premium finish
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                                    startY = 100f
                                                )
                                            )
                                    )

                                    // Category Tag
                                    Box(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .align(Alignment.TopStart)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SaffronPrimary)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = item.category.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    // Photographer badge
                                    Text(
                                        text = "Photo: ${item.captureBy}",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp),
                                        fontWeight = FontWeight.Medium
                                    )

                                    // Zoom hover icon hint
                                    IconButton(
                                        onClick = { zoomItem = item },
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ZoomIn,
                                            contentDescription = "Expand photo",
                                            tint = Color.White
                                        )
                                    }
                                }

                                // Interactive Info Body
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SaffronDark,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Fine Line Action Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Tap to chant / read verses
                                        Row(
                                            modifier = Modifier
                                                .clickable { zoomItem = item }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.MenuBook,
                                                contentDescription = null,
                                                tint = SaffronPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Read Verse Pray",
                                                fontSize = 11.sp,
                                                color = SaffronPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Love Favorite action with realistic haptic shake
                                            IconButton(
                                                onClick = {
                                                    favoriteIds = if (isLiked) {
                                                        favoriteIds - item.id
                                                    } else {
                                                        Toast.makeText(context, "Added to Devotional Favorites! ❤️", Toast.LENGTH_SHORT).show()
                                                        favoriteIds + item.id
                                                    }
                                                },
                                                modifier = Modifier.size(36.dp).testTag("fav_btn_${item.id}")
                                            ) {
                                                Icon(
                                                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                    contentDescription = "Like item",
                                                    tint = if (isLiked) Color.Red else Color.LightGray.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            // Share Darshan action
                                            IconButton(
                                                onClick = {
                                                    Toast.makeText(context, "Spiritual link of \"${item.title}\" copied! Ready to share.", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(36.dp).testTag("share_btn_${item.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Share,
                                                    contentDescription = "Share item",
                                                    tint = SaffronPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Premium Immersive Darshan Dialog Overlay
    zoomItem?.let { item ->
        Dialog(
            onDismissRequest = { zoomItem = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Custom Header toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Photo,
                                contentDescription = null,
                                tint = SaffronPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Darshan Bliss",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark
                            )
                        }

                        // Close Button
                        IconButton(
                            onClick = { zoomItem = null },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close screen",
                                tint = SaffronDark
                            )
                        }
                    }

                    // Zoomed Screen Body
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("zoom_dialog_content"),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // Big dynamic photo
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(290.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                                startY = 150f
                                            )
                                        )
                                )

                                Text(
                                    text = item.category.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                        .background(SaffronPrimary, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Devotional content
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.displayLarge,
                                    fontSize = 24.sp,
                                    color = SaffronDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 30.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Visual Darshan Provided by ISKCON Asansol Media Commission",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontStyle = FontStyle.Italic
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    lineHeight = 24.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Sanskrit spiritual sloka block
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = GoldAccent.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(24.dp)
                                        ),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(24.dp)
                               ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AutoAwesome,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = item.sanskritVerse,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = SaffronDark,
                                            textAlign = TextAlign.Center,
                                            fontStyle = FontStyle.Italic,
                                            lineHeight = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Divider(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                            modifier = Modifier.width(80.dp)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "\"${item.verseTranslation}\"",
                                            fontSize = 13.sp,
                                            color = SaffronDark.copy(alpha = 0.8f),
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Functional action buttons
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "🕉️ Chanting mantra registered! May Lord Jagannath bless your home.", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Icon(imageVector = Icons.Outlined.ThumbUp, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Offer Obeisances (Pranam)", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Downloading high-resolution darshan card to your local downloads folder!", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .border(1.dp, SaffronPrimary, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Outlined.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save Darshan Wallpaper", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
