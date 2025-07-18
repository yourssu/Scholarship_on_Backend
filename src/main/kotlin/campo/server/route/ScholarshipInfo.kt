package campo.server.route

import campo.server.annotation.RouteDesc
import campo.server.annotation.HttpMethodType
import campo.server.util.ApiExamples
import io.vertx.core.Vertx
import io.vertx.core.internal.logging.LoggerFactory
import io.vertx.ext.web.impl.RouterImpl

@RouteDesc("/info", "장학금 관련 정보를 관리합니다.")
class ScholarshipInfo(vertx: Vertx) : ScholarRouter(vertx) {
    val logger = LoggerFactory.getLogger(javaClass)

    init {
        allScholarship()
        recommendation()
        recommendation_detail()
        search()
    }

    @RouteDesc(
        "/info/",
        "전체 장학금 공고 리스트 조회",
        HttpMethodType.GET,
        ApiExamples.SCHOLARSHIP_LIST_SUCCESS,
        [
            ApiExamples.SCHOLARSHIP_NOT_FOUND
        ]
    )
    fun allScholarship() {

    }
    @RouteDesc(
        "/info/recommendation",
        "사용자 개인 조건에 맞는 장학금 공고 조회",
        HttpMethodType.GET,
        ApiExamples.SCHOLARSHIP_LIST_SUCCESS,
        [
            ApiExamples.NO_MATCHING_SCHOLARSHIPS
        ]
    )
    fun recommendation() {

    }

    @RouteDesc(
        "/info/recommendation/detail",
        "개별 장학금 상세 정보 조회",
        HttpMethodType.GET,
        ApiExamples.SCHOLARSHIP_DETAIL_SUCCESS,
        [
            ApiExamples.SCHOLARSHIP_NOT_FOUND
        ]
    )
    fun recommendation_detail() {

    }

    @RouteDesc(
        "/info/search",
        "장학금 검색",
        HttpMethodType.GET,
        ApiExamples.SCHOLARSHIP_LIST_SUCCESS,
        [
            ApiExamples.NO_MATCHING_SCHOLARSHIPS
        ]
    )
    fun search() {

    }



}