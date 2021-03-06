package com.tangzy.pdfrenderer;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tangzy.pdfrenderer.adapter.PDFRecycleAdapter1;

public class RecycleViewActivity1 extends AppCompatActivity {
    private PDFRecycleAdapter1 adapter;
    private RecyclerView recycleView;
    private TextView tvNum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recycleview1);
        String url = this.getIntent().getStringExtra("url");
        recycleView = findViewById(R.id.recycleView);
        tvNum = findViewById(R.id.tvNum);
        adapter = new PDFRecycleAdapter1(this, url, tvNum);
        //1线性
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//

        recycleView.setLayoutManager(layoutManager);

        recycleView.setAdapter(adapter);
        recycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d("tagnzy", "onScrollStateChanged newState = "+newState);
                if (RecyclerView.SCROLL_STATE_IDLE == newState){
                    int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    Log.d("tagnzy", "onScrollStateChanged lastVisibleItemPosition = "+lastVisibleItemPosition);
                    recycleView.smoothScrollToPosition(lastVisibleItemPosition);
//                    ((LinearLayoutManager) recyclerView.getLayoutManager()).
                }

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d("tagnzy", "onScrolled dy = "+dy);
            }
        });
    }

}
