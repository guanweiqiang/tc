import { API_BASE } from "./config.js";
import {initUser} from "./nav-bar.js";

const TOKEN_KEY = "Authorization";
let oldEmailToken = "";

// --- 页面初始化 ---
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    initUser();
    // 初始化数据
    fetchProfile();

    // 绑定所有事件监听器
    initEventListeners();
});

function initEventListeners() {
    // 1. 侧边栏 Tab 切换
    document.querySelectorAll('.menu-item').forEach(item => {
        item.addEventListener('click', () => {
            const tabType = item.getAttribute('data-tab');
            switchTab(tabType, item);
        });
    });

    // 2. 头像上传
    document.getElementById('avatarInput').addEventListener('change', uploadFile);

    // 3. 更新昵称
    document.getElementById('btnUpdateNickname').addEventListener('click', saveNickname);

    // 4. 修改邮箱流程
    document.getElementById('sendOldBtn').addEventListener('click', sendOldEmailCode);
    document.getElementById('btnVerifyOldEmail').addEventListener('click', verifyOldEmail);
    document.getElementById('sendNewBtn').addEventListener('click', sendNewEmailCode);
    document.getElementById('btnSaveNewEmail').addEventListener('click', saveNewEmail);
    document.getElementById('btnResetEmailStep').addEventListener('click', resetEmailStep);

    // 5. 修改密码
    document.getElementById('btnSavePwd').addEventListener('click', savePwd);

    // 6. 密码可见性切换
    document.querySelectorAll('.toggle-pwd').forEach(icon => {
        icon.addEventListener('click', () => {
            const targetId = icon.getAttribute('data-target');
            const input = document.getElementById(targetId);
            input.type = input.type === 'password' ? 'text' : 'password';
        });
    });
}

// --- 核心业务逻辑 ---

async function fetchProfile() {
    const token = localStorage.getItem(TOKEN_KEY);
    try {
        const res = await fetch(`${API_BASE}/user/profile`, {
            headers: { "Authorization": token }
        });
        const result = await res.json();
        if (result.isSuccess) {
            const u = result.data;
            document.getElementById('username').value = u.username;
            document.getElementById('nickname').value = u.nickname || "";
            document.getElementById('emailDisplay').innerText = u.email;
            document.getElementById('oldEmailDisplay').value = u.email;
            document.getElementById('mainAvatar').src = u.avatar || "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix";
        }
    } catch (e) {
        console.error("加载个人资料失败", e);
    }
}

async function uploadFile(event) {
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    try {
        const res = await fetch(`${API_BASE}/user/updateAvatar`, {
            method: "POST",
            headers: { "Authorization": localStorage.getItem(TOKEN_KEY) },
            body: formData
        });
        const result = await res.json();
        if (result.isSuccess) {
            alert("头像已成功更新");
            fetchProfile(); // 刷新头像显示
        }
    } catch (e) {
        alert("头像上传失败");
    }
}

async function saveNickname() {
    const nickname = document.getElementById('nickname').value.trim();
    if(!nickname) return showTip('infoMsg', '昵称不能为空', false);

    const res = await fetch(`${API_BASE}/user/updateNickname`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": localStorage.getItem(TOKEN_KEY)
        },
        body: JSON.stringify({ nickname })
    });
    const result = await res.json();
    showTip('infoMsg', result.message, result.isSuccess);
}

// --- 邮箱修改逻辑 ---

async function sendOldEmailCode() {
    const email = document.getElementById('oldEmailDisplay').value;
    const btn = document.getElementById('sendOldBtn');
    try {
        await fetch(`${API_BASE}/auth/sendVerificationCode?email=${email}&purpose=UPDATE_EMAIL_OLD`, {
            headers: { "Authorization": localStorage.getItem(TOKEN_KEY) }
        });
        startCountdown(btn);
    } catch (e) {
        showTip('emailMsg', '发送失败', false);
    }
}

async function verifyOldEmail() {
    const code = document.getElementById('oldEmailCode').value.trim();
    if (!code) return showTip('emailMsg', '请输入验证码', false);

    const res = await fetch(`${API_BASE}/user/verifyOldEmail`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": localStorage.getItem(TOKEN_KEY)
        },
        body: JSON.stringify({ code })
    });

    const result = await res.json();
    if (result.isSuccess) {
        oldEmailToken = result.data; // 存储 ticket
        document.getElementById('step1_verifyOld').style.display = 'none';
        document.getElementById('step2_bindNew').style.display = 'block';
        document.getElementById('emailStepDesc').innerText = "第二步：绑定新邮箱";
        showTip('emailMsg', '旧邮箱验证成功', true);
    } else {
        showTip('emailMsg', result.message, false);
    }
}

async function sendNewEmailCode() {
    const email = document.getElementById('newEmail').value.trim();
    if (!/^\S+@\S+\.\S+$/.test(email)) return alert("邮箱格式错误");

    const btn = document.getElementById('sendNewBtn');
    await fetch(`${API_BASE}/auth/sendVerificationCode?email=${email}&purpose=UPDATE_EMAIL_NEW`);
    startCountdown(btn);
}

async function saveNewEmail() {
    const email = document.getElementById('newEmail').value.trim();
    const code = document.getElementById('newEmailCode').value.trim();

    const res = await fetch(`${API_BASE}/user/updateEmail`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": localStorage.getItem(TOKEN_KEY)
        },
        body: JSON.stringify({ email, code, ticket: oldEmailToken })
    });

    const result = await res.json();
    showTip('emailMsg', result.message, result.isSuccess);
    if (result.isSuccess) {
        setTimeout(() => location.reload(), 1500);
    }
}

// --- 修改密码 ---

async function savePwd() {
    const oldPassword = document.getElementById('oldPwd').value;
    const newPassword = document.getElementById('newPwd').value;
    const confirmPwd = document.getElementById('confirmPwd').value;

    if (newPassword.length < 8) return showTip('pwdMsg', '新密码至少8位', false);
    if (newPassword !== confirmPwd) return showTip('pwdMsg', '两次密码输入不一致', false);

    const res = await fetch(`${API_BASE}/user/changePassword`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": localStorage.getItem(TOKEN_KEY)
        },
        body: JSON.stringify({ oldPassword, newPassword })
    });
    const result = await res.json();
    if (result.isSuccess) {
        alert("密码修改成功，请重新登录");
        localStorage.removeItem(TOKEN_KEY);
        window.location.href = 'login.html';
    } else {
        showTip('pwdMsg', result.message, false);
    }
}

// --- 辅助工具函数 ---

function switchTab(type, el) {
    document.querySelectorAll('.menu-item, .section').forEach(m => m.classList.remove('active'));
    el.classList.add('active');
    document.getElementById(type + 'Section').classList.add('active');
    lucide.createIcons(); // 重新渲染新显示的图标
}

function resetEmailStep() {
    document.getElementById('step1_verifyOld').style.display = 'block';
    document.getElementById('step2_bindNew').style.display = 'none';
    document.getElementById('emailStepDesc').innerText = "第一步：验证当前绑定邮箱";
}

function showTip(id, text, isOk) {
    const el = document.getElementById(id);
    if(!el) return;
    el.innerText = text;
    el.className = "msg-tip " + (isOk ? "success" : "error");
}

function startCountdown(btn) {
    let count = 60;
    btn.disabled = true;
    const originalText = btn.innerText;
    const timer = setInterval(() => {
        if (count <= 0) {
            clearInterval(timer);
            btn.innerText = originalText;
            btn.disabled = false;
        } else {
            btn.innerText = `${count--}s`;
        }
    }, 1000);
}