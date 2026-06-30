package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Question
import com.example.ui.WisaalViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionsScreen(
    viewModel: WisaalViewModel,
    modifier: Modifier = Modifier
) {
    val allQuestions by viewModel.allQuestions.collectAsState()
    val discussedCount by viewModel.discussedQuestionsCount.collectAsState()

    var selectedCategory by remember { mutableStateOf("deep") } // deep, playful, future, memories, favorites
    var showAddCustomDialog by remember { mutableStateOf(false) }
    var customQuestionText by remember { mutableStateOf("") }
    var customQuestionCategory by remember { mutableStateOf("deep") }

    // State for the active question on the discussion card
    var activeQuestion by remember { mutableStateOf<Question?>(null) }

    // Filter questions based on selected category or favorites
    val filteredQuestions = remember(allQuestions, selectedCategory) {
        when (selectedCategory) {
            "favorites" -> allQuestions.filter { it.isFavorite }
            else -> allQuestions.filter { it.category == selectedCategory }
        }
    }

    // Update active question when category changes or when it's null
    LaunchedEffect(filteredQuestions) {
        if (filteredQuestions.isNotEmpty()) {
            // Keep current if it is in the list, otherwise select a random one
            if (activeQuestion == null || activeQuestion !in filteredQuestions) {
                activeQuestion = filteredQuestions.firstOrNull()
            }
        } else {
            activeQuestion = null
        }
    }

    // Function to fetch another random question from current category
    val loadRandomQuestion = {
        if (filteredQuestions.size > 1) {
            val remaining = filteredQuestions.filter { it.id != activeQuestion?.id }
            activeQuestion = remaining[Random.nextInt(remaining.size)]
        } else if (filteredQuestions.isNotEmpty()) {
            activeQuestion = filteredQuestions.first()
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            // --- Screen Header ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        text = "أسئلة وصال العاطفية",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "أسئلة مصممة بعناية لفتح نقاشات دافئة وعميقة تكشف جوانب جديدة لشريكك.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // --- Category Tabs Row (Scrollable or centered) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf(
                    Triple("deep", "عميقة 🧠", MaterialTheme.colorScheme.primary),
                    Triple("playful", "مرحة 🎈", Color(0xFFFF9800)),
                    Triple("future", "مستقبلنا 🔮", Color(0xFF9C27B0)),
                    Triple("memories", "ذكريات 📸", Color(0xFF00BCD4))
                )

                categories.forEach { (catId, label, color) ->
                    val isSelected = selectedCategory == catId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = catId },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.15f),
                            selectedLabelColor = color,
                            selectedLeadingIconColor = color
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = color.copy(alpha = 0.3f),
                            selectedBorderColor = color,
                            selectedBorderWidth = 1.5.dp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Add Favorites Tab Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isFavSelected = selectedCategory == "favorites"
                FilterChip(
                    selected = isFavSelected,
                    onClick = { selectedCategory = "favorites" },
                    label = {
                        Text(
                            text = "الأسئلة المفضلة ⭐",
                            fontWeight = if (isFavSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFD54F).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFFF57F17)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Active Card Display Section ---
            activeQuestion?.let { question ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("question_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Category Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (question.category) {
                                        "deep" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        "playful" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                        "future" -> Color(0xFF9C27B0).copy(alpha = 0.1f)
                                        else -> Color(0xFF00BCD4).copy(alpha = 0.1f)
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = when (question.category) {
                                    "deep" -> "أسئلة عميقة وروحية"
                                    "playful" -> "تسلية ومرح"
                                    "future" -> "الأحلام والمستقبل"
                                    else -> "ذكريات ومشاعر قديمة"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = when (question.category) {
                                    "deep" -> MaterialTheme.colorScheme.primary
                                    "playful" -> Color(0xFFFF9800)
                                    "future" -> Color(0xFF9C27B0)
                                    else -> Color(0xFF00BCD4)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Question Text
                        Text(
                            text = question.text,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 20.sp,
                                lineHeight = 32.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Divider line
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Favorite Button
                            IconButton(
                                onClick = { viewModel.toggleFavoriteQuestion(question) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = if (question.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (question.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Discussed / Checked Off
                            Button(
                                onClick = { viewModel.completeQuestionDiscussion(question) },
                                enabled = !question.isDiscussed,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    disabledContentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (question.isDiscussed) Icons.Filled.CheckCircle else Icons.Filled.Forum,
                                    contentDescription = "Discussed"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (question.isDiscussed) "تحدثنا معاً 🥰" else "ناقشنا هذا السؤال! (+10)",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Next Question Card Button
                            IconButton(
                                onClick = { loadRandomQuestion() },
                                enabled = filteredQuestions.size > 1,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NavigateBefore,
                                    contentDescription = "Next question",
                                    tint = if (filteredQuestions.size > 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            } ?: run {
                // Empty state for selected Category (no favorites, etc)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (selectedCategory == "favorites") Icons.Filled.StarOutline else Icons.Filled.FolderOpen,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedCategory == "favorites") "لا توجد أسئلة مفضلة مضافة حالياً!" else "لا توجد أسئلة في هذا القسم.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedCategory == "favorites") "اضغط على نجمة المفضلة أثناء استعراض الأسئلة لحفظها هنا لترجعا إليها لاحقاً." else "أضف أسئلة مخصصة لتمتلئ هذه الصفحة بالحب والوصال.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Custom Local Question Addition ---
            OutlinedButton(
                onClick = { showAddCustomDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .testTag("add_custom_question_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ابتكار سؤال حواري مخصص",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Dialogs ---

        // Add Custom Question Dialog
        if (showAddCustomDialog) {
            Dialog(onDismissRequest = { showAddCustomDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ابتكار سؤال عاطفي جديد",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "اصنع سؤالاً شيقاً لمناقشته الليلة مع شريكك.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Category choice dropdown/row
                        Text(
                            text = "اختر القسم:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val listCats = listOf("deep" to "عميقة", "playful" to "مرحة", "future" to "مستقبل", "memories" to "ذكريات")
                            listCats.forEach { (catKey, catLabel) ->
                                val isSelectedCat = customQuestionCategory == catKey
                                FilterChip(
                                    selected = isSelectedCat,
                                    onClick = { customQuestionCategory = catKey },
                                    label = { Text(catLabel, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customQuestionText,
                            onValueChange = { customQuestionText = it },
                            label = { Text("اكتب سؤالك المميز هنا...") },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAddCustomDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("إلغاء")
                            }

                            Button(
                                onClick = {
                                    if (customQuestionText.isNotBlank()) {
                                        viewModel.addCustomQuestion(customQuestionText.trim(), customQuestionCategory)
                                        customQuestionText = ""
                                    }
                                    showAddCustomDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("حفظ السؤال")
                            }
                        }
                    }
                }
            }
        }
    }
}
