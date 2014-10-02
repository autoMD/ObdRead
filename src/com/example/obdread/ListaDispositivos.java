package com.example.obdread;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Actividad que aparecerá como diaglogo. Se muestra una lista de los dispositivos emparejados
 * y los dispositivos detectados en el área despues de un tiempo.
 */
public class ListaDispositivos extends Activity {
   
    // Dirección a devolver a la clase principal
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Datos miembro
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mDispositivosEmparejadosArrayAdapter;
    private ArrayAdapter<String> mNuevosDispositivosArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup 
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.lista_dispositivos);

        // Cancelamos en caso de volver atrás
        setResult(Activity.RESULT_CANCELED);

        // Inicializamos el boton de búsqueda
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });
        //Inicializamos los ArrayAdapters. Habrá uno para dispositivos emparejados
        // y otro para nuevos dispositivos.
       
        mDispositivosEmparejadosArrayAdapter = new ArrayAdapter<String>(this, R.layout.nombre_dispositivo);
        mNuevosDispositivosArrayAdapter = new ArrayAdapter<String>(this, R.layout.nombre_dispositivo);

        // Buscamos el listView para dispositivos emparejados
        ListView pairedListView = (ListView) findViewById(R.id.dispositivos_emparejados);
        pairedListView.setAdapter(mDispositivosEmparejadosArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Buscamos el listView para nuevos dispositivos
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNuevosDispositivosArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Registramos cuando un dispositivo nuevo es descubierto
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Registramos cuando acabamos de descubrir
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Inicializamos el local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Obtenemos los dispositivos emparejados
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Si hay dispositivos emparejados, los metemos en el ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_dispositivos_emparejados).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mDispositivosEmparejadosArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mDispositivosEmparejadosArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Nos aseguramos de que no estamos en la fase de descubrimiento
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Desregistramos los listener
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Metodo para descubrir dispositivos haciendo uso del btAdapter
     */
    private void doDiscovery() {
        
        // Indicamos que estamos buscando
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Activamos subtitulo para nuevos dispositivos
        findViewById(R.id.title_nuevos_dispositivos).setVisibility(View.VISIBLE);

        // Si ya han sido descubiertos paramos
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

         mBtAdapter.startDiscovery();
    }

    /**
     * Listener para todos los dispositivos en la ListView
     */
    
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancelamos la busqueda
            mBtAdapter.cancelDiscovery();

            // Obtenemos la dirección MAC, que está en los últimos 17 carácteres
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Incluimos la dirección en el Intent
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Finalizamos
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    /**
     * listen para dispositivos descubiertos y cambia el title cuando se acaba la búsqueda
     */
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Cuando encontramos un dispositivo
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Tomamos el objeto del intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Si ya ha sido emparejado ya está en la lista
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNuevosDispositivosArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // Cuando finaliza la busqueda cambiamos el title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNuevosDispositivosArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNuevosDispositivosArrayAdapter.add(noDevices);
                }
            }
        }
    };

}