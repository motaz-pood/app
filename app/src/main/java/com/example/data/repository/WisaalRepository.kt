package com.example.data.repository

import com.example.data.local.WisaalDao
import com.example.data.models.Challenge
import com.example.data.models.Question
import com.example.data.models.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class WisaalRepository(private val wisaalDao: WisaalDao) {

    val allChallenges: Flow<List<Challenge>> = wisaalDao.getAllChallenges()
    val userStats: Flow<UserStats?> = wisaalDao.getUserStatsFlow()
    val completedChallengesCount: Flow<Int> = wisaalDao.getCompletedChallengesCountFlow()
    val discussedQuestionsCount: Flow<Int> = wisaalDao.getDiscussedQuestionsCountFlow()

    fun getQuestionsByCategory(category: String): Flow<List<Question>> =
        wisaalDao.getQuestionsByCategory(category)

    fun getFavoriteQuestions(): Flow<List<Question>> =
        wisaalDao.getFavoriteQuestions()

    val allQuestions: Flow<List<Question>> = wisaalDao.getAllQuestions()

    suspend fun insertChallenge(challenge: Challenge) = wisaalDao.insertChallenge(challenge)

    suspend fun updateChallenge(challenge: Challenge) = wisaalDao.updateChallenge(challenge)

    suspend fun deleteChallenge(challenge: Challenge) = wisaalDao.deleteChallenge(challenge)

    suspend fun insertQuestion(question: Question) = wisaalDao.insertQuestion(question)

    suspend fun updateQuestion(question: Question) = wisaalDao.updateQuestion(question)

    suspend fun deleteQuestion(question: Question) = wisaalDao.deleteQuestion(question)

    suspend fun saveUserStats(stats: UserStats) = wisaalDao.insertUserStats(stats)

    suspend fun updateUserStats(stats: UserStats) = wisaalDao.updateUserStats(stats)

    // Prepopulate DB if empty
    suspend fun prepopulateIfNeeded() {
        val currentStats = wisaalDao.getUserStats()
        if (currentStats == null) {
            wisaalDao.insertUserStats(UserStats(id = 1))
        }

        val challenges = wisaalDao.getAllChallenges().first()
        if (challenges.isEmpty()) {
            val defaultChallenges = listOf(
                Challenge(text = "اكتب رسالة ورقية صغيرة لشريكك تخبره فيها بـ 3 أشياء تحبها فيه وخبئها في مكان يجده صدفة."),
                Challenge(text = "اصنع كوباً من الشاي أو القهوة الدافئة لشريكك وقدمه له بابتسامة وقبلة دافئة."),
                Challenge(text = "خصِّص 10 دقائق الليلة للنظر في عيني شريكك بصمت واستمعا لنبضات قلوبكما."),
                Challenge(text = "اصنعا معاً عشاءً بسيطاً وخططا لتناوله تحت ضوء الشموع بدون هواتف."),
                Challenge(text = "اتصلا ببعضكما في منتصف اليوم فقط لقول 'أنا أحب وجودك في حياتي، يومك سعيد'."),
                Challenge(text = "خذا نزهة قصيرة ممسكي الأيدي وتحدثا فقط عن أجمل لحظة قضيتموها هذا الأسبوع."),
                Challenge(text = "قم بتدليك كتفي أو قدمي شريكك برفق لمدة 10 دقائق لمساعدته على الاسترخاء بعد يوم طويل.")
            )
            for (challenge in defaultChallenges) {
                wisaalDao.insertChallenge(challenge)
            }
        }

        val questions = wisaalDao.getAllQuestions().first()
        if (questions.isEmpty()) {
            val defaultQuestions = listOf(
                // Deep Questions
                Question(text = "ما هي أكثر لحظة شعرت فيها بأننا روح واحدة في جسدين؟", category = "deep"),
                Question(text = "إذا كان بإمكانك تغيير شيء واحد في طريقتنا في حل المشاكل، ماذا سيكون؟", category = "deep"),
                Question(text = "ما هو الخوف الأكبر لديك بشأن علاقتنا، وكيف يمكنني مساعدتك في تبديده؟", category = "deep"),
                Question(text = "ما الذي يجعلك تشعر بالأمان والتقدير التام معي؟ صف لي تصرّفاً محدداً.", category = "deep"),
                Question(text = "إذا عاد بنا الزمن، هل هناك شيء كنت تتمنى لو عرفته عني في بداية تعارفنا؟", category = "deep"),

                // Playful Questions
                Question(text = "إذا كنا في مغامرة خيالية، ما هي القوة الخارقة التي تمتلكها وما القوة التي أمتلكها أنا؟", category = "playful"),
                Question(text = "ما هي أكثر عادة غريبة أو مضحكة لدي تحبها بالسر؟", category = "playful"),
                Question(text = "إذا فزنا بمليون دولار اليوم، ما هو أول شيء مجنون سنشتريه معاً؟", category = "playful"),
                Question(text = "لو كنت طبقاً رئيسياً في مطعم، أي طبق ستكون وبأي صلصة؟", category = "playful"),
                Question(text = "ما هو اللقب السري المضحك الذي تود إطلاقه علي بيننا فقط؟", category = "playful"),

                // Future Questions
                Question(text = "أين ترى علاقتنا بعد خمس سنوات من الآن، وما الحلم الأكبر الذي تود تحقيقه معي؟", category = "future"),
                Question(text = "كيف تبدو شيخوختنا معاً في نظرك؟ صف لي يوماً عادياً سنعيشه عندما نكبر.", category = "future"),
                Question(text = "ما هو البلد أو المدينة التي تحلم بالسفر إليها معي لنقضي فيها وقتاً طويلاً؟", category = "future"),
                Question(text = "كيف يمكنني مساعدتك ودعمك لتحقيق أهدافك المهنية أو الشخصية هذا العام؟", category = "future"),

                // Memories Questions
                Question(text = "هل تتذكر أول فكرة خطرت ببالك عندما رأيتني للمرة الأولى؟ شاركني إياها بالتفصيل.", category = "memories"),
                Question(text = "ما هي أجمل ذكرى سفر أو نزهة قضيناها معاً، ولماذا هي مميزة في قلبك؟", category = "memories"),
                Question(text = "ما هي الهدية البسيطة التي قدمتها لك وظلت محفورة في ذاكرتك حتى الآن؟", category = "memories"),
                Question(text = "ما هي الأغنية التي عندما تسمعها تذكرك بي وبأوقاتنا السعيدة فوراً؟", category = "memories")
            )
            for (q in defaultQuestions) {
                wisaalDao.insertQuestion(q)
            }
        }
    }
}
