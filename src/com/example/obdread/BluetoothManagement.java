package com.example.obdread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;



/** 
 * En esta clase se gestiona y configura la conexión
 * bluetooth con otros dispositivos.
 * Está compuesta de una hebra para escuchar conexiones entrantes, 
 * otra hebra para conectar con los dispositivos y otra hebra para
 * transmitir una vez conectados
 * 
 * 
 * @author Juan Cristóbal Peinado Fernández.
 *
 */
public class BluetoothManagement {
	
	
	
	//Datos miembro
	private  final BluetoothAdapter mAdapter;
	private  final Handler mHandler;
	private int mEstado;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	
	
    // Unique UUID for this application
    final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	
	//Constantes pra indicar el estado actual de la conexión
	public static final int STATE_NONE=0;
	public static final int STATE_LISTEN=1;
	public static final int STATE_CONNECTING=2;
	public static final int STATE_CONNECTED=3;
	
	public static boolean SELEC_MODO=false;
	
	
	//Buffer de almacenamiento
	 //private static BufferedReader r;
	
	/**
	 * Constructor de la clase
	 * @param context Contexto
	 * @param handler
	 */
	
	public BluetoothManagement(Context context, Handler handler){
		 mAdapter = BluetoothAdapter.getDefaultAdapter();
	        mEstado = STATE_NONE;
	        mHandler = handler;
	    }

