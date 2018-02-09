package com.example.root.facialdetection.Model;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;

import java.util.HashMap;
import java.util.Map;

public class StudentsModel extends BaseModel implements Parcelable{
    private String studentName;
    private String studentMatNumber;
    private int sitNumber;
    private String id;
    private String imageUrl;
    private String sex;
    private String phoneNumber;
    private String department;

    public StudentsModel() {
    }

    public static StudentsModel fromDictionary(Object dictionary){
        return fromDictionary(dictionary,StudentsModel.class);
    }

    private StudentsModel(Parcel in) {
        id = in.readString();
        studentName = in.readString();
        sex = in.readString();
        studentMatNumber = in.readString();
        phoneNumber = in.readString();
        department = in.readString();
        imageUrl = in.readString();
    }

    public static final Parcelable.Creator<StudentsModel> CREATOR = new Parcelable.Creator<StudentsModel>() {
        @Override
        public StudentsModel createFromParcel(Parcel in) {
            return new StudentsModel(in);
        }

        @Override
        public StudentsModel[] newArray(int size) {
            return new StudentsModel[size];
        }
    };

    public String getStudentMatNumber() {
        return studentMatNumber;
    }

    public void setStudentMatNumber(String studentMatNumber) {
        this.studentMatNumber = studentMatNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getSitNumber() {
        return sitNumber;
    }

    public void setSitNumber(int sitNumber) {
        this.sitNumber = sitNumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void saveToDatabase(final AppCompatActivity activity, final Database database){

        if (database == null)
        {
            Toast.makeText(activity, "Cannot to save to store. Database unavailable.", Toast.LENGTH_SHORT).show();
            return;
        }

        Document StudentDocument;
        Map<String, Object> properties;

        if (TextUtils.isEmpty(this.getId())){
            //new style
            StudentDocument  = database.createDocument();
            this.setId(StudentDocument.getId());
            properties = this.toDictionary();
        }
        else{
            StudentDocument = database.getDocument(this.getId());
            properties = new HashMap<>();
            properties.putAll(StudentDocument.getProperties());
            properties.putAll(this.toDictionary());
        }

        try {
            StudentDocument.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            Toast.makeText(activity, "Failed to save to store. Fatal error occurred.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(id);
        dest.writeString(studentName);
        dest.writeString(sex);
        dest.writeString(studentMatNumber);
        dest.writeString(phoneNumber);
        dest.writeString(department);
        dest.writeString(imageUrl);

    }
}
