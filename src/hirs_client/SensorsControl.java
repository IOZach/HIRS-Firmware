package hirs_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.concurrent.*;

import com.phidget22.*;

public class SensorsControl {
	    
    // This is demo user id from the backend
	private static String sensorsServerURL = "http://princez.co/hirs/api/";
    private static String username = "nick1@nick1.com";
    private static String password = "youwillget100percent";   
    
    // Phidget Board information
    private static final int PHIDGETSERIALNUMBER = 148564;
    private static final int PHIDGETVERSION = 904;
    
    // Sensor and Controllers Channels
    private static int ThreeSixtyDegreeSLIDER = 0;
    private static int MINIJOYSTICK = 1;
    private static int TOUCHSENSOR = 2;
    private static int TEMPHUMIDITYSENSOR = 3;
    private static int SLIDER = 4;

    // MINIJOYSTICKS CONTROLELR VALUES
    private static final int TOP = 1;
    private static final int RIGHT = 2;
    private static final int BOTTOM = 3;
    private static final int LEFT = 4;
    private static final int CENTER = 5;
    
    // Use time of initialisation to refresh access token of the api
    private static long startTokenExpireDateSeconds;
    private static long endTokenExpireDateSeconds;
    
    // HTTP VERBS
    private static final String POST = "POST";
    private static final String GET = "GET";
    
    // API EndPoints
    private static final String LOGINPOINT = "login";
    private static final String REFRESHPOINT = "refresh";
    private static final String GETSENSOR = "getsensor";
    private static final String UPDATESENSOR = "updatesensor";
    private static final String GETHOUSE = "gethouse";
    private static final String UPDATEHOUSE = "updatehouse";
    
    // ConnectionTypes
    private static final int WRITEJSON = 1;
    private static final int GETJSON = 2;
    
    // SensorsTypes
    private static final int GATE = 1;
    private static final int HEAT = 2;
    private static final int LIGHT = 3;
    private static final int ALARM = 4;
    private static final int HUMIDITY = 5;
    
    // Sensors Types Refresh Intervals
    private static final int ONEHOUR = 1;
    private static final int TWOSECONDS = 2;
    
    // Gate Sensor Parameters
    private static final int GOPENED = 180;
    private static final int GCLOSED = 0;
    
    // Sensors Switches
    private static final int ON = 0;
    private static final int OFF = 1;
    
    // House Notes Types
    private static final int WARNING = 1;
    private static final int NOTIFICATION = 2;
    
    // Heat Levels
    private static final int COLD = 0;
    private static final int COOL = 8;
    private static final int HOT = 15;
    private static final int VERYHOT = 30;
    
    // Places Types
    private static final int KITCHEN = 1;
    private static final int GARAGE = 2;
    private static final int LIVINGROOM = 3;
    private static final int BEDROOM = 4;
    private static final int STORAGE = 5;
    private static final int KIDSROOM = 6;
    private static final int OUTSIDE = 7;
    private static final int HOUSE = 8;
    private static final int ALL = 9;
    
    // Sensors Places of Light and Alarm State 
    private static double HSTATE = 0;
    private static double LSTATE = 0;
    private static double GSTATE = 0;
    private static double ASTATE = 0;
        
    // Demo User Initialization
    private Login demouser = new Login(username,password);
    private Session loggeddemouser = null;
    
    // Demo Customer House Initialization
    private House house = new House();
    
    // Gate Motor
    private RCServo gateServo = new RCServo();
	private Sensor gate = new Sensor();
	
	// House Temperature Sensor and Controller
	private VoltageRatioInput housetemperaturehumid = new VoltageRatioInput();
	private VoltageRatioInput housetemperaturecontroller = new VoltageRatioInput();
	
	private Sensor HouseTempSensor = new Sensor();
	private Sensor HumidityTempSensor = new Sensor();
	
	// Alarm Controller using touch sensor
	private VoltageRatioInput alarmSensor = new VoltageRatioInput();
	private Sensor Alarm = new Sensor();
	
	// Alarm switch using joystick from left to right
	private VoltageRatioInput alarmControllerStick = new VoltageRatioInput();
	private Sensor AlarmState = new Sensor();
	
