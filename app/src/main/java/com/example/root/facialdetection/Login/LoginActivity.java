package com.example.root.facialdetection.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.root.facialdetection.Home;
import com.example.root.facialdetection.R;

public class LoginActivity extends AppCompatActivity {
    Button login;
    EditText email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.input_logemail);
        password = (EditText) findViewById(R.id.input_logpassword);
        login = (Button) findViewById(R.id.logregbtn);
        // disable the keyboard on start until the user clicks on it
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logUserin();
            }
        });
    }

    private boolean logUserin() {
        // performs the login
        String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();

        if (TextUtils.isEmpty(Email)||TextUtils.isEmpty(Password)){
            Toast.makeText(this,"Please enter valid text ",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Email.equalsIgnoreCase("ADMIN")&&Password.equalsIgnoreCase("PASSWORD")){
            startActivity(new Intent(this, Home.class));
        }else{
            Toast.makeText(this,"You don't have access to enter",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
