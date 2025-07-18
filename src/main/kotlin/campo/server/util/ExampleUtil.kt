package campo.server.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

object ExampleUtil {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }
    
    // 성공 응답 예시 생성
    fun success(message: String = "성공", data: Any? = null): String {
        val response = ApiResponse(
            success = true,
            message = message,
            data = data
        )
        return json.encodeToString(response)
    }
    
    // 일반적인 오류 응답 예시들
    fun badRequest(message: String = "잘못된 요청입니다.", error: String? = null): String {
        val response = ApiResponse<Nothing>(
            success = false,
            message = message,
            error = error ?: "Bad Request"
        )
        return json.encodeToString(response)
    }
    
    fun unauthorized(message: String = "인증되지 않은 요청입니다."): String {
        val response = ApiResponse<Nothing>(
            success = false,
            message = message,
            error = "Unauthorized"
        )
        return json.encodeToString(response)
    }
    
    fun forbidden(message: String = "권한이 없습니다."): String {
        val response = ApiResponse<Nothing>(
            success = false,
            message = message,
            error = "Forbidden"
        )
        return json.encodeToString(response)
    }
    
    fun notFound(message: String = "찾을 수 없습니다."): String {
        val response = ApiResponse<Nothing>(
            success = false,
            message = message,
            error = "Not Found"
        )
        return json.encodeToString(response)
    }
    
    fun internalServerError(message: String = "서버 내부 오류입니다.", error: String? = null): String {
        val response = ApiResponse<Nothing>(
            success = false,
            message = message,
            error = error ?: "Internal Server Error"
        )
        return json.encodeToString(response)
    }
    
    // 커스텀 응답 예시 생성
    fun custom(success: Boolean, message: String, data: Any? = null, error: String? = null): String {
        val response = ApiResponse(
            success = success,
            message = message,
            data = data,
            error = error
        )
        return json.encodeToString(response)
    }
    
    // 자주 사용되는 데이터 타입별 예시들
    object Auth {
        fun loginSuccess(token: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."): String {
            return success("로그인 성공", mapOf("token" to token))
        }
        
        fun registerSuccess(userId: Long = 1, email: String = "user@example.com"): String {
            return success("회원가입 성공", mapOf("userId" to userId, "email" to email))
        }
        
        fun invalidCredentials(): String {
            return unauthorized("이메일 또는 비밀번호가 잘못되었습니다.")
        }
        
        fun emailAlreadyExists(): String {
            return badRequest("이미 존재하는 이메일입니다.")
        }
        
        fun weakPassword(): String {
            return badRequest("비밀번호는 8자 이상이어야 합니다.")
        }
    }
    
    object User {
        fun userInfo(
            id: Long = 1, 
            email: String = "user@example.com", 
            school: String = "숭실대학교",
            grade: String = "3학년"
        ): String {
            return success("사용자 정보 조회 성공", mapOf(
                "id" to id,
                "email" to email,
                "school" to school,
                "grade" to grade
            ))
        }
        
        fun updateSuccess(): String {
            return success("사용자 정보가 수정되었습니다.")
        }
        
        fun deleteSuccess(): String {
            return success("회원탈퇴가 완료되었습니다.")
        }
        
        fun userNotFound(): String {
            return notFound("사용자를 찾을 수 없습니다.")
        }
    }
    
    object Scholarship {
        fun scholarshipList(): String {
            return success("장학금 목록 조회 성공", listOf(
                mapOf(
                    "id" to 1,
                    "title" to "국가우수장학금",
                    "amount" to 5000000,
                    "deadline" to "2024-03-31"
                ),
                mapOf(
                    "id" to 2,
                    "title" to "지역인재장학금",
                    "amount" to 3000000,
                    "deadline" to "2024-04-15"
                )
            ))
        }
        
        fun scholarshipDetail(
            id: Long = 1,
            title: String = "국가우수장학금",
            description: String = "성적 우수 학생 대상 장학금"
        ): String {
            return success("장학금 상세 정보 조회 성공", mapOf(
                "id" to id,
                "title" to title,
                "description" to description,
                "amount" to 5000000,
                "deadline" to "2024-03-31",
                "requirements" to listOf("학점 3.5 이상", "소득분위 8분위 이하")
            ))
        }
        
        fun noMatchingScholarships(): String {
            return success("조건에 맞는 장학금이 없습니다.", emptyList<Any>())
        }
    }
    
    object Common {
        fun fileNotFound(): String {
            return notFound("파일을 찾을 수 없습니다.")
        }
        
        fun maintenanceMode(): String {
            return internalServerError("현재 시스템 점검 중입니다.")
        }
    }
}