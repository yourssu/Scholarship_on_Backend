package campo.server.route

import campo.server.annotation.HttpMethodType
import campo.server.annotation.Parameter
import campo.server.annotation.ParameterType
import campo.server.annotation.RouteDesc
import campo.server.data.User
import campo.server.database.AuthDatabase
import campo.server.util.ApiExamples
import campo.server.util.ResponseUtil
import io.vertx.core.Vertx
import io.vertx.core.internal.logging.LoggerFactory
import io.vertx.ext.auth.authentication.AuthenticationProvider
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials

@RouteDesc("/api/auth", "사용자 정보를 관리합니다.")
class Auth(vertx: Vertx) : ScholarRouter(vertx) {
    val logger = LoggerFactory.getLogger(javaClass)
    val authDB = AuthDatabase(vertx)
    val sqlAuth: AuthenticationProvider = authDB.getSqlAuth()

    init {
        login()
        logout()
        register()
        editUserInfo()
        deleteUser()
        getUserInfo()
    }

    @RouteDesc(
        "/api/auth/login",
        "로그인", 
        HttpMethodType.POST,
        successExample = ApiExamples.LOGIN_SUCCESS,
        errorExamples = [
            ApiExamples.INVALID_CREDENTIALS,
            ApiExamples.INTERNAL_SERVER_ERROR
        ],
        parameters = [
            Parameter("email", "이메일 주소", ParameterType.FORM, true, "user@example.com"),
            Parameter("password", "비밀번호", ParameterType.FORM, true, "password123")
        ]
    )
    fun login() {
        post("/login").handler { context ->
            val email = context.request().getFormAttribute("email")
            val password = context.request().getFormAttribute("password")

            sqlAuth.authenticate(UsernamePasswordCredentials(email, password))
                .onSuccess {
                    setLoggedIn(context, it.principal())
                    ResponseUtil.success(context, "로그인 되었습니다.")
                }
                .onFailure {
                    ResponseUtil.badRequest(context, "이메일 또는 비밀번호가 일치하지 않습니다.")
                }

        }
    }

    @RouteDesc(
        "/api/auth/logout",
        "로그아웃",
        HttpMethodType.POST,
        successExample = ApiExamples.LOGOUT_SUCCESS,
        errorExamples = [
            ApiExamples.UNAUTHORIZED,
        ]
    )
    fun logout() {
        post("/logout").handler { context ->
            if(isLoggedIn(context)) {
                logout(context)
                ResponseUtil.success(context, "로그아웃 완료")
            } else {
                ResponseUtil.unauthorized(context)
            }
        }
    }

    @RouteDesc(
        "/api/auth/register",
        "회원가입", 
        HttpMethodType.POST,
        successExample = ApiExamples.REGISTER_SUCCESS,
        errorExamples = [
            ApiExamples.EMAIL_ALREADY_EXISTS,
            ApiExamples.WEAK_PASSWORD,
            ApiExamples.MISSING_REQUIRED_FIELDS
        ],
        parameters = [
            Parameter("email", "이메일 주소", ParameterType.FORM, true, "user@example.com"),
            Parameter("password", "비밀번호", ParameterType.FORM, true, "password123"),
            Parameter("school", "학교명", ParameterType.FORM, true, "숭실대학교"),
            Parameter("class", "학년", ParameterType.FORM, true, "1"),
            Parameter("major", "전공", ParameterType.FORM, true, "자유전공학부"),
            Parameter("location", "거주지", ParameterType.FORM, true, "서울 동작구"),
            Parameter("income", "소득분위", ParameterType.FORM, true, "5"),
            Parameter("grade", "성적", ParameterType.FORM, true, "3.8")
        ]
    )
    fun register() {
        post("/register").handler { context ->

            val email = context.request().getFormAttribute("email") ?: ""
            val password = context.request().getFormAttribute("password") ?: ""
            val school = context.request().getFormAttribute("school") ?: "" // 학교명
            val classOfSchool = context.request().getFormAttribute("class")?.toInt() ?: 0 // 학년
            val majorOfSchool = context.request().getFormAttribute("major") ?: "" // 전공
            val location = context.request().getFormAttribute("location") ?: "" // 거주지
            val levelOfIncome = context.request().getFormAttribute("income")?.toInt() ?: 0 // 소득분위
            val grade = context.request().getFormAttribute("grade")?.toDouble() ?: 0.0 // 성적

            val user = User(
                email,
                password,
                school,
                classOfSchool,
                majorOfSchool,
                location,
                levelOfIncome,
                grade
            )


            authDB.emailExists(email).onSuccess {
                if(it) {
                    ResponseUtil.badRequest(context, "이미 존재하는 이메일입니다.")
                } else {
                    authDB.registerUser(user).onSuccess { id ->
                        ResponseUtil.successTyped(context, "가입에 성공했습니다.", user)
                    }.onFailure {
                        logger.error(it.localizedMessage)
                        ResponseUtil.badRequest(context, it.localizedMessage)
                    }
                }
            }

        }
    }

