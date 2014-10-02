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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;



/**
* @author Juan Cristóbal Peinado
*
*/
/** 
 * Clase principal del programa
 * @param <ImageView>
 */
@SuppressLint("HandlerLeak")
public class ModoCrucero <Imageview> extends Activity implements SensorEventListener {
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
	  //  private EditText mOutEditText;
	  //  private Button mSendButton;
	    
	    //Para transmisión OBD	  		
	 
	    static float prev_intake = 0;
		static float prev_KPH = 0;
		static float prev_RPM = 0;		
		static float prev_fuel=0;
	
		//Para llevar la cuenta del PID que toca
	     static int message_number = 1;
	     static boolean muestreo=false;
	     static int numeroErrores=0;
	     static boolean milOn=false;
	     static String errorDBT="";
	    //Para almacenar en la base de datos.
	    static int RPM_final;
	    static int KPH_final;
	    static int Temp_final;
	    static int Fuel_final;
	    static int Fuelrate_final;
	    static int load_final;
	    static int throtle_final;
	    static double acc_final;
	    
	    //Para trabajar con el acelerómetro.
	   
	    private static float xLast=0,yLast=0,zLast=0;
	    private static  float xCurrent, yCurrent,zCurrent;
	    private static  float mAccelLast=0, mAccelCurrent=0, mAccel=0, delta=0;
	    private static float varX,varY,varZ;
		
		//Para la base de datos
		private static  DbAdapter dataSource;
		private Timer t;
		
		//Para las animacions
		private static ImageView pointer;
    	private static ImageView pointer1;
    	private static ImageView pointer2;
    	private static ImageView pointer3;
    	private static RotateAnimation RPM_animation;
    	private static RotateAnimation Intake_animation;
    	private static RotateAnimation fuelLevel_animation;
    	private static RotateAnimation MPH_animation;
    	
    	//Para la interfaz
    	private static TextView RPM ;
    	private static TextView MPH ;
    	private static TextView fuelLevel;
    	private static TextView loadEngine; 
    	private static TextView intakeTemperature; 
    	private static TextView fuelRate ; 
    	private static ProgressBar barra;
    	
    	//Para trabajar con el GPS
    	private static LocationManager locationManager;
    	private static LocationListener locationListener;
    	private static String latitud;
    	private static String longitud;
    	
    	 
        //Datos necesarios para obtener el valor de los datos recibidos	
        	float needle_value=0;
            int value = 0;
        	int PID = 0;
        	int modo=0;
        	
        //Para evitar apagado de pantalla
        	 protected PowerManager.WakeLock wakelock;
		
/**
 * Método onCreate
 */
		  @Override
		    public void onCreate(Bundle savedInstanceState) {
			  super.onCreate(savedInstanceState);
			  
			  //Seleccionamos el modo en BluetoothManagement.
			  BluetoothManagement.SELEC_MODO=false;
			//Quitamos barra de notificaciones
				this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		        // Indicamos el layout
		        setContentView(R.layout.modocrucero);
		        
		      //Evitar que la pantalla se apague
		        final PowerManager pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
		        this.wakelock=pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "etiqueta");
		        wakelock.acquire();
		        
		    	//<--------- Inicializamos los distintos displays ---------->//    	
	        	RPM = (TextView) findViewById(R.id.RPMView); 
	        	MPH = (TextView) findViewById(R.id.KMHView); 
	        	fuelLevel = (TextView) findViewById(R.id.fuelView);
	        	loadEngine = (TextView) findViewById(R.id.loadView); 
	        	intakeTemperature = (TextView) findViewById(R.id.intakeView); 
	        	fuelRate = (TextView) findViewById(R.id.fuelrateView); 
	        	barra=(ProgressBar) findViewById(R.id.progressBar1);
	        	
	        	
	        	//<-------- Inicializamos las animaciones --------->//
	        			       
	        	pointer = (ImageView) findViewById(R.id.imageView1);
	        	pointer1 = (ImageView) findViewById(R.id.imageView2);
	        	pointer2 = (ImageView) findViewById(R.id.imageView3);
	        	pointer3 = (ImageView) findViewById(R.id.imageView4);

		        // Obtenemos el adapter local para conexión bluetooth
		        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		        // Si el adapter devuelve null no disponemos de bluetooth
		        if (mBluetoothAdapter == null) {
		            Toast.makeText(this, "Bluetooth no está diponible", Toast.LENGTH_LONG).show();
		            finish();
		            return;
		        }
		        
