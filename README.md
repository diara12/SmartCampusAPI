# Smart Campus API

A RESTful API built with JAX-RS (Jersey) for managing Rooms and Sensors across a university campus. The API supports full CRUD operations, sensor reading history, filtered queries, and robust error handling.

## API Design Overview

The API follows RESTful principles with a versioned base path of `/api/v1`. It is built using JAX-RS with Jersey as the implementation, deployed on Apache Tomcat, and uses in-memory data structures for storage.

### Resource Hierarchy

```
/api/v1/                        → Discovery endpoint
/api/v1/rooms                   → Room collection
/api/v1/rooms/{roomId}          → Individual room
/api/v1/sensors                 → Sensor collection
/api/v1/sensors?type={type}     → Filtered sensor list
/api/v1/sensors/{sensorId}/readings → Sensor reading history (sub-resource)
```

### Core Entities

- **Room** - Represents a physical room on campus with an ID, name, capacity, and a list of sensor IDs
- **Sensor** - Represents a sensor deployed in a room with an ID, type, status, current value, and room reference
- **SensorReading** - Represents a historical reading captured by a sensor with an ID, timestamp, and value

### Error Handling

| Exception | HTTP Status | Scenario |
|---|---|---|
| RoomNotFoundException | 404 Not Found | Room ID does not exist |
| RoomNotEmptyException | 409 Conflict | Deleting a room that still has sensors |
| LinkedResourceNotFoundException | 422 Unprocessable Entity | Sensor references a non-existent room |
| SensorUnavailableException | 403 Forbidden | Posting a reading to a MAINTENANCE sensor |
| GlobalExceptionMapper | 500 Internal Server Error | Any unexpected runtime error |

---

## How to Build and Run

### Prerequisites

- Java JDK 8 or higher
- Apache Maven 3.6+
- Apache Tomcat 9
- NetBeans IDE

### Step 1: Clone the Repository

```bash
git clone https://github.com/diara12/SmartCampusAPI.git
cd SmartCampusAPI
```

### Step 2: Build the Project

```bash
mvn clean install
```

This will compile the project and generate a `.war` file in the `target/` directory.

### Step 3: Deploy to Tomcat

**Using NetBeans:**
1. Open the project in NetBeans
2. Right-click the project → **Run**
3. NetBeans will automatically deploy to Tomcat

### Step 4: Verify the Server is Running

Open your browser and go to:
```
http://localhost:8080/SmartCampusAPI/api/v1/
```

You should see a JSON response with API metadata.

---

## Sample curl Commands

### 1. Discovery Endpoint — GET /api/v1/
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/
```
Expected response:
```json
{
    "version": "1.0",
    "name": "Smart Campus API",
    "contact": "admin@smartcampus.com",
    "resources": {
        "rooms": "/api/v1/rooms",
        "sensors": "/api/v1/sensors"
    }
}
```

### 2. Create a Room — POST /api/v1/rooms
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"LIB-301\", \"name\": \"Library Quiet Study\", \"capacity\": 50}"
```
Expected response: `201 Created` with the created room object.

### 3. Register a Sensor — POST /api/v1/sensors
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"TEMP-001\", \"type\": \"Temperature\", \"status\": \"ACTIVE\", \"currentValue\": 22.5, \"roomId\": \"LIB-301\"}"
```
Expected response: `201 Created` with the created sensor object.

### 4. Filter Sensors by Type — GET /api/v1/sensors?type=Temperature
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Temperature"
```
Expected response: A JSON array containing only sensors of type `Temperature`.

### 5. Add a Sensor Reading — POST /api/v1/sensors/{sensorId}/readings
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\": 24.5}"
```
Expected response: `201 Created` with the new reading. Also updates the sensor's `currentValue` to `24.5`.

### 6. Get Reading History — GET /api/v1/sensors/{sensorId}/readings
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings
```
Expected response: A JSON array of all historical readings for that sensor.

### 7. Delete a Room with Sensors (409 Error) — DELETE /api/v1/rooms/{roomId}
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```
Expected response: `409 Conflict` because the room still has sensors assigned.

### 8. Register Sensor with Invalid Room (422 Error) — POST /api/v1/sensors
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"CO2-001\", \"type\": \"CO2\", \"status\": \"ACTIVE\", \"currentValue\": 0.0, \"roomId\": \"FAKE-ROOM\"}"
```
Expected response: `422 Unprocessable Entity` because `FAKE-ROOM` does not exist.

