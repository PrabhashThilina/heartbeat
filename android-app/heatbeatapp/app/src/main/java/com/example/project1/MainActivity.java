package com.example.project1;

import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Delay for 2 seconds and then switch to the main content
        new Handler().postDelayed(() -> {
            //Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            //startActivity(intent);

            // Set the main content layout
            setContentView(R.layout.activity_login);
            setupSecondLayout();
        }, 5000); // 2000 milliseconds = 2 seconds

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupSecondLayout() {
        // Connect the button in the second layout to move to the third layout
        findViewById(R.id.nextButton).setOnClickListener(v -> {
            setContentView(R.layout.activity_input);;
        });
    }
}