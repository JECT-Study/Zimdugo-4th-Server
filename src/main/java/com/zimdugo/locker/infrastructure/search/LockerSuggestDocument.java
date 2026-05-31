package com.zimdugo.locker.infrastructure.search;

import java.time.LocalDateTime;
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

@Document(indexName = "locker_suggest")
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
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
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

    @Field(type = FieldType.Text)
    private String roadAddress;

    @Field(type = FieldType.Keyword)
    private String lockerType;

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
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
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

    @GeoPointField
    private GeoPoint location;

    @GeoPointField
    private GeoPoint placeLocation;
}
