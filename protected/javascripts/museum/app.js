var app = angular.module('App', [ 'angular.filter', 'ngResource', 'ngRoute',
    'ngTagsInput', 'ui.bootstrap', 'pascalprecht.translate', 'ngFileUpload' ]);

app.config([ '$routeProvider', '$translateProvider',
    function($routeProvider, $translateProvider) {
      $routeProvider.when('/model/add', {
        templateUrl : '/securedassets/partials/model/addModel.html',
        reloadOnSearch : false
      }).when('/model/:modelId', {
        templateUrl : '/securedassets/partials/viewer/viewer.html',
        reloadOnSearch : false
      }).when('/browser', {
        templateUrl : '/securedassets/partials/browser/browser.html',
        reloadOnSearch : false
      }).when('/admin/', {
        redirectTo : '/admin/activateUser'
      }).when('/admin/:page', {
        templateUrl : '/securedassets/partials/admin/admin.html',
        reloadOnSearch : false
      }).otherwise({
        redirectTo : '/model/1'
      });

      $translateProvider.useStaticFilesLoader({
        prefix : '/securedassets/javascripts/museum/i18n/locale-',
        suffix : '.json'
      });
      $translateProvider.preferredLanguage('sv');
    } ]);

app.controller('ViewerController', [
    '$scope',
    '$resource',
    '$http',
    '$translate',
    '$routeParams',
    function($scope, $resource, $http, $translate, $routeParams) {
      _this = this;

      _this.model = {};
      _this.tags = [];

      _this.init = function() {
        $http.get('/model/' + $routeParams.modelId).then(
            function(result) {
              _this.model = result.data;

              $http.get('/tags/model/' + _this.model.id).then(function(result) {
                _this.tags = result.data;
              });

              var viewer = new Viewer("#canvas-place-holder", "/uploads/"
                  + _this.model.f1, "/uploads/" + _this.model.f2);
              viewer.initCanvas();

              $("#reset-view").click(function() {
                viewer.resetView();
              });

              $("#toogle-bounding-box").click(function() {
                viewer.toogleBoundingBox();
              });

              $("#toogle-pan-Y").click(function() {
                viewer.tooglePanY();
              });

              tinyMCE.remove();

              var tiny = tinymce.init({
                selector : "#text",
                language : museumCookie["languageCode"] === "en" ? "en"
                    : "sv_SE",
                height : "300px",
                entity_encoding : "raw",
                menubar : false,
                statusbar : false,
                toolbar : false,
                readonly : true,
                setup : function(editor) {
                  editor.on('init', function() {
                    $(".mce-panel").css({
                      'border-width' : '0px'
                    });
                    tinymce.editors[0].setContent($('<textarea />').html(
                        _this.model.text).text().replace(/\n/ig, "<br>"));
                  });
                }

              });
              console.log(tiny);
            });

      };

    }

]);

app.controller('AdminController', [ '$scope', '$resource', '$http',
    '$translate', '$routeParams',
    function($scope, $resource, $http, $translate, $routeParams) {
      _this = this;

      _this.init = function() {
        _this.page = $routeParams.page;
      };

    }

]);

app.controller('BrowserAppController', [ '$scope', '$resource', '$http',
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

app.controller('ModelAddController', [
    '$scope',
    '$resource',
    '$http',
    '$translate',
    'Upload',
    function($scope, $resource, $http, $translate, Upload) {
      _this = this;

      _this.model = {};
      _this.alerts = [];

      _this.init = function() {
        tinyMCE.remove();
        tinymce.init({
          selector : "#text",
          statusbar : true,
          force_p_newlines : false,
          force_br_newlines : true,
          convert_newlines_to_brs : false,
          remove_linebreaks : true,
          language : museumCookie["languageCode"] === "en" ? "en" : "sv_SE",
          height : "300px",
          entity_encoding : "raw"
        });
      }
      _this.loadTags = function($query) {
        return $http.get('/tags?query=' + $query);
      };

      _this.submit = function() {
        if (_this.model.tags.length === 0) {
          delete _this.model.tags;
        }

        _this.model.text = tinymce.DOM.encode(tinymce.editors[0].getContent()
            .replace(new RegExp('\r?\n', 'g'), ''));

        $http.post('/model', _this.model).success(
            function(data, status, headers, config) {
              _this.uploadFile(_this.objectFile, data.urlObject);
              _this.uploadFile(_this.textureFile, data.urlTexture);
              _this.uploadFile(_this.thumbnailFile, data.urlThumbnail);
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

      _this.uploadFile = function(file, url) {
          $http.put(url, file.pop().slice()).success(
              function(data, status, headers, config) {
                console.log("data" + data, "status" + status, "headers"
                    + headers, "config" + config);
              }).error(function(data, status, headers, config) {
                console.log("data" + data, "status" + status, "headers"
                    + headers, "config" + config);
          });
        
        
      }
      
      /*_this.addFile = function(inputId, uploadUrl) {
        var f = document.getElementById(inputId).files[0], 
        ar = new FileReader();
        r.onloadend = function(e) {
          var data = e.target.result;
          r.readAsArrayBuffer(f);
          console.log(data);
          $http.put(uploadUrl, data).success(
              function(data, status, headers, config) {
                console.log("data" + data, "status" + status, "headers"
                    + headers, "config" + config);
              }).error(function(data, status, headers, config) {
                console.log("data" + data, "status" + status, "headers"
                    + headers, "config" + config);
          });
        }                
      }*/

      _this.addAlert = function(alert) {
        _this.alerts.push(alert);
      };

      _this.closeAlert = function(index) {
        _this.alerts.splice(index, 1);
      };

    } ]);

app.factory('User', [ '$resource', function($resource) {
  return $resource('/user/:email', null, {
    'update' : {
      method : 'PUT'
    }
  });
} ]);

app.controller('MenuController', [ '$scope', '$location',
    function($scope, $location) {
      $scope.isActive = function(viewLocation) {
        return viewLocation === $location.path();
      };
    } ]);

app.controller('ActivateUserController', [ '$scope', '$resource', '$http',
    'User', function($scope, $resource, $http, User) {
      $http.get('/user').success(function(data) {
        $scope.users = data;
      });
      $http.get('/role').success(function(data) {
        $scope.roles = data;
      });
      $scope.selectUser = function(user) {
        $scope.selectedUser = user;
        $http.get('/organization').success(function(data) {
          $scope.organizations = data;
        });
      }
      $scope.selectOrganization = function(organization) {
        $scope.selectedOrganization = organization;
      }
      $scope.selectRole = function(role) {
        $scope.selectedRole = role;
      }
      $scope.updateUser = function() {
        var user = new User($scope.selectedUser.email);
        User.update({
          email : $scope.selectedUser.email
        }, {
          organizationId : $scope.selectedOrganization.id,
          role : $scope.selectedRole.name
        });
      }
    } ]);

app.controller('AdminOrganizationController', [ '$scope', '$http',
    function($scope, $http) {
      $scope.submitForm = function() {
        var data = angular.toJson($scope.formData);
        $http.post('/organization', data).success(function(response) {
          $scope.response = response;
          alert('ok');
        }).error(function(error) {
          $scope.error = error;
          alert('error');
        });
      }
    } ]);
