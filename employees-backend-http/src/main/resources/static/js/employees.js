window.onload = function () {
    const url = '/api/employees';

    // const createEmployeeForm = document.querySelector("#create-employee-form");
    // createEmployeeForm.addEventListener('submit', e => e.preventDefault());

    const printMessage = (message) => {
        const messageDiv = document.querySelector("#message-div");
        messageDiv.innerHTML = `<p>${message}</p>`;
    }

    const updateTable = () => {
        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Error calling server: ${response.status}`);
                }
                return response.json();
            })
            .then(jsonData => {
                const employeesTable = document.querySelector("#employees-table");
                employeesTable.innerHTML = "";
                jsonData.forEach(item => {
                    employeesTable.innerHTML +=
                        `<tr><td>${item.id}</td><td>${item.name}</td></tr>`;
                });
            })
            .catch(reason => {
                printMessage(`Error: ${reason.message}`);
            })
        ;
    }
    updateTable();

    const createButton = document.querySelector("#create-button");
    createButton.onclick = () => {
        const nameInput = document.querySelector("#name-input");
        const name = nameInput.value;

        fetch(url, {
            method: "post",
            body: `{"name": "${name}"}`,
            headers: {
                "Content-Type": "application/json"
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Error calling server: ${response.status}`);
                }
                return response.json();
            })
            .then(jsonData => {
                printMessage("Created")
                nameInput.value = "";
                updateTable();
            }).catch(reason => {
                printMessage(`Error: ${reason.message}`);
            });
    };
}