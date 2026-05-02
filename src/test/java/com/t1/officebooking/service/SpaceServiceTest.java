package com.t1.officebooking.service;

import com.t1.officebooking.dto.request.CreatingSpaceTypeRequest;
import com.t1.officebooking.dto.request.FilteringSpacesRequest;
import com.t1.officebooking.dto.request.PointRequest;
import com.t1.officebooking.model.Floor;
import com.t1.officebooking.model.Location;
import com.t1.officebooking.model.Organization;
import com.t1.officebooking.model.Space;
import com.t1.officebooking.model.SpaceType;
import com.t1.officebooking.repository.FloorRepository;
import com.t1.officebooking.repository.SpaceRepository;
import com.t1.officebooking.repository.SpaceTypeRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock
    private SpaceTypeRepository spaceTypeRepository;
    @Mock
    private SpaceRepository spaceRepository;
    @Mock
    private FloorRepository floorRepository;
    @Mock
    private LocationService locationService;

    @InjectMocks
    private SpaceService spaceService;

    private Location location;

    @BeforeEach
    void setUp() {
        Organization org = new Organization("Org");
        org.setId(1L);
        location = new Location("HQ", "City", "Addr", true,
                java.time.LocalTime.of(9, 0),
                java.time.LocalTime.of(18, 0),
                "Europe/Moscow");
        location.setId(10L);
        location.setOrganization(org);
    }

    @Test
    void getFilteredSpaces_withoutFloor_delegatesToRepositoryWithoutFloor() {
        FilteringSpacesRequest req = new FilteringSpacesRequest();
        req.setLocationId(1L);
        req.setSpaceTypeId(2L);
        req.setFloorNumber(null);

        when(spaceRepository.findByLocationIdAndSpaceTypeId(1L, 2L)).thenReturn(List.of());

        assertThat(spaceService.getFilteredSpaces(req)).isEmpty();
        verify(spaceRepository).findByLocationIdAndSpaceTypeId(1L, 2L);
        verify(spaceRepository, never()).findByLocationIdAndSpaceTypeIdAndFloor(any(), any(), any());
    }

    @Test
    void getFilteredSpaces_withFloor_delegatesToRepositoryWithFloor() {
        FilteringSpacesRequest req = new FilteringSpacesRequest();
        req.setLocationId(1L);
        req.setSpaceTypeId(2L);
        req.setFloorNumber(3);

        when(spaceRepository.findByLocationIdAndSpaceTypeIdAndFloor(1L, 2L, 3)).thenReturn(List.of());

        assertThat(spaceService.getFilteredSpaces(req)).isEmpty();
        verify(spaceRepository).findByLocationIdAndSpaceTypeIdAndFloor(1L, 2L, 3);
    }

    @Test
    void addSpaceType_whenTypeAlreadyExists_throwsEntityExistsException() {
        CreatingSpaceTypeRequest req = new CreatingSpaceTypeRequest();
        req.setLocationId(location.getId());
        req.setType("MEETING_ROOM");
        req.setAllowedDurations(List.of("PT1H"));

        when(locationService.findById(location.getId())).thenReturn(location);
        SpaceType existing = new SpaceType("MEETING_ROOM", List.of("PT1H"), location);
        when(spaceTypeRepository.findByType("MEETING_ROOM")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> spaceService.addSpaceType(req))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining("already exists");
        verify(spaceTypeRepository, never()).save(any());
    }

    @Test
    void spaceTypeExists_delegatesToRepository() {
        SpaceType st = new SpaceType("T", List.of(), location);
        when(spaceTypeRepository.findByType("T")).thenReturn(Optional.of(st));

        assertThat(spaceService.spaceTypeExists(st)).isTrue();
    }

    @Test
    void findSpaceById_whenMissing_throws() {
        when(spaceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> spaceService.findSpaceById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findOrCreateFloor_whenFloorMissingAndPolygonEmpty_throws() {
        when(floorRepository.findByLocationIdAndFloorNumber(10L, 2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> spaceService.findOrCreateFloor(10L, 2, List.of()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("polygon");
    }

    @Test
    void findOrCreateFloor_whenFloorExists_returnsWithoutSavingNewFloor() {
        Floor floor = new Floor(location, 2, List.of());
        floor.setId(5L);
        when(floorRepository.findByLocationIdAndFloorNumber(10L, 2)).thenReturn(Optional.of(floor));

        PointRequest p = new PointRequest();
        p.setX(0);
        p.setY(0);
        Floor result = spaceService.findOrCreateFloor(10L, 2, List.of(p));

        assertThat(result).isSameAs(floor);
        verify(floorRepository, never()).save(any());
    }
}
