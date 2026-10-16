package dev.xox.assistant;

import android.content.Context;
import android.service.voice.VoiceInteractionSession;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class XoxVoiceInteractionSession extends VoiceInteractionSession {
    public XoxVoiceInteractionSession(Context context) {
        super(context);
    }

    @Override
    public View onCreateContentView() {
        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(32, 32, 32, 32);
        root.setBackgroundColor(0xFF101014);

        TextView title = new TextView(getContext());
        title.setText("Xox");
        title.setTextSize(28);
        title.setTextColor(0xFFF5F3FF);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView message = new TextView(getContext());
        message.setText("Your assistant is ready.");
        message.setTextSize(16);
        message.setTextColor(0xFFB8B5C5);
        message.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 16;
        root.addView(message, params);
        return root;
    }
}