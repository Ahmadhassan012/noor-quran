package com.example.data

import java.util.Locale

data class Surah(
  val number: Int,
  val englishName: String,
  val phoneticName: String,
  val arabicName: String,
  val verseCount: Int,
  val juzStart: Int,
  val type: String // Meccan or Medinan
)

object QuranData {
  val surahs = listOf(
    Surah(1, "Al-Fatihah", "The Opening", "الفاتحة", 7, 1, "Meccan"),
    Surah(2, "Al-Baqarah", "The Cow", "البقرة", 286, 1, "Medinan"),
    Surah(3, "Ali 'Imran", "Family of Imran", "آل عمران", 200, 3, "Medinan"),
    Surah(4, "An-Nisa", "The Women", "النساء", 176, 4, "Medinan"),
    Surah(5, "Al-Ma'idah", "The Table Spread", "المائدة", 120, 6, "Medinan"),
    Surah(6, "Al-An'am", "The Cattle", "الأنعام", 165, 7, "Meccan"),
    Surah(7, "Al-A'raf", "The Heights", "الأعراف", 206, 8, "Meccan"),
    Surah(8, "Al-Anfal", "The Spoils of War", "الأنفال", 75, 9, "Medinan"),
    Surah(9, "At-Tawbah", "The Repentance", "التوبة", 129, 10, "Medinan"),
    Surah(10, "Yunus", "Jonah", "يونس", 109, 11, "Meccan"),
    Surah(11, "Hud", "Hud", "هود", 123, 11, "Meccan"),
    Surah(12, "Yusuf", "Joseph", "يوسف", 111, 12, "Meccan"),
    Surah(13, "Ar-Ra'd", "The Thunder", "الرعد", 43, 13, "Medinan"),
    Surah(14, "Ibrahim", "Abraham", "إبراهيم", 52, 13, "Meccan"),
    Surah(15, "Al-Hijr", "The Rocky Tract", "الحجر", 99, 14, "Meccan"),
    Surah(16, "An-Nahl", "The Bee", "النحل", 128, 14, "Meccan"),
    Surah(17, "Al-Isra", "The Night Journey", "الإسراء", 111, 15, "Meccan"),
    Surah(18, "Al-Kahf", "The Cave", "الكهف", 110, 15, "Meccan"),
    Surah(19, "Maryam", "Mary", "مريم", 98, 16, "Meccan"),
    Surah(20, "Taha", "Ta-Ha", "طه", 135, 16, "Meccan"),
    Surah(21, "Al-Anbiya", "The Prophets", "الأنبياء", 112, 17, "Meccan"),
    Surah(22, "Al-Hajj", "The Pilgrimage", "الحج", 78, 17, "Medinan"),
    Surah(23, "Al-Mu'minun", "The Believers", "المؤمنون", 118, 18, "Meccan"),
    Surah(24, "An-Nur", "The Light", "النور", 64, 18, "Medinan"),
    Surah(25, "Al-Furqan", "The Criterion", "الفرقان", 77, 18, "Meccan"),
    Surah(26, "Ash-Shu'ara", "The Poets", "الشعراء", 227, 19, "Meccan"),
    Surah(27, "An-Naml", "The Ant", "النمل", 93, 19, "Meccan"),
    Surah(28, "Al-Qasas", "The Stories", "القصص", 88, 20, "Meccan"),
    Surah(29, "Al-'Ankabut", "The Spider", "العنكبوت", 69, 20, "Meccan"),
    Surah(30, "Ar-Rum", "The Romans", "الروم", 60, 21, "Meccan"),
    Surah(31, "Luqman", "Luqman", "لقمان", 34, 21, "Meccan"),
    Surah(32, "As-Sajdah", "The Prostration", "السجدة", 30, 21, "Meccan"),
    Surah(33, "Al-Ahzab", "The Combined Forces", "الأحزاب", 73, 21, "Medinan"),
    Surah(34, "Saba", "Sheba", "سبأ", 54, 22, "Meccan"),
    Surah(35, "Fatir", "The Originator", "فاطر", 45, 22, "Meccan"),
    Surah(36, "Ya-Sin", "Ya Seen", "يس", 83, 22, "Meccan"),
    Surah(37, "As-Saffat", "Those Who Set The Ranks", "الصافات", 182, 23, "Meccan"),
    Surah(38, "Sad", "The Letter Sad", "ص", 88, 23, "Meccan"),
    Surah(39, "Az-Zumar", "The Troops", "الزمر", 75, 23, "Meccan"),
    Surah(40, "Ghafir", "The Forgiver", "غافر", 85, 24, "Meccan"),
    Surah(41, "Fussilat", "Explained In Detail", "فصلت", 54, 24, "Meccan"),
    Surah(42, "Ash-Shura", "The Consultation", "الشورى", 53, 25, "Meccan"),
    Surah(43, "Az-Zukhruf", "The Ornaments Of Gold", "الزخرف", 89, 25, "Meccan"),
    Surah(44, "Ad-Dukhan", "The Smoke", "الدخان", 59, 25, "Meccan"),
    Surah(45, "Al-Jathiyah", "The Crouching", "الجاثية", 37, 25, "Meccan"),
    Surah(46, "Al-Ahqaf", "The Wind-Curved Sandhills", "الأحقاف", 35, 26, "Meccan"),
    Surah(47, "Muhammad", "Muhammad", "محمد", 38, 26, "Medinan"),
    Surah(48, "Al-Fath", "The Victory", "الفتح", 29, 26, "Medinan"),
    Surah(49, "Al-Hujurat", "The Rooms", "الحجرات", 18, 26, "Medinan"),
    Surah(50, "Qaf", "The Letter Qaf", "ق", 45, 26, "Meccan"),
    Surah(51, "Adh-Dhariyat", "The Winnowing Winds", "الذاريات", 60, 26, "Meccan"),
    Surah(52, "At-Tur", "The Mount", "الطور", 49, 27, "Meccan"),
    Surah(53, "An-Najm", "The Star", "النجم", 62, 27, "Meccan"),
    Surah(54, "Al-Qamar", "The Moon", "القمر", 55, 27, "Meccan"),
    Surah(55, "Ar-Rahman", "The Beneficent", "الرحمن", 78, 27, "Medinan"),
    Surah(56, "Al-Waqi'ah", "The Inevitable", "الواقعة", 96, 27, "Meccan"),
    Surah(57, "Al-Hadid", "The Iron", "الحديد", 29, 27, "Medinan"),
    Surah(58, "Al-Mujadilah", "The Pleading Woman", "المجادلة", 22, 28, "Medinan"),
    Surah(59, "Al-Hashr", "The Exile", "الحشر", 24, 28, "Medinan"),
    Surah(60, "Al-Mumtahanah", "She That Is To Be Examined", "الممتحنة", 13, 28, "Medinan"),
    Surah(61, "As-Saff", "The Ranks", "الصف", 14, 28, "Medinan"),
    Surah(62, "Al-Jumu'ah", "The Congregation", "الجمعة", 11, 28, "Medinan"),
    Surah(63, "Al-Munafiqun", "The Hypocrites", "المنافقون", 11, 28, "Medinan"),
    Surah(64, "At-Taghabun", "The Mutual Disillusion", "التغابن", 18, 28, "Medinan"),
    Surah(65, "At-Talaq", "The Divorce", "الطلاق", 12, 28, "Medinan"),
    Surah(66, "At-Tahrim", "The Prohibition", "التحريم", 12, 28, "Medinan"),
    Surah(67, "Al-Mulk", "The Sovereignty", "الملك", 30, 29, "Meccan"),
    Surah(68, "Al-Qalam", "The Pen", "القلم", 52, 29, "Meccan"),
    Surah(69, "Al-Haqqah", "The Reality", "الحاقة", 52, 29, "Meccan"),
    Surah(70, "Al-Ma'arij", "The Ascending Stairways", "المعارج", 44, 29, "Meccan"),
    Surah(71, "Nuh", "Noah", "نوح", 28, 29, "Meccan"),
    Surah(72, "Al-Jinn", "The Jinn", "الجن", 28, 29, "Meccan"),
    Surah(73, "Al-Muzzammil", "The Enshrouded One", "المزمل", 20, 29, "Meccan"),
    Surah(74, "Al-Muddaththir", "The Cloaked One", "المدثر", 56, 29, "Meccan"),
    Surah(75, "Al-Qiyamah", "The Resurrection", "القيامة", 40, 29, "Meccan"),
    Surah(76, "Al-Insan", "The Man", "الإنسان", 31, 29, "Medinan"),
    Surah(77, "Al-Mursalat", "The Emissaries", "المرسلات", 50, 29, "Meccan"),
    Surah(78, "An-Naba", "The Tidings", "النبأ", 40, 30, "Meccan"),
    Surah(79, "An-Nazi'at", "Those Who Drag Forth", "النازعات", 46, 30, "Meccan"),
    Surah(80, "Abasa", "He Frowned", "عبس", 42, 30, "Meccan"),
    Surah(81, "At-Takwir", "The Overthrowing", "التكوير", 29, 30, "Meccan"),
    Surah(82, "Al-Infitar", "The Cleaving", "الانفطار", 19, 30, "Meccan"),
    Surah(83, "Al-Mutaffifin", "The Defrauders", "المطففين", 36, 30, "Meccan"),
    Surah(84, "Al-Inshiqaq", "The Sundering", "الانشقاق", 25, 30, "Meccan"),
    Surah(85, "Al-Buruj", "The Mansions of the Stars", "البروج", 22, 30, "Meccan"),
    Surah(86, "At-Tariq", "The Nightcomer", "الطارق", 17, 30, "Meccan"),
    Surah(87, "Al-A'la", "The Most High", "الأعلى", 19, 30, "Meccan"),
    Surah(88, "Al-Ghashiyah", "The Overwhelming", "الغاشية", 26, 30, "Meccan"),
    Surah(89, "Al-Fajr", "The Dawn", "الفجر", 30, 30, "Meccan"),
    Surah(90, "Al-Balad", "The City", "البلد", 20, 30, "Meccan"),
    Surah(91, "Ash-Shams", "The Sun", "الشمس", 15, 30, "Meccan"),
    Surah(92, "Al-Layl", "The Night", "الليل", 21, 30, "Meccan"),
    Surah(93, "Ad-Duha", "The Morning Hours", "الضحى", 11, 30, "Meccan"),
    Surah(94, "Ash-Sharh", "The Relief", "الشرح", 8, 30, "Meccan"),
    Surah(95, "At-Tin", "The Fig", "التين", 8, 30, "Meccan"),
    Surah(96, "Al-'Alaq", "The Clot", "العلق", 19, 30, "Meccan"),
    Surah(97, "Al-Qadr", "The Power", "القدر", 5, 30, "Meccan"),
    Surah(98, "Al-Bayyinah", "The Clear Proof", "البينة", 8, 30, "Medinan"),
    Surah(99, "Az-Zalzalah", "The Earthquake", "الزلزلة", 8, 30, "Medinan"),
    Surah(100, "Al-'Adiyat", "The Courser", "العاديات", 11, 30, "Meccan"),
    Surah(101, "Al-Qari'ah", "The Calamity", "القارعة", 11, 30, "Meccan"),
    Surah(102, "At-Takathur", "The Rivalry in World Increase", "التكاثر", 8, 30, "Meccan"),
    Surah(103, "Al-'Asr", "The Declining Day", "العصر", 3, 30, "Meccan"),
    Surah(104, "Al-Humazah", "The Traducer", "الهمزة", 9, 30, "Meccan"),
    Surah(105, "Al-Fil", "The Elephant", "الفيل", 5, 30, "Meccan"),
    Surah(106, "Quraysh", "Quraysh", "قريش", 4, 30, "Meccan"),
    Surah(107, "Al-Ma'un", "The Small Kindnesses", "الماعون", 7, 30, "Meccan"),
    Surah(108, "Al-Kawthar", "The Abundance", "الكوثر", 3, 30, "Meccan"),
    Surah(109, "Al-Kafirun", "The Disbelievers", "الكافرون", 6, 30, "Meccan"),
    Surah(110, "An-Nasr", "The Divine Support", "النصر", 3, 30, "Medinan"),
    Surah(111, "Al-Masad", "The Palm Fiber", "المسد", 5, 30, "Meccan"),
    Surah(112, "Al-Ikhlas", "The Sincerity", "الإخلاص", 4, 30, "Meccan"),
    Surah(113, "Al-Falaq", "The Daybreak", "الفلق", 5, 30, "Meccan"),
    Surah(114, "An-Nas", "Mankind", "الناس", 6, 30, "Meccan")
  )

