<h3>Coaless</h3>

A lightweight Java microservice for managing tasks, using JPA/Hibernate, 
HikariCP connection pooling, and a layered architecture. 
Designed as a single microservice that can serve a SPA frontend via RESTful endpoints.

To build: mvn clean test package<p/>
To run: java -jar target/tasks-spa-1.0-SNAPSHOT.jar<p/>
To use: http://localhost:8080/</p>
To test API: http://localhost:8080/swagger-ui/index.html