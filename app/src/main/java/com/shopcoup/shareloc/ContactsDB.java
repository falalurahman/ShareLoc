package com.shopcoup.shareloc;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContactsDB extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Contacts.db";

    //Attributes And Table Name of Contact Table
    private static final String TABLE_CONTACTS = "CONTACTS";
    private static final String COLUMN_CONTACT_NAME = "CONTACT_NAME";
    private static final String COLUMN_NUMBER = "NUMBER";

    private static final String TABLE_ADDRESS = "CONTACT_ADDRESS";
    private static final String COLUMN_UID = "UID";
    private static final String COLUMN_ADDRESS_NAME = "ADDRESS_NAME";
    private static final String COLUMN_PHONE_NUMBER = "PHONE_NUMBER";
    private static final String COLUMN_LATITUDE = "LATITUDE";
    private static final String COLUMN_LONGITUDE = "LONGITUDE";
    private static final String COLUMN_TEXT_ADDRESS = "TEXT_ADDRESS";
    private static final String COLUMN_AUDIO_ADDRESS_LOCATION = "AUDIO_ADDRESS_LOCATION";
    private static final String COLUMN_IS_AUDIO_ADDRESS = "IS_AUDIO_ADDRESS";
    private static final String COLUMN_AUDIO_FILENAME = "AUDIO_FILENAME";
    private static final String COLUMN_IS_PUBLIC = "IS_PUBLIC";

    private static final String TABLE_UPDATED = "UPDATED";
    private static final String COLUMN_UPDATED = "UPDATED_ONCE";


    public ContactsDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //Create Contacst Table
        String query = "CREATE TABLE " + TABLE_CONTACTS + "(" +
                        COLUMN_CONTACT_NAME + " TEXT, " +
                        COLUMN_NUMBER + " TEXT );";
        sqLiteDatabase.execSQL(query);

        query = "CREATE TABLE " + TABLE_ADDRESS + " (" +
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

        query = "CREATE TABLE " + TABLE_UPDATED + " (" +
                COLUMN_UPDATED + " SMALLINT " +
                ");";
        sqLiteDatabase.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ADDRESS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_UPDATED);
        onCreate(sqLiteDatabase);
    }

    public void Upgrade(Activity activity){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        String query = "CREATE TABLE " + TABLE_CONTACTS + "(" +
                COLUMN_CONTACT_NAME + " TEXT, " +
                COLUMN_NUMBER + " TEXT );";
        sqLiteDatabase.execSQL(query);
        Cursor phones = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, "DISPLAY_NAME ASC");
        while (phones.moveToNext())
        {
            String tempPhoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("\\s+", "");
            String PhoneNumber = tempPhoneNumber;
            if(PhoneNumber.contains("+91-"))
                PhoneNumber = PhoneNumber.replace("+91-","");
            if(PhoneNumber.contains("+91"))
                PhoneNumber = PhoneNumber.replace("+91","");
            if(PhoneNumber.startsWith("0"))
                PhoneNumber = PhoneNumber.substring(1);
            if(HasAddress(PhoneNumber,sqLiteDatabase)) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_CONTACT_NAME, phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                contentValues.put(COLUMN_NUMBER, tempPhoneNumber);
                sqLiteDatabase.insert(TABLE_CONTACTS, null, contentValues);
            }
        }
        phones.moveToFirst();
        while (phones.moveToNext())
        {
            String tempPhoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("\\s+", "");
            String PhoneNumber = tempPhoneNumber;
            if(PhoneNumber.contains("+91-"))
                PhoneNumber = PhoneNumber.replace("+91-","");
            if(PhoneNumber.contains("+91"))
                PhoneNumber = PhoneNumber.replace("+91","");
            if(PhoneNumber.startsWith("0"))
                PhoneNumber = PhoneNumber.substring(1);
            if(!HasAddress(PhoneNumber,sqLiteDatabase)) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_CONTACT_NAME, phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                contentValues.put(COLUMN_NUMBER, tempPhoneNumber);
                sqLiteDatabase.insert(TABLE_CONTACTS, null, contentValues);
            }
        }
        phones.close();
        sqLiteDatabase.close();
    }

    public ArrayList<Contacts> GetAllContacts(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_CONTACTS + ";";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<Contacts> AllContacts = new ArrayList<>();
        while (!cursor.isAfterLast()){
            Contacts rowContact = new Contacts();
            rowContact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_NAME)));
            String tempPhoneNumber = cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER));
            if(tempPhoneNumber.contains("+91-"))
                tempPhoneNumber = tempPhoneNumber.replace("+91-","");
            if(tempPhoneNumber.contains("+91"))
                tempPhoneNumber = tempPhoneNumber.replace("+91","");
            if(tempPhoneNumber.startsWith("0"))
                tempPhoneNumber = tempPhoneNumber.substring(1);
            rowContact.setPhoneNumber(tempPhoneNumber);
            rowContact.setHasAddress(HasAddress(tempPhoneNumber,sqLiteDatabase));
            AllContacts.add(rowContact);
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return AllContacts;
    }

    public ArrayList<Contacts> GetSuggestions(String argument){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query =  "SELECT * FROM " + TABLE_CONTACTS +
                        " WHERE " + COLUMN_CONTACT_NAME + " LIKE \"%" + argument + "%\"" +
                        " OR " + COLUMN_NUMBER + " LIKE \"%" + argument + "%\" " +
                        " ORDER BY " + COLUMN_CONTACT_NAME + " ASC LIMIT 15;";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        ArrayList<Contacts> AllContacts = new ArrayList<>();
        while (!cursor.isAfterLast()){
            Contacts rowContact = new Contacts();
            rowContact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_NAME)));
            rowContact.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER)));
            AllContacts.add(rowContact);
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return AllContacts;
    }

    public String GetContactName(String PhoneNumber){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query =  "SELECT " + COLUMN_CONTACT_NAME + " FROM " + TABLE_CONTACTS +
                        " WHERE " + COLUMN_NUMBER + " = \"" + PhoneNumber + "\";";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();
        if(!cursor.isAfterLast()){
            return cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_NAME));
        }
        return null;
    }

    public ContentValues GetContactAddressAudio(String UID, SQLiteDatabase sqLiteDatabase){
        String query =  "SELECT " + COLUMN_AUDIO_FILENAME + ", " + COLUMN_IS_AUDIO_ADDRESS + "," + COLUMN_AUDIO_ADDRESS_LOCATION +
                        " FROM " + TABLE_ADDRESS + " WHERE " + COLUMN_UID + " = \"" + UID + "\";";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();
        if(!cursor.isAfterLast()){
            ContentValues contentValues = new ContentValues();
            if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_AUDIO_ADDRESS)) == 1)
                contentValues.put("IsAudioAddress",true);
            else
                contentValues.put("IsAudioAddress",false);
            contentValues.put("AudioAddressLocation", cursor.getString(cursor.getColumnIndex(COLUMN_AUDIO_ADDRESS_LOCATION)));
            contentValues.put("AudioFilename", cursor.getString(cursor.getColumnIndex(COLUMN_AUDIO_FILENAME)));
            cursor.close();
            return contentValues;
        }else {
            cursor.close();
            return null;
        }
    }

    public void UpdateContactAddress(Address address ,SQLiteDatabase sqLiteDatabase) {
        String query = "SELECT * FROM " + TABLE_ADDRESS + " WHERE " + COLUMN_UID + " = \"" + address.getUID() + "\";";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            query = "DELETE FROM " + TABLE_ADDRESS +
                    " WHERE " + COLUMN_UID + " = \"" + address.getUID() + "\";";
            sqLiteDatabase.execSQL(query);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_UID, address.getUID());
        contentValues.put(COLUMN_ADDRESS_NAME, address.getAddressName());
        contentValues.put(COLUMN_PHONE_NUMBER, address.getPhoneNumber());
        contentValues.put(COLUMN_LATITUDE, address.getVisualAddressLatitude());
        contentValues.put(COLUMN_LONGITUDE, address.getVisualAddressLongitude());
        contentValues.put(COLUMN_TEXT_ADDRESS, address.getTextualAddress());
        contentValues.put(COLUMN_AUDIO_ADDRESS_LOCATION, address.getAudioAddress());
        contentValues.put(COLUMN_IS_AUDIO_ADDRESS, (address.isSetAudioAddress() ? 1 : 0));
        contentValues.put(COLUMN_AUDIO_FILENAME, address.getAudioFileName());
        contentValues.put(COLUMN_IS_PUBLIC, 1);
        sqLiteDatabase.insert(TABLE_ADDRESS, null, contentValues);
        cursor.close();
    }

    public boolean HasAddress(String PhoneNumber , SQLiteDatabase sqLiteDatabase){
        String query = "SELECT * FROM " + TABLE_ADDRESS + " WHERE " + COLUMN_PHONE_NUMBER + " = \"" + PhoneNumber + "\";";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        if(cursor.getCount() > 0){
            cursor.close();
            return true;
        }else{
            cursor.close();
            return false;
        }
    }

    public ArrayList<JSONObject> GetAddressOfPhoneNumber(String PhoneNumber){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query =  "SELECT " + COLUMN_ADDRESS_NAME + ", " + COLUMN_PHONE_NUMBER + ", " + COLUMN_UID + " FROM " + TABLE_ADDRESS +
                        " WHERE " + COLUMN_PHONE_NUMBER + " = \"" + PhoneNumber + "\";";
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

    public Address getAddressOfName(String AddressName, String UID){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_ADDRESS +
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

    public SQLiteDatabase GetDatabase(){
        return getWritableDatabase();
    }

    public void CloseDatabase(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.close();
    }

    public boolean CheckUpdatedOnce(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_UPDATED + ";";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        if(cursor.getCount() > 0){
            cursor.close();
            sqLiteDatabase.close();
            return true;
        }else {
            cursor.close();
            sqLiteDatabase.close();
            return false;
        }
    }

    public void UpdatedOnce(SQLiteDatabase sqLiteDatabase){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_UPDATED,1);
        sqLiteDatabase.insert(TABLE_UPDATED,null,contentValues);
    }
}
