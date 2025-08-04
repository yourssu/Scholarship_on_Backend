package campo.server.util

object ApiExamples {
    // 성공 응답 예시들
    const val LOGIN_SUCCESS = """{"success":true,"message":"로그인 되었습니다."}"""
    const val LOGOUT_SUCCESS = """{"success":true,"message":"로그아웃 완료"}"""
    const val REGISTER_SUCCESS = """{"success":true,"message":"가입에 성공했습니다.","data":{"email":"user@example.com","password":"","school":"숭실대학교","classOfSchool":1,"majorOfSchool":"자유전공학부","location":"서울 동작구","levelOfIncome":5,"grade":3.8}}"""
    const val USER_INFO_SUCCESS = """{"success":true,"message":"회원정보를 성공적으로 불러왔습니다.","data":{"email":"user@example.com","password":"","school":"숭실대학교","classOfSchool":1,"majorOfSchool":"자유전공학부","location":"서울 동작구","levelOfIncome":5,"grade":3.8}}"""
    const val UPDATE_SUCCESS = """{"success":true,"message":"회원정보를 성공적으로 수정했습니다."}"""
    const val DELETE_SUCCESS = """{"success":true,"message":"회원정보를 삭제했습니다."}"""
    
    const val SCHOLARSHIP_LIST_SUCCESS = """{"success":true,"message":"장학금 공고를 불러왔습니다.","data":[{"번호":"1","운영기관명":"광주남구장학회","상품명":"행복나눔 장학생","운영기관구분":"지자체(출자출연기관)","상품구분":"장학금","학자금유형구분":"지역연고","대학구분":"4년제(5~6년제포함)","학년구분":"대학2학기/대학3학기/대학4학기/대학5학기/대학6학기/대학7학기/대학8학기이상/대학신입생","학과구분":"공학계열/교육계열/사회계열/예체능계열/의약계열/인문계열/자연계열/제한없음","성적기준 상세내용":"○ 직전학기 12학점 이상 취득하고 성적 평균 2.75 이상인 자 (4.3만점은 2.6이상)","소득기준 상세내용":"○ 2024년도 기준 중위소득 100% 이하인 가구※ 직계가족의 3개월 평균국민건강보험료 산정","지원내역 상세내용":"○ 1인당 100만원※ 생활비 지원","특정자격 상세내용":"○ 해당없음","지역거주여부 상세내용":"○ 해당없음","선발방법 상세내용":"○ 서류심사","선발인원 상세내용":"○ 12명","자격제한 상세내용":"○ 해당없음","추천필요여부 상세내용":"○ 해당없음","제출서류 상세내용":"○ 서류목록","홈페이지 주소":"https://namgu.gwangju.kr","모집시작일":"2024-09-23","모집종료일":"2024-10-11"},{"번호":"2","운영기관명":"광주남구장학회","상품명":"일반장학생","운영기관구분":"지자체(출자출연기관)","상품구분":"장학금","학자금유형구분":"지역연고","대학구분":"해당없음","학년구분":"해당없음","학과구분":"해당없음","성적기준 상세내용":"○ 직전학기 12학점 이상 취득하고 성적 평균 3.0 이상인 자 (4.3만점은 2.8이상)","소득기준 상세내용":"○ 2024년도 기준 중위소득 150% 이하인 가구※ 직계가족의 3개월 평균 국민건강보험료 산정","지원내역 상세내용":"○ 1인당 100만원※ 등록금 지원","특정자격 상세내용":"○ 해당없음","지역거주여부 상세내용":"○ 해당없음","선발방법 상세내용":"○ 서류심사","선발인원 상세내용":"○ 23명","자격제한 상세내용":"○ 해당없음","추천필요여부 상세내용":"○ 해당없음","제출서류 상세내용":"○ 서류목록","홈페이지 주소":"https://namgu.gwangju.kr/","모집시작일":"2024-09-23","모집종료일":"2024-10-11"}]}"""
    const val SCHOLARSHIP_SEARCH_SUCCESS = """{"success":true,"message":"장학금 공고를 불러왔습니다.","data":[{"번호":"1","운영기관명":"광주남구장학회","상품명":"행복나눔 장학생","운영기관구분":"지자체(출자출연기관)","지원내역 상세내용":"○ 1인당 100만원※ 생활비 지원","모집종료일":"2024-10-11"},{"번호":"2","운영기관명":"광주남구장학회","상품명":"일반장학생","운영기관구분":"지자체(출자출연기관)","지원내역 상세내용":"○ 1인당 100만원※ 등록금 지원","모집종료일":"2024-10-11"}]}"""
    const val SCHOLARSHIP_COUNT_SUCCESS = """{"success":true,"message":"장학금 공고 데이터 수를 불러왔습니다.","data":3450}"""
    const val NO_MATCHING_SCHOLARSHIPS = """{"success":true,"message":"조건에 맞는 장학금이 없습니다.","data":[]}"""
    const val SEARCH_KEYWORD_REQUIRED = """{"success":false,"message":"검색어를 넣어주세요.","error":"Bad Request"}"""
    
    // 오류 응답 예시들
    const val BAD_REQUEST = """{"success":false,"message":"잘못된 요청입니다.","error":"Bad Request"}"""
    const val INVALID_EMAIL_FORMAT = """{"success":false,"message":"이메일 형식이 올바르지 않습니다.","error":"Bad Request"}"""
    const val MISSING_REQUIRED_FIELDS = """{"success":false,"message":"필수 정보가 누락되었습니다.","error":"Bad Request"}"""
    const val EMAIL_ALREADY_EXISTS = """{"success":false,"message":"이미 존재하는 이메일입니다.","error":"Bad Request"}"""
    const val WEAK_PASSWORD = """{"success":false,"message":"비밀번호는 8자 이상이어야 합니다.","error":"Bad Request"}"""
    
    const val UNAUTHORIZED = """{"success":false,"message":"인증되지 않은 요청입니다.","error":"Unauthorized"}"""
    const val INVALID_CREDENTIALS = """{"success":false,"message":"이메일 또는 비밀번호가 일치하지 않습니다.","error":"Bad Request"}"""
    const val TOKEN_EXPIRED = """{"success":false,"message":"토큰이 만료되었습니다.","error":"Unauthorized"}"""
    
    const val FORBIDDEN = """{"success":false,"message":"권한이 없습니다.","error":"Forbidden"}"""
    
    const val NOT_FOUND = """{"success":false,"message":"찾을 수 없습니다.","error":"Not Found"}"""
    const val USER_NOT_FOUND = """{"success":false,"message":"사용자를 찾을 수 없습니다.","error":"Not Found"}"""
    const val SCHOLARSHIP_NOT_FOUND = """{"success":false,"message":"장학금을 찾을 수 없습니다.","error":"Not Found"}"""
    const val FILE_NOT_FOUND = """{"success":false,"message":"파일을 찾을 수 없습니다.","error":"Not Found"}"""
    
    const val INTERNAL_SERVER_ERROR = """{"success":false,"message":"서버 내부 오류입니다.","error":"Internal Server Error"}"""
    const val MAINTENANCE_MODE = """{"success":false,"message":"현재 시스템 점검 중입니다.","error":"Internal Server Error"}"""
    const val DATABASE_ERROR = """{"success":false,"message":"데이터베이스 오류가 발생했습니다.","error":"Internal Server Error"}"""
}