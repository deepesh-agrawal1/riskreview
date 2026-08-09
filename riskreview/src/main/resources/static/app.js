const analyzeButton = document.getElementById("analyzeButton");
const prUrlInput = document.getElementById("prUrl");

const result = document.getElementById("result");
const loading = document.getElementById("loading");
const errorMessage = document.getElementById("errorMessage");

const riskScore = document.getElementById("riskScore");
const riskLevel = document.getElementById("riskLevel");

const signalsContainer = document.getElementById("signals");
const recommendationsContainer =
    document.getElementById("recommendations");


analyzeButton.addEventListener("click", analyzePR);


async function analyzePR() {

    const prUrl = prUrlInput.value.trim();

    errorMessage.textContent = "";
    result.classList.add("hidden");

    if (!prUrl) {

        errorMessage.textContent =
            "Please enter a GitHub PR URL.";

        return;
    }

    loading.classList.remove("hidden");
    analyzeButton.disabled = true;

    try {

        const response = await fetch("/api/review/analyze", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                prUrl: prUrl
            })
        });

        const data = await response.json();

        if (!response.ok) {

            errorMessage.textContent =
                data.message || data;

            return;
        }

        displayResult(data);

    } catch (error) {

        errorMessage.textContent =
            "Unable to connect to RiskReview.";

    } finally {

        loading.classList.add("hidden");
        analyzeButton.disabled = false;
    }
}


function displayResult(data) {

    result.classList.remove("hidden");

    riskScore.textContent = data.riskScore;
    riskLevel.textContent = data.riskLevel;

    displaySignals(data.signals);
    displayRecommendations(data.recommendations);
}


function displaySignals(signals) {

    signalsContainer.innerHTML = "";

    if (!signals || signals.length === 0) {

        signalsContainer.innerHTML =
            "<p>No significant risk signals detected.</p>";

        return;
    }

    signals.forEach(signal => {

        const div = document.createElement("div");

        div.className = "signal";

        div.innerHTML = `
            <strong>${signal.type} — ${signal.severity}</strong>
            <div>${signal.reason}</div>
            ${signal.file
                ? `<small>File: ${signal.file}</small>`
                : ""}
        `;

        signalsContainer.appendChild(div);
    });
}


function displayRecommendations(recommendations) {

    recommendationsContainer.innerHTML = "";

    if (!recommendations || recommendations.length === 0) {

        recommendationsContainer.innerHTML =
            "<li>No specific recommendations.</li>";

        return;
    }

    recommendations.forEach(recommendation => {

        const li = document.createElement("li");

        li.textContent = recommendation;

        recommendationsContainer.appendChild(li);
    });
}