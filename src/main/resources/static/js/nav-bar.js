
async function initUser() {
    const token = localStorage.getItem("Authorization");
    const loginLink = document.getElementById("loginLink");
    const avatarWrapper = document.getElementById("avatarWrapper");

    if (!token) {
        loginLink.style.display = "flex";
        avatarWrapper.style.display = "none";
        return;
    }

    try {
        const res = await fetch(`http://192.168.0.101:8080/user/profile`, {
            headers: { "Authorization": token }
        });
        const result = await res.json();
        if (result.isSuccess) {
            loginLink.style.display = "none";
            avatarWrapper.style.display = "flex";
            document.getElementById("userAvatar").src = result.data.avatar || 'https://via.placeholder.com/100';
            document.getElementById("navNickname").innerText = result.data.nickname || result.data.username;
        }
    } catch (e) {
        console.error("用户信息加载失败");
    }
}


document.addEventListener('DOMContentLoaded', () => {
    const publishBtn = document.getElementById("publishBtn");

    if (publishBtn) {
        publishBtn.addEventListener('click', (e) => {
            const token = localStorage.getItem("Authorization");

            if (!token) {
                // 1. 阻止默认跳转跳转（虽然 href 是 javascript:void(0)，但养成好习惯）
                e.preventDefault();

                // 2. 友好提示
                alert("您需要登录后才能发布文章");

                // 3. 跳转到登录页
                window.location.href = "login.html";
            } else {
                // 4. 已登录，跳转到发布页
                window.location.href = "add-article.html";
            }
        });
    }
});