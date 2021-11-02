package com.example.coolweather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coolweather.util.OnItemClickListener;
import com.example.coolweather.util.Saved;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<Saved> list=new ArrayList<>();
    private OnItemClickListener Listener=null;

    public MyAdapter(List<Saved> list, OnItemClickListener Listener){
        this.Listener=Listener;
        this.list=list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        TextView countryText;
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
            countryText=itemView.findViewById(R.id.country_name);
            imageView=itemView.findViewById(R.id.country_image);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item,parent,false);
        final ViewHolder viewHolder=new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Listener.onItemClick(viewHolder.getAdapterPosition());
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Saved saved=list.get(position);
        holder.countryText.setText(saved.getCountryName());
        int temp=new Random().nextInt(10);
        switch (temp){
            case 0:
                holder.imageView.setImageResource(R.drawable.city1);
                break;
            case 1:
                holder.imageView.setImageResource(R.drawable.city2);
                break;
            case 2:
                holder.imageView.setImageResource(R.drawable.city3);
                break;
            case 3:
                holder.imageView.setImageResource(R.drawable.city4);
                break;
            case 4:
                holder.imageView.setImageResource(R.drawable.city5);
                break;
            case 5:
                holder.imageView.setImageResource(R.drawable.city6);
                break;
            case 6:
                holder.imageView.setImageResource(R.drawable.city7);
                break;
            case 7:
                holder.imageView.setImageResource(R.drawable.city8);
                break;
            case 8:
                holder.imageView.setImageResource(R.drawable.city9);
                break;
            case 9:
                holder.imageView.setImageResource(R.drawable.city10);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
