const API_BASE = "http://192.168.0.101:8080";
const TOKEN_KEY = "Authorization";



window.onload = () => {
    if(!localStorage.getItem(TOKEN_KEY)) location.href = 'login.html';
    fetchProfile();
    initUser();
};

async function fetchProfile() {
    try {
        const res = await fetch(`${API_BASE}/user/profile`, {
            headers: { "Authorization": localStorage.getItem(TOKEN_KEY) }
        });
        const result = await res.json();
        if(result.isSuccess) {
            const u = result.data;
            document.getElementById('username').value = u.username;
            document.getElementById('nickname').value = u.nickname || "";
            document.getElementById('emailDisplay').innerText = u.email;
            const avatar = u.avatar || "https://via.placeholder.com/100";
            document.getElementById('mainAvatar').src = avatar;

            if(document.getElementById('oldEmailDisplay')) {
                document.getElementById('oldEmailDisplay').value = u.email;
            }
        }
    } catch(e) { console.error("加载失败", e); }
}

async function uploadFile() {
    // 兼容两个输入框
    const input = event.target;
    const file = input.files[0];
    if(!file) return;

    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch(`${API_BASE}/user/updateAvatar`, {
        method: "POST",
        headers: { "Authorization": localStorage.getItem(TOKEN_KEY) },
        body: formData
    });
    const result = await res.json();
    if(result.isSuccess) {
        alert("头像已成功更新");
        fetchProfile();
    }
}

function switchTab(type, el) {
    document.querySelectorAll('.menu-item').forEach(m => m.classList.remove('active'));
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));

    el.classList.add('active');
    document.getElementById(type + 'Section').classList.add('active');

    lucide.createIcons();
}

function togglePwd(id) {
    const input = document.getElementById(id);
    input.type = input.type === 'password' ? 'text' : 'password';
}

document.addEventListener('DOMContentLoaded', fetchProfile);

async function saveNickname() {
    const nickname = document.getElementById('nickname').value.trim();
    const res = await fetch(`${API_BASE}/user/updateNickname`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": localStorage.getItem(TOKEN_KEY) },
        body: JSON.stringify({ nickname })
    });
    const result = await res.json();
    showTip('infoMsg', result.message, result.isSuccess);
}


async function savePwd() {
    const oldPassword = document.getElementById('oldPwd').value;
    const newPassword = document.getElementById('newPwd').value;
    const confirmPwd = document.getElementById('confirmPwd').value;

    if(newPassword !== confirmPwd) return showTip('pwdMsg', '两次密码输入不一致', false);

    const res = await fetch(`${API_BASE}/user/changePassword`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": localStorage.getItem(TOKEN_KEY) },
        body: JSON.stringify({ oldPassword, newPassword })
    });
    const result = await res.json();
    if(result.isSuccess) {
        alert("密码修改成功，请重新登录");
        logout();
    } else {
        showTip('pwdMsg', result.message, false);
    }
}

function showTip(id, text, isOk) {
    const el = document.getElementById(id);
    el.innerText = text;
    el.className = "msg-tip " + (isOk ? "success" : "error");
}

function logout() {
    localStorage.removeItem(TOKEN_KEY);
    location.href = 'login.html';
}


let oldEmailToken = ""; // 用于存储第一步验证成功后的临时凭证

// 1. 获取旧邮箱验证码
async function sendOldEmailCode() {
    const email = document.getElementById('oldEmailDisplay').value;
    const btn = document.getElementById('sendOldBtn');

    const res = await fetch(`${API_BASE}/auth/sendVerificationCode?email=${email}&purpose=UPDATE_EMAIL_OLD`, {
        headers: { "Authorization": localStorage.getItem(TOKEN_KEY) }
    });
    startCountdown(btn);
}

// 2. 验证旧邮箱验证码
async function verifyOldEmail() {
    document.getElementById('')
    const code = document.getElementById('oldEmailCode').value;
    if(!code) return showTip('emailMsg', '请输入验证码', false);

    const res = await fetch(`${API_BASE}/user/verifyOldEmail`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": localStorage.getItem(TOKEN_KEY)
        },
        body: JSON.stringify({ code })
    });

    const result = await res.json();
    if(result.isSuccess) {
        oldEmailToken = result.data;
        // 切换到第二步
        document.getElementById('step1_verifyOld').style.display = 'none';
        document.getElementById('step2_bindNew').style.display = 'block';
        document.getElementById('emailStepDesc').innerText = "第二步：绑定新邮箱";
        showTip('emailMsg', '旧邮箱验证成功，请输入新邮箱', true);
    } else {
        showTip('emailMsg', result.message, false);
    }
}

// 3. 获取新邮箱验证码
async function sendNewEmailCode() {
    const email = document.getElementById('newEmail').value.trim();
    if(!/^\S+@\S+\.\S+$/.test(email)) return alert("邮箱格式错误");

    const btn = document.getElementById('sendNewBtn');
    const res = await fetch(`${API_BASE}/auth/sendVerificationCode?email=${email}&purpose=UPDATE_EMAIL_NEW`);
    startCountdown(btn);
}

// 4. 提交绑定新邮箱
async function saveNewEmail() {
    const email = document.getElementById('newEmail').value;
    const code = document.getElementById('newEmailCode').value;
    const ticket = oldEmailToken;

    const res = await fetch(`${API_BASE}/user/updateEmail`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": localStorage.getItem(TOKEN_KEY)
        },
        body: JSON.stringify({ email, code, ticket})
    });

    const result = await res.json();
    showTip('emailMsg', result.message, result.isSuccess);
    if(result.isSuccess) {
        setTimeout(() => location.reload(), 1500); // 绑定成功后刷新页面
    }
}

// 辅助：重置步骤
function resetEmailStep() {
    document.getElementById('step1_verifyOld').style.display = 'block';
    document.getElementById('step2_bindNew').style.display = 'none';
    document.getElementById('emailStepDesc').innerText = "第一步：验证当前绑定邮箱";
}

// 辅助：通用倒计时
function startCountdown(btn) {
    let count = 60;
    btn.disabled = true;
    btn.style.width = btn.offsetWidth + 'px';
    const timer = setInterval(() => {
        if(count <= 0) {
            clearInterval(timer);
            btn.innerText = "获取验证码";
            btn.disabled = false;
            btn.style.width = 'auto';
        } else {
            btn.innerText = `${count--}s`;
        }
    }, 1000);
}