package com.bwssystems.hal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.util.TextStringFormatter;
import com.google.gson.Gson;

public class HalInfo {
    private static final Logger log = LoggerFactory.getLogger(HalInfo.class);
    private static final String DEVICE_REQUEST = "/DeviceData!DeviceCmd=GetNames!DeviceType=";
    private static final String HVAC_REQUEST = "/HVACData!HVACCmd=GetNames";
    private static final String GROUP_REQUEST = "/GroupData!GroupCmd=GetNames";
    private static final String MACRO_REQUEST = "/MacroData!MacroCmd=GetNames";
    private static final String SCENE_REQUEST = "/SceneData!SceneCmd=GetNames";
    private static final String IRDATA_REQUEST = "/IrData!IRCmd=GetNames";
    private static final String IRBUTTON_REQUEST = "/IrData!IRCmd=GetButtons!IrDevice=";
    private static final String TOKEN_REQUEST = "?Token=";
    private static final String LIGHT_REQUEST = "Light";
    private static final String APPL_REQUEST = "Appl";
    // private static final String VIDEO_REQUEST = "Video";
    private static final String THEATRE_REQUEST = "Theatre";
    private static final String CUSTOM_REQUEST = "Custom";
    private static final String HVAC_TYPE = "HVAC";
    private static final String HOME_TYPE = "Home";
    private static final String GROUP_TYPE = "Group";
    private static final String MACRO_TYPE = "Macro";
    private static final String SCENE_TYPE = "Scene";
    private static final String IRDATA_TYPE = "IrData";
    private HttpClient httpClient;
    private NamedIP halAddress;
	private String theToken;

    public HalInfo(NamedIP addressName, String aGivenToken) {
		super();
        httpClient = HttpClients.createDefault();
        halAddress = addressName;
        theToken = aGivenToken;
	}

	public List<HalDevice> getLights() {
    	return getHalDevices(DEVICE_REQUEST + LIGHT_REQUEST + TOKEN_REQUEST, LIGHT_REQUEST);
    }

	public List<HalDevice> getAppliances() {
    	return getHalDevices(DEVICE_REQUEST + APPL_REQUEST + TOKEN_REQUEST, APPL_REQUEST);
    }

	public List<HalDevice> getTheatre() {
    	return getHalDevices(DEVICE_REQUEST + THEATRE_REQUEST + TOKEN_REQUEST, THEATRE_REQUEST);
    }

	public List<HalDevice> getCustom() {
    	return getHalDevices(DEVICE_REQUEST + CUSTOM_REQUEST + TOKEN_REQUEST, CUSTOM_REQUEST);
    }

	public List<HalDevice> getHVAC() {
    	return getHalDevices(HVAC_REQUEST + TOKEN_REQUEST, HVAC_TYPE);
    }

	public List<HalDevice> getGroups() {
    	return getHalDevices(GROUP_REQUEST + TOKEN_REQUEST, GROUP_TYPE);
    }

	public List<HalDevice> getMacros() {
    	return getHalDevices(MACRO_REQUEST + TOKEN_REQUEST, MACRO_TYPE);
    }

	public List<HalDevice> getScenes() {
    	return getHalDevices(SCENE_REQUEST + TOKEN_REQUEST, SCENE_TYPE);
    }

	public List<HalDevice> getButtons() {
		 List<HalDevice> irDataDevices = getHalDevices(IRDATA_REQUEST + TOKEN_REQUEST, IRDATA_TYPE);
		 
		 return getDeviceButtons(irDataDevices);
    }

	public List<HalDevice> getHome(String theDeviceName) {
		List<HalDevice> deviceList = null;
    	deviceList = new ArrayList<HalDevice>();
		HalDevice aNewHalDevice = new HalDevice();
		aNewHalDevice.setHaldevicetype(HOME_TYPE);
		aNewHalDevice.setHaldevicename(theDeviceName);
		deviceList.add(aNewHalDevice);
    	return deviceList;
    }