  // Map of lowercase names with aliases to Surah number
  private val surahLookupMap: Map<String, Int> = buildMap {
    surahs.forEach { s ->
      val sName = s.englishName.lowercase()
      put(sName, s.number)
      put(sName.replace("-", " "), s.number)
      put(sName.replace("-", ""), s.number)
      put(sName.replace("al-", ""), s.number)
      put(sName.replace("an-", ""), s.number)
      put(sName.replace("ash-", ""), s.number)
      put(sName.replace("at-", ""), s.number)
      put(sName.replace("ar-", ""), s.number)
      put(sName.replace("as-", ""), s.number)
      put(sName.replace("az-", ""), s.number)
      put(sName.replace("ad-", ""), s.number)

      val sPhonetic = s.phoneticName.lowercase()
      put(sPhonetic, s.number)
      put(sPhonetic.replace(" ", ""), s.number)
    }
    // Custom common colloquial pronunciations or Urdu/Arabic accents
    put("fatiha", 1)
    put("fateha", 1)
    put("baqra", 2)
    put("baqarah", 2)
    put("baqra", 2)
    put("imran", 3)
    put("nisa", 4)
    put("maidah", 5)
    put("anam", 6)
    put("araf", 7)
    put("anfal", 8)
    put("taubah", 9)
    put("tobah", 9)
    put("yunus", 10)
    put("hud", 11)
    put("yousuf", 12)
    put("yusuf", 12)
    put("rad", 13)
    put("ibrahim", 14)
    put("hijr", 15)
    put("nahl", 16)
    put("isra", 17)
    put("kahf", 18)
    put("maryam", 19)
    put("meriam", 19)
    put("taha", 20)
    put("anbiya", 21)
    put("hajj", 22)
    put("mu'minun", 23)
    put("muminun", 23)
    put("noor", 24)
    put("nur", 24)
    put("furqan", 25)
    put("shuara", 26)
    put("naml", 27)
    put("qasas", 28)
    put("ankabut", 29)
    put("room", 30)
    put("rum", 30)
    put("luqman", 31)
    put("sajdah", 32)
    put("sajda", 32)
    put("ahzab", 33)
    put("saba", 34)
    put("fatir", 35)
    put("yaseen", 36)
    put("yasin", 36)
    put("saffat", 37)
    put("saad", 38)
    put("sad", 38)
    put("zumar", 39)
    put("ghafir", 40)
    put("fussilat", 41)
    put("shura", 42)
    put("zukhruf", 43)
    put("dukhan", 44)
    put("jathiyah", 45)
    put("ahqaf", 46)
    put("muhammad", 47)
    put("fath", 48)
    put("fateh", 48)
    put("hujurat", 49)
    put("qaf", 50)
    put("dhariyat", 51)
    put("tur", 52)
    put("najm", 53)
    put("qamar", 54)
    put("rahman", 55)
    put("rehman", 55)
    put("waqi'ah", 56)
    put("waqiah", 56)
    put("hadid", 57)
    put("mujadilah", 58)
    put("hashr", 59)
    put("mumtahanah", 60)
    put("saff", 61)
    put("jumu'ah", 62)
    put("jumua", 62)
    put("jummah", 62)
    put("munafiqun", 63)
    put("taghabun", 64)
    put("talaq", 65)
    put("tahrim", 66)
    put("mulk", 67)
    put("qalam", 68)
    put("haqqah", 69)
    put("ma'arij", 70)
    put("nuh", 71)
    put("jinn", 72)
    put("muzzammil", 73)
    put("muddaththir", 74)
    put("qiyamah", 75)
    put("insan", 76)
    put("mursalat", 77)
    put("naba", 78)
    put("nazi'at", 79)
    put("naziat", 79)
    put("abasa", 80)
    put("takwir", 81)
    put("infitar", 82)
    put("mutaffifin", 83)
    put("inshiqaq", 84)
    put("buruj", 85)
    put("tariq", 86)
    put("a'la", 87)
    put("ala", 87)
    put("ghashiyah", 88)
    put("ghashia", 88)
    put("fajr", 89)
    put("balad", 90)
    put("shams", 91)
    put("layl", 92)
    put("ailail", 92)
    put("duha", 93)
    put("sharh", 94)
    put("inshirah", 94)
    put("tin", 95)
    put("alaq", 96)
    put("qadr", 97)
    put("qadar", 97)
    put("bayyinah", 98)
    put("zalzalah", 99)
    put("adiyat", 100)
    put("qari'ah", 101)
    put("qariah", 101)
    put("takathur", 102)
    put("asr", 103)
    put("humazah", 104)
    put("fil", 105)
    put("feel", 105)
    put("quraysh", 106)
    put("qureish", 106)
    put("quraish", 106)
    put("ma'un", 107)
    put("maun", 107)
    put("kawthar", 108)
    put("kausar", 108)
    put("kafirun", 109)
    put("nasr", 110)
    put("masad", 111)
    put("lahab", 111)
    put("ikhlas", 112)
    put("falaq", 113)
    put("nas", 114)
  }

  fun findSurahNumber(name: String): Int? {
    val clean = name.lowercase().trim()
      .replace("' ", "")
      .replace("'", "")
      .replace("`", "")
      .replace("’", "")
      .replace("the ", "")
      .replace("chapter ", "")
      .replace("surah ", "")
      .replace("sura ", "")
      .replace("-", "")
    
    // Exact lookup
    surahLookupMap[clean]?.let { return it }

    // Fuzzy matching / substring
    for ((key, value) in surahLookupMap) {
      if (clean == key || clean.contains(key) || key.contains(clean)) {
        return value
      }
    }
    return null
  }
}
