package com.campus.dto.response;

import lombok.Data;

@Data
public class EventJoinResponseDTO {
    private Integer currentParticipants;
    private Integer maxParticipants;
}