    // Places Light controllers, light switches using slider to far right or left as on or off.
	private VoltageRatioInput lightSwitches = new VoltageRatioInput();
	private DigitalOutput lightoutput  = new DigitalOutput();
    private static final int DemoLivingRoomLightChannelID = 0;
    private Sensor LightState = new Sensor();
	
	// GSON Builder Initialisation
	private GsonBuilder builder = new GsonBuilder();
	private Gson gson = builder.create();
	
	private int previousControllerTemperatureState = -1;
	private int previousGateState = -1;
	private int previousAlarmState = -1;
	private int previousLightState = -1;

	
	public static void main(String[] args) throws PhidgetException, JsonIOException, IOException{
    		new SensorsControl();
	}
	
    private SensorsControl() throws PhidgetException {
	    
    		try {
    			
    		   // Login demo user to obtain access token and interact between server, sensors a
    		   setupCustomer();
    		   // Setup customer house
    		   setupHouse();
    		   // Setup Alarm Controller
    		   setupAlarmController();
    	   	   // Setup alarm sensor, check if alarm is turned on and if there is any activity report it back.
    	   	   setupAlarmSensor();
    	   	   // Get & Setup Gate Sensor and Motor from server
    	   	   setupGate();
    	   	   // Setup house temperature sensor
    	   	   setupHouseTemperatureHumiditySensor();
    	   	   // Setup house temperature controller
    	   	   setupHouseTemperatureController();
    	   	   // Setup Living room light
    	   	   setupPlacesLight(DemoLivingRoomLightChannelID);
    	   	   
    	    	   Runnable secondsPlayer = () -> {
     	       try {
     	    	   
     	    	   		// Refresh login session token to keep api working
     	    	   		if(isLoginSessionNearToExpired()) {
     	    	   			System.out.println("Api Session Token expired.");
     	    	   			refreshLoginSessionToken(loggeddemouser);
     	    	   		}
     	    	   		
     	    	   		// Observe data in server and let actuators act based on, i.e. open or close gate.
     	    	   		modifyGate();
     	    	   		
        	   	   		// update house temperature of every room and place using slider
        	   	   		updateHouseTemperatureFromController();
        	   	   		
        	   	   		// Update Alarm state from alarm Controller
          			updateAlarmState();
        	   	   		
          			// update house notes if alarm movement detected
          			updateHouseNotesAlarm();
          			
          			// update living room lights status
          			updateLivingRoomLigthStatus();
        	   	   		
     	       }
     	       catch (NumberFormatException | JsonIOException e) {
     	           System.out.println("In Seconds Sensor Observer Failed");
     	       }
     	   };
     	   
	    	   Runnable hourlyPlayer = () -> {
		       try {
		   	   		// Observe temperature precision sensor and update house accordingly with humidity every hour
		   	   		updateHouseTemperatureFromSensor();
		       }
		       catch (NumberFormatException | JsonIOException e) {
		           System.out.println("In hours Sensor Observer Failed");
		       }
		   };
     	   
		   // Service Execution Scheduler
     	   ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
     	   
     	   // execute runnable task every 2 seconds to check sensors
     	   executor.scheduleWithFixedDelay(secondsPlayer, 0, TWOSECONDS, TimeUnit.SECONDS);
     	   executor.scheduleWithFixedDelay(hourlyPlayer, 0, ONEHOUR, TimeUnit.HOURS);
			
		} catch (JsonSyntaxException | JsonIOException e) {
			 System.err.println("Uknown error occured, please contact system provider.");
	         housetemperaturehumid.close();
	         housetemperaturecontroller.close();
	         alarmControllerStick.close();
	         gateServo.close();  
	         e.getStackTrace(); 
		}		
    }
    
