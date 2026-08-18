package com.lasias.hostelbookingbackend.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void getRoomsById_shouldReturnRoomDTO_whenRoomExists() {
        // given
        Long roomId = 1L;

        RoomType roomType = new RoomType();

        RoomEntity room = RoomEntity.builder()
                .id(roomId)
                .roomNumber(101L)
                .roomType(roomType)
                .extraBed(true)
                .build();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // when
        RoomDTO result = roomService.getRoomsById(roomId);

        // then
        assertNotNull(result);
        assertEquals(roomId, result.getId());
        assertEquals(101L, result.getRoomNumber());
        assertEquals(roomType, result.getRoomType());
        assertTrue(result.isExtraBed());

        verify(roomRepository).findById(roomId);
    }

    @Test
    void getRoomsById_shouldThrowException_whenRoomNotFound() {
        // given
        Long roomId = 99L;

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RoomNotFoundException.class, () ->
                roomService.getRoomsById(roomId)
        );

        verify(roomRepository).findById(roomId);
    }

    @Test
    void getRooms_shouldReturnListOfRoomDTOs() {
        // given
        RoomType type1 = new RoomType();
        RoomType type2 = new RoomType();

        RoomEntity room1 = RoomEntity.builder()
                .id(1L)
                .roomNumber(101L)
                .roomType(type1)
                .extraBed(false)
                .build();

        RoomEntity room2 = RoomEntity.builder()
                .id(2L)
                .roomNumber(102L)
                .roomType(type2)
                .extraBed(true)
                .build();

        when(roomRepository.findAll()).thenReturn(List.of(room1, room2));

        // when
        List<RoomDTO> result = roomService.getRooms();

        // then
        assertEquals(2, result.size());

        assertEquals(101L, result.get(0).getRoomNumber());
        assertFalse(result.get(0).isExtraBed());

        assertEquals(102L, result.get(1).getRoomNumber());
        assertTrue(result.get(1).isExtraBed());

        verify(roomRepository).findAll();
    }
}