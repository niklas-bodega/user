package com.lasias.hostelbookingbackend.dtos;

public record CustomPrincipal(Long userID,String jwtBearerToken) {
}