    private void updateAlarmState() {
    		
		try {
			
			double joystickpositionvalue = alarmControllerStick.getVoltageRatio();
						
			Alarm =  gson.fromJson(toServer(getFullEndPoint(GETSENSOR,ALARM,HOUSE),GETJSON, loggeddemouser.getAccessToken(), GET, null, "GetHouseAlarmState"),Sensor.class);
						
			if(previousAlarmState != ON && joystickpositionvalue >= 0.5) {
				Alarm.setSensorValue(ON);
				System.out.println("House Alarm turned ON");
				String result = toServer(UPDATESENSOR, WRITEJSON, loggeddemouser.getAccessToken(), POST, "["+gson.toJson(Alarm)+"]", "TurnAlarmOff");
				Alarm = gson.fromJson(result,Sensor.class);
			}else if(previousAlarmState != OFF && joystickpositionvalue <= 0.1) {
				Alarm.setSensorValue(OFF);
				System.out.println("House Alarm turned OFF");
				String result = toServer(UPDATESENSOR, WRITEJSON, loggeddemouser.getAccessToken(), POST, "["+gson.toJson(Alarm)+"]", "TurnAlarmOff");
				Alarm = gson.fromJson(result,Sensor.class);
			}
			
			previousAlarmState = Alarm.getSensorValue();

						
		} catch (JsonSyntaxException | JsonIOException | IOException | PhidgetException e1) {
    			System.out.println("Updating house alarm state failed. please make sure to connect joystick controller and connect to internet.");
			// e1.printStackTrace();
		}
    	
    }
    
    private void updateLivingRoomLigthStatus() {
    	
		try {
			
			String result = "";
			
			// Get Living Room Light State
			LightState = gson.fromJson(toServer(getFullEndPoint(GETSENSOR,LIGHT,LIVINGROOM),GETJSON, loggeddemouser.getAccessToken(), GET, null, "GetLivingRoomLightState"),Sensor.class);
			LightState.setChannelId(DemoLivingRoomLightChannelID);
			
			// Get slider position and use sliding to far right and left as ON or OFF switches for light 
			double sliderposition = lightSwitches.getVoltageRatio();
			
			if(previousLightState != ON && sliderposition >= 0 && sliderposition <= 0.50 || LightState.getSensorValue() == ON) {
				LightState.setSensorValue(ON);
				lightoutput.setState(true);
				System.out.println("Living Room Light turned ON");
				result = toServer(UPDATESENSOR, WRITEJSON, loggeddemouser.getAccessToken(), POST, "["+gson.toJson(LightState)+"]", "TurnLivingRoomLightOn");
				LightState = gson.fromJson(result,Sensor.class);
			}else if(previousLightState != OFF && sliderposition >= 0.51 && sliderposition <= 1  || LightState.getSensorValue() == OFF) {
				LightState.setSensorValue(OFF);
				lightoutput.setState(false);
				System.out.println("Living Room Light turned OFF");
				result = toServer(UPDATESENSOR, WRITEJSON, loggeddemouser.getAccessToken(), POST, "["+gson.toJson(LightState)+"]", "TurnLivingRoomLightOff");
				LightState = gson.fromJson(result,Sensor.class);
			}		

			previousLightState = LightState.getSensorValue();

		} catch (JsonSyntaxException | JsonIOException | IOException | PhidgetException e1) {
    			System.out.println("Updating room light failed. please make sure to connect light lamp to output channel id "+DemoLivingRoomLightChannelID+".");
			// e1.printStackTrace();
		}
    	
    }

    private void updateHouseNotesAlarm() {
    	
		try {
			
			int movement = Math.round((float)alarmSensor.getVoltageRatio());
			
			// Check if alarm is on and there is movement in house
			if(previousAlarmState == ON && movement == 1) {
				house.setHouseNotes(WARNING+"");
				house = gson.fromJson(toServer(UPDATEHOUSE, WRITEJSON, loggeddemouser.getAccessToken(), POST, gson.toJson(house), "UpdateMovementDetectionNotes"),House.class);
				System.out.println("Movement in house detected while alarm is on and warning pushed to user notes");
			}

		} catch (JsonSyntaxException | JsonIOException | IOException | PhidgetException e1) {
    			System.out.println("Detecting movement in the house failed. please make sure to connect the touch sensor and connect to internet.");
			// e1.printStackTrace();
		}
    	
    }
    
