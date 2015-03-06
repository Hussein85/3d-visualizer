var myApp = angular.module('ModelApp', [ 'ngResource', 'ngRoute',
    'ngTagsInput', 'ui.bootstrap', 'pascalprecht.translate' ]);

myApp.config([ '$routeProvider', '$translateProvider',
    function($routeProvider, $translateProvider) {
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

myApp.controller('ModelAddController', [ '$scope', '$resource', '$http',
    '$translate', 'Model',
    function($scope, $resource, $http, $translate, Model) {
      _this = this;

      _this.model = {};
      _this.alerts = [];

      _this.init = function() {
        museum.initTinyMCE();
      }
      _this.loadTags = function($query) {
        return $http.get('/tags?query=' + $query);
      };

      _this.submit = function() {
        $http.post('/model', _this.model).success(function(data, status, headers, config) {
          _this.addAlert({msg:'Save successfull', type:'success'});
        }).error(function(data, status, headers, config) {
          _this.addAlert({msg:'Save unsuccessfull', type:'danger'});
        });
        ;
      }

      _this.addAlert = function(alert) {
        _this.alerts.push(alert);
      };

      _this.closeAlert = function(index) {
        _this.alerts.splice(index, 1);
      };

    } ]);
