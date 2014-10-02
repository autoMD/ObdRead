package com.example.obdread;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
@SuppressLint("HandlerLeak")
public class ModoTutor extends Activity implements SensorEventListener {
	
//Mensajes tipo enviados de BluethoothManagement
	
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
//Datos miembro para conexión bluetooth
	private static String mNombreDispositivoConectado=null;
	private static ArrayAdapter<String> mConversacionArrayAdapter;
	private static StringBuffer mBufferSalida;
	private static BluetoothAdapter mBluetoothAdapter=null;
	private static BluetoothManagement mServicioChat=null;
	
// Nombres recibidos de bluetooth management
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

	
//Codigo interno
	private static final int REQUEST_CONNECT_DEVICE_SECURE=1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE=2;
	private static final int REQUEST_ENABLE_BT=3;
	
// Elementos para interfaz
    private static ListView mConversacionView;	
    
  //Para llevar la cuenta del PID que toca
    static int message_number = 1;
    static boolean muestreo=false;
    static int numeroErrores=0;
    static boolean milOn=false;
    static String errorDBT="";    
    static boolean diesel=true;
    
  //Para la base de datos
  	private static  DbAdapter dataSource;
  	private Timer t;    

//Datos miembro
	
    GforceView view;
       
  //Para acelerometro
     private double yCurrent=0, xCurrent=0,zCurrent=0;
     private double xLast=0, yLast=0, zLast=0;
  //  private SensorManager mSensorManager;
     private float mAccel=0; // acceleration apart from gravity
     private float mAccelCurrent=0; // current acceleration including gravity
     private float mAccelLast=0; // last acceleration including gravity
     
   //Para almacenar en la base de datos.
	    static int RPM_final=0;
	    static int KPH_final=0;
	    static double acel_final=0;  
	    static int load_final=0;
     
     
    //Para la interfaz
     private static TextView Gear;
 	 private static TextView eficiencia;
	 private static TextView velocidad; 
	 private static TextView rpm; 
	 private static TextView aceleracion ; 
	 private static ProgressBar barra;
	
	//Para el almacenamiento de rutas
	 private static String latitud="";
	 private static String longitud="";
 	private static LocationManager locationManager;
 	private static LocationListener locationListener;
 	//Para el sonido;
 	private Context pContext;
	private SoundPool sndPool;
	private float rate = 1.0f;
	private float leftVolume = 1.0f;
	private float rightVolume = 1.0f;
	private int sonido=0;

     
   //Datos necesarios para obtener el valor de los datos recibidos	
 	
    int value = 0;
 	int PID = 0;
 	int modo=0;
 	//Para evitar apagado de pantalla
	 protected PowerManager.WakeLock wakelock;

	/**
	 * Método onCreate
	 */
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//Seleccionamos el modo
		BluetoothManagement.SELEC_MODO=true;
		//Quitamos barra de notificaciones
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	        // Indicamos el layout
			setContentView(R.layout.layout_tutor);
			
