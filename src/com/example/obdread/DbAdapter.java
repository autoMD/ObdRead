package com.example.obdread;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.obdread.SqlDb.AuxDB;

public class DbAdapter  {
	
	private SQLiteDatabase db;
    private SqlDb dbHelper;
    
   /* private String[] columnas = { AuxDB.COLUMNA_ID,
    		AuxDB.COLUMNA_VELOCIDAD, AuxDB.COLUMNA_RPM, AuxDB.COLUMNA_TEMP, AuxDB.COLUMNA_FUEL, AuxDB.COLUMNA_FUELRATE, AuxDB.COLUMNA_LOAD, AuxDB.COLUMNA_ACC,
    		AuxDB.COLUMNA_LAT, AuxDB.COLUMNA_LONG, AuxDB.COLUMNA_THROTLE};*/
 
    public DbAdapter(Context context) {
        dbHelper = new SqlDb(context);
    }
 
    public void open() {
        db = dbHelper.getWritableDatabase();
    }
 
    public void close() {
        dbHelper.close();
    }
    
    public void borrarTabla(){
    	//db.execSQL("DROP TABLE IF EXISTS "+AuxDB.TABLA_NOMBRE);
    	db.delete(AuxDB.TABLA_NOMBRE, null, null);
    	open();
    }
 /*
  * Método para guardar registros en la base de datos
  */
    public void crearRegistro(int rpm, int vel, int temp, int fuel, int fuelrate, int load, double acc, int throtle, String latitud, String longitud) {
        ContentValues values = new ContentValues();
        values.put(AuxDB.COLUMNA_RPM, rpm);
        values.put(AuxDB.COLUMNA_VELOCIDAD, vel);
        values.put(AuxDB.COLUMNA_TEMP, temp);
        values.put(AuxDB.COLUMNA_FUEL, fuel);
        values.put(AuxDB.COLUMNA_FUELRATE, fuelrate);
        values.put(AuxDB.COLUMNA_LOAD, load);
        values.put(AuxDB.COLUMNA_ACC, acc);
        values.put(AuxDB.COLUMNA_LAT, latitud);
        values.put(AuxDB.COLUMNA_LONG, longitud);
        values.put(AuxDB.COLUMNA_THROTLE, throtle);
        db.insert(AuxDB.TABLA_NOMBRE, null, values);
    }
    
    
    /*
     * Mñetodo para guardar registros en la base de datos
     */
    public void crearRegistro2(int rpm, int vel,  int load, double acc, String latitud, String longitud){
    	ContentValues values=new ContentValues();
    	values.put(AuxDB.COLUMNA_RPM,rpm);
    	values.put(AuxDB.COLUMNA_VELOCIDAD, vel);
     	values.put(AuxDB.COLUMNA_LOAD,load);
    	values.put(AuxDB.COLUMNA_ACC, acc);
    	values.put(AuxDB.COLUMNA_LAT, latitud);
    	values.put(AuxDB.COLUMNA_LONG, longitud);
    	db.insert(AuxDB.TABLA_NOMBRE,null,values);
    	
    	
    }
 
    /*
     * Método para obtener una columna entera de la base de datos
     */
    public Number[] getColum(String columna){
        int i=0;
        Number array[];
       
        
        //Cursor cursor = db.rawQuery("SELECT "+columna+" FROM "+AuxDB.TABLA_NOMBRE, null);
       Cursor cursor=db.query(AuxDB.TABLA_NOMBRE, null, null, null, null, null, AuxDB.COLUMNA_ID,Integer.toString(2));
       
       if(cursor.getColumnIndex(columna)==(-1)){ //La columna no existe
    	array=new Number[1024];
    	
       }
       
       else{ //La columna existe
    	   //Seleccionamos todos los valores de la columna.
    	cursor = db.rawQuery("SELECT "+columna+" FROM "+AuxDB.TABLA_NOMBRE, null);   
       	array=new Number[cursor.getCount()];
              
       	cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
       
            double dato = cursor.getDouble(0);           //float dato=cursor.getFloat(0)
            array[i]=dato;
            cursor.moveToNext();
            i++;
        }
    }
 
        cursor.close();
        return array;
    }
    
    
 
   /* public void borrarNota(Nota nota) {
        long id = nota.getId();
        db.delete(TablaNotas.TABLA_NOTAS, TablaNotas.COLUMNA_ID + " = " + id,
                null);
    }*/
    
}