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
int isTurning = 0;
int isRunning = 0;

elapsedMillis timeElapsed; 

int distance()   // measure the distance ahead
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

void advance()     // move forward
{
  digitalWrite(pinLB, LOW);   // right wheel moves forward
  digitalWrite(pinRB, HIGH);  // left wheel moves forward
  analogWrite(pinLF, 255);
  analogWrite(pinRF, 255);
}
void stopp()         // stop
{
  digitalWrite(pinLB, HIGH);
  digitalWrite(pinRB, LOW);
  analogWrite(pinLF, 0);
  analogWrite(pinRF, 0);
}
void right()        // turn right (single wheel)
{
  digitalWrite(pinLB, LOW); // left wheel moves forward
  digitalWrite(pinRB, LOW); // right wheel moves backward
  analogWrite(pinLF, 200);
  analogWrite(pinRF, 200);
}
void left()         // turn left (single wheel)
{
  digitalWrite(pinLB, HIGH); // left wheel moves forward
  digitalWrite(pinRB, HIGH); // right wheel moves backward
  analogWrite(pinLF, 200);
  analogWrite(pinRF, 200);
}
void back()          // move backward
{ digitalWrite(pinLB, HIGH); // motor moves to left rear
  digitalWrite(pinRB, LOW); // motor moves to right rear
  analogWrite(pinLF, 200);
  analogWrite(pinRF, 200);
}

void setup()
{
  Serial.begin(9600);     // define pin for motor output
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

void loop()
{
  /*
  if (timeElapsed > 100 && !isTurning && isRunning) 
  {       
    int dist = distance();
    Serial.println(dist);
    if (dist < 20) {
      stopp();
      isTurning = 0;
      isRunning = 0;
      myservo.write(20);
      delay(500);
      myservo.write(160);
      delay(500);
      myservo.write(90);
    }
    timeElapsed = 0;       // reset the counter to 0 so the counting starts over...
  }
*/
    int val = Serial.read();
    switch (val) {
      case 'U':
        myservo.write(90);
        /*
        if (distance() < 20) {
          stopp();
          isTurning = 0;
          myservo.write(20);
          delay(500);
          myservo.write(160);
          delay(500);
          myservo.write(90);
          break;
        }*/
        advance();
        isRunning = 1;
        delay(300);
        stopp();
        isTurning = 0;
        
        break;
      case 'D':
        back();
        myservo.write(90);
        delay(300);
        stopp();
        isTurning = 0;
        isRunning = 0;
        break;
      case 'L':
        left();
        myservo.write(130);
        isTurning = 1;
        isRunning = 0;
        delay(100);
        stopp();
        isTurning = 0;
        break;
      case 'R':
        myservo.write(50);
        right();
        isTurning = 1;
        isRunning = 0;
        delay(100);
        stopp();
        isTurning = 0;
        
        break;
      case 'S':
        myservo.write(90);
        stopp();
        isTurning = 0;
        isRunning = 0;
        break;
      default:
         break;
    }
}
