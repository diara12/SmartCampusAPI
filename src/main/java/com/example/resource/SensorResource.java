/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.resource;

/**
 *
 * @author Dii
 */

//import com.example.exception.LinkedResourceNotFoundException;
//import com.example.exception.SensorNotFoundException;
import com.example.model.Sensor;
import com.example.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // GET /api/v1/sensors - get all sensors (with optional type filter)
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(DataStore.sensors.values());

        if (type != null && !type.isEmpty()) {
            List<Sensor> filtered = new ArrayList<>();
            for (Sensor s : sensorList) {
                if (s.getType().equalsIgnoreCase(type)) {
                    filtered.add(s);
                }
            }
            return Response.ok(filtered).build();
        }

        return Response.ok(sensorList).build();
    }

    // POST /api/v1/sensors - register a new sensor
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(400).entity("{\"error\":\"Sensor ID is required\"}").build();
        }
        if (!DataStore.rooms.containsKey(sensor.getRoomId())) {
            //throw new LinkedResourceNotFoundException("Room not found: " + sensor.getRoomId());
        }
        DataStore.sensors.put(sensor.getId(), sensor);

        // Add sensor ID to the room's sensor list
        DataStore.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        return Response.status(201).entity(sensor).build();
    }

    // Sub-resource locator for readings
    //@Path("{sensorId}/readings")
    //public SensorReadingResource getReadings(@PathParam("sensorId") String sensorId) {
        //if (!DataStore.sensors.containsKey(sensorId)) {
            //throw new SensorNotFoundException("Sensor not found: " + sensorId);
        //}
        //return new SensorReadingResource(sensorId);
    //}
}