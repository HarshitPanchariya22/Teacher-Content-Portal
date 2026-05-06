document.addEventListener("DOMContentLoaded", init);

function init() {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "../pages/login.html";
        return;
    }
}

function logout() {
    localStorage.removeItem("token");
    window.location.href = "../pages/login.html";
}

function downloadTemplate() {
    const csvContent = "userId,password\nteacher1,pass123\nteacher2,pass456\n";
    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = "teacher_template.csv";
    a.click();

    URL.revokeObjectURL(url);
}

async function uploadCsv() {
    const fileInput = document.getElementById("csvFile");
    const file = fileInput.files[0];

    if (!file) {
        alert("Please select a CSV file");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
        const response = await fetch("/admin/users/import-teachers", {
            method: "POST",
            headers: {
                Authorization: "Bearer " + localStorage.getItem("token")
            },
            body: formData
        });

        const result = await response.json();

        if (!response.ok) {
            alert("CSV upload failed");
            return;
        }

        renderResult(result);
    } catch (error) {
        console.error(error);
        alert("Something went wrong");
    }
}

function renderResult(result) {
    const resultBox = document.getElementById("resultBox");

    let html = `
        <div class="result-card">
            <div class="result-header">
                <h3>Import Completed</h3>
                <div class="result-badge">${result.successCount} Added</div>
            </div>
<!--            <p class="success-note">Passwords were hashed before saving.</p>-->
    `;

    if (result.errors && result.errors.length > 0) {
        html += `
            <div class="error-section">
                <h4>Errors</h4>
                <ul>
        `;

        result.errors.forEach(error => {
            html += `<li>${error}</li>`;
        });

        html += `
                </ul>
            </div>
        `;
    } else {
        html += `
            <p class="success-note">No errors found.</p>
        `;
    }

    html += `
        </div>
    `;

    resultBox.innerHTML = html;
}