#include <Servo.h>
#include <elapsedMillis.h>

const String firmwareVersion  = "robotCar.base.1"; 
int pinLB = 12;     // define pin 12
int pinLF = 3;     // define pin 3
int pinRB = 13;    // define pin 13
int pinRF = 11;    // define pin 11
////////////////////////////////
int inputPin = 4;    // define pin for sensor echo
int outputPin = 5;   // define pin for sensor trig

Servo myservo;        // set myservo

elapsedMillis timeElapsed;
short timer;

union shortbyte {
    unsigned short ss; 
    byte bb[2];
  } sb;
  
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
  timer = -1;
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


void SerialWriteBytes(byte mess[], int llength) {
  Serial.println();
  for(int i=0;i<llength;i++){
    // Serial.write(mess[i]);
    Serial.println(String(i)+": "+(char) mess[i] + " "+ String((byte) mess[i]));
  }
}

void command(byte cmd[]) {
  /*Serial.print("-SubCommand: ");
  SerialWriteBytes(cmd, 10);
  Serial.println();
  */
  byte temp[4];

  // TODO: motor commands should happen at once!
  
  switch (cmd[0]) {
    case 'V':
      Serial.println("@V@"+firmwareVersion);
      break;
    case 'l':
      digitalWrite(pinLB, HIGH);
      analogWrite(pinLF, cmd[1]);
      temp[0] = cmd[2];
      temp[1] = cmd[3];
      temp[2] = cmd[4];
      temp[3] = cmd[5];

      command(temp);
      break;
    case 'L':
      digitalWrite(pinLB, LOW);
      analogWrite(pinLF, cmd[1]);
      temp[0] = cmd[2];
      temp[1] = cmd[3];
      temp[2] = cmd[4];
      temp[3] = cmd[5];
      
      command(temp);
      break;
    case 'r':
      
      digitalWrite(pinRB, LOW);
      analogWrite(pinRF, cmd[1]);
      sb.bb[0] = cmd[2];
      sb.bb[1] = cmd[3];
      timer = sb.ss;
      
      timeElapsed = 0;
      break;
    case 'R':
      digitalWrite(pinRB, HIGH);
      analogWrite(pinRF, cmd[1]);
      sb.bb[0] = cmd[2];
      sb.bb[1] = cmd[3];
      timer = sb.ss;
      
      timeElapsed = 0;
      break;
  }
}
byte inData[20]; // Allocate some space for the string
byte inChar = -1; // Where to store the character read
byte index = 0; // Index into array; where to store the character

void loop()
{
  while (Serial.available() > 0) // Don't read unless
    // there you know there is data
  {
    inChar = Serial.read(); // Read a character
    inData[index] = inChar; // Store it
    index++; // Increment where to write next
    
    if (inChar == '@') {
      index = 0;
      command(inData);

    }
    if (index > 10) {
      // Command should be shorter - error!
      break;
    }
  }

  if (timer >= 0) {
    timer = timer - timeElapsed;
    timeElapsed = 0;
    if (timer < 0) {
      timer = -1;
      // Stop
      analogWrite(pinLF, 0);
      analogWrite(pinRF, 0);
    }  
  }
}

