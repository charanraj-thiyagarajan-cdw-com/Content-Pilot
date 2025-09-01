$(document).ready(function () {
  // var $coreCompDialogForm = $("#coreCompDialogForm");
  // var $btnSubmit1 = $("#btnSubmit1");
  // var $responseField = $("#dialogResponseField");
  // if ($coreCompDialogForm.length && $btnSubmit1.length) {
  //   $coreCompDialogForm.on("submit", function (e) {
  //     e.preventDefault();
  //     var params = new URLSearchParams();
  //     $(this)
  //       .serializeArray()
  //       .forEach(function (field) {
  //         params.append(field.name, field.value);
  //       });
  //     fetch("/bin/core-comp-dialog?" + params.toString(), {
  //       method: "GET",
  //     })
  //       .then(function (response) {
  //         return response.text();
  //       })
  //       .then(function (text) {
  //         if ($responseField.length) {
  //           $responseField.val(text);
  //         }
  //       })
  //       .catch(function (err) {
  //         if ($responseField.length) {
  //           $responseField.val("Error: " + err);
  //         }
  //       });
  //   });
  // }
  // https://www.scrapingcourse.com/ecommerce/

  var $adkForm = $("#adkForm");
  var $btnSubmit2 = $("#btnSubmit2");
  var $jsonResponse = $("#jsonResponse");
  var $componentTab = $("._coral-Tabs-itemLabel").filter(function () {
    return $(this).text().trim() === "Component";
  });

  if ($adkForm.length && $btnSubmit2.length) {
    $adkForm.on("submit", function (e) {
      e.preventDefault();
      var formData = new FormData(this);
      fetch("/bin/adk", {
        method: "POST",
        body: formData,
      })
        .then(function (response) {
          return response.text();
        })
        .then(function (text) {
          if ($jsonResponse.length) {
            $jsonResponse.val(text);
          }
          if ($componentTab.length) {
            $componentTab.click();
          }
        })
        .catch(function (err) {
          if ($jsonResponse.length) {
            $jsonResponse.val("Error: " + err);
          }
        });
    });
  }

  var $componentCreateForm = $("#componentCreateForm");
  var $btnSubmit3 = $("#btnSubmit3");
  var $createResponseContainer = $("#createResponseContainer");
  if ($componentCreateForm.length && $btnSubmit3.length) {
    $componentCreateForm.on("submit", function (e) {
      e.preventDefault();
      var formData = new FormData(this);
      fetch("/bin/importJson", {
        method: "POST",
        body: formData,
      })
        .then(function (response) {
          return response.text();
        })
        .then(function (text) {
          if ($createResponseContainer.length) {
            $createResponseContainer.empty();
            var url = text.trim();
            var $label = $("<span>", {
              text: "Page and components created successfully at:",
              style: "display:block;margin:8px 0;",
            });
            var $link = $("<a>", {
              href: url,
              text: url,
              target: "_blank",
              style: "color:#0073e6;word-break:break-all;display:block;margin:8px 0;",
            });
            $createResponseContainer.append($label).append($link);
          }
        })
        .catch(function (err) {
          if ($createResponseContainer.length) {
            $createResponseContainer.empty();
            $createResponseContainer.text("Error: " + err);
          }
        });
    });
  }
});