    @RouteDesc(
        "/api/auth/",
        "회원정보 조회",
        HttpMethodType.GET,
        successExample = ApiExamples.USER_INFO_SUCCESS,
        errorExamples = [
            ApiExamples.UNAUTHORIZED,
            ApiExamples.INTERNAL_SERVER_ERROR
        ]
    )
    fun getUserInfo() {
        get("/").handler { context ->
            if(isLoggedIn(context)) {
                authDB.getUserByEmail(getEmailFromLoggedIn(context))
                    .onSuccess { user ->
                        if(user != null) {
                            setUserInfo(context, user)
                            ResponseUtil.successTyped(context, "회원정보를 성공적으로 불러왔습니다.", user)
                        } else {
                            ResponseUtil.internalServerError(context, "회원정보를 불러오지 못했습니다.")
                        }
                    }.onFailure {
                        logger.error(it)
                        ResponseUtil.internalServerError(context, it.localizedMessage)
                    }
            } else {
                ResponseUtil.unauthorized(context)
            }
        }
    }

    @RouteDesc(
        "/api/auth/",
        "회원정보 수정",
        HttpMethodType.PUT,
        successExample = ApiExamples.UPDATE_SUCCESS,
        errorExamples = [
            ApiExamples.INTERNAL_SERVER_ERROR
        ],
        parameters = [
            Parameter("password", "비밀번호 (변경시에만)", ParameterType.FORM, false, "newpassword123"),
            Parameter("school", "학교명", ParameterType.FORM, false, "숭실대학교"),
            Parameter("class", "학년", ParameterType.FORM, false, "1"),
            Parameter("major", "전공", ParameterType.FORM, false, "자유전공학부"),
            Parameter("location", "거주지", ParameterType.FORM, false, "서울 동작구"),
            Parameter("income", "소득분위", ParameterType.FORM, false, "5"),
            Parameter("grade", "성적", ParameterType.FORM, false, "3.8")
        ]
    )
    fun editUserInfo() {
        put("/").handler { context ->
            if(!isLoggedIn(context)) {
                ResponseUtil.unauthorized(context)
            }

            val email = context.request().getFormAttribute("email") ?: ""
            val password = context.request().getFormAttribute("password") ?: ""
            val school = context.request().getFormAttribute("school") ?: "" // 학교명
            val classOfSchool = context.request().getFormAttribute("class")?.toInt() ?: 0 // 학년
            val majorOfSchool = context.request().getFormAttribute("major") ?: "" // 전공
            val location = context.request().getFormAttribute("location") ?: "" // 거주지
            val levelOfIncome = context.request().getFormAttribute("income")?.toInt() ?: 0 // 소득분위
            val grade = context.request().getFormAttribute("grade")?.toDouble() ?: 0.0 // 성적

            val user = User(
                email,
                password,
                school,
                classOfSchool,
                majorOfSchool,
                location,
                levelOfIncome,
                grade
            )

            authDB.updateUser(getEmailFromLoggedIn(context), user)
                .onSuccess { id ->
                    ResponseUtil.success(context, "회원정보를 성공적으로 수정했습니다.")
                }.onFailure {
                    ResponseUtil.internalServerError(context, it.localizedMessage)
                }

        }
    }

    @RouteDesc(
        "/api/auth/",
        "회원탈퇴", HttpMethodType.DELETE,
        successExample = ApiExamples.DELETE_SUCCESS,
        errorExamples = [
            ApiExamples.UNAUTHORIZED,
            ApiExamples.INTERNAL_SERVER_ERROR
        ]
    )
    fun deleteUser() {
        delete("/").handler { context ->
            if(isLoggedIn(context)) {
                authDB.deleteUser(getEmailFromLoggedIn(context))
                    .onSuccess {
                        logout(context)  // 사용자 삭제 후 세션도 로그아웃
                        ResponseUtil.success(context, "회원정보를 삭제했습니다.")
                    }.onFailure {
                        ResponseUtil.internalServerError(context, it.localizedMessage)
                    }
            } else {
                ResponseUtil.unauthorized(context)
            }
        }
    }

}