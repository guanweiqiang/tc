import { API_BASE } from "./config.js";
import {initUser} from "./nav-bar";

const TOKEN_KEY = "Authorization";

// 获取 URL 中的 ID 参数
const urlParams = new URLSearchParams(window.location.search);
const articleId = urlParams.get('id');

// 页面初始化
document.addEventListener('DOMContentLoaded', async () => {
    // 权限检查
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
        alert("请先登录");
        window.location.href = "login.html";
        return;
    }
    initUser();

    // 事件绑定
    document.getElementById('submitBtn').addEventListener('click', handleSubmit);
    document.getElementById('cancelBtn').addEventListener('click', () => history.back());

    // 如果有 ID，进入编辑模式
    if (articleId) {
        document.getElementById('pageTitle').innerText = "修改文章";
        await loadOriginalData(articleId);
    }
});

// 通用请求头获取
function getHeaders() {
    const token = localStorage.getItem(TOKEN_KEY);
    return {
        "Content-Type": "application/json",
        "Authorization": token.startsWith("Bearer ") ? token : "Bearer " + token
    };
}

/**
 * 加载原文章数据
 */
async function loadOriginalData(id) {
    const loadingEl = document.getElementById('loading');
    loadingEl.style.display = 'block';

    try {
        const res = await fetch(`${API_BASE}/article/searchDetail`, {
            method: "POST",
            headers: getHeaders(),
            body: JSON.stringify({ id: id })
        });
        const result = await res.json();

        if (result.isSuccess || result.code === 200) {
            document.getElementById('title').value = result.data.title;
            document.getElementById('content').value = result.data.content;
        } else {
            alert("加载失败: " + (result.message || "文章不存在"));
            history.back();
        }
    } catch (err) {
        console.error(err);
        alert("网络错误，无法加载数据");
    } finally {
        loadingEl.style.display = 'none';
    }
}

/**
 * 提交逻辑 (新增或更新)
 */
async function handleSubmit() {
    const title = document.getElementById('title').value.trim();
    const content = document.getElementById('content').value.trim();

    if (!title || !content) {
        alert("标题和内容不能为空");
        return;
    }

    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.innerText = "提交中...";

    const isEdit = !!articleId;
    // 根据模式决定接口
    const url = isEdit ? `${API_BASE}/article/update` : `${API_BASE}/article/add`;
    const method = isEdit ? "PUT" : "POST";

    const payload = { title, content };
    if (isEdit) payload.id = parseInt(articleId); // 确保 ID 是数字类型

    try {
        const res = await fetch(url, {
            method: method,
            headers: getHeaders(),
            body: JSON.stringify(payload)
        });
        const result = await res.json();

        if (result.isSuccess || result.code === 200) {
            alert(isEdit ? "修改成功！" : "发布成功！");
            // 跳转到文章详情或列表页
            window.location.href = isEdit ? `article-detail.html?id=${articleId}` : "article.html";
        } else {
            alert("提交失败：" + (result.message || "未知错误"));
        }
    } catch (err) {
        alert("网络请求失败，请检查后端服务");
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerText = "提交保存";
    }
}