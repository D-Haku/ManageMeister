package com.example.managemeister.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.managemeister.R
import kotlinx.android.synthetic.main.activity_intro.*

@Suppress("DEPRECATION")
class IntroActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        btn_sign_in_intro.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignInActivity::class.java))
        }
        btn_sign_up_intro.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignUpActivity::class.java))
        }
    }
}