---

## Report — Answers to Coursework Questions
 
### Part 1.1 — JAX-RS Resource Lifecycle
 
By default, JAX-RS creates a new instance of each resource class for every incoming HTTP request. This means instance variables are not shared between requests and cannot be used to store persistent data — any data stored as an instance variable would be lost as soon as the request completes. 

This has a direct impact on how in-memory data must be managed. Since each request receives its own resource object, all shared data must be stored in a static, application-wide data structure held in a separate DataStore class. In this implementation, a ConcurrentHashMap is used to hold rooms, sensors, and readings. 

Furthermore, because Tomcat handles multiple requests concurrently across different threads, a standard HashMap would be vulnerable to race conditions where two threads attempt to read and write simultaneously, causing data corruption or loss. Using ConcurrentHashMap ensures that all read and write operations are thread-safe, preventing these issues without requiring manual synchronisation blocks. 
 
---
 
### Part 1.2 — HATEOAS and Hypermedia in RESTful APIs
 
HATEOAS is considered a hallmark of advanced RESTful design because it makes APIs self documenting and navigable. Rather than requiring clients to have prior knowledge of all available URLs, a HATEOAS-compliant API embeds links to related resources directly within its responses. 

For example, when a client retrieves a room, the response might include links to that room's sensors, the delete endpoint, and the update endpoint. This means client developers do not need to rely on static external documentation to discover available actions — the API itself guides them. This reduces coupling between the client and server, makes the API easier to explore, and ensures that if URLs change, clients following embedded links adapt automatically rather than breaking. 

Compared to static documentation, which can become outdated and requires clients to be updated manually whenever the API changes, HATEOAS-driven navigation keeps the client and server loosely coupled and resilient to change.
 
---
 
### Part 2.1 — Returning IDs vs Full Objects in List Responses
 
Returning only IDs produces a very lightweight response, which reduces bandwidth usage and server processing time. However, it forces the client to make a separate GET request for each ID to retrieve full details which can significantly increase total latency and the number of HTTP round trips, especially for large collections. 

Returning full room objects in the list response increases the payload size but gives the client all the information it needs in a single request, reducing latency and simplifying client-side logic. For the Smart Campus API, returning full objects is more appropriate since campus management systems need complete room details to function effectively and performance is better served by fewer, larger responses than many small ones.  
 
---
 
### Part 2.2 — Idempotency of the DELETE Operation
 
Yes, the DELETE operation is idempotent in this implementation. Idempotency means that making the same request multiple times produces the same server state as making it once. 

In this API, the first DELETE request for a room that exists and has no sensors will successfully remove it and return 204 No Content. If the exact same DELETE request is sent again, the room no longer exists and the server returns 404 Not Found. While the HTTP status code differs between the first and subsequent calls, the server state remains the same — the room is absent. This satisfies the definition of idempotency, which focuses on the resulting state rather than 
the response code. 
 
---
 
### Part 3.1 — @Consumes Annotation and Content-Type Mismatches
 
The @Consumes(MediaType.APPLICATION_JSON) annotation instructs JAX-RS to only accept requests where the Content-Type header is application/json. If a client sends a request with a different content type, such as text/plain or application/xml, JAX-RS will automatically reject the request and return an HTTP 415 Unsupported Media Type response before the method body is even executed. 

This is handled entirely by the JAX-RS runtime so no manual checking is required in the resource method. The runtime inspects the incoming Content-Type header, compares it against the @Consumes declaration, and if there is a mismatch, it short-circuits the request processing and returns the 415 error immediately. This ensures that the API only processes data in the expected format, preventing malformed or unexpected input from reaching the business logic layer. 
 
---
 
### Part 3.2 — Query Parameters vs Path Parameters for Filtering
 
Using @QueryParam for filtering (e.g., GET /api/v1/sensors?type=CO2) is considered superior to embedding the filter in the path (e.g., /api/v1/sensors/type/CO2) for several reasons. 

