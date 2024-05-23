package com.example.womensafe;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.GravityInt;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.ViewHolder;
import com.squareup.picasso.Picasso;

import java.lang.ref.Reference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ImageAdpter extends RecyclerView.Adapter<ImageAdpter.ImageViewHolder> {
    private final Context mContext;
    private final List<people> mUploads;
    private static OnItemClickListener mlistner;

    public ImageAdpter(Context context, List<people> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.singleperson, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        people uploadCurrent = mUploads.get(position);



        holder.textViewName.setText(("Name: "+uploadCurrent.getPerson_name()));
        holder.textView1.setText(("Mob: "+uploadCurrent.getPerson_mobile()));
        holder.textView2.setText((uploadCurrent.getMember_type()));



        String name = uploadCurrent.getPerson_name().toString();
        String mobile =uploadCurrent.getPerson_mobile().toString();
        String uri1 = uploadCurrent.getUri().toString();
        String type = uploadCurrent.getMember_type().toString();


        Picasso.get()
                .load(uploadCurrent.getUri())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,trustedpeopledetailed.class);

                intent.putExtra("m1",name);
                intent.putExtra("m2",mobile);
                intent.putExtra("m3",uri1);
                intent.putExtra("m4",type);
                mContext.startActivity(intent);

            }
        });


    }




    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textViewName,textView1,textView2,textView3,textViewbtn;
        public ImageView imageView,img1,img2;

        CardView cardView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textView24);
            textView1 = itemView.findViewById(R.id.textView23);
            textView2 = itemView.findViewById(R.id.textView25);
            cardView = itemView.findViewById(R.id.card);

            imageView = itemView.findViewById(R.id.frindprofileimg);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mlistner !=null){
                int position = getAdapterPosition();
                if (position!=RecyclerView.NO_POSITION){
                    mlistner.onItemClick(position);
                }
            }
        }
    }
    public interface OnItemClickListener{
        void onItemClick(int position);
        void buyClick(int position);
        void cartClick(int position);

    }
    public void setOnItemClickListener(OnItemClickListener listener){
        mlistner = listener;
    }
}
