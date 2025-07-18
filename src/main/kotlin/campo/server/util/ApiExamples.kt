package campo.server.util

object ApiExamples {
    // 성공 응답 예시들
    const val LOGIN_SUCCESS = """{"success":true,"message":"로그인 되었습니다."}"""
    const val LOGOUT_SUCCESS = """{"success":true,"message":"로그아웃 완료"}"""
    const val REGISTER_SUCCESS = """{"success":true,"message":"가입에 성공했습니다.","data":{"email":"user@example.com","password":"","school":"숭실대학교","classOfSchool":1,"majorOfSchool":"자유전공학부","location":"서울 동작구","levelOfIncome":5,"grade":3.8}}"""
    const val USER_INFO_SUCCESS = """{"success":true,"message":"회원정보를 성공적으로 불러왔습니다.","data":{"email":"user@example.com","password":"","school":"숭실대학교","classOfSchool":1,"majorOfSchool":"자유전공학부","location":"서울 동작구","levelOfIncome":5,"grade":3.8}}"""
    const val UPDATE_SUCCESS = """{"success":true,"message":"회원정보를 성공적으로 수정했습니다."}"""
    const val DELETE_SUCCESS = """{"success":true,"message":"회원정보를 삭제했습니다."}"""
    
    const val SCHOLARSHIP_LIST_SUCCESS = """{"success":true,"message":"장학금 목록 조회 성공","data":[{"id":1,"title":"국가우수장학금","amount":5000000,"deadline":"2024-03-31"},{"id":2,"title":"지역인재장학금","amount":3000000,"deadline":"2024-04-15"}]}"""
    const val SCHOLARSHIP_DETAIL_SUCCESS = """{"success":true,"message":"장학금 상세 정보 조회 성공","data":{"id":1,"title":"국가우수장학금","description":"성적 우수 학생 대상 장학금","amount":5000000,"deadline":"2024-03-31","requirements":["학점 3.5 이상","소득분위 8분위 이하"]}}"""
    const val NO_MATCHING_SCHOLARSHIPS = """{"success":true,"message":"조건에 맞는 장학금이 없습니다.","data":[]}"""
    
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