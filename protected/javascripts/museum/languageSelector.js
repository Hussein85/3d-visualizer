$( document ).ready(function() {	
$( "#language-sweden" ).click(function() {
		$.ajax({
		    url: jsRoutes.controllers.Application.language().url,
		    data: { code: ""},
		    type: 'POST',
		    success: function(result) {
		        location.reload(false)
		    }
		});
		});
	$( "#language-us" ).click(function() {
		$.ajax({
		    url: jsRoutes.controllers.Application.language().url,
		    data: { code: "en"},
		    type: 'POST',
		    success: function(result) {
		        location.reload(false)
		    }
		});
		});
});