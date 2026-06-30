package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.models.Challenge
import com.example.data.models.Question
import com.example.data.models.UserStats
import com.example.data.remote.Content
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Part
import com.example.data.remote.RetrofitClient
import com.example.data.repository.WisaalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class WisaalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WisaalRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WisaalRepository(database.wisaalDao())
        
        // Initialize and prepopulate DB
        viewModelScope.launch(Dispatchers.IO) {
            repository.prepopulateIfNeeded()
        }
    }

    // --- State Streams ---
    val allChallenges: StateFlow<List<Challenge>> = repository.allChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuestions: StateFlow<List<Question>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStats?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val completedChallengesCount: StateFlow<Int> = repository.completedChallengesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val discussedQuestionsCount: StateFlow<Int> = repository.discussedQuestionsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Current daily challenge based on day of year
    val dailyChallenge: StateFlow<Challenge?> = allChallenges.map { list ->
        if (list.isEmpty()) return@map null
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        list[dayOfYear % list.size]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- AI Generator States ---
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiResultText = MutableStateFlow<String?>(null)
    val aiResultText: StateFlow<String?> = _aiResultText.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    // --- Interactive Games States ---
    
    // 1. How Well Do You Know Me Game
    data class CompatibilityQuestion(
        val questionText: String,
        val options: List<String>
    )

    val compatibilityQuestions = listOf(
        CompatibilityQuestion("ما هي الأكلة المفضلة للطرف الآخر؟", listOf("ورق عنب ومحاشي 🍇", "بيتزا ومعكرونة 🍕", "مشاوي ولحوم 🥩", "برجر وبطاطس 🍔")),
        CompatibilityQuestion("ما هي الوجهة المثالية للسفر في رأي الطرف الآخر؟", listOf("مدن تاريخية وثقافة 🏛️", "شواطئ وجزر استوائية 🏖️", "طبيعة وجبال وجليد 🏔️", "التسوق والترفيه الصاخب 🛍️")),
        CompatibilityQuestion("ما هو أكثر شيء يسعد الطرف الآخر في يوم شاق؟", listOf("حديث طويل معي للتفريغ 💬", "نوم عميق وراحة هادئة 😴", "طعام لذيذ ومشروب دافئ ☕", "مشاهدة فيلم أو مسلسل مفضل 🎬")),
        CompatibilityQuestion("أي من فصول السنة هو المفضل للطرف الآخر؟", listOf("الشتاء والأجواء الماطرة 🌧️", "الربيع والأزهار اللطيفة 🌸", "الصيف والبحر والرحلات ☀️", "الخريف والهدوء والنسمات 🍂")),
        CompatibilityQuestion("ما هي وسيلة التعبير عن الحب الأجمل لدى الطرف الآخر؟", listOf("كلمات التشجيع والحب والمديح 🗣️", "الهدايا البسيطة وغير المتوقعة 🎁", "مساعدته في أداء مهامه اليومية 🤝", "قضاء وقت نوعي وهادئ معاً 💑"))
    )

    private val _quizState = MutableStateFlow(QuizState())
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    data class QuizState(
        val isStarted: Boolean = false,
        val currentRoundIndex: Int = 0,
        val playerATurn: Boolean = true, // Player A sets actuals, Player B guesses
        val playerAAnswers: Map<Int, Int> = emptyMap(), // QuestionIndex -> OptionIndex
        val playerBGuesses: Map<Int, Int> = emptyMap(), // QuestionIndex -> OptionIndex
        val finalScore: Int = 0,
        val isFinished: Boolean = false
    )

    // 2. This or That Game
    data class ThisOrThatItem(
        val title: String,
        val optionA: String,
        val optionB: String
    )

    val thisOrThatItems = listOf(
        ThisOrThatItem("الوجهة المثالية", "شاطئ البحر 🏖️", "كوخ في الجبل 🏔️"),
        ThisOrThatItem("مشروب اللقاء", "قهوة ساخنة ☕", "عصير بارد منعش 🍹"),
        ThisOrThatItem("العشاء المثالي", "الطهي معاً في المنزل 🍳", "الخروج لمطعم راقٍ 🍽️"),
        ThisOrThatItem("أجواء السهرة", "جلسة حوارية دافئة 💬", "مشاهدة فيلم مشوق 🍿"),
        ThisOrThatItem("الساعة الحيوية", "الاستيقاظ المبكر مع الشمس 🌅", "السهر والحديث تحت النجوم 🌌"),
        ThisOrThatItem("التعبير عن الحب", "كلمات وعبارات حب مسموعة 🗣️", "أفعال ومفاجآت وهدايا لطيفة 🎁"),
        ThisOrThatItem("السفر المفضل", "رحلة منظمة ومجدولة بالدقيقة 📅", "رحلة عفوية ومغامرات غير متوقعة 🎒"),
        ThisOrThatItem("نشاط عطلة نهاية الأسبوع", "ألعاب ممتعة وضحك في المنزل 🎲", "الذهاب للتسوق والمقاهي الخارجية 🛍️")
    )

    private val _thisOrThatState = MutableStateFlow(ThisOrThatState())
    val thisOrThatState: StateFlow<ThisOrThatState> = _thisOrThatState.asStateFlow()

    data class ThisOrThatState(
        val currentIndex: Int = 0,
        val partnerAChoice: Int? = null, // 0 for A, 1 for B
        val partnerBChoice: Int? = null, // 0 for A, 1 for B
        val matchCount: Int = 0,
        val totalRounds: Int = 0,
        val history: List<ThisOrThatResult> = emptyList()
    )

    data class ThisOrThatResult(
        val title: String,
        val isMatched: Boolean,
        val partnerA: String,
        val partnerB: String
    )


    // --- Actions ---

    // 1. Complete Challenge
    fun completeChallenge(challenge: Challenge) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = challenge.copy(isCompleted = true, completedDate = System.currentTimeMillis())
            repository.updateChallenge(updated)

            // Award points and update stats
            val current = repository.userStats.firstOrNull() ?: UserStats()
            val lastComp = current.lastCompletedDate
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Streak logic
            var newStreak = current.currentStreak
            if (lastComp > 0) {
                val lastCal = Calendar.getInstance().apply { timeInMillis = lastComp }
                val todayCal = Calendar.getInstance().apply { timeInMillis = today }
                
                // Diff in days
                val diffMs = todayCal.timeInMillis - lastCal.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val diffDays = diffMs / (1000 * 60 * 60 * 24)
                if (diffDays == 1L) {
                    newStreak += 1
                } else if (diffDays > 1L) {
                    newStreak = 1
                }
            } else {
                newStreak = 1
            }

            val updatedStats = current.copy(
                connectionPoints = current.connectionPoints + 15,
                currentStreak = newStreak,
                lastCompletedDate = System.currentTimeMillis()
            )
            repository.saveUserStats(updatedStats)
        }
    }

    // 2. Toggle Favorite Question
    fun toggleFavoriteQuestion(question: Question) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = question.copy(isFavorite = !question.isFavorite)
            repository.updateQuestion(updated)
        }
    }

    // 3. Complete Question Discussion
    fun completeQuestionDiscussion(question: Question) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!question.isDiscussed) {
                val updated = question.copy(isDiscussed = true)
                repository.updateQuestion(updated)

                // Award points
                val current = repository.userStats.firstOrNull() ?: UserStats()
                repository.saveUserStats(current.copy(connectionPoints = current.connectionPoints + 10))
            }
        }
    }

    // 4. Update Partner Names
    fun updatePartnerNames(partnerA: String, partnerB: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.userStats.firstOrNull() ?: UserStats()
            repository.saveUserStats(current.copy(partnerAName = partnerA, partnerBName = partnerB))
        }
    }

    // 5. Add custom local challenge
    fun addCustomChallenge(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertChallenge(Challenge(text = text, isCustom = true))
        }
    }

    // 6. Add custom local question
    fun addCustomQuestion(text: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertQuestion(Question(text = text, category = category, isCustom = true))
        }
    }

    // 7. Generate AI Challenge / Questions via Gemini API
    fun generateAiContent(mood: String, topic: String, isChallenge: Boolean) {
        _aiLoading.value = true
        _aiResultText.value = null
        _aiError.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                _aiError.value = "الرجاء ضبط مفتاح API في لوحة الأسرار (Secrets) لتتمكن من استخدام الذكاء الاصطناعي."
                _aiLoading.value = false
                return@launch
            }

            val prompt = if (isChallenge) {
                """
                أنت خبير علاقات أسرية ومستشار عاطفي روماني. قم بتوليد "تحدي عاطفي رومانسي وترفيهي واحد ومفصل ومميز" لشريكين عاطفيين يبحثان عن تعزيز ترابطهما العاطفي.
                مزاج الشريكين الحالي: $mood
                موضوع التحدي الأساسي: $topic
                
                متطلبات التحدي:
                1. يجب أن يكون التحدي ممتعاً، ومكتوباً بأسلوب عربي دافئ، رقيق، ومحفز للحب.
                2. يجب أن يتضمن خطوات واضحة وممتعة يمكن القيام بها فوراً أو خلال اليوم.
                3. اجعل النص مختصراً وجذاباً (بحدود 3-5 أسطر).
                4. لا تضف أي نص تمهيدي أو ختامي، ابدأ بنص التحدي مباشرة.
                """.trimIndent()
            } else {
                """
                أنت مستشار علاقات دافئ وخبير في الحوارات العاطفية. قم بتوليد "3 أسئلة عميقة وشيقة لتعزيز الترابط والنقاش العاطفي" بين حبيبين أو زوجين.
                المزاج الحوار المطلوب: $mood
                الموضوع: $topic
                
                متطلبات الأسئلة:
                1. يجب أن تكون الأسئلة غير مكررة وعميقة للغاية، وتفتح باباً للحديث الدافئ والصريح والممتع.
                2. اكتب الأسئلة باللغة العربية بأسلوب راقٍ وشاعري.
                3. رتب الأسئلة بنقاط واضحة (1، 2، 3) بدون أي نصوص تمهيدية أو ختامية.
                """.trimIndent()
            }

            try {
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )
                val response = RetrofitClient.geminiService.generateContent(apiKey, request)
                val textResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                if (textResult != null) {
                    _aiResultText.value = textResult.trim()
                } else {
                    _aiError.value = "لم نتمكن من الحصول على استجابة واضحة من الذكاء الاصطناعي."
                }
            } catch (e: Exception) {
                _aiError.value = "خطأ في الاتصال: ${e.localizedMessage ?: "تأكد من اتصالك بالإنترنت ومن صلاحية مفتاح Gemini API."}"
            } finally {
                _aiLoading.value = false
            }
        }
    }

    // Save AI result to local database
    fun saveAiResultToDb(isChallenge: Boolean, category: String) {
        val text = _aiResultText.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            if (isChallenge) {
                repository.insertChallenge(Challenge(text = text, isCustom = true))
            } else {
                // Parse questions by splitting lines
                val lines = text.split("\n")
                    .map { it.replace(Regex("^[123]\\.\\s*|\\*\\s*|^-\\s*"), "").trim() }
                    .filter { it.isNotEmpty() }
                
                if (lines.isNotEmpty()) {
                    for (line in lines) {
                        repository.insertQuestion(Question(text = line, category = category, isCustom = true))
                    }
                } else {
                    repository.insertQuestion(Question(text = text, category = category, isCustom = true))
                }
            }
            // Clear AI text to show saved status
            _aiResultText.value = null
        }
    }


    // --- Games Logic ---

    // 1. "How Well Do You Know Me" Functions
    fun startQuiz() {
        _quizState.value = QuizState(isStarted = true)
    }

    fun answerQuiz(optionIndex: Int) {
        val current = _quizState.value
        val roundIdx = current.currentRoundIndex

        if (current.playerATurn) {
            // Partner A setting correct answers
            val newAnswers = current.playerAAnswers.toMutableMap().apply {
                put(roundIdx, optionIndex)
            }
            if (roundIdx < 2) { // 3 rounds total (0, 1, 2)
                _quizState.value = current.copy(
                    playerAAnswers = newAnswers,
                    currentRoundIndex = roundIdx + 1
                )
            } else {
                // Shift to Partner B guessing
                _quizState.value = current.copy(
                    playerAAnswers = newAnswers,
                    currentRoundIndex = 0,
                    playerATurn = false
                )
            }
        } else {
            // Partner B guessing
            val newGuesses = current.playerBGuesses.toMutableMap().apply {
                put(roundIdx, optionIndex)
            }
            if (roundIdx < 2) {
                _quizState.value = current.copy(
                    playerBGuesses = newGuesses,
                    currentRoundIndex = roundIdx + 1
                )
            } else {
                // Finished! Calculate score
                var correctCount = 0
                for (i in 0..2) {
                    if (current.playerAAnswers[i] == newGuesses[i]) {
                        correctCount++
                    }
                }
                
                // Award points
                viewModelScope.launch(Dispatchers.IO) {
                    val stats = repository.userStats.firstOrNull() ?: UserStats()
                    repository.saveUserStats(stats.copy(connectionPoints = stats.connectionPoints + (correctCount * 10)))
                }

                _quizState.value = current.copy(
                    playerBGuesses = newGuesses,
                    finalScore = correctCount,
                    isFinished = true
                )
            }
        }
    }

    fun resetQuiz() {
        _quizState.value = QuizState()
    }


    // 2. "This or That" Functions
    fun selectThisOrThatOption(optionIndex: Int, isPartnerA: Boolean) {
        val current = _thisOrThatState.value
        val roundItem = thisOrThatItems[current.currentIndex]

        var nextA = current.partnerAChoice
        var nextB = current.partnerBChoice

        if (isPartnerA) {
            nextA = optionIndex
        } else {
            nextB = optionIndex
        }

        if (nextA != null && nextB != null) {
            // Both answered this round!
            val matched = nextA == nextB
            val result = ThisOrThatResult(
                title = roundItem.title,
                isMatched = matched,
                partnerA = if (nextA == 0) roundItem.optionA else roundItem.optionB,
                partnerB = if (nextB == 0) roundItem.optionA else roundItem.optionB
            )

            val updatedHistory = current.history.toMutableList().apply { add(result) }
            val matchInc = if (matched) 1 else 0

            // Award point for compatibility matches
            if (matched) {
                viewModelScope.launch(Dispatchers.IO) {
                    val stats = repository.userStats.firstOrNull() ?: UserStats()
                    repository.saveUserStats(stats.copy(connectionPoints = stats.connectionPoints + 5))
                }
            }

            if (current.currentIndex + 1 < thisOrThatItems.size) {
                _thisOrThatState.value = current.copy(
                    currentIndex = current.currentIndex + 1,
                    partnerAChoice = null,
                    partnerBChoice = null,
                    matchCount = current.matchCount + matchInc,
                    totalRounds = current.totalRounds + 1,
                    history = updatedHistory
                )
            } else {
                // End of game
                _thisOrThatState.value = current.copy(
                    partnerAChoice = null,
                    partnerBChoice = null,
                    matchCount = current.matchCount + matchInc,
                    totalRounds = current.totalRounds + 1,
                    history = updatedHistory
                )
            }
        } else {
            _thisOrThatState.value = current.copy(
                partnerAChoice = nextA,
                partnerBChoice = nextB
            )
        }
    }

    fun resetThisOrThat() {
        _thisOrThatState.value = ThisOrThatState()
    }
}

class WisaalViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WisaalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WisaalViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
