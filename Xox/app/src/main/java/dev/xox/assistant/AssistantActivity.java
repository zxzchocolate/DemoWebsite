package dev.xox.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AssistantActivity extends Activity {
    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().setStatusBarColor(0xFF101014);
        getWindow().setNavigationBarColor(0xFF101014);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(28), dp(24), dp(24));
        root.setBackgroundColor(0xFF101014);

        TextView title = new TextView(this);
        title.setText("XOX");
        title.setTextSize(28);
        title.setTextColor(0xFFB7A7FF);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(56)));

        TextView info = new TextView(this);
        info.setText("Choose Xox as your digital assistant in your phone settings.");
        info.setTextSize(16);
        info.setTextColor(0xFFF5F3FF);
        info.setGravity(Gravity.CENTER);
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));

        Button close = new Button(this);
        close.setText("Close");
        close.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.topMargin = dp(24);
        root.addView(close, params);
        setContentView(root);
    }
}