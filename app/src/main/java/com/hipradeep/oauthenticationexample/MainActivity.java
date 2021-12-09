package com.hipradeep.oauthenticationexample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hipradeep.oauthenticationexample.current_location.CurrentLocationActivity;
import com.hipradeep.oauthenticationexample.firebase.FirebaseActivity;
import com.hipradeep.oauthenticationexample.google_logins.GoogleLoginsActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void googleLogin(View view) {
        startActivity(new Intent(this, GoogleLoginsActivity.class));
    }
    public void facebookLogin(View view) {


    }

    public void open_firebase_activity(View view) {
        startActivity(new Intent(this, FirebaseActivity.class));
    }

    public void open_current_location_activity(View view) {
        startActivity(new Intent(this, CurrentLocationActivity.class));
    }
}