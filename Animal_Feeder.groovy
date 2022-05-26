metadata {
    definition (name: "Cat-Dog Feeder", namespace: "sacua", author: "Samuel Cuerrier Auclair") {
        capability "Actuator"
        
        attribute "response", "string"
        attribute "foodGiven", "string"
        
        command "GiveFood", ["number"]
        command "servo", ["number"]
        command "setZero"
        command "calibrate", ["number"]
        
        
    }
    preferences {
        input name: "IPAddress", type: "string", title: "IP Address of the ESP device", required: true, defaultValue: "192.168.0.157"
        input name: "servoclose", type: "number", title: "Close position of the servo", required: true, defaultValue: "95"
        input name: "servoopen", type: "number", title: "Open position of the servo", required: true, defaultValue: "75"
    }
}



def parse(String description) {
}

def GiveFood(foodWeight) {
    try {
        weightfood = foodWeight as float
        String foodweight = (weightfood * state.calibrationFactor).toString();
        
        def params = [uri: "http://"+ IPAddress + "/data/?food_weight=" + foodweight + "&openpos=" + servoopen.toString() + "&closepos=" + servoclose.toString()]
        
        httpGet(params) { resp ->
            if (resp.success) {
                sendEvent(name: "response", value: "Success");
            }
        }
        runIn(65,"FoodGiven")
    }
    catch (Exception e1) {
        sendEvent(name: "response", value: "Failed");
        log.error "EC - Call failed: ${e1.message}"
    }
}

def servo(servoPos) {
    try {
        def params = [uri: "http://"+ IPAddress + "/data/?servo=" + servoPos]
        
        httpGet(params) { resp ->
            if (resp.success) {
                sendEvent(name: "response", value: "Success");
            }
        }
    }
    catch (Exception e1) {
      sendEvent(name: "response", value: "Failed");
      log.error "EC - Call failed: ${e1.message}"
    }
}

def setZero() {
    try {
        def params = [uri: "http://"+ IPAddress + "/data/?readweight=0"]
        
        httpGet(params) { resp ->
            if (resp.success) {
                String currentValue = resp.data;
                state.zero = Integer.parseInt(currentValue);
            }
        }
    }
    catch (Exception e1) {
      sendEvent(name: "response", value: "Failed");
      log.error "EC - Call failed: ${e1.message}"
    }
}

def calibrate(weight) {
    try {
        def params = [uri: "http://"+ IPAddress + "/data/?readweight=0"]
        
        httpGet(params) { resp ->
            if (resp.success) {
                String currentValue = resp.data;
                int CurrentValue = Integer.parseInt(currentValue);
                weightCal = weight as float;
                state.calibrationFactor = (CurrentValue - state.zero)/weightCal as float;
            }
        }
    }
    catch (Exception e1) {
      sendEvent(name: "response", value: "Failed");
      log.error "EC - Call failed: ${e1.message}"
    }
}

def FoodGiven() {
    try {
        def params = [uri: "http://"+ IPAddress + "/data/?feedSuccess=0"]
        
        httpGet(params) { resp ->
            if (resp.success) {
                sendEvent(name: "foodGiven", value: resp.data);
            }
        }
    }
    catch (Exception e1) {
      sendEvent(name: "response", value: "Failed");
      log.error "EC - Call failed: ${e1.message}"
    } 
}
