let user = undefined
let currentPage = 0
let currentPageSize = 5
let totalScholarships = 0
let scholarshipData = []
let isSearchMode = false
let isRecommendMode = false
let isFilterMode = false
let currentSearchKeyword = ""
let currentPagination = null
let currentFilterValues = {
    classOfSchool: "",
    majorOfSchool: "",
    location: "",
    levelOfIncome: "",
    grade: ""
}

$(document).ready(function () {
    checkUser()
    
    // URL에서 상태 복원 시도
    if (!restoreFromURL()) {
        loadScholarships()
    }
    
    // 검색 버튼 클릭
    $("#searchBtn").click(function() {
        loadScholarships()
    })
    
    // 나만의 추천 버튼 클릭
    $("#recommendBtn").click(function() {
        if (user) {
            loadRecommendedScholarships()
        } else {
            // 비로그인 사용자에게 필터 옵션 제공
            toggleFilterSection()
        }
    })
    
    // 필터 적용 버튼 클릭
    $("#filterRecommendBtn").click(function() {
        loadFilteredRecommendations()
    })
    
    // 공유 버튼 클릭
    $("#shareBtn").click(function() {
        shareCurrentState()
    })
    
    // 페이지 크기 변경
    $("#pageSize").change(function() {
        currentPageSize = parseInt($(this).val())
        currentPage = 0
        if (isFilterMode) {
            loadFilteredRecommendations()
        } else if (isSearchMode) {
            loadScholarships()
        } else if (isRecommendMode) {
            loadRecommendedScholarships()
        } else {
            loadScholarships()
        }
    })
    
    // 엔터키로 검색
    $("#searchInput").keypress(function(e) {
        if(e.which == 13) {
            loadScholarships()
        }
    })
})

