import { API_BASE } from "./config.js";

const TOKEN_KEY = "Authorization";
let countdown = 60;
let isSending = false; // 防止重复点击发送

// --- 页面初始化绑定 ---
document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initSubmitButtons();
    initVerifyBtn();
});

// 1. 初始化 Tab 切换
function initTabs() {
    const tabs = document.querySelectorAll('.tab-item');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const type = tab.getAttribute('data-type');

            // 样式切换
            document.querySelectorAll('.tab-item, .login-form').forEach(el => el.classList.remove('active'));
            tab.classList.add('active');

            // 兼容你原来的 ID 命名逻辑：user -> userForm
            const formId = type + 'Form';
            document.getElementById(formId).classList.add('active');
            showError("");
        });
    });
}

// 2. 初始化所有登录按钮
function initSubmitButtons() {
    document.querySelectorAll('.btn-login').forEach(btn => {
        btn.addEventListener('click', () => {
            const mode = btn.getAttribute('data-mode');
            handleLogin(mode);
        });
    });
}

// 3. 初始化发送验证码按钮
function initVerifyBtn() {
    const sendBtn = document.getElementById('sendBtn');
    if (sendBtn) {
        sendBtn.addEventListener('click', sendCode);
    }
}

// --- 业务逻辑函数 ---

async function sendCode() {
    if (isSending) return;

    showError("");

    const email = document.getElementById("emailC").value.trim();

    if (!/^\S+@\S+\.\S+$/.test(email)) {
        showError("请输入有效的邮箱");
        return;
    }

    isSending = true;
    const btn = document.getElementById("sendBtn");

    try {
        const response = await fetch(`${API_BASE}/auth/sendVerificationCode?email=${email}&purpose=LOGIN`);
        const data = await response.json();

        if (data.isSuccess) {
            showError("");
            startCountdown();
        } else {
            showError(data.message || "发送失败");
            isSending = false;
        }
    } catch (e) {
        showError("网络异常");
        isSending = false;
    }
}

function startCountdown() {
    const btn = document.getElementById("sendBtn");
    btn.disabled = true;
    let timer = setInterval(() => {
        if (countdown <= 0) {
            clearInterval(timer);
            btn.innerText = "获取验证码";
            btn.disabled = false;
            countdown = 60;
            isSending = false;
        } else {
            btn.innerText = `${countdown}s`;
            countdown--;
        }
    }, 1000);
}

async function handleLogin(type) {
    showError("");
    let url = "";
    let payload = {};

    if (type === 'user') {
        url = `${API_BASE}/auth/login`;
        payload = {
            username: document.getElementById("username").value.trim(),
            password: document.getElementById("userPwd").value.trim()
        };
    } else if (type === 'emailPwd') {
        url = `${API_BASE}/auth/loginByEmailPwd`;
        payload = {
            email: document.getElementById("emailP").value.trim(),
            password: document.getElementById("emailPwd").value.trim()
        };
    } else if (type === 'emailCode') {
        url = `${API_BASE}/auth/loginByEmailCode`;
        payload = {
            email: document.getElementById("emailC").value.trim(),
            code: document.getElementById("verifyCode").value.trim()
        };
    }

    if (Object.values(payload).some(v => !v)) {
        showError("请填写完整信息");
        return;
    }

    try {
        const res = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const result = await res.json();

        if (result.isSuccess) {
            localStorage.setItem(TOKEN_KEY, "Bearer " + result.data.token);
            // 这里也可以存储其他信息，比如 nickname 等
            window.location.href = "index.html";
        } else {
            showError(result.message);
        }
    } catch (e) {
        showError("系统繁忙");
    }
}

function showError(msg) {
    document.getElementById("errorMsg").innerText = msg;
}