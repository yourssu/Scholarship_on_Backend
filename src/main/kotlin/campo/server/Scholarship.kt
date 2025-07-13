package campo.server

import campo.server.annotation.RouteDesc
import campo.server.route.Auth
import campo.server.route.Common
import campo.server.route.Docs
import campo.server.route.ScholarshipInfo
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.core.internal.logging.LoggerFactory
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*

val logger = LoggerFactory.getLogger("Scholarship")

val HTTP_PORT = 80
val HTTPS_PORT = 443

fun main() {
    val vertx = Vertx.vertx()
    val mainRouter = Router.router(vertx)


    // Session
    mainRouter.route().handler(
        SessionHandler.create(LocalSessionStore.create(vertx))
            .setCookieHttpOnlyFlag(true)
//            .setCookieSecureFlag(true) // HTTPS 관련 문제 발생 시 삭제 (HTTP 에서 비정상 동작할 수 있음)
            .setSessionTimeout(1800000)
    )

    // multipart
    mainRouter.route().handler(BodyHandler.create())

    // 라우터 인스턴스 생성 및 등록
    mainRouter.route("/api/auth/*").subRouter(Auth(vertx))
    mainRouter.route("/api/info/*").subRouter(ScholarshipInfo(vertx))
    mainRouter.route("/docs/*").subRouter(Docs(vertx))
    mainRouter.route("/common/*").subRouter(Common(vertx))

    mainRouter.get("/").handler { context ->
        val sb = StringBuilder()

        /**
         *
         */

        val commonClass = Common::class.java
        sb.append("<h3>" + (commonClass.annotations.find { it is RouteDesc } as RouteDesc).description + "</h3><ul>")
        for (method in commonClass.declaredMethods) {
            val desc = method.annotations.forEach {
                if(it is RouteDesc) {
                    sb.append("<li>" +  it.path + " (" + it.description + ")</li>")
                }
            }
        }
        sb.append("</ul>")

        /**
         *
         */

        val authClass = Auth::class.java
        sb.append("<h3>" + (authClass.annotations.find { it is RouteDesc } as RouteDesc).description + "</h3><ul>")
        for (method in authClass.declaredMethods) {
            val desc = method.annotations.forEach {
                if(it is RouteDesc) {
                    sb.append("<li>" +  it.path + " (" + it.description + ")</li>")
                }
            }
        }
        sb.append("</ul>")

        /**
         *
         */

        val infoClass = ScholarshipInfo::class.java
        sb.append("<h3>" + (infoClass.annotations.find { it is RouteDesc } as RouteDesc).description + "</h3><ul>")
        for (method in infoClass.declaredMethods) {
            val desc = method.annotations.forEach {
                if(it is RouteDesc) {
                    sb.append("<li>" +  it.path + " (" + it.description + ")</li>")
                }
            }
        }
        sb.append("</ul>")


        context.response()
            .setStatusCode(200)
            .putHeader("Content-Type", "text/html; charset=utf-8")
            .end(sb.toString())

    }


    // 개방
    val server = vertx.createHttpServer()
    server.requestHandler(mainRouter).listen(HTTP_PORT).onSuccess {server: HttpServer ->
        logger.info(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()) + "부로 서버 개통되었습니다.")
        logger.info("http://" + InetAddress.getLocalHost().hostAddress + ":" + server.actualPort())
    }

}