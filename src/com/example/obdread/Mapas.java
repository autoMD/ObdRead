package com.example.obdread;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.obdread.SqlDb.AuxDB;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class Mapas extends android.support.v4.app.FragmentActivity { 
	
	
	// Referencia a GoogleMap 
	private transient GoogleMap mMap;
	//Para acceder a la base de datos
	private DbAdapter dataSource;
	//Para representar la ruta
		
		 private Number[] lat;
		 private Number[] lon;
		 private Number[] efi;
		/* int[] load={20, 20,20,30,30,20,20,20,20,20,30,30,20,20,20};
		 String[] latis={"37.237252","37.237209", "37.237183", "37.237171","37.237162","37.237145","37.237166","37.237102","37.237021","37.236842","37.236718","37.236577","37.236415","37.236423","37.236466"};
		 String[] longis={"-3.572655","-3.572617","-3.57251","-3.572355","-3.57207","-3.571727","-3.571534","-3.571362","-3.571287","-3.571089","-3.57096","-3.570869","-3.570772","-3.570633","-3.570488"};
		 int [] rpm={980,1500,2000,2430,1420,2700,1550,1700,1680,1670,2500,2000,1500,1700,1650};
		 int [] kph={0,10,13,15,25,35,50,50,50,50,39,30,45,50,50};*/
	
	/**
	 * Constructor de la clase
	 */
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.layout_mapas);
			
			
			//Abrimos la base de datos
	        dataSource = new DbAdapter(this);
	        dataSource.open();
	      //Obtenemos las columnas
		  // folloneo(load, latis, longis,rpm,kph);
		       lat=dataSource.getColum(AuxDB.COLUMNA_LAT);
		       lon=dataSource.getColum(AuxDB.COLUMNA_LONG);
		       efi=dataSource.getColum(AuxDB.COLUMNA_LOAD);
		       
		       cargaGoogleMap();  
		        //Mostramos la ruta
		       //mostrarLineas(lat,lon,efi);
		       mostrarRuta(lat,lon,efi);
		}
		
		/**
		 * @see android.support.v4.app.FragmentActivity#onResume()
		 */
		@Override
		protected void onResume() {
			super.onResume();
			cargaGoogleMap();
		}
		
		/*public void folloneo(int[] carga, String[] lati, String[] longi, int[] revo, int[] velo){
			//Log.e("Llamos a folloneo", "hila");
			 for(int k=0;k<lati.length;k++){
		    	  dataSource.crearRegistro(revo[k], velo[k], (int)5, (int)5, 5, (int)carga[k], (double)5,(int) 5, lati[k].toString(), longi[k].toString());
		      }
			
		}*/
		
	



/*
 * El método cargaGoogleMap realiza la carga de la referencia a GoogleMap.
 */
private void cargaGoogleMap() {
	if (mMap == null) {
		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		
		if (mMap != null) {
			//Definimos el tipo de mapa
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			//Colocamos marcadores de inicio y fin.
			setMarker((new LatLng(lat[(lat.length-1)].doubleValue(),lon[(lon.length-1)].doubleValue())),false);
			setMarker((new LatLng(lat[0].doubleValue(),lon[0].doubleValue())),true);
			//Centramos la cámara en el inicio de la ruta
			mMap.setMyLocationEnabled(true);
			CameraUpdate camUpd1 =CameraUpdateFactory.newLatLngZoom((new LatLng(lat[0].doubleValue(),lon[0].doubleValue())),15);
				 
			mMap.moveCamera(camUpd1);
		}
	}
}
/**
 * Método para colocar marcadores en el mapa
 * @param position
 * @param inicio
 */
private void setMarker(LatLng position, boolean inicio) {
// Agregamos marcadores para indicar sitios de interéses.
	if(inicio){
		 mMap.addMarker(new MarkerOptions()
		.position(position)
		.title("Inicio")//Agrega un titulo al marcador
		.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); //Color del marcador
	}	
	else{
		 mMap.addMarker(new MarkerOptions()
		.position(position)
		.title("Fin")//Agrega un titulo al marcador
		.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))); //Color del mar
	}
}

