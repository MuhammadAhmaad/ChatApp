package com.companyname.chatapp.chatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.companyname.chatapp.chatapp.R;
import com.companyname.chatapp.chatapp.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileDisplayName;
    private TextView mProfileStatus;
    private TextView mProfileFriendsCount;
    private Button mProfileSendReqBtn;
    private Button mProfileDeclineRequest;
    private DatabaseReference mUserDatabase;
    private ProgressDialog mProgressDialog;
    private String current_state;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mNotificationsDatabase;
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Intent intent = getIntent();
        final String user_id = intent.getStringExtra("user_id");
        mUserDatabase = FirebaseDatabase.getInstance().getReference("Users").child(user_id);
        mProfileImage = (ImageView) findViewById(R.id.profile_image_view);
        mProfileDisplayName = (TextView) findViewById(R.id.profile_display_name);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_total_friends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_request_button);
        mProfileDeclineRequest = (Button) findViewById(R.id.profile_decline);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("friend_request");
        mNotificationsDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        current_state = "not_friends";


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.please_wait));
        mProgressDialog.show();
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProgressDialog.dismiss();
                User user = dataSnapshot.getValue(User.class);
                mProfileDisplayName.setText(user.getName());
                mProfileStatus.setText(user.getStatus());
                Glide.with(getApplicationContext())
                        .load(user.getImage())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_person_black_24dp))
                        .into(mProfileImage);


                mFriendRequestDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            current_state = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (current_state.equals("sent"))
                                mProfileSendReqBtn.setText(getResources().getString(R.string.cancel_friend_request));
                            else {
                                mProfileSendReqBtn.setText(getResources().getString(R.string.accept_friend_request));
                                mProfileDeclineRequest.setVisibility(View.VISIBLE);
                                mProfileDeclineRequest.setEnabled(true);
                            }
                        } else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        current_state = "friends";
                                        mProfileSendReqBtn.setText(getResources().getString(R.string.unfriend_person));
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (mCurrentUser.getUid().equals(user_id))
            mProfileSendReqBtn.setVisibility(View.INVISIBLE);
        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (current_state.equals("not_friends")) {

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child("user_id").push();
                    String newNotificationId = newNotificationRef.getKey();
                    HashMap<String, String> notifiacionData = new HashMap<>();
                    notifiacionData.put("from", mCurrentUser.getUid());
                    notifiacionData.put("type", "request");
                    Map requestMap = new HashMap();
                    requestMap.put("friend_request/" + mCurrentUser.getUid() + "/" + user_id + "/" + "request_type", "sent");
                    requestMap.put("friend_request/" + user_id + "/" + mCurrentUser.getUid() + "/" + "request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notifiacionData);
                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                current_state = "sent";
                                mProfileSendReqBtn.setText(getResources().getString(R.string.cancel_friend_request));

                            }
                        }
                    });
                } else if (current_state.equals("sent")) {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        current_state = "not_friends";
                                        mProfileSendReqBtn.setText(getResources().getString(R.string.send_friend_request));
                                    }
                                });
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to cancel friend request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else if (current_state.equals("received")) {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    current_state = "friends";
                                                    mProfileSendReqBtn.setText(getResources().getString(R.string.unfriend_person));
                                                    mProfileDeclineRequest.setVisibility(View.INVISIBLE);
                                                    mProfileDeclineRequest.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                } else if (current_state.equals("friends")) {
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    current_state = "not_friends";
                                    mProfileSendReqBtn.setText(getResources().getString(R.string.send_friend_request));
                                }
                            });
                        }
                    });
                }
            }
        });
        mProfileDeclineRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                current_state = "not_friends";
                                mProfileSendReqBtn.setText(getResources().getString(R.string.send_friend_request));
                                mProfileDeclineRequest.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequest.setEnabled(false);
                            }
                        });
                    }
                });
            }
        });
    }
}
