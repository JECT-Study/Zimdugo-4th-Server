-- ==========================================
-- Zimdugo Database Dummy Data Seed Script
-- ==========================================

-- Clean up existing tables safely (only if they exist)
DO $$
DECLARE
    t_name TEXT;
BEGIN
    FOR t_name IN 
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public' 
          AND table_name IN (
              'users', 'places', 'lockers', 'locker_details', 'locker_translations', 
              'locker_aliases', 'place_translations', 'place_aliases', 'locker_votes', 
              'favorite_lockers', 'locker_reports', 'admin_documents', 'admin_document_sections', 
              'admin_document_translations', 'admin_document_section_translations'
          )
    LOOP
        EXECUTE 'TRUNCATE TABLE public.' || quote_ident(t_name) || ' RESTART IDENTITY CASCADE';
    END LOOP;
END $$;

-- 1. Create 120 Users (100 Active Users, 10 Suspended, 10 Deleted, 2 Admins)
DO $$
BEGIN
    FOR i IN 1..120 LOOP
        INSERT INTO users (email, nickname, profile_image_url, status, role, created_at, updated_at)
        VALUES (
            'user' || i || '@zimdugo.com',
            'ZimduUser_' || i,
            'https://picsum.photos/id/' || (i % 100) || '/200/200',
            CASE WHEN i > 110 THEN 'DELETED'
                 WHEN i > 100 THEN 'INACTIVE'
                 ELSE 'ACTIVE' END,
            CASE WHEN i IN (1, 2) THEN 'ADMIN' ELSE 'USER' END,
            NOW() - (i || ' days')::interval,
            NOW() - (i || ' days')::interval
        );
    END LOOP;
END $$;

-- 2. Populate Temporary Places Table
CREATE TEMP TABLE temp_places (
    idx INT,
    name VARCHAR(100),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    road_address VARCHAR(255),
    name_en VARCHAR(100),
    road_en VARCHAR(255),
    name_ja VARCHAR(100),
    road_ja VARCHAR(255),
    name_zhs VARCHAR(100),
    road_zhs VARCHAR(255),
    name_zht VARCHAR(100),
    road_zht VARCHAR(255),
    alias1 VARCHAR(100),
    alias2 VARCHAR(100)
);

