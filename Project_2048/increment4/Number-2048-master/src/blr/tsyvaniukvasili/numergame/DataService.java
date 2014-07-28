package blr.tsyvaniukvasili.numergame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

public class DataService extends IntentService implements BluetoothAdapter.LeScanCallback{
	public DataService() {
		super("DataService");
		// TODO Auto-generated constructor stub
	}
	private static final String TAG = "BluetoothGattActivity";
	 TestGesture t = new TestGesture();

    private static final String DEVICE_NAME = "SensorTag";
  
  
    /* Humidity Service */
    private static final UUID HUMIDITY_SERVICE = UUID.fromString("f000aa20-0451-4000-b000-000000000000");
    private static final UUID HUMIDITY_DATA_CHAR = UUID.fromString("f000aa21-0451-4000-b000-000000000000");
    private static final UUID HUMIDITY_CONFIG_CHAR = UUID.fromString("f000aa22-0451-4000-b000-000000000000");
    /* Barometric Pressure Service */
    private static final UUID PRESSURE_SERVICE = UUID.fromString("f000aa40-0451-4000-b000-000000000000");
    private static final UUID PRESSURE_DATA_CHAR = UUID.fromString("f000aa41-0451-4000-b000-000000000000");
    private static final UUID PRESSURE_CONFIG_CHAR = UUID.fromString("f000aa42-0451-4000-b000-000000000000");
    private static final UUID PRESSURE_CAL_CHAR = UUID.fromString("f000aa43-0451-4000-b000-000000000000");
    /* Acceleromter configuration servcie */
    private static final UUID ACCELEROMETER_SERVICE = UUID.fromString("f000aa10-0451-4000-b000-000000000000");
    private static final UUID ACCELEROMETER_DATA_CHAR = UUID.fromString("f000aa11-0451-4000-b000-000000000000");
    private static final UUID ACCELEROMETER_CONFIG_CHAR = UUID.fromString("f000aa12-0451-4000-b000-000000000000");
    private static final UUID ACCELEROMETER_PERIOD_CHAR = UUID.fromString("f000aa13-0451-4000-b000-000000000000");

    /* Gyroscope Configuration service 
    private static final UUID GYROSCOPE_SERVICE = UUID.fromString("f000aa50-0451-4000-b000-000000000000");
    private static final UUID GYROSCOPE_DATA_CHAR = UUID.fromString("f000aa51-0451-4000-b000-000000000000");
    private static final UUID GYROSCOPE_CONFIG_CHAR = UUID.fromString("f000aa52-0451-4000-b000-000000000000");
    */
    /* Client Configuration Descriptor */
    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final Handler handler = new Handler();
    private BluetoothAdapter mBluetoothAdapter;
    private SparseArray<BluetoothDevice> mDevices;
    

    private BluetoothGatt mConnectedGatt;
    public String TemperatureData,HumidityData,AccelerometerData,PressureData="";
    
