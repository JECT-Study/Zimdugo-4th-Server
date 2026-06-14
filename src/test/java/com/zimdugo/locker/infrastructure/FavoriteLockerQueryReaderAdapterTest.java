package com.zimdugo.locker.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.zimdugo.locker.domain.FavoriteLockerListPage;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.locker.infrastructure.localization.LocalizedLockerContent;
import com.zimdugo.locker.infrastructure.localization.TranslationLookupService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class FavoriteLockerQueryReaderAdapterTest {

    @Mock
    private FavoriteLockerRepository favoriteLockerRepository;

    @Mock
    private TranslationLookupService translationLookupService;

    @Mock
    private FavoriteLockerListQueryProjection firstProjection;

    @Mock
    private FavoriteLockerListQueryProjection secondProjection;

    @InjectMocks
    private FavoriteLockerQueryReaderAdapter adapter;

    @Test
    void resolvesFavoriteLockerTranslationsInSingleBatch() {
        PageRequest pageable = PageRequest.of(0, 20);
        given(firstProjection.getLockerId()).willReturn(10L);
        given(secondProjection.getLockerId()).willReturn(11L);
        given(favoriteLockerRepository.findFavoriteLockers(1L, 37.55, 126.98, pageable))
            .willReturn(new PageImpl<>(List.of(firstProjection, secondProjection), pageable, 2));
        given(translationLookupService.resolveLockers(List.of(10L, 11L))).willReturn(Map.of(
            10L, content("Myeongdong Station Exit 3 B1"),
            11L, content("Seoul Station Exit 1 2F")
        ));

        FavoriteLockerListPage result = adapter.findAll(1L, 37.55, 126.98, 0, 20);

        assertThat(result.items()).extracting(item -> item.lockerName())
            .containsExactly("Myeongdong Station Exit 3 B1", "Seoul Station Exit 1 2F");
        verify(translationLookupService).resolveLockers(List.of(10L, 11L));
    }

    private LocalizedLockerContent content(String name) {
        return new LocalizedLockerContent(name, "Seoul", null, SupportedLanguage.ENGLISH);
    }
}