INSERT INTO temp_places VALUES
(1, '명동역', 37.5609, 126.9863, '서울특별시 중구 퇴계로 지하 126', 'Myeong-dong Station', '126, Toegye-ro, Jung-gu, Seoul', '明洞駅', 'ソウル特別市中区退渓路地下126', '明洞站', '首尔特别市중구退溪路地下126', '明洞站', '首爾特別市中區退溪路地下126', '명동', 'myeongdong'),
(2, '홍대입구역', 37.5575, 126.9244, '서울특별시 마포구 양화로 지하 160', 'Hongik Univ. Station', '160, Yanghwa-ro, Mapo-gu, Seoul', '弘大入り口駅', 'ソウル特別市麻浦区楊花路地下160', '弘大入口站', '首尔特别市麻浦区杨花路地下160', '弘大入口站', '首爾特別市麻浦區楊花路地下160', '홍대', 'hongdae'),
(3, '강남역', 37.4979, 127.0276, '서울특별시 강남구 강남대로 지하 396', 'Gangnam Station', '396, Gangnam-daero, Gangnam-gu, Seoul', '江南駅', 'ソウル特別市江南区江南大路地下396', '江南站', '首尔特别市江南区江南大道地下396', '江南站', '首爾特別市江南區江南大道地下396', '강남', 'gangnam'),
(4, '서울역', 37.5547, 126.9706, '서울특별시 중구 한강대로 405', 'Seoul Station', '405, Hangang-daero, Jung-gu, Seoul', 'ソウル駅', 'ソウル特別市中区漢江大路405', '首尔站', '首尔特别市中区汉江大路405', '首爾站', '首爾特別市中區漢江大路405', '서울', 'seoul'),
(5, '인천국제공항 제1여객터미널', 37.4485, 126.4512, '인천광역시 중구 공항로 272', 'Incheon Int''l Airport Terminal 1', '272, Gonghang-ro, Jung-gu, Incheon', '仁川国際空港 第1旅客ターミナル', '仁川広域市中区空港路272', '仁川国际机场 第一航站楼', '仁川广域市中区机场路272', '仁川國際機場 第一航廈', '仁川廣域市中區機場路272', '인천공항', 'incheon airport'),
(6, '인천국제공항 제2여객터미널', 37.4642, 126.4385, '인천광역시 중구 공항로 272', 'Incheon Int''l Airport Terminal 2', '272, Gonghang-ro, Jung-gu, Incheon', '仁川国際空港 第2旅客ターミナル', '仁川広域市中区空港路272', '仁川国际机场 第二航站楼', '仁川广域市中区机场路272', '仁川國際機場 第二航廈', '仁川廣域市中區機場路272', '인공', 'incheon terminal 2'),
(7, '김포국제공항', 37.5582, 126.7979, '서울특별시 강서구 하늘길 112', 'Gimpo Int''l Airport', '112, Haneul-gil, Gangseo-gu, Seoul', 'Gimpo Int''l Airport', 'ソウル特別市江西区空路112', '金浦国际机场', '首尔特别市江西区天空路112', '金浦國際機場', '首爾特別市江西區天空路112', '김포공항', 'gimpo airport'),
(8, '부산역', 35.1154, 129.0422, '부산광역시 동구 중앙대로 206', 'Busan Station', '206, Jungang-daero, Dong-gu, Busan', '釜山駅', '釜山広域市東区中央大路206', '釜山站', '釜山广域市东区中央大路206', '釜山站', '釜山廣域市東區中央大路206', '부산', 'busan'),
(9, '제주국제공항', 33.5104, 126.4913, '제주특별자치도 제주시 공항로 2', 'Jeju Int''l Airport', '2, Gonghang-ro, Jeju-si, Jeju-do', '済州国際空港', '済주特別自治道済州市空港路2', '济州国际机场', '济州特别道济州市机场路2', '濟州國際機場', '濟州特別道濟州市機場路2', '제주공항', 'jeju airport'),
(10, '해운대해수욕장', 35.1587, 129.1603, '부산광역시 해운대구 해운대해변로 264', 'Haeundae Beach', '264, Haeundaehaebyeon-ro, Haeundae-gu, Busan', '海雲台海水浴場', '釜山広域市海雲台区海雲台海辺路264', '海云台海水浴场', '釜山广域市海云台区海云台海边路264', '海雲台海水浴場', '釜山廣域市海雲台區海雲台海邊路264', '해운대', 'haeundae'),
(11, '경복궁', 37.5796, 126.9770, '서울특별시 종로구 사직로 161', 'Gyeongbokgung Palace', '161, Sajik-ro, Jongno-gu, Seoul', '景福宮', 'ソウル特別市鐘路区社稷路161', '景福宫', '首尔特别市钟路区社稷路161', '景福宮', '首爾特別市鐘路區社稷路161', '경복궁', 'gyeongbokgung'),
(12, '동대문디자인플라자(DDP)', 37.5668, 127.0094, '서울특별시 중구 을지로 281', 'Dongdaemun Design Plaza', '281, Eulji-ro, Jung-gu, Seoul', '東大門デザインプラザ', 'ソウル特別市中区乙支路281', '东大门设计广场', '首尔特别市中区乙支路281', '東大門設計廣場', '首爾特別市中區乙支路281', 'ddp', 'dongdaemun'),
(13, '남산서울타워', 37.5512, 126.9882, '서울특별시 용산구 N서울타워', 'N Seoul Tower', '105, Namsangongwon-gil, Yongsan-gu, Seoul', 'Nソウルタワー', 'ソウル特別市龍山区南山公園路105', 'N首尔塔', '首尔特别市龙山区南山公园路105', 'N首爾塔', '首爾特別市龍山區南山公園路105', '남산타워', 'namsan tower'),
(14, '잠실역', 37.5133, 127.1001, '서울특별시 송파구 올림픽로 지하 265', 'Jamil Station', '265, Olympic-ro, Songpa-gu, Seoul', '蚕室駅', 'ソウル特別市松坡区オリンピック路地下265', '蚕室站', '首尔特别市松坡区奥林匹克路地下265', '蠶室站', '首爾特別市松坡區奧林匹克路地下265', '잠실', 'jamsil'),
(15, '여의도역', 37.5216, 126.9242, '서울특별시 영등포구 여의도동 3', 'Yeouido Station', '3, Yeouido-dong, Yeongdeungpo-gu, Seoul', '汝矣島駅', 'ソウル特別市永登浦区汝矣島洞3', '汝矣岛站', '首尔特别市永登浦区汝矣岛洞3', '汝矣島站', '首爾特別市永登浦區汝矣島洞3', '여의도', 'yeouido'),
(16, '광안리해수욕장', 35.1532, 129.1189, '부산광역시 수영구 광안해변로 219', 'Gwangalli Beach', '219, Gwanganhaebyeon-ro, Suyeong-gu, Busan', '広安里海水浴場', '釜山広域市水営区広安海辺路219', '广安里海水浴场', '釜山广域市水营区광안해변로219', '廣安里海水浴場', '釜山廣域市水營區廣安海邊路219', '광안리', 'gwanganli'),
(17, '성산일출봉', 33.4585, 126.9421, '제주특별자치도 서귀포시 성산읍 일출로 284-12', 'Seongsan Ilchulbong', '284-12, Ilchul-ro, Seongsan-eup, Seogwipo-si, Jeju-do', '城山日出峰', '済州特別自治道西帰浦市城山邑日出路284-12', '城山日出峰', '济州特别道西归浦市城山邑日出路284-12', '城山日出峰', '濟州特別道西歸浦市城山邑日出路284-12', '성산', 'seongsan'),
(18, '울릉도 도동항', 37.4842, 130.9125, '경상북도 울릉군 울릉읍 도동길 14', 'Ulleungdo Dodong Port', '14, Dodong-gil, Ulleung-eup, Ulleung-gun, Gyeongsangbuk-do', '鬱陵島道洞港', '慶尚北道鬱陵郡鬱陵邑道洞路14', '郁陵岛道洞港', '庆尚北道郁陵郡郁陵邑道洞路14', '鬱陵島道洞港', '慶尚北道鬱陵郡鬱陵邑道洞路14', '도동항', 'dodong port'),
(19, '독도', 37.2429, 131.8692, '경상북도 울릉군 울릉읍 독도안용복길 3', 'Dokdo Island', '3, Dokdoanyongbok-gil, Ulleung-eup, Ulleung-gun, Gyeongsangbuk-do', '独島', '慶尚北道鬱陵郡鬱陵邑独島安龍福路3', '独岛', '庆尚北道郁陵郡郁陵邑独岛安龙福路3', '獨島', '慶尚北道鬱陵郡鬱陵邑獨島安龍福路3', '독도', 'dokdo'),
(20, '런던 히드로 공항', 51.4700, -0.4543, 'Longford TW6, United Kingdom', 'London Heathrow Airport', 'Longford TW6, United Kingdom', 'ロンドン・ヒースロー空港', 'ロンドン TW6', '伦敦希思罗机场', '伦敦 TW6', '倫敦希斯路機場', '倫敦 TW6', '히드로', 'heathrow'),
(21, '롯데백화점 강남점', 37.4969, 127.0532, '서울특별시 강남구 도곡로 401', 'Lotte Department Store Gangnam Branch', '401, Dogok-ro, Gangnam-gu, Seoul', 'ロッテ百貨店 江南店', 'ソウル特別市江南区道谷路401', '乐天百货 江南店', '首尔特别市江南区道谷路401', '樂天百貨 江南店', '首爾特別市江南區道谷路401', '강남롯데백화점', 'gangnam lotte'),
(22, '신세계백화점 본점', 37.5609, 126.9809, '서울특별시 중구 소공로 63', 'Shinsegae Department Store Main Branch', '63, Sogong-ro, Jung-gu, Seoul', '新世界百貨店 本店', 'ソウル特別市中区小公路63', '新世界百货 总店', '首尔特别市中区小公路63', '新世界百貨 總店', '首爾特別市中區小公路63', '명동신세계백화점', 'myeongdong shinsegae'),
(23, '스타필드 코엑스몰', 37.5131, 127.0586, '서울특별시 강남구 영동대로 513', 'Starfield COEX Mall', '513, Yeongdong-daero, Gangnam-gu, Seoul', 'スターフィールド・コエックスモール', 'ソウル特別市江南区永東大路513', '星空COEX Mall', '首尔特别市江南区永东大路513', '星空COEX Mall', '首爾特別市江南區永東大路513', '코엑스몰', 'coex mall'),
(24, '더현대 서울', 37.5259, 126.9284, '서울특별시 영등포구 여의대로 108', 'The Hyundai Seoul', '108, Yeoui-daero, Yeongdeungpo-gu, Seoul', 'ザ・现代ソウル', 'ソウル特別市永登浦区汝矣大路108', '现代百货首尔', '首尔特别市永登浦区汝矣大路108', '現代百貨首爾', '首爾特別市永登浦區汝矣大路108', '여의도더현대', 'hyundai seoul'),
(25, '롯데월드타워', 37.5126, 127.1025, '서울특별시 송파구 올림픽로 300', 'Lotte World Tower', '300, Olympic-ro, Songpa-gu, Seoul', 'ロッテワールドタワー', 'ソウル特別市松坡区オリンピック路300', '乐天世界塔', '首尔特别市松坡区奥林匹克路300', '樂天世界塔', '首爾特別市松坡區奧林匹크路300', '월드타워', 'world tower'),
(26, '현대백화점 무역센터점', 37.5086, 127.0598, '서울특별시 강남구 테헤란로 517', 'Hyundai Department Store Trade Center Branch', '517, Teheran-ro, Gangnam-gu, Seoul', '現代百貨店 貿易センター店', 'ソウル特別市江南区テ헤란路517', '现代百货 贸易中心店', '首尔特别市江南区德黑兰路517', '現代百貨 貿易中心店', '首爾特別市江南區德黑蘭路517', '삼성역현대백화점', 'hyundai trade center'),
(27, '신세계백화점 강남점', 37.5049, 127.0049, '서울특별시 서초구 신반포로 176', 'Shinsegae Department Store Gangnam Branch', '176, Sinbanpo-ro, Seocho-gu, Seoul', '新世界百貨店 江南店', 'ソウル特別市瑞草区新盤浦路176', '新世界百货 江南店', '首尔特别市瑞草区新盘浦路176', '新世界百貨 江南店', '首爾特別市瑞草區新盤浦路176', '강남신세계', 'gangnam shinsegae'),
(28, '타임스퀘어 영등포', 37.5172, 126.9038, '서울특별시 영등포구 영중로 15', 'Times Square Yeongdeungpo', '15, Yeongjung-ro, Yeongdeungpo-gu, Seoul', 'タイムズスクエア 永登浦', 'ソウル特別市永登浦区永中路15', '时代广场 永登浦', '首尔特别市永登浦区永中路15', '時代廣場 永登浦', '首爾特別市永登浦區永中路15', '영등포타임스퀘어', 'yeongdeungpo times square'),
(29, '스타필드 하남', 37.5455, 127.2241, '경기도 하남시 미사대로 750', 'Starfield Hanam', '750, Misa-daero, Hanam-si, Gyeonggi-do', 'スターフィールド河南', '京畿道河南市渼沙大路750', '星空河南', '京畿道河南市渼沙大路750', '星空河南', '京畿道河南市渼沙大路750', '하남스타필드', 'starfield hanam'),
(30, '롯데몰 김포공항', 37.5663, 126.8026, '서울특별시 강서구 하늘길 38', 'Lotte Mall Gimpo Airport Branch', '38, Haneul-gil, Gangseo-gu, Seoul', 'ロッテモール金浦空港', 'ソウル特別市江西区空路38', '乐天商城金浦机场', '首尔特别市江西区天空路38', '樂天商城金浦機場', '首爾特別市江西區天空路38', '김포공항롯데몰', 'gimpo lotte mall'),
(31, '블루보틀커피 성수 카페', 37.5480, 127.0445, '서울특별시 성동구 아차산로 7', 'Blue Bottle Coffee Seongsu Cafe', '7, Achasan-ro, Seongdong-gu, Seoul', 'ブルーボトルコーヒー聖水', 'ソウル特別市城東区峨嵯山路7', '蓝瓶咖啡 圣水店', '首尔特别市城东区峨常路7', '藍瓶咖啡 聖水店', '首爾特別市城東區峨常路7', '성수블루보틀', 'blue bottle seongsu'),
(32, '카페 레이어드 연남점', 37.5612, 126.9248, '서울특별시 마포구 성미산로 161-4', 'Cafe Layered Yeonnam', '161-4, Seongmisan-ro, Mapo-gu, Seoul', 'カフェレイヤード延南', 'ソウル特別市麻浦区城美山路161-4', 'Layered咖啡馆 延南店', '首尔特别市麻浦区城美山路161-4', 'Layered咖啡館 延南店', '首爾特別市麻浦區城美山路161-4', '연남레이어드', 'layered yeonnam'),
(33, '라인프렌즈 플래그십스토어 이태원점', 37.5348, 126.9936, '서울특별시 용산구 이태원로 200', 'Line Friends Flagship Store Itaewon', '200, Itaewon-ro, Yongsan-gu, Seoul', 'LINE FRIENDS イテウォン店', 'ソウル特別市龍山区梨泰院路200', 'LINE FRIENDS 梨泰院店', '首尔特别市龙山区梨泰院路200', 'LINE FRIENDS 梨泰院店', '首爾特別市龍山區梨泰院路200', '이태원라인프렌즈', 'line friends itaewon'),
(34, '대학로 마로니에공원', 37.5818, 127.0022, '서울특별시 종로구 대학로 104', 'Daehakro Maronier Park', '104, Daehak-ro, Jongno-gu, Seoul', '大学路マロニエ公園', 'ソウル特別市鐘路区大学路104', '大学路马罗尼矣公园', '首尔特别市钟路区大学路104', '大學路馬羅尼矣公園', '首爾特別市鐘路區大學路104', '마로니에공원', 'maronier park'),
(35, '인사동 쌈지길', 37.5742, 126.9848, '서울특별시 종로구 인사동길 44', 'Ssamzigil Insadong', '44, Insadong-gil, Jongno-gu, Seoul', '仁寺洞サムジギル', 'ソウル特別市鐘路区仁寺洞路44', '仁寺洞人人广场', '首尔特别市钟路区仁寺洞路44', '仁寺洞人人廣場', '首爾特別市鐘路區仁寺洞路44', '쌈지길', 'ssamzigil'),
(36, '삼청동 코리아목욕탕', 37.5822, 126.9818, '서울특별시 종로구 삼청로7길 22', 'Samcheong-dong Korea Bathhouse', '22, Samcheong-ro 7-gil, Jongno-gu, Seoul', '三清洞コリア銭湯', 'ソウル特別市鐘路区三清路7街22', '三清洞韩国浴室', '首尔特别市钟路区三清路7街22', '三清洞韓國浴室', '首爾特別市鐘路區三清路7街22', '코리아목욕탕', 'korea bathhouse'),
(37, '현대백화점 신촌점', 37.5560, 126.9360, '서울특별시 서대문구 신촌로 83', 'Hyundai Department Store Sinchon Branch', '83, Sinchon-ro, Seodaemun-gu, Seoul', '現代百貨店 新村店', 'ソウル特別市西大門区新村路83', '现代百货 新村店', '首尔特别市西大门区新村路83', '現代百貨 新村店', '首爾特別市西大門區新村路83', '신촌현백', 'sinchon hyundai'),
(38, '압구정 로데오거리', 37.5275, 127.0385, '서울특별시 강남구 압구정로50길 22', 'Apgujeong Rodeo Street', '22, Apgujeong-ro 50-gil, Gangnam-gu, Seoul', '狎鴎亭ロデオ通り', 'ソウル特別市江南区狎鴎亭路50街22', '狎鸥亭罗德奥街', '首尔特别市江南区狎鸥亭路50街22', '狎鷗亭羅德奧街', '首爾特別市江南區狎鷗亭路50街22', '압구정로데오', 'apgujeong rodeo'),
(39, '애플 가로수길', 37.5208, 127.0228, '서울특별시 강남구 가로수길 46', 'Apple Garosugil', '46, Garosu-gil, Gangnam-gu, Seoul', 'Apple カロスキル', 'ソウル特別市江南区カロスキル46', 'Apple 林荫路', '首尔特别市江南区林荫路46', 'Apple 林蔭路', '首爾特別市江南區林蔭路46', '가로수길애플', 'apple garosugil'),
(40, '롯데시네마 건대입구', 37.5385, 127.0728, '서울특별시 광진구 아차산로 272', 'Lotte Cinema Konkuk Univ. Star City', '272, Achasan-ro, Gwangjin-gu, Seoul', 'ロッテシネマ建大入り口', 'ソウル特別市広津区アチャ산路272', '乐天影城建大入口', '首尔特别市广津区阿常路272', '樂天影城建大入口', '首爾特別市廣津區阿常路272', '건대롯데시네마', 'lotte cinema konkuk'),
(41, 'CGV 용산아이파크몰', 37.5295, 126.9645, '서울특별시 용산구 한강대로23길 55', 'CGV Yongsan I-Park Mall', '55, Hangang-daero 23-gil, Yongsan-gu, Seoul', 'CGV龍山アイパークモール', 'ソウル特別市龍山区漢江大路23街55', 'CGV 龙山I-Park Mall', '首尔特别市龙山区馆内大路23街55', 'CGV 龍山I-Park Mall', '首爾特別市龍山區館內大路23街55', '용산cgv', 'cgv yongsan'),
(42, '세종문화회관', 37.5725, 126.9758, '서울특별시 종로구 세종대로 175', 'Sejong Center for the Performing Arts', '175, Sejong-daero, Jongno-gu, Seoul', '世宗文化会館', 'ソウル特別市鐘路区世宗大路175', '世宗文化会馆', '首尔特别市钟路区世宗大路175', '世宗文化會館', '首爾特別市鐘路區世宗大路175', '세종문화회관', 'sejong center'),
(43, '예술의전당', 37.4785, 127.0118, '서울특별시 서초구 남부순환로 2406', 'Seoul Arts Center', '2406, Nambusunhwan-ro, Seocho-gu, Seoul', '芸術の殿堂', 'ソウル特別市瑞草区南部循環路2406', '艺术殿堂', '首尔特别市瑞草区南部循环路2406', '藝術殿堂', '首爾特別市瑞草區南部循環路2406', '예술의전당', 'seoul arts center'),
(44, '올림픽공원 평화의문', 37.5205, 127.1165, '서울특별시 송파구 올림픽로 424', 'Olympic Park World Peace Gate', '424, Olympic-ro, Songpa-gu, Seoul', 'オリンピック公園 平和の門', 'ソウル特別市松坡区オリンピック路424', '奥林匹克公园 和平之门', '首尔特别市松坡区奥林匹克路424', '奧林匹克公園 和平之門', '首爾特別市松坡區奧林匹克路424', '평화의문', 'peace gate'),
(45, '북촌한옥마을 안내소', 37.5828, 126.9835, '서울특별시 종로구 북촌로5길 48', 'Bukchon Hanok Village Information Center', '48, Bukchon-ro 5-gil, Jongno-gu, Seoul', '北村韓屋村案内所', 'ソウル特別市鐘路区北村路5街48', '北村韩屋村咨询处', '首尔特别市钟路区北村路5街48', '北村韓屋村諮詢處', '首爾特別市鐘路區北村路5街48', '북촌한옥마을', 'bukchon hanok village'),
(46, '뚝섬한강공원 보관함', 37.5285, 127.0678, '서울특별시 광진구 강변북로 139', 'Ttukseom Hangang Park Locker', '139, Gangbyeonbuk-ro, Gwangjin-gu, Seoul', '뚝섬ハンガン公園ロッカー', 'ソウル特別市広津区江辺北路139', '뚝섬汉江公园寄存柜', '首尔特别市广津区江边北路139', '뚝섬漢江公園寄存櫃', '首爾特別市廣津區江邊北路139', '뚝섬유원지', 'ttukseom park'),
(47, '여의도한강공원 멀티플라자', 37.5282, 126.9332, '서울특별시 영등포구 여의동로 330', 'Yeouido Hangang Park Multiplaza', '330, Yeouidong-ro, Yeongdeungpo-gu, Seoul', '汝矣島ハンガン公園マルチプラザ', 'ソウル特別市永登浦区汝矣東路330', '汝矣岛汉江公园广场', '首尔特别市永登浦区汝矣东路330', '汝矣島漢江公園廣場', '首爾特別市永登浦區汝矣東路330', '여의도한강공원', 'yeouido hangang park'),
(48, '청계광장 소라탑', 37.5692, 126.9778, '서울특별시 중구 태평로1가 1', 'Cheonggye Plaza Spring Tower', '1, Taepyeongno 1-ga, Jung-gu, Seoul', '清渓広場スプリングタワー', 'ソウル特別市中区太平路1街1', '清溪广场海螺塔', '首尔特别市中区太平路1街1', '清溪廣場海螺塔', '首爾特別市中區太平路1街1', '청계광장', 'cheonggye plaza'),
(49, '광화문광장 세종대왕동상 지하', 37.5728, 126.9768, '서울특별시 종로구 세종대로 172', 'Gwanghwamun Square King Sejong Statue Underground', '172, Sejong-daero, Jongno-gu, Seoul', '光化門広場 世宗大王銅像 地下', 'ソウル特別市鐘路区世宗大路172', '光化门广场 世宗大王铜像 地下', '首尔特别市钟路区世宗大路172', '光化門廣場 世宗大王銅像 地下', '首爾特別市鐘路區世宗大路172', '세종대왕동상', 'king sejong statue'),
(50, '남대문시장 중앙통로', 37.5592, 126.9772, '서울특별시 중구 남대문시장4길 21', 'Namdaemun Market Main Passage', '21, Namdaemunsijang 4-gil, Jung-gu, Seoul', '南大門市場 中央通路', 'ソウル特別市中区南大門市場4街21', '南大门市场 中央通道', '首尔特别市中区南大门市场4街21', '南大門市場 中央通道', '首爾特別市中區南大門市場4街21', '남대문시장', 'namdaemun market');

