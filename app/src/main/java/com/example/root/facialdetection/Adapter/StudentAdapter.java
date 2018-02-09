package com.example.root.facialdetection.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.example.root.facialdetection.Model.StudentsModel;
import com.example.root.facialdetection.R;
import com.example.root.facialdetection.StudentDetailsActivity;
import com.example.root.facialdetection.Util.IntentExtra;
import com.example.root.facialdetection.VerificationActivity;
import com.example.root.facialdetection.helpers.DataHelper;

import java.util.ArrayList;

/** ex
 * Created by Richard on 8/10/17.
 */

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.MyViewHolder> {

    //custom adapter used to display the list of students
    private Context context;
    private Database studentDatabase;
    ArrayList<StudentsModel>studentsModels;


    public StudentAdapter(Context context, ArrayList<StudentsModel> studentsModels) {
        this.context = context;
        this.studentsModels = studentsModels;
        studentDatabase = DataHelper.getDatabase(context, DataHelper.STUDENT_DATA);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_student_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
    // binding the view to the ui
        StudentsModel model = studentsModels.get(position);
        holder.tvMainText.setText(model.getStudentName());
        holder.tvSubText.setText(model.getSex());
        holder.featuredImage.setImageBitmap(BitmapFactory.decodeFile(model.getImageUrl()));

    }

    @Override
    public int getItemCount() {
        return studentsModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView featuredImage;
        TextView tvMainText, tvSubText;
        ImageButton delete;
        public MyViewHolder(final View itemView) {
            super(itemView);
            delete = (ImageButton) itemView.findViewById(R.id.deleteStudent);
            featuredImage = (ImageView) itemView.findViewById(R.id.studentProfileImage);
            tvMainText = (TextView)itemView.findViewById(R.id.tv_singleCustomersName);
            tvSubText = (TextView)itemView.findViewById(R.id.tv_singleCustomersNamesubtitle);
            final String key = studentsModels.get(getAdapterPosition()+1).getId();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent send = new Intent(context, StudentDetailsActivity.class);
                    send.putExtra(IntentExtra.STUDENT_ID,studentsModels.get(getAdapterPosition()));
                    context.startActivity(send);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    final CharSequence[] items = {"Verify Student "};

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {

                            Intent send = new Intent(context, VerificationActivity.class);
                            send.putExtra(IntentExtra.STUDENT_ID,studentsModels.get(getAdapterPosition()));
                            context.startActivity(send);

                            }


                    });
                    builder.show();
                    return true;
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View view) {

                    if (delete.getTag() == null){
                        //confirm imgActionDelete before deleting
                        final Toast noticeToast = Toast.makeText(context, "Tap again to permanently Delete", Toast.LENGTH_SHORT);
                        noticeToast.show();
                        delete.setImageTintList(ColorStateList.valueOf(Color.RED));
                        delete.setTag("imgActionDelete");

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                delete.setImageTintList(ColorStateList.valueOf(Color.BLACK));
                                noticeToast.cancel();
                                delete.setTag(null);
                            }
                        }, 2000);
                    }
                    else{
                        Database database = DataHelper.getDatabase(context, DataHelper.STUDENT_DATA);
                        if (database != null) {
                            Document document = database.getDocument(key);
                            if (document != null)
                            {
                                try {
                                    document.delete();
                                    StudentAdapter.this.studentsModels.remove(getAdapterPosition());
                                    StudentAdapter.this.notifyDataSetChanged();
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                } catch (CouchbaseLiteException e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            });
        }
    }
    public void setFilter(ArrayList<StudentsModel> student) {
        studentsModels = new ArrayList<>();
        studentsModels.addAll(student);
        notifyDataSetChanged();
    }

}
