package com.example.foodies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StationsAdaptor extends RecyclerView.Adapter<StationsAdaptor.StationViewholder> {


    Context context;
    List<Stations> stations;

    public StationsAdaptor(Context context, List<Stations> stations) {
        this.context = context;
        this.stations = stations;
    }


    @NonNull
    @Override
    public StationViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.statons_layout,parent,false);

        StationViewholder st =  new StationViewholder(v);
        return st;

    }

    @Override
    public void onBindViewHolder(@NonNull StationViewholder holder, int position) {
         Stations currentitem =  stations.get(position);

         holder.name.setText(currentitem.getName());
         holder.picuture.setImageResource(currentitem.getPicture());


    }

    @Override
    public int getItemCount() {
        return stations.size();
    }


    public static class StationViewholder extends RecyclerView.ViewHolder{

        TextView name;
        ImageView picuture;


        public StationViewholder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.station_name);
            picuture = itemView.findViewById(R.id.station_picture);

        }
    }

}
