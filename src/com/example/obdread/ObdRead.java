package com.example.obdread;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;



/**
* @author Juan Cristóbal Peinado
*
*/
/** 
 * Primera Actitity de la aplicacion
 * @param <ImageView>
 */

public class ObdRead extends Activity {
	
	
	
/**
 * Constructor de la clase
 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_obd_read);
	}
	
	
	/**
	 * Método para iniciar la actividad ModoCrucero
	 */
	 public void lanzarModoCrucero(View view){

			Intent i = new Intent(this, ModoCrucero.class);

			startActivity(i);
		   }
	 
	 /**
		 * Método para iniciar la actividad Graficos
		 */
	 public void lanzarGraficos(View view){
		 Intent i = new Intent(this, MuestraGraficos.class);

			startActivity(i);
	 }
	 
	 /**
		 * Método para iniciar la actividad AcercaDe
		 */
	 public void lanzarAcercaDe(View view) {

		 Intent i = new Intent(this, AcercaDe.class);

		startActivity(i);

		 }
	 
	 /**
		 * Método para iniciar la actividad ModoTutor
		 */
	 
	 public void lanzarModoTutor(View view) {

		 Intent i = new Intent(this, ModoTutor.class);

		startActivity(i);

		 }
	 
	 
	 /**
		 * Método para iniciar la actividad Mapas
		 */
	 public void lanzarMapas(View view) {

		 Intent i = new Intent(this, Mapas.class);

		startActivity(i);

		 }
	
	
		
	}
	

