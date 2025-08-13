package campo.server.util

import campo.server.annotation.RouteDesc
import campo.server.annotation.HttpMethodType
import campo.server.annotation.Parameter
import campo.server.annotation.ParameterType
import campo.server.route.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

data class RouteInfo(
    val path: String,
    val description: String,
    val className: String,
    val methodName: String,
    val httpMethod: HttpMethodType,
    val successExample: String,
    val errorExamples: Array<String>,
    val parameters: Array<Parameter>
)

class RouteDocGenerator {
    
    private val routeClasses = listOf(
        Auth::class,
        Common::class,
        ScholarshipInfo::class,
        Docs::class
    )
    
    fun scanRoutes(): List<RouteInfo> {
        val routes = mutableListOf<RouteInfo>()
        
        routeClasses.forEach { clazz ->
            val classAnnotation = clazz.findAnnotation<RouteDesc>()
            
            clazz.functions.forEach { function ->
                val methodAnnotation = function.findAnnotation<RouteDesc>()
                methodAnnotation?.let { annotation ->
                    routes.add(
                        RouteInfo(
                            path = annotation.path,
                            description = annotation.description,
                            className = clazz.simpleName ?: "",
                            methodName = function.name,
                            httpMethod = annotation.method,
                            successExample = annotation.successExample,
                            errorExamples = annotation.errorExamples,
                            parameters = annotation.parameters
                        )
                    )
                }
            }
        }
        
        return routes.sortedBy { it.path }
    }
    
    fun generateSwaggerJson(): String {
        val routes = scanRoutes()
        
        val paths = routes.groupBy { it.path }.map { (path, routeList) ->
            val operations = routeList.map { route ->
                val methodName = route.httpMethod.name.lowercase()
                
                // Parameters와 RequestBody 생성
                val formParams = route.parameters.filter { it.type == ParameterType.FORM }
                val otherParams = route.parameters.filter { it.type != ParameterType.FORM }
                
                val parametersJson = if (otherParams.isNotEmpty()) {
                    val params = otherParams.map { param ->
                        val paramIn = when (param.type) {
                            ParameterType.QUERY -> "query"
                            ParameterType.PATH -> "path"
                            ParameterType.HEADER -> "header"
                            else -> "query"
                        }
                        """
                        {
                            "name": "${param.name}",
                            "in": "$paramIn",
                            "description": "${param.description}",
                            "required": ${param.required},
                            "schema": {
                                "type": "string",
                                "example": "${param.example}"
                            }
                        }""".trimIndent()
                    }.joinToString(",\n")
                    """"parameters": [$params],"""
                } else ""
                
                val requestBodyJson = if (formParams.isNotEmpty()) {
                    val properties = formParams.map { param ->
                        """"${param.name}": {
                            "type": "string",
                            "description": "${param.description}",
                            "example": "${param.example}"
                        }"""
                    }.joinToString(",\n")
                    
                    val required = formParams.filter { it.required }.map { "\"${it.name}\"" }.joinToString(",")
                    val requiredSection = if (required.isNotEmpty()) """,
                        "required": [$required]""" else ""
                    
                    """"requestBody": {
                        "content": {
                            "application/x-www-form-urlencoded": {
                                "schema": {
                                    "type": "object",
                                    "properties": {
                                        $properties
                                    }$requiredSection
                                }
                            }
                        }
                    },"""
                } else ""
                
                val responses = buildString {
                    append("""
                        "200": {
                            "description": "Success"
                    """.trimIndent())
                    
                    if (route.successExample.isNotEmpty()) {
                        append(""",
                            "content": {
                                "application/json": {
                                    "example": ${route.successExample}
                                }
                            }""")
                    }
                    append("\n                        }")
                    
                    if (route.errorExamples.isNotEmpty()) {
                        val errorStatusCodes = listOf("400", "401", "403", "404", "500")
                        route.errorExamples.forEachIndexed { index, errorExample ->
                            val statusCode = if (index < errorStatusCodes.size) errorStatusCodes[index] else "500"
                            val description = when (statusCode) {
                                "400" -> "Bad Request"
                                "401" -> "Unauthorized"
                                "403" -> "Forbidden"
                                "404" -> "Not Found"
                                "500" -> "Internal Server Error"
                                else -> "Error"
                            }
                            append(""",
                        "$statusCode": {
                            "description": "$description",
                            "content": {
                                "application/json": {
                                    "example": $errorExample
                                }
                            }
                        }""")
                        }
                    }
                }
                
                """
                    "$methodName": {
                        "summary": "${route.description}",
                        "tags": ["${route.className}"],
                        $parametersJson
                        $requestBodyJson
                        "responses": {
                            $responses
                        }
                    }
                """.trimIndent()
            }.joinToString(",\n")
            
            """
                "$path": {
                    $operations
                }
            """.trimIndent()
        }.joinToString(",\n")
        
        return """
            {
                "openapi": "3.0.0",
                "info": {
                    "title": "ScholarshipON API",
                    "version": "1.0.0",
                    "description": "장학금 관리 시스템 API"
                },
                "paths": {
                    $paths
                }
            }
        """.trimIndent()
    }
    
    fun generateHtmlDoc(): String {
        val routes = scanRoutes()
        
        val routeRows = routes.map { route ->
            val successExample = if (route.successExample.isNotEmpty()) {
                "<details><summary>성공 예시</summary><pre>${route.successExample}</pre></details>"
            } else ""
            
            val errorExamples = if (route.errorExamples.isNotEmpty()) {
                route.errorExamples.mapIndexed { index, example ->
                    "<details><summary>오류 예시 ${index + 1}</summary><pre>$example</pre></details>"
                }.joinToString("<br>")
            } else ""
            
            val examples = listOfNotNull(
                if (successExample.isNotEmpty()) successExample else null,
                if (errorExamples.isNotEmpty()) errorExamples else null
            ).joinToString("<br>")
            
            """
                <tr>
                    <td><code>${route.httpMethod.name}</code></td>
                    <td><code>${route.path}</code></td>
                    <td>${route.description}</td>
                    <td>${route.className}</td>
                    <td>${route.methodName}</td>
                    <td>${if (examples.isNotEmpty()) examples else "없음"}</td>
                </tr>
            """.trimIndent()
        }.joinToString("\n")
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>ScholarshipON API Documentation</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    table { border-collapse: collapse; width: 100%; }
                    th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                    th { background-color: #f2f2f2; }
                    code { background-color: #f4f4f4; padding: 2px 4px; border-radius: 3px; }
                    h1 { color: #333; }
                </style>
            </head>
            <body>
                <h1>ScholarshipON API Documentation</h1>
                <table>
                    <thead>
                        <tr>
                            <th>HTTP Method</th>
                            <th>Path</th>
                            <th>Description</th>
                            <th>Class</th>
                            <th>Method</th>
                            <th>응답 예시</th>
                        </tr>
                    </thead>
                    <tbody>
                        $routeRows
                    </tbody>
                </table>
            </body>
            </html>
        """.trimIndent()
    }
}