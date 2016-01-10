var app = angular.module('App', ['angular.filter', 'ngResource', 'ngRoute',
    'ngTagsInput', 'ui.bootstrap', 'pascalprecht.translate', 'uiGmapgoogle-maps']);

app.config(['$routeProvider', '$translateProvider', 'uiGmapGoogleMapApiProvider',
    function ($routeProvider, $translateProvider, uiGmapGoogleMapApiProvider) {
        $routeProvider.when('/model/add', {
            templateUrl: '/securedassets/partials/model/addModel.html',
            reloadOnSearch: false
        }).when('/model/:modelId', {
            templateUrl: '/securedassets/partials/viewer/viewer.html',
            reloadOnSearch: false
        }).when('/browser', {
            templateUrl: '/securedassets/partials/browser/browser.html',
            reloadOnSearch: false
        }).when('/admin/', {
            redirectTo: '/admin/activateUser'
        }).when('/admin/:page', {
            templateUrl: '/securedassets/partials/admin/admin.html',
            reloadOnSearch: false
        }).when('/map', {
            templateUrl: '/securedassets/partials/map/map.html',
            reloadOnSearch: false
        }).otherwise({
            redirectTo: '/browser'
        });

        $translateProvider.useStaticFilesLoader({
            prefix: '/securedassets/javascripts/museum/i18n/locale-',
            suffix: '.json'
        });
        $translateProvider.preferredLanguage('sv');

        uiGmapGoogleMapApiProvider.configure({
            v: '3.17',
            libraries: 'weather,geometry,visualization'
        });

    }]);


app.factory('modelFactory', ['$http', function ($http) {

    var modelFactory = {};

    // Read models from databas and returning a model array
    modelFactory.init = function () {

        var models = {};

        var assignModelAndGetFiles = function (model) {
            models[model.id] = model;
            $http.get('/file/model/' + model.id).then(function (result) {
                var thumbnailPredicate = function (file) {
                    return file.type === 'thumbnail' && file.finished;
                };

                models[model.id].thumbnail = result.data.filter(thumbnailPredicate)[0].getUrl;
            });
        };

        $http.get('/model').then(function (result) {
            result.data.forEach(assignModelAndGetFiles);

        });

    };

    // Read tags from databas and return a tag array
    modelFactory.loadTags = function (modelId) {

        var tags = {};

        $http.get('/tags/model/' + modelId).then(function (result) {
            tags[modelId] = result.data;
        });

        return tags;
    };

    return modelFactory;

}]);


app.controller('ViewerController', [
    '$scope',
    '$resource',
    '$http',
    '$translate',
    '$routeParams',
    function ($scope, $resource, $http, $translate, $routeParams) {
        _this = this;

        _this.model = {};
        _this.tags = [];

        _this.init = function () {
            $http.get('/model/' + $routeParams.modelId).then(
                function (result) {
                    _this.model = result.data;


                    $http.get('/file/model/' + _this.model.id).then(function (result) {
                        var objectPredicate = function (file) {
                            return file.type === 'webObject' && file.finished;
                        };

                        var texturePredicate = function (file) {
                            return file.type === 'texture' && file.finished;
                        };

                        _this.model.object = result.data.filter(objectPredicate)[0].getUrl;

                        if (result.data.filter(texturePredicate).length > 0) {
                            _this.model.texture = result.data.filter(texturePredicate)[0].getUrl;
                        }

                        var viewer = new Viewer("#canvas-place-holder",
                            _this.model.object, _this.model.texture);

                        viewer.initCanvas();

                        $("#reset-view").click(function () {
                            viewer.resetView();
                        });

                        $("#toogle-bounding-box").click(function () {
                            viewer.toogleBoundingBox();
                        });

                        $("#toogle-pan-Y").click(function () {
                            viewer.tooglePanY();
                        });

                        $("#toogle-texture").click(function () {
                            viewer.toogleTexture();
                        });

                        $("#toogle-fullscreen").click(function () {
                            viewer.fullscreen();
                        });


                    });

                    $http.get('/tags/model/' + _this.model.id).then(function (result) {
                        _this.tags = result.data;
                    });

                    // Text box
                    tinyMCE.remove();

                    var tiny = tinymce.init({
                        selector: "#text",
                        language: museumCookie["languageCode"] === "en" ? "en"
                            : "sv_SE",
                        height: "300px",
                        entity_encoding: "raw",
                        menubar: false,
                        statusbar: false,
                        toolbar: false,
                        readonly: true,
                        setup: function (editor) {
                            editor.on('init', function () {
                                $(".mce-panel").css({
                                    'border-width': '0px'
                                });
                                tinymce.editors[0].setContent($('<textarea />').html(
                                    _this.model.text).text().replace(/\n/ig, "<br>"));
                            });
                        }

                    });
                });

        };

    }

]);


