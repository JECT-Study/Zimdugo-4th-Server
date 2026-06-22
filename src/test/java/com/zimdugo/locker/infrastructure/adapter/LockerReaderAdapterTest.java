package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.publication.PublicationStatus;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockerReaderAdapterTest {

    @Mock
    private LockerRepository lockerRepository;

    @Test
    void considersOnlyActiveLockerAsExisting() {
        LockerReaderAdapter adapter = new LockerReaderAdapter(lockerRepository);
        given(lockerRepository.existsByIdAndPublicationStatus(1L, PublicationStatus.ACTIVE))
            .willReturn(true);

        boolean exists = adapter.existsById(1L);

        assertThat(exists).isTrue();
        verify(lockerRepository, never()).existsById(1L);
    }
}
