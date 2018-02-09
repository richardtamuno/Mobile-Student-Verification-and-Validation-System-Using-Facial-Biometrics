package com.example.root.facialdetection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.example.root.facialdetection.Model.StudentsModel;
import com.example.root.facialdetection.Util.IntentExtra;
import com.example.root.facialdetection.helpers.DataHelper;

import java.util.ArrayList;

public class StudentDetailsActivity extends AppCompatActivity {

    // displays the student details from the database
    Database database;
    String studentId;
    private SharedPreferences mSharedPreferences;


    ArrayList<StudentsModel> studentsModelArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(StudentDetailsActivity.this);
        database = DataHelper.getDatabase(getApplicationContext(), DataHelper.STUDENT_DATA);

        final StudentsModel studentsModel = getIntent().getParcelableExtra(IntentExtra.STUDENT_ID);
        studentId = studentsModel.getId();




        if (getSupportActionBar()!= null){
            getSupportActionBar().setTitle(studentsModel.getStudentName());
        }



        ImageView profile = (ImageView)findViewById(R.id.img_customersProfilePic);
        TextView profileName = (TextView)findViewById(R.id.tv_customersName);
        TextView tvSex = (TextView)findViewById(R.id.tv_customerSex);
        TextView tvMatNum = (TextView)findViewById(R.id.matnumber);
       // Button VerificationActivity = (Button)findViewById(R.id.btnVerify);
        TextView tvDepartment = (TextView)findViewById(R.id.tv_department);
        TextView tvPhone = (TextView)findViewById(R.id.tv_phone);



        profile.setImageBitmap(BitmapFactory.decodeFile(studentsModel.getImageUrl()));
        profileName.setText(studentsModel.getStudentName());
        tvSex.setText(studentsModel.getSex());
        tvDepartment.setText(studentsModel.getDepartment());
        tvMatNum.setText(studentsModel.getStudentMatNumber());
        tvPhone.setText(studentsModel.getPhoneNumber());

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent back = new Intent(StudentDetailsActivity.this,StudentListActivity.class);
        back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        back.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(back);
    }
}
