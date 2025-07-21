document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("importForm").addEventListener("submit", function () {
        document.getElementById("importProgressBar").style.display = "block";
    });
});

function downloadSampleCsv() {
    window.location.href = '/assets/stock/files/sample-product-stock.csv';
}

function triggerFileInput() {
    document.getElementById('csvFileInput').click();
}
