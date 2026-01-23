import { API_BASE } from "./config.js";
import { initUser } from "./nav-bar.js";

let currentPage = 1;
const pageSize = 9;

// --- 1. 初始化 ---
document.addEventListener('DOMContentLoaded', () => {
    initUser();
    fetchArticles(1);

    // 绑定分页按钮监听
    document.getElementById('prevBtn')?.addEventListener('click', () => changePage(-1));
    document.getElementById('nextBtn')?.addEventListener('click', () => changePage(1));

    // 绑定详情弹窗关闭
    const modal = document.getElementById('articleModal');
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target.id === 'articleModal' || e.target.classList.contains('close-btn')) {
                closeModal();
            }
        });
    }

    // 核心：文章列表容器的事件委托
    const container = document.getElementById('articleList');
    if (container) {
        container.addEventListener('click', handleListClick);
    }
});

// --- 2. 事件委托分发器 ---
function handleListClick(e) {
    const target = e.target;

    // A. 点击标题进入详情
    const titleEl = target.closest('.article-title-link');
    if (titleEl) {
        showDetail(titleEl.dataset.id);
        return;
    }

    // B. 点击点赞/收藏/评论按钮
    const trigger = target.closest('.action-trigger');
    if (trigger) {
        const action = trigger.dataset.action;
        const articleItem = trigger.closest('.article-item');
        const articleId = articleItem.dataset.id;

        if (action === 'like') handleLike(articleId, trigger);
        else if (action === 'collect') handleCollect(articleId, trigger);
        else if (action === 'view-comments') toggleCommentsSection(articleId, articleItem);
        return;
    }

    // C. 评论区内部交互
    if (target.classList.contains('reply-btn')) {
        const { commentId, nickname, rootId } = target.dataset;
        const articleId = target.closest('.article-item').dataset.id;
        openReplyInput(articleId, rootId || commentId, commentId, nickname, target);
    }
    else if (target.classList.contains('view-sub-btn')) {
        fetchSubComments(target.dataset.commentId, target);
    }
    else if (target.classList.contains('submit-comment-btn')) {
        const { articleId, rootId, replyToId } = target.dataset;
        submitComment(articleId, rootId, replyToId, target);
    }
    else if (target.classList.contains('cancel-reply-btn')) {
        target.parentElement.remove();
    }
}

