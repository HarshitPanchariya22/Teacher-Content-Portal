const submittedAssignments = new Set();

function getSubmissionStateKey() {
    const token = localStorage.getItem("token") || "anonymous";
    return "submittedAssignments::" + token;
}

function loadSubmissionState() {
    try {
        const raw = localStorage.getItem(getSubmissionStateKey());
        if (!raw) return;
        const ids = JSON.parse(raw);
        if (Array.isArray(ids)) {
            ids.forEach(id => submittedAssignments.add(Number(id)));
        }
    } catch (error) {
        console.log(error);
    }
}

function persistSubmissionState() {
    try {
        localStorage.setItem(
            getSubmissionStateKey(),
            JSON.stringify(Array.from(submittedAssignments))
        );
    } catch (error) {
        console.log(error);
    }
}

async function loadFolders() {

    const token = localStorage.getItem("token");

    if (!token) {
        location.href = "../pages/login.html";
        return;
    }

    try {
        const res = await fetch("/student/folders", {
            headers: {
                Authorization: "Bearer " + token
            }
        });

        if (res.status === 401 || res.status === 403) {
            localStorage.clear();
            location.href = "login.html";
            return;
        }

        if (!res.ok) {
            return;
        }

        const data = await res.json();

        let html = "";

        if (data.length === 0) {
            html = `<div class="empty">Nothing Present</div>`;
        } else {
            data.forEach(folder => {
                html += `
                    <div class="card">
                        <h3>${folder.name}</h3>
                        <p>${folder.year} Year</p>
                        <p>${folder.branch}</p>

                        <button class="openBtn"
                            onclick="openFolder(${folder.id})">
                            Open Folder
                        </button>
                    </div>
                `;
            });
        }

        document.getElementById("folders").innerHTML = html;
        document.getElementById("assignments").innerHTML = `
            <div class="empty">Open a folder to view assignments</div>
        `;

    } catch (error) {
        console.log(error);
    }
}



async function openFolder(folderId) {
    await Promise.all([
        loadFiles(folderId),
        loadAssignments(folderId)
    ]);
    document.getElementById("assignments").scrollIntoView({ behavior: "smooth", block: "start" });
}

async function loadFiles(folderId) {

    const token = localStorage.getItem("token");

    const res = await fetch("/student/folders/" + folderId, {
        headers: {
            Authorization: "Bearer " + token
        }
    });

    const data = await res.json();
    const cards = document.querySelectorAll("#folders .card");
    cards.forEach(card => card.classList.remove("active"));

    const activeButton = document.querySelector(`button.openBtn[onclick="openFolder(${folderId})"]`);
    if (activeButton) {
        activeButton.closest(".card").classList.add("active");
    }

    let html = "";

    if (data.length === 0) {
        html = `<div class="empty">Nothing Present</div>`;
    } else {
        data.forEach(file => {
            html += `
                <div class="file">
                    <a href="${file.fileUrl}" target="_blank">
                        ${file.title}
                    </a>
                </div>
            `;
        });
    }

    document.getElementById("files").innerHTML = html;
}

async function loadAssignments(folderId) {
    const token = localStorage.getItem("token");
    const assignmentsNode = document.getElementById("assignments");

    assignmentsNode.innerHTML = `
        <div class="empty">
            Loading assignments...
        </div>
    `;

    try {
        const res = await fetch("/student/folders/" + folderId + "/assignments", {
            headers: {
                Authorization: "Bearer " + token
            }
        });

        if (res.status === 401 || res.status === 403) {
            localStorage.clear();
            location.href = "../pages/login.html";
            return;
        }

        if (!res.ok) {
            assignmentsNode.innerHTML = `<div class="empty">Unable to load assignments</div>`;
            return;
        }

        const assignments = await res.json();
        renderAssignments(assignments);
    } catch (error) {
        console.log(error);
        assignmentsNode.innerHTML = `<div class="empty">Unable to load assignments</div>`;
    }
}

