package com.example.obdread;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.example.obdread.SqlDb.AuxDB;

public class MuestraGraficos extends Activity {
	private XYPlot mySimpleXYPlot;
	private DbAdapter dataSource;
	 
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graficos);
        //Abrimos la base de datos
        dataSource = new DbAdapter(this);
        dataSource.open();
        
        //Cargamos los datos de la base de datos
        Number[] series1Numbers=dataSource.getColum(AuxDB.COLUMNA_RPM);
        
        
        
        // Inicializamos el objeto XYPlot búscandolo desde el layout:
        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        mySimpleXYPlot.setTitle("Revoluciones por minuto");
        mySimpleXYPlot.setRangeLabel("Revoluciones por minuto");
               
        // Añadimos Línea Número UNO:
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),  // Array de datos
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Sólo valores verticales
                "Series1"); // Nombre de la primera serie
 
       
 
        // Modificamos los colores de la primera serie
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(0, 200, 0),                   // Color de la línea
                Color.rgb(0, 100, 0),                   // Color del punto
                Color.rgb(150, 190, 150));              // Relleno
 
        // Una vez definida la serie (datos y estilo), la añadimos al panel
        mySimpleXYPlot.addSeries(series1, series1Format);
        
        
    }
    
    public void MostrarColumnas(String columna){
    	
    	//Cargamos los datos de la base de datos
        Number[] series2Numbers=dataSource.getColum(columna);
     // Inicializamos el objeto XYPlot búscandolo desde el layout:
        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        mySimpleXYPlot.clear();
        //Inicializamos el formato de la gráfica
        LineAndPointFormatter series2Format = new LineAndPointFormatter(
        		Color.rgb(0, 0, 200),
        		Color.rgb(0, 0, 100), 
        		Color.rgb(150, 150, 190));;
        
        
        if(columna.equals(AuxDB.COLUMNA_RPM)){
        	mySimpleXYPlot.setTitle("Revoluciones por minuto");
        	mySimpleXYPlot.setRangeLabel("Revoluciones por minuto");
        	// Modificamos los colores de la serie
            series2Format = new LineAndPointFormatter(
            		 Color.rgb(0, 200, 0),                   // Color de la línea
                     Color.rgb(0, 100, 0),                   // Color del punto
                     Color.rgb(150, 190, 150));              // Relleno
        	        	
        }
        else if(columna.equals(AuxDB.COLUMNA_ACC)){
        	
        	mySimpleXYPlot.setTitle("Aceleración");
        	mySimpleXYPlot.setRangeLabel("m/s^2");
        	series2Format = new LineAndPointFormatter(
            		Color.rgb(0, 0, 200),
            		Color.rgb(0, 0, 100), 
            		Color.rgb(150, 150, 190));
        }
        else if(columna.equals(AuxDB.COLUMNA_FUEL)){
        	mySimpleXYPlot.setTitle("Fuel level");
        	mySimpleXYPlot.setRangeLabel("Nivel de fuel (%)");
        	series2Format = new LineAndPointFormatter(
            		Color.rgb(200, 0, 200),
            		Color.rgb(100, 0, 100), 
            		Color.rgb(190, 150, 150));
        }
        else if(columna.equals(AuxDB.COLUMNA_FUELRATE)){
        	mySimpleXYPlot.setTitle("Fuel rate (L/h)");
        	mySimpleXYPlot.setRangeLabel("Litros/hora cada 100 Km");
        	series2Format = new LineAndPointFormatter(
           		 Color.rgb(200, 200, 0),                   // Color de la línea
                 Color.rgb(100, 100, 0),                   // Color del punto
                 Color.rgb(190, 150, 150));              // Relleno
        }
        else if(columna.equals(AuxDB.COLUMNA_LOAD)){
        	mySimpleXYPlot.setTitle("Engine Load (%)");
        	mySimpleXYPlot.setRangeLabel("Carga del motor (%)");
        	series2Format = new LineAndPointFormatter(
            		Color.rgb(0, 0, 200),
            		Color.rgb(0, 0, 100), 
            		Color.rgb(150, 150, 190));
        }
        else if(columna.equals(AuxDB.COLUMNA_TEMP)){
        	mySimpleXYPlot.setTitle("Temperatura (ºC)");
        	mySimpleXYPlot.setRangeLabel("Temperatura(ºC)");
        	series2Format = new LineAndPointFormatter(
            		Color.rgb(200, 0, 200),
            		Color.rgb(100, 0, 100), 
            		Color.rgb(190, 150, 150));
        }
        else if(columna.equals(AuxDB.COLUMNA_THROTLE)){
        	mySimpleXYPlot.setTitle("Acelerador (%)");
        	mySimpleXYPlot.setRangeLabel("Nivel del pedal(%)");
        	series2Format = new LineAndPointFormatter(
              	    Color.rgb(0, 200, 200),                   // Color de la línea
                    Color.rgb(0, 100, 100),                   // Color del punto
                    Color.rgb(150, 150, 190));              // Relleno
        }
        else if(columna.equals(AuxDB.COLUMNA_VELOCIDAD)){
        	mySimpleXYPlot.setTitle("Velocidad(Kph)");
        	mySimpleXYPlot.setRangeLabel("Km/h");
        	series2Format = new LineAndPointFormatter(
            		Color.rgb(0, 0, 200),
            		Color.rgb(0, 0, 100), 
            		Color.rgb(150, 150, 190));
        }
     // Añadimos Línea Número UNO:
        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(series2Numbers),  // Array de datos
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Sólo valores verticales
                "Series1"); // Nombre de la primera serie
 
       
 
     
        // Una vez definida la serie (datos y estilo), la añadimos al panel
        mySimpleXYPlot.addSeries(series2, series2Format);
        mySimpleXYPlot.redraw();
    	
    }

    
    
  
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_graf, menu);
        return true;
    }
    
    @Override public boolean onOptionsItemSelected(MenuItem item) {
   	 //Intent serverIntent = null;
             switch (item.getItemId()) {
             		case R.id.acercaDe:
             			lanzarAcercaDe(null);
             			break;
             			
             		case R.id.MostrarAcc:
             			MostrarColumnas(AuxDB.COLUMNA_ACC);
             			break;
             			
             		case R.id.MostrarFuel:
             			MostrarColumnas(AuxDB.COLUMNA_FUEL);
             			break;
             			
             		case R.id.MostrarFuelRate:
             			MostrarColumnas(AuxDB.COLUMNA_FUELRATE);
             			break;
             			
             		case R.id.MostrarKPH:
             			MostrarColumnas(AuxDB.COLUMNA_VELOCIDAD);
             			break;
             			
             		case R.id.MostrarLoad:
             			MostrarColumnas(AuxDB.COLUMNA_LOAD);
             			break;
             			
             		case R.id.MostrarRPM:
             			MostrarColumnas(AuxDB.COLUMNA_RPM);
             			break;
             			
             		case R.id.MostrarTemp:
             			MostrarColumnas(AuxDB.COLUMNA_TEMP);
             			break;
             			
             		case R.id.MostrarThrotle:
             			MostrarColumnas(AuxDB.COLUMNA_THROTLE);
             			break;
             			

            
             		}
             
           	  

             return true; /** true -> consumimos el item, no se propaga*/

    }

    
    
    protected void onPause() {
	    // TODO Auto-generated method stub
	    dataSource.close();
	    super.onPause();
	}
    
    protected void onResume() {
        // TODO Auto-generated method stub
        dataSource.open();
        super.onResume();
    }
    
    public void lanzarAcercaDe(View view) {

		 Intent i = new Intent(this, AcercaDe.class);

		startActivity(i);

		 }
	

}
