package com.lasias.hostelbookingbackend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomTypeServiceTest {
    @Mock
    RoomTypeRepository roomTypeRepository;
    @InjectMocks
    RoomTypeService roomTypeService;

    @BeforeEach
    void setUp() {
        roomTypeService = new RoomTypeService(roomTypeRepository);
    }

    private List<RoomType> roomTypesToAdd() {
        return List.of(
                RoomType.builder()
                        .name("The Bodega Luxury Suite")
                        .type("Lux Suite")
                        .description("Our crown jewel. A sprawling suite with panoramic Mediterranean views, a private terrace, and bespoke handcrafted furnishings.")
                        .price(480.0)
                        .size(85)
                        .capacity(2)
                        .extraBedAvailable(true)
                        .badge(RoomBadge.SUITE)
                        .featured(true)
                        .imageUrl("https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&q=80&w=1200")
                        .build(),
                RoomType.builder()
                        .name("Classic Single")
                        .type("Single Room")
                        .description("A cozy, thoughtfully appointed room for solo travellers. Warm stone walls, soft linens, and everything you need for a restful stay.")
                        .price(95.0)
                        .size(22)
                        .capacity(1)
                        .extraBedAvailable(false)
                        .badge(RoomBadge.STANDARD)
                        .featured(false)
                        .imageUrl("https://images.unsplash.com/photo-1631049552057-403cdb8f0658?auto=format&fit=crop&q=80&w=800")
                        .build(),
                RoomType.builder()
                        .name("Classic Double")
                        .type("Double Room")
                        .description("Perfect for couples. A generous room with a king-size bed, ensuite bathroom, and warm Mediterranean light flooding through large windows.")
                        .price(150.0)
                        .size(35)
                        .capacity(2)
                        .extraBedAvailable(true)
                        .badge(RoomBadge.STANDARD)
                        .featured(false)
                        .imageUrl("https://images.unsplash.com/photo-1616594039964-ae9021a400a0?auto=format&fit=crop&q=80&w=800")
                        .build(),
                RoomType.builder()
                        .name("Family Suite")
                        .type("Family Suite")
                        .description("Spacious and welcoming, with two bedrooms and a shared living area. Designed for families who refuse to compromise on comfort.")
                        .price(280.0)
                        .size(60)
                        .capacity(4)
                        .extraBedAvailable(true)
                        .badge(RoomBadge.PREMIUM)
                        .featured(false)
                        .imageUrl("https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&q=80&w=800")
                        .build());
    }


    @Test
    void getAllRoomTypes () {
        List<RoomType> mockRoomTypes = roomTypesToAdd();
        when(roomTypeRepository.findAll()).thenReturn(mockRoomTypes);
        assertEquals(4, roomTypeService.getAllRoomTypes().size());
    }

    @Test
    void getRoomTypeById () {
        List<RoomType> mockRoomTypes = roomTypesToAdd();
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(mockRoomTypes.get(0)));
        when(roomTypeRepository.findById(2L)).thenReturn(Optional.of(mockRoomTypes.get(1)));
        assertEquals("The Bodega Luxury Suite", roomTypeService.getRoomTypeById(1L).getName());
        assertEquals("Classic Single", roomTypeService.getRoomTypeById(2L).getName());
    }

    @Test
    void shouldReturnAvailableRoomTypesAsDTOs() {
        // given
        LocalDateTime checkIn = LocalDateTime.of(2026, 6, 1, 13, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 6, 5, 11, 0);
        int guests = 2;

        RoomType roomType1 = new RoomType();
        roomType1.setId(1L);
        roomType1.setName("Single");

        RoomType roomType2 = new RoomType();
        roomType2.setId(2L);
        roomType2.setName("Double");

        List<RoomType> mockResult = List.of(roomType1, roomType2);

        when(roomTypeRepository.findAllByAvailability(checkIn, checkOut, guests))
                .thenReturn(mockResult);

        // when
        LocalDate checkInDate = LocalDate.of(2026, 6, 1);
        LocalDate checkOutDate = LocalDate.of(2026, 6, 5);
        List<AvailableRoomsDTO> result =
                roomTypeService.getAllRoomTypesByAvailability(checkInDate, checkOutDate, guests);

        // then
        assertEquals(2, result.size());

        verify(roomTypeRepository).findAllByAvailability(checkIn, checkOut, guests);
    }
}