$("#loginframe").on("load", function () {
    fillUserInfo()

    // $("#loginframeCol").width(this.contentDocument.body.offsetWidth);
    $("#loginframe").height(this.contentDocument.body.offsetHeight);

    $("#loginframe").contents().find("#registerBtn").click(function () {
        $("#loginframe").attr("src", "/register.html");
    })
    $("#loginframe").contents().find("#logoutBtn").click(function () {
        fetch("api/auth/logout", {
            method: "POST",
        }).then(response => response.json())
            .then((data) => {
                user = undefined
                showToast(data.message, !data.success)
            }).catch((error) => {
            showToast(error, true)
        })
        $("#loginframe").attr("src", "/login.html");
    })
    $("#loginframe").contents().find("#loginBtn").click(function () {
        $("#loginframe").attr("src", "/login.html");
    })

    $("#loginframe").contents().find("#goBackAccountBtn").click(function () {
        checkUser()
    })

    $("#loginframe").contents().find("#EditInfoBtn").click(function () {
        $("#loginframe").attr("src", "/editinfo.html");
    })

    $("#loginframe").contents().find("#unregisterBtn").click(function (e) {

        fetch("api/auth/", {
            method: "DELETE"
        }).then(response => response.json()).then((data) => {
            user = undefined
            if(data.success) {
                showToast(data.message, false)
                $("#loginframe").attr("src", "/login.html");
            } else {
                showToast(data.message, true)
            }
        }).catch((error) => {
            showToast(error, true)
        })
    })

    $("#loginframe").contents().find("#login").submit(function (e) {
        e.preventDefault()

        let email = $("#loginframe").contents().find("#email").val()
        let password = $("#loginframe").contents().find("#password").val()
        if (email.length === 0 || password.length === 0) {
            showToast("ID와 비밀번호를 입력해주세요.", true)
            return
        }

        const formData = new FormData()
        formData.append('email', email)
        formData.append('password', password) // 오타 수정

        fetch("api/auth/login", {
            method: "POST",
            body: formData
        }).then(response => response.json()).then((data) => {
            checkUser()
            showToast(data.message, !data.success)
        }).catch((error) => {
            showToast(error, true)
        })
    })

    $("#loginframe").contents().find("#editInfoForm").submit(function (e) {
        e.preventDefault()

        let password = $("#loginframe").contents().find("#password").val()
        let school = $("#loginframe").contents().find("#school").val()
        let classOfSchool = $("#loginframe").contents().find("#class_of_school").val()
        let majorOfSchool = $("#loginframe").contents().find("#major_of_school").val()
        let location = $("#loginframe").contents().find("#location").val()
        let levelOfIncome = $("#loginframe").contents().find("#level_of_income").val()
        let grade = $("#loginframe").contents().find("#grade").val()

        const formData = new FormData()
        if(password.length > 0)
            formData.append('password', password)
        if(school.length > 0)
            formData.append('school', school)
        if(classOfSchool.length > 0)
            formData.append('class', classOfSchool)
        if(majorOfSchool.length > 0)
            formData.append('major', majorOfSchool)
        if(location.length > 0)
            formData.append('location', location)
        if(levelOfIncome.length > 0)
            formData.append('income', levelOfIncome)
        if(grade.length > 0)
            formData.append('grade', grade)

        fetch("api/auth/", {
            method: "PUT",
            body: formData
        }).then(response => response.json()).then((data) => {
            console.log("회원정보 수정 요청 전송")
            if(data.success) {
                checkUser()
                showToast(data.message, false)
            } else {
                showToast(data.message, true)
            }
        }).catch((error) => {
            showToast(error, true)
        })
    })

    $("#loginframe").contents().find("#register").submit(function (e) {
        e.preventDefault()

        let email = $("#loginframe").contents().find("#email").val()
        let password = $("#loginframe").contents().find("#password").val()
        let school = $("#loginframe").contents().find("#school").val()
        let classOfSchool = $("#loginframe").contents().find("#class_of_school").val()
        let majorOfSchool = $("#loginframe").contents().find("#major_of_school").val()
        let location = $("#loginframe").contents().find("#location").val()
        let levelOfIncome = $("#loginframe").contents().find("#level_of_income").val()
        let grade = $("#loginframe").contents().find("#grade").val()

        const formData = new FormData()
        formData.append('email', email)
        formData.append('password', password)
        formData.append('school', school)
        formData.append('class', classOfSchool)
        formData.append('major', majorOfSchool)
        formData.append('location', location)
        formData.append('income', levelOfIncome)
        formData.append('grade', grade)

        fetch("api/auth/register", {
            method: "POST",
            body: formData
        }).then(response => response.json())
        .then((data) => {

            if(data.success) {
                showToast(data.message)
                $("#loginframe").attr("src", "/login.html");
            } else {
                showToast(data.message, true)
            }
        }).catch((error) => {
            showToast(error)
        })
    })


})


function checkUser() {
    fetch("api/auth/", {
        method: "GET",
    }).then(response => response.json())
        .then((data) => {
            if (data.success) {
                console.log(data)
                user = data.data
                $("#loginframe").attr("src", "/account.html");
            } else {
                $("#loginframe").attr("src", "/login.html");
            }
        }).catch((error) => {
        showToast(error)
    })
}

function showToast(text, isError = false) {
    let toastBootstrap
    if(isError) {
        toastBootstrap = bootstrap.Toast.getOrCreateInstance($("#myErrorToast"));
        $("#toast-error-text").text(text);
    } else {
        toastBootstrap = bootstrap.Toast.getOrCreateInstance($("#myToast"));
        $("#toast-text").text(text);
    }

    toastBootstrap.show()
}

function fillUserInfo() {
    try {
        $("#loginframe").contents().find("#email").val(user.email)
        $("#loginframe").contents().find("#id-label").text(user.email)
        $("#loginframe").contents().find("#school-label").text(user.school)
        $("#loginframe").contents().find("#major-label").text(user.majorOfSchool)
        $("#loginframe").contents().find("#location-label").text(user.location)
        $("#loginframe").contents().find("#grade-label").text(user.grade.toFixed(2))
        $("#loginframe").contents().find("#class-label").text(user.classOfSchool)
        $("#loginframe").contents().find("#income-label").text(user.levelOfIncome)
    } catch (e) {

    }
}

