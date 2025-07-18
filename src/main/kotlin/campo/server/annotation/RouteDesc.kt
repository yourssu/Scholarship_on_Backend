package campo.server.annotation

enum class HttpMethodType {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
}

enum class ParameterType {
    FORM, QUERY, PATH, HEADER
}

annotation class Parameter(
    val name: String,
    val description: String,
    val type: ParameterType = ParameterType.FORM,
    val required: Boolean = true,
    val example: String = ""
)

annotation class RouteDesc(
    val path: String,
    val description: String,
    val method: HttpMethodType = HttpMethodType.GET,
    val successExample: String = "",
    val errorExamples: Array<String> = [],
    val parameters: Array<Parameter> = []
)
