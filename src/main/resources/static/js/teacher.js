let folders = [];
let deleteFolderId = null;

document.addEventListener("DOMContentLoaded", init);

function init() {
  const token = localStorage.getItem("token");

  if (!token) {
    window.location.href = "../pages/login.html";
    return;
  }

  loadFolders();
}

function logout() {
  localStorage.removeItem("token");
  window.location.href = "../pages/login.html";
}

async function loadFolders() {
  try {
    const res = await fetch("/folders", {
      headers: {
        Authorization: "Bearer " + localStorage.getItem("token")
      }
    });

    if (res.status === 401 || res.status === 403) {
      logout();
      return;
    }

    folders = await res.json();
    renderFolders();

  } catch (error) {
    console.error(error);
  }
}

function renderFolders() {
  const list = document.getElementById("folderList");

  if (!folders || folders.length === 0) {
    list.innerHTML = `
      <div class="empty-state">
        <h3>No folders found</h3>
        <p>Create your first folder to start uploading files.</p>
      </div>
    `;
    return;
  }

  list.innerHTML = folders.map(folder => `
    <div class="folder-card">

      <div class="folder-info">
        <h4>${folder.name}</h4>
        <p>${folder.year} • ${folder.branch}</p>
      </div>

      <div class="folder-actions">

        <div class="toggle-box">
          <label class="switch">
            <input
              type="checkbox"
              ${folder.enabled ? "checked" : ""}
              onchange="toggleFolder(${folder.id})"
            >
            <span class="slider"></span>
          </label>

          <span class="status-text">
            ${folder.enabled ? "Visible" : "Hidden"}
          </span>
        </div>

        <button onclick="openFiles(${folder.id})">
          Files
        </button>

        <button class="danger-btn"
          onclick="openDeleteModal(${folder.id})">
          Delete
        </button>

      </div>

    </div>
  `).join("");
}

function openFiles(id) {
  window.location.href =
      "../pages/folderfiles.html?id=" + id;
}

function openFolderModal() {
  document.getElementById("folderModal").style.display = "flex";
}

function closeFolderModal() {
  document.getElementById("folderModal").style.display = "none";

  document.getElementById("folderName").value = "";
  document.getElementById("year").value = "";
  document.getElementById("branch").value = "";
}

async function createFolder() {
  const name =
      document.getElementById("folderName").value.trim();

  const year =
      document.getElementById("year").value.trim();

  const branch =
      document.getElementById("branch").value.trim();

  if (!name || !year || !branch) {
    alert("Fill all fields");
    return;
  }

  const res = await fetch("/folders/upload", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + localStorage.getItem("token")
    },
    body: JSON.stringify({
      name,
      year,
      branch
    })
  });

  if (res.ok) {
    closeFolderModal();
    loadFolders();
  } else {
    alert("Failed to create folder");
  }
}

async function toggleFolder(id) {
  await fetch("/folders/toggle/" + id, {
    method: "PUT",
    headers: {
      Authorization: "Bearer " + localStorage.getItem("token")
    }
  });

  loadFolders();
}

function openDeleteModal(id) {
  deleteFolderId = id;
  document.getElementById("deleteModal").style.display = "flex";
}

function closeDeleteModal() {
  deleteFolderId = null;
  document.getElementById("deleteModal").style.display = "none";
}

async function confirmDelete() {
  if (!deleteFolderId) return;

  const res = await fetch("/folders/" + deleteFolderId, {
    method: "DELETE",
    headers: {
      Authorization: "Bearer " + localStorage.getItem("token")
    }
  });

  if (res.ok) {
    closeDeleteModal();
    loadFolders();
  } else {
    alert("Delete failed");
  }
}

window.onclick = function (e) {
  ["folderModal", "deleteModal"]
      .forEach(id => {
        const modal = document.getElementById(id);

        if (e.target === modal) {
          modal.style.display = "none";
        }
      });
};