// 장학금 목록 로드
function loadScholarships() {
    const searchQuery = $("#searchInput").val().trim()
    
    // 모드 초기화
    isRecommendMode = false
    isFilterMode = false
    hideFilterSection()
    
    // 검색어가 있으면 검색 모드, 없으면 전체 목록 모드
    if (searchQuery.length > 0) {
        // 새로운 검색어인 경우에만 페이지 초기화
        if (!isSearchMode || currentSearchKeyword !== searchQuery) {
            currentPage = 0 // 새로운 검색시에만 페이지 초기화
        }
        isSearchMode = true
        currentSearchKeyword = searchQuery
        
        // 검색 API 호출 (새로운 페이지네이션 형식 사용)
        fetch(`api/info/search?keywords=${encodeURIComponent(searchQuery)}&page=${currentPage}&each=${currentPageSize}`, {
            method: "GET"
        }).then(response => response.json())
        .then((data) => {
            if (data.success) {
                // 새로운 페이지네이션 형식 처리
                if (data.data.scholarships) {
                    scholarshipData = data.data.scholarships
                    currentPagination = data.data.pagination
                    renderScholarshipList(scholarshipData)
                    renderPaginationFromData(currentPagination)
                } else {
                    // 기존 형식 대비
                    scholarshipData = data.data
                    renderScholarshipList(scholarshipData)
                    $("#pagination").html("")
                }
                
                showShareSection() // 공유 버튼 표시
                
                if (scholarshipData.length === 0) {
                    showToast(`"${searchQuery}"에 대한 검색 결과가 없습니다.`, false)
                } else {
                    const totalFound = currentPagination ? currentPagination.totalCount : scholarshipData.length
                    showToast(`"${searchQuery}"에 대한 검색 결과 ${totalFound}개를 찾았습니다.`, false)
                }
            } else {
                showToast("검색에 실패했습니다.", true)
            }
        }).catch((error) => {
            showToast("네트워크 오류가 발생했습니다.", true)
            console.error(error)
        })
    } else {
        // 전체 목록 모드
        isSearchMode = false
        currentSearchKeyword = ""
        
        // 먼저 전체 데이터 개수를 가져옴
        fetch('api/info/length', {
            method: "GET"
        }).then(response => response.json())
        .then((lengthData) => {
            if (lengthData.success) {
                totalScholarships = lengthData.data
                
                // 그 다음 장학금 목록을 가져옴
                fetch(`api/info/?page=${currentPage}&each=${currentPageSize}`, {
                    method: "GET"
                }).then(response => response.json())
                .then((data) => {
                    if (data.success) {
                        scholarshipData = data.data
                        renderScholarshipList(scholarshipData)
                        renderPagination()
                        showShareSection() // 공유 버튼 표시
                    } else {
                        showToast("장학금 정보를 불러오는데 실패했습니다.", true)
                    }
                }).catch((error) => {
                    showToast("네트워크 오류가 발생했습니다.", true)
                    console.error(error)
                })
            } else {
                showToast("전체 데이터 수를 불러오는데 실패했습니다.", true)
            }
        }).catch((error) => {
            showToast("네트워크 오류가 발생했습니다.", true)
            console.error(error)
        })
    }
}

