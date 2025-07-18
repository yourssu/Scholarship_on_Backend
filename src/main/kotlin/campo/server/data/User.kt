package campo.server.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String,
    val password: String,
    val school: String, // 학교
    val classOfSchool: Int, // 학년
    val majorOfSchool: String, // 전공
    val location: String, // 거주지
    val levelOfIncome: Int, // 소득분위
    val grade: Double // 성적
): JsonResponse
