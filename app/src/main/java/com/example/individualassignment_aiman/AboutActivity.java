package com.example.individualassignment_aiman;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tvUrl = findViewById(R.id.tvGithubUrl);
        tvUrl.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/pepnocturnal/ICT602IdividualAssignment-ElectricityBillEstimatorAndroidApp"));
            startActivity(intent);
        });
    }
}