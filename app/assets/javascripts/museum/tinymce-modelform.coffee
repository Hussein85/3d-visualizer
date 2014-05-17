$(document).ready ->
  tinymce.init
    selector: "#text"
    statusbar: true
    language: if museumCookie["languageCode"] == "en" then "en" else "sv_SE"
    height: "300px"