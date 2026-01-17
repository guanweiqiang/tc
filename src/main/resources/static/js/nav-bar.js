import { API_BASE } from "./config.js";

const TOKEN_KEY = "Authorization";

/**
 * 初始化导航栏用户状态和按钮绑定
 */
export function initUser() {
    const token = localStorage.getItem(TOKEN_KEY);
    const loginLink = document.getElementById('loginLink');
    const avatarWrapper = document.getElementById('avatarWrapper');
    const publishBtn = document.getElementById('publishBtn');
    const logoutBtn = document.getElementById('logoutBtn');

    // --- 1. 处理登录状态显示 ---
    if (token) {
        if (loginLink) loginLink.style.display = 'none';
        if (avatarWrapper) avatarWrapper.style.display = 'block';
        // 建议在这里调用获取用户信息的接口，更新头像和昵称
        updateNavUserInfo();
    } else {
        if (loginLink) loginLink.style.display = 'block';
        if (avatarWrapper) avatarWrapper.style.display = 'none';
    }

    // --- 2. 绑定“写文章”按钮 ---
    if (publishBtn) {
        publishBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (!localStorage.getItem(TOKEN_KEY)) {
                alert("请先登录后再发表文章");
                window.location.href = "login.html";
            } else {
                window.location.href = "add-article.html";
            }
        });
    }

    // --- 3. 绑定“退出登录”按钮 ---
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (confirm("确定要退出登录吗？")) {
                localStorage.removeItem(TOKEN_KEY);
                // 也可以清除其他用户信息缓存
                window.location.href = "index.html";
            }
        });
    }
}

/**
 * 获取用户信息并更新导航栏 (可选)
 */
async function updateNavUserInfo() {
    const token = localStorage.getItem(TOKEN_KEY);
    try {
        const res = await fetch(`${API_BASE}/user/profile`, { // 替换为你的真实路径
            headers: { "Authorization": token }
        });
        const result = await res.json();
        if (result.isSuccess) {
            const navNickname = document.getElementById('navNickname');
            const navAvatar = document.getElementById('navAvatar'); // 对应 HTML 中的 id
            if (navNickname) navNickname.innerText = result.data.nickname;
            if (navAvatar && result.data.avatar) navAvatar.src = result.data.avatar;
        }
    } catch (err) {
        console.error("更新导航栏用户信息失败", err);
    }
}