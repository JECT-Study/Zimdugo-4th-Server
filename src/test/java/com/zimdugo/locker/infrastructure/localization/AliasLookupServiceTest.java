package com.zimdugo.locker.infrastructure.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.zimdugo.locker.infrastructure.LockerAliasRepository;
import com.zimdugo.locker.infrastructure.PlaceAliasRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AliasLookupServiceTest {

    @Mock
    private PlaceAliasRepository placeAliasRepository;

    @Mock
    private LockerAliasRepository lockerAliasRepository;

    private AliasLookupService service;

    @BeforeEach
    void setUp() {
        service = new AliasLookupService(placeAliasRepository, lockerAliasRepository);
    }

    @Test
    void normalizesQueryBeforeAliasLookup() {
        when(placeAliasRepository.findByNormalizedAliasContaining("seoulstation")).thenReturn(List.of());

        service.findPlaces(" Ｓｅｏｕｌ Station ");

        verify(placeAliasRepository).findByNormalizedAliasContaining("seoulstation");
    }

    @Test
    void skipsRepositoryLookupForBlankQuery() {
        assertThat(service.findLockers(" \t ")).isEmpty();

        verifyNoInteractions(lockerAliasRepository);
    }
}
