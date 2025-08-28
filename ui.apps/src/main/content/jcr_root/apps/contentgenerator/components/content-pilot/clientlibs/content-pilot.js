document.addEventListener("DOMContentLoaded", function () {
  console.log("[Content Pilot] DOMContentLoaded");
  var form = document.getElementById("contentPilotForm");
  var result = document.querySelector(".result");
  if (form && result) {
    form.addEventListener("submit", function (e) {
      e.preventDefault();
      var formData = new FormData(form);
      fetch("/bin/hello", {
        method: "POST",
        body: formData,
      })
        .then(function (response) {
          return response.text();
        })
        .then(function (text) {
          result.textContent = text;
        })
        .catch(function (err) {
          result.textContent = "Error: " + err;
        });
    });
  }
});
