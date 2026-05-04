let folderId = null;
let currentTab = "content";
let fileIdPendingDelete = null;

document.addEventListener("DOMContentLoaded", init);

function init() {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "../pages/login.html";
        return;
    }

    const params = new URLSearchParams(window.location.search);
    folderId = params.get("id");

    if (!folderId) {
        window.location.href = "../pages/teacherdashboard.html";
        return;
    }

    loadFolderTitle();
    showTab("content");
    loadFiles();
}

function goBack() {
    window.location.href = "../pages/teacherdashboard.html";
}

function loadFolderTitle() {
    const title = document.getElementById("folderTitle");
}

function showTab(tab) {
    currentTab = tab;

    const contentSection = document.getElementById("contentSection");
    const assignmentsSection = document.getElementById("assignmentsSection");

    const contentBtn = document.getElementById("contentTabBtn");
    const assignmentBtn = document.getElementById("assignmentTabBtn");

    if (tab === "content") {
        contentSection.classList.remove("hidden");
        assignmentsSection.classList.add("hidden");
        contentBtn.classList.add("active");
        assignmentBtn.classList.remove("active");
        loadFiles();
    } else {
        contentSection.classList.add("hidden");
        assignmentsSection.classList.remove("hidden");
        assignmentBtn.classList.add("active");
        contentBtn.classList.remove("active");
        loadAssignments();
    }
}

async function loadFiles() {
    const res = await fetch("/folders/files/" + folderId, {
        headers: {
            Authorization: "Bearer " + localStorage.getItem("token")
        }
    });

    if (res.status === 401 || res.status === 403) {
        localStorage.removeItem("token");
        window.location.href = "../pages/login.html";
        return;
    }

    const files = await res.json();
    renderFiles(files);
}

function renderFiles(files) {
    const list = document.getElementById("fileList");

    if (!files || files.length === 0) {
        list.innerHTML = `
            <div class="empty-state">
                <h3>No files found</h3>
                <p>Upload your first file in this folder.</p>
            </div>
        `;
        return;
    }

    list.innerHTML = files.map(file => `
        <div class="file-row">
            <div>
                <div class="item-title">${file.title}</div>
            </div>

            <div class="file-actions">
                <a href="${file.fileUrl}" target="_blank">View</a>
                <button onclick="deleteFile(${file.id})">Delete</button>
            </div>
        </div>
    `).join("");
}

function openUploadModal() {
    document.getElementById("uploadModal").style.display = "flex";
}

function closeUploadModal() {
    document.getElementById("uploadModal").style.display = "none";
    document.getElementById("fileName").value = "";
    document.getElementById("fileInput").value = "";
}

async function uploadFile() {
    const title = document.getElementById("fileName").value.trim();
    const file = document.getElementById("fileInput").files[0];

    if (!title) {
        alert("Enter file name");
        return;
    }

    if (!file) {
        alert("Choose file");
        return;
    }

    const formData = new FormData();
    formData.append("title", title);
    formData.append("file", file);
    formData.append("folderId", folderId);

    const res = await fetch("/content/upload", {
        method: "POST",
        headers: {
            Authorization: "Bearer " + localStorage.getItem("token")
        },
        body: formData
    });

    if (res.ok) {
        closeUploadModal();
        alert("Upload successful");
        loadFiles();
    } else {
        alert("Upload failed");
    }
}

async function deleteFile(id) {
    fileIdPendingDelete = id;
    document.getElementById("deleteConfirmModal").style.display = "flex";
}

function closeDeleteConfirmModal() {
    fileIdPendingDelete = null;
    document.getElementById("deleteConfirmModal").style.display = "none";
}

async function confirmDeleteFile() {
    if (!fileIdPendingDelete) return;

    const res = await fetch("/content/delete/" + fileIdPendingDelete, {
        method: "DELETE",
        headers: {
            Authorization: "Bearer " + localStorage.getItem("token")
        }
    });

    if (res.ok) {
        closeDeleteConfirmModal();
        loadFiles();
    } else {
        alert("Delete failed");
    }
}

