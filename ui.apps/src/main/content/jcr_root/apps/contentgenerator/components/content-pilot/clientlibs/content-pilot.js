$(document).ready(function () {
  var $form = $("#contentPilotForm");
  var $componentCreateForm = $("#componentCreateForm");
  var $responseTextarea = $("#responseTextarea");
  var $responseField = $("#responseField");
  var $componentTab = $("._coral-Tabs-itemLabel").filter(function () {
    return $(this).text().trim() === "Component";
  });

  // First tab form submit
  if ($form.length && $responseTextarea.length) {
    $form.on("submit", function (e) {
      e.preventDefault();
      var formData = new FormData(this);
      fetch("/bin/hello", {
        method: "POST",
        body: formData,
      })
        .then(function (response) {
          return response.text();
        })
        .then(function (text) {
          $responseTextarea.val(text);
          if ($componentTab.length) {
            $componentTab.click();
          }
        })
        .catch(function (err) {
          $responseTextarea.val("Error: " + err);
        });
    });
  }

  // Second tab form submit
  if ($componentCreateForm.length && $responseField.length) {
    $componentCreateForm.on("submit", function (e) {
      e.preventDefault();
      var formData = new FormData(this);
      fetch("/bin/hello", {
        method: "POST",
        body: formData,
      })
        .then(function (response) {
          return response.text();
        })
        .then(function (text) {
          $responseField.val(text);
        })
        .catch(function (err) {
          $responseField.val("Error: " + err);
        });
    });
  }
});