// 나만의 추천 장학금 로드 (로그인 사용자용)
function loadRecommendedScholarships() {
    // 로그인 체크
    if (!user) {
        showToast("로그인이 필요한 서비스입니다.", true)
        return
    }
    
    // 모드 설정 (페이지 초기화는 첫 호출시에만)
    if (!isRecommendMode) {
        currentPage = 0 // 새로운 추천 요청시에만 페이지 초기화
        // UI 초기화
        $("#searchInput").val("")
        hideFilterSection()
    }
    
    isRecommendMode = true
    isSearchMode = false
    isFilterMode = false
    currentSearchKeyword = ""
    
    // 추천 API 호출 (새로운 POST 방식 사용)
    fetch(`api/info/recommendation?page=${currentPage}&each=${currentPageSize}`, {
        method: "POST"
    }).then(response => response.json())
    .then((data) => {
        if (data.success) {
            // 새로운 페이지네이션 형식 처리
            if (data.data.scholarships) {
                scholarshipData = data.data.scholarships
                currentPagination = data.data.pagination
                renderScholarshipList(scholarshipData)
                renderPaginationFromData(currentPagination)
                
                if (scholarshipData.length === 0) {
                    showToast("회원님의 조건에 맞는 장학금이 없습니다.", false)
                } else {
                    const totalFound = currentPagination.totalCount
                    showToast(`회원님께 맞춤 추천된 장학금 총 ${totalFound}개를 찾았습니다.`, false)
                }
            } else {
                // 기존 형식 대비
                scholarshipData = data.data
                renderScholarshipList(scholarshipData)
                $("#pagination").html("")
                
                if (scholarshipData.length === 0) {
                    showToast("회원님의 조건에 맞는 장학금이 없습니다.", false)
                } else {
                    showToast(`회원님께 맞춤 추천된 장학금 ${scholarshipData.length}개를 찾았습니다.`, false)
                }
            }
        } else {
            showToast("추천 장학금을 불러오는데 실패했습니다.", true)
        }
    }).catch((error) => {
        showToast("네트워크 오류가 발생했습니다.", true)
        console.error(error)
    })
}

// 필터 섹션 토글
function toggleFilterSection() {
    const filterSection = $("#filterSection")
    if (filterSection.is(":visible")) {
        hideFilterSection()
    } else {
        showFilterSection()
    }
}

function showFilterSection() {
    $("#filterSection").slideDown()
    $("#searchInput").val("") // 검색어 초기화
}

function hideFilterSection() {
    $("#filterSection").slideUp()
}

// 필터링된 추천 장학금 로드 (비로그인 사용자용)
function loadFilteredRecommendations(isPageChange = false) {
    // 필터 값들 가져오기 (페이지 변경이 아닌 경우에만 새로 읽어옴)
    if (!isPageChange) {
        currentFilterValues.classOfSchool = $("#filterClass").val()
        currentFilterValues.majorOfSchool = $("#filterMajor").val()
        currentFilterValues.location = $("#filterLocation").val().trim()
        currentFilterValues.levelOfIncome = $("#filterIncome").val()
        currentFilterValues.grade = $("#filterGrade").val()
        currentPage = 0 // 새로운 필터 적용시에만 페이지 초기화
    }
    
    // 모드 설정
    isFilterMode = true
    isRecommendMode = false
    isSearchMode = false
    currentSearchKeyword = ""
    
    // URL 매개변수 구성 (저장된 필터 값들 사용)
    const params = new URLSearchParams()
    if (currentFilterValues.classOfSchool) params.append('classOfSchool', currentFilterValues.classOfSchool)
    if (currentFilterValues.majorOfSchool) params.append('majorOfSchool', currentFilterValues.majorOfSchool)
    if (currentFilterValues.location) params.append('location', currentFilterValues.location)
    if (currentFilterValues.levelOfIncome) params.append('levelOfIncome', currentFilterValues.levelOfIncome)
    if (currentFilterValues.grade) params.append('grade', currentFilterValues.grade)
    params.append('page', currentPage.toString())
    params.append('each', currentPageSize.toString())
    
    // 필터링된 추천 API 호출 (GET 방식)
    fetch(`api/info/recommendation?${params.toString()}`, {
        method: "GET"
    }).then(response => response.json())
    .then((data) => {
        if (data.success) {
            // 새로운 페이지네이션 형식 처리
            if (data.data.scholarships) {
                scholarshipData = data.data.scholarships
                currentPagination = data.data.pagination
                renderScholarshipList(scholarshipData)
                renderPaginationFromData(currentPagination)
                showShareSection() // 공유 버튼 표시
                
                if (scholarshipData.length === 0) {
                    showToast("설정하신 조건에 맞는 장학금이 없습니다.", false)
                } else {
                    const totalFound = currentPagination.totalCount
                    showToast(`설정하신 조건에 맞는 장학금 총 ${totalFound}개를 찾았습니다.`, false)
                }
            } else {
                // 기존 형식 대비
                scholarshipData = data.data
                renderScholarshipList(scholarshipData)
                $("#pagination").html("")
                showShareSection() // 공유 버튼 표시
                
                if (scholarshipData.length === 0) {
                    showToast("설정하신 조건에 맞는 장학금이 없습니다.", false)
                } else {
                    showToast(`설정하신 조건에 맞는 장학금 ${scholarshipData.length}개를 찾았습니다.`, false)
                }
            }
        } else {
            showToast("추천 장학금을 불러오는데 실패했습니다.", true)
        }
    }).catch((error) => {
        showToast("네트워크 오류가 발생했습니다.", true)
        console.error(error)
    })
}

