package campo.server.scholarship

import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.prev
import org.jsoup.Jsoup

private fun extractAndFormatDates(text: String): String {
    // Pattern 1: YYYY.MM.DD ~ YYYY.MM.DD
    val regex1 = """.*((?:'\d{2})|\d{4})\.\s*(\d{1,2})\.\s*(\d{1,2})[^~]*?~\s*((?:'\d{2})|\d{4})\.\s*(\d{1,2})\.\s*(\d{1,2})""".toRegex()
    var match = regex1.find(text)
    if (match != null) {
        val (startY, startM, startD, endY, endM, endD) = match.destructured
        val startYear = startY.replace("'", "20")
        val endYear = endY.replace("'", "20")
        return "${startYear}-${startM.padStart(2, '0')}-${startD.padStart(2, '0')} ~ ${endYear}-${endM.padStart(2, '0')}-${endD.padStart(2, '0')}"
    }

    // Pattern 2: YYYY.MM.DD ~ MM.DD
    val regex2 = """.*((?:'\d{2})|\d{4})\.\s*(\d{1,2})\.\s*(\d{1,2})[^~]*?~\s*(\d{1,2})\.\s*(\d{1,2})""".toRegex()
    match = regex2.find(text)
    if (match != null) {
        val (startY, startM, startD, endM, endD) = match.destructured
        val startYear = startY.replace("'", "20")
        return "${startYear}-${startM.padStart(2, '0')}-${startD.padStart(2, '0')} ~ ${startYear}-${endM.padStart(2, '0')}-${endD.padStart(2, '0')}"
    }

    return "신청 기간 정보 없음"
}

// 한국장학재단 장학금 로드
fun loadKOSAF(): DataFrame<*> {
    val baseUrl = "https://www.kosaf.go.kr"
    val url = "https://www.kosaf.go.kr/ko/notice.do?ctgrId1=0000000002"
    val docs = Jsoup.connect(url).get()

    val items = docs.select(".m_b20").select("a").filter { item ->
        item.text().contains("장학금")
    }

    val scholarNames = mutableListOf<String>()
    val scholarStarts = mutableListOf<String>()
    val scholarEnds = mutableListOf<String>()
    val scholarHomepages = mutableListOf<String>()


    for(element in items) {
        val info = Jsoup.connect(baseUrl + element.attr("href")).get().select("tbody").select(".subject")
        var result = info.find { item ->
            (item.text().contains("[0-9]+년".toRegex()) || item.text().contains("[0-9]+학년도".toRegex())) &&
                    item.text().contains("[1-2]학기".toRegex()) &&
                    item.text().contains("[1-2]차".toRegex()) &&
                    item.text().contains(element.text().split(" ").last())
        }

        if(result == null) {
            result = info.find { item ->
                (item.text().contains("신규") || item.text().contains("학생신청") || item.text().contains("학생 신청")) &&
                        item.text().contains("신청") &&
                        !item.text().contains("최종") &&
                        !item.text().contains("결과") &&
                        !item.text().contains("평가") &&
                        !item.text().contains("대학원")
            }
        }
        // 전문 기술인재
        if(result == null) {
            result = info.find { item ->
                item.text().contains("[0-9]+년".toRegex()) &&
                        item.text().contains("신규장학생 선발 공고")
            }
        }
        if(result != null) {
            val noticeURL = url.split("?")[0] + result.select("a").attr("href")
            val notice = Jsoup.connect(noticeURL).get()
            val content = notice.select("tbody").text()
            val dateRange = extractAndFormatDates(content)
            println(result.text() + " : " + dateRange)
            if(dateRange != "신청 기간 정보 없음") {
                scholarNames.add(result.text())
                scholarStarts.add(dateRange.split(" ~ ")[0])
                scholarEnds.add(dateRange.split(" ~ ")[1])
                scholarHomepages.add(noticeURL)
            }

        } else {
            println(element.text() + " : " + "결과 없음")
        }
    }
    val df = dataFrameOf(
        "상품명" to scholarNames,
        "홈페이지 주소" to scholarHomepages,
        "모집시작일" to scholarStarts,
        "모집종료일" to scholarEnds
    )
        .add("운영기관명") { "한국장학재단" }
        .add("상품구분") { "장학금" }
        .add("번호") {
            if (index() < 1) 10000
            else prev()!!.newValue<Int>() + 1
        }
    println(df)
    return df

}