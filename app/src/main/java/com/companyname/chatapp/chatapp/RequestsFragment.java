package com.companyname.chatapp.chatapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {


    private RecyclerView mRequestsList;
    private DatabaseReference mRequestsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;
    private View mMainView;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);
        mRequestsList =  (RecyclerView) mMainView.findViewById(R.id.requests_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrentUserID = mAuth.getCurrentUser().getUid();
        mRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("friend_request").child(mCurrentUserID);
        mRequestsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mRequestsList.setHasFixedSize(true);
        mRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Requests,RequestsFragment.FriendsViewHolder> friendsRecyclerAdapter =  new FirebaseRecyclerAdapter<Requests, RequestsFragment.FriendsViewHolder>(
                Requests.class,
                R.layout.users_single_layout,
                RequestsFragment.FriendsViewHolder.class,
                mRequestsDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestsFragment.FriendsViewHolder viewHolder, final Requests model, int position) {
                viewHolder.setStatus("Request type : "+model.getRequest_type());
                final String list_user_id = getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final User user_list = dataSnapshot.getValue(User.class);
                        viewHolder.setName(user_list.getName());
//                        viewHolder.setStatus(user_list.getStatus());
                        viewHolder.setImage(user_list.getImage(), getContext());
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                profileIntent.putExtra("user_id",list_user_id);
                                startActivity(profileIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mRequestsList.setAdapter(friendsRecyclerAdapter);

    }
    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setName (String date)
        {
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(date);
        }
        public void setStatus(String status)
        {
            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }
        public void setImage(String url, Context context) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            if(context!=null)
                Glide.with(context)
                        .load(url)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_person_black_24dp))
                        .into(userImageView);
        }

    }
}