// --- 3. 获取与渲染 ---
async function fetchArticles(page = 1) {
    currentPage = page;
    const searchDTO = {
        keyword: document.getElementById('keyword')?.value.trim() || null,
        author: document.getElementById('authorName')?.value.trim() || null,
        from: document.getElementById('startDate')?.value || null,
        to: document.getElementById('endDate')?.value || null,
        page: page,
        size: pageSize
    };

    const listContainer = document.getElementById('articleList');
    const token = localStorage.getItem("Authorization");
    listContainer.innerHTML = '<div style="grid-column: 1/-1; text-align: center;">加载中...</div>';

    try {
        const res = await fetch(`${API_BASE}/article/search`, {
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
        console.log(err)
        listContainer.innerHTML = '加载失败，请重试';
    }
}

function renderList(articles) {
    const container = document.getElementById('articleList');
    if (!articles || articles.length === 0) {
        container.innerHTML = `<div style="text-align:center; padding:50px; color:var(--text-muted);">没找到相关文章</div>`;
        return;
    }

    container.innerHTML = articles.map(art => `
        <div class="article-item" data-id="${art.id}">
            <h3 class="article-title-link" data-id="${art.id}" style="cursor:pointer">${art.title}</h3>
            <div class="article-meta-bar">
                <div class="meta-unit action-trigger ${art.isLiked ? 'active' : ''}" 
                     data-action="like" style="${art.isLiked ? 'color: var(--primary);' : ''}">
                    <i data-lucide="thumbs-up" size="14"></i>
                    <span>点赞 <b class="count">${art.likeCount || 0}</b></span>
                </div>
                
                <div class="meta-unit action-trigger ${art.isFavorite ? 'active' : ''}" 
                     data-action="collect" style="${art.isFavorite ? 'color: #fadb14;' : ''}">
                    <i data-lucide="star" size="14"></i>
                    <span>收藏 <b class="count">${art.favoriteCount || 0}</b></span>
                </div>

                <div class="meta-unit action-trigger" data-action="view-comments">
                    <i data-lucide="message-square" size="14"></i>
                    <span>评论 <b>${art.commentCount || 0}</b></span>
                </div>
                
                <div class="meta-unit">
                    <i data-lucide="user" size="14"></i>
                    <span>作者：<b>${art.authorName || '匿名'}</b></span>
                </div>
            </div>
        </div>
    `).join('');

    lucide.createIcons();
}
/**
 * 核心修改：抽离加载逻辑
 */
async function loadComments(articleId, commentBox) {
    commentBox.innerHTML = '<div class="loading-status">加载评论中...</div>';
    try {
        const res = await fetch(`${API_BASE}/article/comment/${articleId}`, {
            headers: { "Authorization": localStorage.getItem("Authorization") }
        });
        const result = await res.json();
        if (result.isSuccess) {
            renderTopComments(commentBox, result.data, articleId);
        }
    } catch (err) {
        commentBox.innerHTML = '<div class="error">无法加载评论</div>';
    }
}

/**
 * 修改后的切换函数
 */
async function toggleCommentsSection(articleId, articleItem) {
    let commentBox = articleItem.querySelector('.comment-section');
    if (commentBox) {
        const isHidden = commentBox.style.display === 'none';
        commentBox.style.display = isHidden ? 'block' : 'none';
        // 如果是从隐藏变为显示，可以选择刷一下最新评论
        if (isHidden) loadComments(articleId, commentBox);
        return;
    }

    commentBox = document.createElement('div');
    commentBox.className = 'comment-section';
    articleItem.appendChild(commentBox);
    await loadComments(articleId, commentBox);
}

/**
 * 渲染顶级评论 - 加入头像和布局优化
 */
function renderTopComments(container, comments, articleId) {
    const listHtml = comments.length === 0
        ? '<div class="empty-tip">暂无评论，点击回复来抢沙发吧</div>'
        : comments.map(c => `
            <div class="top-comment-item" data-comment-id="${c.id}" style="display: flex; gap: 12px; padding: 15px 0;">
                <img src="${c.avatar}" class="comment-avatar">
                <div style="flex: 1;">
                    <div class="comment-content">
                        <b style="color: #1e293b;">${c.nickname}</b>
                        <p style="margin: 4px 0; color: #475569;">${c.content}</p>
                        <div style="font-size: 12px; color: #94a3b8;">
                            <span class="reply-btn" data-comment-id="${c.id}" data-nickname="${c.nickname}" style="cursor:pointer">回复</span>
                            <span class="view-sub-btn" data-comment-id="${c.id}" style="cursor:pointer; margin-left:12px;">查看回复(${c.subCount || 0})</span>
                        </div>
                    </div>
                    <div class="sub-comment-container" id="sub-container-${c.id}"></div>
                </div>
            </div>
        `).join('');

    container.innerHTML = `
        <div class="quick-reply-bar">
            <input type="text" placeholder="发表你的看法..."> 
            <button class="submit-comment-btn" data-article-id="${articleId}">发送</button>
        </div>
        ${listHtml}
    `;
}

/**
 * 获取并渲染次级评论 - 加入头像
 */
async function fetchSubComments(topId, btn) {
    const subContainer = document.getElementById(`sub-container-${topId}`);

    // 获取当前按钮中记录的数量（从原始文本中提取数字，防止消失）
    const countMatch = btn.innerText.match(/\d+/);
    const currentCount = countMatch ? countMatch[0] : 0;

    // 1. 如果已经加载过内容，处理显示/隐藏切换
    if (subContainer.innerHTML !== "") {
        const isHidden = subContainer.style.display === 'none';
        subContainer.style.display = isHidden ? 'block' : 'none';

        // 修复点：切换时重新拼接数量，确保不消失
        btn.innerText = isHidden ? `收起回复(${currentCount})` : `查看回复(${currentCount})`;
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/article/comment/get/${topId}`, {
            headers: { "Authorization": localStorage.getItem("Authorization") }
        });
        const result = await res.json();

        if (result.isSuccess) {
            if (!result.data || result.data.length === 0) {
                subContainer.innerHTML = `<div class="empty-sub-tip">暂无更多回复</div>`;
                btn.innerText = `查看回复(0)`;
            } else {
                subContainer.innerHTML = result.data.map(sc => `
                    <div class="sub-comment-item" style="display: flex; gap: 10px; margin-bottom: 12px;">
                        <img src="${sc.avatar}" class="comment-avatar" style="width:24px; height:24px;">
                        <div style="flex:1; font-size: 13px;">
                            <span style="font-weight:600; color:var(--primary)">${sc.nickname}</span> 
                            回复 <span style="color:#64748b">@${sc.replyToName}</span>: 
                            <span style="color: #334155;">${sc.content}</span>
                            <div style="font-size: 11px; margin-top:2px;">
                                <span class="reply-btn" data-root-id="${topId}" data-comment-id="${sc.id}" data-nickname="${sc.nickname}" style="cursor:pointer; color:#94a3b8;">回复</span>
                            </div>
                        </div>
                    </div>
                `).join('');

                // 加载成功后，更新按钮文字为收起，并显示最新获取的数量
                btn.innerText = `收起回复(${result.data.length})`;
            }
            subContainer.style.display = 'block';
        }
    } catch (err) {
        showActionTip(btn, "加载失败");
    }
}


function openReplyInput(articleId, rootId, replyToId, targetName, btnElement) {
    document.querySelector('.temp-reply-box')?.remove();

    const replyBox = document.createElement('div');
    replyBox.className = 'temp-reply-box';
    replyBox.innerHTML = `
        <input type="text" placeholder="回复 @${targetName}:">
        <div class="reply-btns">
            <button class="btn-text-sm cancel-reply-btn" style="background:none; border:none; color:#94a3b8; cursor:pointer;">取消</button>
            <button class="submit-comment-btn btn-primary-sm" 
                    style="background:var(--primary); color:white; border:none; padding:4px 12px; border-radius:4px; cursor:pointer;"
                    data-article-id="${articleId}" data-root-id="${rootId}" data-reply-to-id="${replyToId}">提交回复</button>
        </div>
    `;

    btnElement.closest('.comment-content, .sub-comment-item').appendChild(replyBox);
    replyBox.querySelector('input').focus();
}


async function submitComment(articleId, rootId, replyToId, btn) {
    const token = localStorage.getItem("Authorization");
    if (!token) return showActionTip(btn, "请先登录");

    // --- 修改点：更精准地定位 input ---
    // 不管按钮套了多少层，都在它所属的那个大容器（评论区或回复框）里找 input
    const box = btn.closest('.quick-reply-bar') || btn.closest('.temp-reply-box');
    const input = box?.querySelector('input');

    const content = input?.value.trim();
    if (!content) return showActionTip(btn, "内容不能为空");

    btn.disabled = true;
    try {
        const url = !rootId ? `${API_BASE}/article/comment/add` : `${API_BASE}/article/comment/reply`;
        const payload = !rootId
            ? { articleId, content }
            : { articleId, rootId, replyToId, content };

        const res = await fetch(url, {
            method: 'POST',
            headers: { "Authorization": token, "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const result = await res.json();

        if (result.isSuccess) {
            showActionTip(btn, "发送成功");
            input.value = "";

            // --- 刷新逻辑 ---
            const articleItem = btn.closest('.article-item');
            const commentBox = articleItem.querySelector('.comment-section');

            if (!rootId) {
                await loadComments(articleId, commentBox);
            } else {
                const subContainer = document.getElementById(`sub-container-${rootId}`);
                if (subContainer) {
                    subContainer.innerHTML = "";
                    const viewBtn = subContainer.parentElement.querySelector('.view-sub-btn');
                    await fetchSubComments(rootId, viewBtn);
                }
                // --- 修改点：这里使用 box 变量来移除临时回复框 ---
                if (box && box.classList.contains('temp-reply-box')) {
                    setTimeout(() => box.remove(), 500);
                }
            }
        } else {
            showError?.(result.message) || showActionTip(btn, result.message);
        }
    } catch (err) {
        showActionTip(btn, "网络异常");
    } finally {
        btn.disabled = false;
    }
}

// --- 5. 详情与通用辅助 ---
async function showDetail(articleId) {
    const modal = document.getElementById('articleModal');
    const modalBody = document.getElementById('modalBody');
    if (!modal || !modalBody) return;

    modal.classList.add('active');
    modalBody.innerHTML = '<div style="padding:20px; text-align:center;">加载中...</div>';

    try {
        const res = await fetch(`${API_BASE}/article/detail/${articleId}`);
        const result = await res.json();
        if (result.isSuccess) {
            const art = result.data;
            modalBody.innerHTML = `
                <h1 style="font-size: 24px; margin-bottom: 16px;">${art.title}</h1>
                <div style="color: var(--text-muted); font-size: 13px; margin-bottom: 20px;">
                    作者：${art.authorName} | 时间：${art.createdAt || '刚刚'}
                </div>
                <div class="detail-text" style="line-height: 1.8; color: var(--text-body); white-space: pre-wrap;">
                    ${art.content}
                </div>
            `;
        }
    } catch (e) {
        modalBody.innerHTML = '内容加载失败';
    }
}

function closeModal() {
    document.getElementById('articleModal')?.classList.remove('active');
}

function updatePaginationUI(currentCount) {
    const pageInfo = document.getElementById('pageInfo');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');

    // 1. 更新页码文字（如果存在）
    if (pageInfo) {
        pageInfo.innerText = `第 ${currentPage} 页`;
    }

    // 2. 处理“上一页”按钮（如果存在）
    if (prevBtn) {
        prevBtn.disabled = (currentPage === 1);
    }

    // 3. 处理“下一页”按钮（如果存在）
    // 增加防御性判断，防止 querySelector 返回 null
    if (nextBtn) {
        // 这里的 pageSize 要么使用全局变量，要么直接写死（如 9）
        nextBtn.disabled = (currentCount < pageSize);
    }
}

function changePage(delta) {
    const newPage = currentPage + delta;
    if (newPage >= 1) {
        fetchArticles(newPage);
        window.scrollTo({top: 0, behavior: 'smooth'});
    }
}

async function handleLike(id, element) {
    commonAction(id, element, `${API_BASE}/article/like/${id}`, "点赞");
}

async function handleCollect(id, element) {
    commonAction(id, element, `${API_BASE}/article/favorite/${id}`, "收藏");
}

// 封装点赞和收藏的通用请求
async function commonAction(id, element, url, actionName) {
    const token = localStorage.getItem("Authorization");
    if (!token) return showActionTip(element, "请先登录");
    if (element.dataset.loading === "true") {
        showActionTip(element, "正在处理...");
        return;
    }


    element.dataset.loading = "true";
    try {
        const res = await fetch(url, {
            method: 'POST',
            headers: { "Authorization": token, "Content-Type": "application/json" }
        });
        const result = await res.json();
        if (result.isSuccess) {
            const countBox = element.querySelector('.count');
            const isActive = result.data;
            countBox.innerText = parseInt(countBox.innerText) + (isActive ? 1 : -1);
            element.classList.toggle('active', isActive);
            element.style.color = isActive ? (actionName === "点赞" ? "var(--primary)" : "#fadb14") : "";
            showActionTip(element, isActive ? `${actionName}成功` : `已取消${actionName}`);
            triggerLikeAnimation(element);
        }
    } catch (err) {
        showActionTip(element, "网络错误");
    } finally {
        element.dataset.loading = "false";
    }
}

function showActionTip(targetElement, message) {
    const oldTip = targetElement.querySelector('.action-tip');
    if (oldTip) oldTip.remove();
    const tip = document.createElement('span');
    tip.className = 'action-tip';
    tip.innerText = message;
    targetElement.style.position = 'relative';
    targetElement.appendChild(tip);
    setTimeout(() => {
        tip.style.opacity = '0';
        tip.style.transform = 'translateY(-20px)';
        setTimeout(() => tip.remove(), 300);
    }, 1500);
}

function triggerLikeAnimation(el) {
    el.style.transform = "scale(1.3)";
    setTimeout(() => { el.style.transform = "scale(1)"; }, 200);
}