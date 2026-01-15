const API_BASE = "http://192.168.0.101:8080/article";


let currentPage = 1;
const pageSize = 9;


// 1. 获取文章列表（带分页和多重筛选）
async function fetchArticles(page = 1) {

    const keyword = document.getElementById('keyword').value.trim() || null;
    const author = document.getElementById('authorName').value.trim() || null;
    const from = document.getElementById('startDate').value || null;
    const to = document.getElementById('endDate').value || null;

    const searchDTO = {
        keyword,
        author,
        from,
        to,
        page: page,
        size: 10
    };
    // 显示加载状态
    const listContainer = document.getElementById('articleList');
    const token = localStorage.getItem("Authorization");

    listContainer.innerHTML = '<div style="grid-column: 1/-1; text-align: center;">加载中...</div>';

    try {
        console.log(searchDTO);
        const res = await fetch(`${API_BASE}/search`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": token
            },
            body: JSON.stringify(searchDTO)
        });
        const data = await res.json();
        if (data.isSuccess) {
            renderList(data.data);
            updatePaginationUI(data.data.length);
        } else {
            listContainer.innerHTML = data.message;
        }
    } catch (err) {
        listContainer.innerHTML = '加载失败，请重试';
    }
}

/**
 * 修复后的分页 UI 更新函数
 * @param {number} currentCount 当前页返回的数据条数
 */
function updatePaginationUI(currentCount) {
    const pageInfo = document.getElementById('pageInfo');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');

    if (pageInfo) {
        pageInfo.innerText = `第 ${currentPage} 页`;
    }

    // 处理“上一页”按钮：第一页时禁用
    if (prevBtn) {
        prevBtn.disabled = (currentPage === 1);
    }

    /**
     * 处理“下一页”按钮逻辑：
     * 如果当前返回的数据条数小于每页条数 (pageSize)，说明已经没有更多数据了，禁用下一页。
     */
    if (nextBtn) {
        const pageSize = 9; // 确保与 fetchArticles 中的 pageSize 一致
        nextBtn.disabled = (currentCount < pageSize);
    }
}

/**
 * 翻页点击事件
 * @param {number} delta 变化量，-1 代表上一页，1 代表下一页
 */
function changePage(delta) {
    const newPage = currentPage + delta;
    if (newPage >= 1) {
        // 调用搜索函数加载新的一页
        fetchArticles(newPage)
        // 自动回到顶部，提升体验
        window.scrollTo({top: 0, behavior: 'smooth'});
    }
}

// 2. 渲染卡片列表
function renderList(articles) {
    const container = document.getElementById('articleList');
    if (!articles || articles.length === 0) {
        container.innerHTML = `<div style="text-align:center; padding:50px; color:var(--text-muted);">没找到相关文章</div>`;
        return;
    }

    container.innerHTML = articles.map(art => {
        // 预判断状态
        const likedClass = art.isLiked ? 'active' : '';
        const likedStyle = art.isLiked ? 'color: var(--primary);' : '';

        const collectedClass = art.isCollected ? 'active' : '';
        const collectedStyle = art.isCollected ? 'color: #fadb14;' : '';

        return `
        <div class="article-item" data-id="${art.id}">
            <h3 onclick="showDetail(${art.id})">${art.title}</h3>
            <div class="article-meta-bar">
                <div class="meta-unit action-trigger ${likedClass}" 
                     data-action="like" style="${likedStyle}">
                    <i data-lucide="thumbs-up" size="14"></i>
                    <span>点赞 <b class="count">${art.likeCount || 0}</b></span>
                </div>
                
                <div class="meta-unit action-trigger ${collectedClass}" 
                     data-action="collect" style="${collectedStyle}">
                    <i data-lucide="star" size="14"></i>
                    <span>收藏 <b class="count">${art.collectCount || 0}</b></span>
                </div>

                <div class="meta-unit" onclick="showDetail(${art.id})">
                    <i data-lucide="message-square" size="14"></i>
                    <span>评论 <b>${art.commentCount || 0}</b></span>
                </div>
                
                <div class="meta-unit">
                    <i data-lucide="user" size="14"></i>
                    <span>作者：<b>${art.authorName || '匿名'}</b></span>
                </div>
            </div>
        </div>
    `;
    }).join('');

    // 渲染新生成的图标
    lucide.createIcons();
}



// 1. 先获取容器（确保 ID 与 HTML 中的一致，比如 'articleList'）
const container = document.getElementById('articleList');

// 2. 然后再绑定事件
if (container) {
    container.addEventListener('click', async (e) => {
        const trigger = e.target.closest('.action-trigger');
        if (!trigger) return;

        e.stopPropagation();

        const action = trigger.dataset.action;
        const articleItem = trigger.closest('.article-item');
        const articleId = articleItem.dataset.id;

        if (action === 'like') {
            handleLike(articleId, trigger);
        } else if (action === 'collect') {
            handleCollect(articleId, trigger);
        }
    });
} else {
    console.error("未找到文章列表容器，请检查 ID 是否正确");
}

async function handleLike(id, element) {
    const token = localStorage.getItem("Authorization");
    if (!token) {
        showActionTip(element, "请先登录"); // 非弹窗提示
        return;
    }

    // 状态锁定：如果正在加载，显示提示并返回
    if (element.dataset.loading === "true") {
        showActionTip(element, "操作太快啦...");
        return;
    }

    element.dataset.loading = "true";

    try {
        const res = await fetch(`${API_BASE}/like/${id}`, {
            method: 'POST',
            headers: {
                "Authorization": token,
                "Content-Type": "application/json"
            }
        });
        const result = await res.json();

        if (result.isSuccess) {
            const countBox = element.querySelector('.count');
            const isLiked = result.data;

            if (isLiked) {
                countBox.innerText = parseInt(countBox.innerText) + 1;
                element.classList.add('active');
                element.style.color = "var(--primary)";
                showActionTip(element, "点赞成功！");
            } else {
                countBox.innerText = Math.max(0, parseInt(countBox.innerText) - 1);
                element.classList.remove('active');
                element.style.color = "";
                showActionTip(element, "已取消点赞");
            }
            triggerLikeAnimation(element);
        }
    } catch (err) {
        showActionTip(element, "网络错误");
    } finally {
        element.dataset.loading = "false";
    }
}