-- 3. Load Places and Place Translations/Aliases via Loop
DO $$
DECLARE
    r RECORD;
    p_id BIGINT;
BEGIN
    FOR r IN (SELECT * FROM temp_places) LOOP
        -- Insert Place
        INSERT INTO places (name, latitude, longitude, road_address)
        VALUES (r.name, r.latitude, r.longitude, r.road_address)
        RETURNING id INTO p_id;

        -- Insert translations
        INSERT INTO place_translations (place_id, language_code, name, road_address) VALUES
        (p_id, 'ko', r.name, r.road_address),
        (p_id, 'en', r.name_en, r.road_en),
        (p_id, 'ja', r.name_ja, r.road_ja),
        (p_id, 'zh-Hans', r.name_zhs, r.road_zhs),
        (p_id, 'zh-Hant', r.name_zht, r.road_zht);

        -- Insert Aliases (Korean, English)
        INSERT INTO place_aliases (place_id, language_code, alias, normalized_alias)
        VALUES (p_id, 'ko', r.alias1, lower(regexp_replace(r.alias1, '[[:space:]]', '', 'g')))
        ON CONFLICT (place_id, normalized_alias) DO NOTHING;

        INSERT INTO place_aliases (place_id, language_code, alias, normalized_alias)
        VALUES (p_id, 'ko', r.name, lower(regexp_replace(r.name, '[[:space:]]', '', 'g')))
        ON CONFLICT (place_id, normalized_alias) DO NOTHING;

        INSERT INTO place_aliases (place_id, language_code, alias, normalized_alias)
        VALUES (p_id, 'en', r.alias2, lower(regexp_replace(r.alias2, '[[:space:]]', '', 'g')))
        ON CONFLICT (place_id, normalized_alias) DO NOTHING;

        INSERT INTO place_aliases (place_id, language_code, alias, normalized_alias)
        VALUES (p_id, 'en', r.name_en, lower(regexp_replace(r.name_en, '[[:space:]]', '', 'g')))
        ON CONFLICT (place_id, normalized_alias) DO NOTHING;

        -- 4. Create 4 Lockers for each Place (Total 80 Lockers with diverse scenarios)
        FOR j IN 1..4 LOOP
            DECLARE
                l_id BIGINT;
                lat DOUBLE PRECISION;
                lng DOUBLE PRECISION;
                l_name_ko VARCHAR(150);
                l_name_en VARCHAR(150);
                l_name_ja VARCHAR(150);
                l_name_zhs VARCHAR(150);
                l_name_zht VARCHAR(150);
                
                min_p INT;
                max_p INT;
                s_time TIME;
                e_time TIME;
                v_acc INT;
                v_inacc INT;
                sizes VARCHAR(100);
                l_type VARCHAR(30);
                in_out VARCHAR(10);
                gr_lvl VARCHAR(20);
                flr INT;
            BEGIN
                IF r.idx >= 31 AND j > 1 THEN
                    CONTINUE;
                END IF;

                -- Distribute coordinate deviation
                lat := r.latitude + (CASE WHEN j = 1 THEN 0.0002 WHEN j = 2 THEN -0.0002 WHEN j = 3 THEN 0.0004 ELSE -0.0004 END);
                lng := r.longitude + (CASE WHEN j = 1 THEN 0.0002 WHEN j = 2 THEN -0.0002 WHEN j = 3 THEN -0.0004 ELSE 0.0004 END);

                -- Scenarios Setup
                IF j = 1 THEN
                    -- Normal Scenario (24 Hours, balanced votes)
                    l_name_ko := r.name || ' 1번 보관함 (24시간)';
                    l_name_en := r.name_en || ' Locker #1 (24h)';
                    l_name_ja := r.name_ja || ' ロッカー 1号';
                    l_name_zhs := r.name_zhs || ' 寄存柜 1号';
                    l_name_zht := r.name_zht || ' 寄存櫃 1號';
                    min_p := 2000; max_p := 8000;
                    s_time := '00:00:00'; e_time := '23:59:59';
                    v_acc := 150 + (p_id * 7) % 50; v_inacc := (p_id * 3) % 15;
                    sizes := 'SMALL,MEDIUM,LARGE';
                    l_type := 'SUBWAY_STATION'; in_out := 'INDOOR'; gr_lvl := 'UNDERGROUND'; flr := -1;
                ELSIF j = 2 THEN
                    -- Free Scenario (Daytime only, small sizes)
                    l_name_ko := r.name || ' 2번 무료 보관함 [주간전용]';
                    l_name_en := r.name_en || ' Free Locker #2 [Daytime]';
                    l_name_ja := r.name_ja || ' 無料ロッカー 2号';
                    l_name_zhs := r.name_zhs || ' 免费寄存柜 2号';
                    l_name_zht := r.name_zht || ' 免費寄存櫃 2號';
                    min_p := 0; max_p := 0;
                    s_time := '08:00:00'; e_time := '22:00:00';
                    v_acc := 80 + (p_id * 11) % 40; v_inacc := (p_id * 5) % 8;
                    sizes := 'SMALL';
                    l_type := 'ETC'; in_out := 'OUTDOOR'; gr_lvl := 'ABOVE_GROUND'; flr := 1;
                ELSIF j = 3 THEN
                    -- Extreme High Price (Private, Large sizes only)
                    l_name_ko := r.name || ' 3번 Premium 보관함 (대형전용)';
                    l_name_en := r.name_en || ' Premium Locker #3 (Large)';
                    l_name_ja := r.name_ja || ' プレミアムロッカー 3号';
                    l_name_zhs := r.name_zhs || ' 豪华寄存柜 3号';
                    l_name_zht := r.name_zht || ' 豪華寄存櫃 3號';
                    min_p := 45000; max_p := 120000;
                    s_time := '10:00:00'; e_time := '20:00:00';
                    v_acc := 12 + (p_id * 2) % 10; v_inacc := (p_id * 13) % 25;
                    sizes := 'LARGE';
                    l_type := 'PRIVATE_LOCKER'; in_out := 'INDOOR'; gr_lvl := 'ABOVE_GROUND'; flr := 2;
                ELSE
                    -- Extreme Time Constraint / Unreliable Scenario (accurate < inaccurate)
                    l_name_ko := r.name || ' 4번 기습보관함 [운영시간 제한]';
                    l_name_en := r.name_en || ' Dynamic Locker #4 [Limited]';
                    l_name_ja := r.name_ja || ' 制限ありロッカー 4号';
                    l_name_zhs := r.name_zhs || ' 限制型寄存柜 4号';
                    l_name_zht := r.name_zht || ' 限制型寄存櫃 4號';
                    min_p := 3000; max_p := 9000;
                    s_time := '09:00:00'; e_time := '09:05:00'; -- 5 minutes only
                    v_acc := (p_id * 3) % 5; v_inacc := 120 + (p_id * 17) % 80;
                    sizes := 'MEDIUM';
                    l_type := 'CONVENIENCE_STORE'; in_out := 'OUTDOOR'; gr_lvl := 'ABOVE_GROUND'; flr := 1;
                END IF;

                -- Insert Locker
                INSERT INTO lockers (name, road_address, latitude, longitude, place_id, location)
                VALUES (l_name_ko, r.road_address, lat, lng, p_id, ST_SetSRID(ST_MakePoint(lng, lat), 4326)::geography)
                RETURNING id INTO l_id;

                -- Insert Locker Detail
                INSERT INTO locker_details (
                    locker_id, locker_type, indoor_outdoor_type, ground_level_type, floor, 
                    min_price, max_price, locker_size, detail_info, start_time, end_time, 
                    image_url, accurate_vote_count, inaccurate_vote_count, created_at, updated_at
                ) VALUES (
                    l_id, l_type, in_out, gr_lvl, flr, 
                    min_p, max_p, sizes, 
                    '상세 가이드: ' || l_name_ko || '는 ' || r.road_address || '에 위치해 있습니다. 다양한 언어로 서비스가 제공되며, 보관 공간 정보는 ' || sizes || ' 입니다. 자세한 요금은 ' || min_p || '원 ~ ' || max_p || '원 사이입니다.', 
                    s_time, e_time, 
                    'https://picsum.photos/id/' || ((l_id * 13) % 100) || '/400/300', 
                    v_acc, v_inacc, 
                    NOW() - (j || ' days')::interval, NOW() - (j || ' days')::interval
                );

                -- Insert Translations
                INSERT INTO locker_translations (locker_id, language_code, name, road_address, detail_info) VALUES
                (l_id, 'ko', l_name_ko, r.road_address, '상세 설명: ' || r.name || ' 부근 보관함. 주소: ' || r.road_address),
                (l_id, 'en', l_name_en, r.road_en, 'Detail Description: Locker near ' || r.name_en || '. Address: ' || r.road_en),
                (l_id, 'ja', l_name_ja, r.road_ja, '詳細な説明: ' || r.name_ja || ' 近くのコインロッカー。住所: ' || r.road_ja),
                (l_id, 'zh-Hans', l_name_zhs, r.road_zhs, '详细说明: ' || r.name_zhs || ' 附近的寄存柜。地址: ' || r.road_zhs),
                (l_id, 'zh-Hant', l_name_zht, r.road_zht, '詳細說明: ' || r.name_zht || ' 附近的寄存櫃。地址: ' || r.road_zht);

                -- Insert Aliases
                INSERT INTO locker_aliases (locker_id, language_code, alias, normalized_alias)
                VALUES (l_id, 'ko', r.name || '보관함' || j, lower(regexp_replace(r.name || '보관함' || j, '[[:space:]]', '', 'g')))
                ON CONFLICT (locker_id, normalized_alias) DO NOTHING;

                INSERT INTO locker_aliases (locker_id, language_code, alias, normalized_alias)
                VALUES (l_id, 'ko', r.alias1 || '로커' || j, lower(regexp_replace(r.alias1 || '로커' || j, '[[:space:]]', '', 'g')))
                ON CONFLICT (locker_id, normalized_alias) DO NOTHING;

                INSERT INTO locker_aliases (locker_id, language_code, alias, normalized_alias)
                VALUES (l_id, 'en', r.alias2 || ' locker ' || j, lower(regexp_replace(r.alias2 || ' locker ' || j, '[[:space:]]', '', 'g')))
                ON CONFLICT (locker_id, normalized_alias) DO NOTHING;

            END;
        END LOOP;
    END LOOP;
