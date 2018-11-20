package com.companyname.chatapp.chatapp.Fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import com.companyname.chatapp.chatapp.Activities.ChatActivity;
import com.companyname.chatapp.chatapp.Model.Conv;
import com.companyname.chatapp.chatapp.R;
import com.companyname.chatapp.chatapp.Model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String POSITION = "position";
    private int current_position = 0;
    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private View mMainView;
    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView= inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList = (RecyclerView) mMainView.findViewById(R.id.chats_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserId);
        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase=FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserId);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);
        if (savedInstanceState != null)
            current_position = savedInstanceState.getInt(POSITION);
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query conversationQuery = mConvDatabase.orderByChild("timpestamp");
        FirebaseRecyclerAdapter<Conv,ConvViewHolder> firebaseRecyclerAdapter =new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
                Conv.class,
                R.layout.users_single_layout,
                ConvViewHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final ConvViewHolder viewHolder, final Conv model, int position) {
                final String list_user_id = getRef(position).getKey();
                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        viewHolder.setMessage(data,model.isSeen());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final User user_list = dataSnapshot.getValue(User.class);
                        viewHolder.setName(user_list.getName());
                        viewHolder.setImage(user_list.getImage(), getContext());
                        viewHolder.setUserOnline(user_list.getOnline());
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                chatIntent.putExtra("user_id",list_user_id);
                                chatIntent.putExtra("user_name",user_list.getName());
                                startActivity(chatIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        };
        mConvList.setAdapter(firebaseRecyclerAdapter);
        mConvList.scrollToPosition(current_position);
    }
    public static class ConvViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public ConvViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setMessage(String message,boolean isSeen)
        {
            TextView status =(TextView) mView.findViewById(R.id.user_single_status);
            status.setText(message);
            if(!isSeen)
            {
                status.setTypeface(status.getTypeface(), Typeface.BOLD);
            }
            else {
                status.setTypeface(status.getTypeface(), Typeface.NORMAL);
            }
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
        public void setUserOnline(long online_status)
        {
            ImageView icon = (ImageView)mView.findViewById(R.id.user_single_online_icon);
            if(online_status == 0)
                icon.setVisibility(View.VISIBLE);
            else
                icon.setVisibility(View.INVISIBLE);
        }
        public void setName (String date)
        {
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(date);
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION, mConvList.getVerticalScrollbarPosition());
    }
}
