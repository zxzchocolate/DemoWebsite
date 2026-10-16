package dev.xox.assistant;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AssistantActivity extends Activity {
    private static final int BG = 0xFF101014;
    private static final int PANEL = 0xFF1B1B22;
    private static final int TEXT = 0xFFF5F3FF;
    private static final int MUTED = 0xFFB8B5C5;
    private static final int ACCENT = 0xFFB7A7FF;
    private SharedPreferences prefs;
    private EditText notesInput;
    private EditText cityInput;
    private TextView weatherResult;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("xox_dashboard", MODE_PRIVATE);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        buildDashboard();
    }

    private TextView text(String value, int size, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout card() {
        LinearLayout layout = column();
        layout.setBackgroundColor(PANEL);
        layout.setPadding(dp(16), dp(14), dp(16), dp(14));
        return layout;
    }

    private void buildDashboard() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root = column();
        root.setPadding(dp(18), dp(20), dp(18), dp(24));
        root.setBackgroundColor(BG);
        scroll.addView(root);

        root.addView(text("XOX", 13, true, ACCENT));
        root.addView(text("Daily dashboard", 28, true, TEXT));
        String date = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(new Date());
        TextView dateView = text(date, 14, false, MUTED);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(-1, -2);
        dateParams.bottomMargin = dp(18);
        root.addView(dateView, dateParams);

        LinearLayout weather = card();
        weather.addView(text("WEATHER", 12, true, ACCENT));
        weatherResult = text("Enter a city and tap Refresh.", 16, true, TEXT);
        LinearLayout.LayoutParams wrp = new LinearLayout.LayoutParams(-1, -2);
        wrp.topMargin = dp(8);
        weather.addView(weatherResult, wrp);
        cityInput = new EditText(this);
        cityInput.setSingleLine(true);
        cityInput.setText(prefs.getString("city", ""));
        cityInput.setHint("City (for example, Istanbul)");
        cityInput.setTextColor(TEXT);
        cityInput.setHintTextColor(MUTED);
        weather.addView(cityInput, new LinearLayout.LayoutParams(-1, dp(52)));
        Button refresh = new Button(this);
        refresh.setText("Refresh weather");
        refresh.setOnClickListener(v -> {
            String city = cityInput.getText().toString().trim();
            if (city.isEmpty()) {
                weatherResult.setText("Enter a city first.");
                return;
            }
            prefs.edit().putString("city", city).apply();
            loadWeather(city);
        });
        weather.addView(refresh, new LinearLayout.LayoutParams(-1, dp(48)));
        root.addView(weather);

        LinearLayout.LayoutParams cardGap = new LinearLayout.LayoutParams(-1, -2);
        cardGap.topMargin = dp(14);
        LinearLayout notes = card();
        notes.addView(text("NOTES", 12, true, ACCENT));
        notes.addView(text("Your notes stay on this phone.", 13, false, MUTED));
        notesInput = new EditText(this);
        notesInput.setGravity(Gravity.TOP | Gravity.START);
        notesInput.setMinLines(5);
        notesInput.setText(prefs.getString("notes", ""));
        notesInput.setHint("Write a reminder, idea, or to-do...");
        notesInput.setTextColor(TEXT);
        notesInput.setHintTextColor(MUTED);
        notes.addView(notesInput, new LinearLayout.LayoutParams(-1, dp(130)));
        Button save = new Button(this);
        save.setText("Save notes");
        save.setOnClickListener(v -> {
            prefs.edit().putString("notes", notesInput.getText().toString()).apply();
            android.widget.Toast.makeText(this, "Notes saved on this phone.", android.widget.Toast.LENGTH_SHORT).show();
        });
        notes.addView(save, new LinearLayout.LayoutParams(-1, dp(48)));
        root.addView(notes, cardGap);

        LinearLayout quick = card();
        quick.addView(text("QUICK LINKS", 12, true, ACCENT));
        Button openNotes = new Button(this);
        openNotes.setText("Focus notes");
        openNotes.setOnClickListener(v -> {
            notesInput.requestFocus();
            scroll.smoothScrollTo(0, notes.getTop());
        });
        quick.addView(openNotes, new LinearLayout.LayoutParams(-1, dp(48)));
        root.addView(quick, cardGap);

        root.addView(text("Weather data: Open-Meteo. Notes are stored locally.", 12, false, MUTED), cardGap);
        setContentView(scroll);

        String savedCity = prefs.getString("city", "");
        if (!savedCity.isEmpty()) loadWeather(savedCity);
    }

    private void loadWeather(String city) {
        weatherResult.setText("Loading weather...");
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String encoded = URLEncoder.encode(city, "UTF-8");
                URL geocode = new URL("https://geocoding-api.open-meteo.com/v1/search?name="
                        + encoded + "&count=1&language=en&format=json");
                connection = (HttpURLConnection) geocode.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                JSONObject geo = new JSONObject(readAll(connection.getInputStream()));
                JSONArray results = geo.optJSONArray("results");
                if (results == null || results.length() == 0) throw new Exception("City not found. Check the spelling.");
                JSONObject place = results.getJSONObject(0);
                double lat = place.getDouble("latitude");
                double lon = place.getDouble("longitude");
                String placeName = place.optString("name", city);
                String country = place.optString("country", "");
                connection.disconnect();

                URL forecast = new URL("https://api.open-meteo.com/v1/forecast?latitude="
                        + lat + "&longitude=" + lon + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m&timezone=auto");
                connection = (HttpURLConnection) forecast.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                JSONObject data = new JSONObject(readAll(connection.getInputStream()));
                JSONObject current = data.getJSONObject("current");
                double temp = current.getDouble("temperature_2m");
                double feels = current.getDouble("apparent_temperature");
                double wind = current.getDouble("wind_speed_10m");
                int humidity = current.getInt("relative_humidity_2m");
                int code = current.getInt("weather_code");
                String summary = placeName + (country.isEmpty() ? "" : ", " + country)
                        + "\n" + Math.round(temp) + "°C · " + weatherDescription(code)
                        + "\nFeels like " + Math.round(feels) + "°C · Humidity " + humidity + "%"
                        + "\nWind " + Math.round(wind) + " km/h";
                runOnUiThread(() -> weatherResult.setText(summary));
            } catch (Exception e) {
                String message = e.getMessage() == null ? "Weather unavailable. Check your internet connection." : e.getMessage();
                runOnUiThread(() -> weatherResult.setText(message));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private String readAll(InputStream stream) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) out.append(line);
            return out.toString();
        }
    }

    private String weatherDescription(int code) {
        if (code == 0) return "Clear sky";
        if (code <= 3) return "Partly cloudy";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 57) return "Drizzle";
        if (code >= 61 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Rain showers";
        if (code >= 85 && code <= 86) return "Snow showers";
        if (code >= 95) return "Thunderstorm";
        return "Current conditions";
    }
}