	private List<HalDevice> getHalDevices(String apiType, String deviceType) {
		DeviceElements theHalApiResponse = null;
		List<HalDevice> deviceList = null;

		String theUrl = null;
    	String theData;
   		theUrl = "http://" + halAddress.getIp() + apiType + theToken;
   		theData = doHttpGETRequest(theUrl);
    	if(theData != null) {
    		log.debug("GET " + deviceType + " HalApiResponse - data: " + theData);
	    	theHalApiResponse = new Gson().fromJson(theData, DeviceElements.class);
	    	if(theHalApiResponse.getDeviceElements() == null) {
	    		StatusDescription theStatus = new Gson().fromJson(theData, StatusDescription.class);
	    		if(theStatus.getStatus() == null) {
	    			log.warn("Cannot get an devices for type " + deviceType + " for hal " + halAddress.getName() + " as response is not parsable.");
	    		}
	    		else {
	    			log.warn("Cannot get an devices for type " + deviceType + " for hal " + halAddress.getName() + ". Status: " + theStatus.getStatus() + ", with description: " + theStatus.getDescription());
	    		}
	        	return deviceList;
	    	}
	    	deviceList = new ArrayList<HalDevice>();
	    	
	    	Iterator<DeviceName> theDeviceNames = theHalApiResponse.getDeviceElements().iterator();
	    	while(theDeviceNames.hasNext()) {
	    		DeviceName theDevice = theDeviceNames.next();
				HalDevice aNewHalDevice = new HalDevice();
				aNewHalDevice.setHaldevicetype(deviceType);
				aNewHalDevice.setHaldevicename(theDevice.getDeviceName());
				deviceList.add(aNewHalDevice);
	    		
	    	}
    	}
    	else {
    		log.warn("Get Hal device types " + deviceType + " for " + halAddress.getName() + " - returned null, no data.");
    	}
    	return deviceList;
    }

	private List<HalDevice> getDeviceButtons(List<HalDevice> theIrDevices) {
		DeviceElements theHalApiResponse = null;
		List<HalDevice> deviceList = null;

		String theUrl = null;
		String theData;
		if(theIrDevices == null)
			return null;
		Iterator<HalDevice> theHalDevices = theIrDevices.iterator();
		deviceList = new ArrayList<HalDevice>();
		while (theHalDevices.hasNext()) {
			HalDevice theHalDevice = theHalDevices.next();
			theUrl = "http://" + halAddress.getIp() + IRBUTTON_REQUEST + TextStringFormatter.forQuerySpaceUrl(theHalDevice.getHaldevicename()) + TOKEN_REQUEST + theToken;
			theData = doHttpGETRequest(theUrl);
			if (theData != null) {
				log.debug("GET IrData for IR Device " + theHalDevice.getHaldevicename() + " HalApiResponse - data: " + theData);
				theHalApiResponse = new Gson().fromJson(theData, DeviceElements.class);
				if (theHalApiResponse.getDeviceElements() == null) {
					StatusDescription theStatus = new Gson().fromJson(theData, StatusDescription.class);
					if (theStatus.getStatus() == null) {
						log.warn("Cannot get buttons for IR Device " + theHalDevice.getHaldevicename() + " for hal "
								+ halAddress.getName() + " as response is not parsable.");
					} else {
						log.warn("Cannot get buttons for IR Device " + theHalDevice.getHaldevicename() + " for hal "
								+ halAddress.getName() + ". Status: " + theStatus.getStatus() + ", with description: "
								+ theStatus.getDescription());
					}
					return deviceList;
				}
				theHalDevice.setButtons(theHalApiResponse);
				deviceList.add(theHalDevice);

			} else {
				log.warn("Get Hal buttons for IR Device " + theHalDevice.getHaldevicename() + " for "
						+ halAddress.getName() + " - returned null, no data.");
			}
		}
		return deviceList;
	}

	//	This function executes the url against the hal
    protected String doHttpGETRequest(String url) {
    	String theContent = null;
        log.debug("calling GET on URL: " + url);
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            log.debug("GET on URL responded: " + response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() == 200){
                theContent = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8")); //read content for data
                EntityUtils.consume(response.getEntity()); //close out inputstream ignore content
            }
        } catch (IOException e) {
            log.error("doHttpGETRequest: Error calling out to HA gateway: " + e.getMessage());
        }
        return theContent;
    }

	public NamedIP getHalAddress() {
		return halAddress;
	}

	public void setHalAddress(NamedIP halAddress) {
		this.halAddress = halAddress;
	}

}
