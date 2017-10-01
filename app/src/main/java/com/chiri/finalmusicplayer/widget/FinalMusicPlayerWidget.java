/*package com.chiri.finalmusicplayer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.chiri.finalmusicplayer.R;
import com.chiri.finalmusicplayer.activities.MainActivity;
import com.chiri.finalmusicplayer.model.Codes;
import com.chiri.finalmusicplayer.model.Song;
*/
/**
 * Implementation of App Widget functionality.
 */
/*public class FinalMusicPlayerWidget extends AppWidgetProvider {

    private TextView songName, artistName;
    private ImageView albumArt;
    private ImageButton playPlause, nextSong, previousSong;
    private BroadcastReceiver receiverResult;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.final_music_player_widget);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        LocalBroadcastManager.getInstance(context).registerReceiver((receiverResult),
                new IntentFilter(Codes.TAG_SEND_RESULT)
        );
        // Enter relevant functionality for when the first widget is created

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiverResult);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Song s = intent.getExtras().getParcelable(Codes.TAG_SONG);
        Log.i("Cancion recibida", s.getSongName());
        songName.setText(s.getSongName());
        albumArt.setImageURI(Uri.parse(s.getAlbumArt()));
        artistName.setText(s.getArtistName());
    }
}

*/