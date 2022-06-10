#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <WiFiManager.h>
#include <HX711.h>
#include <Servo.h>

ESP8266WebServer server(80);

// HX711 circuit wiring
const int LOADCELL_DOUT_PIN = 0;
const int LOADCELL_SCK_PIN = 5;
float calibration_factor = 1;
String feedSuccess = "";

HX711 scale;
Servo myservo;

void handleSentVar() {
  if (server.hasArg("food_weight")) { // this is the variable sent from the client
    int food = server.arg("food_weight").toInt();
    int openpos = server.arg("openpos").toInt();
    int closepos = server.arg("closepos").toInt();
    server.send(200, "text/html", "Data received");

    scale.power_up();
    delay(400);
    scale.tare();
    float weight = scale.get_units();
    unsigned long runtime = 0;
    unsigned long currenttime = millis();
    myservo.attach(4, 900, 2100); // attaches the servo on GIO2 to the servo object
    while (weight < food && runtime < 60000) {
      myservo.write(openpos); //open
      delay(100);
      myservo.write(closepos); //close
      delay(100);
      weight = scale.get_units();
      runtime = millis() - currenttime;
    }
    feedSuccess = String(weight);
    myservo.detach(); //To ensure that the servo applied no torque while in rest and reduce power consumption
    scale.power_down(); //Reduce power consumption

  } else if (server.hasArg("servo")) { // this is the variable sent from the client
    int servopos = server.arg("servo").toInt();
    server.send(200, "text/html", "Data received");
    myservo.attach(4, 900, 2100); // attaches the servo on GIO2 to the servo object
    myservo.write(servopos);
    delay(200);
    myservo.detach();

  } else if (server.hasArg("feedSuccess")) { // this is the variable sent from the client
    server.send(200, "text/html", feedSuccess);

  } else if (server.hasArg("readweight")) { // this is the variable sent from the client
    scale.power_up();
    delay(500);
    int reading = scale.read_average(10);
    String weight = String(reading);
    server.send(200, "text/html", weight);
    scale.power_down(); //Reduce power consumption
  }
}

void setup() {
  Serial.begin(9600);
  delay(10);

  WiFiManager wifiManager;
  wifiManager.autoConnect();
  Serial.println("Connected.");

  server.on("/data/", HTTP_GET, handleSentVar); // when the server receives a request with /data/ in the string then run the handleSentVar function
  server.begin();

  scale.begin(LOADCELL_DOUT_PIN, LOADCELL_SCK_PIN);
  scale.set_scale(calibration_factor); //The reading is divided by this value when get_units is call
  scale.power_down();
}

void loop() {
  server.handleClient();
}