		        //Abrimos la base de datos
		        dataSource = new DbAdapter(this);
		        dataSource.open();
		        
		        
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
		            	  message_number=message_number+1;;
		            	  if(message_number==6){
		            		  message_number = 1;
  		            		acc_final=(double)mAccel;
  		            		dataSource.crearRegistro(RPM_final, KPH_final, Temp_final, Fuel_final, Fuelrate_final, load_final, acc_final, throtle_final, latitud, longitud);
  		            		}
		              }
		          }
		               
		      },
		      //Set how long before to start calling the TimerTask (in milliseconds)
		      0,
		      //Set the amount of time between each execution (in milliseconds)
		      300);
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
		   * Método onStart()
		   */
		    @Override
		    public void onStart() {
		        super.onStart();
		      //Seleccionamos el modo
				BluetoothManagement.SELEC_MODO=false;
				
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
		     * Método que necesitamos introducir para el manejo del acelerómetro.
		     */
		    @Override
		    public void onAccuracyChanged(Sensor sensor,int accuracy){}
		    
		    
		    /*
		     * Método para obtener datos del acelerómentro
		     */
		    @Override
		    public void onSensorChanged(SensorEvent event){
		    	//Sincronizamos para eveitar problemas de coocncurrencia
		    	synchronized (this){
		    		
		    		
		    		xCurrent=event.values[0];
		    		yCurrent=event.values[1];
		    		zCurrent=event.values[2];
		    		
		    		//Vemos la variación
		    		varZ=(int)(zCurrent-zLast);
		            varY=(int)(yCurrent-yLast);
		            varX=(int)(xCurrent-xLast);
		            
		            //Actualizamos
		            zLast=zCurrent;
		            yLast=yCurrent;
		            xLast=xCurrent;
		            
		          //Valor exacto de la aceleración con filtro high-pass para la gravedad
		            mAccelLast = mAccelCurrent;
		            mAccelCurrent = (float)Math.abs(Math.sqrt((double) (varZ*varZ+varY*varY+varX*varX)));
		            delta = mAccelCurrent - mAccelLast;
		            mAccel = mAccel * 0.9f + delta; // perform low-cut filter 
		            
		            //Representamos
		    		
 				((TextView) findViewById(R.id.acceleracionView)).setText(String.format("%.2f", mAccel));
		    			
		    		
		    		
		    	
		    	
		    	}
		    	
		    }
		    
		    /*
		     * (non-Javadoc)
		     * @see android.app.Activity#onResume()
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
		        SensorManager sm=(SensorManager) getSystemService(SENSOR_SERVICE);
		        List <Sensor> sensors=sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
		        if (sensors.size()>0){
		        	sm.registerListener(this,sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
		        }
		    }
		    
		    
		    /**
		     * Método setupChat
		     */
		    
		    private void setupChat() {
		       

		        // Initialize the array adapter for the conversation thread
		        mConversacionArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		        mConversacionView = (ListView) findViewById(R.id.in);
		        mConversacionView.setAdapter(mConversacionArrayAdapter);

		     

		        // Initialize the BluetoothChatService to perform bluetooth connections
		        mServicioChat = new BluetoothManagement(this, mHandler);

		        // Initialize the buffer for outgoing messages
		        mBufferSalida = new StringBuffer("");
		        
		        //---Get Vehicle Data Button---
		        final ToggleButton getDataButton = (ToggleButton) findViewById(R.id.toggleButton1);
		        getDataButton.setOnClickListener(new View.OnClickListener() 
		        {
		        	
		            public void onClick(View v) {
		 
		            	if(getDataButton.isChecked()) {
		            		startTransmission();
		            	}
		            	else {
		            		message_number = 1;
		            	}

		            }
		        });
		        
		        
		     /**   //---Clear Trouble Codes Button---
		        Button getCodesButton = (Button) findViewById(R.id.button2);
		        getCodesButton.setOnClickListener(new View.OnClickListener() 
		        {
		    	
		            public void onClick(View v) {
		 
		            	clearCodes();
		            }
		        });   */
		       
		    }
		    
		    
		    /*
		     * Método para empezar la transimisón
		     */
		    public void startTransmission() {
		    	//sendMessage("AT Z" + '\r');
		    	if(mNombreDispositivoConectado != null) {
		    		dataSource.borrarTabla();
			      
			    	sendMessage("01 00 1" + '\r');  
		    		muestreo=true;
		    		
		    		Fuel_final=53;	//Para almacenar en la base datos
					String displayFuel = String.valueOf(Fuel_final);
					fuelLevel.setText(displayFuel+ "%");
            
					needle_value =((100-Fuel_final)/100)*(35);
					//needle_value=((-35)*)
					fuelLevel_animation = new RotateAnimation(35, needle_value, 36, 56);
					fuelLevel_animation.setInterpolator(new LinearInterpolator());
					fuelLevel_animation.setDuration(100);
					fuelLevel_animation.setFillAfter(true);
					((View) pointer3).startAnimation(fuelLevel_animation);  		
		    		
		    	}
		    	else{
		    		Toast.makeText(getApplicationContext(), "Adaptador OBD no conectado", Toast.LENGTH_SHORT).show();	
		    	}
		    }
		    
		    /*
		     * Método que manda los PID que solicitamos a la ECU
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
		        		sendMessage("01 04 1" + '\r'); //get load engine
		        		
		        		
		        		break;
		        	
		        	case 4:
		        		sendMessage("01 05 1" + '\r'); //get Intake Temperature
		        		
		        		
		        		break;
		        		
		        	case 5:
		        		//sendMessage("01 11 1" + '\r'); //get throttle position
		        		sendMessage("01 05 1" + '\r'); //get Intake Temperature
		        		break;
		        	
		        	
		        	case 6:
		        		sendMessage("01 2F 1" + '\r'); //get fuel level
		        		break;
		        		
		        	case 7:
		        		sendMessage("01 5E 1" + '\r'); //get fuel-rate
		        		
		        		break;
		        		
		        	default: ; 		 
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
		    	if(mNombreDispositivoConectado!=null && muestreo==false){
		    		sendMessage("01 01"+'\r'); //Enviamos el comando para consultar los codigos de error almacenados
		    		muestreo=false;
		    	}
		    	
		    	else{
		    		Toast.makeText(getApplicationContext(), "Adaptador OBD no conectado o recibiendo ya datos", Toast.LENGTH_SHORT).show();
		    	}
		    	
		    	
		    }
		    
		    /**
		     * Método que envía el comando para obtener los códigos de error en concreto.
		     */
		    public void obtenerErrores(){
		    	
		    		sendMessage("03"+'\r');
		    	
		    }
		    /*
		     * (non-Javadoc)
		     * @see android.app.Activity#onPause()
		     */
		    @Override
		    public synchronized void onPause() {
		        super.onPause();
		        dataSource.close();
		        
		       // t.cancel();
		   }
		    
		    /*
		     * (non-Javadoc)
		     * @see android.app.Activity#onStop()
		     */

		    @Override
		    public void onStop() {
		        super.onStop();
		        
		        //Cerramos base de datos
		        dataSource.close();
		        //Desactivamos sensores
		        SensorManager sm= (SensorManager) getSystemService(SENSOR_SERVICE);
		        sm.unregisterListener(this);
		        //Paramos el temporizador para muestreo
		        t.cancel();
		        //Anulamos el servicio GPS
		        locationManager.removeUpdates(locationListener);
		        
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
		        this.wakelock.release();
		        

		    }

		    /*private void ensureDiscoverable() {
		        if(D) Log.d(TAG, "ensure discoverable");
		        if (mBluetoothAdapter.getScanMode() !=
		            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
		            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		            startActivity(discoverableIntent);
		        }
		    }*/

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

		   /* // The action listener for the EditText widget, to listen for the return key
		    private TextView.OnEditorActionListener mWriteListener =
		        new TextView.OnEditorActionListener() {
		        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		            // If the action is a key-up event on the return key, send the message
		            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
		                String message = view.getText().toString();
		                sendMessage(message); 
		            }
		          return true;
		        }
		    };*/
		    
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
		                //byte[] writeBuf = (byte[]) msg.obj;
		                // Costruimos un String para el nuevo buffer
		                //String writeMessage = new String(writeBuf);
		              
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
			            	
			        						case 5://PID(05): Intake Temperature-Esperamos un byte de respuesta
			        							
			        							value=Integer.parseInt(bytes[2].trim(),16); //Convertimos el valor hexadecimal a decimal
			        							value = value - 40; //Formula for Intake Temperature
			        							Temp_final=value;   //Valor a almacenar en la base de datos.
			        							String displayIntakeTemp = String.valueOf(value);
			        							intakeTemperature.setText(displayIntakeTemp+"ºC");
			        							
			        							//Log.e("El valor es", displayIntakeTemp);
				                    
			        							needle_value =((value/10)-5)*8-35;  //Valor para la animación.
				                    
			        							if(prev_intake ==0) {
			        								Intake_animation = new RotateAnimation(-35, needle_value, 36, 56);
			        								Intake_animation.setInterpolator(new LinearInterpolator());
			        								Intake_animation.setDuration(100);
			        								Intake_animation.setFillAfter(true);
			        								((View) pointer2).startAnimation(Intake_animation); 
			        								prev_intake = needle_value;
						            	   
			        							}
			        							else {
			        								Intake_animation = new RotateAnimation(prev_intake, needle_value, 36, 56);
			        								Intake_animation.setInterpolator(new LinearInterpolator());
			        								Intake_animation.setDuration(100);
			        								Intake_animation.setFillAfter(true);
			        								((View) pointer2).startAnimation(Intake_animation); 
			        								prev_intake = needle_value;
			        							}
				                    
				                    
			        							break;
				            		
			        						case 47://PID(2F): Fuel level
			        							value=Integer.parseInt(bytes[2].trim(),16); //Convertimos el valor hexadecimal a decimal
			        							value = value*100/255; //Formula for para el el fuel level
			        							Fuel_final=value;	//Para almacenar en la base datos
			        							String displayFuel = String.valueOf(Fuel_final);
			        							fuelLevel.setText(displayFuel+ "%");
					                    
			        							needle_value =((100-value)/100)*(35);
			        							//needle_value =((100-value)/100)*(-35)*3;
			        							
			        							//Log.e("El valor es", displayFuel);
					                    
			        							if(prev_fuel ==0) {
			        								fuelLevel_animation = new RotateAnimation(35, needle_value, 36, 56);
			        								fuelLevel_animation.setInterpolator(new LinearInterpolator());
			        								fuelLevel_animation.setDuration(100);
			        								fuelLevel_animation.setFillAfter(true);
			        								((View) pointer3).startAnimation(fuelLevel_animation); 
			        								prev_fuel = needle_value;
							            	   
			        							}
			        							else {
			        								fuelLevel_animation = new RotateAnimation(prev_fuel, needle_value, 36, 56);
			        								fuelLevel_animation.setInterpolator(new LinearInterpolator());
			        								fuelLevel_animation.setDuration(100);
			        								fuelLevel_animation.setFillAfter(true);
			        								((View) pointer3).startAnimation(fuelLevel_animation); 
			        								prev_fuel = needle_value;
			        							}
					                    
					                    
			        							break;
				            			            		
				            
				            	
			        						case 12: //PID(0C): RPM- Esperamos 2 bytes de respuesta
			        							
			        							String resultado=bytes[2]+bytes[3];
			        							value=Integer.parseInt(resultado.trim(),16); //Convertimos el valor hexadecimal a decimal
			        							
			        							float RPM_value = (value)/4;
			        							RPM_final=(int)RPM_value;	//Para la base de datos
			        							needle_value = ((RPM_value*45)/1000)-135 ;
			        						
			        							if(RPM_value>=6000){
			        								needle_value=135;
			        							}
				                		
			        							if(prev_RPM ==0) {
			        								RPM_animation = new RotateAnimation(-135, needle_value, 72, 105);
			        								RPM_animation.setInterpolator(new LinearInterpolator());
			        								RPM_animation.setDuration(100);
			        								RPM_animation.setFillAfter(true);
			        								((View) pointer).startAnimation(RPM_animation);
			        								prev_RPM = needle_value;
				    	            	 
			        							}
			        							else {
			        								RPM_animation = new RotateAnimation(prev_RPM, needle_value, 72, 105);
			        								RPM_animation.setInterpolator(new LinearInterpolator());
			        								RPM_animation.setDuration(100);
			        								RPM_animation.setFillAfter(true);
			        								((View) pointer).startAnimation(RPM_animation);
			        								prev_RPM = needle_value;
			        							}
				                		
			        							String displayRPM = String.valueOf(RPM_final);
			        							RPM.setText(displayRPM);
			        							//Log.e("Bits juntos", resultado);
			        							//Log.e("El valor es", displayRPM);
			        							
			        							//Acelerador y fuelRate
			        							

			        							if(RPM_final>1000){
			        								Fuelrate_final=RPM_final/250;		//Para almacenar en la base de datos
			        							} else{
			        								Fuelrate_final=(RPM_final/RPM_final)-1;		//Para almacenar en la base de datos;
			        							}
			        							
			        							String displayFuelR=String.valueOf(Fuelrate_final);
			        							fuelRate.setText(displayFuelR);
			        							
			        							Fuel_final=(int)53;
			        							
			        							if(RPM_final>1000){
			        							throtle_final=RPM_final/56;	//Para almacenar en la base de datos.
			        							} else{
			        								throtle_final=RPM_final/RPM_final;
			        							}
			        							barra.setProgress(throtle_final); //Movemos la barra de aceleración
			        							
			        							
			        							break;
				            		
				            		
			        						case 13://PID(0D): MPH
			        							value=Integer.parseInt(bytes[2].trim(),16); //Convertimos el valor hexadecimal a decimal
			        							
			        							if(value<=100) {
			        								needle_value = ((value* 27)/20) - 135;}
				           				
			        							else{
			        								needle_value=((value/20)-5)*23;
			        								if(value>=220){
			        									needle_value=135;
			        								}
			        							}
			        							KPH_final=value;	//Para la base de taos
				            		
				            		
			        							if(prev_KPH ==0 ) {
			        								MPH_animation = new RotateAnimation(-135, needle_value, 72, 105);
			        								MPH_animation.setInterpolator(new LinearInterpolator());
			        								MPH_animation.setDuration(100);
			        								MPH_animation.setFillAfter(true);
			        								((View) pointer1).startAnimation(MPH_animation);
			        								prev_KPH = needle_value;
						             	    
			        							}
			        							else {
			        								MPH_animation = new RotateAnimation(prev_KPH, needle_value, 72, 105);
			        								MPH_animation.setInterpolator(new LinearInterpolator());
			        								MPH_animation.setDuration(100);
			        								MPH_animation.setFillAfter(true);
			        								((View) pointer1).startAnimation(MPH_animation);
			        								prev_KPH = needle_value;
			        							}
				            		
			        							String displayKPH = String.valueOf(value);
			        							MPH.setText(displayKPH);
			        							
			        							
			        							//Log.e("El valor es", displayKPH);
			        							break;
			        							
			        						case 4: //Load engine-PID(01 04)- 1 byte de retorno
			        							value=Integer.parseInt(bytes[2].trim(),16); //Convertimos el valor hexadecimal a decimal
			        							//value = value*100/255; //Formula for para el % de load enginne
			        							load_final=value*100/255;		//Para almacenar en la base de datos
			        							String displayLoad = String.valueOf(load_final);
			        							loadEngine.setText(displayLoad+ "%");
			        							
			        							
			        							//Log.e("El valor es", displayLoad);
			        							break;
			        							
			        						case 94: //PID(01 5E)- Fuel rate-2 byte de retorno
			        							if(dataRecieved.length()==11){
			        							String resultado2=bytes[2];
			        							String resultado3=bytes[3];
			        							double valor=((Integer.parseInt(resultado2.trim(),16)*256)+Integer.parseInt(resultado3.trim(),16))*0.05; //Convertimos el valor hexadecimal a decimal
			        							Fuelrate_final=(int)valor;		//Para almacenar en la base de datos
			        							//String displayFuelR=String.valueOf(valor);
			        							//fuelRate.setText(displayFuelR);
			        							
			        							//Log.e("Bits juntos", resultado2);
			        							//Log.e("El valor es", displayFuelR);
			        							}
			        							break;
			        							
			        						case 17: //PID(01 11)-Posicion del acelerador-1 byte de retorno
			        							value=Integer.parseInt(bytes[2].trim(),16); //Convertimos el valor hexadecimal a decimal
			        							//value = value*100/255; //Formula for para el % de load enginne
			        							throtle_final=value;	//Para almacenar en la base de datos.
			        							barra.setProgress(value); //Movemos la barra de aceleración
			        						
			        							//Log.e("El valor es", String.valueOf(value));
			        							
			        							break;
			        							
			        						case 1://PID(01 01)-Consulta de los códigos de error-4 bytes de retorno
			        							String valor=Integer.toBinaryString(Integer.parseInt(bytes[2].trim(),16));
			        							valor=completeDigits(valor);
			        							Log.e("Entro en el caso 1", String.valueOf(valor));
			        							if(valor.charAt(0)=='1'){
			        								//Si entramos aquí es que la MIL está encendida
			        								//y hay errores almacenados, sacamos el número de
			        								//Errores almacenados
			        								milOn=true;
			        								numeroErrores=StringToDecimal(valor);
			        								//Enviamos mensaje para obtener los errores en cuestion
			        								obtenerErrores();
			        							}
			        							else{
			        								//No hay errores almacenados
			        								milOn=false;
			        								Toast.makeText(getApplicationContext(), "No hay errores almacenados", Toast.LENGTH_SHORT).show();
			        							}
			        							break;
			        							
			        							
			        						default: 
			        							//value=0;
			        							//PID=0;
			        							break;
			        						
			        									        									        									        					
			        			} // Con este cerramos el switch PID
			        					/*if(message_number == 8){
			    		            		message_number = 1;
			    		            		acc_final=(int)mAccel;
			    		            		dataSource.crearRegistro(RPM_final, KPH_final, Temp_final, Fuel_final, Fuelrate_final, load_final, acc_final, throtle_final);
			    		            		}*/
			        					Log.e("Vamos a incrementos el menssage number", String.valueOf(message_number));
			        					
			        					/*if(muestreo==true){			//Seguimos muestreando en caso de que no hayamos hecho una consulta DBT
			    		            	getData(message_number++);}*/
			    		            	
			    		            	
			    		            	Log.e("Incrementos el menssage number", String.valueOf(message_number));////////////////////////////////////////////////////////

			        		}
			        			
			        			
			        			else if(bytes[0].equals("43")){ //Hemos recibido mensaje de errores.
			        				errorDBT=bytes[1]+bytes[2];
			        				errorDBT=codigoError(errorDBT);
			        				Toast.makeText(getApplicationContext(), "El código de error es: "+"\r"+errorDBT, Toast.LENGTH_SHORT).show();
			        				
			        		
			        	}
		            
		               
		            
		            /*if(bytes[0].equals("01")==false){
		            	if(message_number == 8){
		            		message_number = 1;
		            		acc_final=(int)movement;
		            		dataSource.crearRegistro(RPM_final, KPH_final, Temp_final, Fuel_final, Fuelrate_final, load_final, acc_final, throtle_final);
		            		}
		            	getData(message_number++);}*/
		            	
		            
		           
		                
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

		    private void connectDevice(Intent data, boolean secure) {
		        // Get the device MAC address
		        String address = data.getExtras()
		            .getString(ListaDispositivos.EXTRA_DEVICE_ADDRESS);
		        // Get the BluetoothDevice object
		        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		        // Attempt to connect to the device
		        mServicioChat.connect(device, secure);
		    }

		    @Override
		    public boolean onCreateOptionsMenu(Menu menu) {
		        MenuInflater inflater = getMenuInflater();
		        inflater.inflate(R.menu.obd_read, menu);
		        return true;
		    }

		  /* @Override
		    public boolean onOptionsItemSelected(MenuItem item) {
		        Intent serverIntent = null;
		        switch (item.getItemId()) {
		        case R.id.secure_connect_scan:
		            // Launch the DeviceListActivity to see devices and do scan
		            serverIntent = new Intent(this, ListaDispositivos.class);
		            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
		            return true;
		       /* case R.id.insecure_connect_scan:
		            // Launch the DeviceListActivity to see devices and do scan
		            serverIntent = new Intent(this, DeviceListActivity.class);
		            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
		            return true;
		        case R.id.discoverable:
		            // Ensure this device is discoverable by others
		            ensureDiscoverable();
		            return true;*/
		       /* }
		        return false;
		    }*/
		    
		  	    

		     @Override public boolean onOptionsItemSelected(MenuItem item) {
		    	 Intent serverIntent = null;
		              switch (item.getItemId()) {
		              case R.id.secure_connect_scan:
				            // Launch the DeviceListActivity to see devices and do scan
				            serverIntent = new Intent(this, ListaDispositivos.class);
				            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
				            return true;

		              case R.id.acercaDe:

		                     lanzarAcercaDe(null);

		                     break;

		              case R.id.DTB:
		            	  clearCodes();
		            	  break;
		            	  
		              case R.id.ConsultarDBT:
		            	  consultarDBT();
		            	  break;

		              }
		              
		            	  

		              return true; /** true -> consumimos el item, no se propaga*/

		     }
		     public void lanzarAcercaDe(View view) {

				 Intent i = new Intent(this, AcercaDe.class);

				startActivity(i);

				 }
		    
		    
		    
			
}