function renderAssignments(assignments) {
    const assignmentsNode = document.getElementById("assignments");

    if (!assignments || assignments.length === 0) {
        assignmentsNode.innerHTML = `<div class="empty">No assignments available</div>`;
        return;
    }

    assignmentsNode.innerHTML = assignments.map(function (assignment) {
        const due = assignment.dueDate
            ? new Date(assignment.dueDate).toLocaleString()
            : "No due date";
        const statusText = assignment.open ? "Open" : "Closed";
        const statusClass = assignment.open ? "open" : "closed";
        const alreadySubmitted = Boolean(assignment.submitted) || submittedAssignments.has(Number(assignment.id));
        const warningMessage = alreadySubmitted
            ? "You have already submitted this assignment."
            : "";

        return `
            <div class="assignment-card">
                <div class="assignment-head">
                    <h3>${assignment.title}</h3>
                    <span class="assignment-status ${statusClass}">${statusText}</span>
                </div>
                <p>${assignment.description}</p>
                <div class="assignment-meta">Due: ${due}</div>
                <div id="warning-${assignment.id}" class="submission-warning ${alreadySubmitted ? "" : "hidden"}">
                    ${warningMessage}
                </div>
                <div class="submission-row">
                    <input type="file" id="submissionFile-${assignment.id}" ${assignment.open && !alreadySubmitted ? "" : "disabled"}>
                    <button class="submitBtn" onclick="submitAssignment(${assignment.id})" ${assignment.open && !alreadySubmitted ? "" : "disabled"}>
                        Submit
                    </button>
                </div>
            </div>
        `;
    }).join("");
}

async function submitAssignment(assignmentId) {
    const token = localStorage.getItem("token");
    if (submittedAssignments.has(Number(assignmentId))) {
        showAssignmentWarning(assignmentId, "You already submitted this assignment.");
        return;
    }

    const fileInput = document.getElementById("submissionFile-" + assignmentId);
    const file = fileInput ? fileInput.files[0] : null;

    if (!file) {
        alert("Please choose a file before submitting.");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
        const res = await fetch("/student/assignment/" + assignmentId + "/submit", {
            method: "POST",
            headers: {
                Authorization: "Bearer " + token
            },
            body: formData
        });

        if (res.status === 401 || res.status === 403) {
            localStorage.clear();
            location.href = "../pages/login.html";
            return;
        }

        if (res.status === 409) {
            submittedAssignments.add(Number(assignmentId));
            persistSubmissionState();
            showAssignmentWarning(assignmentId, "You have already submitted this assignment.");
            if (fileInput) {
                fileInput.value = "";
                fileInput.disabled = true;
            }
            const duplicateBtn = document.querySelector(`button.submitBtn[onclick="submitAssignment(${assignmentId})"]`);
            if (duplicateBtn) {
                duplicateBtn.disabled = true;
            }
            return;
        }

        if (res.ok) {
            let responseData = null;
            try {
                responseData = await res.json();
            } catch (ignored) {
                responseData = null;
            }

            const warning = responseData && responseData.warning ? responseData.warning : null;
            if (warning && warning.toLowerCase().includes("already submitted")) {
                submittedAssignments.add(Number(assignmentId));
                persistSubmissionState();
                showAssignmentWarning(assignmentId, warning);
                if (fileInput) {
                    fileInput.value = "";
                    fileInput.disabled = true;
                }
                const duplicateBtn = document.querySelector(`button.submitBtn[onclick="submitAssignment(${assignmentId})"]`);
                if (duplicateBtn) {
                    duplicateBtn.disabled = true;
                }
                return;
            }

            alert("Assignment submitted successfully.");
            submittedAssignments.add(Number(assignmentId));
            persistSubmissionState();
            showAssignmentWarning(assignmentId, "Submission recorded. Re-submission is not allowed.");
            fileInput.value = "";
            fileInput.disabled = true;
            const submitBtn = document.querySelector(`button.submitBtn[onclick="submitAssignment(${assignmentId})"]`);
            if (submitBtn) {
                submitBtn.disabled = true;
            }
            return;
        }

        let message = "Submission failed";
        try {
            const errorData = await res.json();
            if (errorData && errorData.message) {
                message = errorData.message;
            }
        } catch (e) {
            try {
                const responseText = await res.text();
                if (responseText) {
                    message = responseText;
                }
            } catch (ignored) {
                // Keep default fallback message.
            }
        }

        const duplicateMessage = String(message).toLowerCase();
        if (duplicateMessage.includes("already submitted") || duplicateMessage.includes("duplicate submission")) {
            submittedAssignments.add(Number(assignmentId));
            persistSubmissionState();
            showAssignmentWarning(assignmentId, "You have already submitted this assignment.");
            if (fileInput) {
                fileInput.disabled = true;
            }
            const submitBtn = document.querySelector(`button.submitBtn[onclick="submitAssignment(${assignmentId})"]`);
            if (submitBtn) {
                submitBtn.disabled = true;
            }
            return;
        }

        alert(message);
    } catch (error) {
        console.log(error);
        alert("Submission failed");
    }
}

function showAssignmentWarning(assignmentId, message) {
    const warning = document.getElementById("warning-" + assignmentId);
    if (warning) {
        warning.textContent = message;
        warning.classList.remove("hidden");
    }
}



function logout(){
    localStorage.clear();
    location.href = "../pages/login.html";
}

loadSubmissionState();
loadFolders();