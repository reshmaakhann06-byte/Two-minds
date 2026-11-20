package com.example.data.content

object CuratedContent {

    data class PrayerInfo(val name: String, val time: String, val sunnahNote: String)
    val dailyPrayers = listOf(
        PrayerInfo("Fajr", "05:12 AM", "2 Sunnah + 2 Fard"),
        PrayerInfo("Dhuhr", "12:45 PM", "4 Sunnah + 4 Fard + 2 Sunnah"),
        PrayerInfo("Asr", "04:32 PM", "4 Sunnah + 4 Fard"),
        PrayerInfo("Maghrib", "06:58 PM", "3 Fard + 2 Sunnah"),
        PrayerInfo("Isha", "08:20 PM", "4 Fard + 2 Sunnah + 3 Witr")
    )

    data class TasbeehPreset(val title: String, val arabic: String, val target: Int, val virtue: String)
    val tasbeehPresets = listOf(
        TasbeehPreset("SubhanAllah", "سُبْحَانَ اللَّهِ", 100, "Glory be to Allah — fills the scales of good deeds."),
        TasbeehPreset("Alhamdulillah", "الْحَمْدُ لِلَّهِ", 100, "All praise belongs to Allah — gratitude increases blessings."),
        TasbeehPreset("Allahu Akbar", "اللَّهُ أَكْبَرُ", 100, "Allah is the Greatest — anchors the heart above worldly worry."),
        TasbeehPreset("Astaghfirullah", "أَسْتَغْفِرُ اللَّهَ", 100, "Seeking forgiveness opens doors of ease and rizq."),
        TasbeehPreset("Durood Sharif", "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", 100, "10x mercy returned for every single salawat sent."),
        TasbeehPreset("SubhanAllahi wa Bihamdihi", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", 100, "Light on the tongue, heavy on the scale.")
    )

    data class DuaEntry(
        val id: String,
        val category: String, // "Morning", "Evening", "Night", "Small Duas", "Big Duas"
        val title: String,
        val arabic: String,
        val transliteration: String,
        val translation: String,
        val repeatCount: String
    )

    val duasList = listOf(
        DuaEntry(
            "d1", "Small Duas", "For Beneficial Knowledge & Rizq",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا",
            "Allahumma inni as'aluka 'ilman nafi'an, wa rizqan tayyiban, wa 'amalan mutaqabbalan.",
            "O Allah, I ask You for beneficial knowledge, pure provision, and accepted deeds.",
            "1x after Fajr"
        ),
        DuaEntry(
            "d2", "Small Duas", "Increase in Knowledge",
            "رَبِّ زِدْنِي عِلْمًا",
            "Rabbi zidni 'ilma.",
            "My Lord, increase me in knowledge.",
            "3x before study"
        ),
        DuaEntry(
            "d3", "Morning", "Morning Gratitude & Protection",
            "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ",
            "Asbahna wa asbahal-mulku lillah, wal-hamdulillah.",
            "We have reached the morning and at this very time all sovereignty belongs to Allah.",
            "1x every morning"
        ),
        DuaEntry(
            "d4", "Evening", "Seeking Protection from Harm",
            "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            "Bismillahil-ladhi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
            "In the name of Allah, with whose name nothing on earth or in heaven can cause harm.",
            "3x Morning & Evening"
        ),
        DuaEntry(
            "d5", "Night", "Before Sleeping",
            "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            "Bismika Allahumma amutu wa ahya.",
            "In Your name, O Allah, I die and I live.",
            "1x at bedtime"
        ),
        DuaEntry(
            "d6", "Big Duas", "Sayyidul Istighfar (Master Supplication for Forgiveness)",
            "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'udhu bika min sharri ma sana'tu, abu'u laka bini'matika 'alayya, wa abu'u bidhanbi faghfir li fa-innahu la yaghfirudh-dhunuba illa Anta.",
            "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant... Forgive me, for none forgives sins except You.",
            "1x Morning & Evening"
        ),
        DuaEntry(
            "d7", "Big Duas", "Dua for Strength, Guidance & Halal Independence",
            "اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ، وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ",
            "Allahummak-fini bihalalika 'an haramika, wa aghnini bifadlika 'amman siwaka.",
            "O Allah, suffice me with what You have made lawful against what You have forbidden, and enrich me by Your grace from needing anyone besides You.",
            "3x daily for barakah in business"
        )
    )

    val sunnatItems = listOf(
        "Brush teeth (Miswak / after waking)",
        "Recite Ayat-ul-Kursi after every Fard prayer",
        "Eat & drink with the right hand",
        "Say Bismillah before starting work or meals",
        "Greet your brother with Assalamu Alaikum & a smile",
        "Sleep early after Isha on your right side"
    )

    data class SurahLesson(
        val number: Int,
        val name: String,
        val arabicName: String,
        val ayahs: Int,
        val keyTheme: String,
        val studyArabicSample: String,
        val studyTranslation: String,
        val tajweedTip: String
    )

    val surahLessons = listOf(
        SurahLesson(
            1, "Surah Al-Fatihah", "الفاتحة", 7,
            "The Opening — Core prayer & seeking the straight path",
            "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
            "You alone we worship, and You alone we ask for help.",
            "Hold the Shaddah cleanly on Iyyaka without rushing the double ya."
        ),
        SurahLesson(
            2, "Surah Al-Baqarah", "البقرة", 286,
            "Guidance for the conscious, Ayatul Kursi & steadfastness",
            "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
            "Allah does not burden a soul beyond what it can bear.",
            "Apply Ghunnah (2 counts nasalization) on 'nafsan' with Tanween."
        ),
        SurahLesson(
            29, "Surah Al-Ankabut", "العنكبوت", 69,
            "Striving in faith & self-accountability (29:6)",
            "وَمَن جَاهَدَ فَإِنَّمَا يُجَاهِدُ لِنَفْسِهِ",
            "And whoever strives only strives for himself.",
            "Apply Ikhfa on 'man jahada' and Madd of 2 counts on 'yujahidu'."
        ),
        SurahLesson(
            67, "Surah Al-Mulk", "الملك", 30,
            "Protection from the grave & reflection on creation",
            "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            "Blessed is He in whose hand is dominion, and He is over all things competent.",
            "Give Qalqalah echo on the letter Dal in 'qadir' only if stopping on a sukoon letter like 'khalq'."
        ),
        SurahLesson(
            55, "Surah Ar-Rahman", "الرحمن", 78,
            "Gratitude for countless blessings",
            "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ",
            "So which of the favors of your Lord would you deny?",
            "Elongate Madd Badal and Madd Munfasil smoothly at 4 counts."
        )
    )

    data class BookInfo(
        val id: String,
        val title: String,
        val author: String,
        val assignedTo: String, // "Me", "Ubaid", "Both"
        val category: String,
        val defaultProgress: Int,
        val keyInsights: List<String>
    )

    val builtInBooks = listOf(
        BookInfo(
            "thinking_clearly",
            "The Art of Thinking Clearly",
            "Rolf Dobelli",
            "Me",
            "Cognitive Biases & Decision Making",
            68,
            listOf(
                "Survivorship Bias: Study failures as closely as winners before judging odds.",
                "Swimmer's Body Illusion: Separate selection criteria from actual cause of results.",
                "Sunk Cost Fallacy: Base decisions strictly on future value, never past spend.",
                "Confirmation Bias: Actively hunt for evidence that disproves your favorite thesis."
            )
        ),
        BookInfo(
            "made_to_stick",
            "Made to Stick",
            "Chip Heath & Dan Heath",
            "Ubaid",
            "Communication & Marketing",
            42,
            listOf(
                "SUCCESs Framework: Simple, Unexpected, Concrete, Credible, Emotional, Stories.",
                "Curse of Knowledge: Once you know something, it's hard to imagine not knowing it.",
                "Simple = Core + Compact: Find the single core idea and strip everything else.",
                "Concrete imagery beats abstract corporate jargon in every marketing campaign."
            )
        ),
        BookInfo(
            "reading_minds",
            "The Art of Reading Minds",
            "Henrik Fexeus",
            "Both",
            "Psychology & Nonverbal Influence",
            78,
            listOf(
                "Rapport through subtle pacing & mirroring builds unconscious trust in minutes.",
                "Watch micro-shifts in posture, breathing rate, and eye access cues.",
                "Framing questions with anchored emotions guides negotiations ethically.",
                "Observe baseline behavior before interpreting any single body-language gesture."
            )
        ),
        BookInfo(
            "psychology_focus",
            "Deep Focus & Behavioral Psychology Guide",
            "Two Minds Curated Study",
            "Both",
            "Psychology & Habits",
            55,
            listOf(
                "Implementation Intentions: 'At [Time], I will [Action] in [Location]' doubles follow-through.",
                "Environment Design beats raw willpower — remove friction from deep work.",
                "Dopamine Detox: Protect the first 90 minutes of the morning from short-form feeds."
            )
        )
    )

    data class SkillModule(
        val id: String,
        val track: String, // "UI/UX Design" or "Digital Marketing"
        val owner: String, // "Me" or "Ubaid"
        val title: String,
        val description: String,
        val exercise: String
    )

    val skillModules = listOf(
        // UI/UX Design (Me)
        SkillModule("ux1", "UI/UX Design", "Me", "Visual Hierarchy & 8pt Grid", "Master spacing rhythm, typographic scale, and focal contrast.", "Audit 2 screens today and align every padding value to multiples of 4dp/8dp."),
        SkillModule("ux2", "UI/UX Design", "Me", "Design Systems & Tokens", "Semantic color tokens, light/dark surface elevation, and reusable components.", "Define primary, surface, border, and state tokens before designing cards."),
        SkillModule("ux3", "UI/UX Design", "Me", "User Flows & Micro-Interactions", "Reduce cognitive load per screen and craft spring-based feedback.", "Map a 3-step onboarding flow with zero dead-end states."),
        SkillModule("ux4", "UI/UX Design", "Me", "Accessibility & Usability Testing", "48dp touch targets, WCAG AA contrast, and 5-user task testing.", "Run a 5-minute critique on your latest Figma prototype."),
        // Digital Marketing (Ubaid)
        SkillModule("dm1", "Digital Marketing", "Ubaid", "Hook-Story-Offer Copywriting", "Write high-converting hooks using Made to Stick principles.", "Draft 3 ad headlines focusing on concrete customer pain points."),
        SkillModule("dm2", "Digital Marketing", "Ubaid", "Meta & Google Ads Funnel", "Structure TOFU/MOFU/BOFU campaigns with ROAS & CPA tracking.", "Calculate break-even CPA and target CTR for a sample product."),
        SkillModule("dm3", "Digital Marketing", "Ubaid", "SEO & Organic Distribution", "Keyword intent mapping, on-page structure, and retention loops.", "Outline 1 high-intent pillar article + 3 short-form reels."),
        SkillModule("dm4", "Digital Marketing", "Ubaid", "Analytics & A/B Split Testing", "Test one variable at a time: creative, headline, or landing CTA.", "Review funnel drop-off between landing view and checkout.")
    )

    data class LanguageLesson(
        val id: String,
        val language: String, // "Turkish" (Me) or "Spanish" (Ubaid)
        val phrase: String,
        val pronunciation: String,
        val english: String,
        val category: String,
        val ttsLocaleTag: String
    )

    val languageLessons = listOf(
        // Turkish for Me
        LanguageLesson("tr1", "Turkish", "Merhaba, nasılsın?", "Mehr-ha-ba, na-suhl-suhn", "Hello, how are you?", "Greeting", "tr-TR"),
        LanguageLesson("tr2", "Turkish", "İyiyim, teşekkür ederim.", "Ee-yee-yim, teh-shek-koor eh-deh-rim", "I am good, thank you.", "Greeting", "tr-TR"),
        LanguageLesson("tr3", "Turkish", "Bugün çok çalışmamız lazım.", "Boo-gyoon chok chah-luhsh-mah-muhz lah-zuhm", "Today we need to work hard.", "Business", "tr-TR"),
        LanguageLesson("tr4", "Turkish", "Bu proje çok güzel görünüyor.", "Boo pro-zheh chok gyoo-zel gyoh-roo-nyoor", "This project looks very good.", "Design", "tr-TR"),
        LanguageLesson("tr5", "Turkish", "Sabır ve disiplin başarı getirir.", "Sah-buhr veh dee-see-plin bah-shah-ruh geh-tee-rir", "Patience and discipline bring success.", "Mindset", "tr-TR"),
        LanguageLesson("tr6", "Turkish", "Allah'a emanet ol.", "Al-lah-ah eh-mah-net ol", "Be entrusted to Allah (Goodbye).", "Daily", "tr-TR"),
        // Spanish for Ubaid
        LanguageLesson("es1", "Spanish", "Hola, ¿cómo estás hoy?", "Oh-lah, koh-moh es-tahs oy", "Hello, how are you today?", "Greeting", "es-ES"),
        LanguageLesson("es2", "Spanish", "Estoy listo para trabajar.", "Es-toy lees-toh pah-rah trah-bah-har", "I am ready to work.", "Business", "es-ES"),
        LanguageLesson("es3", "Spanish", "Necesitamos mejorar la estrategia.", "Neh-seh-see-tah-mohs meh-hoh-rar lah es-trah-teh-hyah", "We need to improve the strategy.", "Marketing", "es-ES"),
        LanguageLesson("es4", "Spanish", "La constancia es la clave del éxito.", "Lah kon-stahn-syah es lah klah-veh del ek-see-toh", "Consistency is the key to success.", "Mindset", "es-ES"),
        LanguageLesson("es5", "Spanish", "¿Cuánto cuesta este servicio?", "Kwahn-toh kwes-tah es-teh ser-vee-syoh", "How much does this service cost?", "Business", "es-ES"),
        LanguageLesson("es6", "Spanish", "Muchas gracias por tu ayuda.", "Moo-chahs grah-syahs por too ah-yoo-dah", "Thank you very much for your help.", "Daily", "es-ES")
    )

    // Verbatim from Home_Fitness_Weekly_Plan_Detailed_f2365a98.pdf
    data class WorkoutExercise(
        val name: String,
        val dose: String,
        val howTo: String,
        val avoid: String
    )

    data class WorkoutDay(
        val dayShort: String,
        val dayFull: String,
        val focus: String,
        val exercises: List<WorkoutExercise>
    )

    val weeklyWorkouts = listOf(
        WorkoutDay(
            "Mon", "Monday", "Chest + Triceps",
            listOf(
                WorkoutExercise("Incline push-up", "2 sets x 6-10 • rest 60-90 sec", "Hands on a secure counter, slightly wider than shoulders. Walk feet back so body is straight. Bend elbows diagonally back, bring chest toward counter, then press away.", "Hips sagging, elbows flaring straight out, shrugging or unstable furniture."),
                WorkoutExercise("Wall push-up", "1 set x 8-12 • rest 60 sec", "Face wall, hands at chest height and shoulder-width. Step back slightly. Keep body straight; bend elbows to bring chest toward wall, then push back.", "Bending at waist or letting head poke forward."),
                WorkoutExercise("Close-grip wall push-up", "1 set x 6-8 • rest 60 sec", "Hands just inside shoulder width on wall. Keep elbows near sides, lower chest toward wall and press away.", "Hands too close together or elbows flaring."),
                WorkoutExercise("Forearm plank", "2 holds x 15-20 sec • rest 45 sec", "Elbows under shoulders, forearms on floor. Extend legs and keep head, hips and heels in one line. Tighten tummy gently and breathe.", "Hips sagging or too high, holding breath or pushing through pain.")
            )
        ),
        WorkoutDay(
            "Tue", "Tuesday", "Back + Biceps",
            listOf(
                WorkoutExercise("Light backpack row", "2x8-12 • rest 60-90 sec", "Put only a few light books in a closed backpack. Hinge at hips, knees soft and back neutral. Pull elbows toward ribs, pause, lower slowly.", "Rounding back, jerking, twisting or using a heavy bag."),
                WorkoutExercise("Towel row hold", "2 holds x 10 sec • rest 45 sec", "Sit tall, loop a towel around both feet only if secure. Hold ends and gently pull elbows back while feet resist. Keep breathing.", "Yanking towel, rounded back, or continue if towel slips."),
                WorkoutExercise("Bottle curl", "2x8-12 • rest 60 sec", "Hold two light water bottles by sides. Keep elbows close to ribs; bend elbows to lift bottles, pause, lower slowly.", "Swinging torso or using heavy bottles."),
                WorkoutExercise("Bird-dog", "2x6 each side • rest 45 sec", "Start on hands and knees. Extend opposite arm and leg without rotating hips; pause briefly, return and switch sides.", "Arching lower back or letting hips tilt.")
            )
        ),
        WorkoutDay(
            "Wed", "Wednesday", "Legs",
            listOf(
                WorkoutExercise("Bodyweight squat", "2x8-12 • rest 60-90 sec", "Feet about shoulder-width. Send hips back and bend knees, keeping knees in line with toes. Lower comfortably, then push through whole foot to stand.", "Knees collapsing inward, heels lifting or forcing extra depth."),
                WorkoutExercise("Reverse lunge", "2x6 each leg • rest 60-90 sec", "Stand tall, step one foot back and bend both knees gently. Keep front foot flat and knee tracking over toes. Push through front foot to return.", "Wobbling, overstriding, front knee caving inward or slamming back knee."),
                WorkoutExercise("Glute bridge", "2x10-12 • rest 60 sec", "Lie on back, knees bent, feet flat. Tighten tummy gently, lift hips until shoulders-hips-knees align, then lower slowly.", "Overarching lower back or pushing through neck."),
                WorkoutExercise("Calf raise", "2x12-15 • rest 45 sec", "Stand near wall. Rise onto balls of feet, pause, then lower slowly with control.", "Bouncing or rolling ankles outward.")
            )
        ),
        WorkoutDay(
            "Thu", "Thursday", "Shoulders + Abs",
            listOf(
                WorkoutExercise("Light bottle shoulder press", "2x8-10 • rest 60 sec", "Hold very light bottles at shoulder height. Keep ribs down and tummy gently tight. Press overhead without arching back, then lower slowly.", "Heavy bottles, shrugging or arching lower back."),
                WorkoutExercise("Wall slides", "2x8 • rest 45 sec", "Stand with back near wall. Bend elbows like a goalpost; slide arms upward only as far as comfortable, then return slowly.", "Forcing arms overhead or shrugging."),
                WorkoutExercise("Dead bug", "2x6 each side • rest 45 sec", "Lie on back, arms up and knees bent. Gently brace tummy. Slowly extend opposite arm and leg; keep lower back gently against floor, return and switch.", "Lower back arching away or moving too fast."),
                WorkoutExercise("Knee side-plank", "2x10-15 sec each side • rest 45 sec", "Lie on side with knees bent, elbow under shoulder. Lift hips so shoulder-hip-knee align. Hold while breathing, then lower.", "Shoulder collapsing, hips rolling forward or holding breath.")
            )
        ),
        WorkoutDay(
            "Fri", "Friday", "Full Body",
            listOf(
                WorkoutExercise("Bodyweight squat", "2x10 • rest 60 sec", "Feet shoulder-width, hips back and knees tracking toes. Lower comfortably and stand with control.", "Knees collapsing inward or heels lifting."),
                WorkoutExercise("Incline push-up", "2x6-10 • rest 60-90 sec", "Hands on secure counter, body straight. Lower chest toward support with elbows angled back, then press away.", "Sagging hips, flared elbows or unstable surface."),
                WorkoutExercise("Light backpack row", "2x10 • rest 60-90 sec", "Light closed bag, hip hinge and neutral back. Pull elbows toward ribs, pause, lower slowly.", "Heavy bag, rounded back or jerking."),
                WorkoutExercise("Glute bridge", "2x12 • rest 60 sec", "Lie with knees bent and feet flat. Lift hips using glutes until body forms a gentle line, lower slowly.", "Overarching lower back."),
                WorkoutExercise("Dead bug", "2x6 each side • rest 45 sec", "On back, arms up and knees bent. Extend opposite arm and leg slowly while keeping lower back steady; switch.", "Arching back or rushing.")
            )
        ),
        WorkoutDay(
            "Sat", "Saturday", "Light Cardio + Core",
            listOf(
                WorkoutExercise("Easy cardio", "20-25 min • easy pace", "Choose a comfortable walk, cycling, dancing or a sport. You should be able to talk while moving.", "All-out sprinting or pushing through dizziness."),
                WorkoutExercise("Bird-dog", "2x6 each side • rest 45 sec", "On hands and knees, extend opposite arm and leg, keep hips level, return and switch.", "Twisting torso or arching back."),
                WorkoutExercise("Forearm plank", "2x15-20 sec • rest 45 sec", "Elbows under shoulders, legs extended, body in a straight line. Brace gently and breathe.", "Hips sagging, too high or breath-holding.")
            )
        ),
        WorkoutDay(
            "Sun", "Sunday", "Rest + Recovery",
            listOf(
                WorkoutExercise("Active Recovery", "15-20 min optional walk", "No planned strength workout. Let your muscles recover. Optional relaxed walk, gentle mobility or a fun game. Eat normal meals and sleep well.", "Do not train to failure or skip rest when sore.")
            )
        )
    )

    data class MealEntry(val timeMeal: String, val whatToEat: String, val proteinGuide: String)
    val dailyMeals = listOf(
        MealEntry("7:00 AM • Breakfast", "Option A: 2 eggs + 2 small rotis + fruit.\nOption B: oats (40g) cooked in 250 ml milk + banana.\nOption C: poha + one bowl curd.", "Approx. 12-18g protein"),
        MealEntry("10:30 AM • Mid-morning", "1 seasonal fruit + 25-30g roasted chana OR a small handful of peanuts.", "Approx. 5-7g protein"),
        MealEntry("1:30 PM • Lunch", "2 rotis OR 1-1.5 cups cooked rice + 1 bowl dal + seasonal sabzi + 1/2-1 bowl curd. Add egg if available.", "Approx. 14-20g protein"),
        MealEntry("4:30 PM • Pre-workout", "1 banana + small handful roasted chana OR a glass of milk. Eat 45-60 min before workout.", "Approx. 4-8g protein"),
        MealEntry("6:10 PM • Post-workout", "250 ml milk + fruit OR 2 eggs + roti OR curd with boiled chana.", "Approx. 8-14g protein"),
        MealEntry("7:00 PM • Evening snack", "Boiled chana chaat / sprouts chaat OR curd + fruit.", "Approx. 6-10g protein"),
        MealEntry("8:00 PM • Dinner", "2 rotis or rice + dal / egg curry / chicken (if family cooks it) + vegetables.", "Approx. 15-25g protein")
    )

    val cheapProteinGuide = listOf(
        "Milk (250 ml)" to "8g protein",
        "Egg (1 large)" to "6g protein",
        "Dal cooked (1 bowl / 150g)" to "7-9g protein",
        "Curd (150g)" to "5-6g protein",
        "Roasted chana (30g)" to "6g protein",
        "Peanuts (30g)" to "7-8g protein"
    )

    data class TradingLesson(val title: String, val badge: String, val summary: String, val checklist: String)
    val tradingLessons = listOf(
        TradingLesson("Risk Management (1-2% Rule)", "Essential", "Never risk more than 1-2% of total capital on a single setup. Protect downside first.", "Always calculate stop-loss distance before clicking Buy/Sell."),
        TradingLesson("Candlestick Structure & Rejection", "Price Action", "Pin bars, engulfing candles, and volume spikes at key support/resistance show institutional flow.", "Wait for candle close; avoid chasing mid-candle wicks."),
        TradingLesson("Trend & Moving Averages (SMA 7/25)", "Technical", "When short SMA crosses above long SMA with rising volume, momentum favors continuation.", "Trade with higher-timeframe trend, not against it."),
        TradingLesson("Trading Psychology & Journaling", "Mindset", "Avoid FOMO and revenge trading. Log why you entered, your target, and emotional state.", "Take a 30-minute break after any 2 consecutive losses.")
    )

    val defaultChecklistTitles = listOf(
        "Fajr Prayer",
        "Quran Reading (15 min)",
        "Tasbeeh (100x)",
        "Book Reading (12+ Pages)",
        "Workout & Water Plan",
        "Business & Drive Sync",
        "Skill & Language Practice"
    )
}
