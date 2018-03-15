package cordova.plugin.deezer;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogError;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.player.AbstractTrackListPlayer;
import com.deezer.sdk.player.AlbumPlayer;
import com.deezer.sdk.player.ArtistRadioPlayer;
import com.deezer.sdk.player.PlayerWrapper;
import com.deezer.sdk.player.PlaylistPlayer;
import com.deezer.sdk.player.RadioPlayer;
import com.deezer.sdk.player.TrackPlayer;
import com.deezer.sdk.player.event.OnBufferProgressListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.deezer.sdk.model.User;

import com.deezer.sdk.model.PlayableEntity;

import cordova.plugin.deezer.DeezerPlugin;

public class DeezerSDKController {

    private final static String LOG_TAG = "DeezerSDKController";
    public static String token;
    /** Permissions requested on Deezer accounts. */
    private final static String[] PERMISSIONS = new String[] {
            "email","offline_access"
    };

    private static JSONArray errorMessage = new JSONArray();

    private Activity mActivity;
    private DeezerConnect mConnect;

    private PlayerWrapper mPlayerWrapper;
    private DeezerPlugin mPlugin;

    /**
     *
     * @param activity
     */
    public DeezerSDKController(final Activity activity, final DeezerPlugin plugin) {
        mActivity = activity;
        mPlugin = plugin;
    }

    // /////////////////////////////////////////////////////////////////////////////
    // DeezerJSListener Implementation
    // /////////////////////////////////////////////////////////////////////////////

    public void init(final CallbackContext callbackContext, final String appId) {
        mConnect = new DeezerConnect(mActivity, appId);
        callbackContext.success();

    }

    public void setAuthInfo(final CallbackContext callbackContext, final String token, final String userId, final String userString) {
        Editor context1;
        (context1 = mActivity.getSharedPreferences("deezer-session", 0).edit()).putString("access_token", token);

        context1.putLong("expires_in", 0);
        context1.putString("user", userString);
        context1.putString("access_token_" + userId, token);
        context1.putLong("expires_in_" + userId, 0);
        context1.putString("user_" + userId, userString);

        context1.commit();
        SessionStore sessionStore = new SessionStore();
        sessionStore.restore(mConnect, mActivity);
        callbackContext.success();        
    }

