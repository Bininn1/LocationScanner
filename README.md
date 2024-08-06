## LocationScanner README

### 프로젝트 제목
LocationScanner - 위치 기반 알람 서비스

### 소개
LocationScanner는 사용자가 설정한 위치와 반경 내에 들어가면 알람을 울리는 안드로이드 애플리케이션입니다. 이 앱은 대중교통 이용 시 하차할 정류장을 놓치는 것을 방지하는 데 도움을 줍니다. 사용자가 원하는 장소를 설정하고, 해당 반경 내에 들어가면 핸드폰(또는 이어폰)에서 알람이 울리도록 설계되었습니다.

### 설치
프로젝트 실행 및 설치 방법입니다.

1. 리포지토리를 클론합니다.
   ```bash
   git clone https://github.com/Rachel-3/LocationScanner.git
   cd LocationScanner
   ```

2. 필요한 의존성을 설치합니다.
   - 프로젝트의 `build.gradle` 파일에서 필요한 의존성을 확인하고 설치합니다.
   - `gradle-wrapper.properties` 파일이 포함되어 있어 자동으로 Gradle 환경이 설정됩니다.

3. `gradle.properties` 파일에 카카오맵 API 키를 추가합니다.
   - 프로젝트 루트 디렉토리에 `gradle.properties` 파일을 생성하거나, 기존 파일이 있는 경우 열어 다음 내용을 추가합니다.
   ```properties
   kakaoApiKey=YOUR_API_KEY
   ```

4. `AndroidManifest.xml` 파일에 카카오맵 API 키를 설정합니다.
   - `AndroidManifest.xml` 파일에서 다음과 같이 카카오맵 API 키를 참조합니다.
   ```xml
   <meta-data
       android:name="com.kakao.sdk.AppKey"
       android:value="${kakaoApiKey}"/>
   ```

5. 앱을 빌드하고 실행합니다.
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

### 사용 방법
이 프로젝트는 간단한 안드로이드 애플리케이션으로, 다음 기능을 구현합니다.

- **장소 검색** : 사용자가 원하는 장소를 검색하고 선택할 수 있습니다.
- **위치 및 거리 계산** : GPS를 이용하여 현재 위치를 불러오고 설정한 장소와의 직선 거리를 계산합니다.
- **알람** : 설정한 반경에 들어오면 알람이 울리며, 이어폰 착용 시 이어폰에서 알람이 울립니다.
- **백그라운드 기능** : 앱이 백그라운드에서도 거리를 계산하고 알람을 울립니다.

#### 주요 파일 및 디렉토리
- `app/src/main/java/com/example/locationscanner/MainActivity.java` : 메인 액티비티 파일로, 장소 검색과 알람 설정 기능을 구현합니다.
- `app/src/main/java/com/example/locationscanner/LocationService.java` : 위치 서비스 파일로, 백그라운드에서 위치를 추적하고 알람을 울리는 기능을 구현합니다.
- `app/src/main/res/layout/activity_main.xml` : 메인 액티비티의 레이아웃 파일입니다.
- `app/src/main/res/layout/second_main.xml` : 알람 설정 액티비티의 레이아웃 파일입니다.
- `app/libs/libDaumMapAndroid.jar` : 카카오맵 API 라이브러리입니다.

### 기술 스택
- **언어** : Java
- **플랫폼** : Android
- **빌드 도구** : Gradle
- **라이브러리** : 카카오맵 API, Retrofit, Gson

### 라이센스
이 프로젝트는 MIT 라이센스를 따릅니다. 자세한 내용은 `LICENSE` 파일을 참고하세요.

### 연락처
문의사항은 다음 이메일로 연락주세요.

- **Author** : Chae-rim Yoon
- **Email** : cofla226@naver.com
