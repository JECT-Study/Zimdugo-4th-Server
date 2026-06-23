package com.zimdugo.locker.application.seo;

import com.zimdugo.locker.application.result.seo.LockerSeoResult;
import com.zimdugo.locker.domain.seo.LockerSeo;
import com.zimdugo.locker.domain.seo.LockerSeoReader;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LockerSeoQueryServiceTest {

    @Mock
    private LockerSeoReader lockerSeoReader;

    @InjectMocks
    private LockerSeoQueryService lockerSeoQueryService;

    @Test
    @DisplayName("활성화된 보관함의 SEO 조회용 최소 목록을 정상적으로 반환한다")
    void returnsLockerSeoList() {
        // given
        List<LockerSeo> expectedList = List.of(
            new LockerSeo(1L, Map.of("ko", "서울역 1번출구", "en", "Seoul Station Exit 1")),
            new LockerSeo(2L, Map.of("ko", "홍대입구역 2번출구"))
        );
        given(lockerSeoReader.readAllForSeo()).willReturn(expectedList);

        // when
        List<LockerSeoResult> results = lockerSeoQueryService.getSeoList();

        // then
        assertThat(results).hasSize(2);

        LockerSeoResult first = results.get(0);
        assertThat(first.lockerId()).isEqualTo(1L);
        assertThat(first.names()).containsEntry("ko", "서울역 1번출구");
        assertThat(first.names()).containsEntry("en", "Seoul Station Exit 1");

        LockerSeoResult second = results.get(1);
        assertThat(second.lockerId()).isEqualTo(2L);
        assertThat(second.names()).containsEntry("ko", "홍대입구역 2번출구");
    }
}
