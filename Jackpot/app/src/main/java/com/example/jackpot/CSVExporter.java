package com.example.jackpot;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for exporting event data to CSV format.
 */
public class CSVExporter {
    private static final String TAG = "CSVExporter";

    /**
     * Exports the confirmed attendees list to a CSV file.
     *
     * @param context The application context
     * @param event The event whose attendees are being exported
     * @param attendees The list of confirmed attendees
     * @return The URI of the created CSV file, or null if export failed
     */
    public static Uri exportAttendeesList(Context context, Event event, List<User> attendees) {
        if (attendees == null || attendees.isEmpty()) {
            Toast.makeText(context, "No attendees to export", Toast.LENGTH_SHORT).show();
            return null;
        }

        try {
            // Create CSV file
            File csvFile = createCSVFile(context, event);
            if (csvFile == null) {
                return null;
            }

            // Write data to CSV
            writeAttendeesToCSV(csvFile, event, attendees);

            // Get URI for the file
            Uri fileUri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider",
                    csvFile);

            Toast.makeText(context, "CSV exported successfully", Toast.LENGTH_SHORT).show();
            return fileUri;

        } catch (IOException e) {
            Log.e(TAG, "Error exporting CSV", e);
            Toast.makeText(context, "Failed to export CSV: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Creates a CSV file in the app's cache directory.
     *
     * @param context The application context
     * @param event The event for naming the file
     * @return The created File object
     * @throws IOException If file creation fails
     */
    private static File createCSVFile(Context context, Event event) throws IOException {
        // Create a unique filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String eventName = event.getName() != null ?
                event.getName().replaceAll("[^a-zA-Z0-9]", "_") : "event";
        String filename = eventName + "_attendees_" + timestamp + ".csv";

        // Use cache directory for temporary files
        File cacheDir = context.getCacheDir();
        File csvFile = new File(cacheDir, filename);

        if (!csvFile.exists()) {
            csvFile.createNewFile();
        }

        return csvFile;
    }

    /**
     * Writes attendee data to the CSV file.
     *
     * @param csvFile The file to write to
     * @param event The event information
     * @param attendees The list of attendees
     * @throws IOException If writing fails
     */
    private static void writeAttendeesToCSV(File csvFile, Event event, List<User> attendees)
            throws IOException {
        FileWriter writer = new FileWriter(csvFile);

        try {
            // Write event information header
            writer.append("Event Name:,").append(escapeCsvValue(event.getName())).append("\n");

            if (event.getDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
                writer.append("Event Date:,").append(dateFormat.format(event.getDate())).append("\n");
            }

            if (event.getLocation() != null) {
                writer.append("Location:,").append(escapeCsvValue(event.getLocation())).append("\n");
            }

            writer.append("Total Attendees:,").append(String.valueOf(attendees.size())).append("\n");
            writer.append("\n");

            // Write column headers
            writer.append("Name,Email,Phone\n");

            // Write attendee data
            for (User user : attendees) {
                writer.append(escapeCsvValue(user.getName())).append(",");
                writer.append(escapeCsvValue(user.getEmail())).append(",");
                writer.append(escapeCsvValue(user.getPhone())).append("\n");
            }

        } finally {
            writer.flush();
            writer.close();
        }
    }

    /**
     * Escapes special characters in CSV values.
     *
     * @param value The value to escape
     * @return The escaped value
     */
    private static String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }

        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    /**
     * Opens a share dialog to share the CSV file.
     *
     * @param context The application context
     * @param fileUri The URI of the CSV file
     */
    public static void shareCSVFile(Context context, Uri fileUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(shareIntent, "Share CSV File");
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        }
    }
}