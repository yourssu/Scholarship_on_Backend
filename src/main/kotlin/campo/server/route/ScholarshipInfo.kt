package campo.server.route

import campo.server.annotation.HttpMethodType
import campo.server.annotation.Parameter
import campo.server.annotation.ParameterType
import campo.server.annotation.RouteDesc
import campo.server.util.ApiExamples
import campo.server.util.ResponseUtil
import io.vertx.core.Vertx
import io.vertx.core.internal.logging.LoggerFactory
import kotlinx.datetime.LocalDate
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.dropNulls
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.getRows
import org.jetbrains.kotlinx.dataframe.api.sortByDesc
import org.jetbrains.kotlinx.dataframe.io.readCsvStr
import org.jetbrains.kotlinx.dataframe.io.toJson
import java.io.BufferedReader
import java.nio.charset.Charset

@RouteDesc("/api/info", "장학금 관련 정보를 관리합니다.")
class ScholarshipInfo(vertx: Vertx) : ScholarRouter(vertx) {
    val logger = LoggerFactory.getLogger(javaClass)
    val df: DataFrame<*>

    init {
        logger.info("장학금 정보를 불러옵니다.")

        val inputStream = javaClass.getResourceAsStream("/scholarData.csv")
        val br = BufferedReader(inputStream!!.reader(Charset.forName("EUC-KR")))
        val sb = StringBuilder()
        var ls: String = System.getProperty("line.separator")
        while(br.readLine().also { sb.append(it); sb.append(ls); } != null) {}
        br.close()

        df = DataFrame.readCsvStr(sb.toString())

        logger.info("장학금 정보를 불러왔습니다. 데이터 수 : ${df.rowsCount()}")

        allScholarship()
        totalScholarshipDataSize()
        recommendation()
        recommendationWithoutAuth()
//        recommendationDetail()
        search()
    }

    @RouteDesc(
        "/api/info/",
        "전체 장학금 공고 리스트 조회",
        HttpMethodType.GET,
        ApiExamples.SCHOLARSHIP_LIST_SUCCESS,
        [
            ApiExamples.INTERNAL_SERVER_ERROR
        ],
        parameters = [
            Parameter("page", "페이지(0부터 n까지) (기본값 0)", ParameterType.QUERY, false, "0"),
            Parameter("each", "페이지 당 장학금 공고 수 (기본값 10)", ParameterType.QUERY, false, "10")
        ]
    )
    fun allScholarship() {
        get("/").handler { context ->
            val page = context.request().getParam("page")?.toInt() ?: 0
            val each = context.request().getParam("each")?.toInt() ?: 10

            ResponseUtil.successJson(context, "장학금 공고를 불러왔습니다.",
                run {
                    val sortedDf = df.sortByDesc { "모집종료일"<LocalDate>() }
                    val totalCount = sortedDf.rowsCount()
                    val startIndex = page * each
                    val endIndex = minOf(startIndex + each, totalCount)
                    
                    if (startIndex >= totalCount) {
                        sortedDf.getRows(0 until 0)
                    } else {
                        sortedDf.getRows(startIndex until endIndex)
                    }
                }.toJson()
                )
        }
    }

    @RouteDesc(
        "/api/info/length",
        "전체 장학금 데이터 수 조회. 전체 공고 페이지네이션 구현에 사용하세요",
        HttpMethodType.GET,
        ApiExamples.SCHOLARSHIP_COUNT_SUCCESS,
        [
            ApiExamples.INTERNAL_SERVER_ERROR
        ]
    )
    fun totalScholarshipDataSize() {
        get("/length").handler { context ->
            ResponseUtil.successTyped(context, "장학금 공고 데이터 수를 불러왔습니다.",
                df.rowsCount()
            )
        }
    }