			//evitar que la pantalla se apague
	        final PowerManager pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
	        this.wakelock=pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "etiqueta");
	        wakelock.acquire();
	        
	      //Inicializamos los elementos de la interfaz
			view=(GforceView)findViewById(R.id.surfaceView1);	
			Gear = (TextView) findViewById(R.id.gearView); 
			eficiencia=(TextView) findViewById(R.id.eficienciaView);
			velocidad=(TextView) findViewById(R.id.velocidadView);
			rpm=(TextView) findViewById(R.id.RPMView);
			aceleracion=(TextView) findViewById(R.id.acelView);
			barra=(ProgressBar) findViewById(R.id.progressBar2);
			
			
			
			// Obtenemos el adapter local para conexión bluetooth
	        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        
	     // Si el adapter devuelve null no disponemos de bluetooth
	        if (mBluetoothAdapter == null) {
	            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
	            finish();
	            return;
	        }
	        
	        //Abrimos la base de datos
	        dataSource = new DbAdapter(this);
	        dataSource.open();
	        
	      //Cargamos auido
	        sndPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 100);
			pContext=this.getApplicationContext();
			sonido=load(R.raw.sonido_cambio);
	      //Inicializamos acceso a GPS
		      //Obtenemos una referencia al LocationManager
		        locationManager =(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		     
		        //Obtenemos la última posición conocida
		        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		        //Nos registramos para recibir actualizaciones de la posicion
		        locationListener = new LocationListener(){
		        	public void onLocationChanged(Location location){
		        		guardarLonLat(location);
		        	}
		        	//Si el GPS del movil se desactiva
		        	public void onProviderDisabled(String provider){
		        		
		        	}
		        	//Si el Gps del móvil se activa
		        	public void onProviderEnabled(String provider){
		        		
		        	}
		        	public void onStatusChanged(String provider, int status, Bundle extras){
		        		
		        	}	        	
		        };
		        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	        
	      //Creamos el temporizador para el muestreo
	         t = new Timer();
	        	//Set the schedule function and rate
	        	t.scheduleAtFixedRate(new TimerTask() {

	          @Override
	          public void run() {
	              if(muestreo==true){
	            	  getData(message_number);
	            	  message_number++;
	            	  Log.e("Voy leyendo", Integer.toString(message_number));
	            	  if(message_number==4){
	            		  message_number = 1;
		            		acel_final=(double)mAccel;
		            		Log.e("Guardo", Integer.toString((int)acel_final));
		              		dataSource.crearRegistro2(RPM_final, KPH_final,load_final, acel_final, latitud, longitud);
		            		}
	              }
	          }
	               
	      },
	      //Set how long before to start calling the TimerTask (in milliseconds)
	      0,
	      //Set the amount of time between each execution (in milliseconds)
	      200);
	        	
	        
	        
			        
	        
	     //Para el acelerometro
	       // mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);	
	}
	
	
	
	/**
	 * Método para coger y guardar la latitud y la longitud de nuestra posición en una variable.
	 * 
	 */
	  
	  public void guardarLonLat(Location location){
		  if(location!=null){
			  latitud= String.valueOf(location.getLatitude());
			  longitud= String.valueOf(location.getLongitude());
		  }
		  else{
			  latitud="sin_datos";
			  longitud="sin_datos";
		  }
		  
	  }
	
	
	 /**
      * Método que necesitamos introducir para el manejo del acelerómetro.
      */
    @Override
    public void onAccuracyChanged(Sensor sensor,int accuracy){}
    


/**
  * Método para obtener datos del acelerómentro
  */
