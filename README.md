# springapp

installation:

- rabbitmq
- MongoDB server
- JBoss/Wildfly (default settings)
- git clone && mvn clean package


in order to fill the database, one or more POST-requests should be send:

http://localhost:8080/espring/persons   #(POST)
  {"firstName":"max","lastName":"plank","dateOfBirth":"1982-05-01"}

http://localhost:8080/espring/persons   #(POST)
  {"firstName":"max2","lastName":"plank","dateOfBirth":"1982-05-10"}

http://localhost:8080/espring/persons   #(POST)
  {"firstName":"max3","lastName":"plank","dateOfBirth":"1982-06-30"}


- (REST)

http://localhost:8080/espring/persons/init?byMonth=5   #(GET)

http://localhost:8080/espring/persons/birthdays?jobId=b878515a-f096-49c9-bdf0-19b938a1560a   #(GET)


- (WEBSOCKET)

http://localhost:8080/espring/

connect -> send (result will be printed when it will be ready)