    @RouteDesc(
        "/api/info/recommendation",
        "사용자 개인 조건에 맞는 장학금 공고 조회 (로그인 필요)",
        HttpMethodType.POST,
        ApiExamples.SCHOLARSHIP_RECOMMENDATION_SUCCESS,
        [
            ApiExamples.UNAUTHORIZED,
            ApiExamples.NO_MATCHING_SCHOLARSHIPS
        ],
        parameters = [
            Parameter("page", "페이지(0부터 n까지) (기본값 0)", ParameterType.QUERY, false, "0"),
            Parameter("each", "페이지 당 장학금 공고 수 (기본값 10)", ParameterType.QUERY, false, "10")
        ]
    )
    fun recommendation() {
        post("/recommendation").handler { context ->
            if(!isLoggedIn(context)) {
                ResponseUtil.unauthorized(context)
                return@handler
            }

            val user = getUserInfo(context)
            if (user == null) {
                ResponseUtil.badRequest(context, "사용자 정보를 찾을 수 없습니다.")
                return@handler
            }

            val page = context.request().getParam("page")?.toInt() ?: 0
            val each = context.request().getParam("each")?.toInt() ?: 10

            val filteredScholarships = df.dropNulls("학년구분")
                .dropNulls("학과구분")
                .dropNulls("지역거주여부 상세내용")
                .dropNulls("소득기준 상세내용")
                .dropNulls("성적기준 상세내용")
                .filter { row ->
                    // 학년 조건 확인 (학년을 학기로 변환)
                    val classCondition = "학년구분"<String>()
                    val classMatch = classCondition == "해당없음" || 
                        classCondition.split("/").any { semester ->
                            val userSemesters = getSemestersFromClass(user.classOfSchool)
                            userSemesters.any { userSemester -> semester.contains("${userSemester}학기") }
                        }
                    
                    // 전공 조건 확인  
                    val majorCondition = "학과구분"<String>()
                    val majorMatch = majorCondition == "해당없음" || 
                        majorCondition.contains(user.majorOfSchool) ||
                        majorCondition.contains("전체학과")
                    
                    // 거주지 조건 확인
                    val locationCondition = "지역거주여부 상세내용"<String>()
                    val locationMatch = locationCondition == "해당없음" || 
                        locationCondition.contains(user.location)
                    
                    // 소득 조건 확인 (소득분위가 낮을수록 조건에 부합)
                    val incomeCondition = "소득기준 상세내용"<String>()
                    val incomeMatch = incomeCondition == "해당없음" || 
                        incomeCondition.contains("제한없음") ||
                        incomeCondition.contains("${user.levelOfIncome}분위")
                    
                    // 성적 조건 확인 (문자열에서 숫자 추출하여 비교)
                    val gradeCondition = "성적기준 상세내용"<String>()
                    val gradeMatch = gradeCondition == "해당없음" || 
                        gradeCondition.contains("제한없음") ||
                        extractGradeFromString(gradeCondition)?.let { requiredGrade ->
                            user.grade >= requiredGrade
                        } ?: true
                    
                    classMatch && majorMatch && locationMatch && incomeMatch && gradeMatch
                }
                .sortByDesc { "모집종료일"<LocalDate>() }

            val responseJson = createPaginatedResponse(filteredScholarships, page, each)

            ResponseUtil.successJson(context, "개인별 장학금 공고를 불러왔습니다.",
                responseJson
            )
        }
    }

    @RouteDesc(
        "/api/info/recommendation",
        "사용자 개인 조건에 맞는 장학금 공고 조회 (로그인 불필요)",
        HttpMethodType.GET,
        ApiExamples.SCHOLARSHIP_RECOMMENDATION_SUCCESS,
        [
            ApiExamples.NO_MATCHING_SCHOLARSHIPS
        ],
        parameters = [
            Parameter("classOfSchool", "학년 (1-6)", ParameterType.QUERY, false, "1"),
            Parameter("majorOfSchool", "전공", ParameterType.QUERY, false, "인문계열"),
            Parameter("location", "거주지역", ParameterType.QUERY, false, "서울"),
            Parameter("levelOfIncome", "소득분위 (1-10)", ParameterType.QUERY, false, "3"),
            Parameter("grade", "성적 (0-4.5)", ParameterType.QUERY, false, "3.5"),
            Parameter("page", "페이지(0부터 n까지) (기본값 0)", ParameterType.QUERY, false, "0"),
            Parameter("each", "페이지 당 장학금 공고 수 (기본값 10)", ParameterType.QUERY, false, "10")
        ]
    )
    fun recommendationWithoutAuth() {
        get("/recommendation").handler { context ->
            val classOfSchool = context.request().getParam("classOfSchool")?.toIntOrNull()
            val majorOfSchool = context.request().getParam("majorOfSchool")?.takeIf { it.isNotBlank() }
            val location = context.request().getParam("location")?.takeIf { it.isNotBlank() }
            val levelOfIncome = context.request().getParam("levelOfIncome")?.toIntOrNull()
            val grade = context.request().getParam("grade")?.toDoubleOrNull()
            val page = context.request().getParam("page")?.toInt() ?: 0
            val each = context.request().getParam("each")?.toInt() ?: 10

            val filteredScholarships = df.dropNulls("학년구분")
                .dropNulls("학과구분")
                .dropNulls("지역거주여부 상세내용")
                .dropNulls("소득기준 상세내용")
                .dropNulls("성적기준 상세내용")
                .filter { row ->
                    // 학년 조건 확인 (학년을 학기로 변환)
                    val classCondition = "학년구분"<String>()
                    val classMatch = classOfSchool == null || classCondition == "해당없음" ||
                            classCondition.split("/").any { semester ->
                                val userSemesters = getSemestersFromClass(classOfSchool)
                                userSemesters.any { userSemester -> semester.contains("${userSemester}학기") }
                            }

                    // 전공 조건 확인
                    val majorCondition = "학과구분"<String>()
                    val majorMatch = majorOfSchool == null || majorCondition == "해당없음" ||
                            majorCondition.contains(majorOfSchool) ||
                            majorCondition.contains("전체학과")

                    // 거주지 조건 확인
                    val locationCondition = "지역거주여부 상세내용"<String>()
                    val locationMatch = location == null || locationCondition == "해당없음" ||
                            locationCondition.contains(location)

                    // 소득 조건 확인 (소득분위가 낮을수록 조건에 부합)
                    val incomeCondition = "소득기준 상세내용"<String>()
                    val incomeMatch = levelOfIncome == null || incomeCondition == "해당없음" ||
                            incomeCondition.contains("제한없음") ||
                            incomeCondition.contains("${levelOfIncome}분위")

                    // 성적 조건 확인 (문자열에서 숫자 추출하여 비교)
                    val gradeCondition = "성적기준 상세내용"<String>()
                    val gradeMatch = grade == null || gradeCondition == "해당없음" ||
                            gradeCondition.contains("제한없음") ||
                            extractGradeFromString(gradeCondition)?.let { requiredGrade ->
                                grade >= requiredGrade
                            } ?: true

                    classMatch && majorMatch && locationMatch && incomeMatch && gradeMatch
                }
                .sortByDesc { "모집종료일"<LocalDate>() }

            val responseJson = createPaginatedResponse(filteredScholarships, page, each)

            ResponseUtil.successJson(context, "개인별 장학금 공고를 불러왔습니다.",
                responseJson
            )
        }
    }

