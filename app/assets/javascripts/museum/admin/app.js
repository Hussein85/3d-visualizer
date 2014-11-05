var myApp = angular.module('AdminApp', ['ngResource', 'ngRoute']);

  myApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
    when('/', {
      templateUrl: '/assets/partials/admin/activateUser.html',
      controller: 'ActivateUserController',
      reloadOnSearch: false
    }).
      when('/activateUser', {
        templateUrl: '/assets/partials/admin/activateUser.html',
        controller: 'ActivateUserController',
        reloadOnSearch: false
      }).
      when('/addOrganization', {
        templateUrl: '/assets/partials/admin/addOrganization.html',
        controller: 'ActivateUserController',
        reloadOnSearch: false
      }).
      otherwise({
        redirectTo: '/admin'
      });
  }]);

  myApp.factory('User', ['$resource', function($resource) {
    return $resource('/user/:email', null,
        {
            'update': { method:'PUT' }
        });
    }]);
  
  myApp.controller('MenuController', ['$scope', '$location' , function($scope, $location){
    $scope.isActive = function (viewLocation) { 
      return viewLocation === $location.path();
    };
  } ]);

  myApp.controller('ActivateUserController', [ '$scope', '$resource', '$http', 'User' , function($scope, $resource, $http, User) {
      $http.get('/user').success(function(data) {
        $scope.users = data;
       });
      $http.get('/role').success(function(data) {
        $scope.roles = data;
       });
      $scope.selectUser = function(user){
        $scope.selectedUser = user;
        $http.get('/organization').success(function(data) {
          $scope.organizations = data;
         });
      }
      $scope.selectOrganization = function(organization){
        $scope.selectedOrganization = organization;
      }
      $scope.selectRole = function(role){
        $scope.selectedRole = role;
      }
      
      $scope.updateUser = function(){
        var user = new User($scope.selectedUser.email);
        User.update({email:$scope.selectedUser.email}, {organizationId: $scope.selectedOrganization.id, role: $scope.selectedRole.name});
      }
  } ]);