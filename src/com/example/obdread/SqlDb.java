package com.example.obdread;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlDb extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "PARAMETROS.db";
    private static final int DATABASE_VERSION = 1;
     
    public static class AuxDB{
        public static String TABLA_NOMBRE = "parametros";
        public static String COLUMNA_ID = "_id";
        public static String COLUMNA_VELOCIDAD = "velocidad";
        public static String COLUMNA_RPM = "rpm";
        public static String COLUMNA_TEMP = "temperatura";
        public static String COLUMNA_FUEL= "fuel";
        public static String COLUMNA_FUELRATE= "fuelrate";
        public static String COLUMNA_LOAD= "load";
        public static String COLUMNA_ACC= "acc";
        public static String COLUMNA_THROTLE= "throtle";
        public static String COLUMNA_LAT="latitud";
        public static String COLUMNA_LONG="longitud";
    }
     
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + AuxDB.TABLA_NOMBRE + " (" 
    		+ AuxDB.COLUMNA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
            + AuxDB.COLUMNA_VELOCIDAD + " INTEGER, " 
            + AuxDB.COLUMNA_RPM+ " INTEGER, "
            +AuxDB.COLUMNA_TEMP +" INTEGER, "
            +AuxDB.COLUMNA_FUEL +" INTEGER, "
            +AuxDB.COLUMNA_FUELRATE +" INTEGER, "
            +AuxDB.COLUMNA_LOAD +" INTEGER, "
            +AuxDB.COLUMNA_ACC +" TEXT, "
            +AuxDB.COLUMNA_LAT +" TEXT, "
            +AuxDB.COLUMNA_LONG +" TEXT, "
            + AuxDB.COLUMNA_THROTLE+ " INTEGER );";
     
    public SqlDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(DATABASE_CREATE);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("delete table if exists " + AuxDB.TABLA_NOMBRE);
        onCreate(db);
    }
 
}
