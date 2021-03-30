$(function () {
    $("#uploadForm").submit(upload);
    $("#update-password-form").submit(check_data);
    // 清除鼠标焦点上input的错误提示
    $("input").focus(clear_error);
});

function check_data() {
    var pwd1 = $("#new-password").val();
    var pwd2 = $("#confirm-password").val();
    if (pwd1 != pwd2) {
        $("#confirm-password").addClass("is-invalid");
        return false;
    }
    return true;
}

function clear_error() {
    $(this).removeClass("is-invalid");
}

function upload() {



    // 异步请求
    // processData:false 不把表单的内容转为字符串
    // contentType:false 不让jQuery设置上传的类型，浏览器会自动的进行设置
    $.ajax({
        url: "http://upload-z1.qiniup.com",
        method: "post",
        // 是否需要把表单的内容转换为字符串
        processData: false,
        // 是否让jquery设置上传的类型（false：不让）
        contentType: false,
        // $("#uploadForm")得到的为jquery对象，$("#uploadForm")[0]得到的为js对象
        data: new FormData($("#uploadForm")[0]),
        success: function (data) {
            if (data && data.code == 0) {
                // 更新头像的访问路径
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {"fileName": $("input[name='key']").val()},
                    function (da) {
                        // 将字符串转为json对象
                        da = $.parseJSON(da);
                        if (da.code == 0) {
                            window.location.reload();
                        } else {
                            alert(da.msg);
                        }
                    }
                );
            } else {
                alert("上传失败！");
            }
        }
    });

    // 事件到此为止，不再继续处理
    return false;
}