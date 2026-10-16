package dev.xox.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
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
import java.util.List;
import java.util.Locale;

public class AssistantActivity extends Activity {
    private static final int BG = 0xFF0B0D12;
    private static final int PANEL = 0xFF171B24;
    private static final int TEXT = 0xFFF4F6FC;
    private static final int MUTED = 0xFFA3ADBF;
    private static final int ACCENT = 0xFFB9C7FF;
    private static final int LINE = 0xFF303746;
    private static final int LOCATION_PERMISSION = 42;

    private SharedPreferences prefs;
    private EditText notesInput;
    private EditText cityInput;
    private TextView weatherResult;
    private TextView locationStatus;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ScrollView dashboardScroll;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("xox_dashboard", MODE_PRIVATE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
        buildDashboard();
        requestLocationAccess();
    }

    private TextView text(String value, int size, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        else view.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
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
        d.setCornerRadius(dp(18));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }

    private LinearLayout card() {
        LinearLayout layout = column();
        layout.setBackground(shape(PANEL, LINE));
        layout.setPadding(dp(17), dp(17), dp(17), dp(17));
        return layout;
    }

    private LinearLayout.LayoutParams gapParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(14);
        return p;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(0xFF111522);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackground(shape(ACCENT, 0));
        if (Build.VERSION.SDK_INT >= 21) b.setElevation(dp(0));
        return b;
    }

    private EditText edit(String hint, boolean multiline) {
        EditText e = new EditText(this);
        e.setTextColor(TEXT);
        e.setHintTextColor(MUTED);
        e.setHint(hint);
        e.setTextSize(15);
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        e.setBackground(shape(0xFF10141C, LINE));
        if (multiline) {
            e.setGravity(Gravity.TOP | Gravity.START);
            e.setMinLines(4);
        } else {
            e.setSingleLine(true);
        }
        return e;
    }

    private void buildDashboard() {
        dashboardScroll = new ScrollView(this);
        dashboardScroll.setFillViewport(true);
        dashboardScroll.setClipToPadding(false);
        LinearLayout root = column();
        root.setPadding(dp(20), dp(24), dp(20), dp(30));
        root.setBackgroundColor(BG);
        dashboardScroll.addView(root);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);
        TextView brand = text("XOX / DAILY", 12, true, ACCENT);
        top.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        TextView live = text("PERSONAL SPACE", 10, true, MUTED);
        top.addView(live);
        root.addView(top);

        TextView headline = text("Make today\ncount.", 34, true, TEXT);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.topMargin = dp(18);
        root.addView(headline, hp);
        String date = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(new Date());
        TextView dateView = text(date, 14, false, MUTED);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(-1, -2);
        dp.topMargin = dp(8);
        dp.bottomMargin = dp(22);
        root.addView(dateView, dp);

        LinearLayout weather = card();
        LinearLayout weatherTop = new LinearLayout(this);
        weatherTop.setGravity(Gravity.CENTER_VERTICAL);
        TextView weatherLabel = text("LOCAL WEATHER", 12, true, ACCENT);
        weatherTop.addView(weatherLabel, new LinearLayout.LayoutParams(0, -2, 1));
        weatherTop.addView(text("LIVE DATA", 10, true, MUTED));
        weather.addView(weatherTop);
        weatherResult = text("Finding your location…", 19, true, TEXT);
        LinearLayout.LayoutParams wrp = new LinearLayout.LayoutParams(-1, -2);
        wrp.topMargin = dp(15);
        weather.addView(weatherResult, wrp);
        locationStatus = text("Location permission may be needed.", 12, false, MUTED);
        LinearLayout.LayoutParams lsp = new LinearLayout.LayoutParams(-1, -2);
        lsp.topMargin = dp(9);
        weather.addView(locationStatus, lsp);
        Button locate = button("Detect my location");
        LinearLayout.LayoutParams locatep = new LinearLayout.LayoutParams(-1, dp(48));
        locatep.topMargin = dp(15);
        weather.addView(locate, locatep);
        locate.setOnClickListener(v -> requestLocationAccess());
        root.addView(weather);

        LinearLayout manual = card();
        manual.addView(text("SEARCH A PLACE", 12, true, ACCENT));
        TextView manualHelp = text("Or enter a city if you prefer.", 13, false, MUTED);
        LinearLayout.LayoutParams mhp = new LinearLayout.LayoutParams(-1, -2);
        mhp.topMargin = dp(7);
        manual.addView(manualHelp, mhp);
        cityInput = edit("City, e.g. Istanbul", false);
        cityInput.setText(prefs.getString("city", ""));
        LinearLayout.LayoutParams cip = new LinearLayout.LayoutParams(-1, dp(50));
        cip.topMargin = dp(12);
        manual.addView(cityInput, cip);
        Button search = button("Show city weather");
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, dp(48));
        sp.topMargin = dp(10);
        manual.addView(search, sp);
        search.setOnClickListener(v -> {
            String city = cityInput.getText().toString().trim();
            if (city.isEmpty()) {
                weatherResult.setText("Enter a city name first.");
                return;
            }
            prefs.edit().putString("city", city).apply();
            loadWeatherForCity(city);
        });
        root.addView(manual, gapParams());

        LinearLayout notes = card();
        notes.addView(text("NOTES & REMINDERS", 12, true, ACCENT));
        TextView notesHelp = text("A small place to keep what matters.", 13, false, MUTED);
        LinearLayout.LayoutParams nhp = new LinearLayout.LayoutParams(-1, -2);
        nhp.topMargin = dp(7);
        notes.addView(notesHelp, nhp);
        notesInput = edit("Write a reminder, idea, or to-do…", true);
        notesInput.setText(prefs.getString("notes", ""));
        LinearLayout.LayoutParams nip = new LinearLayout.LayoutParams(-1, dp(132));
        nip.topMargin = dp(13);
        notes.addView(notesInput, nip);
        Button save = button("Save notes");
        LinearLayout.LayoutParams savep = new LinearLayout.LayoutParams(-1, dp(48));
        savep.topMargin = dp(10);
        notes.addView(save, savep);
        save.setOnClickListener(v -> {
            prefs.edit().putString("notes", notesInput.getText().toString()).apply();
            Toast.makeText(this, "Notes saved on this phone.", Toast.LENGTH_SHORT).show();
        });
        root.addView(notes, gapParams());

        TextView footer = text("Weather by Open-Meteo  ·  Location lookup by Android  ·  Notes stay on this device", 11, false, MUTED);
        footer.setGravity(Gravity.CENTER);
        footer.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2);
        fp.topMargin = dp(20);
        root.addView(footer, fp);
        setContentView(dashboardScroll);

        String savedCity = prefs.getString("city", "");
        if (!savedCity.isEmpty()) {
            weatherResult.setText("Ready to check " + savedCity);
        }
    }

    private boolean hasLocationPermission() {
        return Build.VERSION.SDK_INT < 23 ||
                (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                 checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestLocationAccess() {
        if (!hasLocationPermission()) {
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                }, LOCATION_PERMISSION);
            }
            return;
        }
        detectLocation();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION) {
            boolean granted = false;
            for (int result : grantResults) if (result == PackageManager.PERMISSION_GRANTED) granted = true;
            if (granted) detectLocation();
            else locationStatus.setText("Location access is off. You can still search by city.");
        }
    }

    private void detectLocation() {
        if (!hasLocationPermission()) return;
        locationStatus.setText("Checking GPS and network location…");
        try {
            Location best = null;
            for (String provider : locationManager.getProviders(true)) {
                Location candidate = locationManager.getLastKnownLocation(provider);
                if (candidate != null && (best == null || candidate.getTime() > best.getTime())) best = candidate;
            }
            if (best != null && System.currentTimeMillis() - best.getTime() < 30 * 60 * 1000L) {
                showLocalWeather(best);
                return;
            }
            locationListener = new LocationListener() {
                @Override public void onLocationChanged(Location location) {
                    if (locationListener != null) {
                        try { locationManager.removeUpdates(locationListener); } catch (Exception ignored) {}
                        locationListener = null;
                    }
                    showLocalWeather(location);
                }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override public void onProviderEnabled(String provider) {}
                @Override public void onProviderDisabled(String provider) {}
            };
            boolean requested = false;
            for (String provider : locationManager.getProviders(true)) {
                try {
                    locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
                    requested = true;
                } catch (SecurityException ignored) {}
            }
            if (!requested) locationStatus.setText("Turn on Location in Android settings, then try again.");
            else locationStatus.setText("Waiting for a GPS fix. This can take a little while outdoors.");
        } catch (SecurityException e) {
            locationStatus.setText("Allow location access for Xox in Settings.");
        } catch (Exception e) {
            locationStatus.setText("Couldn't detect location. Try searching for a city.");
        }
    }

    private void showLocalWeather(Location location) {
        prefs.edit().putFloat("last_lat", (float) location.getLatitude())
                .putFloat("last_lon", (float) location.getLongitude()).apply();
        runOnUiThread(() -> {
            locationStatus.setText("Location detected · " + String.format(Locale.getDefault(), "%.3f, %.3f",
                    location.getLatitude(), location.getLongitude()));
            weatherResult.setText("Getting your local weather…");
        });
        new Thread(() -> {
            String placeName = "Your current location";
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address a = addresses.get(0);
                    if (a.getLocality() != null) placeName = a.getLocality();
                    else if (a.getSubAdminArea() != null) placeName = a.getSubAdminArea();
                    if (a.getCountryName() != null) placeName += ", " + a.getCountryName();
                }
            } catch (Exception ignored) {}
            loadWeatherAt(location.getLatitude(), location.getLongitude(), placeName, true);
        }).start();
    }

    private void loadWeatherForCity(String city) {
        weatherResult.setText("Loading weather for " + city + "…");
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String encoded = URLEncoder.encode(city, "UTF-8");
                URL url = new URL("https://geocoding-api.open-meteo.com/v1/search?name=" + encoded + "&count=1&language=en&format=json");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "XoxDashboard/1.0");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                JSONObject geo = new JSONObject(readAll(connection.getInputStream()));
                JSONArray results = geo.optJSONArray("results");
                if (results == null || results.length() == 0) throw new Exception("City not found. Check the spelling.");
                JSONObject place = results.getJSONObject(0);
                String label = place.optString("name", city);
                if (!place.optString("country", "").isEmpty()) label += ", " + place.optString("country");
                loadWeatherAt(place.getDouble("latitude"), place.getDouble("longitude"), label, false);
            } catch (Exception e) {
                String msg = e.getMessage() == null ? "Weather unavailable. Check your internet connection." : e.getMessage();
                runOnUiThread(() -> weatherResult.setText(msg));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void loadWeatherAt(double lat, double lon, String placeName, boolean fromGps) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon
                    + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m&timezone=auto");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "XoxDashboard/1.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            JSONObject data = new JSONObject(readAll(connection.getInputStream()));
            JSONObject current = data.getJSONObject("current");
            String summary = placeName + "\n" + Math.round(current.getDouble("temperature_2m")) + "°C · "
                    + weatherDescription(current.getInt("weather_code"))
                    + "\nFeels like " + Math.round(current.getDouble("apparent_temperature")) + "°C"
                    + " · Humidity " + current.getInt("relative_humidity_2m") + "%"
                    + "\nWind " + Math.round(current.getDouble("wind_speed_10m")) + " km/h";
            final String status = fromGps ? "Using your device location." : "Showing weather for searched city.";
            runOnUiThread(() -> {
                weatherResult.setText(summary);
                locationStatus.setText(status);
            });
        } catch (Exception e) {
            String msg = e.getMessage() == null ? "Weather unavailable. Check your internet connection." : e.getMessage();
            runOnUiThread(() -> weatherResult.setText(msg));
        } finally {
            if (connection != null) connection.disconnect();
        }
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

    @Override protected void onDestroy() {
        if (locationManager != null && locationListener != null) {
            try { locationManager.removeUpdates(locationListener); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }
}