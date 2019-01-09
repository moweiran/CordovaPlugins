package com.txw.plugin;

import android.os.Handler;
import android.os.Message;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

public class VersionCheck extends CordovaPlugin {

  public final static String XmlVersionUrl = "http://api.lctxw.com/DownloadCenter/merchant-version.xml";
  public final static String AppPackageName = "merchant.lctxw.com";
  private Handler updateHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case 0: // have new version exist
          break;
        case 1: // cancel download
          break;
      }
    }

    ;
  };

  @Override
  public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
    return true;
  }

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    final UpdateManager manager = new UpdateManager(cordova.getActivity(),
      updateHandler);

    manager.checkUpdate(XmlVersionUrl,AppPackageName);
  }
}
