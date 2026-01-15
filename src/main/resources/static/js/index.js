const TOKEN_KEY = "Authorization";
const API_BASE = "http://192.168.0.101:8080";

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
    const welcomeText = document.getElementById("welcomeText");

    if (localStorage.getItem(TOKEN_KEY)) {
        // 已登录状态
        loginLink.style.display = "none";
        avatarWrapper.style.display = "flex";

        // 获取用户信息填充 UI
        fetchUserInfo();
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
            // 填充头像
            if (user.avatar) {
                document.getElementById("userAvatar").src = user.avatar;
            }

            // 填充下拉菜单昵称
            document.getElementById("navNickname").innerText = user.nickname || user.username;
            // 首页欢迎语个性化
            document.getElementById("welcomeText").innerText = `欢迎回来, ${user.nickname || user.username}`;
        }
    } catch (error) {
        console.error("加载用户信息失败:", error);
    }
}

// 3. 退出登录逻辑
document.getElementById("logoutBtn")?.addEventListener("click", async () => {
    if (!confirm("确定要退出登录吗？")) return;

    try {
        // 通知后端清理 Session (可选)
        await fetch(`${API_BASE}/auth/logout`, {
            method: "POST",
            headers: getHeaders()
        });
    } catch (error) {
        console.error("登出请求失败:", error);
    } finally {
        // 无论如何都要清理本地 Token
        localStorage.removeItem(TOKEN_KEY);
        location.href = "login.html";
    }
});


// 4. 执行初始化
document.addEventListener("DOMContentLoaded", initPage);