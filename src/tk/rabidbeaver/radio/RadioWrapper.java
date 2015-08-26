package tk.rabidbeaver.radio;

import android.util.Log;

public class RadioWrapper {
	protected static Radio radio;
	
	static {
        System.loadLibrary("hdtuner");
    }
	public RadioWrapper(Radio dynamic){
		radio = dynamic;
	}
	public native void initRadio();
	public native void deinitRadio();
	public native void sendCommand(String command);

	public static void radioCallback(String name, String value){
		//TODO: do something with the callback data
		Log.d("RadioWrapper","GOT BACK: "+name+"/"+value);
		radio.updateStatus(name,value);
	}
	
}
