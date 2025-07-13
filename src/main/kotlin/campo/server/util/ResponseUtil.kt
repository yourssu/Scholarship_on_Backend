package campo.server.util

import io.vertx.ext.web.RoutingContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val error: String? = null
)

object ResponseUtil {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    fun success(context: RoutingContext, message: String = "성공", data: Any? = null) {
        val response = ApiResponse(
            success = true,
            message = message,
            data = data
        )
        sendJson(context, 200, response)
    }
    
    fun badRequest(context: RoutingContext, message: String = "잘못된 요청입니다.", error: String? = null) {
        val response = ApiResponse<Nothing>(
            success = false,
            message = message,
            error = error
        )
        sendJson(context, 400, response)
    }
    
    fun unauthorized(context: RoutingContext, message: String = "인증되지 않은 요청입니다.") {
        val response = ApiResponse<Nothing>(
            success = false,
            message = message,
            error = "Unauthorized"
        )
        sendJson(context, 401, response)
    }
    
    fun forbidden(context: RoutingContext, message: String = "권한이 없습니다.") {
        val response = ApiResponse<Nothing>(
            success = false,
            message = message,
            error = "Forbidden"
        )
        sendJson(context, 403, response)
    }
    
    fun notFound(context: RoutingContext, message: String = "찾을 수 없습니다.") {
        val response = ApiResponse<Nothing>(
            success = false,
            message = message,
            error = "Not Found"
        )
        sendJson(context, 404, response)
    }
    
    fun internalServerError(context: RoutingContext, message: String = "서버 내부 오류입니다.", error: String? = null) {
        val response = ApiResponse<Nothing>(
            success = false,
            message = message,
            error = error ?: "Internal Server Error"
        )
        sendJson(context, 500, response)
    }
    
    fun custom(context: RoutingContext, statusCode: Int, success: Boolean, message: String, data: Any? = null, error: String? = null) {
        val response = ApiResponse(
            success = success,
            message = message,
            data = data,
            error = error
        )
        sendJson(context, statusCode, response)
    }
    
    private fun sendJson(context: RoutingContext, statusCode: Int, response: ApiResponse<*>) {
        try {
            val jsonString = json.encodeToString(response)
            context.response()
                .setStatusCode(statusCode)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(jsonString)
        } catch (e: Exception) {
            context.response()
                .setStatusCode(500)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end("""{"success":false,"message":"응답 처리 중 오류가 발생했습니다","error":"${e.message}"}""")
        }
    }
    
//    fun sendFile(context: RoutingContext, filePath: String, onFailure: (String) -> Unit) {
//        context.response()
//            .putHeader("content-type", "text/plain; charset=utf-8")
//            .sendFile(filePath)
//            .onFailure { throwable ->
//                onFailure(throwable.message ?: "파일 전송 실패")
//            }
//    }
}