package com.example.individualassignment_aiman;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class AboutActivity extends AppCompatActivity {


    private static final String GITHUB_URL = "https://github.com/pepnocturnal/ICT602IdividualAssignment-ElectricityBillEstimatorAndroidApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setupGithubLink();
    }

    private void setupGithubLink() {
        TextView tvUrl = findViewById(R.id.tvGithubUrl);
        tvUrl.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL));
            startActivity(browserIntent);
        });
    }
}