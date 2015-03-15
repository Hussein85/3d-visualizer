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

myApp.controller('ModelAddController', [
    '$scope',
    '$resource',
    '$http',
    '$translate',
    'Model',
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
        if(_this.model.tags.length === 0){
          delete _this.model.tags;
        }
        
        _this.model.text = tinymce.DOM.encode(tinymce.editors[0].getContent().replace(new RegExp('\r?\n','g'), ''));
        
        $http.post('/model', _this.model).success(
            function(data, status, headers, config) {
              _this.addAlert({
                msg : data,
                type : 'success'
              });
            }).error(function(data, status, headers, config) {
          _this.addAlert({
            msg : data,
            type : 'danger'
          });
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