@Override
public void onSensorChanged(SensorEvent se){
	
	xCurrent = se.values[0];
    yCurrent = se.values[1];
    zCurrent = se.values[2];
    int varZ=(int)(zCurrent-zLast);
    int varY=(int)(yCurrent-yLast);
    int varX=(int)(xCurrent-xLast);
    
    //Para la representación
    zLast=zCurrent;
    yLast=yCurrent;
    xLast=xCurrent;
    view.setCoordenadaX(varZ*10);
    view.setCoordenadaY(varY*10);
    //double acel=Math.sqrt(varZ*varZ+varY*varY+varX*varX);
    
    //Valor exacto de la aceleración con filtro high-pass para la gravedad
     mAccelLast = mAccelCurrent;
     mAccelCurrent = (float)Math.abs(Math.sqrt((double) (varZ*varZ+varY*varY+varX*varX)));
     float delta = mAccelCurrent - mAccelLast;
     mAccel = mAccel * 0.9f + delta; // perform low-cut filter      
     aceleracion.setText(String.format("%.2f", mAccel));
	
}
	
	/**
	   * Método onStart()
	   */
	    @Override
	    public void onStart() {
	        super.onStart();
	      //Seleccionamos el modo
			BluetoothManagement.SELEC_MODO=true;
			//Comprobamos si el GPS está activado.
			//Si el GPS no está habilitado
			if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Intent settingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				this.startActivityForResult(settingsIntent, 0);
			  
			}
	        // Si el bluetooth no está activado, solicitamos encendido
	        if (!mBluetoothAdapter.isEnabled()) {
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	        // Si ya estuviese encendido, empezamos la comunicación
	        } else {
	            if (mServicioChat == null) setupChat();
	        }
	    }
	
	/*
	 * Método onResume
	 */
	 @Override
	    public synchronized void onResume() {
	        super.onResume();
	        if (mServicioChat != null) {
	            //Solo si el estado es STATE_NONE sabremos que no hemos empezado aun la conexión
	            if (mServicioChat.getState() ==BluetoothManagement.STATE_NONE) {
	              // Empezamos el servicio chat
	              mServicioChat.start();
	            }
	        }
	        dataSource.open();
	        //view.activar();

	        
	        SensorManager sm=(SensorManager) getSystemService(SENSOR_SERVICE);
	        List <Sensor> sensors=sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
	        if (sensors.size()>0){
	        	sm.registerListener(this,sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
	        }
	    }
	 
	 
	/**
	 * Método para cargar una pista de sonido 
	 * @param sound_id
	 * @return
	 */
	 
	 public int load(int sound_id){
			return sndPool.load(pContext, sound_id, 1);
		}
	 
/**
 * Método para reproducir una pista de sonido
 * @param sound_id
 */
		 
		public void play(int sound_id){
			sndPool.play(sound_id, leftVolume, rightVolume, 1, 0, rate);
		}
		
/**
 * Método para liberar memoria de audio
 */
		
		public void unloadAll(){
			sndPool.release();
		}
	 
	  /**
	     * Método setupChat
	     */
	    
	    private void setupChat() {
	       

	        // Initialize the array adapter for the conversation thread
	        mConversacionArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
	        mConversacionView = (ListView) findViewById(R.id.in2);
	        
	        mConversacionView.setAdapter(mConversacionArrayAdapter);

	     

	        // Initialize the BluetoothChatService to perform bluetooth connections
	        mServicioChat = new BluetoothManagement(this, mHandler);

	        // Initialize the buffer for outgoing messages
	        mBufferSalida = new StringBuffer(""); 
	        
	        
	 
	        
	    }
	    
	    
	    /**
	     *  Método para empezar la transimisón
	     */
	    public void startTransmission() {
	    	//sendMessage("AT Z" + '\r');
	    	//Eliminamos los datos existentes en la base de datos e inicializamos otra
	    	if(mNombreDispositivoConectado != null) {
	    		dataSource.borrarTabla();
		       
		    	sendMessage("01 00 1" + '\r'); 
		    	muestreo=true;
	    	}
	    	else{
	    		Toast.makeText(getApplicationContext(), "Adaptador OBD no conectado", Toast.LENGTH_SHORT).show();	
	    	}
	    	
	    }
	 
	    /**
	     *  Método que manda los PID que solicitamos a la ECU
	     */

		public void getData(int messagenumber) {
		
							
			switch(messagenumber) {
	    	
	        	case 1:
	        		sendMessage("01 0C 1" + '\r'); //get RPM
	        	   
	        		break;
	        		
	        	case 2:
	        		sendMessage("01 0D 1" + '\r'); //get MPH
	        		
	        		break;
	        		
	        	case 3:
	        		sendMessage("01 04 1" + '\r'); //get load
	        		
	        		break;
	        		        		
	        	default: 
	        		break; 		 
			}
	    }	 
	 
		/**
		 * 
		 * Método que borra los códigos de error que haya en el vehículo y apaga la MILF
		 * 
		 */
	    public void clearCodes() {
	    	
	    			    	
	        if(mNombreDispositivoConectado != null) {
	        		
	        	sendMessage("04" + '\r'); //send Clear Trouble Codes Command
	        	
	        	Toast.makeText(getApplicationContext(), "Codigos de error OBD borrados", Toast.LENGTH_SHORT).show();
	        
	        }
	        else {
	        	Toast.makeText(getApplicationContext(), "Adaptador OBD no conectado", Toast.LENGTH_SHORT).show();
	        }
	        
	    }
	    
	    /**
	     * Método para consultar los códigos de error
	     * 
	     */
	    
	    public void consultarDBT(){
	    	if(mNombreDispositivoConectado!=null){
	    		sendMessage("01 01"+'\r'); //Enviamos el comando para consultar los codigos de error almacenados
	    		muestreo=false;
	    	}
	    	
	    	else{
	    		Toast.makeText(getApplicationContext(), "Adaptador OBD no conectado", Toast.LENGTH_SHORT).show();
	    	}
	    	
	    	
	    }
	    
	    /**
	     * Método que envía el comando para obtener los códigos de error en concreto.
	     */
	    public void obtenerErrores(){
	    	sendMessage("03"+'\r');
	    }
	 
	 
	 
	 
	 /*
	  * Método onStop
	  */
	  @Override
	    public void onStop() {
	        super.onStop();
	        SensorManager sm= (SensorManager) getSystemService(SENSOR_SERVICE);
	        sm.unregisterListener(this);
	        view.cancelar();
	        //Cerramos la base de datos.
	        dataSource.close();
	        //Anulamos el temporizador
	        t.cancel();
	      //Anulamos el servicio GPS
	        locationManager.removeUpdates(locationListener);
	       //Liberamos sonido
	        unloadAll();
	        
	    }
	  /**
	   * Método onPause
	   */
	  @Override
	    public synchronized void onPause() {
	        super.onPause();
	        dataSource.close();
	        
	       // view.cancelar();
	       // t.cancel();
	   }
	  
	  /**
	     * (non-Javadoc)
	     * @see android.app.Activity#onDestroy()
	     */

	    @Override
	    public void onDestroy() {
	        super.onDestroy();
	        // Stop the Bluetooth chat services
	        if (mServicioChat != null) mServicioChat.stop();
	        //Paramos el hilo del timer
	        t.cancel();
	       //Anulamos el servicio GPS
	        locationManager.removeUpdates(locationListener);
	        //Bloqueo de pantalla
	        this.wakelock.release();
	      
	        //Cancelamos la view presonalizada
	        view.cancelar();
	        //Quitamos sonido
	        unloadAll();

	    }
	    
	    
	    /**
	     * Sends a message.
	     * @param message  A string of text to send.
	     */
	    private void sendMessage(String message) {
	        // Check that we're actually connected before trying anything
	        if (mServicioChat.getState() != BluetoothManagement.STATE_CONNECTED) {
	            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
	            return;
	        }

	        // Check that there's actually something to send
	        if (message.length() > 0) {
	            // Get the message bytes and tell the BluetoothChatService to write
	            byte[] send = message.getBytes();
	            mServicioChat.write(send);

	            // Reset out string buffer to zero and clear the edit text field
	            mBufferSalida.setLength(0);
	            //mOutEditText.setText(mOutStringBuffer);
	        }
	    }
	  
	  
	
	 
    
    /**
     * 
     * @param resId
     */

    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    /**
     * 
     * @param subTitle
     */
    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }
    
    /**
     * 
     * Método que completa los bits a la izquierda cuando pasamos un string a bit
     * @param binNum
     * @return Un string con todos sus digitos
     */
    public String completeDigits(String binNum) {
        for (int i = binNum.length(); i < 8; i++) {
            binNum = "0" + binNum;
        }
        return binNum;
    }
    
    /**
     * Método para obtener el valor decimal de un byte contanto solo los 7 bits menos significativos
     * @param numero
     * @return Valor entero de los 7 bits menos significativos
     */
    public int StringToDecimal(String numero){
    	int resultado=0;
    	for(int k=0;k<=6;k++){
    		resultado=resultado+((int)Math.pow(2,k))*(numero.charAt(7-k)-48);//48 es el valor ascii del '0'
    		
    		
    	}
    	
    	return resultado;
    	
    }
    
    /**
     * Método para transformar los códigos de error que nos devuelve la interfaz OBD
     * a los estándares normalizados.
     * @param cadena
     * @return código estandarizado
     */
    public String codigoError(String cadena){
    	String fin="";
    	if(cadena.charAt(0)=='0'||cadena.charAt(0)=='1'||cadena.charAt(0)=='2'||cadena.charAt(0)=='3'){
     		fin="P"+cadena;
    	}
    	if(cadena.charAt(0)=='4'||cadena.charAt(0)=='5'||cadena.charAt(0)=='6'||cadena.charAt(0)=='7'){
     		fin="C"+(cadena.charAt(0)-52)+cadena.charAt(1)+cadena.charAt(2)+cadena.charAt(3);
    	}
    	if(cadena.charAt(0)=='8'||cadena.charAt(0)=='9'){
     		fin="B"+(cadena.charAt(0)-56)+cadena.charAt(1)+cadena.charAt(2)+cadena.charAt(3);
    	}
    	if(cadena.charAt(0)=='A'||cadena.charAt(0)=='B'){
     		fin="B"+(cadena.charAt(0)-62)+cadena.charAt(1)+cadena.charAt(2)+cadena.charAt(3);
    	}
    	if(cadena.charAt(0)=='C'||cadena.charAt(0)=='D'||cadena.charAt(0)=='E'||cadena.charAt(0)=='F'){
     		fin="U"+(cadena.charAt(0)-67)+cadena.charAt(1)+cadena.charAt(2)+cadena.charAt(3);
    	} 	
    	
    	
    	
    	return fin;
    	
    	
    	
    }
    
    
    
    /*
     * Método que nos indica que marcha debemos engranar
     *
     */
    
    
    public void selectedGear(int RPM, int speed){
    	int gear=0;
    	int rojo=Color.RED;
    	int verde=Color.GREEN;
    	int negro=Color.BLACK;
    	
    	if(diesel==true){
    		//Velocidad 0. Marcha 0
    		if(speed<=2){
    			gear=0;
    			Gear.setText(Integer.toString(gear));
    			//Gear.setBackgroundColor(negro);
    		}
    		//Primera
    		if(speed<=13.2 && speed>5){
    			if(RPM>1800){
    				Gear.setBackgroundColor(rojo);
    				play(sonido);
    				gear=1;
    				Gear.setText(Integer.toString(gear));
    			}
    			else{
    				Gear.setBackgroundColor(verde);
    				gear=1;
    				Gear.setText(Integer.toString(gear));
    			}
    		
    		}
    		//Segunda
    		if(speed>13.2 && speed<=28.4){
    			if(RPM>1800){
    				Gear.setBackgroundColor(rojo);
    				gear=2;
    				play(sonido);
    				Gear.setText(Integer.toString(gear));
    			}
    			else{
    				Gear.setBackgroundColor(verde);
    				gear=2;
    				Gear.setText(Integer.toString(gear));
    			}
    		}
    		//Tercera
    		if(speed>32.4 && speed<=49){
    			if(RPM>1800){
    				Gear.setBackgroundColor(rojo);
    				gear=3;
    				play(sonido);
    				Gear.setText(Integer.toString(gear));
    		}
    		else{
    			Gear.setBackgroundColor(verde);
    			gear=3;
        		Gear.setText(Integer.toString(gear));
    			}
    		}
    		//Cuarta
    		if(speed>49 && speed<=67.4){
    			if(RPM>1800){
    				Gear.setBackgroundColor(rojo);
    				gear=4;
    				play(sonido);
    				Gear.setText(Integer.toString(gear));
    			}
    			else{
    				Gear.setBackgroundColor(verde);
    				gear=4;
    				Gear.setText(Integer.toString(gear));
    			}
    		}
    		//Quinta
    		if(speed>67.4){
    			if(RPM>1800){
    				Gear.setBackgroundColor(rojo);
    				gear=5;
    				play(sonido);
    				Gear.setText(Integer.toString(gear));
    			}
    			else{
    				Gear.setBackgroundColor(verde);
    				gear=5;
    				Gear.setText(Integer.toString(gear));
    			}
    		}
    	}
    	else{//Motor Gasolina, diferentes revoluciones
    		//Velocidad 0. Marcha 0
    		if(speed==0){
    			gear=0;
    			Gear.setText(Integer.toString(gear));
    		}
    		//Primera
    		if(speed<=17.2){
    			if(RPM>2300){
    				Gear.setBackgroundColor(rojo);
    				gear=1;
    				play(sonido);
    				Gear.setText(Integer.toString(gear));
    			}
    			else{
    				Gear.setBackgroundColor(verde);
    				gear=1;
    				Gear.setText(Integer.toString(gear));
    			}
    		
    		}
    		//Segunda
    		if(speed>17.2 && speed<=32.4){
    			if(RPM>2300){
    				Gear.setBackgroundColor(rojo);
    				gear=2;
    				play(sonido);
    				Gear.setText(Integer.toString(gear));
    			}
    			else{
    				Gear.setBackgroundColor(verde);
    				gear=2;
    				Gear.setText(Integer.toString(gear));
    			}
    		}
    		//Tercera
    		if(speed>32.4 && speed<=49){
    			if(RPM>2300){
    				Gear.setBackgroundColor(rojo);
    				gear=3;
    				play(sonido);
    				Gear.setText(Integer.toString(gear));
    		}
    		else{
    			Gear.setBackgroundColor(verde);
    			gear=3;
        		Gear.setText(Integer.toString(gear));
    			}
    		}
    		//Cuarta
    		if(speed>49 && speed<=67.4){
    			if(RPM>2300){
    				Gear.setBackgroundColor(rojo);
    				gear=4;
    				play(sonido);
    				Gear.setText(Integer.toString(gear));
    			}
    			else{
    				Gear.setBackgroundColor(verde);
    				gear=4;
    				Gear.setText(Integer.toString(gear));
    			}
    		}
    		//Quinta
    		if(speed>67.4){
    			if(RPM>2500){
    				Gear.setBackgroundColor(rojo);
    				gear=5;
    				Gear.setText(Integer.toString(gear));
    			}
    			else{
    				Gear.setBackgroundColor(verde);
    				gear=5;
    				Gear.setText(Integer.toString(gear));
    			}
    		}
    		
    	}

    }
    
    /**
     * Método que marca que trabajamos con motor diesel
     */
    public void selectDiesel(){
    	if(mNombreDispositivoConectado != null) {
    		Toast.makeText(getApplicationContext(), "No se puede cambiar este valor una vez conectado", Toast.LENGTH_SHORT).show();
    	}
    	else{
    		diesel=true;
    		Toast.makeText(getApplicationContext(), "Motor Diesel seleccionado", Toast.LENGTH_SHORT).show();
    	}
    }
    
    
    /**
     *Método que marca que trabajamos con motor gasolina
     * 
     */
    public void selectGasolina(){
    	if(mNombreDispositivoConectado != null) {
    		Toast.makeText(getApplicationContext(), "No se puede cambiar este valor una vez conectado", Toast.LENGTH_SHORT).show();
    	}
    	else{
    		diesel=false;
    		Toast.makeText(getApplicationContext(), "Motor Gasolina seleccionado", Toast.LENGTH_SHORT).show();
    	}
    }
    
    
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
    		

        @Override
        public void handleMessage(Message msg) {
        	
        
       
        	
        	//Casteamos el estado de la comunicación.
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothManagement.STATE_CONNECTED:
                    setStatus(getString(R.string.title_connected_to, mNombreDispositivoConectado));
                    mConversacionArrayAdapter.clear();
                    break;
                case BluetoothManagement.STATE_CONNECTING:
                    setStatus(R.string.title_connecting);
                    break;
                case BluetoothManagement.STATE_LISTEN:
                case BluetoothManagement.STATE_NONE:
                    setStatus(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // Costruimos un String para el nuevo buffer
                String writeMessage = new String(writeBuf);
              
                //mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // Construimos un String con los bytes validos               
                String dataRecieved = new String(readBuf, 0, msg.arg1);
                // ------- ADDED CODE FOR OBD -------- //      
	               
                Log.e("Del handler llega", dataRecieved);     //////////////////////////////////////////////////////////////////////////////////////
                
              
                
                	
                	//Quitamos los espacios de al principio y final del String
	        			dataRecieved = dataRecieved.trim();
	        		//Dividimos el string en pequeños string según cuando encontremos un espacio
	        			String[] bytes = dataRecieved.split(" ");
	        			//Log.e("Byte[0]", bytes[0]);
	        			//Log.e("Byte[1]", bytes[1]);
	        			//Log.e("Byte[2]", bytes[2]);
	        			
	        			if(bytes[0].equals("STOPPED")){
	        				
	        				//Toast.makeText(getApplicationContext(), "PARADO", Toast.LENGTH_SHORT).show();
	        			}
	        			
	        			else if(bytes[0].equals("AT")){
	        				
	        				//Toast.makeText(getApplicationContext(), "ELM reset", Toast.LENGTH_SHORT).show();
	        			}
	
	        			else if(((bytes[0].equals("41"))||(bytes[0].equals("SEARCHING..."))||(bytes[0].equals("NO"))) && (dataRecieved.length()>=7) ) {
	        				
	        				if(bytes[0].equals("41")){
	        					modo = Integer.parseInt(bytes[0].trim(), 16);
	        					PID = Integer.parseInt(bytes[1].trim(), 16); 
	        					Log.e("Entro en modo 41 porque molo", String.valueOf(dataRecieved.length()));}///////////////////////////////////////////////////
	        					//Log.e("El PID es", bytes[1]);
	        				
	        				else{
	        					PID=0;
	        					Log.e("No entro en 41", bytes[0]);///////////////////////////////////////////////////////////////////////////
	        				}
	        				
	        			
	        			
	        					switch(PID) {
	        					
	        					case 12: //PID(0C): RPM- Esperamos 2 bytes de respuesta
        							if(dataRecieved.length()==11){
        							String resultado=bytes[2]+bytes[3];
        							value=Integer.parseInt(resultado.trim(),16); //Convertimos el valor hexadecimal a decimal
        							
        							float RPM_value = (value)/4;
        							RPM_final=(int)RPM_value;	//Para la base de datos
        								                		
        							String displayRPM = String.valueOf(RPM_final);
        							rpm.setText(displayRPM);
        							barra.setProgress(RPM_final);
        							//Log.e("Bits juntos", resultado);
        							Log.e("El valor RPM es", displayRPM);
        							}
        							break;
	            		
	            		
        						case 13://PID(0D): MPH
        							value=Integer.parseInt(bytes[2].trim(),16); //Convertimos el valor hexadecimal a decimal        						
        							KPH_final=value;	//Para la base de tabla
        							String displayKPH = String.valueOf(KPH_final);
        							velocidad.setText(displayKPH+" Km/h");
        							selectedGear(RPM_final,KPH_final);
        							
        							Log.e("El valor KPH es", displayKPH);
        							break;
        							
        						case 4: //Load engine-PID(01 04)- 1 byte de retorno
        							value=Integer.parseInt(bytes[2].trim(),16); //Convertimos el valor hexadecimal a decimal
        							//value = value*100/255; //Formula for para el % de load enginne
        							load_final=value*100/255;		//Para almacenar en la base de datos
        							int load=100-(value*100/255);	
          							String displayLoad = String.valueOf(load);
        							eficiencia.setText(displayLoad+" %");
        							
        							
        							Log.e("El valor LOAD es", displayLoad);
        							break;
        						default: 
        							value=0;
        							PID=0;
        							break;
        						
        									        									        									        					
        			}
	        					
	        		}
	        			
	        			
	        			else if(bytes[0].equals("43")){ //Hemos recibido mensaje de errores.
	        				errorDBT=bytes[1]+bytes[2];
	        				
	        				errorDBT=codigoError(errorDBT);
	        				Toast.makeText(getApplicationContext(), "El código de error es: "+"\r"+errorDBT, Toast.LENGTH_SHORT).show();
	        		
	        		
	        	}
        	
            
           
                
	        			break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mNombreDispositivoConectado = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mNombreDispositivoConectado, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }

		
    };
    
    /**
     * 
     */
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occurred
              
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    /**
     * 
     * @param data
     * @param secure
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(ListaDispositivos.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mServicioChat.connect(device, secure);
    }
    
    /**
     * 
     */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tutor, menu);
        return true;
    }
    
    /**
     * 
     */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
   	 Intent serverIntent = null;
             switch (item.getItemId()) {
             case R.id.secure_connect_scan:
		            // Launch the DeviceListActivity to see devices and do scan
		            serverIntent = new Intent(this, ListaDispositivos.class);
		            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
		            return true;
		            
		      	  
             case R.id.SeleccionarDiesel:
            	 selectDiesel();
            	 break;
             case R.id.SeleccionarGasolina:
            	 selectGasolina();
            	 break;

             case R.id.DTB:
           	  clearCodes();
           	  break;
           
           	  
             case R.id.ConsultarDBT:
           	  consultarDBT();
           	  break;
           	  
             case R.id.ComenzarTrans:
            	 startTransmission();
            	 break;
             case R.id.acercaDe:

                 lanzarAcercaDe(null);

                 break;

             }
             
             
             return true; /** true -> consumimos el item, no se propaga*/

    }
    
    /**
     * 
     * @param view
     */
    public void lanzarAcercaDe(View view) {

		 Intent i = new Intent(this, AcercaDe.class);

		startActivity(i);

		 }
    
    
    
    
    
    
    
    
    }
    	
    	
    	
    	
    
    
    


    
    
    
    

    