app.controller('AdminController', ['$scope', '$resource', '$http',
    '$translate', '$routeParams',
    function ($scope, $resource, $http, $translate, $routeParams) {
        _this = this;

        _this.init = function () {
            _this.page = $routeParams.page;
        };

    }

]);


app.controller('BrowserAppController', ['$scope', '$resource', '$http',
    '$translate', function ($scope, $resource, $http, $translate) {

        _this = this;

        _this.models = {};
        _this.tags = {};

        // Load models
        _this.init = function () {

            var assignModelAndGetFiles = function (model) {
                _this.models[model.id] = model;
                $http.get('/file/model/' + model.id).then(function (result) {
                    var thumbnailPredicate = function (file) {
                        return file.type === 'thumbnail' && file.finished;
                    };

                    _this.models[model.id].thumbnail = result.data.filter(thumbnailPredicate)[0].getUrl;
                });
            };

            $http.get('/model').then(function (result) {
                result.data.forEach(assignModelAndGetFiles);
            });
        };


        // Load tags from modelId
        _this.loadTags = function (modelId) {
            //_this.tags = modelFactory.loadTags(modelId);
            $http.get('/tags/model/' + modelId).then(function (result) {
                _this.tags[modelId] = result.data;
            });

        };

    }

]);


app.controller('ModalMapCtrl', ['$scope', '$modal', function ($scope, $modal) {

    $scope.coordinates = {};        // coordinates are saved here when choosing a location on modal window

    $scope.openModal = function ($event) {
        // preventing the button from submitting the form
        $event.preventDefault();

        var modalInstance = $modal.open({
            templateUrl: '/securedassets/partials/model/map_dtl.html',
            controller: 'mapSelectLocationCtrl'
        });

        // When save-location button are pressed and closed
        modalInstance.result.then(function (coords) {

            // send coords to parent controller (i.e ModelAddController)
            $scope.$emit('SendCoords', coords);
        });
    }

}]);


