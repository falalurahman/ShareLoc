package com.shopcoup.shareloc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SQLiteHandler extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ShareLoc.db";

    //Attributes And Table Name of Number Table
    private static final String TABLE_NUMBER = "NUMBER";
    private static final String COLUMN_NUMBER = "COLUMN_NUMBER";

    //Attributes And Table Name of My Address
    private static final String TABLE_MY_ADDRESS = "MY_ADDRESS";
    private static final String COLUMN_UID = "UID";
    private static final String COLUMN_ADDRESS_NAME = "ADDRESS_NAME";
    private static final String COLUMN_PHONE_NUMBER = "PHONE_NUMBER";
    private static final String COLUMN_LATITUDE = "LATITUDE";
    private static final String COLUMN_LONGITUDE = "LONGITUDE";
    private static final String COLUMN_TEXT_ADDRESS = "TEXT_ADDRESS";
    private static final String COLUMN_AUDIO_ADDRESS_LOCATION = "AUDIO_ADDRESS_LOCATION";
    private static final String COLUMN_IS_AUDIO_ADDRESS = "IS_AUDIO_ADDRESS";
    private static final String COLUMN_AUDIO_FILENAME = "AUDIO_FILENAME";
    private static final String COLUMN_AUDIO_CHANGED = "IS_AUDIO_CHANGED";
    private static final String COLUMN_IS_PUBLIC = "IS_PUBLIC";
    private static final String COLUMN_SERVER_UPDATED = "SERVER_UPDATED";

    private static final String TABLE_RECENT_ADDRESS = "RECENT_ADDRESS";
    private static final String TABLE_NOW_ADDRESS = "NOW_ADDRESS";
    private static final String COLUMN_ID = "ID";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Create Number Table
        String query = "CREATE TABLE " + TABLE_NUMBER + "(" + COLUMN_NUMBER + " TEXT);";
        sqLiteDatabase.execSQL(query);

        //Create My Address Table
        query = "CREATE TABLE " + TABLE_MY_ADDRESS + " (" +
                COLUMN_UID + " TEXT, " +
                COLUMN_ADDRESS_NAME + " TEXT, " +
                COLUMN_PHONE_NUMBER + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_TEXT_ADDRESS + " TEXT, " +
                COLUMN_AUDIO_ADDRESS_LOCATION + " TEXT, " +
                COLUMN_IS_AUDIO_ADDRESS + " SMALLINT, " +
                COLUMN_AUDIO_FILENAME + " TEXT, " +
                COLUMN_AUDIO_CHANGED + " SMALLINT, " +
                COLUMN_IS_PUBLIC + " SMALLINT, " +
                COLUMN_SERVER_UPDATED + " SMALLINT " +
                    ");";
        sqLiteDatabase.execSQL(query);

        //Create Recent Address Table
        query = "CREATE TABLE " + TABLE_RECENT_ADDRESS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_UID + " TEXT, " +
                COLUMN_ADDRESS_NAME + " TEXT, " +
                COLUMN_PHONE_NUMBER + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_TEXT_ADDRESS + " TEXT, " +
                COLUMN_AUDIO_ADDRESS_LOCATION + " TEXT, " +
                COLUMN_IS_AUDIO_ADDRESS + " SMALLINT, " +
                COLUMN_AUDIO_FILENAME + " TEXT, " +
                COLUMN_IS_PUBLIC + " SMALLINT " +
                ");";
        sqLiteDatabase.execSQL(query);

        //Create Now Address Table
        query = "CREATE TABLE " + TABLE_NOW_ADDRESS + " (" +
                COLUMN_UID + " TEXT, " +
                COLUMN_ADDRESS_NAME + " TEXT, " +
                COLUMN_PHONE_NUMBER + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_TEXT_ADDRESS + " TEXT, " +
                COLUMN_AUDIO_ADDRESS_LOCATION + " TEXT, " +
                COLUMN_IS_AUDIO_ADDRESS + " SMALLINT, " +
                COLUMN_AUDIO_FILENAME + " TEXT, " +
                COLUMN_IS_PUBLIC + " SMALLINT " +
                ");";
        sqLiteDatabase.execSQL(query);
    }

    public void Upgrade(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NUMBER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MY_ADDRESS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENT_ADDRESS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NOW_ADDRESS);
        onCreate(sqLiteDatabase);
        sqLiteDatabase.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    //Add Number To Number Table
    public void addNumber(String number){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NUMBER, number);
        db.insert(TABLE_NUMBER, null, values);
        db.close();
    }

    //Get number present
    public String getNumber(){
        String number = null;
        String query = "Select * from "+TABLE_NUMBER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToLast()){
            number = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return number;
    }

    //Check if number present
    public boolean hasNumber(){
        boolean hasNumber = false;
        String query = "Select * from "+TABLE_NUMBER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() > 0){
            hasNumber = true;
        }
        cursor.close();
        db.close();
        return hasNumber;
    }

    //Add Address To My Address
    public void addAddress(Address address){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ADDRESS_NAME,address.getAddressName());
        values.put(COLUMN_PHONE_NUMBER,address.getPhoneNumber());
        values.put(COLUMN_LATITUDE,address.getVisualAddressLatitude());
        values.put(COLUMN_LONGITUDE,address.getVisualAddressLongitude());
        values.put(COLUMN_TEXT_ADDRESS,address.getTextualAddress());
        values.put(COLUMN_AUDIO_ADDRESS_LOCATION,address.getAudioAddress());
        values.put(COLUMN_IS_AUDIO_ADDRESS,(address.isSetAudioAddress() ? 1 : 0));
        values.put(COLUMN_AUDIO_CHANGED,(address.isAudioAddressChanged() ? 1 : 0));
        values.put(COLUMN_IS_PUBLIC, (address.isPublic() ? 1 : 0));
        values.put(COLUMN_SERVER_UPDATED,0);
        values.put(COLUMN_AUDIO_FILENAME, address.getAudioFileName());
        values.put(COLUMN_UID,address.getUID());
        db.insert(TABLE_MY_ADDRESS, null, values);
        db.close();
    }


    //Get All Address
    public ArrayList<Address> getMyAddress(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_MY_ADDRESS +" ;";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();
        ArrayList<Address> myAddress = new ArrayList<>();
        while (!cursor.isAfterLast()){
            if(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME))!= null) {
                Address tempsubject = new Address();
                tempsubject.setAddressName(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME)));
                tempsubject.setVisualAddressLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)));
                tempsubject.setVisualAddressLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)));
                tempsubject.setTextualAddress(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT_ADDRESS)));
                tempsubject.setAudioAddress(cursor.getString(cursor.getColumnIndex(COLUMN_AUDIO_ADDRESS_LOCATION)));
                if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_AUDIO_ADDRESS)) == 1)
                    tempsubject.setIsSetAudioAddress(true);
                else
                    tempsubject.setIsSetAudioAddress(false);
                myAddress.add(tempsubject);
            }
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return myAddress;
    }

    //Get All Address Name To Make The My Address Page
    public ArrayList<JSONObject> GetAllAddressNames(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT " + COLUMN_ADDRESS_NAME + ", " + COLUMN_PHONE_NUMBER + ", " + COLUMN_UID + " FROM " + TABLE_MY_ADDRESS +" ;";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();
        ArrayList<JSONObject> AllAddressNames = new ArrayList<>();
        while (!cursor.isAfterLast()){
            if(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME)) != null){
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("AddressName", cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME)));
                    jsonObject.put("PhoneNumber", cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                    jsonObject.put("uuid",cursor.getString(cursor.getColumnIndex(COLUMN_UID)));
                }catch (JSONException ex){
                }
                AllAddressNames.add(jsonObject);
            }
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return AllAddressNames;
    }

    //Check If Address Name Already Present
    public boolean isAddressName(String AddressName){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_MY_ADDRESS + " WHERE " +
                        COLUMN_ADDRESS_NAME + "=\"" + AddressName +"\";";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();
        if(cursor.getCount()>0)
            return true;
        else
            return false;
    }

    //Get All Address
    public Address getAddressOfName(String AddressName, String UID){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_MY_ADDRESS +
                " WHERE " + COLUMN_ADDRESS_NAME + "=\"" + AddressName +"\"" +
                " AND " + COLUMN_UID + "=\"" + UID + "\";";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        Address tempAddress = new Address();
        while (!cursor.isAfterLast()){
            if(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME))!= null) {
                tempAddress.setAddressName(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME)));
                tempAddress.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                tempAddress.setVisualAddressLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)));
                tempAddress.setVisualAddressLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)));
                tempAddress.setTextualAddress(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT_ADDRESS)));
                tempAddress.setAudioAddress(cursor.getString(cursor.getColumnIndex(COLUMN_AUDIO_ADDRESS_LOCATION)));
                if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_AUDIO_ADDRESS)) == 1)
                    tempAddress.setIsSetAudioAddress(true);
                else
                    tempAddress.setIsSetAudioAddress(false);
                if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PUBLIC)) == 1)
                    tempAddress.setIsPublic(true);
                else
                    tempAddress.setIsPublic(false);
            }
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return tempAddress;
    }

    //Edit Address
    public void editAddress(Address address){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "UPDATE " + TABLE_MY_ADDRESS + " SET " +
                        COLUMN_LATITUDE + " = " + Double.toString(address.getVisualAddressLatitude()) + ", " +
                        COLUMN_LONGITUDE + " = " + Double.toString(address.getVisualAddressLongitude()) + ", " +
                        COLUMN_TEXT_ADDRESS + " = \"" + address.getTextualAddress() + "\", " +
                        COLUMN_IS_AUDIO_ADDRESS + " = " + Integer.toString((address.isSetAudioAddress ? 1 : 0)) + ", " +
                        COLUMN_AUDIO_ADDRESS_LOCATION + " = \"" + address.getAudioAddress() + "\", " +
                        COLUMN_AUDIO_CHANGED + " = " + Integer.toString((address.isAudioAddressChanged() ? 1 : 0)) + ", " +
                        COLUMN_SERVER_UPDATED + " = 0 " +
                        " WHERE " + COLUMN_ADDRESS_NAME + " = \"" + address.getAddressName() +"\"" +
                        " AND " + COLUMN_PHONE_NUMBER + " = \"" + address.getPhoneNumber() + "\";";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    //Check If Address Public Or Private
    public boolean GetAddressIsPublic (String AddressName){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query =  "SELECT " + COLUMN_IS_PUBLIC + " FROM " + TABLE_MY_ADDRESS +
                        " WHERE " + COLUMN_ADDRESS_NAME + " = \"" + AddressName + "\";";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();
        if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PUBLIC)) == 1) {
            cursor.close();
            sqLiteDatabase.close();
            return true;
        }
        else {
            cursor.close();
            sqLiteDatabase.close();
            return false;
        }
    }

    //Toggle Public And Private Of Address
    public void toggleIsPublic(String AddressName){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query =  "SELECT " + COLUMN_IS_PUBLIC + " FROM " + TABLE_MY_ADDRESS +
                        " WHERE " + COLUMN_ADDRESS_NAME + " = \"" + AddressName + "\";";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();
        if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PUBLIC)) == 1) {
            query = "UPDATE " + TABLE_MY_ADDRESS + " SET " +
                    COLUMN_IS_PUBLIC + " = 0, " +
                    COLUMN_SERVER_UPDATED + " = 0" +
                    " WHERE " + COLUMN_ADDRESS_NAME + " = \"" + AddressName + "\";";
            sqLiteDatabase.execSQL(query);
            cursor.close();
            sqLiteDatabase.close();
        }else {
            query = "UPDATE " + TABLE_MY_ADDRESS + " SET " +
                    COLUMN_IS_PUBLIC + " = 1, " +
                    COLUMN_SERVER_UPDATED + " = 0" +
                    " WHERE " + COLUMN_ADDRESS_NAME + " = \"" + AddressName + "\";";
            sqLiteDatabase.execSQL(query);
            cursor.close();
            sqLiteDatabase.close();
        }
    }

    //Get All UnUpdated Addresses
    public ArrayList<Address> GetNotUpdatedAddresses(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query =  "SELECT * FROM " + TABLE_MY_ADDRESS +
                        " WHERE " + COLUMN_SERVER_UPDATED + " = 0;";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<Address> AllNotUpdatedAddress = new ArrayList<>();
        while (!cursor.isAfterLast()){
            if(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME))!= null) {
                Address tempAddress = new Address();
                tempAddress.setUID(cursor.getString(cursor.getColumnIndex(COLUMN_UID)));
                tempAddress.setAddressName(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME)));
                tempAddress.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                tempAddress.setVisualAddressLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)));
                tempAddress.setVisualAddressLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)));
                tempAddress.setTextualAddress(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT_ADDRESS)));
                tempAddress.setAudioAddress(cursor.getString(cursor.getColumnIndex(COLUMN_AUDIO_ADDRESS_LOCATION)));
                if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_AUDIO_ADDRESS)) == 1)
                    tempAddress.setIsSetAudioAddress(true);
                else
                    tempAddress.setIsSetAudioAddress(false);
                tempAddress.setAudioFileName(cursor.getString(cursor.getColumnIndex(COLUMN_AUDIO_FILENAME)));
                if(cursor.getInt(cursor.getColumnIndex(COLUMN_AUDIO_CHANGED)) == 1)
                    tempAddress.setAudioAddressChanged(true);
                else
                    tempAddress.setAudioAddressChanged(false);
                if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PUBLIC)) == 1)
                    tempAddress.setIsPublic(true);
                else
                    tempAddress.setIsPublic(false);
                AllNotUpdatedAddress.add(tempAddress);
            }
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return AllNotUpdatedAddress;
    }

    //Set Audio File Name For Address
    public void SetAudioFileName(String AudioFileName, String AddressName){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query =  "UPDATE " + TABLE_MY_ADDRESS + " SET " +
                        COLUMN_AUDIO_CHANGED + " = 0, " +
                        COLUMN_AUDIO_FILENAME + " = \"" + AudioFileName + "\", " +
                        COLUMN_SERVER_UPDATED + " = 0" +
                        " WHERE " + COLUMN_ADDRESS_NAME + " = \"" + AddressName + "\";";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }


    //Set UID For Address
    public void SetUID(String UID, String AddressName){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query =  "UPDATE " + TABLE_MY_ADDRESS + " SET " +
                COLUMN_UID + " = \"" + UID + "\", " +
                COLUMN_SERVER_UPDATED + " = 0" +
                " WHERE " + COLUMN_ADDRESS_NAME + " = \"" + AddressName + "\";";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    //Set ServerUpdated For Address
    public void SetServerUpdatedTrue(String AddressName){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query =  "UPDATE " + TABLE_MY_ADDRESS + " SET " +
                COLUMN_SERVER_UPDATED + " = 1" +
                " WHERE " + COLUMN_ADDRESS_NAME + " = \"" + AddressName + "\";";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    //Add Address To My Address
    public void addRecentAddress(Address address){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ADDRESS_NAME,address.getAddressName());
        values.put(COLUMN_PHONE_NUMBER,address.getPhoneNumber());
        values.put(COLUMN_LATITUDE,address.getVisualAddressLatitude());
        values.put(COLUMN_LONGITUDE,address.getVisualAddressLongitude());
        values.put(COLUMN_TEXT_ADDRESS,address.getTextualAddress());
        values.put(COLUMN_AUDIO_ADDRESS_LOCATION,address.getAudioAddress());
        values.put(COLUMN_IS_AUDIO_ADDRESS,(address.isSetAudioAddress() ? 1 : 0));
        values.put(COLUMN_IS_PUBLIC, (address.isPublic() ? 1 : 0));
        values.put(COLUMN_AUDIO_FILENAME, address.getAudioFileName());
        values.put(COLUMN_UID,address.getUID());
        db.insert(TABLE_NOW_ADDRESS, null, values);
        db.close();
        DeleteUIDRow(address.getUID());
        db = getWritableDatabase();
        db.insert(TABLE_RECENT_ADDRESS, null, values);
        db.close();
    }

    public void clearNowAddress(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "DROP TABLE IF EXISTS " + TABLE_NOW_ADDRESS;
        sqLiteDatabase.execSQL(query);

        //Create Now Address Table
        query = "CREATE TABLE " + TABLE_NOW_ADDRESS + " (" +
                COLUMN_UID + " TEXT, " +
                COLUMN_ADDRESS_NAME + " TEXT, " +
                COLUMN_PHONE_NUMBER + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_TEXT_ADDRESS + " TEXT, " +
                COLUMN_AUDIO_ADDRESS_LOCATION + " TEXT, " +
                COLUMN_IS_AUDIO_ADDRESS + " SMALLINT, " +
                COLUMN_AUDIO_FILENAME + " TEXT, " +
                COLUMN_IS_PUBLIC + " SMALLINT " +
                ");";
        sqLiteDatabase.execSQL(query);
    }


    //Get All Address Name To Make The My Address Page
    public ArrayList<JSONObject> GetAllRecentAddressNames(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT " + COLUMN_ADDRESS_NAME + ", " + COLUMN_PHONE_NUMBER + ", " + COLUMN_UID + " FROM " + TABLE_RECENT_ADDRESS +
                " ORDER BY " + COLUMN_ID  + " DESC LIMIT 20 ;";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();
        ArrayList<JSONObject> AllAddressNames = new ArrayList<>();
        while (!cursor.isAfterLast()){
            if(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME)) != null){
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("AddressName", cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME)));
                    jsonObject.put("PhoneNumber", cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                    jsonObject.put("uuid",cursor.getString(cursor.getColumnIndex(COLUMN_UID)));
                }catch (JSONException ex){
                }
                AllAddressNames.add(jsonObject);
            }
            cursor.moveToNext();
        }
        cursor.close();
        query = "DELETE FROM " + TABLE_RECENT_ADDRESS +
                " WHERE " + COLUMN_ID + " NOT IN (" + " SELECT " + COLUMN_ID + "  FROM ( SELECT " + COLUMN_ID + " FROM " + TABLE_RECENT_ADDRESS +
                " ORDER BY " + COLUMN_ID + " DESC LIMIT 20 ));";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
        return AllAddressNames;
    }

    //Get All Address Name To Make The My Address Page
    public ArrayList<JSONObject> GetAllNowAddressNames(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT " + COLUMN_ADDRESS_NAME + ", " + COLUMN_PHONE_NUMBER + ", " + COLUMN_UID + " FROM " + TABLE_NOW_ADDRESS +" ;";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();
        ArrayList<JSONObject> AllAddressNames = new ArrayList<>();
        while (!cursor.isAfterLast()){
            if(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME)) != null){
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("AddressName", cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME)));
                    jsonObject.put("PhoneNumber", cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                    jsonObject.put("uuid",cursor.getString(cursor.getColumnIndex(COLUMN_UID)));
                }catch (JSONException ex){
                }
                AllAddressNames.add(jsonObject);
            }
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return AllAddressNames;
    }

    //Get All Address
    public Address getRecentAddressOfName(String AddressName, String UID){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_RECENT_ADDRESS +
                " WHERE " + COLUMN_ADDRESS_NAME + "=\"" + AddressName +"\"" +
                " AND " + COLUMN_UID + "=\"" + UID + "\";";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();
        Address tempAddress = new Address();
        while (!cursor.isAfterLast()){
            if(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME))!= null) {
                tempAddress.setAddressName(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME)));
                tempAddress.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                tempAddress.setVisualAddressLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)));
                tempAddress.setVisualAddressLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)));
                tempAddress.setTextualAddress(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT_ADDRESS)));
                tempAddress.setAudioAddress(cursor.getString(cursor.getColumnIndex(COLUMN_AUDIO_ADDRESS_LOCATION)));
                tempAddress.setAudioFileName(cursor.getString(cursor.getColumnIndex(COLUMN_AUDIO_FILENAME)));
                if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_AUDIO_ADDRESS)) == 1)
                    tempAddress.setIsSetAudioAddress(true);
                else
                    tempAddress.setIsSetAudioAddress(false);
                if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PUBLIC)) == 1)
                    tempAddress.setIsPublic(true);
                else
                    tempAddress.setIsPublic(false);
            }
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return tempAddress;
    }


    public boolean GetRecentAddressStored(String UID){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_MY_ADDRESS + " WHERE " + COLUMN_UID + " = \"" + UID + "\";";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();
        if(!cursor.isAfterLast()) {
            cursor.close();
            sqLiteDatabase.close();
            return true;
        }
        else {
            cursor.close();
            sqLiteDatabase.close();
            return false;
        }
    }

    public void DeleteUIDRow(String UID){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "DELETE FROM " + TABLE_RECENT_ADDRESS + " WHERE " + COLUMN_UID + "=\"" + UID + "\";";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    //Get All Address Name To Make The My Address Page
    public ArrayList<JSONObject> GetAllOnceUpdatedAddressNames(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT " + COLUMN_ADDRESS_NAME + ", " + COLUMN_PHONE_NUMBER + ", " + COLUMN_UID + " FROM " + TABLE_MY_ADDRESS +
                        " WHERE NOT " + COLUMN_UID + " = \"\" ;";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<JSONObject> AllAddressNames = new ArrayList<>();
        while (!cursor.isAfterLast()){
            if(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME)) != null){
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("AddressName", cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_NAME)));
                    jsonObject.put("PhoneNumber", cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                    jsonObject.put("uuid",cursor.getString(cursor.getColumnIndex(COLUMN_UID)));
                }catch (JSONException ex){
                }
                AllAddressNames.add(jsonObject);
            }
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return AllAddressNames;
    }
}