    public void login(final CallbackContext callbackContext) {
        final AuthListener listener = new AuthListener(callbackContext);

        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mConnect.authorize(mActivity, PERMISSIONS, listener);
            }
        });
    }

    public  void setChangePosition(long idx){
        long x = mPlayerWrapper.getTrackDuration()*idx/100;
        mPlayerWrapper.seek(x);
    }

    public  void setChangePositionTo(long idx){
        long x =idx/mPlayerWrapper.getTrackDuration();
        if(x > 0){
            mPlayerWrapper.seek(x);
        } else {
            JSONArray array = new JSONArray();
            array.put("incorrect duration");
            mPlugin.sendUpdate(".onError",new Object[]{array});
        }
    }

    public void getToken(CallbackContext context){
        context.success(this.token);
    }

    public boolean setVolume(float val1, float val2) {
        if(mPlayerWrapper.setStereoVolume(val1,val2)){
            JSONArray arr = new JSONArray();
            arr.put((int)val1);
            arr.put((int)val2);
            mPlugin.sendUpdate(".onChangeVolume",new Object[]{arr});
            LOG.d(LOG_TAG,arr.toString());
            return  true;
        }else {
            LOG.e(LOG_TAG,"ERORROR SET VOLUME");
            return false;
        }
    }

    public void onPlayTracks(final CallbackContext callbackContext, final String ids,
                             final int index, final int offset, final boolean autoPlay, final boolean addToQueue) {
        if (mPlayerWrapper != null) {
            mPlayerWrapper.stop();
            mPlayerWrapper.release();
            mPlayerWrapper = null;
        }
        JSONArray array = new JSONArray();
        try {
            // create the track player
            mPlayerWrapper = new TrackPlayer(mActivity.getApplication(),
                    mConnect, new WifiAndMobileNetworkStateChecker());

            // add a listener
            ((TrackPlayer) mPlayerWrapper)
                    .addPlayerListener(new PlayerListener(callbackContext));
            mPlayerWrapper
                    .addOnPlayerProgressListener(new PlayerProgressListener());
            mPlayerWrapper.addOnBufferProgressListener(new PlayerBufferProgressListener());

            // play the given track id
            long trackId = Long.valueOf(ids);
            ((TrackPlayer) mPlayerWrapper).playTrack(trackId);

        }

        catch (TooManyPlayersExceptions e) {
            Log.e(LOG_TAG, "TooManyPlayersExceptions", e);
            callbackContext.error("TooManyPlayersExceptions");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
        catch (DeezerError e) {
            Log.e(LOG_TAG, "DeezerError", e);
            callbackContext.error("DeezerError");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
        catch (NumberFormatException e) {
            Log.e(LOG_TAG, "NumberFormatException", e);
            callbackContext.error("NumberFormatException");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }   }

    public void onPlayAlbum(final CallbackContext callbackContext, final String id,
                            final int index, final int offset, final boolean autoPlay, final boolean addToQueue) {

        // check if a previous player exists
        if (mPlayerWrapper != null) {
            mPlayerWrapper.stop();
            mPlayerWrapper.release();
            mPlayerWrapper = null;
        }

        try {
            // create the album player
            mPlayerWrapper = new AlbumPlayer(mActivity.getApplication(),
                    mConnect, new WifiAndMobileNetworkStateChecker());

            // add a listener
            ((AlbumPlayer) mPlayerWrapper)
                    .addPlayerListener(new PlayerListener(callbackContext));
            mPlayerWrapper
                    .addOnPlayerProgressListener(new PlayerProgressListener());
            mPlayerWrapper.addOnBufferProgressListener(new PlayerBufferProgressListener());

            // play the given album id
            long albumId = Long.valueOf(id);
            ((AlbumPlayer) mPlayerWrapper).playAlbum(albumId, index);

        }
        catch (TooManyPlayersExceptions e) {
            Log.e(LOG_TAG, "TooManyPlayersExceptions", e);
            callbackContext.error("TooManyPlayersExceptions");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
        catch (DeezerError e) {
            Log.e(LOG_TAG, "DeezerError", e);
            callbackContext.error("DeezerError");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
        catch (NumberFormatException e) {
            Log.e(LOG_TAG, "NumberFormatException", e);
            callbackContext.error("NumberFormatException");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }

    }

    public void onPlayPlaylist(final CallbackContext callbackContext, final String id,
                               final int index, final int offset, final boolean autoPlay, final boolean addToQueue) {
        // check if a previous player exists
        if (mPlayerWrapper != null) {
            mPlayerWrapper.stop();
            mPlayerWrapper.release();
            mPlayerWrapper = null;
        }

        try {
            // create the playlist player
            mPlayerWrapper = new PlaylistPlayer(mActivity.getApplication(),
                    mConnect, new WifiAndMobileNetworkStateChecker());

            // add a listener
            ((PlaylistPlayer) mPlayerWrapper)
                    .addPlayerListener(new PlayerListener(callbackContext));
            mPlayerWrapper
                    .addOnPlayerProgressListener(new PlayerProgressListener());
            mPlayerWrapper.addOnBufferProgressListener(new PlayerBufferProgressListener());

            // play the given playlist id
            long playlistId = Long.valueOf(id);
            ((PlaylistPlayer) mPlayerWrapper).playPlaylist(playlistId, index);

        }
        catch (TooManyPlayersExceptions e) {
            Log.e(LOG_TAG, "TooManyPlayersExceptions", e);
            callbackContext.error("TooManyPlayersExceptions");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
        catch (DeezerError e) {
            Log.e(LOG_TAG, "DeezerError", e);
            callbackContext.error("DeezerError");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
        catch (NumberFormatException e) {
            Log.e(LOG_TAG, "NumberFormatException", e);
            callbackContext.error("NumberFormatException");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
    }

    public void onPlayRadio(final CallbackContext callbackContext, final String id,
                            final int index, final int offset, final boolean autoPlay, final boolean addToQueue) {
        // check if a previous player exists
        if (mPlayerWrapper != null) {
            mPlayerWrapper.stop();
            mPlayerWrapper.release();
            mPlayerWrapper = null;
        }
        //array for exeptions
        JSONArray array = new JSONArray();
        try {
            // create the radio player
            mPlayerWrapper = new RadioPlayer(mActivity.getApplication(),
                    mConnect, new WifiAndMobileNetworkStateChecker());

            // add a listener
            ((RadioPlayer) mPlayerWrapper)
                    .addPlayerListener(new PlayerListener(callbackContext));
            mPlayerWrapper
                    .addOnPlayerProgressListener(new PlayerProgressListener());
            mPlayerWrapper.addOnBufferProgressListener(new PlayerBufferProgressListener());

            // play the given radio id
            long radioId = Long.valueOf(id);
            ((RadioPlayer) mPlayerWrapper).playRadio(radioId);

        }
        catch (TooManyPlayersExceptions e) {
            Log.e(LOG_TAG, "TooManyPlayersExceptions", e);
            callbackContext.error("TooManyPlayersExceptions");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
        catch (DeezerError e) {
            Log.e(LOG_TAG, "DeezerError", e);
            callbackContext.error("DeezerError");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
        catch (NumberFormatException e) {
            Log.e(LOG_TAG, "NumberFormatException", e);
            callbackContext.error("NumberFormatException");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
    }

    public void onPlayArtistRadio(final CallbackContext callbackContext, final String id,
                                  final int index, final int offset, final boolean autoPlay, final boolean addToQueue) {
        // check if a previous player exists
        if (mPlayerWrapper != null) {
            mPlayerWrapper.stop();
            mPlayerWrapper.release();
            mPlayerWrapper = null;
        }

        try {
            // create the radio player
            mPlayerWrapper = new ArtistRadioPlayer(mActivity.getApplication(),
                    mConnect, new WifiAndMobileNetworkStateChecker());

            // add a listener
            ((ArtistRadioPlayer) mPlayerWrapper)
                    .addPlayerListener(new PlayerListener(callbackContext));
            mPlayerWrapper
                    .addOnPlayerProgressListener(new PlayerProgressListener());
            mPlayerWrapper.addOnBufferProgressListener(new PlayerBufferProgressListener());

            // play the given radio id
            long radioId = Long.valueOf(id);
            ((ArtistRadioPlayer) mPlayerWrapper).playArtistRadio(radioId);

        }
        catch (TooManyPlayersExceptions e) {
            Log.e(LOG_TAG, "TooManyPlayersExceptions", e);
            callbackContext.error("TooManyPlayersExceptions");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
        catch (DeezerError e) {
            Log.e(LOG_TAG, "DeezerError", e);
            callbackContext.error("DeezerError");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
        catch (NumberFormatException e) {
            Log.e(LOG_TAG, "NumberFormatException", e);
            callbackContext.error("NumberFormatException");
            mPlugin.sendUpdate(".onError",new Object[]{errorMessage.put(e)});
        }
    }

    public void onPlay(final CallbackContext callbackContext) {
        Log.i(LOG_TAG, "onPlay");

        if (mPlayerWrapper != null) {
            mPlayerWrapper.play();
            callbackContext.success();
        } else {
            callbackContext.error("No player to play");
        }

    }

    public void onPause(final CallbackContext callbackContext) {
        Log.i(LOG_TAG, "onPause");

        if (mPlayerWrapper != null) {
            mPlayerWrapper.pause();
            mPlugin.sendUpdate(".onPause",new Object[]{});
            callbackContext.success();
        } else {
            callbackContext.error("No player to pause");
        }

    }

    public void onNext(final CallbackContext callbackContext) {
        Log.i(LOG_TAG, "onNext");

        if (mPlayerWrapper != null) {
            if (mPlayerWrapper.skipToNextTrack()) {
                callbackContext.success();
            } else {
                callbackContext.error(0);
            }
        } else {
            callbackContext.error("No player to next");
        }
    }

    public void onPrev(final CallbackContext callbackContext) {
        Log.i(LOG_TAG, "onPrev");

        if (mPlayerWrapper != null) {
            if (mPlayerWrapper.skipToPreviousTrack()) {
                callbackContext.success();
            } else {
                callbackContext.error(0);
            }
        } else {
            callbackContext.error("No player to previous");
        }
    }

    // /////////////////////////////////////////////////////////////////////////////
    // DeezerJSListener Implementation
    // /////////////////////////////////////////////////////////////////////////////

    private class AuthListener implements DialogListener {

        private CallbackContext mContext;

        public AuthListener(final CallbackContext context) {
            mContext = context;
        }

        @Override
        public void onComplete(final Bundle bundle) {
            DeezerSDKController.token = String.valueOf(bundle.get("access_token"));
            JSONArray array = new JSONArray();
            mPlugin.sendUpdate(".onLogedIn",new Object[]{array});
            JSONObject dict = new JSONObject();
            for (String key : bundle.keySet()) {
                try {
                    dict.put(key, bundle.getString(key));
                }
                catch (JSONException e) {
                    Log.e(LOG_TAG, "JSONException", e);
                }
            }

            // add save session
            // store the current authentication info
            SessionStore sessionStore = new SessionStore();
            sessionStore.save(mConnect, mActivity);

            mContext.success(dict);
        }

        @Override
        public void onCancel() {
            Log.d(LOG_TAG, "onCancel");
            mContext.error("cancel");
            if(DeezerSDKController.token==null) {
                JSONArray array = new JSONArray();
                array.put("did not login");
                mPlugin.sendUpdate(".onDidNotLogin", new Object[]{array});
            }
        }

        public void onDeezerError(final DeezerError e) {
            Log.e(LOG_TAG, "onDeezerError", e);
            mContext.error("DeezerError");
        }

        public void onError(final DialogError e) {
            Log.e(LOG_TAG, "onError", e);
            mContext.error("Error");
        }

        @Override
        public void onException(final Exception e) {
            Log.e(LOG_TAG, "onException", e);
            mContext.error("Error");
        }
    }

    private class PlayerListener implements RadioPlayerListener {

        private boolean mTrackListSent = false;
        private final CallbackContext mContext;


        public PlayerListener(final CallbackContext context) {
            mContext = context;
        }

        public void onPlayTrack(final PlayableEntity track) {
            Log.i(LOG_TAG, "onPlayTrack " + track.getId());

            if (!mTrackListSent) {
                if (mPlayerWrapper instanceof AbstractTrackListPlayer) {

                    JSONObject callback = new JSONObject();
                    JSONArray data = new JSONArray();

                    /*List<Track> tracks = ((AbstractTrackListPlayer) mPlayerWrapper).getTracks();
                     for (Track t : tracks) {
                     try {
                     data.put(t.toJson());
                     }
                     catch (JSONException e) {
                     // ignore
                     }
                     }*/

                    try {
                        callback.put("data", data);
                        mContext.success(callback);
                    }
                    catch (JSONException e) {
                        mContext.error(0);
                    }
                }
            }
            mPlugin.sentToJS_onCurrentTrack(-1, track);
        }
        public void onTrackEnded(final PlayableEntity track) {
            Log.i(LOG_TAG, "onTrackEnded");
            JSONArray array = new JSONArray();
            mPlugin.sendUpdate(".onTrackEnded",new Object[]{array});
        }

        public void onAllTracksEnded() {
            Log.i(LOG_TAG, "onAllTracksEnded");
        }


        public void onRequestException(final Exception e, final Object request) {
            Log.e(LOG_TAG, "onRequestDeezerError", e);
        }


        public void onTooManySkipsException() {
            Log.e(LOG_TAG, "onTooManySkipsException");
        }
    }

    private class PlayerProgressListener implements OnPlayerProgressListener {

        @Override
        public void onPlayerProgress(final long progressMS) {
            //Log.i(LOG_TAG, "onPlayerProgress progressMS: " + progressMS);
            float position = (float) progressMS / 1000;
            float duration = 0f;
            if (mPlayerWrapper != null) {
                duration = mPlayerWrapper.getTrackDuration() / 1000;
                //Log.i(LOG_TAG, "onPlayerProgress duration : " + duration);
            }

            if (mPlugin != null) {
                // mPlugin.sendToJs_positionChanged(position, duration);
                // mPlugin.sendUpdate(".onPlayerPlay",new Object[]{});
            }
        }
    }

    private class PlayerBufferProgressListener implements OnBufferProgressListener {

        @Override
        public void onBufferProgress(double progressMS) {
            //Log.i(LOG_TAG, "onBufferProgress progressMS: " + progressMS);
            float position = (float) progressMS / 1000;
            if (mPlugin != null) {
                //mPlugin.sendToJS_bufferPosition(position);
            }
        }
    }
}