/**
 * 创建一个临时的气泡提示
 */
function showActionTip(targetElement, message) {
    // 移除旧的提示（防止重叠）
    const oldTip = targetElement.querySelector('.action-tip');
    if (oldTip) oldTip.remove();

    // 创建提示元素
    const tip = document.createElement('span');
    tip.className = 'action-tip';
    tip.innerText = message;

    // 将提示插入到按钮中
    targetElement.style.position = 'relative'; // 确保定位基准
    targetElement.appendChild(tip);

    // 1.5秒后自动消失
    setTimeout(() => {
        tip.style.opacity = '0';
        tip.style.transform = 'translateY(-20px)';
        setTimeout(() => tip.remove(), 300);
    }, 1500);
}

// 简单的点赞缩放动画
function triggerLikeAnimation(el) {
    el.style.transform = "scale(1.3)";
    setTimeout(() => {
        el.style.transform = "scale(1)";
    }, 200);
}

async function handleCollect(id, element) {
    // 1. 检查登录状态 (假设你把 token 存在 localStorage)
    const token = localStorage.getItem("Authorization");
    if (!token) {
        alert("请先登录后再收藏");
        window.location.href = "login.html";
        return;
    }

    // 2. 防止重复点击 (Loading 状态)
    if (element.classList.contains('loading')) return;
    element.classList.add('loading');

    try {
        // 3. 调用后端接口 (根据你的后端设计，通常是 POST 切换状态)
        const res = await fetch(`${API_BASE}/collect/${id}`, {
            method: 'POST',
            headers: {
                "Authorization": token,
                "Content-Type": "application/json"
            }
        });
        const result = await res.json();

        if (result.isSuccess) {
            const countBox = element.querySelector('.count');
            const icon = element.querySelector('i');

            // 假设后端返回 result.data 为 true(已收藏) 或 false(取消收藏)
            const isCollected = result.data;

            if (isCollected) {
                // UI 表现：变为已收藏状态
                countBox.innerText = parseInt(countBox.innerText) + 1;
                element.style.color = "#fadb14"; // 经典的星星黄
                element.classList.add('active');
                // 如果需要切换图标，可以重新渲染图标
                icon.setAttribute('data-lucide', 'star-full');
            } else {
                // UI 表现：取消收藏
                countBox.innerText = Math.max(0, parseInt(countBox.innerText) - 1);
                element.style.color = ""; // 恢复默认
                element.classList.remove('active');
                icon.setAttribute('data-lucide', 'star');
            }

            // 重新刷新该元素的 Lucide 图标
            lucide.createIcons();
        } else {
            alert(result.message || "收藏失败");
        }
    } catch (err) {
        console.error("收藏请求出错:", err);
    } finally {
        element.classList.remove('loading');
    }
}


// 3. 详情弹窗逻辑
async function showDetail(articleId) {
    const modal = document.getElementById('articleModal');
    const modalBody = document.getElementById('modalBody');

    // 防御性检查：如果找不到元素，直接报错并返回，不至于让整个脚本卡死
    if (!modal || !modalBody) {
        console.error("错误：找不到 ID 为 articleModal 或 modalBody 的元素！请检查 HTML。");
        return;
    }

    // 显示弹窗
    modal.classList.add('active');
    modalBody.innerHTML = '<div style="padding:20px; text-align:center;">加载中...</div>';

    try {
        const res = await fetch(`${API_BASE}/detail/${articleId}`);
        const result = await res.json();

        if (result.isSuccess) {
            const art = result.data;
            modalBody.innerHTML = `
                <h1 style="font-size: 24px; margin-bottom: 16px;">${art.title}</h1>
                <div style="color: var(--text-muted); font-size: 13px; margin-bottom: 20px;">
                    作者：${art.authorName} | 时间：${art.createdAt || '刚刚'}
                </div>
                <div class="detail-text" style="line-height: 1.8; color: var(--text-body);">
                    ${art.content}
                </div>
            `;
        }
    } catch (e) {
        modalBody.innerHTML = '内容加载失败';
    }
}


function updatePagination(currentCount) {
    document.getElementById('pageInfo').innerText = `第 ${currentPage} 页`;
    document.getElementById('prevBtn').disabled = (currentPage === 1);
    // 假设后端如果不返回总数，如果当前页不满 pageSize，则说明没下一页了
    document.getElementById('nextBtn').disabled = (currentCount < pageSize);
}

function closeModal() {
    const modal = document.getElementById('articleModal');
    if (modal) {
        // 核心修复：不要改 .style.display，而是移除 active 类
        modal.classList.remove('active');
    }
}

// 退出登录逻辑
document.getElementById('logoutBtn')?.addEventListener('click', () => {
    localStorage.removeItem("Authorization");
    location.reload();
});

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    initUser();
    fetchArticles(1);
});

// 点击遮罩层也可以关闭
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('articleModal');
    if (modal) {
        modal.addEventListener('click', (e) => {
            // 如果点的是遮罩层背景（不是内容区），则关闭
            if (e.target.id === 'articleModal') {
                closeModal();
            }
        });
    }
    // initUser();
    // fetchArticles(1);
});
