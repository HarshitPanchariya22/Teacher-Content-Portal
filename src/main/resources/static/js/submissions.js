let assignmentId = null;
let folderId = null;

document.addEventListener("DOMContentLoaded", initPage);

async function initPage() {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "../pages/login.html";
        return;
    }

    const params = new URLSearchParams(window.location.search);
    assignmentId = params.get("assignmentId");

    if (!assignmentId) {
        renderError("Assignment id is missing in URL.");
        return;
    }

    await loadAssignment();
    await loadSubmissions();
}

async function loadAssignment() {
    const res = await fetch("/folders/assignment/id/" + assignmentId, {
        headers: {
            Authorization: "Bearer " + localStorage.getItem("token")
        }
    });

    if (res.status === 401 || res.status === 403) {
        localStorage.removeItem("token");
        window.location.href = "../pages/login.html";
        return;
    }

    if (!res.ok) {
        renderError("Unable to load assignment details.");
        return;
    }

    const assignment = await res.json();
    folderId = assignment.folderId || null;

    document.getElementById("assignmentTitle").textContent = assignment.title || "Untitled assignment";
    document.getElementById("assignmentDescription").textContent = assignment.description || "No description";
    document.getElementById("assignmentDueDate").textContent =
        "Due date: " + (assignment.dueDate ? new Date(assignment.dueDate).toLocaleString() : "Not set");
}

async function loadSubmissions() {
    const container = document.getElementById("submissionsContainer");
    container.innerHTML = `
        <div class="empty-state">
            <h3>Loading submissions...</h3>
            <p>Please wait.</p>
        </div>
    `;

    const res = await fetch("/folders" +
        "/assignment/" + assignmentId + "/submissions", {
        headers: {
            Authorization: "Bearer " + localStorage.getItem("token")
        }
    });

    if (res.status === 401 || res.status === 403) {
        localStorage.removeItem("token");
        window.location.href = "../pages/login.html";
        return;
    }

    if (!res.ok) {
        renderError("Unable to load submissions right now.");
        return;
    }

    const submissions = await res.json();
    document.getElementById("submissionCount").textContent = "Submissions: " + submissions.length;
    renderSubmissions(submissions);
}

function renderSubmissions(submissions) {
    const container = document.getElementById("submissionsContainer");
    if (!submissions || submissions.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <h3>No submissions yet</h3>
                <p>Student submissions will appear here once uploaded.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = `
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Student</th>
                        <th>Submitted At</th>
                        <th>Similarity</th>
                        <th>Matched With</th>
                        <th>File</th>
                    </tr>
                </thead>
                <tbody>
                    ${submissions.map(function (item) {
                        return `
                            <tr>
                                <td>${item.studentUserId || "-"}</td>
                                <td>${item.submittedAt ? new Date(item.submittedAt).toLocaleString() : "-"}</td>
                                <td>${typeof item.similarityScore === "number" ? item.similarityScore + "%" : "-"}</td>
                                <td>${item.matchedStudentID || "-"}</td>
                                <td><a class="action-link" href="${item.fileUrl}" target="_blank">Open File</a></td>
                            </tr>
                        `;
                    }).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function renderError(message) {
    const container = document.getElementById("submissionsContainer");
    container.innerHTML = `
        <div class="empty-state">
            <h3 class="error">${message}</h3>
            <p>Please try again in a moment.</p>
        </div>
    `;
}

function goBack() {
    if (folderId) {
        window.location.href = "../pages/folderfiles.html?id=" + folderId;
        return;
    }

    window.history.back();
}
