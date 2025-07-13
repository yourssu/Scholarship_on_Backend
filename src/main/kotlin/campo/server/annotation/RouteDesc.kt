package campo.server.annotation

enum class HttpMethodType {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
}

annotation class RouteDesc(
    val path: String,
    val description: String,
    val method: HttpMethodType = HttpMethodType.GET,
    val successExample: String = "",
    val errorExamples: Array<String> = []
)
