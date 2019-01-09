package com.cool.toast;  
  
import org.apache.cordova.CallbackContext;  
import org.apache.cordova.CordovaPlugin;  
import org.json.JSONArray;  
import org.json.JSONException;  
import android.content.Intent;  
import android.widget.Toast;  
  
public class ShowToast extends CordovaPlugin {  
  
@Override  
public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {  
// TODO Auto-generated method stub  
if("showToast".equals(action)){  
Toast.makeText(cordova.getActivity(), "show...", Toast.LENGTH_SHORT).show();  
callbackContext.success("success");  
return true;  
}else if("showshowToast".equals(action)){  
String str = args.getString(0);  
int len = args.getInt(1);  
  
if(len == 0){  
Toast.makeText(cordova.getActivity(), str, Toast.LENGTH_SHORT).show();  
callbackContext.success("success" + str);  
return true;  
}else{  
Toast.makeText(cordova.getActivity(), str, Toast.LENGTH_LONG).show();  
callbackContext.success("success" + str);  
return true;  
}  
}else if("openActivity".equals(action)){  
openActivity();  
callbackContext.success("success");  
return true;  
}  
 callbackContext.error("error");  
        return false;   
}  
  
private void openActivity() {  
// Intent intent = new Intent(cordova.getActivity(),TestActivity.class);  
// cordova.getActivity().startActivity(intent);  
}  
  
}  