async function loadAssignments() {
    const res = await fetch("/folders/assignment/folder/" + folderId, {
        headers: {
            Authorization: "Bearer " + localStorage.getItem("token")
        }
    });

    if (res.status === 401) {
        localStorage.removeItem("token");
        window.location.href = "../pages/login.html";
        return;
    }

    if (res.status === 403) {
        const list = document.getElementById("assignmentList");
        list.innerHTML = `
            <div class="empty-state">
                <h3>Access denied</h3>
                <p>You are not allowed to view assignments for this folder.</p>
            </div>
        `;
        return;
    }

    if (!res.ok) {
        const list = document.getElementById("assignmentList");
        list.innerHTML = `
            <div class="empty-state">
                <h3>Unable to load assignments</h3>
                <p>Please try again in a moment.</p>
            </div>
        `;
        return;
    }
    
    const assignments = await res.json();
    renderAssignments(assignments);
}

function renderAssignments(assignments) {
    const list = document.getElementById("assignmentList");

    if (!assignments || assignments.length === 0) {
        list.innerHTML = `
            <div class="empty-state">
                <h3>No assignments found</h3>
                <p>Create your first assignment for this folder.</p>
            </div>
        `;
        return;
    }

    list.innerHTML = assignments.map(a => `
        <div class="assignment-card">
            <div class="assignment-info">
                <h4>${a.title}</h4>
                <p>${a.description}</p>
                <div class="assignment-meta">
                    <span>Due: ${a.dueDate ? new Date(a.dueDate).toLocaleString() : "No due date"}</span>
                    <span class="status ${a.open ? "open" : "closed"}">
                        ${a.open ? "Open" : "Closed"}
                    </span>
                </div>
            </div>

            <div class="assignment-actions">
                <button onclick="toggleAssignment(${a.id})">
                    ${a.open ? "Close" : "Open"}
                </button>
                <button class="secondary-btn" onclick="viewSubmissions(${a.id})">
                    View Submissions
                </button>
            </div>
        </div>
    `).join("");
}

function openAssignmentModal() {
    document.getElementById("assignmentModal").style.display = "flex";
}

function closeAssignmentModal() {
    document.getElementById("assignmentModal").style.display = "none";
    document.getElementById("assignmentTitle").value = "";
    document.getElementById("assignmentDescription").value = "";
    document.getElementById("assignmentDueDate").value = "";
}

async function createAssignment() {
    const title = document.getElementById("assignmentTitle").value.trim();
    const description = document.getElementById("assignmentDescription").value.trim();
    const dueDate = document.getElementById("assignmentDueDate").value;

    if (!title || !description) {
        alert("Fill title and description");
        return;
    }

    const res = await fetch("/folders/assignment/create", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + localStorage.getItem("token")
        },
        body: JSON.stringify({
            title: title,
            description: description,
            folderId: folderId,
            dueDate: dueDate ? dueDate : null
        })
    });

    if (res.ok) {
        closeAssignmentModal();
        alert("Assignment created");
        loadAssignments();
    } else {
        alert("Failed to create assignment");
    }
}

async function toggleAssignment(id) {
    const res = await fetch("/folders/assignment/" + id + "/toggle", {
        method: "PUT",
        headers: {
            Authorization: "Bearer " + localStorage.getItem("token")
        }
    });

    if (res.ok) {
        loadAssignments();
    } else {
        alert("Failed to update assignment");
    }
}

function viewSubmissions(id) {
    window.location.href = "../pages/submissions.html?assignmentId=" + id;
}

window.onclick = function (e) {
    const uploadModal = document.getElementById("uploadModal");
    const assignmentModal = document.getElementById("assignmentModal");
    const deleteConfirmModal = document.getElementById("deleteConfirmModal");

    if (e.target === uploadModal) {
        closeUploadModal();
    }

    if (e.target === assignmentModal) {
        closeAssignmentModal();
    }

    if (e.target === deleteConfirmModal) {
        closeDeleteConfirmModal();
    }
};