    private void updateHouseTemperatureFromSensor() {
    	
    		try {
    				
    				Type type = new TypeToken<ArrayList<Sensor>>() {}.getType();
    				ArrayList<Sensor> sensors = new ArrayList<Sensor>();
    				
	    			housetemperaturehumid.setSensorType(com.phidget22.VoltageRatioSensorType.PN_1125_TEMPERATURE);
	        		int temperature = (int)housetemperaturehumid.getSensorValue();
	        		housetemperaturehumid.setSensorType(com.phidget22.VoltageRatioSensorType.PN_1125_HUMIDITY);
	        		int humidity = (int)housetemperaturehumid.getSensorValue();
	        		
	        		System.out.println("House temperature: "+temperature);
	        		System.out.println("House humidity: "+humidity);
	        		
	        		HouseTempSensor = gson.fromJson(toServer(getFullEndPoint(GETSENSOR,HEAT,HOUSE),GETJSON, loggeddemouser.getAccessToken(), GET, null, "GetHouseTempSensor"),Sensor.class);
	        		HumidityTempSensor = gson.fromJson(toServer(getFullEndPoint(GETSENSOR,HUMIDITY,HOUSE),GETJSON, loggeddemouser.getAccessToken(), GET, null, "GetHumidTempSensor"),Sensor.class);
	        			        		
	        		HouseTempSensor.setSensorValue(temperature);
	        		HumidityTempSensor.setSensorValue(humidity);

	        		sensors.add(HouseTempSensor);
	        		sensors.add(HumidityTempSensor);
	        		String updatesensorresults = toServer(UPDATESENSOR, WRITEJSON, loggeddemouser.getAccessToken(), POST, gson.toJson(sensors), "UpdateHouseTempHumidSensor");
	        		sensors = gson.fromJson(updatesensorresults,type);
    			
    		} catch (PhidgetException | JsonSyntaxException | JsonIOException | IOException e) {
        	    	System.out.println("Update house temperature and humidity failed. Verify sensors are connected to InterfaceBoard.");
    	    		//e.getStackTrace();
    		}
    }
    
    private void updateHouseTemperatureFromController() {
    		
		try {
			
			int controllervalue = (int)(Math.round(housetemperaturecontroller.getSensorValue())*30);
			
			if(controllervalue != previousControllerTemperatureState) {
				
				Type type = new TypeToken<ArrayList<Sensor>>() {}.getType();
				ArrayList<Sensor> sensors = new ArrayList<Sensor>();
				sensors = gson.fromJson(toServer(getFullEndPoint(GETSENSOR,HEAT,ALL),GETJSON, loggeddemouser.getAccessToken(), GET, null, "GetAllPlacesHeatSensors"),type);
				System.out.println("Updating House Temperature via Controller: "+controllervalue);
				for (Sensor sensor : sensors) {
					sensor.setSensorValue(controllervalue);
				}	
				String updatesensorsresults = toServer(UPDATESENSOR, WRITEJSON, loggeddemouser.getAccessToken(), POST, gson.toJson(sensors), "UpdateAllPlacesHeatSensors");
				sensors = gson.fromJson(updatesensorsresults,type);
				
			}
			
			previousControllerTemperatureState = controllervalue;
			
		} catch (PhidgetException | JsonSyntaxException | JsonIOException | IOException e1) {
	    		System.out.println("Update house temperature and humidity failed. Verify sensors are connected to InterfaceBoard.");
		}
    }
    
    private void setupPlacesLight(int channelid) {
        try {
        	
        	// setup up slider controller along with light digital set with outputs channel id 
        	lightSwitches.setDeviceSerialNumber(PHIDGETSERIALNUMBER);
        	lightSwitches.setChannel(SLIDER);
        	lightSwitches.open(1000);

        	lightoutput.setDeviceSerialNumber(PHIDGETSERIALNUMBER);
        	lightoutput.setChannel(channelid);
        	lightoutput.open(1000);
        	
	    } catch (PhidgetException e1) {
		    		System.out.println("Setting up digital output controller for light failed. Connect the light to put channel "+channelid+".");
		    		e1.getStackTrace();
	    }
    }
    
    private void setupAlarmSensor() {
    	
        try {
        	
        	alarmSensor.setDeviceSerialNumber(PHIDGETSERIALNUMBER);
        	alarmSensor.setChannel(TOUCHSENSOR);	
        	alarmSensor.open(1000);	
        	alarmSensor.setDataInterval(1);
        	alarmSensor.setVoltageRatioChangeTrigger(0);

        } catch (PhidgetException e1) {
    			System.out.println("Setting up alarm sensor failed. please make sure to connect the touch sensor. Make sure to close phidget control panel app.");
		    	e1.getStackTrace();
	    }
	    	
    }
    
