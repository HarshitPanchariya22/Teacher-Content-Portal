javascript

async function uploadCsv() {

    const fileInput =
        document.getElementById("csvFile");

    const file =
        fileInput.files[0];

    if (!file) {
        alert("Please select a CSV file");

        return;
    }

    const formData = new FormData();

    formData.append("file", file);

    try {

        const response = await fetch(
            "/admin/users/import-teachers",
            {
                method: "POST",

                headers: {

                    Authorization:
                        "Bearer " +
                        localStorage.getItem("token")

                },

                body: formData
            }
        );

        const result =
            await response.json();

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

    const resultBox =
        document.getElementById("resultBox");

    let html = `
<div class="result-card">

    <h3>
    Successfully Added:
    ${result.successCount}
</h3>
`;

    if (result.errors.length > 0) {

        html += `
<div class="error-section">

    <h4>Errors</h4>

<ul>
    `;

        result.errors.forEach(error => {

            html += `
                <li>${error}</li>
            `;

        });

        html += `
</ul>

</div>
`;
    }

    html += `
</div>
    `;

    resultBox.innerHTML = html;
}
