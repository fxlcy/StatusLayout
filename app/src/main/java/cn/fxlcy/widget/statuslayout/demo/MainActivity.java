package cn.fxlcy.widget.statuslayout.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import cn.fxlcy.widget.StatusLayout;

public class MainActivity extends AppCompatActivity {

    private StatusLayout mSl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSl = findViewById(R.id.sl);
    }

    private int status = -2;

    public void onClick(View v) {
        status--;
        if (status < -7) {
            status = -2;
        }

        mSl.switchStatusLayout(status);
    }
}