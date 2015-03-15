museum.initTinyMCE = ->
  tinymce.init
    selector: "#text"
    statusbar: true
    force_p_newlines : false
    force_br_newlines : true
    convert_newlines_to_brs : false
    remove_linebreaks : true,   
    language: if museumCookie["languageCode"] == "en" then "en" else "sv_SE"
    height: "300px"
    entity_encoding : "raw"