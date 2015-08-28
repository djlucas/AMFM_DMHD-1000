package tk.rabidbeaver.radio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RadioDB extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "RBRadio";
	
	public RadioDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String radiocreate = "CREATE TABLE radiosettings (id INTEGER PRIMARY KEY AUTOINCREMENT, lastfm TEXT, lastam TEXT, lastchannel TEXT, lastpower INTEGER)";
		String favscreate = "CREATE TABLE radiofavorites(id INTEGER PRIMARY KEY AUTOINCREMENT, priority INTEGER, channel TEXT)";
		
		db.execSQL(radiocreate);
		db.execSQL(favscreate);
		
		String setdefaults = "INSERT INTO radiosettings (lastfm, lastam, lastchannel, lastpower) VALUES ('99.9 FM', '1010 AM', '1010 AM', 1)";
		db.execSQL(setdefaults);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// nothing to do until the next version....
	}
	
	public String getLastFM(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT lastfm FROM radiosettings LIMIT 1", null);
		if (cursor.moveToFirst()) {
			return cursor.getString(0);
		}
		return null;
	}
	
	public void setLastFM(String lastfm){
		//Log.d("RADIODB","setLastFM:"+lastfm);
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("lastfm", lastfm);
		values.put("lastchannel", lastfm);
		db.update("radiosettings", values, "id = ?", new String[]{"1"});
		db.close();
	}
	
	public String getLastAM(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT lastam FROM radiosettings LIMIT 1", null);
		if (cursor.moveToFirst()) {
			return cursor.getString(0);
		}
		return null;
	}
	
	public void setLastAM(String lastam){
		//Log.d("RADIODB","setLastAM:"+lastam);
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("lastam", lastam);
		values.put("lastchannel", lastam);
		db.update("radiosettings", values, "id = ?", new String[]{"1"});
		db.close();
	}
	
	public String getLastChannel(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT lastchannel FROM radiosettings LIMIT 1", null);
		if (cursor.moveToFirst()) {
			return cursor.getString(0);
		}
		return null;
	}
	
	public int getLastPower(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT lastpower FROM radiosettings LIMIT 1", null);
		if (cursor.moveToFirst()) {
			return cursor.getInt(0);
		}
		return 0;
	}
	
	public void setLastPower(boolean power){
		//Log.d("RADIODB","setLastPower:"+Boolean.toString(power));
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("lastpower", power?"1":"0");
		db.update("radiosettings", values, "id = ?", new String[]{"1"});
		db.close();
	}
	
	private void resyncFavs(){
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("SELECT channel FROM radiofavorites ORDER BY priority ASC", null);
		int rows = cursor.getCount();
		String[] oldorder = new String[rows];
		
		for (int i=0; i<rows; i++){
			cursor.moveToPosition(i);
			oldorder[i] = cursor.getString(0);
		}
		
		for (int i=rows-1; i>=0; i--){
			db.execSQL("UPDATE radiofavorites SET priority="+10*(i+1)+" WHERE channel='"+oldorder[i]+"'");
		}
		
		db.close();
	}
	
	public void moveFav(String channel, boolean up){
		SQLiteDatabase db = this.getWritableDatabase();

		// first, find the channel's old priority.
		Cursor cursor = db.rawQuery("SELECT priority FROM radiofavorites WHERE channel='"+channel+"'",null);
		int priority = 0;
		if (cursor.moveToFirst()){
			priority = cursor.getInt(0);
		} else return;
		
		// if we are increasing priority, but already are maximum, do nothing -- return.
		if (!up && priority == 10) return;
		
		// increment the priority past the adjacent favorite
		priority = up?priority+11:priority-11;
		db.execSQL("UPDATE radiofavorites SET priority ="+priority+" WHERE channel='"+channel+"'");
		
		db.close();
		resyncFavs();
	}
	
	public void setFav(String channel, boolean add){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("radiofavorites", "channel = ?", new String[]{channel});
		if (add){
			Cursor cursor = db.rawQuery("SELECT MAX(priority) FROM radiofavorites", null);
			int maxpriority = 0;
			if (cursor.moveToFirst()){
				maxpriority = cursor.getInt(0);
			}
			ContentValues values = new ContentValues();
			values.put("channel", channel);
			values.put("priority", maxpriority+10);
			db.insert("radiofavorites", null, values);
		}
		db.close();
		resyncFavs();
	}
	
	public boolean isFavorite(String channel){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM radiofavorites WHERE channel='"+channel+"'", null);
		if (cursor.moveToFirst()) {
			db.close();
			return true;
		}
		db.close();
		return false;
	}
	
	public String[] getAllFavorites(){
		String[] retval = null;
		int rows = 0;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT channel FROM radiofavorites ORDER BY priority ASC", null);
		rows = cursor.getCount();
		retval = new String[rows];
		for (int i=0; i<rows; i++){
			cursor.moveToPosition(i);
			retval[i] = cursor.getString(0);
		}
		return retval;
	}
}
