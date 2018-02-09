package com.example.root.facialdetection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.example.root.facialdetection.Model.StudentsModel;
import com.example.root.facialdetection.helpers.DataHelper;

public class Home extends AppCompatActivity {

    Button viewStudents;
    Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
       database = DataHelper.getDatabase(Home.this, DataHelper.STUDENT_DATA);

        viewStudents = (Button)findViewById(R.id.btnViewStudents);
        viewStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this,StudentListActivity.class));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        int pendingTasks = 0;

        if (database != null)
        {
            Query query = database.createAllDocumentsQuery();
            query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS); //ALL_DOCS by id, BY_SEQUENCE by last modified

            try {
                QueryEnumerator result = query.run();


                for (; result.hasNext(); ) {
                    QueryRow row = result.next();
                    StudentsModel student = StudentsModel.fromDictionary(row.getDocument().getProperties());

                        pendingTasks++;
                }
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        TextView count = (TextView) findViewById(R.id.numberOfStudents);
        count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this,StudentListActivity.class));
            }
        });
        count.setText(String.valueOf(pendingTasks));

    }
}
