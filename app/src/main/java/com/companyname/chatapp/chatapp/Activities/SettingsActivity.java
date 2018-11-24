package com.companyname.chatapp.chatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.companyname.chatapp.chatapp.R;
import com.companyname.chatapp.chatapp.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private CircleImageView profilePicture;
    private TextView mDisplayName;
    private TextView mStatus;
    private Button changeImageBtn;
    private Button changeStatusBtn;
    private final int GALLERY_PICK = 1;
    private StorageReference mStorageRef;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        profilePicture = (CircleImageView) findViewById(R.id.profile_picture);
        mDisplayName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);
        changeImageBtn = (Button) findViewById(R.id.settings_change_image_btn);
        changeStatusBtn = (Button) findViewById(R.id.settings_change_status);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
        databaseReference.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.please_wait));
        mProgressDialog.show();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProgressDialog.dismiss();
                User user = dataSnapshot.getValue(User.class);
                mDisplayName.setText(user.getName());
                mStatus.setText(user.getStatus());

                if (!user.getImage().equals("default"))
                    Glide.with(getApplicationContext())
                            .load(user.getImage())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_person_black_24dp))
                            .into(profilePicture);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        changeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, StatusActivity.class).putExtra("status", mStatus.getText().toString()));
            }
        });
        changeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            mProgressDialog.setMessage("please wait while loading..");
            StorageReference filePath = mStorageRef.child("profile_images").child(userID + ".jpg");
            mProgressDialog.show();
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        mProgressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "uploaded", Toast.LENGTH_LONG).show();
                        String downloadUri = task.getResult().getDownloadUrl().toString();
//                        ContentValues values = new ContentValues();
//                        values.put(UserProvider.PHOTO_URL, downloadUri + ".jpg");
//                        getContentResolver().update(UserProvider.CONTENT_URI, values, null, null);
                        databaseReference.child("image").setValue(downloadUri + ".jpg");
                        databaseReference.child("thumbImages").setValue(downloadUri + ".jpg");
                    } else {
                        mProgressDialog.hide();
                        Toast.makeText(SettingsActivity.this, "error", Toast.LENGTH_LONG).show();
                    }
                }
            });

        } else {
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }
}
