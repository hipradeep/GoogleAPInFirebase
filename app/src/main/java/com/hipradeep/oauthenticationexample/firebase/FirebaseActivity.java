package com.hipradeep.oauthenticationexample.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hipradeep.oauthenticationexample.R;
import com.hipradeep.oauthenticationexample.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FirebaseActivity extends AppCompatActivity {

    String lastImageFileName=" ";
    FirebaseStorage storage;
    Uri imageUri;
    StorageReference storageReference;
    ProgressDialog progressDialog;
    ProgressBar pb_loader;
    TextView tv_selected_image_path, tv_user_data, tv_uploded_image_url, tv_uploded_image_uri;
    EditText et_name,et_mobile;
    ImageView iv_selected_image, iv_uploded_image;
    Button btn_send_to_firebase, btn_check, btn_select_image,btn_upload_image, btn_get_image_from_firebase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);
        tv_selected_image_path=findViewById(R.id.tv_selected_image_path);
        tv_user_data=findViewById(R.id.tv_user_data);
        tv_uploded_image_url=findViewById(R.id.tv_uploded_image_url);
        tv_uploded_image_uri=findViewById(R.id.tv_uploded_image_uri);
        et_name=findViewById(R.id.et_name);
        et_mobile=findViewById(R.id.et_mobile);
        btn_send_to_firebase=findViewById(R.id.btn_send_to_firebase);
        btn_select_image=findViewById(R.id.btn_select_image);
        btn_check=findViewById(R.id.btn_check);
        btn_upload_image=findViewById(R.id.btn_upload_image);
        btn_get_image_from_firebase=findViewById(R.id.btn_get_image_from_firebase);
        iv_selected_image=findViewById(R.id.iv_selected_image);
        iv_uploded_image=findViewById(R.id.iv_uploded_image);
        pb_loader=findViewById(R.id.pb_loader);

        storage = FirebaseStorage.getInstance();

        //progress bar
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading File....");


        //send text to firebase
        btn_send_to_firebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validation()){
                    sendTextToFirebase();
                }

            }
        });

        //check existing text on firebase
        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validation()){
                    checkTextOnFirebase();
                }

            }
        });
        btn_select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFile();
            }
        });
        iv_selected_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFile();
            }
        });
        btn_upload_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri !=null){
                    sendImageToFirebase();
                }else {
                    Toast.makeText(FirebaseActivity.this, "Select Image", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btn_get_image_from_firebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageFromFirebase();
            }
        });



    }

    private void getImageFromFirebase() {

        pb_loader.setVisibility(View.VISIBLE);



        storageReference = FirebaseStorage.getInstance().getReference("images/"+lastImageFileName);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                tv_uploded_image_url.setVisibility(View.VISIBLE);
                iv_uploded_image.setVisibility(View.VISIBLE);
                pb_loader.setVisibility(View.GONE);

                iv_uploded_image.setImageURI(uri);
                tv_uploded_image_url.setText("URI/URL : "+uri.toString());
                Glide.with(FirebaseActivity.this)
                        .load(uri)
                        .thumbnail(0.1f)
                        .into(iv_uploded_image);
                Log.e("TAG", "URI  : "+uri.toString());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG","Error : "+ e.toString());

                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });


    }

    private void checkTextOnFirebase() {
        progressDialog.show();
        FirebaseDatabase.getInstance().getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                Log.e("TAG", user.toString());
                Log.e("TAG", "User data is changed!" + user.name + ", " + user.mobile);
                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                if(dataSnapshot.child("name").exists() && dataSnapshot.child("mobile").exists() )
                {
                   if (user.name.equals(et_name.getText().toString().trim()) && user.mobile.equals(et_mobile.getText().toString().trim())){
                       Toast.makeText(FirebaseActivity.this, "User Exists", Toast.LENGTH_SHORT).show();
                       tv_user_data.setText("name : "+ user.name + "\n"+ "mobile : "+ user.mobile);

                   }else {
                       Toast.makeText(FirebaseActivity.this, "User NOT Exists", Toast.LENGTH_SHORT).show();
                   }
                }


                // Check for null
                if (user == null) {
                    Toast.makeText(FirebaseActivity.this, "User Null", Toast.LENGTH_SHORT).show();
                    return;
                }

            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                Log.e("TAG", "Failed to read user", error.toException());
            }

        });
    }


    public void sendTextToFirebase() {

                User user=new User(et_name.getText().toString(), et_mobile.getText().toString());
                DatabaseReference mDatabase;
                mDatabase = FirebaseDatabase.getInstance().getReference();
                progressDialog.show();
                mDatabase.child("users").setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(FirebaseActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(FirebaseActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }

                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                });




    }


    /// send image to firestore
    public void sendImageToFirebase() {
        progressDialog.show();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
         lastImageFileName = formatter.format(now);

        storageReference = FirebaseStorage.getInstance().getReference("images/"+lastImageFileName);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        btn_get_image_from_firebase.setVisibility(View.VISIBLE);
                        Toast.makeText(FirebaseActivity.this,"Successfully Uploaded",Toast.LENGTH_SHORT).show();
                        Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();
                        String image = taskSnapshot.getUploadSessionUri().toString();
                        Log.e("TAG", "Uploaded image path1 : "+image);
                        tv_uploded_image_uri.setVisibility(View.VISIBLE);
                        tv_uploded_image_uri.setText("URI : "+ image);
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(FirebaseActivity.this,"Failed to Upload",Toast.LENGTH_SHORT).show();


            }
        });

    }


    public void selectImageFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && data != null && data.getData() != null){
            imageUri = data.getData();
            iv_selected_image.setImageURI(data.getData());
            tv_selected_image_path.setText("URI : "+imageUri.toString());
            btn_select_image.setText("Image Selected");
        }
    }

    private  boolean validation(){
        if (et_name.getText().toString().isEmpty()) {
             Toast.makeText(FirebaseActivity.this, "Enter Username", Toast.LENGTH_SHORT).show();
            return  false;
        }
        if (et_mobile.getText().toString().isEmpty()) {
            Toast.makeText(FirebaseActivity.this, "Enter Mobile", Toast.LENGTH_SHORT).show();
            return  false;
        }

        return true;
    }


}