    private fun extractGradeFromString(gradeString: String): Double? {
        val regex = Regex("""(\d+\.?\d*)\s*이상""")
        val matchResult = regex.find(gradeString)
        return matchResult?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun getSemestersFromClass(classYear: Int): List<Int> {
        return when (classYear) {
            1 -> listOf(1, 2)
            2 -> listOf(3, 4)
            3 -> listOf(5, 6)
            4 -> listOf(7, 8)
            5 -> listOf(9, 10)
            6 -> listOf(11, 12)
            else -> emptyList()
        }
    }

    private fun createPaginatedResponse(
        dataFrame: DataFrame<*>,
        page: Int,
        each: Int,
        message: String = "데이터를 불러왔습니다."
    ): String {
        val totalCount = dataFrame.rowsCount()
        val totalPages = if (totalCount == 0) 0 else (totalCount - 1) / each + 1
        val startIndex = page * each
        val endIndex = minOf(startIndex + each, totalCount)
        
        val pagedData = if (startIndex >= totalCount) {
            dataFrame.getRows(0 until 0)
        } else {
            dataFrame.getRows(startIndex until endIndex)
        }

        val scholarshipsJson = pagedData.toJson()
        val paginationJson = """
            {
                "currentPage": $page,
                "pageSize": $each,
                "totalCount": $totalCount,
                "totalPages": $totalPages,
                "hasNext": ${page < totalPages - 1},
                "hasPrev": ${page > 0}
            }
        """.trimIndent()
        
        return """
            {
                "scholarships": $scholarshipsJson,
                "pagination": $paginationJson
            }
        """.trimIndent()
    }

//    @RouteDesc(
//        "/info/recommendation/detail",
//        "개별 장학금 상세 정보 조회",
//        HttpMethodType.GET,
//        ApiExamples.SCHOLARSHIP_DETAIL_SUCCESS,
//        [
//            ApiExamples.SCHOLARSHIP_NOT_FOUND
//        ]
//    )
//    fun recommendationDetail() {
//
//    }

    @RouteDesc(
        "/api/info/search",
        "장학금 검색",
        HttpMethodType.GET,
        ApiExamples.SCHOLARSHIP_SEARCH_SUCCESS,
        [
            ApiExamples.SEARCH_KEYWORD_REQUIRED,
        ],
        parameters = [
            Parameter("keywords", "검색 키워드 (장학금명 또는 기관명)", ParameterType.QUERY, true, "광주"),
            Parameter("page", "페이지(0부터 n까지) (기본값 0)", ParameterType.QUERY, false, "0"),
            Parameter("each", "페이지 당 장학금 공고 수 (기본값 10)", ParameterType.QUERY, false, "10")
        ]
    )
    fun search() {
        get("/search").handler { context ->
            val keywords = context.request().getParam("keywords") ?: ""
            if(keywords.isBlank() || keywords.isEmpty()) {
                ResponseUtil.badRequest(context, "검색어를 넣어주세요.")
                return@handler
            }
            val page = context.request().getParam("page")?.toInt() ?: 0
            val each = context.request().getParam("each")?.toInt() ?: 10

            val filteredScholarships = df.dropNulls("운영기관명").dropNulls("상품명").filter {
                "운영기관명"<String>().contains(keywords) || "상품명"<String>().contains(keywords)
            }.sortByDesc { "모집종료일"<LocalDate>() }

            val responseJson = createPaginatedResponse(filteredScholarships, page, each)

            ResponseUtil.successJson(context, "장학금 공고를 불러왔습니다.",
                responseJson
            )
        }
    }



}