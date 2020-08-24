package de.datenkraken.datenkrake.logging;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.LogMutation;
import de.datenkraken.datenkrake.logging.db.LogDatabase;
import de.datenkraken.datenkrake.logging.db.LogEntry;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloMutation;
import de.datenkraken.datenkrake.type.CreateLogEntry;
import de.datenkraken.datenkrake.util.Callback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class L {

    private static LogDatabase logDatabase;
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);

    private L() {
    }

    public static void init(Context context) {
        logDatabase = LogDatabase.getInstance(context);
    }

    public static void i(String s, Object... args) {
        AsyncTask.execute(() ->
            logDatabase.daoLog().insertOne(new LogEntry("i", format(s, args))));
    }

    public static void w(String s, Object... args) {
        AsyncTask.execute(() ->
            logDatabase.daoLog().insertOne(new LogEntry("w", format(s, args))));
    }

    public static void e(String s, Object... args) {
        AsyncTask.execute(() ->
            logDatabase.daoLog().insertOne(new LogEntry("e", format(s, args))));
    }

    public static void e(Throwable e, String s, Object... args) {
        AsyncTask.execute(() ->
        logDatabase.daoLog().insertOne(new LogEntry("e",
            format(s + "\n" + Log.getStackTraceString(e), args))));
    }

    public static void e(Exception e) {
        AsyncTask.execute(() ->
        logDatabase.daoLog().insertOne(new LogEntry("e", Log.getStackTraceString(e))));
    }

    private static String format(String s, Object... args) {
        synchronized(format) {
            return String.format("[" + format.format(new Date()) + "] " + s, args);
        }
    }

    public static void send(Callback callback) {
        List<LogEntry> logs = logDatabase.daoLog().getAll();
        if (logs.isEmpty()) {
            callback.onSuccess();
            return;
        }

        List<CreateLogEntry> apolloLogEntries = new ArrayList<>();
        for (LogEntry log : logs) {
            apolloLogEntries.add(CreateLogEntry.builder().message(log.message).type(log.type).build());
        }

        LogMutation mutation = LogMutation.builder()
            .list(apolloLogEntries)
            .build();

        new ApolloMutation<LogMutation.Data>() {
            @Override
            public Mutation getMutation() {
                return mutation;
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                super.onFailure(e);
                callback.onFailure();
            }

            @Override
            public void onResponse(@NonNull Response response) {
                super.onResponse(response);
                logDatabase.daoLog().delete();
                callback.onSuccess();
            }
        }.request();
    }
}