END $$;

-- 5. Drop Temporary Table
DROP TABLE temp_places;

-- 6. Generate Massive Votes (Around 600 Votes distributed amongst active users and lockers)
DO $$
DECLARE
    u_id BIGINT;
    l_id BIGINT;
    vote_type VARCHAR(20);
    cnt INT := 0;
BEGIN
    FOR l_id IN (SELECT id FROM lockers) LOOP
        -- Each locker gets 4 to 12 random votes
        FOR k IN 1..(4 + (l_id % 9)) LOOP
            u_id := 1 + ((l_id * 19 + k * 29) % 100); -- Pick from active users (IDs 1-100)
            vote_type := CASE WHEN (l_id + k) % 6 = 0 THEN 'INCORRECT' ELSE 'CORRECT' END;

            INSERT INTO locker_votes (user_id, locker_id, vote_type, created_at)
            VALUES (u_id, l_id, vote_type, NOW() - (k || ' hours')::interval)
            ON CONFLICT (user_id, locker_id) DO NOTHING;
            
            cnt := cnt + 1;
        END LOOP;
    END LOOP;
END $$;

-- 7. Generate Favorites (Around 250 favorites)
DO $$
DECLARE
    u_id BIGINT;
    l_id BIGINT;
BEGIN
    FOR l_id IN (SELECT id FROM lockers) LOOP
        -- Lockers favorited by 1 to 4 users
        FOR k IN 1..((l_id % 4) + 1) LOOP
            u_id := 1 + ((l_id * 31 + k * 17) % 100);
            
            INSERT INTO favorite_lockers (user_id, locker_id, created_at)
            VALUES (u_id, l_id, NOW() - (k || ' days')::interval)
            ON CONFLICT (user_id, locker_id) DO NOTHING;
        END LOOP;
    END LOOP;
