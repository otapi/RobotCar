#include <Servo.h>
#include <elapsedMillis.h>
int pinLB = 12;     // define pin 12
int pinLF = 3;     // define pin 3
int pinRB = 13;    // define pin 13
int pinRF = 11;    // define pin 11
////////////////////////////////
int inputPin = 4;    // define pin for sensor echo
int outputPin = 5;   // define pin for sensor trig

Servo myservo;        // set myservo

elapsedMillis timeElapsed; 

void setup()
{
  Serial.begin(9600);     // define pin for motor output

  //Serial.write("Power On");
  
  pinMode(pinLB, OUTPUT); // pin 12
  pinMode(pinLF, OUTPUT); // pin 3 (PWM)
  pinMode(pinRB, OUTPUT); // pin 13
  pinMode(pinRF, OUTPUT); // pin 11 (PWM)
  pinMode(inputPin, INPUT);    // define input pin for sensor
  pinMode(outputPin, OUTPUT);  // define output pin for sensor

  // attaches the servo on pin 9 to the servo object 
  myservo.attach(9);
  myservo.write(90);
  timeElapsed = 0;
}

int distance()   // measure the distance ahead (cm)
{
  
  digitalWrite(outputPin, LOW);   // ultrasonic sensor transmit low level signal 2μs
  delayMicroseconds(2);
  digitalWrite(outputPin, HIGH);  // ultrasonic sensor transmit high level signal10μs, at least 10μs
  delayMicroseconds(10);
  digitalWrite(outputPin, LOW);    // keep transmitting low level signal
  float Fdistance = pulseIn(inputPin, HIGH);  // read the time in between
  Fdistance = Fdistance / 5.8 / 10;  // convert time into distance (unit: cm)
  return Fdistance;              // read the distance into Fdist
}

char inData[20]; // Allocate some space for the string
char inChar=-1; // Where to store the character read
byte index = 0; // Index into array; where to store the character

void setup() {

}

void command(string cmd) {
  switch (cmd[0]) {
            case 'l':
              digitalWrite(pinLB, HIGH);
              analogWrite(pinLF, cmd[1]);
              command(cmd.substring(2);
              break;
            case 'L':
              digitalWrite(pinLB, LOW);
              analogWrite(pinLF, cmd[1]);
              command(cmd.substring(2);
              break;
            case 'r':
              digitalWrite(pinLB, LOW);
              analogWrite(pinLF, cmd[1]);

              // TODO: combine two bytes to a short: http://projectsfromtech.blogspot.hu/2013/09/combine-2-bytes-into-int-on-arduino.html
              short timer = cmd[2]+cmd[4];
              break;
            case 'R':
              digitalWrite(pinLB, HIGH);
              analogWrite(pinLF, cmd[1]);
              command(cmd.substring(2);
              break;
            
          }
}

void loop()
{
    while (Serial.available() > 0) // Don't read unless
                                   // there you know there is data
    {
        inChar = Serial.read(); // Read a character
        inData[index] = inChar; // Store it
        index++; // Increment where to write next
        if (inChar == '\n') {
          inData[index] = '\0'; // Null terminate the string
          index = 0;
          //TODO: do the command
          command(inData);
          
        }
        if (index>10) {
          // Command should be shorter - error!
          break;
        }
    }
}

