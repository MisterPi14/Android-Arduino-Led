char Incoming_value = 0;
bool connected = false;

void setup() {
  Serial.begin(9600);         
  pinMode(13, OUTPUT);       
}

void loop() {
  if (!connected) {
    checkForConnection();
  }

  if (connected && Serial.available() > 0) {
    Incoming_value = Serial.read();
    Serial.print("Valor recibido: ");
    Serial.println(Incoming_value);
    if (Incoming_value == '1')
      digitalWrite(13, HIGH);
    else if (Incoming_value == '0')
      digitalWrite(13, LOW);
  }
}

void checkForConnection() {
  if (Serial.available() > 0) {
    if (Serial.read() == 'A') { // Señal de conexión desde la app
      connected = true;
      Serial.println("Conexión establecida.");
    }
  }
}