END $$;

-- 8. Generate Locker Reports (Around 55 reports with all possible status types)
DO $$
DECLARE
    u_id BIGINT;
    l_name VARCHAR(100);
    status_type VARCHAR(20);
    p_name VARCHAR(120);
BEGIN
    FOR i IN 1..55 LOOP
        u_id := 1 + ((i * 23) % 100);
        l_name := '제보 로커_' || i || '호';
        status_type := CASE WHEN i % 5 = 0 THEN 'REJECTED'
                            WHEN i % 3 = 0 THEN 'APPROVED'
                            ELSE 'SUBMITTED' END;
        p_name := CASE WHEN i % 3 = 0 THEN '홍대입구역' WHEN i % 3 = 1 THEN '강남역' ELSE '명동역' END;

        INSERT INTO locker_reports (
            user_id, name, road_address, ground_level_type, floor, indoor_outdoor_type, 
            locker_type, locker_size, is_free, min_price, max_price, additional_info, 
            start_time, end_time, image_url, location_consent_agreed, latitude, longitude, 
            status, created_at, updated_at
        ) VALUES (
            u_id, l_name, '서울특별시 마포구 양화로 ' || (80 + i), 
            CASE WHEN i % 2 = 0 THEN 'ABOVE_GROUND' ELSE 'UNDERGROUND' END,
            CASE WHEN i % 2 = 0 THEN 2 ELSE -2 END,
            CASE WHEN i % 3 = 0 THEN 'OUTDOOR' ELSE 'INDOOR' END,
            'SUBWAY_STATION', 'SMALL,LARGE', false, 1500, 7000, 
            p_name || ' 근처 신규 제보합니다. 상태: ' || status_type, 
            '07:00:00', '23:30:00', 
            'https://picsum.photos/id/' || (i + 150) || '/400/300', 
            true, 
            37.5575 + (i * 0.00015), 126.9244 + (i * 0.00015), 
            status_type, 
            NOW() - (i || ' days')::interval, NOW() - (i || ' days')::interval
        );
    END LOOP;
