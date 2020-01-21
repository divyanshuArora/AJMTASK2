package com.divyanshu.ajmtask2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity()
{

    var userType = "0"
    var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtn.setOnClickListener {

            login()
        }

    }

    private fun login()
    {
        userType = type.text.toString().trim()
        password = pass.text.toString().trim()


        if (userType.isEmpty())
        {
            type.setError("Enter User Type")
        }
        else if (password.isEmpty())
        {
            pass.setError("Enter Password")
        }
//        else if (userType!="1"||userType!="2")
//        {
//            toast("Login With User Type 1 or 2")
//        }
        else
        {
            startActivity<MainActivity>("USER_TYPE" to userType)
        }




    }
}
