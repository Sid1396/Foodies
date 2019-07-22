package com.example.foodies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class AllpostsActivity extends AppCompatActivity {

    private String stName;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private RecyclerView SearchResultList;
    private DatabaseReference UsersRef,PostsRef;
    private TextView stationname;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allposts);

        mAuth = FirebaseAuth.getInstance();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        stationname=(TextView)findViewById(R.id.allpoststation) ;
        stName = getIntent().getExtras().getString("StationName");
        stationname.setText(stName);


        SearchResultList = (RecyclerView) findViewById(R.id.staionsPostsList);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        StaionPosts(stName);


    }

    private void StaionPosts(String stationname) {

        Query posts = PostsRef.orderByChild("station")
                .startAt(stationname).endAt(stationname + "\uf8ff");

        FirebaseRecyclerAdapter<SortPosts, SearchByCategoryViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<SortPosts, SearchByCategoryViewHolder>
                (
                        SortPosts.class,
                        R.layout.statons_layout,
                        SearchByCategoryViewHolder.class,
                        posts
                ){
            @Override
            protected void populateViewHolder(SearchByCategoryViewHolder viewHolder, SortPosts model, int position) {


                viewHolder.setPostimage(model.getPostimage());
                viewHolder.setDescription(model.getDescription());



            }
        };
        SearchResultList.setAdapter(firebaseRecyclerAdapter);


    }

    public static class SearchByCategoryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView postimages;
        TextView post_description;
        String currentUserID;
        DatabaseReference PostsRef;




        public SearchByCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            postimages = (ImageView)mView.findViewById(R.id.station_picture);
            post_description = (TextView)mView.findViewById(R.id.station_name);
            PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }




        public void setPostimage(String postimage) {
            ImageView image = (ImageView) mView.findViewById(R.id.station_picture);
            Picasso.get().load(postimage).into(image);

        }


        public void setDescription(String description) {

            TextView PostDiscription = (TextView) mView.findViewById(R.id.station_name);
            PostDiscription.setText(description);

        }



    }

}
