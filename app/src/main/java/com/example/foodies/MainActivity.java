package com.example.foodies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.Image;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ImageView postbtn;
    private ImageView logoutbtn;
    private FirebaseAuth mAuth;

//are madarchod github



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        ArrayList<Stations> stations = new ArrayList<>();
        stations.add(new Stations("Churchgate", R.drawable.churchgate));
        stations.add(new Stations("Mumbai Central", R.drawable.mumbaicentral));
        stations.add(new Stations("Dadar", R.drawable.dadar));
        stations.add(new Stations("Bandra", R.drawable.bandra));
        stations.add(new Stations("Andheri", R.drawable.andheri));
        stations.add(new Stations("Goregoan", R.drawable.goregoan));
        stations.add(new Stations("Kandiwali", R.drawable.kandivali));
        stations.add(new Stations("Borivali", R.drawable.borivali));
        stations.add(new Stations("Bhaynder", R.drawable.bhaynder));
        stations.add(new Stations("Vasai Road", R.drawable.vasairoad));
        stations.add(new Stations("Virar", R.drawable.virar));

        recyclerView = (RecyclerView) findViewById(R.id.stations_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this,2);
        adapter = new StationsAdaptor(this, stations);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        postbtn = (ImageView) findViewById(R.id.add_post_btn);
        logoutbtn = (ImageView) findViewById(R.id.logout_btn);
        mAuth = FirebaseAuth.getInstance();


        postbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PostActivity.class);
                startActivity(intent);
            }
        });


        mAuth = FirebaseAuth.getInstance();

        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

                final TextView sname = (TextView) view.findViewById(R.id.station_name);

                String text1;

                text1 = sname.getText().toString();


                Intent details = new Intent(MainActivity.this, AllpostsActivity.class);


                details.putExtra("StationName", text1);
                startActivity(details);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }


    public interface ClickListener
    {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }


    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private ClickListener clickListener;
        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final MainActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }
        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }
        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }








    }
