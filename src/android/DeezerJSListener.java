package cordova.plugin.deezer;

import org.apache.cordova.CallbackContext;

public interface DeezerJSListener {
    
    void init(CallbackContext callbackContext, String appId);
    
    void login(CallbackContext callbackContext);
    
    void onPlayTracks(CallbackContext callbackContext, String ids, int index,
                      int offset, boolean autoPlay, boolean addToQueue);
    
    void onPlayAlbum(CallbackContext callbackContext, String id, int index,
                     int offset, boolean autoPlay, boolean addToQueue);
    
    void onPlayPlaylist(CallbackContext callbackContext, String id, int index,
                        int offset, boolean autoPlay, boolean addToQueue);
    
    void onPlayRadio(CallbackContext callbackContext, String id, int index,
                     int offset, boolean autoPlay, boolean addToQueue);
    
    void onPlayArtistRadio(CallbackContext callbackContext, String id,
                           int index, int offset, boolean autoPlay, boolean addToQueue);
    
    void onPlay(CallbackContext callbackContext);
    
    void onPause(CallbackContext callbackContext);
    
    void onNext(CallbackContext callbackContext);
    
    void onPrev(CallbackContext callbackContext);
    
    void setChangePosition(long idxPos);

    void setChangePositionTo(long idxPos);
    
    boolean setVolume(float val1, float val2);

    void getToken(CallbackContext callbackContext);

    void setAuthInfo(CallbackContext callbackContext, String token, String userId, String userString);
}