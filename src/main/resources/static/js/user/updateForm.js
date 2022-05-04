let valid = {
    password: {
        state: false,
        msg: ""
    }
}

// 이미지 변화 확인을 위한 변수
let count = 0;

$("#password").focusout(() => {
    passwordSameCheck();
});

$("#same-password").focusout(() => {
    passwordSameCheck();
});

function validation() {
    if (valid.password.state) {
        return true;
    } else {
        let errorMsgs = ``;

        errorMsgs += `${valid.password.msg}<br/>`;

        $(".my_error_box").html(errorMsgs);
        $(".my_error_box").removeClass("my_hidden");
        return false;
    }
}

function passwordSameCheck() {
    let password = $("#password").val();
    let samePassword = $("#same-password").val();

    if (password === samePassword) {
        valid.password.state = true;
        valid.password.msg = "";
    } else {
        valid.password.state = false;
        valid.password.msg = "패스워드가 동일하지 않습니다.";
    }
}

let update = async() => {

    let id = $("#id").val();

    let fileForm = $("#update-form")[0];
    let formData = new FormData();

    if (count > 0) {
        let profile = $("#profile-img-input")[0].files[0];
        formData.append(fileForm[0].name, profile);
    }

    formData.append(fileForm[3].name, fileForm[3].value);
    formData.append(fileForm[5].name, fileForm[5].value);

    let response = await fetch(`/s/user/${id}`, {
        method: "PUT",
        // header에 x-www 붙여주니 오류 발생 => 원인 찾기
        body: formData
    });


    if (response.status == 200) {
        alert("정보가 수정되었습니다.");
        location.href = `/user/${id}/post`;
    }
};

$("#btn-update").click(() => {
    if (validation()) {
        update();
    }
});

$("#profile-img-btn").click(() => {
    $("#profile-img-input").click();
});

$("#profile-img-input").change((event) => {

    let f = event.target.files[0];

    if (!f.type.match("image.*")) {
        alert("이미지를 선택해주세요!");
        return;
    }
    count += 1;

    let reader = new FileReader();

    // 콜백 함수
    reader.onload = (event) => {
        //console.log(event);
        $("#profile-img-btn").attr("src", event.target.result);
        $("#profile-img-header").attr("src", event.target.result);
    };

    // 파일 다운로드
    reader.readAsDataURL(f); // IO=>오래걸림 =>내부적으로 await와 async
});