/*
 * Bidirectional Android -> Arduino TCP Client
 * 
 * 28.04.2012
 * by Laurid Meyer
 * 
 * http://www.lauridmeyer.com
 * 
 */
package sPPBTestWiFi.Package;
/*
 *  Use GIT to control version is great
 * */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SPPBTestWiFiActivity extends Activity implements OnInitListener {
	
	//TTS object
    private TextToSpeech myTTS;
    //status check code
    private static final int MY_DATA_CHECK_CODE = 0;
	
	EditText editIp_master;
	EditText editIp_slave;
	EditText editIp_sitStand;
	EditText editIp_gait;
	
	Button buttonConnect_master;//(dis)connect Button
	Button buttonConnect_slave;//(dis)connect Button
	Button buttonConnect_sitStand;//(dis)connect Button
	Button buttonConnect_gait;//(dis)connect Button
	
	
    public NetworkTaskMaster networktask_master;//networktask is the included class to handle the socketconnection
    public NetworkTaskSlave networktask_slave;//networktask is the included class to handle the socketconnection
    public NetworkTaskSitStand networktask_sitStand;//networktask is the included class to handle the socketconnection
    public NetworkTaskGait networktask_gait;//networktask is the included class to handle the socketconnection
    
    public String ipTail_master = "104";
    public String ipTail_slave = "103";
    public String ipTail_sitStand = "105";
    public String ipTail_gait = "106";
    
    public Timer checkConnection_timer = null;
    boolean Connect_flag_master = false;
    boolean Connect_flag_slave = false;
    boolean Connect_flag_sitStand = false;
    boolean Connect_flag_gait = false;
    static int Fail_Count_Master = 0;
    static int Fail_Count_Slave = 0;
    static int Fail_Count_SitStand = 0;
    static int Fail_Count_Gait = 0;
    final int Fail_Threshold = 100;
    boolean MasterConnected = false;
    boolean SlaveConnected = false;
    boolean SitStandConnected = false;
    boolean GaitConnected = false;
    
    // -------------------- Declarations of variables for measurement - begin -----------------
    private boolean startFlag = false;
   
    private PendingIntent pi;
    private BroadcastReceiver br;
    private AlarmManager am;
    
    
    public String User_Name = null;
    public String User_Name_Gait = null;
    public String User_Name_SitStand = null;
    public String User_Name_Balance = null;
    
    public int Selected_Time = 6;
    
    private TextView TextBalanceUserName = null;
    private TextView TextFirstTest = null;
    private TextView TextSecondTest = null;
    private TextView TextThirdTest = null;
    private TextView TextBalanceTestStatus = null;
    private TextView TextBalanceResult = null;
    private TextView TextMasterPoint = null;
    private TextView TextSlavePoint = null;
    private TextView TextBalanceTimeFirst = null;
    private TextView TextBalanceTimeSecond = null;
    private TextView TextBalanceTimeThird = null;
    
    private TextView TextSitStandUserName = null;
    private TextView TextSitStandPoint = null;
    private TextView TextSitStandStatus = null;
    private TextView TextSitStandResult = null;
    private TextView TextSitStandTime = null;
    
    private TextView TextGaitUserName = null;
    private TextView TextGaitPoint = null;
    private TextView TextGaitStatus = null;
    private TextView TextGaitResult = null;
    private TextView TextGaitTimeForward = null;
    private TextView TextGaitTimeReturn = null;
    
    // command flags for WiFi modules
    
    private boolean RFID_Tag_Flag = false;
    private boolean Score_Flag = false;
    private boolean Total_Time_Flag = false;
    private int test_num = 1;
    private int balance_score = 0;
    private int balance_time_1 = 0;
    private int balance_time_2 = 0;
    private int balance_time_3 = 0;
    private boolean SitStandstartFlag = false;
    private int sitStandTime = 0;
    private int sitStandScore = 0;
    private boolean GaitstartFlag = false;
    private int gaitScore = 0;
    private int gaitTimeForward = 0;
    private int gaitTimeReturn = 0;
    
    public DatabaseHandler db;
    
    // -------------------- Declarations of variables for measurement - end -----------------
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        //Settings.System.putInt( context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
        
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        
        //connect the view and the objects
	    buttonConnect_master = (Button)findViewById(R.id.connect_master);
	    buttonConnect_slave = (Button)findViewById(R.id.connect_slave);
	    buttonConnect_sitStand = (Button)findViewById(R.id.connect_sitStand);
	    buttonConnect_gait = (Button)findViewById(R.id.connect_gait);
	    
	    
//	    editIp_master = (EditText)findViewById(R.id.editIpTail_master);
//	    editIp_slave = (EditText)findViewById(R.id.editIpTail_slave);
//	    editIp_sitStand = (EditText)findViewById(R.id.editIpTail_sitStand);
//	    editIp_gait = (EditText)findViewById(R.id.editIpTail_gait);
	    //changeConnectionStatusMaster(false);//change connectionstatus to "disconnected"
	    //changeConnectionStatusSlave(false);//change connectionstatus to "disconnected"
	    
	    //add Eventlisteners
	    buttonConnect_master.setOnClickListener(buttonConnectMasterOnClickListener);
	    buttonConnect_slave.setOnClickListener(buttonConnectSlaveOnClickListener);
	    buttonConnect_sitStand.setOnClickListener(buttonConnectSitStandOnClickListener);
	    buttonConnect_gait.setOnClickListener(buttonConnectGaitOnClickListener);
	    
	    TextBalanceUserName = (TextView)findViewById(R.id.Balance_User);
		TextFirstTest = (TextView)findViewById(R.id.FirstTestResult);
		TextSecondTest = (TextView)findViewById(R.id.SecondTestResult);
		TextThirdTest = (TextView)findViewById(R.id.ThirdTestResult);
		TextBalanceTestStatus = (TextView)findViewById(R.id.BalanceTestStatus);
		TextBalanceResult = (TextView)findViewById(R.id.BalanceResult);
		TextMasterPoint = (TextView)findViewById(R.id.ConnectionMasterPoint);
		TextSlavePoint = (TextView)findViewById(R.id.ConnectionSlavePoint);	
		TextBalanceTimeFirst = (TextView)findViewById(R.id.FirstTestTime);
		TextBalanceTimeSecond = (TextView)findViewById(R.id.SecondTestTime);
		TextBalanceTimeThird = (TextView)findViewById(R.id.ThirdTestTime);
		
		TextSitStandUserName = (TextView)findViewById(R.id.SitStandUser);
		TextSitStandPoint = (TextView)findViewById(R.id.SitStandConnection);
	    TextSitStandStatus = (TextView)findViewById(R.id.SitAndStandStatus);
	    TextSitStandResult = (TextView)findViewById(R.id.SitStandResult);
	    TextSitStandTime = (TextView)findViewById(R.id.SitStandTime);
	    
	    TextGaitUserName = (TextView)findViewById(R.id.Gait_User);
	    TextGaitPoint = (TextView)findViewById(R.id.GaitConnection);
	    TextGaitStatus = (TextView)findViewById(R.id.GaitStatus);
	    TextGaitResult = (TextView)findViewById(R.id.GaitResult);
	    TextGaitTimeForward = (TextView)findViewById(R.id.GaitTimeForward);
	    TextGaitTimeReturn = (TextView)findViewById(R.id.GaitTimeReturn);
	    
        networktask_master = new NetworkTaskMaster();//Create initial instance so SendDataToNetwork doesn't throw an error.
        networktask_slave = new NetworkTaskSlave();//Create initial instance so SendDataToNetwork doesn't throw an error.
        networktask_sitStand = new NetworkTaskSitStand();//Create initial instance so SendDataToNetwork doesn't throw an error.
        networktask_gait = new NetworkTaskGait();//Create initial instance so SendDataToNetwork doesn't throw an error.
        
        checkConnection_timer = new Timer();
        checkConnection_timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					public void run() {
						// TODO Auto-generated method stub
						new Thread(new Runnable() {
							public void run() {
								if (Connect_flag_master) 
								{
									try {
										if(!networktask_master.nsocket.getInetAddress().isReachable(100)) {
											if ((++Fail_Count_Master) >= Fail_Threshold) {
												runOnUiThread(new Runnable() {
													public void run() {
														// TODO Auto-generated method stub
														changeConnectionStatusMaster(false);
													}
												});
											}											
										} else {
											Fail_Count_Master = 0;
										}
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
								if (Connect_flag_slave)
								{	
									try {
										if(!networktask_slave.nsocket.getInetAddress().isReachable(100)) {
											if ((++Fail_Count_Slave) >= Fail_Threshold) {
												runOnUiThread(new Runnable() {
													public void run() {
														// TODO Auto-generated method stub
														changeConnectionStatusSlave(false);
													}
												});
											}
										} else {
											Fail_Count_Slave = 0;
										}
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								if (Connect_flag_sitStand) 
								{
									try {
										if(!networktask_sitStand.nsocket.getInetAddress().isReachable(100)) {
											if ((++Fail_Count_SitStand) >= Fail_Threshold) {
												runOnUiThread(new Runnable() {
													public void run() {
														// TODO Auto-generated method stub
														changeConnectionStatusSitStand(false);
													}
												});
											}											
										} else {
											Fail_Count_SitStand = 0;
										}
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
								if (Connect_flag_gait)
								{	
									try {
										if(!networktask_gait.nsocket.getInetAddress().isReachable(100)) {
											if ((++Fail_Count_Gait) >= Fail_Threshold) {
												runOnUiThread(new Runnable() {
													public void run() {
														// TODO Auto-generated method stub
														changeConnectionStatusGait(false);
													}
												});
											}
										} else {
											Fail_Count_Gait = 0;
										}
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}).start();
					}

				});
			}
		}, 0, 1000);
        
        db = new DatabaseHandler(this);
    }
    
 // ----------------------- CONNECT BUTTON EVENTLISTENER - end ----------------------------
    
    public void showToast(final String toast)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(SPPBTestWiFiActivity.this, toast, Toast.LENGTH_SHORT).show();
	        }
	    });
	}
    
 // ----------------------- THE NETWORK TASK - begin ----------------------------
    public class NetworkTaskSitStand extends AsyncTask<Void, byte[], Boolean> {
        Socket nsocket; //Network Socket
        InputStream nis; //Network Input Stream
        OutputStream nos; //Network Output Stream
        BufferedReader inFromServer;//Buffered reader to store the incoming bytes
        SocketAddress sockaddr;

        @Override
        protected void onPreExecute() {
        	//change the connection status to "connected" when the task is started
        	Connect_flag_sitStand = true;
        	changeConnectionStatusSitStand(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) { //This runs on a different thread
            boolean result = false;
            try {
            	//create a new socket instance
            	
                nsocket = new Socket();
                nsocket.connect(sockaddr, 5000);//connect and set a 10 second connection timeout
                if (nsocket.isConnected()) {//when connected
                    nis = nsocket.getInputStream();//get input
                    nos = nsocket.getOutputStream();//and output stream from the socket
                    inFromServer = new BufferedReader(new InputStreamReader(nis));//"attach the inputstreamreader"
                    while(true){//while connected
                    	String msgFromServer = inFromServer.readLine();//read the lines coming from the socket
                    	System.out.println(msgFromServer);
                    	byte[] theByteArray = msgFromServer.getBytes();//store the bytes in an array
                    	String Test [] = msgFromServer.split(",");
	    				signalProcessing(Test[0]);
                    }
                }
            //catch exceptions
            } catch (IOException e) {
                e.printStackTrace();
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                result = true;
            } finally {
            	closeSocket();
            }
            return result;
        }
        
        //Method closes the socket
        public void closeSocket(){
        	try {
                nis.close();
                nos.close();
                nsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        //Method tries to send Strings over the socket connection
        public void SendDataToNetwork(String cmd) { //You run this from the main thread.
            try {
                if (nsocket.isConnected()) {
                    nos.write(cmd.getBytes());
                } else {
                	showToast("SendDataToNetwork: Cannot send message. Socket is closed");
                }
            } catch (Exception e) {
            	showToast("SendDataToNetwork: Message send failed. Caught an exception");
            }
        }

        //Methods is called everytime a new String is recieved from the socket connection
        @Override
        protected void onProgressUpdate(byte[]... values) {
            if (values.length > 0) {//if the recieved data is at least one byte
                String command=new String(values[0]);//get the String from the recieved bytes
            }
        }
        
        //Method is called when task is cancelled
        @Override
        protected void onCancelled() {
        	Connect_flag_sitStand = false;//change the connection to "disconnected"
        	changeConnectionStatusSitStand(false);
        }
        
        //Method is called after taskexecution
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
            	showToast("onPostExecute: Completed with an Error.");
            } else {
            	showToast("onPostExecute: Completed.");
            }
            Connect_flag_master = false;//change the connection to "disconnected"
        	changeConnectionStatusSitStand(false);
        }
    }
    // ----------------------- THE NETWORK TASK - end ----------------------------
    
 // ----------------------- THE NETWORK TASK - begin ----------------------------
    public class NetworkTaskGait extends AsyncTask<Void, byte[], Boolean> {
        Socket nsocket; //Network Socket
        InputStream nis; //Network Input Stream
        OutputStream nos; //Network Output Stream
        BufferedReader inFromServer;//Buffered reader to store the incoming bytes
        SocketAddress sockaddr;

        @Override
        protected void onPreExecute() {
        	//change the connection status to "connected" when the task is started
        	Connect_flag_gait = true;
        	changeConnectionStatusGait(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) { //This runs on a different thread
            boolean result = false;
            try {
            	//create a new socket instance
            	
                nsocket = new Socket();
                nsocket.connect(sockaddr, 5000);//connect and set a 10 second connection timeout
                if (nsocket.isConnected()) {//when connected
                    nis = nsocket.getInputStream();//get input
                    nos = nsocket.getOutputStream();//and output stream from the socket
                    inFromServer = new BufferedReader(new InputStreamReader(nis));//"attach the inputstreamreader"
                    while(true){//while connected
                    	String msgFromServer = inFromServer.readLine();//read the lines coming from the socket
                    	System.out.println(msgFromServer);
                    	byte[] theByteArray = msgFromServer.getBytes();//store the bytes in an array
                    	String Test [] = msgFromServer.split(",");
	    				signalProcessing(Test[0]);
                    }
                }
            //catch exceptions
            } catch (IOException e) {
                e.printStackTrace();
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                result = true;
            } finally {
            	closeSocket();
            }
            return result;
        }
        
        //Method closes the socket
        public void closeSocket(){
        	try {
                nis.close();
                nos.close();
                nsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        //Method tries to send Strings over the socket connection
        public void SendDataToNetwork(String cmd) { //You run this from the main thread.
            try {
                if (nsocket.isConnected()) {
                    nos.write(cmd.getBytes());
                } else {
                	showToast("SendDataToNetwork: Cannot send message. Socket is closed");
                }
            } catch (Exception e) {
            	showToast("SendDataToNetwork: Message send failed. Caught an exception");
            }
        }

        //Methods is called everytime a new String is recieved from the socket connection
        @Override
        protected void onProgressUpdate(byte[]... values) {
            if (values.length > 0) {//if the recieved data is at least one byte
                String command=new String(values[0]);//get the String from the recieved bytes
            }
        }
        
        //Method is called when task is cancelled
        @Override
        protected void onCancelled() {
        	Connect_flag_gait = false;//change the connection to "disconnected"
        	changeConnectionStatusGait(false);
        }
        
        //Method is called after taskexecution
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
            	showToast("onPostExecute: Completed with an Error.");
            } else {
            	showToast("onPostExecute: Completed.");
            }
            Connect_flag_gait = false;//change the connection to "disconnected"
        	changeConnectionStatusGait(false);
        }
    }
    // ----------------------- THE NETWORK TASK - end ----------------------------
    
    
 // ----------------------- THE NETWORK TASK - begin ----------------------------
    public class NetworkTaskMaster extends AsyncTask<Void, byte[], Boolean> {
        Socket nsocket; //Network Socket
        InputStream nis; //Network Input Stream
        OutputStream nos; //Network Output Stream
        BufferedReader inFromServer;//Buffered reader to store the incoming bytes
        SocketAddress sockaddr;

        @Override
        protected void onPreExecute() {
        	//change the connection status to "connected" when the task is started
        	Connect_flag_master = true;
        	changeConnectionStatusMaster(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) { //This runs on a different thread
            boolean result = false;
            try {
            	//create a new socket instance
            	
                nsocket = new Socket();
                nsocket.connect(sockaddr, 5000);//connect and set a 10 second connection timeout
                if (nsocket.isConnected()) {//when connected
                    nis = nsocket.getInputStream();//get input
                    nos = nsocket.getOutputStream();//and output stream from the socket
                    inFromServer = new BufferedReader(new InputStreamReader(nis));//"attach the inputstreamreader"
                    while(true){//while connected
                    	String msgFromServer = inFromServer.readLine();//read the lines coming from the socket
                    	System.out.println(msgFromServer);
                    	byte[] theByteArray = msgFromServer.getBytes();//store the bytes in an array
                    	String Test [] = msgFromServer.split(",");
	    				signalProcessing(Test[0]);
                    }
                }
            //catch exceptions
            } catch (IOException e) {
                e.printStackTrace();
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                result = true;
            } finally {
            	closeSocket();
            }
            return result;
        }
        
        //Method closes the socket
        public void closeSocket(){
        	try {
                nis.close();
                nos.close();
                nsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        //Method tries to send Strings over the socket connection
        public void SendDataToNetwork(String cmd) { //You run this from the main thread.
            try {
                if (nsocket.isConnected()) {
                    nos.write(cmd.getBytes());
                } else {
                	showToast("SendDataToNetwork: Cannot send message. Socket is closed");
                }
            } catch (Exception e) {
            	showToast("SendDataToNetwork: Message send failed. Caught an exception");
            }
        }

        //Methods is called everytime a new String is recieved from the socket connection
        @Override
        protected void onProgressUpdate(byte[]... values) {
            if (values.length > 0) {//if the recieved data is at least one byte
                String command=new String(values[0]);//get the String from the recieved bytes
            }
        }
        
        //Method is called when task is cancelled
        @Override
        protected void onCancelled() {
        	Connect_flag_master = false;//change the connection to "disconnected"
        	changeConnectionStatusMaster(false);
        }
        
        //Method is called after taskexecution
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
            	showToast("onPostExecute: Completed with an Error.");
            } else {
            	showToast("onPostExecute: Completed.");
            }
            Connect_flag_master = false;//change the connection to "disconnected"
        	changeConnectionStatusMaster(false);
        }
    }
    // ----------------------- THE NETWORK TASK - end ----------------------------
    
    
 // ----------------------- THE NETWORK TASK - begin ----------------------------
    public class NetworkTaskSlave extends AsyncTask<Void, byte[], Boolean> {
        Socket nsocket; //Network Socket
        InputStream nis; //Network Input Stream
        OutputStream nos; //Network Output Stream
        BufferedReader inFromServer;//Buffered reader to store the incoming bytes
        SocketAddress sockaddr;
        boolean serverConnected = false;

        @Override
        protected void onPreExecute() {
        	//change the connection status to "connected" when the task is started
        	Connect_flag_slave = true;//change the connection to "disconnected"
        	changeConnectionStatusSlave(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) { //This runs on a different thread
            boolean result = false;
            try {
            	//create a new socket instance
            	
                nsocket = new Socket();
                nsocket.connect(sockaddr, 5000);//connect and set a 10 second connection timeout
                if (nsocket.isConnected()) {//when connected
                    nis = nsocket.getInputStream();//get input
                    nos = nsocket.getOutputStream();//and output stream from the socket
                    inFromServer = new BufferedReader(new InputStreamReader(nis));//"attach the inputstreamreader"
                    while(true){//while connected
                    	String msgFromServer = inFromServer.readLine();//read the lines coming from the socket
                    	System.out.println(msgFromServer);
                    	byte[] theByteArray = msgFromServer.getBytes();//store the bytes in an array
                    	//showToast(msgFromServer);
                    	String Test [] = msgFromServer.split(",");
	    				signalProcessing(Test[0]);
                    }
                }
            //catch exceptions
            } catch (IOException e) {
                e.printStackTrace();
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                result = true;
            } finally {
            	closeSocket();
            }
            return result;
        }
        
        //Method closes the socket
        public void closeSocket(){
        	try {
                nis.close();
                nos.close();
                nsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        //Method tries to send Strings over the socket connection
        public void SendDataToNetwork(String cmd) { //You run this from the main thread.
            try {
                if (nsocket.isConnected()) {
                    nos.write(cmd.getBytes());
                } else {
                	showToast("SendDataToNetwork: Cannot send message. Socket is closed");
                }
            } catch (Exception e) {
            	showToast("SendDataToNetwork: Message send failed. Caught an exception");
            }
        }

        //Methods is called everytime a new String is recieved from the socket connection
        @Override
        protected void onProgressUpdate(byte[]... values) {
            if (values.length > 0) {//if the recieved data is at least one byte
                String command=new String(values[0]);//get the String from the recieved bytes
            }
        }
        
        //Method is called when task is cancelled
        @Override
        protected void onCancelled() {
        	Connect_flag_slave = false;//change the connection to "disconnected"
        	changeConnectionStatusSlave(false);
        }
        
        //Method is called after taskexecution
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
            	showToast("onPostExecute: Completed with an Error.");
            } else {
            	showToast("onPostExecute: Completed.");
            }
            Connect_flag_slave = false;//change the connection to "disconnected"
        	changeConnectionStatusSlave(false);
        }
    }
    // ----------------------- THE NETWORK TASK - end ----------------------------
    
 // ----------------------- CONNECT BUTTON EVENTLISTENER - begin ----------------------------
    private OnClickListener buttonConnectMasterOnClickListener = new OnClickListener() {
        public void onClick(View v){
        	//ipTail_master = editIp_master.getText().toString();
        	
        	//System.out.println("192.168.0." + ipTail_master);
        	
        	if(!MasterConnected){//if not connected
        		
        		networktask_master = new NetworkTaskMaster(); //New instance of NetworkTask
        		
        		networktask_master.sockaddr = new InetSocketAddress("192.168.0.104", 4567);
        		networktask_master.execute();
        		
        		
        	}else{
        		
        		if(networktask_master!=null){
        			networktask_master.closeSocket();
        			networktask_master.cancel(true);
        		}
        	}
        }
    };
    
    private OnClickListener buttonConnectSlaveOnClickListener = new OnClickListener() {
        public void onClick(View v){
        	//ipTail_slave = editIp_slave.getText().toString();
        	
        	if(!SlaveConnected){//if not connected
        		
        		networktask_slave = new NetworkTaskSlave(); //New instance of NetworkTask
        		networktask_slave.sockaddr = new InetSocketAddress("192.168.0.103", 4567);
        		networktask_slave.execute();
        	}else{
        		
        		if(networktask_slave!=null){
        			networktask_slave.closeSocket();
        			networktask_slave.cancel(true);
        		}
        	}
        }
    };
    
    private OnClickListener buttonConnectSitStandOnClickListener = new OnClickListener() {
        public void onClick(View v){
        	//ipTail_master = editIp_master.getText().toString();
        	
        	//System.out.println("192.168.0." + ipTail_master);
        	
        	if(!SitStandConnected){//if not connected
        		
        		networktask_sitStand = new NetworkTaskSitStand(); //New instance of NetworkTask
        		
        		networktask_sitStand.sockaddr = new InetSocketAddress("192.168.0.105", 4567);
        		networktask_sitStand.execute();
        		
        		
        	}else{
        		
        		if(networktask_sitStand!=null){
        			networktask_sitStand.closeSocket();
        			networktask_sitStand.cancel(true);
        		}
        	}
        }
    };
    
    private OnClickListener buttonConnectGaitOnClickListener = new OnClickListener() {
        public void onClick(View v){
        	//ipTail_master = editIp_master.getText().toString();
        	
        	//System.out.println("192.168.0." + ipTail_master);
        	
        	if(!GaitConnected){//if not connected
        		
        		networktask_gait = new NetworkTaskGait(); //New instance of NetworkTask
        		
        		networktask_gait.sockaddr = new InetSocketAddress("192.168.0.106", 4567);
        		networktask_gait.execute();
        		
        		
        	}else{
        		
        		if(networktask_gait!=null){
        			networktask_gait.closeSocket();
        			networktask_gait.cancel(true);
        		}
        	}
        }
    };
 // ----------------------- CONNECT BUTTON EVENTLISTENER - end ----------------------------
    
    
 // Method changes the connection status
  	public void changeConnectionStatusSitStand(Boolean isConnected) {
  		SitStandConnected=isConnected;//change variable
  		if(isConnected){//if connection established
  			showToast("successfully connected to server");//log
  			speakWords("SitStand terminal is connected!");
  			TextSitStandPoint.setText("SitStand terminal: Online");
  			//buttonConnect_sitStand.setText("Disconnect from SitStand Terminal");//change Buttontext
  			//buttonConnect_sitStand.setTextColor(255);
  		}else{
  			showToast("disconnected from Server!");//log
  			speakWords("Lost connection to SitStand terminal!");
  			Connect_flag_sitStand = false;
  			TextSitStandPoint.setText("SitStand terminal: Offline");
  			//buttonConnect_sitStand.setText("Connect To SitStand Terminal");//change Buttontext
  			//buttonConnect_sitStand.setTextColor(50);
  		}
  	}
  	
 // Method changes the connection status
  	public void changeConnectionStatusGait(Boolean isConnected) {
  		GaitConnected=isConnected;//change variable
  		if(isConnected){//if connection established
  			showToast("successfully connected to server");//log
  			speakWords("Gait terminal is connected!");
  			TextGaitPoint.setText("Gait terminal: Online");
  			//buttonConnect_gait.setText("Disconnect from Gait Terminal");//change Buttontext
  			//buttonConnect_gait.setTextColor(255);
  		}else{
  			showToast("disconnected from Server!");//log
  			speakWords("Lost connection to Gait terminal!");
  			Connect_flag_gait = false;
  			TextGaitPoint.setText("Gait terminal: Offline");
  			//buttonConnect_gait.setText("Connect To Gait Terminal");//change Buttontext
  			//buttonConnect_gait.setTextColor(50);
  		}
  	}   
 
    
    // Method changes the connection status
 	public void changeConnectionStatusMaster(Boolean isConnected) {
 		MasterConnected=isConnected;//change variable
 		if(isConnected){//if connection established
 			showToast("successfully connected to server");//log
 			speakWords("Master terminal is connected!");
 			TextMasterPoint.setText("Master terminal: Online");
 			//buttonConnect_master.setText("Disconnect from Master Terminal");//change Buttontext
 			//buttonConnect_master.setTextColor(255);
 		}else{
 			showToast("disconnected from Server!");//log
 			speakWords("Lost connection to Master terminal!");
 			Connect_flag_master = false;
 			TextMasterPoint.setText("Master terminal: Offline");
 			//buttonConnect_master.setText("Connect To Master Terminal");//change Buttontext
 			//buttonConnect_master.setTextColor(50);
 		}
 	}
 	
 	public void changeConnectionStatusSlave(Boolean isConnected) {
 		SlaveConnected=isConnected;//change variable
 		if(isConnected){//if connection established
 			showToast("successfully connected to server");//log
 			speakWords("Slave terminal is connected!");
 			TextSlavePoint.setText("Slave terminal: Online");
 			//buttonConnect_slave.setText("Disconnect from Slave Terminal");//change Buttontext
 			//buttonConnect_slave.setTextColor(255);
 		}else{
 			showToast("disconnected from Server!");//log
 			speakWords("Lost connection to Slave terminal!");
 			Connect_flag_slave = false;
 			TextSlavePoint.setText("Slave terminal: Offline");
 			//buttonConnect_slave.setText("Connect To Slave Terminal");//change Buttontext
 			//buttonConnect_slave.setTextColor(50);
 		}
 	}
 	
 	private void signalProcessing(String text) {
        String textToSpeak = null;
        
		//////////////////////////////////////////////////////////////////////////////
		//             Balance Test Start
		//////////////////////////////////////////////////////////////////////////////
        
        if(text.equalsIgnoreCase("start")) {
        	if (!startFlag){
        		switch (test_num) {
        		case 1:
        			textToSpeak = "First test start";
	        		startFlag = true;
	        		runOnUiThread(new Runnable() {
						public void run() {
							// TODO Auto-generated method stub
							TextBalanceTestStatus.setText("Test Status: STARTED");
							TextFirstTest.setText("First Test: Testing...");
						}
	        		});
        			break;
        		case 2:
        			textToSpeak = "Second test start";
        			startFlag = true;
        			runOnUiThread(new Runnable() {
    					public void run() {
    						// TODO Auto-generated method stub
    						TextBalanceTestStatus.setText("Test Status: STARTED");
    						TextSecondTest.setText("Second Test: Testing...");
    					}
            		});
        			break;
        		case 3:        			
        			textToSpeak = "Third test start";
        			startFlag = true;
        			runOnUiThread(new Runnable() {
    					public void run() {
    						// TODO Auto-generated method stub
    						TextBalanceTestStatus.setText("Test Status: STARTED");
    						TextThirdTest.setText("Third Test: Testing...");
    					}
            		});
        			break;
        		}
        		
        		
        	}
        }
        
        if(text.equalsIgnoreCase("RFID_Tag")) {
        	RFID_Tag_Flag = true;
        } 

        if(text.equalsIgnoreCase("Score")) {
        	Score_Flag = true;
        } 

        if(text.equalsIgnoreCase("first")) {
        	textToSpeak = "First test is done";
        	startFlag = false;
        	test_num = 2;
        	runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					TextFirstTest.setText("First Test: Done");
				}
    		});	
        } 

        if(text.equalsIgnoreCase("second")) {
        	textToSpeak = "Second test is done";
        	startFlag = false;
        	test_num = 3;
        	runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					TextSecondTest.setText("Second Test: Done");
				}
    		});
        } 

        if(text.equalsIgnoreCase("third")) {
        	textToSpeak = "Third test is done. Congratulations!";
        	startFlag = false;
        	test_num = 1;
        	runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					TextThirdTest.setText("Third Test: Done");
				}
    		});
        } 

        if(text.equalsIgnoreCase("movement")) {
        	if(startFlag) {
        		String test = "1";
            	byte []buffer = test.getBytes();
            	try {
    				networktask_master.nos.write(buffer);
    				networktask_master.nos.flush();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				System.out.println("cannot send the byte");
    				e.printStackTrace();
    			}
            	startFlag = false;
            	test_num = 1;
            	
            	textToSpeak = "Motion detected!";
        	}
        } 

        if(text.equalsIgnoreCase("stop")){
        	
        	startFlag = false;
    		test_num = 1;
    		
        	textToSpeak = "Motion detected!";
        	
        } 
        
        if(text.equalsIgnoreCase("BalanceTimeFirst")) {
        	//startFlag = false;	
        	
        	balance_time_1 = Integer.valueOf(text.substring(8));
    		runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					TextBalanceTimeFirst.setText("Balance Time First: " + balance_time_1/1000.0f + " seconds");
				}
    			
    		});
        	textToSpeak = "Your Balance test time (First) is " + balance_time_1/1000.0f + " seconds";
        	
        	writeFileToSD(User_Name_Balance + ".txt", "\n Balance time First: " + balance_time_1/1000.0f + " seconds\n");
        }
        
        if(text.equalsIgnoreCase("BalanceTimeSecond")) {
        	//startFlag = false;	
        	
        	balance_time_2 = Integer.valueOf(text.substring(8));
    		runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					TextBalanceTimeSecond.setText("Balance Time Second: " + balance_time_2/1000.0f + " seconds");
				}
    			
    		});
        	textToSpeak = "Your Balance test time (Second) is " + balance_time_2/1000.0f + " seconds";
        	
        	writeFileToSD(User_Name_Balance + ".txt", "\n Balance time Second: " + balance_time_2/1000.0f + " seconds\n");
        }
        
        if(text.equalsIgnoreCase("BalanceTimeThird")) {
        	//startFlag = false;	
        	
        	balance_time_3 = Integer.valueOf(text.substring(8));
    		runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					TextBalanceTimeThird.setText("Balance Time Third: " + balance_time_3/1000.0f + " seconds");
				}
    			
    		});
        	textToSpeak = "Your Balance test time (Third) is " + balance_time_3/1000.0f + " seconds";
        	
        	writeFileToSD(User_Name_Balance + ".txt", "\n Balance time Third: " + balance_time_3/1000.0f + " seconds\n");
        }
        
        
        
        if(text.startsWith("BalanceScore")){
    		//startFlag = false;	
    		balance_score = Integer.valueOf(text.substring(12));
    		runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					TextBalanceResult.setText("Result: " + balance_score);
					TextFirstTest.setText("First Test: ");
					TextSecondTest.setText("Second Test: ");
					TextThirdTest.setText("Third Test: ");
					TextBalanceTestStatus.setText("Test Status: Pending...");
				}
    			
    		});
        	textToSpeak = "Your Balance test score is " + balance_score;
        	
        	writeFileToSD(User_Name_Balance + ".txt", "\n Balance score: " + balance_score + "\n");
        } 
        
		//////////////////////////////////////////////////////////////////////////////
		//             Balance Test End
		//////////////////////////////////////////////////////////////////////////////
        
        //////////////////////////////////////////////////////////////////////////////
        //             Sit and Stand Test Start
        //////////////////////////////////////////////////////////////////////////////
        
        if(text.equalsIgnoreCase("SitStandstart")) {
        	if (!SitStandstartFlag){
        		textToSpeak = "Sit and Stand test start";
        		SitStandstartFlag = true;
        		runOnUiThread(new Runnable() {
					public void run() {
						// TODO Auto-generated method stub
						TextSitStandStatus.setText("Test Status: STARTED");
					}
        		});
        	}
        }
        
        if(text.equalsIgnoreCase("SitTime")) {
        	//startFlag = false;	
        	
    		sitStandTime = Integer.valueOf(text.substring(8));
    		runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					TextSitStandTime.setText("SitStand Time: " + sitStandTime/1000.0f + " seconds");
				}
    			
    		});
        	textToSpeak = "Your SitStand test time is " + sitStandTime/1000.0f + " seconds";
        	
        	writeFileToSD(User_Name_SitStand + ".txt", "\n Repeat sit and stand time: " + sitStandTime/1000.0f + " seconds\n");
        }
        
        if(text.startsWith("SitScore")){
    		//startFlag = false;	
        	SitStandstartFlag = false;
    		sitStandScore = Integer.valueOf(text.substring(8));
    		runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					TextSitStandResult.setText("SitStand Result: " + sitStandScore);
					TextSitStandStatus.setText("SitStand Status: Pending...");
				}
    			
    		});
        	textToSpeak = "Your SitStand test score is " + sitStandScore;
        	
        	writeFileToSD(User_Name_SitStand + ".txt", "\n Repeat sit and stand score: " + sitStandScore + "\n");
        } 
        
		//////////////////////////////////////////////////////////////////////////////
		//             Sit and Stand Test End
		//////////////////////////////////////////////////////////////////////////////
        
		//////////////////////////////////////////////////////////////////////////////
		//             Gait Test Start
		//////////////////////////////////////////////////////////////////////////////
        
        if(text.equalsIgnoreCase("Gaitstart")) {
        	if (!GaitstartFlag){
        		textToSpeak = "Gait speed test start";
        		GaitstartFlag = true;
        		runOnUiThread(new Runnable() {
					public void run() {
						// TODO Auto-generated method stub
						TextGaitStatus.setText("Test Status: STARTED");
					}
        		});
        	}
        }
        
        if(text.equalsIgnoreCase("GiatTimeForward")) {
        	//startFlag = false;	
        	
        	gaitTimeForward = Integer.valueOf(text.substring(8));
    		runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					TextGaitTimeForward.setText("Gait Time Forward: " + gaitTimeForward/1000.0f + " seconds");
				}
    			
    		});
        	textToSpeak = "Your forward gait test time is " + gaitTimeForward/1000.0f + " seconds";
        	
        	writeFileToSD(User_Name_Gait + ".txt", "\n Gait time forward: " + gaitTimeForward/1000.0f + " seconds\n");
        }
        
        if(text.equalsIgnoreCase("GiatTimeReturn")) {
        	//startFlag = false;	
        	
        	gaitTimeReturn = Integer.valueOf(text.substring(8));
    		runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					TextGaitTimeReturn.setText("Gait Time Return: " + gaitTimeReturn/1000.0f + " seconds");
				}
    			
    		});
        	textToSpeak = "Your return gait test time is " + gaitTimeReturn/1000.0f + " seconds";
        	
        	writeFileToSD(User_Name_Gait + ".txt", "\n Gait time return: " + gaitTimeReturn/1000.0f + " seconds\n");
        }
        
        if(text.startsWith("GaitScore")){
    		//startFlag = false;
        	GaitstartFlag = false;
    		gaitScore = Integer.valueOf(text.substring(9));
    		runOnUiThread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					TextGaitResult.setText("Gait Result: " + gaitScore);
					TextGaitStatus.setText("Gait Status: Pending...");
				}
    			
    		});
        	textToSpeak = "Your Gait speed test score is " + gaitScore;
        	
        	writeFileToSD(User_Name_Gait + ".txt", "\n Gait speed score: " + gaitScore + "\n");
        } 
        
		//////////////////////////////////////////////////////////////////////////////
		//             Gait Test End
		//////////////////////////////////////////////////////////////////////////////

        if(text.startsWith("16")){//16002FDD8D69
            
        	System.out.println(text.substring(0, 12));
        	Contact contact = db.getContact(text.substring(0, 12));       
        	User_Name = contact.getName();
        	if(User_Name != null) {
        		if(text.endsWith("GT")) {
        			User_Name_Gait = User_Name;
            		textToSpeak = User_Name_Gait + ", Please go ahead for the gait speed test";
            		runOnUiThread(new Runnable() {
        				public void run() {
        					// TODO Auto-generated method stub
        					TextGaitUserName.setText("User Name: " + User_Name_Gait);
        				}
            			
            		});
            	} else if(text.endsWith("ST")) {
            		User_Name_SitStand = User_Name;
            		textToSpeak = User_Name_SitStand + ", Please go ahead push the button and start the sit stand test";
            		runOnUiThread(new Runnable() {
        				public void run() {
        					// TODO Auto-generated method stub
        					TextSitStandUserName.setText("User Name: " + User_Name_SitStand);
        				}
            			
            		});
            	} else if(text.endsWith("BT")) {
            		User_Name_Balance = User_Name;
            		textToSpeak = User_Name_Balance + ", Please go ahead push the button and start the Balance test";
            		runOnUiThread(new Runnable() {
        				public void run() {
        					// TODO Auto-generated method stub
        					TextBalanceUserName.setText("User Name: " + User_Name_Balance);
        				}
            			
            		});
            	} else {
            		textToSpeak = User_Name;
            	}
        	} else {
        		textToSpeak = "TAG is not recognized. Please register first!";
        	}
        	
        }       
    	speakWords(textToSpeak);
    }
    
    
  //speak the user text
    private void speakWords(String speech) {
            //speak straight away
            myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }
 	 	
 	//Method is called when app is closed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        am.cancel(pi);
	    unregisterReceiver(br);
        if(networktask_master!=null){//In case the task is currently running
        	networktask_master.cancel(true);//cancel the task
		}
        if(networktask_slave!=null){//In case the task is currently running
        	networktask_slave.cancel(true);//cancel the task
		}
    }
    
    public static boolean isNumeric(String str) {  
		try  
		{  
			double d = Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
		return true;  
	}
    
  //act on result of TTS data check
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode){
    	case MY_DATA_CHECK_CODE:
    		if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //the user has the necessary data - create the TTS
            myTTS = new TextToSpeech(this, this);
            }
            else {
                    //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
    		break; 
    	}
        
    }
        //setup TTS
    public void onInit(int initStatus) {
            //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
	 * write data to SD card
	 */
	public void writeFileToSD(String File_name, String gait_speed_data) {
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
			Log.d("TestFile", "SD card is not avaiable/writeable right now.");
			return;
		}
		try {
			String pathName = Environment.getExternalStorageDirectory()
					.getPath() + "/SPPB/";
			String fileName = File_name;
			File path = new File(pathName);
			File file = new File(pathName + fileName);
			if (!path.exists()) {
				Log.d("TestFile", "Create the path:" + pathName);
				path.mkdir();
			}
			if (!file.exists()) {
				Log.d("TestFile", "Create the file:" + fileName);
				file.createNewFile();
			}
			FileOutputStream stream = new FileOutputStream(file, true);
			// String s = "this is a test string writing to file.";
			byte[] buf = gait_speed_data.getBytes();
			stream.write(buf);
			
			stream.close();

		} catch (Exception e) {
			Log.e("TestFile", "Error on writeFilToSD.");
			e.printStackTrace();
		}
	}
	
	@Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return super.onCreateOptionsMenu(menu);
	  }
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	    case R.id.Registration:
	    	startActivity(new Intent(this, RFIDDatabaseWiFiActivity.class));
	      break;
	    
	    default:
	      return super.onOptionsItemSelected(item);
	    }
	    return true;
	  }
}