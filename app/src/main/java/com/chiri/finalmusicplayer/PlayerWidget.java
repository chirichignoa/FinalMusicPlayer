package com.chiri.finalmusicplayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.chiri.finalmusicplayer.model.Codes;
import com.chiri.finalmusicplayer.model.Song;
import com.chiri.finalmusicplayer.service.MusicService;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.chiri.finalmusicplayer.model.Codes.TAG_SEND_RESULT;

/**
 * Implementation of App Widget functionality.
 */
public class PlayerWidget extends AppWidgetProvider {

    private static Song currentSong;

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.d("Widget","Aca en el updateAppWidget");
        String mensaje = "No hay reproducción";

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.player_widget);

        //Asociamos los 'eventos' al widget
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(Codes.TAG_TYPE,TAG_SEND_RESULT);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, appWidgetId,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.updateButton, pendingIntent);

        if(currentSong != null) {
            mensaje = currentSong.getSongName();
        }
        else {
            mensaje = "no hay reproducción";
        }
        Log.d("Widget", "Mensaje: " + mensaje);
        views.setTextViewText(R.id.appwidget_text, mensaje);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Iteramos la lista de widgets en ejecución
        for (int i = 0; i < appWidgetIds.length; i++) {
            //ID del widget actual
            int widgetId = appWidgetIds[i];

            //Actualizamos el widget actual
            updateAppWidget(context, appWidgetManager, widgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("Widget", "Recibiendo");
        if (intent != null) {
            if (intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE")) {
                //Obtenemos el ID del widget a actualizar
                int widgetId = intent.getIntExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);

                //Obtenemos el widget manager de nuestro contexto
                AppWidgetManager widgetManager =
                        AppWidgetManager.getInstance(context);

                currentSong = intent.getParcelableExtra(Codes.TAG_SONG);
                if(currentSong != null) {
                    Log.d("Widget", "Recibi: " + currentSong.toString());
                    Log.d("Widget", "Para el widget con id " + widgetId);
                } else {
                    Log.d("Widget", "Recibi null ");
                }

                //Actualizamos el widget
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    updateAppWidget(context, widgetManager, widgetId);
                }
            }
        } else {
            Log.d("Widget", "intent null");
        }
    }

    public static void setCurrentSong(Song song) {
        currentSong = song;
        Log.d("Widget", "Cancion set");
        Log.d("Widget", currentSong.toString());
    }
}