	    /**
	     * Set the current state of the chat connection
	     * @param state  An integer defining the current connection state
	     */
	    private synchronized void setState(int state) {
	        
	        mEstado = state;
	        if(SELEC_MODO==false){
	        // Give the new state to the Handler so the UI Activity can update
	        	mHandler.obtainMessage(ModoCrucero.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	        	}
	        if(SELEC_MODO==true){
	        	 // Give the new state to the Handler so the UI Activity can update
	        	mHandler.obtainMessage(ModoTutor.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	        }
	        
	    }

	    /**
	     * Return the current connection state. */
	    public synchronized int getState() {
	        return mEstado;
	    }

	    /**
	     * Start the chat service. Specifically start AcceptThread to begin a
	     * session in listening (server) mode. Called by the Activity onResume() */
	    public synchronized void start() {
	        
	        // Cancel any thread attempting to make a connection
	        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

	        // Cancel any thread currently running a connection
	        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

	        setState(STATE_LISTEN);

	       
	    }

	    /**
	     * Start the ConnectThread to initiate a connection to a remote device.
	     * @param device  The BluetoothDevice to connect
	     * @param secure Socket Security type - Secure (true) , Insecure (false)
	     */
	    public synchronized void connect(BluetoothDevice device, boolean secure) {
	       

	        // Cancel any thread attempting to make a connection
	        if (mEstado == STATE_CONNECTING) {
	            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
	        }

	        // Cancel any thread currently running a connection
	        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

	        // Start the thread to connect with the given device
	        mConnectThread = new ConnectThread(device, secure);
	        mConnectThread.start();
	        setState(STATE_CONNECTING);
	    }

	    /**
	     * Start the ConnectedThread to begin managing a Bluetooth connection
	     * @param socket  The BluetoothSocket on which the connection was made
	     * @param device  The BluetoothDevice that has been connected
	     */
	    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
	            device, final String socketType) {
	        

	        // Cancel the thread that completed the connection
	        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

	        // Cancel any thread currently running a connection
	        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

	        

	        // Start the thread to manage the connection and perform transmissions
	        mConnectedThread = new ConnectedThread(socket, socketType);
	        mConnectedThread.start();
	        if(SELEC_MODO==false){
	        	// Send the name of the connected device back to the UI Activity
	        	Message msg = mHandler.obtainMessage(ModoCrucero.MESSAGE_DEVICE_NAME);
	        	Bundle bundle = new Bundle();
	        	bundle.putString(ModoCrucero.DEVICE_NAME, device.getName());
	        	msg.setData(bundle);
	        	mHandler.sendMessage(msg);
	        }
	        if(SELEC_MODO==true){
	        	// Send the name of the connected device back to the UI Activity
	        	Message msg = mHandler.obtainMessage(ModoTutor.MESSAGE_DEVICE_NAME);
	        	Bundle bundle = new Bundle();
	        	bundle.putString(ModoTutor.DEVICE_NAME, device.getName());
	        	msg.setData(bundle);
	        	mHandler.sendMessage(msg);	
	        		
	        }

	        setState(STATE_CONNECTED);
	    }

	    /**
	     * Stop all threads
	     */
	    public synchronized void stop() {
	       

	        if (mConnectThread != null) {
	            mConnectThread.cancel();
	            mConnectThread = null;
	        }

	        if (mConnectedThread != null) {
	            mConnectedThread.cancel();
	            mConnectedThread = null;
	        }

	        
	        setState(STATE_NONE);
	    }

	    /**
	     * Write to the ConnectedThread in an unsynchronized manner
	     * @param out The bytes to write
	     * @see ConnectedThread#write(byte[])
	     */
	    public void write(byte[] out) {
	        // Create temporary object
	        ConnectedThread r;
	        // Synchronize a copy of the ConnectedThread
	        synchronized (this) {
	            if (mEstado != STATE_CONNECTED) return;
	            r = mConnectedThread;
	        }
	        // Perform the write unsynchronized
	        r.write(out);
	    }

	    /**
	     * Indicate that the connection attempt failed and notify the UI Activity.
	     */
	    private void connectionFailed() {
	    	  if(SELEC_MODO==false){
	    		  // Send a failure message back to the Activity
	    		  Message msg = mHandler.obtainMessage(ModoCrucero.MESSAGE_TOAST);
	    		  Bundle bundle = new Bundle();
	    		  bundle.putString(ModoCrucero.TOAST, "Imposible conectar dispositivo");
	    		  msg.setData(bundle);
	    		  mHandler.sendMessage(msg);
	    	  }
	    	  if(SELEC_MODO==true){
	    		// Send a failure message back to the Activity
	    		  Message msg = mHandler.obtainMessage(ModoTutor.MESSAGE_TOAST);
	    		  Bundle bundle = new Bundle();
	    		  bundle.putString(ModoTutor.TOAST, "Imposible conectar dispositivo");
	    		  msg.setData(bundle);
	    		  mHandler.sendMessage(msg); 
	    		  
	    	  }
	        // Start the service over to restart listening mode
	        BluetoothManagement.this.start();
	    }

	    /**
	     * Indicate that the connection was lost and notify the UI Activity.
	     */
	    private void connectionLost() {
	    	
	    	if(SELEC_MODO==false){
	    		// Send a failure message back to the Activity
	    		Message msg = mHandler.obtainMessage(ModoCrucero.MESSAGE_TOAST);
	    		Bundle bundle = new Bundle();
	    		bundle.putString(ModoCrucero.TOAST, "Se perdio la conexión con el dispositivo");
	    		msg.setData(bundle);
	    		mHandler.sendMessage(msg);
	    	}
	    	if(SELEC_MODO==true){
	    		// Send a failure message back to the Activity
	    		Message msg = mHandler.obtainMessage(ModoTutor.MESSAGE_TOAST);
	    		Bundle bundle = new Bundle();
	    		bundle.putString(ModoTutor.TOAST, "Se perdio la conexión con el dispositivo");
	    		msg.setData(bundle);
	    		mHandler.sendMessage(msg);
	    	}

	        // Start the service over to restart listening mode
	        BluetoothManagement.this.start();
	    }

	    /**
	     * This thread runs while attempting to make an outgoing connection
	     * with a device. It runs straight through; the connection either
	     * succeeds or fails.
	     */
	    private class ConnectThread extends Thread {
	        private final BluetoothSocket mmSocket;
	        private final BluetoothDevice mmDevice;
	        private String mSocketType;
	        
	        
	        
	        public ConnectThread(BluetoothDevice device, boolean secure) {
	            mmDevice = device;
	            BluetoothSocket tmp = null;
	            mSocketType = secure ? "Secure" : "Insecure";
	            
	            //Modified to work with SPP Devices
	            final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	           
	            // Get a BluetoothSocket for a connection with the
	            // given BluetoothDevice
	            try {
	                //if (secure) {
	                    //tmp = device.createRfcommSocketToServiceRecord(
	                    //        MY_UUID_SECURE);
	                	 
	                tmp = device.createRfcommSocketToServiceRecord(SPP_UUID);
	                	
	                	
	               // } else {
	                    //tmp = device.createInsecureRfcommSocketToServiceRecord(
	                    //        MY_UUID_INSECURE);
	                	//tmp = device.createRfcommSocketToServiceRecord(SPP_UUID);
	                	
	                	//Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
	                    //tmp = (BluetoothSocket) m.invoke(device, 1);
	               // }
	            } catch (IOException e) {
	               
	            }
	            mmSocket = tmp;
	        }

	        public void run() {
	            
	            setName("ConnectThread" + mSocketType);

	            // Always cancel discovery because it will slow down a connection
	            mAdapter.cancelDiscovery();

	            // Make a connection to the BluetoothSocket
	            try {
	                // This is a blocking call and will only return on a
	                // successful connection or an exception
	                mmSocket.connect();
	            } catch (IOException e) {
	                // Close the socket
	                try {
	                    mmSocket.close();
	                } catch (IOException e2) {
	                  
	                }
	                connectionFailed();
	                return;
	            }

	            // Reset the ConnectThread because we're done
	            synchronized (BluetoothManagement.this) {
	                mConnectThread = null;
	            }

	            // Start the connected thread
	            connected(mmSocket, mmDevice, mSocketType);
	        }

	        public void cancel() {
	            try {
	                mmSocket.close();
	            } catch (IOException e) {
	              
	            }
	        }
	    }

	    /**
	     * This thread runs during a connection with a remote device.
	     * It handles all incoming and outgoing transmissions.
	     */
	    private class ConnectedThread extends Thread {
	        private final BluetoothSocket mmSocket;
	        private final InputStream mmInStream;
	        private final OutputStream mmOutStream;
	       

	        public ConnectedThread(BluetoothSocket socket, String socketType) {
	            
	            mmSocket = socket;
	            InputStream tmpIn = null;
	            OutputStream tmpOut = null;

	            // Get the BluetoothSocket input and output streams
	            try {
	                tmpIn = socket.getInputStream();
	                tmpOut = socket.getOutputStream();
	            } catch (IOException e) {
	               
	            }

	            mmInStream = tmpIn;
	            mmOutStream = tmpOut;
	        }

	        public void run() {
	          
	            byte[] buffer = new byte[1024];
	            int bytes;
	            String line="";
	            BufferedReader r;

	            // Keep listening to the InputStream while connected
	            while (true) {
	                try {
	                    // Read from the InputStream
	                    r = new BufferedReader(new InputStreamReader(mmInStream));
	                    Log.e("Hemos creado el bufferreader y el stringBuilder", "hola");
	                   	line=r.readLine();
	                    
	                   	if((line)!=null && (line)!=""){
	                       	bytes=String.valueOf(line).length();
	                       	buffer=String.valueOf(line).getBytes();
	                       	Log.e("Estamos en if", line);                    	
	                       
	                       	if(SELEC_MODO==false){
	                       		// Send the obtained bytes to the UI Activity
	                       		mHandler.obtainMessage(ModoCrucero.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
	                   		}
	                   		if(SELEC_MODO==true){
	                   		// Send the obtained bytes to the UI Activity
	                       		mHandler.obtainMessage(ModoTutor.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
	                   		}
	                   	}
	                } catch (IOException e) {
	                   
	                    connectionLost();
	                    // Start the service over to restart listening mode
	                    BluetoothManagement.this.start();
	                    break;
	                }
	            }
	        }

	        /**
	         * Write to the connected OutStream.
	         * @param buffer  The bytes to write
	         */
	        public void write(byte[] buffer) {
	            try {
	                mmOutStream.write(buffer);
	                if(SELEC_MODO==false){
	                	// Share the sent message back to the UI Activity
	                	mHandler.obtainMessage(ModoCrucero.MESSAGE_WRITE, -1, -1, buffer)
	                        	.sendToTarget();
	                }
	                if(SELEC_MODO==true){
	                	// Share the sent message back to the UI Activity
	                	mHandler.obtainMessage(ModoTutor.MESSAGE_WRITE, -1, -1, buffer)
	                        	.sendToTarget();
	                }
	            } catch (IOException e) {
	                
	            }
	        }

	        public void cancel() {
	            try {
	                mmSocket.close();
	            } catch (IOException e) {
	                
	            }
	        }
	        
	        
	    }
	}