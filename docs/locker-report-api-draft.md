# 보관함 제보 API 초안

## 범위

이 문서는 신규 보관함 제보 흐름의 API 초안이다.
정확해요/부정확해요 투표 기능은 포함하지 않는다.

## 아직 확인할 것

- `lockers.location` 컬럼이 어떻게 생성되고 갱신되는지 확인해야 한다.
- 좌표를 주소로 변환하는 책임이 프론트, 지도 백엔드, 제보 백엔드 중 어디에 있는지 확인해야 한다.
- 신규 `locker` 생성 시 `name`, `roadAddress`, `latitude`, `longitude` 외에 필요한 필드가 있는지 확인해야 한다.

## 중복 후보 조회 API

```http
GET /api/v1/locker-reports/duplicates?lat=37.556&lng=126.923&radius=30
```

신규 제보 위치 기준으로 반경 내 기존 보관함 후보를 조회한다.

응답 예시:

```json
{
  "code": "S200",
  "message": "common.ok",
  "status": 200,
  "data": [
    {
      "lockerId": 1,
      "name": "홍대입구역 보관함",
      "roadAddress": "서울 마포구 양화로 160",
      "latitude": 37.556,
      "longitude": 126.923,
      "distanceMeters": 12
    }
  ]
}
```

## 제보 등록 API

```http
POST /api/v1/locker-reports
Authorization: Bearer {accessToken}
Content-Type: application/json
```

신규 보관함 제보를 등록한다.
로그인 사용자의 `userId`와 생성 또는 선택된 `lockerId`를 제보 이력에 연결한다.

요청 예시:

```json
{
  "duplicateHandlingType": "CREATE_NEW",
  "existingLockerId": null,
  "name": "홍대입구역 보관함",
  "roadAddress": null,
  "detailLocation": null,
  "buildingName": "홍대입구역",
  "floor": null,
  "indoorOutdoorType": null,
  "lockerType": "UNKNOWN",
  "sizeInfo": null,
  "priceInfo": null,
  "operatingHours": null,
  "imageUrl": null,
  "latitude": 37.556,
  "longitude": 126.923
}
```

응답 예시:

```json
{
  "code": "S200",
  "message": "common.ok",
  "status": 200,
  "data": {
    "reportId": 10,
    "lockerId": 1,
    "name": "홍대입구역 보관함",
    "roadAddress": null,
    "latitude": 37.556,
    "longitude": 126.923,
    "reportStatus": "COMPLETED"
  }
}
```

## 검증 초안

- `name`, `latitude`, `longitude`는 필수다.
- `lockerType`이 없으면 서버에서 `UNKNOWN`으로 저장한다.
- `imageUrl`, `detailLocation`, `floor`, `priceInfo`, `sizeInfo`, `operatingHours`는 선택값이다.
- `duplicateHandlingType`이 `ADD_TO_EXISTING`이면 `existingLockerId`가 필요하다.
