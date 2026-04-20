/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.resource;

/**
 *
 * @author Dii
 */

//import com.example.exception.RoomNotFoundException;
//import com.example.exception.RoomNotEmptyException;
import com.example.model.Room;
import com.example.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // GET /api/v1/rooms - list all rooms
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(DataStore.rooms.values());
        return Response.ok(roomList).build();
    }

    // POST /api/v1/rooms - create a new room
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(400).entity("{\"error\":\"Room ID is required\"}").build();
        }
        if (DataStore.rooms.containsKey(room.getId())) {
            return Response.status(409).entity("{\"error\":\"Room already exists\"}").build();
        }
        DataStore.rooms.put(room.getId(), room);
        return Response.status(201)
                .entity(room)
                .build();
    }

    // GET /api/v1/rooms/{roomId} - get a specific room
    @GET
    @Path("{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            //throw new RoomNotFoundException("Room not found: " + roomId);
        }
        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId} - delete a room
    @DELETE
    @Path("{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            //throw new RoomNotFoundException("Room not found: " + roomId);
        }
        if (!room.getSensorIds().isEmpty()) {
            //throw new RoomNotEmptyException("Room " + roomId + " still has sensors assigned.");
        }
        DataStore.rooms.remove(roomId);
        return Response.noContent().build();
    }
}
