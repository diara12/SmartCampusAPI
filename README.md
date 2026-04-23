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
