package dev.xox.assistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.animation.ValueAnimator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AssistantActivity extends Activity {
    private static final int TEXT = 0xFFF4F6FC;
    private static final int MUTED = 0xFF9AA8BE;
    private static final int ACCENT = 0xFFB9C7FF;
    private static final int LINE = 0x443B4561;
    private final ArrayList<ApplicationInfo> allApps = new ArrayList<>();
    private LinearLayout appsContainer;
    private TextView countLabel;
    private EditText appSearch;
    private FlowingGradientView flowingBackground;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF101426);
        getWindow().setNavigationBarColor(0xFF101426);
        if (Build.VERSION.SDK_INT >= 21) getWindow().getDecorView().setSystemUiVisibility(0);
        buildLauncher();
    }

    private TextView text(String value, int size, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        view.setIncludeFontPadding(false);
        return view;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private GradientDrawable shape(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(15));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }

    private void buildLauncher() {
        FrameLayout frame = new FrameLayout(this);
        flowingBackground = new FlowingGradientView(this);
        frame.addView(flowingBackground, new FrameLayout.LayoutParams(-1, -1));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(0x00000000);
        frame.addView(scroll, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout root = column();
        root.setPadding(dp(18), dp(25), dp(18), dp(24));
        root.setBackgroundColor(0x00000000);
        scroll.addView(root);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("XOX", 14, true, ACCENT);
        top.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(text("APP LAUNCHER", 10, true, MUTED));
        root.addView(top);

        TextView title = text("Applications", 30, true, TEXT);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
        titleParams.topMargin = dp(18);
        root.addView(title, titleParams);

        TextView subtitle = text("Tap an app to open it. Package identifiers are shown underneath.", 13, false, MUTED);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = dp(7);
        root.addView(subtitle, subtitleParams);

        appSearch = new EditText(this);
        appSearch.setSingleLine(true);
        appSearch.setTextSize(15);
        appSearch.setTextColor(TEXT);
        appSearch.setHintTextColor(MUTED);
        appSearch.setHint("Search name or package identifier");
        appSearch.setPadding(dp(14), dp(12), dp(14), dp(12));
        appSearch.setBackground(shape(0xCC141A2A, LINE));
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(-1, dp(50));
        searchParams.topMargin = dp(20);
        root.addView(appSearch, searchParams);

        countLabel = text("Loading applications…", 11, false, MUTED);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(-1, -2);
        countParams.topMargin = dp(12);
        countParams.bottomMargin = dp(8);
        root.addView(countLabel, countParams);

        appsContainer = column();
        root.addView(appsContainer, new LinearLayout.LayoutParams(-1, -2));
        setContentView(frame);

        loadApps();
        renderApps("");
        appSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderApps(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable e) {}
        });
    }

    private void loadApps() {
        allApps.clear();
        try {
            int flags = 0;
            if (Build.VERSION.SDK_INT >= 24) flags |= PackageManager.MATCH_DISABLED_COMPONENTS;
            List<ApplicationInfo> installed = getPackageManager().getInstalledApplications(flags);
            allApps.addAll(installed);
            Collections.sort(allApps, (a, b) -> {
                String an = String.valueOf(getPackageManager().getApplicationLabel(a));
                String bn = String.valueOf(getPackageManager().getApplicationLabel(b));
                return an.compareToIgnoreCase(bn);
            });
        } catch (Exception ignored) {}
    }

    private void renderApps(String query) {
        if (appsContainer == null) return;
        appsContainer.removeAllViews();
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        int shown = 0;
        PackageManager pm = getPackageManager();

        for (ApplicationInfo info : allApps) {
            String label;
            try { label = String.valueOf(pm.getApplicationLabel(info)); }
            catch (Exception e) { label = info.packageName; }
            String pkg = info.packageName == null ? "" : info.packageName;
            if (!needle.isEmpty() && !label.toLowerCase(Locale.ROOT).contains(needle)
                    && !pkg.toLowerCase(Locale.ROOT).contains(needle)) continue;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(11), dp(12), dp(11));
            row.setBackground(shape(0xC9161C2D, LINE));

            ImageView icon = new ImageView(this);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            try { icon.setImageDrawable(pm.getApplicationIcon(info)); }
            catch (Exception ignored) {}
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(44), dp(44));
            iconParams.rightMargin = dp(12);
            row.addView(icon, iconParams);

            LinearLayout labels = column();
            labels.setGravity(Gravity.CENTER_VERTICAL);
            TextView name = text(label, 14, true, TEXT);
            TextView identifier = text(pkg, 10, false, MUTED);
            identifier.setSingleLine(true);
            identifier.setEllipsize(android.text.TextUtils.TruncateAt.END);
            labels.addView(name);
            LinearLayout.LayoutParams idParams = new LinearLayout.LayoutParams(-1, -2);
            idParams.topMargin = dp(5);
            labels.addView(identifier, idParams);

            boolean system = (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            TextView kind = text(system ? "SYSTEM" : "APP", 9, true, ACCENT);
            kind.setGravity(Gravity.CENTER);
            kind.setPadding(dp(7), dp(5), dp(7), dp(5));
            kind.setBackground(shape(0x332D3B66, 0));
            row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
            LinearLayout.LayoutParams kindParams = new LinearLayout.LayoutParams(-2, -2);
            kindParams.leftMargin = dp(8);
            row.addView(kind, kindParams);

            row.setOnClickListener(v -> openAppOrInfo(info));
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
            rowParams.bottomMargin = dp(7);
            appsContainer.addView(row, rowParams);
            shown++;
        }

        if (shown == 0) {
            appsContainer.addView(text("No matching applications.", 13, false, MUTED));
        }
        countLabel.setText(shown + " applications shown  ·  " + allApps.size() + " installed packages");
    }

    private void openAppOrInfo(ApplicationInfo info) {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(info.packageName);
            if (launch != null && info.enabled) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launch);
            } else {
                Intent details = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + info.packageName));
                startActivity(details);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Android cannot open this component directly.", Toast.LENGTH_SHORT).show();
        }
    }

    private final class FlowingGradientView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private ValueAnimator animator;
        private float phase = 0f;

        FlowingGradientView(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        @Override protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(20000L);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.addUpdateListener(a -> {
                phase = (Float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        @Override protected void onDetachedFromWindow() {
            if (animator != null) {
                animator.cancel();
                animator = null;
            }
            super.onDetachedFromWindow();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            if (w <= 0 || h <= 0) return;
            float dx = (float) Math.sin(phase * Math.PI * 2) * w * 0.42f;
            float dy = (float) Math.cos(phase * Math.PI * 2) * h * 0.25f;
            LinearGradient gradient = new LinearGradient(
                    dx, dy, w - dx * 0.35f, h - dy,
                    new int[] { 0xFF111B35, 0xFF33245B, 0xFF174E59, 0xFF10131E, 0xFF111B35 },
                    null, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);
        }
    }
}
