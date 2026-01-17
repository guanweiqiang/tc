// js/add-article.js
import { API_BASE } from "./config.js";
import {initUser} from "./nav-bar.js";

document.addEventListener('DOMContentLoaded', () => {
    // 1. 获取登录状态 (注意：我们统一 Token 的存储方式)
    const token = localStorage.getItem("Authorization");

    // 2. 权限拦截
    if (!token) {
        alert("请先登录后再发表文章");
        location.href = "login.html";
        return;
    }
    initUser();

    const submitBtn = document.getElementById('submitBtn');
    if (submitBtn) {
        submitBtn.addEventListener('click', publishArticle);
    }
});

async function publishArticle() {
    const titleInput = document.getElementById('postTitle');
    const contentInput = document.getElementById('postContent');
    const title = titleInput.value.trim();
    const content = contentInput.value.trim();

    // 这里需要注意：如果你的登录逻辑已经存了 "Bearer " 前缀，这里就不需要重复加
    const token = localStorage.getItem("Authorization");

    if (!title || !content) {
        alert("标题和内容不能为空");
        return;
    }

    const btn = document.getElementById('submitBtn');
    btn.disabled = true;
    const originalText = btn.innerHTML;
    btn.innerText = "发布中...";

    try {
        // --- 修复点：去掉多余的双引号 ---
        const res = await fetch(`${API_BASE}/article/add`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                // 如果你的 TOKEN 存的时候没带 Bearer，这里手动加上
                "Authorization": token.startsWith("Bearer ") ? token : "Bearer " + token
            },
            body: JSON.stringify({
                title: title,
                content: content
            })
        });

        const result = await res.json();

        if (result.isSuccess) {
            alert("发布成功！");
            location.href = "article.html";
        } else {
            alert("发布失败：" + (result.message || "未知原因"));
            btn.disabled = false;
            btn.innerHTML = originalText;
        }
    } catch (err) {
        console.error("请求异常:", err);
        alert("网络异常，请稍后再试");
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}