    private void setupAlarmController() {
        try {
        	
	        	alarmControllerStick.setDeviceSerialNumber(PHIDGETSERIALNUMBER);
	        	alarmControllerStick.setChannel(MINIJOYSTICK);	
	        	alarmControllerStick.open(1000);	
	        	alarmControllerStick.setDataInterval(1);

        } catch (PhidgetException e1) {
		    	System.out.println("Setting up alarm controller failed. Connect the mini joy stick and move right to left to turn alarm on or off.");
    	    		e1.getStackTrace();
        }
    }
    
    private void setupHouseTemperatureController() {
        try {
        		housetemperaturecontroller.setDeviceSerialNumber(PHIDGETSERIALNUMBER);
        		housetemperaturecontroller.setChannel(ThreeSixtyDegreeSLIDER);	
        		housetemperaturecontroller.open(1000);	
        } catch (PhidgetException e1) {
        	    System.out.println("Setting up temperature controller failed. please make sure to connect the 360 slider. Make sure to close phidget control panel app.");
	    		e1.getStackTrace();
        }
    }
    
    
    private void setupHouseTemperatureHumiditySensor() {
        try {
        		housetemperaturehumid.setDeviceSerialNumber(PHIDGETSERIALNUMBER);
        		housetemperaturehumid.setChannel(TEMPHUMIDITYSENSOR);	
        		housetemperaturehumid.open(1000);	
        } catch (PhidgetException e1) {
        	    System.out.println("Setting up precision temperature sensor failed. please make sure to connect the sensor. Make sure to close phidget control panel app.");
	    		e1.getStackTrace();
        }
    }
    
    private void setupHouse() {
    	
		try {
			house = gson.fromJson(toServer(getFullEndPoint(GETHOUSE,0,0),GETJSON, loggeddemouser.getAccessToken(), GET, null, "GetHouse"),House.class);
	   	} catch (JsonSyntaxException | JsonIOException | IOException e) {
			System.out.println("Login credentials failed. Please verify login credentials.");
    			//e.getStackTrace();
		}
    	
    }
    
    private void setupCustomer() {
	   	try {
			loggeddemouser = gson.fromJson(toServer(LOGINPOINT,WRITEJSON, null, POST, gson.toJson(demouser), "GetUserLoginSession"),Session.class);
	   	} catch (JsonSyntaxException | JsonIOException | IOException e) {
			System.out.println("Login credentials failed. Please verify login credentials.");
    			//e.getStackTrace();
		}
   	    // Initialise login session tokens start and end date 
   	    startTokenExpireDateSeconds = (System.currentTimeMillis() / 1000);
   	    endTokenExpireDateSeconds = startTokenExpireDateSeconds + loggeddemouser.getExpiresIn();
    }
    
    private void setupGate()  {
		
        try {
	       	gate = gson.fromJson(toServer(getFullEndPoint(GETSENSOR,GATE,OUTSIDE),GETJSON, loggeddemouser.getAccessToken(), GET, null, "GetGate"),Sensor.class);
	       	gateServo.open(1000);
	       	gateServo.setTargetPosition(gate.getSensorValue());
	       	gateServo.setEngaged(true);
        } catch (PhidgetException | JsonSyntaxException | JsonIOException | IOException e1) {
    	    		System.out.println("Setting up gate motor failed. please make sure to connect servo motor.");
    	    		e1.getStackTrace();
		}
        
		gateServo.addTargetPositionReachedListener(new RCServoTargetPositionReachedListener() {
			public synchronized void onTargetPositionReached(RCServoTargetPositionReachedEvent e) {
				System.out.printf("Gate Opening/Closing Finished: %.3g \n", e.getPosition());
			}
		});
        
    }

