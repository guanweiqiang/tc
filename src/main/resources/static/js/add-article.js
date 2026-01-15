document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem("Authorization");

    // 1. 权限拦截
    if (!token) {
        alert("请先登录后再发表文章");
        location.href = "login.html";
        return;
    }

    const submitBtn = document.getElementById('submitBtn');
    submitBtn.addEventListener('click', publishArticle);
});

async function publishArticle() {
    const title = document.getElementById('postTitle').value.trim();
    const content = document.getElementById('postContent').value.trim();
    const token = localStorage.getItem("Authorization");

    if (!title || !content) {
        alert("标题和内容不能为空");
        return;
    }

    // 禁用按钮防止重复点击
    const btn = document.getElementById('submitBtn');
    btn.disabled = true;
    btn.innerText = "发布中...";

    try {
        const res = await fetch("http://192.168.0.101:8080/article/add", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify({
                title: title,
                content: content
            })
        });

        const result = await res.json();

        if (result.isSuccess) {
            alert("发布成功！");
            location.href = "article.html"; // 跳回列表页
        } else {
            alert("发布失败：" + result.message);
            btn.disabled = false;
            btn.innerText = "重新发布";
        }
    } catch (err) {
        console.error(err);
        alert("网络异常，请稍后再试");
        btn.disabled = false;
    }
}