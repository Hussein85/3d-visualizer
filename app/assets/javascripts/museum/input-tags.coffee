museum.initTagsInput = ->
  tagId = "#model-add-tags-group"
  tags = $.get jsRoutes.controllers.Model.allTags().url
  $(tagId).tagsinput typeahead:
    source: -> tags
       
  $(tagId).tagsinput('input').parent().width("100%")
  $(tagId).tagsinput('input').width("100%")