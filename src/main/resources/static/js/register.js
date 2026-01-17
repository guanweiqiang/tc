import { API_BASE } from "./config.js";

let countdown = 60;
let timer = null;
let isSending = false; // 防止暴力点击

// --- 页面加载完成后的初始化 ---
document.addEventListener('DOMContentLoaded', () => {
    const sendBtn = document.getElementById("sendBtn");
    const regBtn = document.getElementById("regBtn");

    if (sendBtn) {
        sendBtn.addEventListener('click', handleSendCode);
    }

    if (regBtn) {
        regBtn.addEventListener('click', handleRegister);
    }
});

/**
 * 发送验证码逻辑
 */
async function handleSendCode() {
    if (isSending) return;

    const email = document.getElementById("email").value.trim();
    const errorMsg = document.getElementById("errorMsg");
    const sendBtn = document.getElementById("sendBtn");

    // 基础邮箱校验
    if (!email || !/^\S+@\S+\.\S+$/.test(email)) {
        showError("请输入正确的邮箱地址");
        return;
    }

    showError(""); // 清空错误
    isSending = true;
    sendBtn.disabled = true;

    try {
        const url = `${API_BASE}/auth/sendVerificationCode?email=${email}&purpose=REGISTER`;
        const res = await fetch(url, { method: "GET" });
        const data = await res.json();

        if (data.isSuccess) {
            startCountdown();
        } else {
            showError(data.message || "发送失败");
            sendBtn.disabled = false;
            isSending = false;
        }
    } catch (e) {
        showError("邮件服务繁忙，请稍后再试");
        sendBtn.disabled = false;
        isSending = false;
    }
}

/**
 * 倒计时逻辑
 */
function startCountdown() {
    const sendBtn = document.getElementById("sendBtn");
    timer = setInterval(() => {
        if (countdown > 0) {
            sendBtn.innerText = `${countdown}s 后重发`;
            countdown--;
        } else {
            clearInterval(timer);
            sendBtn.innerText = "获取验证码";
            sendBtn.disabled = false;
            countdown = 60;
            isSending = false;
        }
    }, 1000);
}

/**
 * 注册逻辑
 */
async function handleRegister() {
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    const email = document.getElementById("email").value.trim();
    const verifyCode = document.getElementById("verifyCode").value.trim();

    if (!username || !password || !email || !verifyCode) {
        showError("所有字段均为必填项");
        return;
    }

    const regBtn = document.getElementById("regBtn");
    regBtn.disabled = true;
    regBtn.innerText = "注册中...";

    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                username: username,
                password: password,
                email: email,
                code: verifyCode
            })
        });

        const result = await res.json();

        if (result.isSuccess) {
            alert("注册成功！即将跳转登录");
            window.location.href = "login.html";
        } else {
            showError(result.message);
            regBtn.disabled = false;
            regBtn.innerText = "立即注册";
        }
    } catch (e) {
        showError("服务器异常，请稍后再试");
        regBtn.disabled = false;
        regBtn.innerText = "立即注册";
    }
}

function showError(msg) {
    document.getElementById("errorMsg").innerText = msg;
}