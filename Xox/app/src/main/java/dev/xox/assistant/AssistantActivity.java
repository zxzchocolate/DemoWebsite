package dev.xox.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(0xFF101014);
        getWindow().setNavigationBarColor(0xFF101014);
        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(28), dp(24), dp(24));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(0xFF101014);

        TextView mark = new TextView(this);
        mark.setText("XOX");
        mark.setTextSize(14);
        mark.setLetterSpacing(0.24f);
        mark.setTextColor(0xFFB7A7FF);
        mark.setGravity(Gravity.CENTER);
        root.addView(mark, new LinearLayout.LayoutParams(-1, dp(40)));

        TextView title = new TextView(this);
        title.setText("Hey. I'm here.");
        title.setTextSize(30);
        title.setTextColor(0xFFF5F3FF);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(28), 0, dp(8));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView info = new TextView(this);
        info.setText("Your quick assistant, one shortcut away.\n\nAndroid may still require your PIN to open protected apps.");
        info.setTextSize(15);
        info.setTextColor(0xFFB8B5C5);
        info.setGravity(Gravity.CENTER);
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));

        Button settings = button("Configure shortcut");
        settings.setOnClickListener(v -> showShortcutSettings());
        root.addView(settings, buttonParams());

        Button close = button("Close");
        close.setOnClickListener(v -> finish());
        root.addView(close, buttonParams());
        setContentView(root);
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.topMargin = dp(12);
        return params;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        return button;
    }

    private void showShortcutSettings() {
        String[] options = {
                "Volume Up, then Volume Down",
                "Volume Down, then Volume Up",
                "Disable volume shortcut"
        };
        int savedMode = ShortcutPrefs.getMode(this);
        int checkedMode = savedMode >= 0 && savedMode < options.length ? savedMode : 0;

        new AlertDialog.Builder(this)
                .setTitle("Physical-key shortcut")
                .setSingleChoiceItems(options, checkedMode, (dialog, which) -> {
                    ShortcutPrefs.setMode(this, which);
                    dialog.dismiss();

                    if (which == 2) {
                        return; // Disabling the shortcut should not send users to Accessibility Settings.
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Enable key detection")
                            .setMessage("Android requires you to enable Xox's accessibility service manually. Xox uses it only to detect the chosen volume-key sequence.")
                            .setNegativeButton("Later", null)
                            .setPositiveButton("Open Settings", (d, w) ->
                                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
