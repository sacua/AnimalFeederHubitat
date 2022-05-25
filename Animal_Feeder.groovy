metadata {
    definition (name: "Animal Feeder", namespace: "sacua", author: "Samuel Cuerrier Auclair") {
        capability "Actuator"
        
        attribute "response", "string"
        attribute "foodGiven", "string"
        
        command "GiveFood", ["number"]
        
    }
    preferences {
        input name: "IPAddress", type: "string", title: "IP Address of the ESP device", required: true, defaultValue: "192.168.0.1"
    }
}



def parse(String description) {
}

def GiveFood(foodWeight) {
    try {
        def params = [uri: "http://"+ IPAddress + "/data/?food_weight=" + foodWeight]
        
        httpGet(params) { resp ->
            if (resp.success) {
                sendEvent(name: "response", value: "Success");
            }
        }
        runIn(65,"FoodGiven")
    }
    catch (Exception e1) {
      sendEvent(name: "response", value: "Failed");
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
    } 
}
