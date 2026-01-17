import {initUser} from "./nav-bar.js";

const TOKEN_KEY = "Authorization";
import {API_BASE} from "./config.js";

// 通用请求头处理
function getHeaders() {
    const token = localStorage.getItem(TOKEN_KEY);
    return {
        "Authorization": token ? (token.startsWith("Bearer ") ? token : "Bearer " + token) : "",
        "Content-Type": "application/json"
    };
}

// 1. 初始化页面状态
async function initPage() {
    const loginLink = document.getElementById("loginLink");
    const avatarWrapper = document.getElementById("avatarWrapper");

    if (localStorage.getItem(TOKEN_KEY)) {
        // 已登录状态
        loginLink.style.display = "none";
        avatarWrapper.style.display = "flex";

        // 获取用户信息填充 UI
        fetchUserInfo();
        initUser();
    } else {
        // 未登录状态
        loginLink.style.display = "block";
        avatarWrapper.style.display = "none";
    }
}



// 2. 获取用户基础信息（头像、昵称）
async function fetchUserInfo() {
    try {
        const response = await fetch(`${API_BASE}/user/profile`, {
            method: "GET",
            headers: getHeaders()
        });
        const result = await response.json();

        if (result.isSuccess) {
            const user = result.data;

            // 填充下拉菜单昵称
            document.getElementById("navNickname").innerText = user.nickname || user.username;
            // 首页欢迎语个性化
            document.getElementById("welcomeText").innerText = `欢迎回来, ${user.nickname || user.username}`;
        }
    } catch (error) {
        console.error("加载用户信息失败:", error);
    }
}


// 4. 执行初始化
document.addEventListener("DOMContentLoaded", initPage);