// 장학금 목록 렌더링
function renderScholarshipList(scholarships) {
    const listContainer = $("#scholarshipList")
    
    if (!scholarships || scholarships.length === 0) {
        listContainer.html(`
            <div class="text-center p-4 text-muted">
                <p>조건에 맞는 장학금이 없습니다.</p>
            </div>
        `)
        return
    }
    
    let html = ""
    scholarships.forEach((scholarship, index) => {
        html += `
            <div class="card mb-3 scholarship-item">
                <div class="card-body">
                    <div class="row">
                        <div class="col-12 col-md-8">
                            <h6 class="card-title text-primary mb-1">${scholarship.상품명}</h6>
                            <p class="card-text text-muted mb-2">
                                <i class="fas fa-building"></i> ${scholarship.운영기관명}
                                <span class="badge bg-secondary ms-2">${scholarship.운영기관구분}</span>
                            </p>
                            <p class="card-text mb-2">
                                <strong>지원내역:</strong> ${scholarship['지원내역 상세내용'] || '상세내용 확인 필요'}
                            </p>
                            <p class="card-text mb-2">
                                <strong>대상:</strong> ${scholarship.학년구분} / ${scholarship.학과구분}
                            </p>
                        </div>
                        <div class="col-12 col-md-4 text-md-end text-center mt-2 mt-md-0">
                            <div class="mb-2">
                                <small class="text-muted">모집기간</small><br>
                                <strong>${formatDate(scholarship.모집시작일)} ~ ${formatDate(scholarship.모집종료일)}</strong>
                            </div>
                            <button class="btn btn-primary btn-sm" onclick="showScholarshipDetail(${scholarship.번호})">
                                상세보기
                            </button>
                            ${(scholarship.홈페이지주소 || scholarship['홈페이지 주소']) ? `
                                <a href="${scholarship.홈페이지주소 || scholarship['홈페이지 주소']}" target="_blank" class="btn btn-outline-secondary btn-sm ms-1">
                                    홈페이지
                                </a>
                            ` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `
    })
    
    listContainer.html(html)
}

