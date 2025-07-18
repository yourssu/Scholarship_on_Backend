let user = undefined

$(document).ready(function () {
    checkUser()
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