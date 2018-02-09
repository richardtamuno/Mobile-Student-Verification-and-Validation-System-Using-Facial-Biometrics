package com.example.root.facialdetection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.example.root.facialdetection.Model.StudentsModel;
import com.example.root.facialdetection.Util.IntentExtra;
import com.example.root.facialdetection.helpers.DataHelper;
import com.example.root.facialdetection.helpers.ImageHelper;
import com.example.root.facialdetection.helpers.LogHelper;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.VerifyResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class VerificationActivity extends AppCompatActivity {

    Database database;
    String studentId;
    StudentsModel studentsModel;
    private class VerificationTask extends AsyncTask<Void, String, VerifyResult> {
        // The IDs of two face to VerificationActivity.
        private UUID mFaceId0;
        private UUID mFaceId1;

        VerificationTask (UUID faceId0, UUID faceId1) {
            mFaceId0 = faceId0;
            mFaceId1 = faceId1;
        }

        @Override
        protected VerifyResult doInBackground(Void... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = App.getFaceServiceClient();
            try{
                publishProgress("Verifying...");

                // Start verification.
                return faceServiceClient.verify(
                        mFaceId0,      /* The first face ID to VerificationActivity */
                        mFaceId1);     /* The second face ID to VerificationActivity */
            }  catch (Exception e) {
                publishProgress(e.getMessage());
                addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
            addLog("Request: Verifying face " + mFaceId0 + " and face " + mFaceId1);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            progressDialog.setMessage(progress[0]);
            setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(VerifyResult result) {
            progressDialog.dismiss();
            if (result != null) {
                addLog("Response: Success. Face " + mFaceId0 + " and face "
                        + mFaceId1 + (result.isIdentical ? " " : " don't ")
                        + "belong to the same person");

            }

            // Show the result on screen when verification is done.
            setUiAfterVerification(result);
        }
    }

    private void setInfo(String progres) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(progres);
    }

    private void setUiAfterVerification(VerifyResult result) {
        // Verification is done, hide the progress dialog.

        // Enable all the buttons.
        setAllButtonEnabledStatus(true);

        // Show verification result.
        if (result != null) {
            DecimalFormat formatter = new DecimalFormat("#0.00");
            String verificationResult = (result.isIdentical ? "The same person": "Different persons")
                    + ". The confidence is " + formatter.format(result.confidence);
            setInfo(verificationResult);

            Button button = (Button) findViewById(R.id.continueB);
            button.setVisibility(View.VISIBLE);
            button.setText("Continue");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(VerificationActivity.this,StudentListActivity.class));
                }
            });


        }
    }

    private void setAllButtonEnabledStatus(boolean b) {
        Button selectImage0 = (Button) findViewById(R.id.select_image_0);
        selectImage0.setEnabled(b);

        Button selectImage1 = (Button) findViewById(R.id.select_image_1);
        selectImage1.setEnabled(b);

        Button verify = (Button) findViewById(R.id.verify);
        verify.setEnabled(b);

    }

    private void addLog(String message) {
        LogHelper.addVerificationLog(message);
    }


    // Background task of face detection.
    private class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
        // Index indicates detecting in which of the two images.
        private int mIndex;
        private boolean mSucceed = true;

        DetectionTask(int index) {
            mIndex = index;
        }

        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = App.getFaceServiceClient();
            try{
                publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        false,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        null);
            }  catch (Exception e) {
                mSucceed = false;
                publishProgress(e.getMessage());
                addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
            addLog("Request: Detecting in image" + mIndex);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            progressDialog.setMessage(progress[0]);
            setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(Face[] result) {
            // Show the result on screen when detection is done.
            setUiAfterDetection(result, mIndex, mSucceed);
        }
    }

    // Show the result on screen when detection in image that indicated by index is done.
    private void setUiAfterDetection(Face[] result, int index, boolean succeed) {

        if (succeed) {
            addLog("Response: Success. Detected "
                    + result.length + " face(s) in image" + index);

            setInfo(result.length + " face" + (result.length != 1 ? "s": "")  + " detected");

            // Show the detailed list of detected faces.
            FaceListAdapter faceListAdapter = new FaceListAdapter(result, index);

            // Set the default face ID to the ID of first face, if one or more faces are detected.
            if (faceListAdapter.faces.size() != 0) {
                if (index == 0) {
                    mFaceId0 = faceListAdapter.faces.get(0).faceId;
                }
                else {
                    mFaceId1 = faceListAdapter.faces.get(0).faceId;
                }
                // Show the thumbnail of the default face.
                ImageView imageView = (ImageView) findViewById(index == 0 ? R.id.image_0: R.id.image_1);
                imageView.setImageBitmap(faceListAdapter.faceThumbnails.get(0));
            }

            // Show the list of detected face thumbnails.
            ListView listView = (ListView) findViewById(
                    index == 0 ? R.id.list_faces_0: R.id.list_faces_1);
            listView.setAdapter(faceListAdapter);
            listView.setVisibility(View.VISIBLE);

            // Set the face list adapters and bitmaps.
            if (index == 0) {
                mFaceListAdapter0 = faceListAdapter;
                mBitmap0 = null;
            } else {
                mFaceListAdapter1 = faceListAdapter;
                mBitmap1 = null;
            }
        }

        if (result != null && result.length == 0) {
            setInfo("No face detected!");
        }

        if ((index == 0 && mBitmap1 == null) || (index == 1 && mBitmap0 == null) || index == 2) {
            progressDialog.dismiss();
        }

        if (mFaceId0 != null && mFaceId1 != null) {
            setVerifyButtonEnabledStatus(true);
        }

        
    }

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE_0 = 0;
    private static final int REQUEST_SELECT_IMAGE_1 = 1;


    // The IDs of the two faces to be verified.
    private UUID mFaceId0;
    private UUID mFaceId1;

    // The two images from where we get the two faces to VerificationActivity.
    private Bitmap mBitmap0;
    private Bitmap mBitmap1;

    // The adapter of the ListView which contains the detected faces from the two images.
    protected FaceListAdapter mFaceListAdapter0;
    protected FaceListAdapter mFaceListAdapter1;

    // Progress dialog popped up when communicating with server.
    ProgressDialog progressDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        database = DataHelper.getDatabase(getApplicationContext(), DataHelper.STUDENT_DATA);

         studentsModel = getIntent().getParcelableExtra(IntentExtra.STUDENT_ID);
        studentId = studentsModel.getId();

        if (getSupportActionBar()!= null){
            getSupportActionBar().setTitle(studentsModel.getStudentName());
        }

        // Initialize the two ListViews which contain the thumbnails of the detected faces.
        initializeFaceList(0);
        initializeFaceList(1);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");


        clearDetectedFaces(0);
        clearDetectedFaces(1);

        // Disable button "VerificationActivity" as the two face IDs to VerificationActivity are not ready.
        setVerifyButtonEnabledStatus(false);


    }

    private void setVerifyButtonEnabledStatus(boolean b) {
        Button button = (Button) findViewById(R.id.verify);
        button.setEnabled(b);
    }

    // Called when image selection is done. Begin detecting if the image is selected successfully.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Index indicates which of the two images is selected.
        int index;
        if (requestCode == REQUEST_SELECT_IMAGE_0) {
            index = 0;
        } else if (requestCode == REQUEST_SELECT_IMAGE_1) {

            index = 1;
        } else {
            return;
        }

        if(resultCode == RESULT_OK) {
            // If image is selected successfully, set the image URI and bitmap.
            Bitmap bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                    data.getData(), getContentResolver());
            if (bitmap != null) {
                // Image is select but not detected, disable verification button.
                setVerifyButtonEnabledStatus(false);
                clearDetectedFaces(index);

                // Set the image to detect.
                if (index == 0) {
                    mBitmap0 = bitmap;
                    mFaceId0 = null;
                } else {
                    mBitmap1 = bitmap;
                    mFaceId1 = null;
                }

                // Add verification log.
                addLog("Image" + index + ": " + data.getData() + " resized to " + bitmap.getWidth()
                        + "x" + bitmap.getHeight());

                // Start detecting in image.
                detect(bitmap, index);
            }
        }
    }

    // Start detecting in image specified by index.
    private void detect(Bitmap bitmap, int index) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        // Start a background task to detect faces in the image.
        new DetectionTask(index).execute(inputStream);
        setSelectImageButtonEnabledStatus(false, index);

        // Set the status to show that detection starts.
        setInfo("Detecting...");
    }

    private void setSelectImageButtonEnabledStatus(boolean b, int index) {
        Button button;

        if (index == 0) {
            button = (Button) findViewById(R.id.select_image_0);
        } else{
            button = (Button) findViewById(R.id.select_image_1);
        }

        button.setEnabled(b);
    }

    private void clearDetectedFaces(int index) {
        ListView faceList = (ListView) findViewById(
                index == 0 ? R.id.list_faces_0: R.id.list_faces_1);
        faceList.setVisibility(View.GONE);

        ImageView imageView =
                (ImageView) findViewById(index == 0 ? R.id.image_0: R.id.image_1);
        imageView.setImageResource(android.R.color.transparent);
    }

    // Called when the "Select Image0" button is clicked in face face verification.
    public void selectImage0(View view) {
        selectImage(0);
    }

    private void selectImage(int index) {
        Intent intent = new Intent(this, SelectImageActivity.class);
        startActivityForResult(intent, index == 0 ? REQUEST_SELECT_IMAGE_0: REQUEST_SELECT_IMAGE_1 );
    }

    // Called when the "Select Image1" button is clicked in face face verification.
    public void selectImage1(View view) {
        selectImage(1);
    }

    // Called when the "Verify" button is clicked.
    public void verify(View view) {
        setAllButtonEnabledStatus(false);
        new VerificationTask(mFaceId0, mFaceId1).execute();
    }


    private void initializeFaceList(final int index) {
        ListView listView =
                (ListView) findViewById(index == 0 ? R.id.list_faces_0: R.id.list_faces_1);

        // When a detected face in the GridView is clicked, the face is selected to VerificationActivity.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FaceListAdapter faceListAdapter =
                        index == 0 ? mFaceListAdapter0: mFaceListAdapter1;

                if (!faceListAdapter.faces.get(position).faceId.equals(
                        index == 0 ? mFaceId0: mFaceId1)) {
                    if (index == 0) {
                        mFaceId0 = faceListAdapter.faces.get(position).faceId;
                    } else {
                        mFaceId1 = faceListAdapter.faces.get(position).faceId;
                    }

                    ImageView imageView =
                            (ImageView) findViewById(index == 0 ? R.id.image_0: R.id.image_1);
                    imageView.setImageBitmap(faceListAdapter.faceThumbnails.get(position));

                    setInfo("");
                }

                // Show the list of detected face thumbnails.
                ListView listView = (ListView) findViewById(
                        index == 0 ? R.id.list_faces_0: R.id.list_faces_1);
                listView.setAdapter(faceListAdapter);
            }
        });
    }

    private class FaceListAdapter extends BaseAdapter {

        // The detected faces.
        List<Face> faces;

        int mIndex;

        // The thumbnails of detected faces.
        List<Bitmap> faceThumbnails;

        // Initialize with detection result and index indicating on which image the result is got.
        FaceListAdapter(Face[] detectionResult, int index) {
            faces = new ArrayList<>();
            faceThumbnails = new ArrayList<>();
            mIndex = index;

            if (detectionResult != null) {
                faces = Arrays.asList(detectionResult);
                for (Face face: faces) {
                    try {
                        // Crop face thumbnail without landmarks drawn.
                        faceThumbnails.add(ImageHelper.generateFaceThumbnail(
                                index == 0 ? mBitmap0: mBitmap1, face.faceRectangle));
                    } catch (IOException e) {
                        // Show the exception when generating face thumbnail fails.
                        setInfo(e.getMessage());
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return faces.size();
        }

        @Override
        public Object getItem(int position) {
            return faces.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_face, parent, false);
            }
            convertView.setId(position);

            Bitmap thumbnailToShow = faceThumbnails.get(position);
            if (mIndex == 0 && faces.get(position).faceId.equals(mFaceId0)) {
                thumbnailToShow = ImageHelper.highlightSelectedFaceThumbnail(thumbnailToShow);
            } else if (mIndex == 1 && faces.get(position).faceId.equals(mFaceId1)){
                thumbnailToShow = ImageHelper.highlightSelectedFaceThumbnail(thumbnailToShow);
            }

            // Show the face thumbnail.
            ((ImageView)convertView.findViewById(R.id.image_face)).setImageBitmap(thumbnailToShow);

            return convertView;
        }
    }
    }

