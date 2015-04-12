var myApp = angular.module('BrowserApp', [ 'angular.filter' ,'ngResource', 'ngRoute',
    'ngTagsInput', 'ui.bootstrap', 'pascalprecht.translate']);

myApp.config([ '$routeProvider', '$translateProvider',
    function($routeProvider, $translateProvider) {
      $routeProvider.when('/', {
        templateUrl : '/assets/partials/browser/browser.html',
        reloadOnSearch : false
      }).otherwise({
        redirectTo : '/browser'
      });

      $translateProvider.useStaticFilesLoader({
        prefix : '/assets/javascripts/museum/i18n/locale-',
        suffix : '.json'
      });
      $translateProvider.preferredLanguage('sv');
    } ]);

myApp.controller('BrowserAppController', [ '$scope', '$resource', '$http',
    '$translate', function($scope, $resource, $http, $translate) {
      _this = this;
      
      // TODO: Similar code as viewer break out some to service

      _this.models = [];
      _this.tags = [];

      _this.init = function() {
        $http.get('/model').then(function(result) {
          _this.models = result.data;
        });
      };
      
      _this.loadTags = function(modelId) {
        $http.get('/tags/model/' + modelId).then(function(result) {
          _this.tags[modelId] = result.data;
        });
      };

    }

]);
