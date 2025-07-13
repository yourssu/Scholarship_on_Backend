package campo.server.route

import campo.server.annotation.RouteDesc
import campo.server.annotation.HttpMethodType
import campo.server.util.ResponseUtil
import campo.server.util.ApiExamples
import io.vertx.core.Vertx
import io.vertx.core.internal.logging.LoggerFactory
import io.vertx.ext.web.impl.RouterImpl

@RouteDesc("/auth", "사용자 정보를 관리합니다.")
class Auth(vertx: Vertx) : RouterImpl(vertx) {
    val logger = LoggerFactory.getLogger(javaClass)

    init {

    }

    @RouteDesc(
        "/auth/login", 
        "로그인", 
        HttpMethodType.POST,
        successExample = ApiExamples.LOGIN_SUCCESS,
        errorExamples = [
            ApiExamples.INVALID_CREDENTIALS,
            ApiExamples.INVALID_EMAIL_FORMAT,
            ApiExamples.INTERNAL_SERVER_ERROR
        ]
    )
    fun login() {
        post("/login").handler { context ->
            val email = context.request().getFormAttribute("email")
            val password = context.request().getFormAttribute("password")

        }
    }

    @RouteDesc(
        "/auth/register", 
        "회원가입", 
        HttpMethodType.POST,
        successExample = ApiExamples.REGISTER_SUCCESS,
        errorExamples = [
            ApiExamples.EMAIL_ALREADY_EXISTS,
            ApiExamples.WEAK_PASSWORD,
            ApiExamples.MISSING_REQUIRED_FIELDS
        ]
    )
    fun register() {
        post("/register").handler { context ->
            val email = context.request().getFormAttribute("email")
            val password = context.request().getFormAttribute("password")
            val school = context.request().getFormAttribute("school") // 학교명
            val classOfSchool = context.request().getFormAttribute("class") // 학년
            val majorOfSchool = context.request().getFormAttribute("major") // 전공
            val location = context.request().getFormAttribute("location") // 거주지
            val levelOfIncome = context.request().getFormAttribute("income") // 소득분위
            val grade = context.request().getFormAttribute("grade") // 성적


        }
    }

    @RouteDesc(
        "/auth/",
        "회원정보 조회",
        HttpMethodType.GET,
        successExample = ApiExamples.USER_INFO_SUCCESS
    )
    fun getUserInfo() {
        get("/").handler { context ->

        }
    }

    @RouteDesc(
        "/auth/",
        "회원정보 수정",
        HttpMethodType.PUT,
        successExample = ApiExamples.UPDATE_SUCCESS
    )
    fun editUserInfo() {
        put("/").handler { context ->

        }
    }

    @RouteDesc(
        "/auth/",
        "회원탈퇴", HttpMethodType.DELETE,
        successExample = ApiExamples.DELETE_SUCCESS
    )
    fun deleteUser() {
        delete("/").handler { context ->

        }
    }

}