package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WisaalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiGeneratorScreen(
    viewModel: WisaalViewModel,
    modifier: Modifier = Modifier
) {
    val aiLoading by viewModel.aiLoading.collectAsState()
    val aiResultText by viewModel.aiResultText.collectAsState()
    val aiError by viewModel.aiError.collectAsState()

    var relationshipMood by remember { mutableStateOf("نحتاج لمرح وضحك 🎈") }
    var topicTheme by remember { mutableStateOf("ذكريات بدايتنا الأولى 📸") }
    var isChallenge by remember { mutableStateOf(true) } // true for Challenge, false for 3 Questions

    var saveCompleted by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Reset save success indicator when new text is generated
    LaunchedEffect(aiResultText) {
        saveCompleted = false
    }

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
            // --- AI Header ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Spark",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مولد وصال الذكي (AI)",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "صمم تحديات عاطفية وأسئلة فريدة لشريكك بمساعدة الذكاء الاصطناعي (Gemini) بناءً على مزاجكما الحالي ومواضيع نقاش تختارانها.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // --- Form Inputs ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Relationship Mood Selectors
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🎭 ما هو مزاج الشريكين الحالي؟",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val moods = listOf(
                            "نحتاج لمرح وضحك 🎈",
                            "جلسة هادئة وحوار عميق 🧠",
                            "تخطيط رومانسي للمستقبل 🔮",
                            "إذابة الجليد وتصالح دافئ ❤️‍🩹"
                        )
                        
                        moods.forEach { mood ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { relationshipMood = mood }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = relationshipMood == mood,
                                    onClick = { relationshipMood = mood }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = mood,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 2. Topic Themes Selectors
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💬 ما هو الموضوع الذي تودان التركيز عليه؟",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val topics = listOf(
                            "ذكريات بدايتنا الأولى 📸",
                            "الامتنان والتقدير المتبادل 🙏",
                            "خطط سفر وأحلام مشتركة ✈️",
                            "التواصل العاطفي وحل الخلافات 🤝"
                        )
                        
                        topics.forEach { topic ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { topicTheme = topic }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = topicTheme == topic,
                                    onClick = { topicTheme = topic }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 3. Choice of Content Type: Challenge vs Questions
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚡ نوع المحتوى الذكي المطلوب:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { isChallenge = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isChallenge) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Filled.Star, contentDescription = "Challenge")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تحدي عاطفي رومانسي",
                                    fontSize = 11.sp,
                                    color = if (isChallenge) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { isChallenge = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isChallenge) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Filled.Forum, contentDescription = "Questions")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "3 أسئلة نقاش عميقة",
                                    fontSize = 11.sp,
                                    color = if (!isChallenge) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Spark Generator Button
                Button(
                    onClick = {
                        viewModel.generateAiContent(relationshipMood, topicTheme, isChallenge)
                    },
                    enabled = !aiLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("ai_generate_button")
                ) {
                    if (aiLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("جاري استدعاء الإلهام العاطفي من Gemini...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = "Generate")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("قدح شرارة الحب الذكية ✨", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- AI Response Result Panel ---
                aiResultText?.let { resultText ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_result_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✨ وصفتك العاطفية الخاصة جاهزة:",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(resultText))
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy text",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = resultText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 16.sp,
                                    lineHeight = 26.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action to directly SAVE to local DB
                            AnimatedContent(targetState = saveCompleted, label = "SaveButtonState") { saved ->
                                if (saved) {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                            disabledContentColor = Color(0xFF2E7D32)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(imageVector = Icons.Filled.Done, contentDescription = "Saved")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تم حفظ المحتوى في التطبيق بنجاح! 🥰", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            viewModel.saveAiResultToDb(isChallenge, "deep")
                                            saveCompleted = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("ai_save_result_button")
                                    ) {
                                        Icon(imageVector = Icons.Filled.Save, contentDescription = "Save to DB")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isChallenge) "أضف هذا لتحدياتي اليومية!" else "احفظ هذه الأسئلة الحوارية!",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- AI Error Panel ---
                aiError?.let { errorMsg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorMsg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Security caution notice for prototypes as mandated in Option B:
                Text(
                    text = "تنبيه أمان: يتم تشغيل الميزات الذكية بمفتاح API مخصص للنموذج الأولي. لا تشارك مفتاحك الخاص مطلقاً.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp)
                )
            }
        }
    }
}
