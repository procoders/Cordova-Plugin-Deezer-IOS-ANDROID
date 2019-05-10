 [![N|Solid](http://procoders.tech/art/pro-powered.png)](http://procoders.tech/)

# Cordova Deezer Plugin
This plugin included all main functions of audio player for Deezer servise

## Installation
**At first you must registering you application with** [Deezer](http://developers.deezer.com/sdk/ios)

You may install latest version from master
```sh
cordova plugin add https://github.com/vitali-bantsevich/cordova-plugin-deezer
```
### Removing the Plugin from project
```sh
cordova plugin rm cordova.plugin.deezer
```
## Supported Platforms
> - Android
> - iOS


### Platform specific
:warning: for iOS platform not implemented method `setVolume()` and `event onChengeVolume()`
It will be fixed when this methods will be in Deezer SDK

**Demo applications**
> - https://github.com/jeweller1980stepanets/DeezerTestAppIOS for iOS
> - https://github.com/jeweller1980stepanets/DeezerTestAppAndroid for android

## Using the plugin
> **You must have premium account from Deezer servise for playing music**

After device is ready you must defined the main variable:
```javascript
var deezerCordova = window.cordova.plugins.DeezerPlugin;
```
:thumbsup: *After this you may use all method in your code.*

## Methods
All methods returning promises, but you can also use standard callback functions.

```javascript
deezerCordova.init(onSuccess, onError,appId);
deezerCordova.login(onSuccess, onError);
deezerCordova.playTrack(onSuccess, onError,trackId);
deezerCordova.playAlbum(onSuccess, onError,albumId);
deezerCordova.playPlaylist(onSuccess, onError,playlistId);
deezerCordova.playRadio(onSuccess, onError,radioId);
deezerCordova.play();
deezerCordova.pause();
deezerCordova.next();
deezerCordova.prev();
deezerCordova.seek(position);//value between 0 - 100%
deezerCordova.seek(position);//value in seconds
deezerCordova.logout();
deezerCordova.setVolume(val1,val2);//val1,val2 - the volume for the left and right channel (between 0-100%)
```

## Events
```javascript
onPosition : function(args){},//args[0] - position, args[1] - duration
onBuffering : function (args){},//(args[0] * 100) + " %";
onCurrentTrack : function(arg){},//arg[1] - Title of track
onPlayerPlay : function(){},
onTrackEnded : function(){},
onPause : function(){},
onChangeVolume : function(args){}//args[0] - the volume for the left channel (between 0 and 100%), args[1] -  the volume for the right channel (between 0 and 100%)
onError: function(args){}//args[0] - error message
```

**Exemle for subscribe:**
```javascript
deezerCordova.Events.onPosition = function(args){..`code`..}
```

## Authors

[Procoders.TECH](https://procoders.tech)

We gear IT up!

> Procoders mission is to ship meaningful code, all our partners come from referrals, and our pricing model is transparent and fair. Drop us a line and let’s start a conversation right now. 

[![](https://www.procoders.tech/art/pro-powered.png)](http://procoders.tech/)

