/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.mapper;

/**
 *
 * @author Dii
 */

import com.example.exception.RoomNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotFoundMapper implements ExceptionMapper<RoomNotFoundException> {

    @Override
    public Response toResponse(RoomNotFoundException ex) {
        return Response.status(404)
                .entity("{\"error\": \"" + ex.getMessage() + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}