    String[] Items;
   // private TextView mTemperature, mHumidity, mPressure,mAccelerometer;
	public boolean CheckBluetooth()
	{

		 if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
	            //Bluetooth is disabled
	            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            //startActivity(enableBtIntent);
	            //finish();
			 Toast.makeText(this, "Bluetooth is Turned off.Please Turn On and Start The Game.", Toast.LENGTH_LONG).show();
	            return false;
	        }else
	        {

	        	return true;
	        }

	}
	public boolean CheckBLE()
	{

		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            //finish();
            return false;
        }else
        {
        	
        	return true;
        }

	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        mDevices = new SparseArray<BluetoothDevice>();
        if(CheckBLE() == true && CheckBluetooth()==true)
        {
        	Log.i(TAG, "Bluetooth check and BLE check Passes");
        	startScan();
        	
        }
        
        
        return START_NOT_STICKY;
	}
	@Override
	public void onDestroy()
	{

		if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }
	}

	public void Pause()
	{
		 	mHandler.removeCallbacks(mStopRunnable);
	        mHandler.removeCallbacks(mStartRunnable);
	        mBluetoothAdapter.stopLeScan(this);
	}
	public void Stop()
	{
		if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }

	}
	public String[] ListScannedItems()
	{

		for (int i=0; i < mDevices.size(); i++) {
            BluetoothDevice device = mDevices.valueAt(i);
           Items[i]=device.getName();
        }
		return Items;
	}
	public void CreateConnection(int i)
	{
		if(mDevices.size() >0)
		{
		BluetoothDevice device = mDevices.get(mDevices.keyAt(i));
		//BluetoothDevice device = mDevices.get();
        Log.i(TAG, "Connecting to "+device.getName());
        /*
         * Make a connection with the device using the special LE-specific
         * connectGatt() method, passing in a callback for GATT events
         */
        mConnectedGatt = device.connectGatt(this, false, mGattCallback);
		}

	}
	private Runnable mStopRunnable = new Runnable() {
       
        public void run() {
            stopScan();
        }
    };
    private Runnable mStartRunnable = new Runnable() {
       
        public void run() {
            startScan();
        }
    };

    private void startScan() {
        mBluetoothAdapter.startLeScan(this);
        //setProgressBarIndeterminateVisibility(true);

        mHandler.postDelayed(mStopRunnable, 2500);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
        //setProgressBarIndeterminateVisibility(false);
        if(mDevices.size() >0)
        {
        	CreateConnection(0);
        	
        }
    }

    /* BluetoothAdapter.LeScanCallback */

  
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
        /*
         * We are looking for SensorTag devices only, so validate the name
         * that each device reports before adding it to our collection
         */
        if (DEVICE_NAME.equals(device.getName())) {
            mDevices.put(device.hashCode(), device);
        /*    mConnectedGatt = device.connectGatt(this, false, mGattCallback);
            
            mBluetoothAdapter.stopLeScan(this);
            t.train();*/
            //Update the overflow menu
            //invalidateOptionsMenu();
            t.train();
        }
        
    }

    /*
     * In this callback, we've created a bit of a state machine to enforce that only
     * one characteristic be read or written at a time until all of our sensors
     * are enabled and we are registered to get notifications.
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        /* State Machine Tracking */
        private int mState = 0;

        private void reset() { mState = 0; }

        private void advance() { mState++; }

        /*
         * Send an enable command to each sensor by writing a configuration
         * characteristic.  This is specific to the SensorTag to keep power
         * low by disabling sensors you aren't using.
         */
        private void enableNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Enabling pressure cal");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CONFIG_CHAR);
                    characteristic.setValue(new byte[] {0x02});
                    break;
                case 1:
                    Log.d(TAG, "Enabling pressure");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CONFIG_CHAR);
                    characteristic.setValue(new byte[] {0x01});
                    break;
                case 2:
                    Log.d(TAG, "Enabling humidity");
                    characteristic = gatt.getService(HUMIDITY_SERVICE)
                            .getCharacteristic(HUMIDITY_CONFIG_CHAR);
                    characteristic.setValue(new byte[] {0x01});
                    break;
                case 3:
                    Log.d(TAG, "Enabling Accelerometer");
                    characteristic = gatt.getService(ACCELEROMETER_SERVICE)
                            .getCharacteristic(ACCELEROMETER_CONFIG_CHAR);
                    characteristic.setValue(new byte[] {0x01});
                    break;
                case 4:
                    Log.d(TAG,"Enabling accelerometer");
                    characteristic= gatt.getService(ACCELEROMETER_SERVICE)
                            .getCharacteristic(ACCELEROMETER_PERIOD_CHAR);
                    characteristic.setValue(new byte[]{(byte)10});
                    break;
                default:
                    //mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled 1");
                    return;
            }

            gatt.writeCharacteristic(characteristic);
        }

        /*
         * Read the data characteristic's value for each sensor explicitly
         */
        private void readNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Reading pressure cal");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CAL_CHAR);
                    break;
                case 1:
                    Log.d(TAG, "Reading pressure");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_DATA_CHAR);
                    break;
                case 2:
                    Log.d(TAG, "Reading humidity");
                    characteristic = gatt.getService(HUMIDITY_SERVICE)
                            .getCharacteristic(HUMIDITY_DATA_CHAR);
                    break;
                case 3:
                    Log.d(TAG, "Reading Accelerometer");
                    characteristic = gatt.getService(ACCELEROMETER_SERVICE)
                            .getCharacteristic(ACCELEROMETER_DATA_CHAR);
                    break;
                default:
                    //mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled 2");
                    return;
            }

            gatt.readCharacteristic(characteristic);
        }

        /*
         * Enable notification of changes on the data characteristic for each sensor
         * by writing the ENABLE_NOTIFICATION_VALUE flag to that characteristic's
         * configuration descriptor.
         */
        private void setNotifyNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Set notify pressure cal");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CAL_CHAR);
                    break;
                case 1:
                    Log.d(TAG, "Set notify pressure");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_DATA_CHAR);
                    break;
                case 2:
                    Log.d(TAG, "Set notify humidity");
                    characteristic = gatt.getService(HUMIDITY_SERVICE)
                            .getCharacteristic(HUMIDITY_DATA_CHAR);
                    break;
                case 3:
                    Log.d(TAG, "Set notify accelerometer");
                    characteristic = gatt.getService(ACCELEROMETER_SERVICE)
                            .getCharacteristic(ACCELEROMETER_DATA_CHAR);
                    break;
                default:
                    //mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled 3");
                    return;
            }

            //Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true);
            //Enabled remote notifications
            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Connection State Change: "+status+" -> "+connectionState(newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */
                gatt.discoverServices();
                //mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Discovering Services..."));
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                /*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 */
               // mHandler.sendEmptyMessage(MSG_CLEAR);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If there is a failure at any stage, simply disconnect
                 */
                gatt.disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "Services Discovered: "+status);
           // mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Enabling Sensors..."));
            /*
             * With services discovered, we are going to reset our state machine and start
             * working through the sensors we need to enable
             */
            reset();
            enableNextSensor(gatt);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //For each read, pass the data up to the UI thread to update the display
            if (HUMIDITY_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
            }
            if (PRESSURE_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE, characteristic));
            }
            if (PRESSURE_CAL_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE_CAL, characteristic));
            }
            if (ACCELEROMETER_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_ACCELEROMETER, characteristic));
            }

            //After reading the initial value, next we enable notifications
            setNotifyNextSensor(gatt);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //After writing the enable flag, next we read the initial value
            readNextSensor(gatt);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            /*
             * After notifications are enabled, all updates from the device on characteristic
             * value changes will be posted here.  Similar to read, we hand these up to the
             * UI thread to update the display.
             */
            if (HUMIDITY_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
            }
            if (PRESSURE_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE, characteristic));
            }
            if (PRESSURE_CAL_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE_CAL, characteristic));
            }
            if (ACCELEROMETER_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_ACCELEROMETER, characteristic));
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Once notifications are enabled, we move to the next sensor and start over with enable
            advance();
            enableNextSensor(gatt);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "Remote RSSI: "+rssi);
        }

        private String connectionState(int status) {
            switch (status) {
                case BluetoothProfile.STATE_CONNECTED:
                    return "Connected";
                case BluetoothProfile.STATE_DISCONNECTED:
                    return "Disconnected";
                case BluetoothProfile.STATE_CONNECTING:
                    return "Connecting";
                case BluetoothProfile.STATE_DISCONNECTING:
                    return "Disconnecting";
                default:
                    return String.valueOf(status);
            }
        }
    };

	 /*
     * We have a Handler to process event results on the main thread
     */
    private static final int MSG_HUMIDITY = 101;
    private static final int MSG_PRESSURE = 102;
    private static final int MSG_PRESSURE_CAL = 103;
    private static final int MSG_ACCELEROMETER = 104;
    //private static final int MSG_PROGRESS = 201;
    //private static final int MSG_DISMISS = 202;
    //private static final int MSG_CLEAR = 301;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            BluetoothGattCharacteristic characteristic;
            switch (msg.what) {
                case MSG_HUMIDITY:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining humidity value");
                        return;
                    }
                    updateHumidityValues(characteristic);
                    break;
                case MSG_PRESSURE:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining pressure value");
                        return;
                    }
                    updatePressureValue(characteristic);
                    break;
                case MSG_PRESSURE_CAL:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining cal value");
                        return;
                    }
                    updatePressureCals(characteristic);
                    break;
                case MSG_ACCELEROMETER:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining Accelerometer value");
                        return;
                    }
                    updateAccelerometerValue(characteristic);
                    break;
                
            }
        }

        private void updateHumidityValues(BluetoothGattCharacteristic characteristic) {
            double humidity = SensorTagData.extractHumidity(characteristic);

           HumidityData=String.format("%.0f%%", humidity);
        }
        //DetectMotion motion = new DetectMotion();
        Float isStomp=-2.0f;
        String Stomp="";
        double x1,y1,z1,d=0.0f,norm;
        Boolean trigger=false;
        ArrayList<String> dataPoints = new ArrayList<String>();
        
        private void updateAccelerometerValue(BluetoothGattCharacteristic characteristic) {
        	
        Float[] values = SensorTagData.extractAccelerometerReading(characteristic, 0);
        double x,y,z;
        x=values[0];
        y=values[1];
        z=values[2];

        d= Math.sqrt( Math.pow((x-x1),2 )  + Math.pow((y-y1),2 ) + Math.pow((z-z1),2 ));
        if(d>=0.3 && !trigger){
     	   Log.i("start","start");
     	   trigger=true;
        }else if(d<=0.1 && trigger){
     	   Log.i("end", "end");
     	   trigger=false;
     	   try {
     		   

    		   File returnType=whichGesture(dataPoints);
    		   if(dataPoints.size()>6){
    			   String value= t.test(returnType);
    			   Log.i("detected", value);
	    		   if(value=="fly"){
	    			   	sendPatternTrigger("fly");
	    		   }else if(value=="stomp"){
	    			   	sendPatternTrigger("stomp");
	    		   }else if(value=="left"){
	    			   	sendPatternTrigger("left");
	    		   }else if(value=="right"){
	    			   	sendPatternTrigger("right");
	    		   }
    		   }
     		   
     		} catch (Exception e) {
     			Log.i("error", e+"test failing");
     		}
     	  dataPoints.clear(); 
        }
        if(trigger){
     	  dataPoints.add("[ "+x + " " + y + " " + z+" ] ;");
        }
        //update previous values
        x1=x;y1=y;z1=z;
        
        

        	
        	
        	/*    Float[] data = SensorTagData.extractAccelerometerReading(characteristic,0);
            String formatdata=data[0].toString()+","+data[1].toString()+","+data[2].toString()+"\n";
            isStomp = motion.detectStomp(data[0],data[1],data[2]);
            if(isStomp!=-2.0f){
            	Log.i(TAG, "stomp Detected");
            	Intent intent = new Intent("myproject");
                intent.putExtra("data", "stomp");
                sendBroadcast(intent);
                }
                
            */
           /* if(data[0]>0 && data[1]<0 && data[2]<0)
        	{
        		Stomp="stomp1";
        		
        	}
        	else if(data[0]<0 && data[1]>0 && data[2]<0)
        	{
        		Stomp="stomp2";
        	}
        	else if(data[0]<0 && data[1]<0 && data[2]>0)
        	{
        		Stomp="stomp3";
        	}
            Log.i(TAG, "stomp Detected");
            Log.i(TAG, Stomp);

        	Intent intent = new Intent("myproject");
            intent.putExtra("data", Stomp);
            sendBroadcast(intent);
            Log.i(TAG, formatdata);*/
          //SaveData(formatdata,"rtolTest");
           
         //MainActivity ma=new MainActivity();
         //ma.setAccelerometerData(formatdata);
        }

        private int[] mPressureCals;
        private void updatePressureCals(BluetoothGattCharacteristic characteristic) {
            mPressureCals = SensorTagData.extractCalibrationCoefficients(characteristic);
        }

        private void updatePressureValue(BluetoothGattCharacteristic characteristic) {
            if (mPressureCals == null) return;
            double pressure = SensorTagData.extractBarometer(characteristic, mPressureCals);
            double temp = SensorTagData.extractBarTemperature(characteristic, mPressureCals);

           TemperatureData=String.format("%.1f\u00B0C", temp);
            PressureData=String.format("%.2f", pressure);
        }
    };

    private void sendPatternTrigger(String string) {
 	   Intent intent = new Intent("myproject");
        intent.putExtra("data", string);
        sendBroadcast(intent);
 }
 Boolean gesture = false;
 	StringBuffer buffer = new StringBuffer();
    private File whichGesture(ArrayList<String> datapointsList) throws Exception {
 		
    	
		File outputDir = getApplicationContext().getCacheDir(); // context being the Activity pointer
		File outputFile = File.createTempFile("learn", ".seq", outputDir);
		if(outputFile.exists()){
			outputFile.delete();
			outputFile=File.createTempFile("learn", ".seq", outputDir);
		}
		//BufferedReader reader = new BufferedReader(new FileReader("test.seq"));
	    //code to work with list of values
		List<String> dataPoints= datapointsList;
		for (int i = 0; i < dataPoints.size(); i++) {
	            buffer.append(datapointsList.get(i));
		}
		buffer.append(System.getProperty("line.separator"));
	    
		FileWriter writer = new FileWriter(outputFile);
	    writer.write(buffer.toString());
	    buffer.delete(0, buffer.length());
	    writer.close(); 
	    return outputFile;
 	}
 	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	public String getStompData()
	{
		return AccelerometerData;
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}



}