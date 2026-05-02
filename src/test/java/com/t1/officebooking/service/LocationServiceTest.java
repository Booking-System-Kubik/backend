package com.t1.officebooking.service;

import com.t1.officebooking.dto.request.CreatingLocationRequest;
import com.t1.officebooking.model.Location;
import com.t1.officebooking.model.Organization;
import com.t1.officebooking.repository.LocationRepository;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private UserService userService;

    @InjectMocks
    private LocationService locationService;

    @Test
    void addLocation_whenDuplicate_throwsEntityExists() {
        CreatingLocationRequest req = new CreatingLocationRequest();
        req.setName("HQ");
        req.setCity("City");
        req.setAddress("Street 1");
        req.setIsActive(true);
        req.setWorkDayStart(LocalTime.of(9, 0));
        req.setWorkDayEnd(LocalTime.of(18, 0));
        req.setTimeZone("UTC");
        req.setOrganizationId(3L);

        Organization org = new Organization("O");
        org.setId(3L);
        when(organizationService.findById(3L)).thenReturn(org);
        when(locationRepository.existsByCityAndAddressAndOrganization_Id("City", "Street 1", 3L))
                .thenReturn(true);

        assertThatThrownBy(() -> locationService.addLocation(req))
                .isInstanceOf(EntityExistsException.class);

        verify(locationRepository, never()).save(any());
    }
}
