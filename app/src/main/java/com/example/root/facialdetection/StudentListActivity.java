package com.example.root.facialdetection;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.example.root.facialdetection.Adapter.StudentAdapter;
import com.example.root.facialdetection.Model.StudentsModel;
import com.example.root.facialdetection.helpers.DataHelper;

import java.util.ArrayList;

public class StudentListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    // displays the list of student in the database
    StudentAdapter adapter;
    EditText search;
    Database database;
    RecyclerView mRecyclerView;
    ArrayList<StudentsModel> studentsModelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!= null){
            getSupportActionBar().setTitle("Students");
        }


        database = DataHelper.getDatabase(getApplicationContext(), DataHelper.STUDENT_DATA);
        getAllCustomers();



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(StudentListActivity.this,AddStudentActivity.class));
            }
        });
    }


    private void getAllCustomers() {

        mRecyclerView = (RecyclerView) findViewById(R.id.rvStudentList);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(StudentListActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        if (database == null)
            return;

        Query query = database.createAllDocumentsQuery();
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS); //ALL_DOCS by id, BY_SEQUENCE by last modified

        try {
            QueryEnumerator result = query.run();
            studentsModelArrayList = new ArrayList<>();

            for (; result.hasNext(); ) {
                QueryRow row = result.next();
                StudentsModel customer = StudentsModel.fromDictionary(row.getDocument().getProperties());
                studentsModelArrayList.add(customer);
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            Toast.makeText(StudentListActivity.this, "Get customers info failed", Toast.LENGTH_SHORT).show();
        }
        adapter = new StudentAdapter(StudentListActivity.this,studentsModelArrayList);
      mRecyclerView.setAdapter(adapter);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.home, menu);
        SearchManager manager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
         MenuItem searchViewItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        return true;
    }





    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    private ArrayList<StudentsModel> filter(ArrayList<StudentsModel> students, String query) {
        query = query.toLowerCase();
        final ArrayList<StudentsModel>filteredList = new ArrayList<>();
        for (StudentsModel customer1 : students){
            final String text = customer1.getStudentName().toLowerCase();
            if (text.contains(query)) {
                filteredList.add(customer1);
            }
        }
        return filteredList;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        final ArrayList<StudentsModel> customersList = filter(studentsModelArrayList,newText);
        adapter.setFilter(customersList);
        return true;
    }
}

