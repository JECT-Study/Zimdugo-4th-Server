package com.zimdugo.locker.infrastructure.search;

import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.locker.infrastructure.persistence.LockerAliasRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceAliasRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationRepository;
import com.zimdugo.locker.infrastructure.projection.LockerSuggestIndexQueryProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.AliasData;
import org.springframework.data.elasticsearch.core.index.Settings;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockerSuggestIndexSyncServiceTest {

    @Mock
    private LockerRepository lockerRepository;

    @Mock
    private LockerSuggestSearchRepository lockerSuggestSearchRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private LockerAliasRepository lockerAliasRepository;

    @Mock
    private PlaceAliasRepository placeAliasRepository;

    @Mock
    private LockerTranslationRepository lockerTranslationRepository;

    @Mock
    private PlaceTranslationRepository placeTranslationRepository;

    @Mock
    private IndexOperations entityIndexOperations;

    @Mock
    private IndexOperations aliasIndexOperations;

    @Mock
    private IndexOperations versionedIndexOperations;

    @Mock
    private Settings settings;

    @Mock
    private Document mapping;

    private LockerSuggestIndexSyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new LockerSuggestIndexSyncService(
            lockerRepository,
            lockerSuggestSearchRepository,
            elasticsearchOperations,
            lockerAliasRepository,
            placeAliasRepository,
            lockerTranslationRepository,
            placeTranslationRepository
        );
        lenient().when(elasticsearchOperations.indexOps(LockerSuggestDocument.class)).thenReturn(entityIndexOperations);
        lenient().when(elasticsearchOperations.indexOps(any(IndexCoordinates.class))).thenAnswer(invocation -> {
            IndexCoordinates coordinates = invocation.getArgument(0);
            return "locker_suggest".equals(coordinates.getIndexName())
                ? aliasIndexOperations
                : versionedIndexOperations;
        });
        lenient().when(aliasIndexOperations.exists()).thenReturn(true);
        lenient().when(aliasIndexOperations.getAliasesForIndex("locker_suggest")).thenReturn(Map.of(
            "locker_suggest_v_current", Set.of(writeAlias())
        ));
        lenient().when(lockerTranslationRepository.findByLockerIdIn(any())).thenReturn(List.of());
        lenient().when(lockerAliasRepository.findByLockerIdIn(any())).thenReturn(List.of());
        lenient().when(placeTranslationRepository.findByPlaceIdIn(any())).thenReturn(List.of());
        lenient().when(placeAliasRepository.findByPlaceIdIn(any())).thenReturn(List.of());
    }

    @Test
    @DisplayName("시작 동기화는 새 버전 인덱스를 색인하고 alias를 원자적으로 전환한다")
    void rebuildsVersionedIndexAndSwitchesAliasAtStartup() {
        LockerSuggestIndexQueryProjection projection = projection();
        given(lockerRepository.findAllForSuggestIndex()).willReturn(List.of(projection));
        given(entityIndexOperations.createSettings()).willReturn(settings);
        given(entityIndexOperations.createMapping()).willReturn(mapping);
        given(versionedIndexOperations.create(settings, mapping)).willReturn(true);
        given(versionedIndexOperations.alias(any(AliasActions.class))).willReturn(true);
        given(aliasIndexOperations.getAliasesForIndex("locker_suggest")).willReturn(Map.of(
            "locker_suggest", Set.of()
        ));

        syncService.syncAtStartup();

        verify(elasticsearchOperations).save(any(Iterable.class), any(IndexCoordinates.class));
        verify(lockerSuggestSearchRepository, never()).deleteAll();
        ArgumentCaptor<AliasActions> actions = ArgumentCaptor.forClass(AliasActions.class);
        verify(versionedIndexOperations).alias(actions.capture());
        assertThat(actions.getValue().getActions()).hasSize(2);
        assertThat(actions.getValue().getActions().get(0)).isInstanceOf(AliasAction.RemoveIndex.class);
        assertThat(actions.getValue().getActions().get(1)).isInstanceOf(AliasAction.Add.class);
    }

    @Test
    @DisplayName("새 버전 색인 실패 시 기존 alias를 유지하고 실패한 인덱스만 정리한다")
    void keepsExistingAliasWhenVersionedIndexBuildFails() {
        LockerSuggestIndexQueryProjection projection = projection();
        given(lockerRepository.findAllForSuggestIndex()).willReturn(List.of(projection));
        given(entityIndexOperations.createSettings()).willReturn(settings);
        given(entityIndexOperations.createMapping()).willReturn(mapping);
        given(versionedIndexOperations.create(settings, mapping)).willReturn(true);
        given(versionedIndexOperations.exists()).willReturn(true);
        given(elasticsearchOperations.save(any(Iterable.class), any(IndexCoordinates.class)))
            .willThrow(new IllegalStateException("bulk indexing failed"));

        assertThatThrownBy(syncService::syncAtStartup)
            .isInstanceOf(BusinessException.class);

        verify(versionedIndexOperations, never()).alias(any(AliasActions.class));
        verify(versionedIndexOperations).delete();
    }

    @Test
    @DisplayName("기존 alias는 이전 버전을 유지한 채 새 버전으로 전환한다")
    void switchesExistingAliasWithoutDeletingPreviousVersion() {
        LockerSuggestIndexQueryProjection projection = projection();
        given(lockerRepository.findAllForSuggestIndex()).willReturn(List.of(projection));
        given(entityIndexOperations.createSettings()).willReturn(settings);
        given(entityIndexOperations.createMapping()).willReturn(mapping);
        given(versionedIndexOperations.create(settings, mapping)).willReturn(true);
        given(versionedIndexOperations.alias(any(AliasActions.class))).willReturn(true);
        given(aliasIndexOperations.getAliasesForIndex("locker_suggest")).willReturn(Map.of(
            "locker_suggest_v_previous", Set.of(writeAlias())
        ));

        syncService.syncAtStartup();

        ArgumentCaptor<AliasActions> actions = ArgumentCaptor.forClass(AliasActions.class);
        verify(versionedIndexOperations).alias(actions.capture());
        assertThat(actions.getValue().getActions()).hasSize(2);
        assertThat(actions.getValue().getActions().get(0)).isInstanceOf(AliasAction.Remove.class);
        assertThat(actions.getValue().getActions().get(1)).isInstanceOf(AliasAction.Add.class);
        verify(versionedIndexOperations, never()).delete();
    }

    @Test
    @DisplayName("논리 인덱스가 없으면 제거 작업 없이 신규 alias를 생성한다")
    void createsAliasWithoutRemoveActionWhenLogicalIndexDoesNotExist() {
        given(lockerRepository.findAllForSuggestIndex()).willReturn(List.of());
        given(entityIndexOperations.createSettings()).willReturn(settings);
        given(entityIndexOperations.createMapping()).willReturn(mapping);
        given(versionedIndexOperations.create(settings, mapping)).willReturn(true);
        given(versionedIndexOperations.alias(any(AliasActions.class))).willReturn(true);
        given(aliasIndexOperations.exists()).willReturn(false);

        syncService.syncAtStartup();

        ArgumentCaptor<AliasActions> actions = ArgumentCaptor.forClass(AliasActions.class);
        verify(versionedIndexOperations).alias(actions.capture());
        assertThat(actions.getValue().getActions()).singleElement().isInstanceOf(AliasAction.Add.class);
    }

    @Test
    @DisplayName("장소 부분 재색인은 기존 장소 문서를 삭제하고 다국어 검색 필드로 다시 저장한다")
    void reindexesPlaceWithMultilingualSearchFields() {
        LockerSuggestIndexQueryProjection projection = projection();
        given(lockerRepository.findAllForSuggestIndexByPlaceIds(List.of(101L))).willReturn(List.of(projection));

        LockerTranslationEntity lt = mock(LockerTranslationEntity.class);
        LockerEntity locker = mock(LockerEntity.class);
        given(lt.getLocker()).willReturn(locker);
        given(locker.getId()).willReturn(10L);
        given(lt.getName()).willReturn("Locker A");
        given(lt.getRoadAddress()).willReturn("Seoul Sinchon-ro");
        given(lt.getLanguage()).willReturn(SupportedLanguage.ENGLISH);
        given(lockerTranslationRepository.findByLockerIdIn(any())).willReturn(List.of(lt));

        PlaceTranslationEntity pt1 = mock(PlaceTranslationEntity.class);
        PlaceEntity place = mock(PlaceEntity.class);
        given(pt1.getPlace()).willReturn(place);
        given(place.getId()).willReturn(101L);
        given(pt1.getName()).willReturn("Sinchon Station Exit 1");
        given(pt1.getLanguage()).willReturn(SupportedLanguage.ENGLISH);

        PlaceTranslationEntity pt2 = mock(PlaceTranslationEntity.class);
        given(pt2.getPlace()).willReturn(place);
        given(pt2.getName()).willReturn("Ｓｉｎｃｈｏｎ　Ｓｔａｔｉｏｎ　Ｅｘｉｔ　１");
        given(pt2.getLanguage()).willReturn(SupportedLanguage.KOREAN);
        given(placeTranslationRepository.findByPlaceIdIn(any())).willReturn(List.of(pt1, pt2));

        PlaceAliasEntity pa = mock(PlaceAliasEntity.class);
        given(pa.getPlace()).willReturn(place);
        given(pa.getAlias()).willReturn("신촌 출구");
        given(placeAliasRepository.findByPlaceIdIn(any())).willReturn(List.of(pa));

        syncService.reindexPlaces(List.of(101L, 101L));

        verify(lockerSuggestSearchRepository).deleteByPlaceIdIn(List.of(101L));
        ArgumentCaptor<Iterable<LockerSuggestDocument>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(lockerSuggestSearchRepository).saveAll(captor.capture());
        LockerSuggestDocument document = captor.getValue().iterator().next();
        assertThat(document.getPlaceSearchNames())
            .containsExactly("신촌역1번출구", "sinchonstationexit1", "신촌출구");
        assertThat(document.getLockerSearchNames()).containsExactly("보관함a", "lockera");
        assertThat(document.getSearchAddresses()).containsExactly("서울신촌로", "seoulsinchon-ro");
        assertThat(document.getPlaceSearchNamesDecomposed()).contains("ㅅㅣㄴㅊㅗㄴㅊㅜㄹㄱㅜ");
        assertThat(document.getLockerSize()).containsExactly("SMALL", "MEDIUM", "LARGE");
        assertThat(document.getLocalizedLockerNames()).containsEntry("english", "Locker A");
        assertThat(document.getLocalizedPlaceNames()).containsEntry("english", "Sinchon Station Exit 1");
        assertThat(document.getLocalizedPlaceNames()).containsEntry("korean", "Ｓｉｎｃｈｏｎ　Ｓｔａｔｉｏｎ　Ｅｘｉｔ　１");
        assertThat(document.getLocalizedRoadAddresses()).containsEntry("english", "Seoul Sinchon-ro");
    }

    @Test
    @DisplayName("보관함 부분 재색인은 요청 문서를 제거하고 소속 장소 전체를 다시 색인한다")
    void reindexesLockerAndItsPlace() {
        given(lockerRepository.findPlaceIdsByLockerIds(List.of(10L))).willReturn(List.of(101L));
        given(lockerRepository.findAllForSuggestIndexByPlaceIds(List.of(101L))).willReturn(List.of());

        syncService.reindexLockers(List.of(10L));

        verify(lockerSuggestSearchRepository).deleteAllById(List.of("10"));
        verify(lockerSuggestSearchRepository).deleteByPlaceIdIn(List.of(101L));
        verify(lockerRepository).findAllForSuggestIndexByPlaceIds(eq(List.of(101L)));
    }

    private LockerSuggestIndexQueryProjection projection() {
        LockerSuggestIndexQueryProjection projection =
            mock(LockerSuggestIndexQueryProjection.class);
        given(projection.getLockerId()).willReturn(10L);
        given(projection.getLockerName()).willReturn("보관함 A");
        given(projection.getRoadAddress()).willReturn("서울 신촌로");
        given(projection.getLockerLatitude()).willReturn(37.556);
        given(projection.getLockerLongitude()).willReturn(126.923);
        given(projection.getLockerType()).willReturn("SUBWAY_STATION");
        given(projection.getIndoorOutdoorType()).willReturn("INDOOR");
        given(projection.getLockerSize()).willReturn(" SMALL ,MEDIUM,LARGE,SMALL");
        given(projection.getMinPrice()).willReturn(1000);
        given(projection.getUpdatedAt()).willReturn(LocalDateTime.of(2026, 6, 11, 12, 0));
        given(projection.getPlaceId()).willReturn(101L);
        given(projection.getPlaceName()).willReturn("신촌역 1번 출구");
        given(projection.getPlaceRoadAddress()).willReturn("서울 신촌로");
        return projection;
    }

    private AliasData writeAlias() {
        return AliasData.of("locker_suggest", null, null, null, true, false);
    }
}
