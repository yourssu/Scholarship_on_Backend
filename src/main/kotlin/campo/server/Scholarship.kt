package campo.server

import campo.server.database.AuthDatabase
import campo.server.route.Auth
import campo.server.route.Common
import campo.server.route.Docs
import campo.server.route.ScholarshipInfo
import campo.server.util.ResponseUtil
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.internal.logging.LoggerFactory
import io.vertx.core.net.SelfSignedCertificate
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
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

    AuthDatabase(vertx)

    // Session
    mainRouter.route().handler(
        SessionHandler.create(LocalSessionStore.create(vertx))
            .setCookieHttpOnlyFlag(true)
            .setCookieSecureFlag(true) // HTTPS 관련 문제 발생 시 삭제 (HTTP 에서 비정상 동작할 수 있음)
            .setSessionTimeout(1800000)
    )

    // multipart
    mainRouter.route().handler(BodyHandler.create())
        .failureHandler {
            logger.error(it.failure().localizedMessage)
            ResponseUtil.badRequest(it, it.failure().localizedMessage)
        }

    // 라우터 인스턴스 생성 및 등록
    mainRouter.route("/api/auth/*").subRouter(Auth(vertx))
    mainRouter.route("/api/info/*").subRouter(ScholarshipInfo(vertx))
    mainRouter.route("/docs/*").subRouter(Docs(vertx))
    mainRouter.route("/common/*").subRouter(Common(vertx))

//    mainRouter.get("/").handler { context ->
//        context.response()
//            .setStatusCode(200)
//            .putHeader("Content-Type", "text/html; charset=utf-8")
//            .sendFile("web/index.html")
//
//    }


    // 그 이외의 접근은 웹 파일로 간주
    mainRouter.route().handler(Handler { req: RoutingContext? ->
        var file: String? = ""
        if (req!!.request().path() == "/") {
            file = "index.html"
        } else if (!req.request().path().contains("..")) { // 상위 폴더로 못가게
            file = req.request().path()
        }
        req.response()
            .setStatusCode(200)
            .putHeader("Content-Type", "text/html; charset=utf-8")
            .sendFile("web/$file")
            .onFailure(Handler { err: Throwable? ->
                req.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "text/html; charset=utf-8")
                    .end("NOT FOUND")
            })
    })

    val certificate: SelfSignedCertificate = SelfSignedCertificate.create()
    // 개방
    val server = vertx.createHttpServer(HttpServerOptions()
        .setSsl(true)
        .setKeyCertOptions(certificate.keyCertOptions())
        .setTrustOptions(certificate.trustOptions())
    )

    server.requestHandler(mainRouter).listen(HTTPS_PORT).onSuccess {server: HttpServer ->
        logger.info(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()) + "부로 서버 개통되었습니다.")
        logger.info("https://" + InetAddress.getLocalHost().hostAddress + ":" + server.actualPort())
    }.onFailure {
        logger.error(it.localizedMessage)
    }

}