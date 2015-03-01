var myApp = angular.module('ModelApp', [ 'ngResource', 'ngRoute', 'pascalprecht.translate' ]);

myApp.config([ '$routeProvider', '$translateProvider', function($routeProvider, $translateProvider) {
	$routeProvider.when('/', {
		templateUrl : '/assets/partials/model/addModel.html',
		reloadOnSearch : false
	}).otherwise({
		redirectTo : '/model/add'
	});

	$translateProvider.useStaticFilesLoader({
		prefix : '/assets/javascripts/museum/i18n/locale-',
		suffix : '.json'
	});
	$translateProvider.preferredLanguage('sv');
} ]);

myApp.factory('Model', [ '$resource', function($resource) {
	return $resource('/model', null);
} ]);

myApp.controller('ModelAddController', [ '$scope', '$resource', '$http', '$translate',
		'Model', function($scope, $resource, $http, $translate, Model) {
			_this = this;
			$translate('FOO').then(function (paragraph) {
			    $scope.FOO = paragraph;
			  });
			_this.translated = 
			_this.init = function(){
			museum.initTagsInput();
			museum.initTinyMCE();
			}
		} ]);

