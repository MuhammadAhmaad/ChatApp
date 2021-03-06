package com.companyname.chatapp.chatapp.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.companyname.chatapp.chatapp.Model.Message;
import com.companyname.chatapp.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Mohamed Ahmed on 6/30/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> mMessageList;

    public void setmMessageList(List<Message> mMessageList) {
        this.mMessageList = mMessageList;
    }

    private FirebaseAuth mAuth;
    private Context mContext;
    private DatabaseReference mUserDatabase;

    public MessageAdapter(List<Message> mMessageList, Context mContext) {
        this.mMessageList = mMessageList;
        mAuth = FirebaseAuth.getInstance();
        this.mContext = mContext;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String current_user_id = mAuth.getCurrentUser().getUid();
        Message c = mMessageList.get(position);
        String from_user = c.getFrom();
        String message_type = c.getType();


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                holder.nameText.setText(name);

                Glide.with(mContext).load(image).apply(new RequestOptions().placeholder(R.drawable.ic_person_black_24dp))
                        .into(holder.profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (message_type.equals("text")) {

            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);


        } else {

            holder.setIsRecyclable(false);
            holder.messageText.setVisibility(View.INVISIBLE);
            Glide.with(mContext).load(c.getMessage()).into(holder.messageImage);

        }


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView nameText;
        public TextView timeText;
        public CircleImageView profileImage;
        public ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_layout);
            nameText = (TextView) itemView.findViewById(R.id.message_name_layout);
            timeText = (TextView) itemView.findViewById(R.id.message_time_layout);
            messageImage = (ImageView)itemView.findViewById(R.id.message_image_layout);

        }
    }
}
