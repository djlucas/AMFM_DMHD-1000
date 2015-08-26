package tk.rabidbeaver.radio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Radio extends Activity {
	
	private RadioWrapper rw;
	private RadioDB rdb;
	private Button channelbtn;
	private TextView radiotext;
	private TextView programservice;
	private boolean lastPower = true;
	private String lastChannel = "1010 AM";
	private String lastChannelAM = "1010 AM";
	private String lastChannelFM = "99.9 FM";
	
	private boolean band_fm;
	
	private Thread signalpoll;
	private boolean pollsignal;
	
	private MenuItem signalmenu;
	private MenuItem powermenu;
	
	private int lastss = 0;
	
	private EditText channelinput;
	private Button dialogband;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_radio);
		rdb = new RadioDB(this);
		lastChannel = rdb.getLastChannel();
		lastChannelAM = rdb.getLastAM();
		lastChannelFM = rdb.getLastFM();
		lastPower = rdb.getLastPower()>0?true:false;
		if (lastChannel.contains("FM")) band_fm = true;
		else band_fm = false;
		rw = new RadioWrapper(this);
		
		//channel = (Button) this.findViewById(R.id.channelline);
		radiotext = (TextView) this.findViewById(R.id.radiotextline);
		programservice = (TextView) this.findViewById(R.id.programserviceline);
		
		if (lastChannel.contains("FM")) band_fm=true;
		else band_fm=false;
		
		channelbtn = (Button) this.findViewById(R.id.channelline);
		channelbtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(Radio.this);
				builder.setTitle("Enter New Channel:");
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   //TODO: add some format checking....
			        	   rw.sendCommand("tune "+channelinput.getText().toString()+" "+dialogband.getText().toString());
			        	   dialog.dismiss();
			           }
			       });
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   dialog.dismiss();
			           }
			       });

				AlertDialog dialog = builder.create();
				
				LayoutInflater inflater=Radio.this.getLayoutInflater();
				View layout=inflater.inflate(R.layout.dialog_channel,null);
				dialog.setView(layout);
				channelinput = (EditText) layout.findViewById(R.id.channel);
				dialogband = (Button) layout.findViewById(R.id.dialog_amfm);
				channelinput.append(lastChannel.split(" ")[0]);
				dialogband.setText(band_fm?"FM":"AM");
				dialogband.setOnClickListener(new Button.OnClickListener(){
					@Override
					public void onClick(View v) {
						if (dialogband.getText().toString().equalsIgnoreCase("AM"))
							dialogband.setText("FM");
						else dialogband.setText("AM");
					}
				});
				dialog.show();
			}
		});
		
		Button amfmbtn = (Button) this.findViewById(R.id.amfmbtn);
		amfmbtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				if (band_fm){
					rw.sendCommand("tune "+lastChannelAM);
					band_fm = false;
				} else {
					rw.sendCommand("tune "+lastChannelFM);
					band_fm = true;
				}
			}
		});
		
		Button seekupbtn = (Button) this.findViewById(R.id.seekup);
		seekupbtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				rw.sendCommand("seekup");
			}
		});
		
		Button seekdownbtn = (Button) this.findViewById(R.id.seekdown);
		seekdownbtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				rw.sendCommand("seekdown");
			}
		});
		
		Button tuneupbtn = (Button) this.findViewById(R.id.tuneup);
		tuneupbtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				rw.sendCommand("tuneup");
			}
		});
		
		Button tunedownbtn = (Button) this.findViewById(R.id.tunedown);
		tunedownbtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				rw.sendCommand("tunedown");
			}
		});
	}
	
	public void updateStatus(final String name, final String value){
		if (name.equalsIgnoreCase("power")){
			lastPower = true;
			rdb.setLastPower(true);
			rw.sendCommand("volume 50");
			rw.sendCommand("tune "+lastChannel);
			rw.sendCommand("dtr 1");
			if (powermenu != null){
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						powermenu.setIcon(R.drawable.ic_action_volume_on);
					}
				});
			}
		} else {
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					if (name.equalsIgnoreCase("rdsprogramservice"))
						programservice.setText(value.replaceAll(" +", " ").trim());
					if (name.equalsIgnoreCase("rdsradiotext"))
						radiotext.setText(value.replaceAll(" +", " ").trim());
					if (name.equalsIgnoreCase("tune") || name.equalsIgnoreCase("seek")){
						if (name.equalsIgnoreCase("tune")){
							if (signalmenu != null) signalmenu.setIcon(R.drawable.bars_0);
							lastss = 0;
							rw.sendCommand("requestsignalstrength");
							lastChannel = value;
							if (lastChannel.contains("FM")){
								band_fm=true;
								lastChannelFM=lastChannel;
								rdb.setLastFM(lastChannelFM);
							} else {
								band_fm=false;
								lastChannelAM=lastChannel;
								rdb.setLastAM(lastChannelAM);
							}
						}
						channelbtn.setText(value);
						programservice.setText("");
						radiotext.setText("");
					}
					if (name.equalsIgnoreCase("signalstrength")){
						/*
						 * All values are approximations based on observation of the factory faceplate's signal level meter.
						 * Assumption is that maximum signal strength is 2850 = 100%, and that under 400 is completely
						 * garbage 0%. So we scale the value obtained from the radio into the range of 0-->2450 and
						 * then convert to bars and percent.
						 */
						int ss = Integer.parseInt(value);
						Log.d("RADIO","SS: "+ss);
						if (ss < 400) ss = 0;
						else if (ss > 2850) ss = 100;
						else ss = (int)(((float) (ss - 400) / (float) 2450) * 100);
						int bars = 0;
						if (ss > 20) bars++;
						if (ss > 40) bars++;
						if (ss > 60) bars++;
						if (ss > 80) bars++;
						Log.d("RADIO",bars+" Bars, "+ss+"%");
						if (signalmenu != null){
							switch (bars){
								case 0:
									if (lastss != 0){
										signalmenu.setIcon(R.drawable.bars_0);
										lastss = 0;
									}
									break;
								case 1:
									if (lastss != 1){
										signalmenu.setIcon(R.drawable.bars_1);
										lastss = 1;
									}
									break;
								case 2:
									if (lastss != 2){
										signalmenu.setIcon(R.drawable.bars_2);
										lastss = 2;
									}
									break;
								case 3:
									if (lastss != 3){
										signalmenu.setIcon(R.drawable.bars_3);
										lastss = 3;
									}
									break;
								case 4:
									if (lastss != 4){
										signalmenu.setIcon(R.drawable.bars_4);
										lastss = 4;
									}
									break;
							}
						}
					}
				}
			});
		}
	}
	
	/*@Override
	protected void onStop(){
		rw.deinitRadio();
		super.onStop();
	}*/
	
	@Override
	protected void onStart(){
		super.onStart();
		if (lastPower){
			rw.initRadio();
		}
	}

	@Override
	protected void onPause(){
		pollsignal = false;
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		signalpoll = new Thread(new Runnable(){
			@Override
			public void run() {
				pollsignal = true;
				while (pollsignal){
					rw.sendCommand("requestsignalstrength");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {}
				}
			}
		});
		signalpoll.start();
		if (powermenu != null){
			if (lastPower) powermenu.setIcon(R.drawable.ic_action_volume_on);
			else powermenu.setIcon(R.drawable.ic_action_volume_muted);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.radio, menu);
		signalmenu = menu.findItem(R.id.action_signal);
		powermenu = menu.findItem(R.id.action_power);
		if (lastPower) powermenu.setIcon(R.drawable.ic_action_volume_on);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_power) {
			if (lastPower){
				powermenu.setIcon(R.drawable.ic_action_volume_muted);
				rw.sendCommand("dtr 0");
				lastPower = false;
				rdb.setLastPower(false);
			} else {
				powermenu.setIcon(R.drawable.ic_action_volume_on);
				rw.initRadio();
				rw.sendCommand("dtr 1");
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