/**
 * Método para dibujar una Polilyne a partir de los indices del array de coordenadas
 * @param inicio
 * @param fin
 * @param color
 */
private void dibujaLinea(int inicio, int fin, boolean color){
	PolylineOptions linea=new PolylineOptions();
	for(int i=inicio;i<=fin;i++){
		linea.add(new LatLng(lat[i].doubleValue(), lon[i].doubleValue()));
	}
	
	if(color){
		linea.color(Color.GREEN);
	}
	else{
		linea.color(Color.RED);
	}
	
	mMap.addPolyline(linea);
	
}
/**
 * Método para dibujar las Polylines de diferente color sobre el mapa
 * 
 */
	
private void mostrarRuta(Number[] latitud, Number[] longitud, Number[] load){
	//Primero creamos un bucle para recorrer el array 
	int inicio=0;
	int fin=0;
	for(int i=0;i<(latitud.length-1);i++){
		 if(latitud[i].toString().equals("sin_datos") || longitud[i].toString().equals("sin_datos")){
			inicio++;
		 }else{
			 if((load[i].doubleValue()<=50 && load[i+1].doubleValue()<=50) || (load[i].doubleValue()>50 && load[i+1].doubleValue()>50)){
				 fin++;
				 //En caso de ser el ultimo tramo dibujamos
				 if((i+2)==(latitud.length)){
					 if(load[i].doubleValue()<=50){
						 dibujaLinea(inicio, fin, true);
					 }
					 else{
						
						 dibujaLinea(inicio, fin,false);
					 }
				 }
			 
			
			 }
			 //Cuando cambiamos de tramo de eficiente a no eficiente dibujamos
			 if((load[i].doubleValue()<=50 && load[i+1].doubleValue()>50) || (load[i].doubleValue()>50 && load[i+1].doubleValue()<=50)){
				 fin++;
				 if(load[i].doubleValue()<=50){
					 dibujaLinea(inicio, fin, true);
				 }
				 else{
					 dibujaLinea(inicio,fin,false);
				 }
				 inicio=fin;
			
			
			 }
		 }
		
		
		
		
		
		
	}
}

/**
 * Método para agregar el menú desplegable.
 */

@Override
public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.map_menu, menu);
    return true;
}

/**
 * Método donde se definen las opciones del menú
 */
@Override public boolean onOptionsItemSelected(MenuItem item) {
  	 //Intent serverIntent = null;
            switch (item.getItemId()) {
            		case R.id.Inicio:
            			goToInicio();
            			break;
            			
            		case R.id.Fin:
            			goToFin();
            			break;
            			
            		case R.id.EficienciaMedia:
            			calculoEficienciaMedia();
            			break;
            			          			

           
            		}
            
          	  

            return true; /** true -> consumimos el item, no se propaga*/

   }

/**
 * Método que centra la cámara del mapa en el inicio de la Ruta
 */

public void goToInicio(){
	
			CameraUpdate camUpd1 =CameraUpdateFactory.newLatLngZoom((new LatLng(lat[0].doubleValue(),lon[0].doubleValue())),18);
			 
			mMap.moveCamera(camUpd1);
			
	
	
}

/**
 * Método que centra la cámara del mapa en el fin de la Ruta
 *
 */
	public void goToFin(){
	
			CameraUpdate camUpd1 =CameraUpdateFactory.newLatLngZoom((new LatLng(lat[(lat.length-1)].doubleValue(),lon[(lon.length-1)].doubleValue())),18);
			 
			mMap.moveCamera(camUpd1);
			
	
		
	}
	
	/**
	 * Método que calcula la eficiencia media en el trayecto
	 */
public void calculoEficienciaMedia(){
	int calculo=0;
	for(int i=0;i<efi.length;i++){
		calculo=(100-efi[i].intValue())+calculo;
	}
	calculo=(calculo/(efi.length));
	Toast.makeText(getApplicationContext(), "Eficencia media de  "+calculo+" %", Toast.LENGTH_SHORT).show();
}

}
