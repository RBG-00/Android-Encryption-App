package com.example.myproject;


import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    public static ArrayList<String> historyList = new ArrayList<>();
    Button btnBack;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ListView historyListView = findViewById(R.id.historyListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                historyList

        );
        historyListView.setAdapter(adapter);


        btnBack = findViewById(R.id.btnBackToMain);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, MainActivity.class);

            startActivity(intent);
            finish();
        });






    }
}
