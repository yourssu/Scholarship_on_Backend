let user = undefined
let currentPage = 0
let currentPageSize = 5
let totalScholarships = 0
let scholarshipData = []
let isSearchMode = false
let currentSearchKeyword = ""

$(document).ready(function () {
    checkUser()
    loadScholarships()
    
    // 검색 버튼 클릭
    $("#searchBtn").click(function() {
        loadScholarships()
    })
    
    // 페이지 크기 변경
    $("#pageSize").change(function() {
        currentPageSize = parseInt($(this).val())
        currentPage = 0
        loadScholarships()
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
    
    // 검색어가 있으면 검색 모드, 없으면 전체 목록 모드
    if (searchQuery.length > 0) {
        isSearchMode = true
        currentSearchKeyword = searchQuery
        currentPage = 0 // 검색시 페이지 초기화
        
        // 검색 API 호출
        fetch(`api/info/search?keywords=${encodeURIComponent(searchQuery)}`, {
            method: "GET"
        }).then(response => response.json())
        .then((data) => {
            if (data.success) {
                scholarshipData = data.data
                
                // 검색 결과는 페이지네이션 없이 모든 결과 표시
                renderScholarshipList(scholarshipData)
                
                // 검색 결과에는 페이지네이션 숨김
                $("#pagination").html("")
                
                if (scholarshipData.length === 0) {
                    showToast(`"${searchQuery}"에 대한 검색 결과가 없습니다.`, false)
                } else {
                    showToast(`"${searchQuery}"에 대한 검색 결과 ${scholarshipData.length}개를 찾았습니다.`, false)
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

// 페이지네이션 렌더링
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

// 페이지 변경
function changePage(page) {
    if (page < 0) return
    currentPage = page
    loadScholarships()
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