Query parameters are optional by nature, meaning the same endpoint can serve both filtered and unfiltered requests without requiring separate methods or URL patterns. Path parameters imply a fixed resource hierarchy, suggesting that 'type' is a distinct named resource rather than a search criterion, which is semantically incorrect. 

Additionally, query parameters compose easily and multiple filters can be added naturally (e.g., ?type=CO2&status=ACTIVE) without changing the URL structure, whereas path-based filtering becomes unwieldy and requires additional endpoints for each filter combination. Query parameters also follow the established web convention for search and filtering operations, making the API more intuitive for developers. 
 
---
 
### Part 4.1 — Sub-Resource Locator Pattern
 
The Sub-Resource Locator pattern allows a resource method to delegate request handling to a separate dedicated class rather than defining all nested paths in a single controller. In this API, SensorResource delegates /sensors/{sensorId}/readings to a dedicated SensorReadingResource class via a locator method that returns an instance of that class. 

This approach significantly reduces complexity in large APIs. Without delegation, a single resource class would need to handle every possible nested path, growing into an excessively large and unmanageable class with hundreds of methods, all tightly coupled and difficult to test or maintain. By separating concerns into focused classes, each class has a single responsibility and SensorResource manages sensors, SensorReadingResource manages readings, making the codebase easier to read, understand, and test independently. 

New nested resources can be added by creating new classes rather than modifying existing ones, adhering to the Open/Closed Principle of software design. It also enables different teams to work on different resource classes simultaneously without merge conflicts, which is a significant benefit in real-world large-scale API development. 

 
---
 
### Part 5.2 — HTTP 422 vs HTTP 404 for Missing References
 
HTTP 404 Not Found indicates that the requested URL does not exist on the server. HTTP 422 Unprocessable Entity indicates that the server understood the request, the URL is valid, and the request was well-formed, but the data inside the request body contains a semantic error that prevents processing. 

When a client attempts to register a sensor with a roomId that does not exist, the URL /api/v1/sensors is perfectly valid, the endpoint exists and is reachable. The problem is with the content of the request body, not the endpoint itself. Returning 404 would mislead the client into thinking the sensors endpoint does not exist, causing confusion and incorrect error handling on the client side. 

Returning 422 accurately communicates that the request was well-formed and reached the correct endpoint, but the referenced resource (roomId) could not beresolved within the system, allowing the client to correctly identify that they need to correct their data, specifically the roomId value and retry the request.
 
---
 
### Part 5.4 — Security Risks of Exposing Stack Traces
 
Exposing internal Java stack traces to external API consumers presents several serious cybersecurity risks that can significantly aid an attacker in compromising the system. Stack traces reveal internal package and class names, giving attackers a detailed map of the application's internal structure and architecture. They expose library names and exact version numbers, allowing attackers to look up known Common Vulnerabilities and Exposures (CVEs) for those specific versions and craft targeted exploits. They can also reveal file system paths on the server, which aids in directory traversal attacks and helps attackers understand the deployment environment. 

Furthermore, stack traces expose the logic flow and method call sequences within the application, helping attackers understand how data is processed and where the application might be vulnerable to injection attacks, null pointer exploits, or other manipulation techniques. 

By returning a clean, generic 500 Internal Server Error message instead, the Global Exception Mapper ensures that no internal implementation details are ever leaked to potential attackers, following the principle of information minimisation in security design. 
 
---
 
### Part 5.5 — JAX-RS Filters vs Manual Logging
 
Using JAX-RS filters for cross-cutting concerns like logging is far superior to manually inserting Logger.info() statements in every resource method for several important reasons. 

Filters are applied automatically to every request and response without any changes to individual resource methods. This means new endpoints added in the future are automatically covered by the logging behaviour without any developer intervention. Manual logging requires a developer to remember to add log statements to every new method, making it to cause errors and inevitably inconsistent, some endpoints will be logged and others will not, creating gaps in observability. 

Filters also enforce the separation of concerns principle and resource methods focus purely on business logic while the filter handles cross-cutting concerns such as observability, authentication, and CORS. This keeps the codebase clean and each component focused on a single responsibility. Additionally, filters can be enabled, disabled, or modified centrally in one place, making it easy to adjust logging behaviour across the entire API with a single change rather than modifying dozens of individual methods across multiple classes.
 