END $$;

-- 9. Create Multilingual Admin Documents (Terms, Privacy, Notices)
DO $$
DECLARE
    doc_id BIGINT;
    sec_id BIGINT;
BEGIN
    -- [1] Terms of Use (이용 약관) - 3 Sections
    INSERT INTO admin_documents (type, title, active, list_order, created_at, updated_at, applied_at)
    VALUES ('TERMS', '짐두고 서비스 이용약관', true, 1, NOW() - INTERVAL '10 days', NOW(), NOW())
    RETURNING id INTO doc_id;

    INSERT INTO admin_document_translations (admin_document_id, language_code, title) VALUES
    (doc_id, 'ko', '짐두고 서비스 이용약관'),
    (doc_id, 'en', 'Zimdugo Service Terms of Use'),
    (doc_id, 'ja', 'Zimdugo サービス利用規約'),
    (doc_id, 'zh-Hans', 'Zimdugo 服务使用条款'),
    (doc_id, 'zh-Hant', 'Zimdugo 服務使用條款');

    -- Sec 1
    INSERT INTO admin_document_sections (subtitle, content, list_order, admin_document_id)
    VALUES ('제1조 목적', '본 약관은 주식회사 짐두고가 제공하는 서비스 이용에 대한 기본 목적을 규정합니다.', 1, doc_id)
    RETURNING id INTO sec_id;
    INSERT INTO admin_document_section_translations (admin_document_section_id, language_code, subtitle, content) VALUES
    (sec_id, 'ko', '제1조 목적', '본 약관은 주식회사 짐두고가 제공하는 서비스 이용에 대한 기본 목적을 규정합니다.'),
    (sec_id, 'en', 'Article 1 (Purpose)', 'These terms regulate the basic purpose of using the service provided by Zimdugo Corp.'),
    (sec_id, 'ja', '第1条 (目的)', '本規約は、株式会社Zimdugoが提供するサービスの利用に関する基本的な目的を規定します。'),
    (sec_id, 'zh-Hans', '第一条 (目的)', '本条款规定了使用Zimdugo公司提供的服务的基本目的。'),
    (sec_id, 'zh-Hant', '第一條 (目的)', '本條款規定了使用Zimdugo公司提供的服務的基本目的。');

    -- Sec 2
    INSERT INTO admin_document_sections (subtitle, content, list_order, admin_document_id)
    VALUES ('제2조 용어의 정의', '1. "짐두고"란 보관함 검색 플랫폼을 의미합니다. 2. "회원"이란 회원가입을 완료한 자입니다.', 2, doc_id)
    RETURNING id INTO sec_id;
    INSERT INTO admin_document_section_translations (admin_document_section_id, language_code, subtitle, content) VALUES
    (sec_id, 'ko', '제2조 용어의 정의', '1. "짐두고"란 보관함 검색 플랫폼을 의미합니다. 2. "회원"이란 회원가입을 완료한 자입니다.'),
    (sec_id, 'en', 'Article 2 (Definitions)', '1. "Zimdugo" refers to the locker search platform. 2. "Member" refers to a registered user.'),
    (sec_id, 'ja', '第2条 (定義)', '1. 「Zimdugo」とは保管箱検索プラットフォームを指します。2. 「会員」とは会員登録を完了した者を指します。'),
    (sec_id, 'zh-Hans', '第二条 (定义)', '1. "Zimdugo"指寄存柜搜索平台。2. "会员"指完成会员注册的人员。'),
    (sec_id, 'zh-Hant', '第二條 (定義)', '1. "Zimdugo"指寄存櫃搜索平台。2. "會員"指完成會員註冊的人員。');

    -- Sec 3
    INSERT INTO admin_document_sections (subtitle, content, list_order, admin_document_id)
    VALUES ('제3조 약관의 개정', '회사는 필요에 따라 약관을 변경할 수 있으며, 변경 7일 전 서비스 내에 공지합니다.', 3, doc_id)
    RETURNING id INTO sec_id;
    INSERT INTO admin_document_section_translations (admin_document_section_id, language_code, subtitle, content) VALUES
    (sec_id, 'ko', '제3조 약관의 개정', '회사는 필요에 따라 약관을 변경할 수 있으며, 변경 7일 전 서비스 내에 공지합니다.'),
    (sec_id, 'en', 'Article 3 (Revision of Terms)', 'The company may modify terms as necessary and will announce 7 days prior within the service.'),
    (sec_id, 'ja', '第3条 (規約의 改定)', '会社は必要に応じて規約を変更することができ、変更の7일前にサービス内で告知します。'),
    (sec_id, 'zh-Hans', '第三条 (条款修订)', '公司可根据需要修改条款，并于修改前7天在服务内公告。'),
    (sec_id, 'zh-Hant', '第三條 (條款修訂)', '公司可根據需要修改條款，並於修改前7天在服務內公告。');


    -- [2] Privacy Policy (개인정보처리방침) - 2 Sections
    INSERT INTO admin_documents (type, title, active, list_order, created_at, updated_at, applied_at)
    VALUES ('PRIVACY', '짐두고 개인정보 처리방침', true, 2, NOW() - INTERVAL '15 days', NOW(), NOW())
    RETURNING id INTO doc_id;

    INSERT INTO admin_document_translations (admin_document_id, language_code, title) VALUES
    (doc_id, 'ko', '짐두고 개인정보 처리방침'),
    (doc_id, 'en', 'Zimdugo Privacy Policy'),
    (doc_id, 'ja', 'Zimdugo 個人情報処理方針'),
    (doc_id, 'zh-Hans', 'Zimdugo 个人信息处理方针'),
    (doc_id, 'zh-Hant', 'Zimdugo 個人情報處理方針');

    -- Sec 1
    INSERT INTO admin_document_sections (subtitle, content, list_order, admin_document_id)
    VALUES ('1. 수집하는 개인정보', '회사는 회원 식별을 위해 이메일, 닉네임, 프로필 사진 정보를 수집합니다.', 1, doc_id)
    RETURNING id INTO sec_id;
    INSERT INTO admin_document_section_translations (admin_document_section_id, language_code, subtitle, content) VALUES
    (sec_id, 'ko', '1. 수집하는 개인정보', '회사는 회원 식별을 위해 이메일, 닉네임, 프로필 사진 정보를 수집합니다.'),
    (sec_id, 'en', '1. Personal Information Collected', 'The company collects email, nickname, and profile picture for member identification.'),
    (sec_id, 'ja', '1. 収集する個人情報', '会社は会員識別のためにメール、ニックネーム、プロフィール写真情報を収集します。'),
    (sec_id, 'zh-Hans', '1. 收集的个人信息', '公司收集电子邮件、昵称和个人头像信息以用于会员识别。'),
    (sec_id, 'zh-Hant', '1. 收集的個人情報', '公司收集電子郵件、暱稱和個人頭像信息以用於會員識別。');

    -- Sec 2
    INSERT INTO admin_document_sections (subtitle, content, list_order, admin_document_id)
    VALUES ('2. 정보의 이용 목적', '회원 서비스 관리, 사용자 문의 처리, 위치 기반 서비스 고도화를 위해 이용합니다.', 2, doc_id)
    RETURNING id INTO sec_id;
    INSERT INTO admin_document_section_translations (admin_document_section_id, language_code, subtitle, content) VALUES
    (sec_id, 'ko', '2. 정보의 이용 목적', '회원 서비스 관리, 사용자 문의 처리, 위치 기반 서비스 고도화를 위해 이용합니다.'),
    (sec_id, 'en', '2. Purpose of Using Information', 'Used for member service management, user inquiries processing, and advanced location-based services.'),
    (sec_id, 'ja', '2. 情報の利用目的', '会員サービス管理、ユーザーの問い合わせ対応、位置정보서비스의 고도화에 이용합니다.'),
    (sec_id, 'zh-Hans', '2. 信息使用目的', '用于会员 service 管理、处理用户咨询以及优化基于位置的服务。'),
    (sec_id, 'zh-Hant', '2. 信息使用目的', '用於會員服務管理、處理用戶諮詢以及優化基於位置的服務。');


    -- [3] Notice (서비스 공지사항 - 비활성/초안) - 1 Section
    INSERT INTO admin_documents (type, title, active, list_order, created_at, updated_at, applied_at)
    VALUES ('NOTICE', '신규 다국어 검색 고도화 안내 (초안)', false, 3, NOW(), NOW(), null)
    RETURNING id INTO doc_id;

    INSERT INTO admin_document_translations (admin_document_id, language_code, title) VALUES
    (doc_id, 'ko', '신규 다국어 검색 고도화 안내 (초안)'),
    (doc_id, 'en', 'Notice on New Multilingual Search Enhancement (Draft)'),
    (doc_id, 'ja', '新規多言語検索高度化のご案内 (草案)'),
    (doc_id, 'zh-Hans', '全新多语言搜索优化通知 (草案)'),
    (doc_id, 'zh-Hant', '全新多語言搜索優化通知 (草案)');

    -- Sec 1
    INSERT INTO admin_document_sections (subtitle, content, list_order, admin_document_id)
    VALUES ('다국어 형태소 분석기 도입 안내', '전 세계 이용자들을 위해 Nori(한국어), English(영어), Kuromoji(일본어), SmartCN(중국어) 형태소 검색 엔진이 도입되었습니다.', 1, doc_id)
    RETURNING id INTO sec_id;
    INSERT INTO admin_document_section_translations (admin_document_section_id, language_code, subtitle, content) VALUES
    (sec_id, 'ko', '다국어 형태소 분석기 도입 안내', '전 세계 이용자들을 위해 Nori(한국어), English(영어), Kuromoji(일본어), SmartCN(중국어) 형태소 검색 엔진이 도입되었습니다.'),
    (sec_id, 'en', 'Introduction of Multilingual Analyzers', 'To assist global users, Nori (KO), English (EN), Kuromoji (JA), and SmartCN (ZH) analyzers have been integrated.'),
    (sec_id, 'ja', '多言語形態素解析器の導入', 'グローバルユーザーのため、Nori (韓国語)、English (英語)、Kuromoji (日本語)、SmartCN (中国語) が導入されました。'),
    (sec_id, 'zh-Hans', '多语言分词器引入说明', '为了服务全球用户，我们引入了Nori(韩语)、English(英语)、Kuromoji(日语)、SmartCN(中文)分词引擎。'),
    (sec_id, 'zh-Hant', '多語言分詞器引入說明', '為了服務全球用戶，我們引入了Nori(韓語)、English(英語)、Kuromoji(日語)、SmartCN(中文)分詞引擎。');

END $$;

-- Verification Queries
SELECT 'users' AS table_name, COUNT(*) AS row_count FROM users UNION ALL
SELECT 'places', COUNT(*) FROM places UNION ALL
SELECT 'place_translations', COUNT(*) FROM place_translations UNION ALL
SELECT 'place_aliases', COUNT(*) FROM place_aliases UNION ALL
SELECT 'lockers', COUNT(*) FROM lockers UNION ALL
SELECT 'locker_details', COUNT(*) FROM locker_details UNION ALL
SELECT 'locker_translations', COUNT(*) FROM locker_translations UNION ALL
SELECT 'locker_aliases', COUNT(*) FROM locker_aliases UNION ALL
SELECT 'locker_votes', COUNT(*) FROM locker_votes UNION ALL
SELECT 'favorite_lockers', COUNT(*) FROM favorite_lockers UNION ALL
SELECT 'locker_reports', COUNT(*) FROM locker_reports UNION ALL
SELECT 'admin_documents', COUNT(*) FROM admin_documents UNION ALL
SELECT 'admin_document_sections', COUNT(*) FROM admin_document_sections;
