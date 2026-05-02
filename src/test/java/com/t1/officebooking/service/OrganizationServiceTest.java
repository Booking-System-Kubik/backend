package com.t1.officebooking.service;

import com.t1.officebooking.model.Organization;
import com.t1.officebooking.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationService organizationService;

    @Test
    void findById_whenMissing_throws() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.findById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_savesNewOrganization() {
        Organization saved = new Organization("Acme");
        saved.setId(5L);
        when(organizationRepository.save(any(Organization.class))).thenReturn(saved);

        Organization result = organizationService.create("Acme");

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("Acme");
    }

    @Test
    void getAll_delegatesToRepository() {
        when(organizationRepository.findAll()).thenReturn(List.of(new Organization("A")));

        assertThat(organizationService.getAll()).hasSize(1);
        verify(organizationRepository).findAll();
    }
}