// 페이지네이션 렌더링 (기존 방식)
function renderPagination() {
    const totalPages = Math.ceil(totalScholarships / currentPageSize)
    const pagination = $("#pagination")
    
    if (totalPages <= 1) {
        pagination.html("")
        return
    }
    
    let html = ""
    
    // 이전 페이지
    html += `
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage - 1})">이전</a>
        </li>
    `
    
    // 페이지 번호들
    const startPage = Math.max(0, currentPage - 2)
    const endPage = Math.min(totalPages - 1, currentPage + 2)
    
    for (let i = startPage; i <= endPage; i++) {
        html += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" onclick="changePage(${i})">${i + 1}</a>
            </li>
        `
    }
    
    // 다음 페이지
    html += `
        <li class="page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage + 1})">다음</a>
        </li>
    `
    
    pagination.html(html)
}

// 페이지네이션 렌더링 (새로운 API 응답 형식)
function renderPaginationFromData(paginationData) {
    const pagination = $("#pagination")
    
    if (!paginationData || paginationData.totalPages <= 1) {
        pagination.html("")
        return
    }
    
    const { currentPage: apiCurrentPage, totalPages, hasNext, hasPrev } = paginationData
    let html = ""
    
    // 이전 페이지
    html += `
        <li class="page-item ${!hasPrev ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${apiCurrentPage - 1})">이전</a>
        </li>
    `
    
    // 페이지 번호들
    const startPage = Math.max(0, apiCurrentPage - 2)
    const endPage = Math.min(totalPages - 1, apiCurrentPage + 2)
    
    for (let i = startPage; i <= endPage; i++) {
        html += `
            <li class="page-item ${i === apiCurrentPage ? 'active' : ''}">
                <a class="page-link" href="#" onclick="changePage(${i})">${i + 1}</a>
            </li>
        `
    }
    
    // 다음 페이지
    html += `
        <li class="page-item ${!hasNext ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${apiCurrentPage + 1})">다음</a>
        </li>
    `
    
    pagination.html(html)
}

// 페이지 변경
function changePage(page) {
    if (page < 0) return
    currentPage = page
    
    if (isFilterMode) {
        loadFilteredRecommendations(true) // 페이지 변경임을 명시
    } else if (isRecommendMode) {
        loadRecommendedScholarships()
    } else if (isSearchMode) {
        loadScholarships()
    } else {
        loadScholarships()
    }
}

// 날짜 포맷팅
function formatDate(dateString) {
    if (!dateString) return '-'
    const date = new Date(dateString)
    return `${date.getFullYear()}.${(date.getMonth() + 1).toString().padStart(2, '0')}.${date.getDate().toString().padStart(2, '0')}`
}

// 장학금 상세 정보 모달
function showScholarshipDetail(scholarshipId) {
    const scholarship = scholarshipData.find(s => s.번호 === scholarshipId)
    if (!scholarship) return
    
    const modalHtml = `
        <div class="modal fade" id="scholarshipModal" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">${scholarship.상품명}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="row mb-3">
                            <div class="col-sm-3"><strong>운영기관:</strong></div>
                            <div class="col-sm-9">${scholarship.운영기관명} (${scholarship.운영기관구분})</div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-sm-3"><strong>지원내역:</strong></div>
                            <div class="col-sm-9">${scholarship['지원내역 상세내용'] || '-'}</div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-sm-3"><strong>성적기준:</strong></div>
                            <div class="col-sm-9">${scholarship['성적기준 상세내용'] || '-'}</div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-sm-3"><strong>소득기준:</strong></div>
                            <div class="col-sm-9">${scholarship['소득기준 상세내용'] || '-'}</div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-sm-3"><strong>지역거주:</strong></div>
                            <div class="col-sm-9">${scholarship['지역거주여부 상세내용'] || '-'}</div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-sm-3"><strong>선발방법:</strong></div>
                            <div class="col-sm-9">${scholarship['선발방법 상세내용'] || '-'}</div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-sm-3"><strong>선발인원:</strong></div>
                            <div class="col-sm-9">${scholarship['선발인원 상세내용'] || '-'}</div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-sm-3"><strong>제출서류:</strong></div>
                            <div class="col-sm-9">${scholarship['제출서류 상세내용'] || '-'}</div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-sm-3"><strong>모집기간:</strong></div>
                            <div class="col-sm-9">${scholarship.모집시작일} ~ ${scholarship.모집종료일}</div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        ${(scholarship.홈페이지주소 || scholarship['홈페이지 주소']) ? `
                            <a href="${scholarship.홈페이지주소 || scholarship['홈페이지 주소']}" target="_blank" class="btn btn-primary">
                                홈페이지 방문
                            </a>
                        ` : ''}
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                    </div>
                </div>
            </div>
        </div>
    `
    
    // 기존 모달 제거
    $("#scholarshipModal").remove()
    
    // 새 모달 추가 및 표시
    $("body").append(modalHtml)
    $("#scholarshipModal").modal('show')
}

// URL에서 상태 복원
function restoreFromURL() {
    const urlParams = new URLSearchParams(window.location.search)
    
    // 공유된 상태가 있는지 확인
    const mode = urlParams.get('mode')  // 'search', 'filter', 'all'
    const page = parseInt(urlParams.get('page')) || 0
    const each = parseInt(urlParams.get('each')) || 5
    
    if (!mode) return false
    
    // 공통 설정
    currentPage = page
    currentPageSize = each
    $("#pageSize").val(each.toString())
    
    if (mode === 'search') {
        // 검색 모드 복원
        const keywords = urlParams.get('keywords')
        if (keywords) {
            $("#searchInput").val(keywords)
            currentSearchKeyword = keywords
            isSearchMode = true
            
            // 검색 실행
            fetch(`api/info/search?keywords=${encodeURIComponent(keywords)}&page=${currentPage}&each=${currentPageSize}`, {
                method: "GET"
            }).then(response => response.json())
            .then((data) => {
                if (data.success) {
                    if (data.data.scholarships) {
                        scholarshipData = data.data.scholarships
                        currentPagination = data.data.pagination
                        renderScholarshipList(scholarshipData)
                        renderPaginationFromData(currentPagination)
                        showShareSection()
                    }
                }
            }).catch((error) => {
                console.error(error)
                return false
            })
            
            return true
        }
    } else if (mode === 'filter') {
        // 필터 모드 복원
        const classOfSchool = urlParams.get('classOfSchool') || ""
        const majorOfSchool = urlParams.get('majorOfSchool') || ""
        const location = urlParams.get('location') || ""
        const levelOfIncome = urlParams.get('levelOfIncome') || ""
        const grade = urlParams.get('grade') || ""
        
        // 필터 값 설정
        $("#filterClass").val(classOfSchool)
        $("#filterMajor").val(majorOfSchool)
        $("#filterLocation").val(location)
        $("#filterIncome").val(levelOfIncome)
        $("#filterGrade").val(grade)
        
        // 필터 값 저장
        currentFilterValues.classOfSchool = classOfSchool
        currentFilterValues.majorOfSchool = majorOfSchool
        currentFilterValues.location = location
        currentFilterValues.levelOfIncome = levelOfIncome
        currentFilterValues.grade = grade
        
        // 필터 섹션 표시
        showFilterSection()
        isFilterMode = true
        
        // 필터 실행
        const params = new URLSearchParams()
        if (classOfSchool) params.append('classOfSchool', classOfSchool)
        if (majorOfSchool) params.append('majorOfSchool', majorOfSchool)
        if (location) params.append('location', location)
        if (levelOfIncome) params.append('levelOfIncome', levelOfIncome)
        if (grade) params.append('grade', grade)
        params.append('page', currentPage.toString())
        params.append('each', currentPageSize.toString())
        
        fetch(`api/info/recommendation?${params.toString()}`, {
            method: "GET"
        }).then(response => response.json())
        .then((data) => {
            if (data.success) {
                if (data.data.scholarships) {
                    scholarshipData = data.data.scholarships
                    currentPagination = data.data.pagination
                    renderScholarshipList(scholarshipData)
                    renderPaginationFromData(currentPagination)
                    showShareSection()
                }
            }
        }).catch((error) => {
            console.error(error)
            return false
        })
        
        return true
    } else if (mode === 'all') {
        // 전체 목록 모드 복원
        fetch('api/info/length', {
            method: "GET"
        }).then(response => response.json())
        .then((lengthData) => {
            if (lengthData.success) {
                totalScholarships = lengthData.data
                
                fetch(`api/info/?page=${currentPage}&each=${currentPageSize}`, {
                    method: "GET"
                }).then(response => response.json())
                .then((data) => {
                    if (data.success) {
                        scholarshipData = data.data
                        renderScholarshipList(scholarshipData)
                        renderPagination()
                        showShareSection()
                    }
                })
            }
        }).catch((error) => {
            console.error(error)
            return false
        })
        
        return true
    }
    
    return false
}

// 현재 상태를 URL로 공유
function shareCurrentState() {
    let shareUrl = window.location.origin + window.location.pathname
    const params = new URLSearchParams()
    
    params.append('page', currentPage.toString())
    params.append('each', currentPageSize.toString())
    
    if (isSearchMode && currentSearchKeyword) {
        // 검색 모드
        params.append('mode', 'search')
        params.append('keywords', currentSearchKeyword)
    } else if (isFilterMode) {
        // 필터 모드
        params.append('mode', 'filter')
        if (currentFilterValues.classOfSchool) params.append('classOfSchool', currentFilterValues.classOfSchool)
        if (currentFilterValues.majorOfSchool) params.append('majorOfSchool', currentFilterValues.majorOfSchool)
        if (currentFilterValues.location) params.append('location', currentFilterValues.location)
        if (currentFilterValues.levelOfIncome) params.append('levelOfIncome', currentFilterValues.levelOfIncome)
        if (currentFilterValues.grade) params.append('grade', currentFilterValues.grade)
    } else {
        // 전체 목록 모드
        params.append('mode', 'all')
    }
    
    shareUrl += '?' + params.toString()
    
    // 클립보드에 복사
    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(shareUrl).then(() => {
            showToast("공유 링크가 클립보드에 복사되었습니다!", false)
        }).catch(() => {
            // fallback
            fallbackCopyTextToClipboard(shareUrl)
        })
    } else {
        // fallback
        fallbackCopyTextToClipboard(shareUrl)
    }
}

// 클립보드 복사 fallback
function fallbackCopyTextToClipboard(text) {
    const textArea = document.createElement("textarea")
    textArea.value = text
    textArea.style.top = "0"
    textArea.style.left = "0"
    textArea.style.position = "fixed"
    
    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()
    
    try {
        document.execCommand('copy')
        showToast("공유 링크가 클립보드에 복사되었습니다!", false)
    } catch (err) {
        // 모달로 URL 표시
        showShareModal(text)
    }
    
    document.body.removeChild(textArea)
}

// 공유 모달 표시
function showShareModal(url) {
    const modalHtml = `
        <div class="modal fade" id="shareModal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">공유 링크</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <p>아래 링크를 복사하여 공유하세요:</p>
                        <div class="input-group">
                            <input type="text" class="form-control" value="${url}" id="shareUrlInput" readonly>
                            <button class="btn btn-outline-secondary" type="button" onclick="selectShareUrl()">선택</button>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                    </div>
                </div>
            </div>
        </div>
    `
    
    $("#shareModal").remove()
    $("body").append(modalHtml)
    $("#shareModal").modal('show')
}

// 공유 URL 선택
function selectShareUrl() {
    const input = document.getElementById('shareUrlInput')
    input.select()
    input.setSelectionRange(0, 99999)
}

// 공유 섹션 표시
function showShareSection() {
    $("#shareSection").show()
}

// 공유 섹션 숨김
function hideShareSection() {
    $("#shareSection").hide()
}

// 기존 함수들을 수정하여 공유 섹션 표시/숨김 처리
function updateShareSectionVisibility() {
    if (isSearchMode || isFilterMode || (!isRecommendMode && scholarshipData.length > 0)) {
        showShareSection()
    } else {
        hideShareSection()
    }
}