app.controller("mapSelectLocationCtrl", function ($scope, $modalInstance, uiGmapGoogleMapApi, $timeout) {

    $scope.map = {
        center: {latitude: 62, longitude: 15},
        zoom: 4,
        markers: [],
        events: {
            click: function (map, eventName, originalEventArgs) {
                var e = originalEventArgs[0];
                var lat = e.latLng.lat(), lon = e.latLng.lng();
                var marker = {
                    id: Date.now(),
                    coords: {
                        latitude: lat,
                        longitude: lon
                    }
                };
                $scope.map.markers.pop();                  //for showing only one marker when clicking
                $scope.map.markers.push(marker);

                $scope.$apply();
            }
        }

    };

    // When Map is loaded render it on html modal view. Otherwise map will be gray on the modal window
    uiGmapGoogleMapApi.then(function (maps) {
        $timeout(function () {
            $scope.showMap = true;
        }, 100);
    });

    var coordinates = {};

    // When clicking on save-location button
    $scope.ok = function () {

        if ($scope.map.markers[0]) {
            coordinates.latitude = $scope.map.markers[0].coords.latitude;
            coordinates.longitude = $scope.map.markers[0].coords.longitude;
            $modalInstance.close(coordinates);

        } else {
            alert("Choose a location.")
            return false;           //preventing alert from submitting when the dialog is closed
        }
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
});


app.controller('ModelAddController', [
    '$scope',
    '$resource',
    '$http',
    '$location',
    function ($scope, $resource, $http, $location) {
        _this = this;

        _this.model = {};
        _this.alerts = [];
        _this.filesUploading = 0;

        //listen to events from child controller. (get coordinates from ModalMapCtrl)
        $scope.$on('SendCoords', function (event, coords) {
            _this.model.latitude = coords.latitude;
            _this.model.longitude = coords.longitude;

            GetAddress(coords.latitude, coords.longitude, function (address) {
                _this.model.location = address;
            });

        });

        // Get address from longitude and latitude
        var GetAddress = function (lat, lng, callback) {
            var address;
            var latlng = new google.maps.LatLng(lat, lng);
            var geocoder = geocoder = new google.maps.Geocoder();
            geocoder.geocode({'latLng': latlng}, function (results, status) {
                if (status == google.maps.GeocoderStatus.OK) {
                    address = results[1].formatted_address;
                    callback(address);   // geocoder is a asynchronous function. Returns value when ready
                } else {
                    console.log("Address was not successful for the following reason: " + status);
                }

            });
        }

        _this.init = function () {

            tinyMCE.remove();

            tinymce.init({
                selector: "#text",
                statusbar: true,
                force_p_newlines: false,
                force_br_newlines: true,
                convert_newlines_to_brs: false,
                remove_linebreaks: true,
                language: museumCookie["languageCode"] === "en" ? "en" : "sv_SE",
                height: "300px",
                entity_encoding: "raw",
                setup: function (editor) {
                    editor.on('init', function () {
                        tinymce.editors[0].setContent($('<textarea />').html(
                            '').text());
                    });
                }
            });

            $("#object-file").on('change', function () {
                $scope.$apply();
            });

            $("#texture-file").on('change', function () {
                $scope.$apply();
            });

        };

        _this.uploading = function () {
            return _this.filesUploading > 0;
        }

        _this.loadTags = function ($query) {
            return $http.get('/tags?query=' + $query);
        };

        _this.valid = function (valid) {
            if (valid && document.getElementById('object-file').files.length > 0) {
                return true;
            } else {
                return false;
            }
        }

        _this.submit = function () {
            if (_this.model.tags.length === 0) {
                delete _this.model.tags;
            }

            _this.model.text = tinymce.DOM.encode(tinymce.editors[0].getContent()
                .replace(new RegExp('\r?\n', 'g'), ''));

            $http.post('/model', _this.model).success(
                function (model, status, headers, config) {
                    _this.uploadFile(document.getElementById('object-file').files[0], model.id, 'object');
                    if (document.getElementById('texture-file').files.length > 0) {
                        _this.uploadFile(document.getElementById('texture-file').files[0], model.id, 'texture');
                    }
                }).error(function (data, status, headers, config) {
                    _this.addAlert({
                        msg: data,
                        type: 'danger'
                    });
                });
            ;
        }

        _this.uploadFile = function (file, modelId, type) {

            _this.filesUploading++;

            var sendToS3 = function (data) {
                $http.put(data.putUrl, file.slice())
                    .success(function () {
                        accFile(data);
                    })
                    .error(function (data, status, headers, config) {
                        console.log("data" + data, "status" + status, "headers"
                            + headers, "config" + config);
                    });
            };

            var accFile = function (data) {
                $http.put('/file/acc/' + data.id, {})
                    .success(function () {
                        var markCompleteAndCheckIfShouldRedirect = function () {
                            _this.filesUploading--;
                            if (!_this.uploading()) {

                                $location.url('/model/' + data.modelId);
                            }
                        }
                        setTimeout(markCompleteAndCheckIfShouldRedirect(), 100);
                    })
                    .error(function (data, status, headers, config) {
                        console.log("data" + data, "status" + status, "headers"
                            + headers, "config" + config);
                    });
            };

            var filePost = {
                modelId: modelId,
                type: type
            };


            $http.post("/file", filePost).success(
                function (model, status, headers, config) {
                    sendToS3(model);
                }).error(function (data, status, headers, config) {
                    console.log("data" + data, "status" + status, "headers"
                        + headers, "config" + config);
                });
        }

        _this.addAlert = function (alert) {
            _this.alerts.push(alert);
        };

        _this.closeAlert = function (index) {
            _this.alerts.splice(index, 1);
        };


    }]);


app.controller('ModelPublishController', [
    '$scope',
    '$resource',
    '$http',
    '$location',
    function ($scope, $resource, $http, $location) {
        var _this = this;

        _this.model = {};
        _this.models = [];
        _this.alerts = [];
        _this.filesUploading = 0;

        $http.get('/model/unpublished').success(function (data) {
            $scope.models = data;
        });

        $("#web-object-file").on('change', function () {
            $scope.$apply();
        });

        $("#thumbnail-file").on('change', function () {
            $scope.$apply();
        });

        _this.selectModel = function (model) {
            _this.model = model;
        }

        _this.uploading = function () {
            return _this.filesUploading > 0;
        }

        _this.valid = function (valid) {
            if (valid &&
                document.getElementById('web-object-file').files.length > 0 &&
                document.getElementById('thumbnail-file').files.length > 0
            ) {
                return true;
            } else {
                return false;
            }
        }

        _this.submit = function () {
            _this.uploadFile(document.getElementById('web-object-file').files[0], _this.model.id, 'webObject');
            _this.uploadFile(document.getElementById('thumbnail-file').files[0], _this.model.id, 'thumbnail');
        }

        _this.uploadFile = function (file, modelId, type) {

            _this.filesUploading++;

            var sendToS3 = function (data) {
                $http.put(data.putUrl, file.slice())
                    .success(function () {
                        acc(data);
                    })
                    .error(function (data, status, headers, config) {
                        console.log("data" + data, "status" + status, "headers"
                            + headers, "config" + config);
                    });
            };

            var acc = function (data) {
                $http.put('/file/acc/' + data.id, {})
                    .success(function () {
                        var publishIfFilesUploaded = function () {
                            _this.filesUploading--;
                            if (!_this.uploading()) {
                                $http.put('/model/' + modelId + '/published', {published: true})
                                _this.addAlert({
                                    msg: "File with id: " + data.id + " uploaded",
                                    type: 'success'
                                });
                            }
                        }
                        setTimeout(publishIfFilesUploaded(), 100);
                    })
                    .error(function (data, status, headers, config) {
                        _this.addAlert({
                            msg: data,
                            type: 'danger'
                        });
                        console.log("data" + data, "status" + status, "headers"
                            + headers, "config" + config);
                    });
            };

            var filePost = {
                modelId: modelId,
                type: type
            };


            $http.post("/file", filePost).success(
                function (model, status, headers, config) {
                    sendToS3(model);
                }).error(function (data, status, headers, config) {
                    console.log("data" + data, "status" + status, "headers"
                        + headers, "config" + config);
                });
        }

        _this.addAlert = function (alert) {
            _this.alerts.push(alert);
        };

        _this.closeAlert = function (index) {
            _this.alerts.splice(index, 1);
        };

    }]);


app.factory('User', ['$resource', function ($resource) {
    return $resource('/user/:email', null, {
        'update': {
            method: 'PUT'
        }
    });
}]);


app.controller('MenuController', ['$scope', '$location',
    function ($scope, $location) {
        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    }]);


app.controller('ActivateUserController', ['$scope', '$resource', '$http',
    'User', function ($scope, $resource, $http, User) {
        $http.get('/user').success(function (data) {
            $scope.users = data;
        });
        $http.get('/role').success(function (data) {
            $scope.roles = data;
        });
        $scope.selectUser = function (user) {
            $scope.selectedUser = user;
            $http.get('/organization').success(function (data) {
                $scope.organizations = data;
            });
        }
        $scope.selectOrganization = function (organization) {
            $scope.selectedOrganization = organization;
        }
        $scope.selectRole = function (role) {
            $scope.selectedRole = role;
        }
        $scope.updateUser = function () {
            var user = new User($scope.selectedUser.email);
            User.update({
                email: $scope.selectedUser.email
            }, {
                organizationId: $scope.selectedOrganization.id,
                role: $scope.selectedRole.name
            });
        }
    }]);


app.controller('AdminOrganizationController', ['$scope', '$http',
    function ($scope, $http) {
        $scope.submitForm = function () {
            var data = angular.toJson($scope.formData);
            $http.post('/organization', data).success(function (response) {
                $scope.response = response;
                alert('ok');
            }).error(function (error) {
                $scope.error = error;
                alert('error');
            });
        }
    }]);


app.controller('MapCtrl', ['$scope', '$http', '$q', function ($scope, $http, $q) {
    var _this = this;

    $scope.markers = [];

    // Initialize map and load models from database
    var models = {};
    var map = {};
    init();

    // The bounding box for markers
    var bounds = new google.maps.LatLngBounds();

    // Open infowindow when clicking in the list
    var lastinfowindow = new google.maps.InfoWindow();
    $(document).on("click", ".loc", function () {
        var thisloc = $(this).data("locid");
        for (var i = 0; i < $scope.markers.length; i++) {
            if ($scope.markers[i].locid == thisloc) {
                if (lastinfowindow instanceof google.maps.InfoWindow) lastinfowindow.close();
                map.panTo($scope.markers[i].getPosition());
                openInfoWindow($scope.markers[i].infowindow, $scope.markers[i]);
                lastinfowindow = $scope.markers[i].infowindow;
            }
        }
    });

    // Create markers and extending bounding box
    function createMarkers(models) {

        for (var key in models) {
            // Create the marker and add to array
            var marker = createMarker(models[key]);

            // extending the bounding box
            bounds.extend(marker.position);

            // zoom on the bounding box
            map.fitBounds(bounds);
            map.setZoom(13);
        }
    }

    function reloadThumbnailUrl(model){
        return $http.get('/file/model/' + model.id).then(function (result) {
            var thumbnailpredicate = function (file) {
                return file.type === 'thumbnail' && file.finished;
            };

            models[model.id].thumbnail = result.data.filter(thumbnailpredicate)[0].getUrl;
        });
    }

    function openInfoWindow(infoWindow, marker) {
        reloadThumbnailUrl(marker.model).then(function(){
            infoWindow.setContent(_this.markerContent(marker.model));
            infoWindow.open(map, marker);
        });
    }

    // Create marker and add listeners etc
    function createMarker(model) {

        // create a marker
        var marker = new google.maps.Marker({
            map: map,
            position: new google.maps.LatLng(model.latitude, model.longitude),
            title: model.name
        });

        marker.locid = model.id;
        marker.loc = model.location;
        marker.model = model;

        // create marker content.
        _this.markerContent = function(model) {
            return '<h4 class="media-heading" style="padding-left: 40px;padding-bottom: 10px"><em>' +model.name + '</em></h4>' +
            '<a href="#/model/' + model.id + '">' +
            '<img class="media-object" src="' + model.thumbnail + '"+ width="128px" height="128px">' +
            '</a>';
        };

        // Create info window
        var infoWindow = new google.maps.InfoWindow({
            content: _this.markerContent(marker)
        });
        marker.infowindow = infoWindow;

        // Add listeners
        google.maps.event.addListener(marker, 'click', function () {
            openInfoWindow(infoWindow, marker);
        });

        // Display info window when the mouse is over the marker
        google.maps.event.addListener(marker, 'mouseover', function () {
            openInfoWindow(infoWindow, marker);
        });

        // Exit info window when the mouse is out of the marker
        google.maps.event.addListener(marker, 'mouseout', function () {
            infoWindow.close();
        });

        // Go to modelviewer when clicking on marker.
        google.maps.event.addListener(marker, 'click', function () {
            window.location.href = "#/model/" + model.id;

        });



        $scope.markers.push(marker);

        return marker;
    }

    // Initialize map and load models from database
    function init() {

        //  Config the map
        var mapOptions = {
            zoom: 9,
            center: new google.maps.LatLng(55.6, 13.0),
            mapTypeId: google.maps.MapTypeId.TERRAIN
        }

        // Show the map on page
        map = new google.maps.Map(document.getElementById('map'), mapOptions);

        $(window).resize(function () {
              $('#map').css('height', $('#map-row').height());
        }).resize();

        // load the models
        getModels();
    };

    function getModels() {

        var promises = [];

        // load the models
        var assignModelAndGetFiles = function (model) {
            models[model.id] = model;
            promises.push($http.get('/file/model/' + model.id).then(function (result) {
                var thumbnailPredicate = function (file) {
                    return file.type === 'thumbnail' && file.finished;
                };

                models[model.id].thumbnail = result.data.filter(thumbnailPredicate)[0].getUrl;
            }));

        };

        $http.get('/model').then(function (result) {
            result.data.forEach(assignModelAndGetFiles);
            $q.all(promises).then(createMarkers(models));
        });
    }

}]);
