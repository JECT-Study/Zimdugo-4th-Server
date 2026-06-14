package com.zimdugo.locker.infrastructure.search;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Document(indexName = "locker_suggest", createIndex = false)
@Setting(settingPath = "/elasticsearch/locker-suggest-settings.json")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerSuggestDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long lockerId;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(
                suffix = "autocomplete",
                type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "suggest_search_analyzer"
            ),
            @InnerField(suffix = "keyword", type = FieldType.Keyword),
            @InnerField(suffix = "ko", type = FieldType.Text, analyzer = "nori_analyzer"),
            @InnerField(suffix = "en", type = FieldType.Text, analyzer = "english_analyzer"),
            @InnerField(suffix = "ja", type = FieldType.Text, analyzer = "kuromoji_analyzer"),
            @InnerField(suffix = "zh", type = FieldType.Text, analyzer = "smartcn_analyzer")
        }
    )
    private String lockerName;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(
                suffix = "autocomplete",
                type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "suggest_search_analyzer"
            )
        }
    )
    private String lockerNameDecomposed;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(
                suffix = "autocomplete",
                type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "suggest_search_analyzer"
            ),
            @InnerField(suffix = "ko", type = FieldType.Text, analyzer = "nori_analyzer"),
            @InnerField(suffix = "en", type = FieldType.Text, analyzer = "english_analyzer"),
            @InnerField(suffix = "ja", type = FieldType.Text, analyzer = "kuromoji_analyzer"),
            @InnerField(suffix = "zh", type = FieldType.Text, analyzer = "smartcn_analyzer")
        }
    )
    private List<String> lockerSearchNames;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(
                suffix = "autocomplete",
                type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "suggest_search_analyzer"
            )
        }
    )
    private List<String> lockerSearchNamesDecomposed;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(
                suffix = "autocomplete",
                type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "suggest_search_analyzer"
            )
        }
    )
    private String roadAddress;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(
                suffix = "autocomplete",
                type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "suggest_search_analyzer"
            )
        }
    )
    private String roadAddressDecomposed;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(
                suffix = "autocomplete",
                type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "suggest_search_analyzer"
            )
        }
    )
    private List<String> searchAddresses;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(
                suffix = "autocomplete",
                type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "suggest_search_analyzer"
            )
        }
    )
    private List<String> searchAddressesDecomposed;

    @Field(type = FieldType.Keyword)
    private String lockerType;

    @Field(type = FieldType.Keyword)
    private String indoorOutdoorType;

    @Field(type = FieldType.Keyword)
    private List<String> lockerSize;

    @Field(type = FieldType.Integer)
    private Integer minPrice;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Long)
    private Long placeId;

    @Field(type = FieldType.Integer)
    private int lockerCount;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(
                suffix = "autocomplete",
                type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "suggest_search_analyzer"
            ),
            @InnerField(suffix = "keyword", type = FieldType.Keyword),
            @InnerField(suffix = "ko", type = FieldType.Text, analyzer = "nori_analyzer"),
            @InnerField(suffix = "en", type = FieldType.Text, analyzer = "english_analyzer"),
            @InnerField(suffix = "ja", type = FieldType.Text, analyzer = "kuromoji_analyzer"),
            @InnerField(suffix = "zh", type = FieldType.Text, analyzer = "smartcn_analyzer")
        }
    )
    private String placeName;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(
                suffix = "autocomplete",
                type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "suggest_search_analyzer"
            )
        }
    )
    private String placeNameDecomposed;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(
                suffix = "autocomplete",
                type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "suggest_search_analyzer"
            ),
            @InnerField(suffix = "ko", type = FieldType.Text, analyzer = "nori_analyzer"),
            @InnerField(suffix = "en", type = FieldType.Text, analyzer = "english_analyzer"),
            @InnerField(suffix = "ja", type = FieldType.Text, analyzer = "kuromoji_analyzer"),
            @InnerField(suffix = "zh", type = FieldType.Text, analyzer = "smartcn_analyzer")
        }
    )
    private List<String> placeSearchNames;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(
                suffix = "autocomplete",
                type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "suggest_search_analyzer"
            )
        }
    )
    private List<String> placeSearchNamesDecomposed;

    @GeoPointField
    private GeoPoint location;

    @GeoPointField
    private GeoPoint placeLocation;

    @Field(type = FieldType.Object)
    private Map<String, String> localizedLockerNames;

    @Field(type = FieldType.Object)
    private Map<String, String> localizedPlaceNames;

    @Field(type = FieldType.Object)
    private Map<String, String> localizedRoadAddresses;
}
