package com.example.root.facialdetection;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.example.root.facialdetection.Model.StudentsModel;
import com.example.root.facialdetection.helpers.DataHelper;
import com.nguyenhoanglam.imagepicker.activity.ImagePicker;
import com.nguyenhoanglam.imagepicker.activity.ImagePickerActivity;
import com.nguyenhoanglam.imagepicker.model.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.root.facialdetection.R.id.inputCustomerName;

public class AddStudentActivity extends AppCompatActivity {

    // add students to the database

    ImageButton imgButtonAddCustomerImage;
    EditText inputStudentName;
    EditText inputStudentMatNumber;
    EditText inputStudentPhone;
    Spinner inputStudentDepartment;
    Button saveStudent;
    RadioGroup rdgSex;
    ArrayList<Image> images;
    private static final int REQUEST_CODE_PICKER = 0x8;
    StudentsModel studentsModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        if (getSupportActionBar()!= null){
            getSupportActionBar().setTitle("Register Students");
        }
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        imgButtonAddCustomerImage = (ImageButton)findViewById(R.id.imgAddCustomerImage);
        inputStudentName = (EditText)findViewById(inputCustomerName);
        inputStudentPhone = (EditText)findViewById(R.id.txtCustomerPhoneNumber);
        inputStudentDepartment = (Spinner) findViewById(R.id.spinnerDepartments);
        inputStudentMatNumber = (EditText)findViewById(R.id.txtMat);
        rdgSex = (RadioGroup) findViewById(R.id.rdgSex);
        saveStudent = (Button)findViewById(R.id.btnSave);

        if (studentsModel != null){
            inputStudentName.setText(studentsModel.getStudentName());
            inputStudentPhone.setText(studentsModel.getPhoneNumber());
            inputStudentMatNumber.setText(studentsModel.getStudentMatNumber());
            try{
                imgButtonAddCustomerImage.setImageBitmap(BitmapFactory.decodeFile(studentsModel.getImageUrl()));
            }
            catch (Exception ignored){}
        }
        else
            studentsModel = new StudentsModel();
// save student
        saveStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!saveStudentInfo())
                return;

                finish();
                Intent viewCustomers = new Intent(AddStudentActivity.this, StudentListActivity.class);
                viewCustomers.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(viewCustomers);
            }
        });

        imgButtonAddCustomerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStudentImage();
            }
        });
    }


    // set the student image
    private void setStudentImage() {
        ImagePicker.create(this)
                .single()
                .origin(images)
                .imageTitle("Tap to select student profile") // image selection title
                .start(REQUEST_CODE_PICKER); // start image picker activity with request code
    }

    private File getImagesFolder() {
        File file = new File(getFilesDir(), "students-images");
        if (!file.exists() || !file.isDirectory())
        {
            boolean createDir = file.mkdir();
            if (!createDir)
                Toast.makeText(AddStudentActivity.this, "Error", Toast.LENGTH_SHORT).show();
        }
        return file;
    }

    // do something when the image picker is done
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            images = data.getParcelableArrayListExtra(ImagePickerActivity.INTENT_EXTRA_SELECTED_IMAGES);
            if (images.size() > 0){
                imgButtonAddCustomerImage.setImageBitmap(BitmapFactory.decodeFile(images.get(0).getPath()));
            }
        }
    }

    private boolean saveStudentInfo() {


        String name = inputStudentName.getText().toString().trim();
        String department = inputStudentDepartment.getSelectedItem().toString();
        String phone = inputStudentPhone.getText().toString().trim();
        String matNumber = inputStudentMatNumber.getText().toString().trim();
        String sex = ((RadioButton)findViewById(rdgSex.getCheckedRadioButtonId())).getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(department) || TextUtils.isEmpty(phone)||TextUtils.isEmpty(matNumber)){
            Toast.makeText(AddStudentActivity.this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // saving to student database
        Database database = DataHelper.getDatabase(getApplicationContext(), DataHelper.STUDENT_DATA);
        //if new student, save to db first
        if (TextUtils.isEmpty(studentsModel.getId()))
            studentsModel.saveToDatabase(AddStudentActivity.this, database);

        studentsModel.setStudentName(name);
        studentsModel.setDepartment(department);
        studentsModel.setPhoneNumber(phone);
        studentsModel.setStudentMatNumber(matNumber);
        studentsModel.setSex(sex);

        if (images != null && images.size() > 0){
            String sourceImagePath = images.get(0).getPath();
            studentsModel.setImageUrl(processImage(sourceImagePath));
        }


        studentsModel.saveToDatabase(AddStudentActivity.this, database);

        return true;
    }

    private String processImage(String path) {

        String filenameArray[] = path.split("\\.");
        String extension = filenameArray[filenameArray.length-1];
        String newFileName = "student" + String.valueOf(Calendar.getInstance().getTimeInMillis())
                + extension;
        //save image to app internal storage folder
        File destFile = new File(getImagesFolder(), newFileName);
        String destImagePath = destFile.getPath();

        if (com.example.root.facialdetection.Util.ImageUtils.resizeUploadImage(path, destImagePath))
            return destImagePath;
        else
            return null;
    }

}
