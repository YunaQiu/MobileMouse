package person.yuna.mobilemouse;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    TextView show;
    RelativeLayout mainLayout;
    Button leftBtn,rightBtn,adjust;
    Button blueToothBtn, changeMode;
    Spinner blueToothSpinner;
    BluetoothSocket socket = null;
    private SensorManager sensorManager;
    private Sensor sensor;
    boolean isConnect = false;
    int moveMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        show = (TextView) findViewById(R.id.show);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        leftBtn = (Button) findViewById(R.id.leftBtn);
        rightBtn = (Button) findViewById(R.id.rightBtn);
        blueToothBtn = (Button) findViewById(R.id.blueToothBtn);
        blueToothSpinner = (Spinner) findViewById(R.id.blueToothSpinner);
        changeMode = (Button) findViewById(R.id.changeMode);
        adjust = (Button) findViewById(R.id.adjust);
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        blueToothBtn.setOnClickListener(new BlueButtonListener());
        adjust.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i("info", "----adjust----");
                show.setText("校准");
                sendMessage("adjust");
            }
        });
        mainLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i("info", "----leftClick----");
                show.setText("左键点击");
                sendMessage("leftclick");
            }
        });
        mainLayout.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if (moveMode == 0){
                    if (touchListener.isMove() || touchListener.isUp()){
                        return false;
                    }
                }
                Log.i("info", "----leftLongClick----");
                show.setText("左键长按");
                sendMessage("longclick");
                Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                vib.vibrate(100);
                return true;
            }
        });
        leftBtn.setOnTouchListener(leftBtnListener);
        rightBtn.setOnTouchListener(rightBtnListener);
        changeMode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            switch(moveMode){
                case 0:
                    moveMode = 1;
                    mainLayout.setOnTouchListener(null);
                    sensorManager.registerListener(sensorlistener, sensor, 10);
                    changeMode.setText("切换至触屏滑动模式");
                    break;
                case 1:
                    moveMode = 0;
                    mainLayout.setOnTouchListener(touchListener);
                    sensorManager.unregisterListener(sensorlistener);
                    changeMode.setText("切换至重力感应模式");
                    break;
            }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (moveMode == 0) {
            mainLayout.setOnTouchListener(touchListener);
        }else if(moveMode == 1){
            sensorManager.registerListener(sensorlistener, sensor, 10);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (moveMode == 0) {
            mainLayout.setOnTouchListener(null);
        }else if(moveMode == 1){
            sensorManager.unregisterListener(sensorlistener);
        }
    }

    private View.OnTouchListener leftBtnListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("info", "---left down-----");
                    show.setText("左键按下");
                    sendMessage("leftdown");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("info", "---left up-----");
                    show.setText("左键弹起");
                    sendMessage("leftup");
            }
            return true;
        }
    };
    private View.OnTouchListener rightBtnListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("info", "---right down-----");
                    show.setText("右键按下");
                    sendMessage("rightdown");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("info", "---right up-----");
                    show.setText("右键弹起");
                    sendMessage("rightup");
            }
            return true;
        }
    };
    private ScreenTouchListener touchListener = new ScreenTouchListener();
    private class ScreenTouchListener implements View.OnTouchListener{
        private float deltaX;
        private float deltaY;
        private float preX;
        private float preY;
        private boolean isMove = false;
        private boolean isUp = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("info", "---action down-----");
                    isUp = false;
                    isMove = false;
                    preX = event.getX();
                    preY = event.getY();
                    return false;
                case MotionEvent.ACTION_MOVE:
                    Log.i("info", "---action move-----");
                    deltaX = event.getX() - preX;
                    deltaY = event.getY() - preY;
                    show.setText("移动中");
                    sendMessage("move:(" + deltaX + "," + deltaY + ")");
                    preX = event.getX();
                    preY = event.getY();
                    isMove = true;
                    return true;
                case MotionEvent.ACTION_UP:
                    Log.i("info", "---action up-----");
                    isUp = true;
                    if (isMove){
                        isMove = false;
                        return true;
                    }else{
                        return false;
                    }
            }
            return true;
        }

        public boolean isMove(){
            Log.i("info", "isMove:" + isMove);
            return this.isMove;
        }
        public boolean isUp(){
            Log.i("info", "isUp:" + isUp);
            return this.isUp;
        }
    }

    private SensorEventListener sensorlistener= new SensorEventListener(){
        float gravityX, gravityY, gravityZ;
        @Override
        public void onSensorChanged(SensorEvent event) {
            gravityX=event.values[0];
            gravityY=event.values[1];
            gravityZ=event.values[2];
            show.setText("加速度感应XYZ：("+gravityX+","+gravityY+","+gravityZ+")");
            sendMessage("gravity:("+gravityX+","+gravityY+","+gravityZ+")");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private boolean sendMessage(String message){
        if (isConnect) {
            try {
                OutputStream outStream = socket.getOutputStream();
                outStream.write((message + "\n").getBytes());
                Log.i("info","---发送成功-----");
                return true;
            } catch (IOException e) {
                Log.e("TAG", e.toString());
                Log.i("info","---发送失败-----");
                return false;
            }
        }else{
            Log.i("info","---发送失败-----");
            return false;
        }
    }
    public class BlueSelectorItem{
        private String mac;
        private String name;
        private BluetoothDevice device;

        public BlueSelectorItem(BluetoothDevice device){
            this.device = device;
            this.name = device.getName();
            this.mac = device.getAddress();
        }

        public String getName(){
            return this.name;
        }
        public String getMac(){
            return this.mac;
        }
        public BluetoothDevice getDevice(){
            return this.device;
        }


        //重写toString方法，设置适配器显示的数据
        @Override
        public String toString() {
            String output = this.name + "(" + this.mac + ")";
            return output;
        }
    }

    private class BlueButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null){
                Log.i("info","本机没有蓝牙设备");
                Toast.makeText(MainActivity.this, "找不到本机的蓝牙设备", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i("info","本机有蓝牙设备");
            if (isConnect){
                if (socket != null) {
                    try {
                        socket.close();
                        Toast.makeText(MainActivity.this, "链接已断开", Toast.LENGTH_SHORT).show();
                        Log.i("info","---链接断开-----");
                    } catch (IOException e) {
                        Log.e("TAG", e.toString());
                    }
                }
                blueToothBtn.setText("扫描蓝牙设备");
                isConnect = false;
            }else {
                if (!adapter.isEnabled()) {  //启动蓝牙设备
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intent);
                }
                //设置蓝牙可见性
                Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                        120);
                startActivity(discoveryIntent);
                //获取已配对列表
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                if (devices.size() > 0) {
                    List<BlueSelectorItem> selectList = new ArrayList<BlueSelectorItem>();
                    for (Iterator iterator = devices.iterator(); iterator.hasNext(); ) {
                        BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
                        selectList.add(new BlueSelectorItem(bluetoothDevice));
                        Log.i("info", bluetoothDevice.getAddress());
                    }
                    ArrayAdapter<BlueSelectorItem> selectAdapter = new ArrayAdapter<BlueSelectorItem>(MainActivity.this, android.R.layout.simple_spinner_item, selectList);
//                    selectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    blueToothSpinner.setAdapter(selectAdapter);
                    blueToothSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());
                }
                adapter.cancelDiscovery();
            }
        }
    }

    private class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener{
        static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = ((BlueSelectorItem) blueToothSpinner.getSelectedItem()).getDevice();
            UUID uuid = UUID.fromString(SPP_UUID);
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                socket.connect();
                isConnect = true;
                blueToothBtn.setText("切断蓝牙链接");
                Toast.makeText(MainActivity.this, "链接成功", Toast.LENGTH_SHORT).show();
                Log.i("info","---链接成功-----");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                Log.e("TAG", e.toString());
                Toast.makeText(MainActivity.this, "链接失败", Toast.LENGTH_SHORT).show();
                Log.i("info","---链接失败-----");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (socket != null) {
                try {
                    socket.close();
                    isConnect = false;
                    Log.i("info","---链接断开-----");
                } catch (IOException e) {
                    Log.e("TAG", e.toString());
                }
            }
        }
    }
}
