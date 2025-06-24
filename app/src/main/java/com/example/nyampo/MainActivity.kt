package com.example.nyampo

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nyampo.ui.AttendanceDialog
import com.example.nyampo.ui.ClosetDialog
import com.example.nyampo.ui.FeedDialog
import com.example.nyampo.ui.MoneyDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    // Firebase reference for current user(firebase 연결용)
    private lateinit var prefs: SharedPreferences
    private lateinit var userId: String
    private val userRef by lazy {
        FirebaseDatabase
            .getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
            .child(userId)
    }

    //초기 사용자의 고정값
    private var leafCount = 0
    private var moneyCount = 0
    private var levelCount = 1

    //preference keys
    private val PREFS_NAME = "GamePrefs"
    private val KEY_LEAF = "leafCount"
    private val KEY_MONEY = "moneyCount"
    private val KEY_LEVEL = "levelCount"
    private val KEY_MASCOT = "selectedMascotIndex"
    private val KEY_BACKGROUND = "selectedBackgroundIndex"

    //view binding
    private val leafTextView: TextView by lazy { findViewById(R.id.leafNumberText) }
    private val moneyTextView: TextView by lazy { findViewById(R.id.moneyNumberText) }
    private val levelTextView: TextView by lazy { findViewById(R.id.heartNumberText) }
    private val checkIcon: ImageView by lazy { findViewById(R.id.check_icon) }
    private val userText: TextView by lazy { findViewById(R.id.userText) }

    private val mascotViews: List<ImageButton> by lazy {
        listOf(
            findViewById(R.id.imageButton_haero),
            findViewById(R.id.imageButton_toro),
            findViewById(R.id.imageButton_tino)
        )
    }

    private val backgrounds: List<ImageView> by lazy {
        listOf(
            findViewById(R.id.background_base),
            findViewById(R.id.background_oido),
            findViewById(R.id.background_park),
            findViewById(R.id.background_wavepark),
            findViewById(R.id.background_tuk)
        )
    }

    //firebase 실시간 동기화 함수
    fun syncStatsToFirebase(feed: Int, money: Int, level: Int) {
        val updates = mapOf(
            "feed" to feed,
            "money" to money,
            "level" to level
        )
        userRef.updateChildren(updates)
            .addOnFailureListener { e ->
                Log.e("FirebaseSync", "동기화 실패: ${e.message}")
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        userId = intent.getStringExtra("userId") ?: ""

        fetchUserFeedAndMoney() //Firebase에서 값 가져오기
        checkAndUnlockCharacter() //레벨 10되면 캐릭터 언락
        fetchNickname()
        showWelcomePopupIfNeeded()
        observeUserData() //실시간 DB 연동

        val mascotFromIntent = intent.getStringExtra("selected_mascot")
        if (mascotFromIntent != null) {
            val mascotIndex = when (mascotFromIntent) {
                "hero" -> 0
                "toro" -> 1
                else -> 2
            }
            changeMascot(mascotIndex)
            prefs.edit().putInt(KEY_MASCOT, mascotIndex).apply()
        } else {
            //selectedCharacter 기준 초기화
            val userRef =
                FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users").child(userId)

            userRef.child("selectedCharacter").get().addOnSuccessListener { snapshot ->
                val selected = snapshot.getValue(String::class.java)
                val mascotIndex = when (selected) {
                    "hero" -> 0
                    "toro" -> 1
                    else -> 2 // fallback (예: tino)
                }
                changeMascot(mascotIndex)
                prefs.edit().putInt(KEY_MASCOT, mascotIndex).apply()
            }
        }


        val savedBackgroundIndex = prefs.getInt(KEY_BACKGROUND, 0)
        changeBackground(savedBackgroundIndex, backgrounds)

        val progressBar = findViewById<ProgressBar>(R.id.ProgressBar)
        progressBar.max = 10
        progressBar.progress = 0


        // --- 버튼 및 UI 설정 ---
        val haeroButton = findViewById<ImageButton>(R.id.imageButton_haero)
        val tinoButton = findViewById<ImageButton>(R.id.imageButton_tino)
        val settingButton = findViewById<ImageButton>(R.id.imageButton_setting)
        val calendarButton = findViewById<ImageButton>(R.id.imageButton_calendar)
        val walkButton = findViewById<ImageButton>(R.id.imageButton_walk)
        val feedButton = findViewById<Button>(R.id.button_feed)
        val moneyButton = findViewById<Button>(R.id.button_money)
        val qrButton = findViewById<ImageButton>(R.id.imageButton_qr)
        val adButton = findViewById<ImageButton>(R.id.imageButton_ad)
        val mailButton = findViewById<ImageButton>(R.id.imageButton_mail)
        val closetButton = findViewById<ImageButton>(R.id.imageButton_closet)

        val attendancePrefs = getSharedPreferences("AttendancePrefs", Context.MODE_PRIVATE)
        val todayKey =
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(System.currentTimeMillis())
        checkIcon.visibility =
            if (attendancePrefs.getBoolean(todayKey, false)) View.VISIBLE else View.GONE

        //firebase에 획득한 background 저장
        val bgIndexFromQR = intent.getIntExtra("background_index", -1)
        if (bgIndexFromQR != -1) {
            changeBackground(bgIndexFromQR, backgrounds)
            prefs.edit().putInt(KEY_BACKGROUND, bgIndexFromQR).apply()

            val bgKey = when (bgIndexFromQR) {
                0 -> "background_base"
                1 -> "background_oido"
                2 -> "background_park"
                3 -> "background_wavepark"
                4 -> "background_tuk"
                else -> null
            }

            if (bgKey != null) {
                val userRef =
                    FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("users").child(userId)

                userRef.child("backgrounds").get().addOnSuccessListener { snapshot ->
                    val current = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                        .toMutableSet()
                    if (!current.contains(bgKey)) {
                        current.add(bgKey)
                        userRef.child("backgrounds").setValue(current.toList())
                    }
                }
            }
        }

        haeroButton.setOnClickListener { showFloatingHearts() }
        tinoButton.setOnClickListener { showFloatingHearts() }

        qrButton.setOnClickListener {
            val intent = Intent(this, QrScannerActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        adButton.setOnClickListener {
            com.example.nyampo.ui.AdDialog.show(this)
        }

        //공지사항 버튼
        mailButton.setOnClickListener {
            com.example.nyampo.ui.NoticeDialog.show(this)
        }

        //설정 버튼
        settingButton.setOnClickListener {
            com.example.nyampo.ui.SettingDialog.show(this)
        }

        //만보기 버튼
        walkButton.setOnClickListener {
            com.example.nyampo.ui.WalkDialog.show(this)
        }

        //캘린더 버튼
        calendarButton.setOnClickListener {
            AttendanceDialog.show(this, userId, checkIcon) {
                FeedDialog.showGetFeedPopup(this) { gained ->
                    leafCount += gained
                    leafTextView.text = leafCount.toString()
                    prefs.edit().putInt(KEY_LEAF, leafCount).apply()
                }
            }
        }

        fun showClosetDialog() {
            val userRef =
                FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users").child(userId)

            userRef.get().addOnSuccessListener { snapshot ->
                val characters =
                    snapshot.child("characters").children.mapNotNull { it.getValue(String::class.java) }
                val backgrounds =
                    snapshot.child("backgrounds").children.mapNotNull { it.getValue(String::class.java) }

                val mascotKeys = listOf("hero", "toro", "tino")
                val bgKeys = listOf(
                    "background_base",
                    "background_oido",
                    "background_park",
                    "background_wavepark",
                    "background_tuk"
                )

                val unlockedMascots = mascotKeys.map { characters.contains(it) }
                val unlockedBackgrounds = bgKeys.map { backgrounds.contains(it) }

                ClosetDialog.show(
                    this,
                    ::changeMascot,
                    { idx -> changeBackground(idx, this.backgrounds) },
                    this.backgrounds,
                    unlockedMascots,
                    unlockedBackgrounds
                )
            }.addOnFailureListener {
                Toast.makeText(this, "옷장 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        closetButton.setOnClickListener {
            showClosetDialog()
        }

        feedButton.setOnClickListener {
            FeedDialog.showFeedPopup(this, leafCount) { newLeaf, rewardMoney, _ ->
                showFloatingHearts()
                // 1) 로컬 업데이트
                leafCount = newLeaf
                moneyCount += rewardMoney
                leafTextView.text = leafCount.toString()
                moneyTextView.text = moneyCount.toString()
                prefs.edit()
                    .putInt(KEY_LEAF, leafCount)
                    .putInt(KEY_MONEY, moneyCount)
                    .apply()

                // 2) Firebase에 즉시 동기화
                syncStatsToFirebase(leafCount, moneyCount, levelCount)

                // 3) 레벨업 처리
                if (progressBar.progress < progressBar.max) progressBar.progress += 1
                checkLevelUp(progressBar)
            }
        }

        moneyButton.setOnClickListener {
            MoneyDialog.show(this, moneyCount)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun changeMascot(index: Int) {
        mascotViews.forEachIndexed { i, view ->
            view.visibility = if (i == index) View.VISIBLE else View.GONE
            view.alpha = 1f
        }
        prefs.edit().putInt(KEY_MASCOT, index).apply()
    }

    private fun changeBackground(index: Int, allBackgrounds: List<ImageView>) {
        allBackgrounds.forEachIndexed { i, bg ->
            if (i == index) {
                bg.alpha = 0f
                bg.visibility = View.VISIBLE
                bg.animate().alpha(0.6f).setDuration(300).start()
            } else {
                bg.visibility = View.GONE
            }
        }
        prefs.edit().putInt(KEY_BACKGROUND, index).apply()
    }

    //캐릭터 하트 띄우는 함수
    private fun showFloatingHearts() {
        val rootLayout = findViewById<ConstraintLayout>(R.id.main)
        val currentMascot = mascotViews.find { it.visibility == View.VISIBLE } ?: return

        repeat(10) {
            val heart = ImageView(this).apply {
                setImageResource(R.drawable.heart_icon)
                layoutParams = ConstraintLayout.LayoutParams(80, 80)
                setColorFilter(android.graphics.Color.RED)
            }

            heart.x = currentMascot.x + currentMascot.width / 2 - 40 + (-150..150).random()
            heart.y = currentMascot.y - 60 + (-80..80).random()

            rootLayout.addView(heart)
            val scale = Random.nextFloat() * 0.6f + 0.7f
            heart.animate()
                .translationYBy(-400f)
                .alpha(0f)
                .rotationBy((-30..30).random().toFloat())
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(1800)
                .withEndAction { rootLayout.removeView(heart) }
                .start()
        }
    }

    //firebase에서 사용자 닉네임 불러오는 함수
    private fun fetchNickname() {
        val userRef =
            FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(userId)

        userRef.child("nickname").get().addOnSuccessListener { snapshot ->
            val nickname = snapshot.getValue(String::class.java)
            userText.text = nickname?.let { "$it" } ?: "user"
        }.addOnFailureListener {
            userText.text = "닉네임 불러오기 실패"
        }
    }

    //firebase에서 사용자의 먹이,머니, 레벨 불러오는 함수
    private fun fetchUserFeedAndMoney() {
        val userRef =
            FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val feed = snapshot.child("feed").getValue(Int::class.java) ?: 0
            val money = snapshot.child("money").getValue(Int::class.java) ?: 0
            val level = snapshot.child("level").getValue(Int::class.java) ?: 0

            leafCount = feed
            moneyCount = money
            levelCount = level

            leafTextView.text = leafCount.toString()
            moneyTextView.text = moneyCount.toString()
            levelTextView.text = levelCount.toString()

            prefs.edit()
                .putInt(KEY_LEAF, leafCount)
                .putInt(KEY_MONEY, moneyCount)
                .putInt(KEY_LEVEL, levelCount)
                .apply()
        }.addOnFailureListener {
            Log.e("Firebase", "먹이/머니 불러오기 실패: ${it.message}")
        }
    }

    //firebase에 실시간 연동을 위한 함수
    private fun observeUserData() {
        val userRef =
            FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(userId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 실시간으로 먹이, 머니, 레벨 동기화
                val feed = snapshot.child("feed").getValue(Int::class.java) ?: 0
                val money = snapshot.child("money").getValue(Int::class.java) ?: 0
                val level = snapshot.child("level").getValue(Int::class.java) ?: 1

                leafCount = feed
                moneyCount = money
                levelCount = level

                leafTextView.text = leafCount.toString()
                moneyTextView.text = moneyCount.toString()
                levelTextView.text = levelCount.toString()

                Log.d("Firebase", "실시간 업데이트 - feed: $feed, money: $money, level: $level")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "데이터 수신 취소됨: ${error.message}")
            }
        })
    }

    //레벨 10되면 캐릭터 언락해주는 함수(해로,토로만 적용)
    private fun checkAndUnlockCharacter() {
        val userRef =
            FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val level = snapshot.child("level").getValue(Int::class.java) ?: 1
            val selected = snapshot.child("selectedCharacter").getValue(String::class.java)
                ?: return@addOnSuccessListener
            val current =
                snapshot.child("characters").children.mapNotNull { it.getValue(String::class.java) }
                    .toMutableSet()

            if (level >= 10 && current.size < 2) {
                val newChar = if (selected == "hero") "toro" else "hero"
                val updated = current.toMutableSet().apply { add(newChar) }
                userRef.child("characters").setValue(updated.toList())
            }


        }
    }

    //사용자의 첫 로그인 시 먹이선물 환영 팝업창
    private fun showWelcomePopupIfNeeded() {
        val userRef =
            FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val isNewUser =
                snapshot.child("firstLoginPopupShown").getValue(Boolean::class.java) != true


            if (isNewUser) {
                showCustomPopup(R.layout.dialog_welcome_popup) {
                    // 먹이 5개 추가
                    leafCount += 5
                    leafTextView.text = leafCount.toString()
                    prefs.edit().putInt(KEY_LEAF, leafCount).apply()
                    userRef.child("feed").setValue(leafCount)

                    val referral = snapshot.child("referral").getValue(String::class.java)
                    if (!referral.isNullOrBlank()) {
                        showCustomPopup(R.layout.dialog_referral_popup) {
                            // 추천인 입력 시 추가 5개
                            leafCount += 5
                            leafTextView.text = leafCount.toString()
                            prefs.edit().putInt(KEY_LEAF, leafCount).apply()
                            userRef.child("feed").setValue(leafCount)

                            userRef.child("firstLoginPopupShown").setValue(true)
                        }
                    } else {
                        userRef.child("firstLoginPopupShown").setValue(true)
                    }
                }
            }
        }
    }

    private fun showCustomPopup(layoutRes: Int, onClose: () -> Unit) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(layoutRes, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val closeBtn = view.findViewById<Button>(R.id.popup_close_btn)
        closeBtn?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setOnDismissListener {
            onClose()
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    fun unlockCharacterAtLevel10() {
        userRef.get().addOnSuccessListener { snap ->
            val selected = snap.child("selectedCharacter").getValue(String::class.java)
                ?: return@addOnSuccessListener
            val owned =
                snap.child("characters").children.mapNotNull { it.getValue(String::class.java) }
                    .toMutableSet()

            if (owned.size < 2) {
                val newCharKey = if (selected == "hero") "toro" else "hero"
                owned.add(newCharKey)
                userRef.child("characters").setValue(owned.toList())

                val iconRes = if (newCharKey == "hero") R.drawable.haero else R.drawable.toro
                FeedDialog.showGetFeedPopup(this, message = "캐릭터 획득!", iconResId = iconRes) {}
            }
        }
    }

    fun unlockCharacterAtLevel15() {
        userRef.get().addOnSuccessListener { snap ->
            val characters =
                snap.child("characters").children.mapNotNull { it.getValue(String::class.java) }
                    .toMutableSet()

            if (!characters.contains("tino")) {
                characters.add("tino")
                userRef.child("characters").setValue(characters.toList())

                FeedDialog.showGetFeedPopup(
                    this,
                    message = "캐릭터 획득!",
                    iconResId = R.drawable.tino
                ) {}
            }
        }
    }

    //레벨 관련 함수(캐릭터 언락, 팝업, 레벨 증가 시 먹이 증정)
    fun checkLevelUp(progressBar: ProgressBar) {
        if (progressBar.progress >= progressBar.max) {
            showCustomPopup(R.layout.dialog_level_up) {
                // 레벨 증가
                levelCount += 1
                levelTextView.text = levelCount.toString()
                prefs.edit().putInt(KEY_LEVEL, levelCount).apply()

                // 먹이 +3
                val heartText = findViewById<TextView>(R.id.heartNumberText)
                val newHeartCount = heartText.text.toString().toIntOrNull()?.plus(1) ?: 1
                heartText.text = newHeartCount.toString()

                leafCount += 3
                leafTextView.text = leafCount.toString()
                prefs.edit().putInt(KEY_LEAF, leafCount).apply()

                // Firebase 동기화
                syncStatsToFirebase(leafCount, moneyCount, levelCount)

                // 3) 캐릭터 언락 & 팝업 (레벨 10일 때만)
                checkAndUnlockCharacter()
                // 캐릭터 해금 로직
                if (levelCount == 10) unlockCharacterAtLevel10()
                if (levelCount == 15) unlockCharacterAtLevel15()

                // 5. 프로그레스 초기화
                progressBar.progress = 0
            }
        }
    }
}