	private void modifyGate() {
		
	previousGateState = gate.getSensorValue();
	  
	  try {
		  
		  // Check server if gate changed.
	      Sensor newGateState = gson.fromJson(toServer(getFullEndPoint(GETSENSOR,GATE,OUTSIDE),GETJSON, loggeddemouser.getAccessToken(), GET, null, "GateListenerCall"),Sensor.class);
	      
	      int sensor_value = newGateState.getSensorValue();
	 	  
	      if(previousGateState != sensor_value){
	 		  
	           gateServo.setTargetPosition(sensor_value);
	           gateServo.setEngaged(true);
	           
	 		   if(sensor_value == GCLOSED) {
	 	           System.out.println("Setting gate target position to "+ sensor_value +" i.e. closed.");
	 		   }else if(sensor_value == GOPENED){
	 	           System.out.println("Setting gate target position to "+ sensor_value +" i.e. opened.");
	 		   }else {
	 	           System.out.println("Setting gate target position to "+ 0 +" i.e. uknown.");
	 		   }
	           // update gate sensor value
	 		   gate = newGateState;
	 		   previousGateState = gate.getSensorValue();
	 	  }
	 	  
	  } catch (PhidgetException | JsonSyntaxException | JsonIOException | IOException e1) {
			System.out.println("Sorry couldn't attach to motor, please make sure to connect servo motor."+e1.getMessage());
    			e1.getStackTrace();
	  }
 	         
	}
    
	private synchronized String toServer(String endpoint, int connectionType, String AuthorizationKey, String HttpVerb, String jsonobject, String message) throws IOException, JsonIOException{
	
	    URL url;
	    HttpURLConnection conn = null;
	    BufferedReader rd;
	    String fullURL = sensorsServerURL+endpoint;
	    System.out.println(message+" by Calling server at: "+fullURL);
	    url = new URL(fullURL);
	    conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod(HttpVerb);
	    conn.setDoOutput(true);
	    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	    if(AuthorizationKey != null) {
		    conn.setRequestProperty("Authorization", "Bearer "+AuthorizationKey);
	    }
	       
	    String line;
	    String result = "";
	    GsonBuilder builder = new GsonBuilder();
	    Gson gson = builder.create();
	    String json = "";

	    try {	    	

	    switch(connectionType) {
	    
		    case WRITEJSON:
		    		json = jsonobject;
	 	        OutputStream os = conn.getOutputStream();
		        os.write(json.getBytes("UTF-8"));
		        os.flush();
		        os.close();
		        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		        while ((line = rd.readLine()) != null) {
		          result += line;
		        }
		        rd.close();
		    	break;
		    case GETJSON:
		        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		        while ((line = rd.readLine()) != null) {
		          result += line;
		        }
		        rd.close();
		    	break;
		    default:
		    break;
	    }
	    
	    } catch (IOException e) {
			System.err.println(e.getMessage());
    			//e.getStackTrace();
	    }
	    
	    	    
	    return result;
	
	}

    private void refreshLoginSessionToken(Session session) {
		// GSON Builder Initialisation
	    GsonBuilder builder = new GsonBuilder();
	    Gson gson = builder.create();
    		Refresh refresh_token = new Refresh(loggeddemouser.getRefreshToken());
    		try {
 	   	   // Login demo user to obtain access token and interact between server, sensors a
 	   	   loggeddemouser = gson.fromJson(toServer(REFRESHPOINT,WRITEJSON, null, POST, gson.toJson(refresh_token), "RefreshLoginSessionTokens"),Session.class);
		} catch (JsonIOException e) {
			System.err.println("Sorry refresh token object couldn't be parsed int json.");
    			//e.getStackTrace();
		} catch (IOException e) {
			System.err.println("Place make sure login credential matches.");
    			//e.getStackTrace();
		}
    }
	
	// check if session expirey date is near to 1 day
    private boolean isLoginSessionNearToExpired() {
		long dayinseconds = 86400;
    		long currentTime = (System.currentTimeMillis() / 1000);
    		long diff =  endTokenExpireDateSeconds - currentTime;
    		return diff <= dayinseconds;
    }
    
    private String getFullEndPoint(String endpoint, int type_id, int place_id) {
    		if(type_id == 0 && place_id == 0) {
    			return endpoint;
    		}else {
    			return endpoint+"?type="+type_id+"&place="+place_id;
    		}
    }
    
    private boolean joystickPosition(double value, int position) {
    		
	    	switch(position) {
	    	
		    	case TOP:
		    		return value >= 0.5 && value <= 0.7;
		    	case RIGHT:
		    		return value >= 0.9 && value <= 1;
		    	case BOTTOM:
		    		return value >= 0.3 && value <= 5;
		    	case LEFT:
		    		return value >= 0 && value <= 2;
	    	
	    	}
	    	
	    	return false;
    	
    }
	
}
