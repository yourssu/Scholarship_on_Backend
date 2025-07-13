package campo.server.route

import campo.server.annotation.RouteDesc
import campo.server.annotation.HttpMethodType
import campo.server.util.ResponseUtil
import io.vertx.core.Vertx
import io.vertx.core.internal.logging.LoggerFactory
import io.vertx.ext.web.impl.RouterImpl

@RouteDesc(path = "/common", "각종 서식을 처리합니다.")
class Common(vertx: Vertx) : RouterImpl(vertx) {
    val logger = LoggerFactory.getLogger(javaClass)

    init {
        privacy()
        term()
    }

    @RouteDesc("/common/privacy", "개인정보 처리방침을 제공합니다.", HttpMethodType.GET)
    fun privacy() {
        get("/privacy").handler { context ->
            context.response()
                .setStatusCode(200)
                .putHeader("content-type", "text/plain; charset=utf-8")
                .sendFile("privacy.txt")
                .onFailure {
                    context.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "text/plain; charset=utf-8")
                        .end("현재 개인정보 처리방침 제공이 어렵습니다.")
                }
        }
    }

    @RouteDesc("/common/term", "이용약관을 제공합니다.", HttpMethodType.GET)
    fun term() {
        get("/term").handler { context ->
            context.response()
                .setStatusCode(200)
                .putHeader("content-type", "text/plain; charset=utf-8")
                .sendFile("term.txt")
                .onFailure {
                    context.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "text/plain; charset=utf-8")
                        .end("현재 이용약관 제공이 어렵습니다.")
                }
        }
    }
}