package campo.server.route

import campo.server.RouteDocGenerator
import campo.server.annotation.RouteDesc
import io.vertx.core.Vertx
import io.vertx.core.internal.logging.LoggerFactory
import io.vertx.ext.web.impl.RouterImpl

@RouteDesc("/docs", "API 문서를 제공합니다.")
class Docs(vertx: Vertx) : RouterImpl(vertx) {
    val logger = LoggerFactory.getLogger(javaClass)
    private val docGenerator = RouteDocGenerator()
    
    init {
        swagger()
        apiDocs()
        staticDocs()
    }
    
    @RouteDesc("/docs/swagger.json", "Swagger JSON 스펙을 제공합니다.")
    fun swagger() {
        get("/swagger.json").handler { context ->
            try {
                val swaggerJson = docGenerator.generateSwaggerJson()
                context.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(swaggerJson)
            } catch (e: Exception) {
                logger.error("Swagger JSON 생성 실패", e)
                context.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end("""{"error": "API 문서 생성에 실패했습니다."}""")
            }
        }
    }
    
    @RouteDesc("/docs/", "Swagger UI로 API 문서를 제공합니다.")
    fun apiDocs() {
        get("/").handler { context ->
            try {
                val swaggerUI = generateSwaggerUI()
                context.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "text/html; charset=utf-8")
                    .end(swaggerUI)
            } catch (e: Exception) {
                logger.error("Swagger UI 생성 실패", e)
                context.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "text/html; charset=utf-8")
                    .end("<h1>API 문서 생성에 실패했습니다.</h1>")
            }
        }
    }
    
    @RouteDesc("/docs/static", "정적 HTML 테이블로 API 문서를 제공합니다.")
    fun staticDocs() {
        get("/static").handler { context ->
            try {
                val htmlDoc = docGenerator.generateHtmlDoc()
                context.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "text/html; charset=utf-8")
                    .end(htmlDoc)
            } catch (e: Exception) {
                logger.error("정적 HTML 문서 생성 실패", e)
                context.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "text/html; charset=utf-8")
                    .end("<h1>API 문서 생성에 실패했습니다.</h1>")
            }
        }
    }
    private fun generateSwaggerUI(): String {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>ScholarshipON API Documentation</title>
                <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui.css" />
                <style>
                    html {
                        box-sizing: border-box;
                        overflow: -moz-scrollbars-vertical;
                        overflow-y: scroll;
                    }
                    *, *:before, *:after {
                        box-sizing: inherit;
                    }
                    body {
                        margin:0;
                        background: #fafafa;
                    }
                </style>
            </head>
            <body>
                <div id="swagger-ui"></div>
                <script src="https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui-bundle.js"></script>
                <script src="https://unpkg.com/swagger-ui-dist@5.10.3/swagger-ui-standalone-preset.js"></script>
                <script>
                    window.onload = function() {
                        const ui = SwaggerUIBundle({
                            url: '/docs/swagger.json',
                            dom_id: '#swagger-ui',
                            deepLinking: true,
                            presets: [
                                SwaggerUIBundle.presets.apis,
                                SwaggerUIStandalonePreset
                            ],
                            plugins: [
                                SwaggerUIBundle.plugins.DownloadUrl
                            ],
                            layout: "StandaloneLayout"
                        });
                    };
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}