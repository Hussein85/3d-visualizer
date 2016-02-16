// Create a constructor
function Viewer(id, modelPath, texturePath) {
    this.renderer;
    this.scene;
    this.id = id;
    this.camera;
    this.modelPath = modelPath;
    this.texturePath = texturePath;
    this.object;
    this.controls;
    this.ambientLight;
    this.dirLight;
    this.texture;
    this.bb;
    this.savedCam;
    this.hotspots;
    this.hotspot_number;
}

Viewer.prototype.getHotspots = function () {
    return this.hotspots;
};

Viewer.prototype.initHotspots = function () {
    this.hotspots = [];
    var el = document.getElementById('hotspots');
    while ( el.firstChild ) el.removeChild( el.firstChild );
    this.hotspot_number = 1;
};

Viewer.prototype.fullscreen = function () {

    $("#toogle-fullscreen").off();
    $("#toogle-fullscreen").tooltip('hide');

    var fullscreenDiv = document.getElementById("toogle-fullscreen");

    //fullscreenDiv.setAttribute("data-html", "true");
    //fullscreenDiv.setAttribute("data-original-title", "{{ 'VIEWER.VIEWER.CONTROLLS.EXITFULLSCREEN'| translate }} ");
    fullscreenDiv.setAttribute("data-original-title", "Avsluta helskärmsläge");

    fullscreenDiv.innerHTML="<div class='glyphicon glyphicon-resize-small white' ></div>";
    fullscreenDiv.setAttribute("id", "exit-fullscreen");

    $("#exit-fullscreen").mouseover(function() {
        $("#exit-fullscreen").tooltip('show');
    });

    $("#exit-fullscreen").mouseleave(function() {
        $("#exit-fullscreen").tooltip('hide');
    });

    var el = document.getElementById('drawLine');
    while ( el.firstChild ) el.removeChild( el.firstChild );
    clicks = 0;
    distance = 0;
    document.getElementById("distance-infowindow").innerHTML="<div class=dist-infowindow-text >" + "Avstånd: " + 0 + " cm" + "</div>";

    var i = document.getElementById('canvas-place-holder');

    // go fullscreen - support most browsers
    if (i.requestFullscreen) {
        i.requestFullscreen();
    } else if (i.webkitRequestFullscreen) {
        i.webkitRequestFullscreen();
    } else if (i.mozRequestFullScreen) {
        i.mozRequestFullScreen();
    } else if (i.msRequestFullscreen) {
        i.msRequestFullscreen();
    }
}

Viewer.prototype.exitFullscreen = function () {

    if (document.exitFullscreen) {
        document.exitFullscreen();
    } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
    } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
    } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
    }

    var el = document.getElementById('drawLine');
    while ( el.firstChild ) el.removeChild( el.firstChild );
    clicks = 0;
    distance = 0;
    document.getElementById("distance-infowindow").innerHTML="<div class=dist-infowindow-text >" + "Avstånd: " + 0 + " cm" + "</div>";

    $("#toogle-fullscreen").off();
    $("#toogle-fullscreen").tooltip('hide');

    // Dynimacally modify div element
    var fullscreenDiv = document.getElementById("exit-fullscreen");

    //fullscreenDiv.setAttribute("data-html", "true");
    //fullscreenDiv.setAttribute("data-original-title", "{{ 'VIEWER.VIEWER.CONTROLLS.TOGGLE_FULLSCREEN'| translate }} ");
    fullscreenDiv.setAttribute("data-original-title", "Helskärmsläge");

    fullscreenDiv.innerHTML="<div class='glyphicon glyphicon-resize-full white' ></div>";
    fullscreenDiv.setAttribute("id", "toogle-fullscreen");

    $("#toogle-fullscreen").mouseover(function() {
        $("#toogle-fullscreen").tooltip('show');
    });

    $("#toogle-fullscreen").mouseleave(function() {
        $("#toogle-fullscreen").tooltip('hide');
    });
}

Viewer.prototype.resetView = function () {
    this.camera.position.set(this.savedCam.position.x, this.savedCam.position.y, this.savedCam.position.z);
    this.camera.rotation.set(this.savedCam.rotation.x, this.savedCam.rotation.y, this.savedCam.rotation.z);
    this.controls.center.set(this.savedCam.controlCenter.x, this.savedCam.controlCenter.y, this.savedCam.controlCenter.z);
    this.controls.update();
}

Viewer.prototype.lightUpdate = function (that) {
    if (that.dirLight !== undefined && that.dirLight !== null
        && that.camera.position !== undefined && that.camera.position !== null) {
        that.dirLight.position.copy(that.camera.position);
    }
}

Viewer.prototype.initCanvas = function () {
    var that = this;

    var textureReady = false;
    that.savedCam = {};

    that.initHotspots();

    if (!Detector.webgl)
        Detector.addGetWebGLMessage();

    var size = {
        width: $(that.id).width(),
        height: 400
    };

    // Create background scene
    var bgScene = new THREE.Scene();
    var bgCamera = new THREE.Camera();

    var mouse, raycaster;

    init();
    animate();

    function init() {

        clicks = 0;
        lastClick = [0, 0];
        distance = 0;
        last3dPoint = new THREE.Vector3();

        $('#dist-button').click(function(){
             if($(this).toggleClass('active').hasClass('active')){
                // When distance-button is active.
                document.getElementById("canvas-place-holder").addEventListener('click', drawLine, false);
                document.getElementById("distance-infowindow").innerHTML="<div class=dist-infowindow-text >" + "Avstånd: " + distance + " cm" + "</div>";
                $("#distance-infowindow").fadeIn(500);
                document.getElementById("distance-resetButton").innerHTML="<div class=distance-resetButton-text >" + "Nollställ" + "</div>";
                $("#distance-resetButton").fadeIn(500);
                $("#distance-resetButton").click(function(){
                    var el = document.getElementById('drawLine');
                    while ( el.firstChild ) el.removeChild( el.firstChild );
                    clicks = 0;
                    distance = 0;
                    document.getElementById("distance-infowindow").innerHTML="<div class=dist-infowindow-text >" + "Avstånd: " + 0 + " cm" + "</div>";
                });
             }else{
                // When distance-button is not active
                document.getElementById("canvas-place-holder").removeEventListener('click', drawLine, false);
                $("#distance-infowindow").fadeOut();
                $("#distance-resetButton").fadeOut();
                var el = document.getElementById('drawLine');
                while ( el.firstChild ) el.removeChild( el.firstChild );
                clicks = 0;
                distance = 0;
             }
         });

        // The scene
        that.scene = new THREE.Scene();

        // The camera
        that.camera = new THREE.PerspectiveCamera(45, size.width / size.height,
            0.1, 2000);

        /** * Texture Loading ** */
        var manager = new THREE.LoadingManager();
        manager.onLoad = function () {
            $("#circularLoader").fadeOut("slow");
            $("#canvas-place-holder").fadeIn("slow");
            $("#controls").fadeIn(500);
        };

        // Circular loader
        var ctx = document.getElementById('circularLoader').getContext('2d');
        var percentComplete = 0;
        var start = 4.72;
        var cw = ctx.canvas.width;
        var ch = ctx.canvas.height;
        var diff;

        // Tooltip for camera controls
        $(document).ready(function () {
            $('[data-toggle="tooltip-camera"]').tooltip();
        });

        $("#controls").hide();
        $("#distance-infowindow").hide();
        $("#distance-resetButton").hide();

        var onProgress = function (xhr) {
            if (xhr.lengthComputable) {
                percentComplete = Math.round(xhr.loaded / xhr.total * 100, 2);
                diff = ((percentComplete / 100) * Math.PI * 2 * 10).toFixed(2);
                ctx.clearRect(0, 0, cw, ch);
                ctx.lineWidth = 5;
                ctx.font = "20px Arial";
                ctx.fillStyle = '#09F';      // color of the text
                ctx.strokeStyle = "#09F";    // color of the circular loader
                ctx.textAlign = 'center';
                ctx.fillText(percentComplete + '%', cw * 0.5, ch * 0.5 + 5, cw);
                ctx.beginPath();
                ctx.arc(35, 35, 30, start, diff / 10 + start, false);
                ctx.stroke();
            }
        };

        var onError = function (xhr) {};

        that.texture = undefined;
        var loader = new THREE.ImageLoader(manager);
        loader.crossOrigin = '';

        if (that.texturePath !== undefined) {
            that.ambientLight = new THREE.AmbientLight(0xffffff);
            that.dirLight = new THREE.DirectionalLight(0xeeeeee, 0.1);
            that.texture = new THREE.Texture();
            // You can set the texture properties in this function.
            // The string has to be the path to your texture file.
            loader.load(that.texturePath, function (image) {
                that.texture.image = image;
                that.texture.needsUpdate = true;
                // I wanted a nearest neighbour filtering for my low-poly character,
                // so that every pixel is crips and sharp. You can delete this lines
                // if have a larger texture and want a smooth linear filter.
                that.texture.magFilter = THREE.NearestFilter;
                that.texture.minFilter = THREE.NearestMipMapLinearFilter;
                textureReady = true;
            });
        } else {
            that.ambientLight = new THREE.AmbientLight(0x555555);
            that.dirLight = new THREE.DirectionalLight(0xffffff, 0.5);
        }

        /** * OBJ Loading ** */
        var loader = new THREE.OBJLoader(manager);

        // As soon as the OBJ has been loaded this function looks for a mesh
        // inside the data and applies the texture to it.
        loader.load(that.modelPath, function (event) {
            var object = event;
            object.traverse(function (child) {
                if (child instanceof THREE.Mesh) {
                    that.object = child;
                    that.object.name = that.modelPath;
                    that.object.castShadow = true;
                    child.material.color.setHex(0xffffff);
                    if (that.texture !== undefined) {
                        child.material.map = that.texture;
                        that.textureOn = true;
                    }
                    child.geometry.computeBoundingBox();
                    child.position.set(0, 0, 0);
                    that.bb = new THREE.BoundingBoxHelper(child, 0xffff00);
                    that.bb.update();
                }
            });
            that.scene.add(object);
            fitCameraToObject();

        }, onProgress, onError);

        // Load background texture
        var texture = THREE.ImageUtils.loadTexture("/securedassets/images/background/img.jpg");
        var bgMesh = new THREE.Mesh(
            new THREE.PlaneGeometry(2, 2, 0),
            new THREE.MeshBasicMaterial({
                map: texture
        }));

        // The background shouldn't care about the z-buffer.
        bgMesh.material.depthTest = false;
        bgMesh.material.depthWrite = false;

        bgScene.add(bgCamera);
        bgScene.add(bgMesh);

        // renderer
        that.renderer = new THREE.WebGLRenderer({antialias: true});
        that.renderer.setSize(size.width, size.height);
        $(that.id).append(that.renderer.domElement);

        that.dirLight.castShadow = true;

        // lights
        that.scene.add(that.ambientLight);
        that.scene.add(that.dirLight);

        // controls
        that.controls = new THREE.OrbitControls(that.camera, that.renderer.domElement);

        //add raycaster and mouse
        raycaster = new THREE.Raycaster();
        mouse = new THREE.Vector2();

        // Add Event listeners
        that.controls.addEventListener('change', function () {
            that.lightUpdate(that);
            updateHotspotPositions();
        });
        that.controls.addEventListener('change', render);
        window.addEventListener('resize', onWindowResize, false);
        document.getElementById("canvas-place-holder").addEventListener("dblclick", addHotspots);
    }

    function drawLine(evnet) {
        var canvas = document.getElementById("canvas-place-holder");
        var pos = getMousePos(canvas, event);

        mouse.x = ( pos.x / that.renderer.domElement.width ) * 2 - 1;
        mouse.y = -( pos.y / that.renderer.domElement.height ) * 2 + 1;

        raycaster.setFromCamera(mouse, that.camera);
        var intersect = raycaster.intersectObject(that.object);

        if (intersect.length > 0) {
            var point = document.createElement("div");
            $('#drawLine').append(point);

            point.setAttribute("class", "point");
            point.setAttribute("style", "top:" + (pos.y-5) + "px; left: " + (pos.x-5) + "px" );

            if (clicks == 0) {
                clicks++;
            } else {
                // compute distance between two points in 3d space (distance are in mm)
                var point1 = last3dPoint;
                var point2 = intersect[0].point;
                distance += Math.round(point1.distanceTo(point2)/10);
                document.getElementById("distance-infowindow").innerHTML="<div class=dist-infowindow-text >" + "Avstånd: " + distance + " cm" + "</div>";

                // draw line
                var x1 = lastClick[0];
                var y1 = lastClick[1];
                var x2 = pos.x;
                var y2 = pos.y;

                var length = Math.sqrt(((x2-x1) * (x2-x1)) + ((y2-y1) * (y2-y1)));
                var thickness = 3;
                var color = "#0F0";
                var cx = ((x1 + x2) / 2) - (length / 2);
                var cy = ((y1 + y2) / 2) - (thickness / 2);

                var angle = Math.atan2((y1-y2),(x1-x2))*(180/Math.PI);

                var htmlLine = "<div style='padding:0px; margin:0px; height:" + thickness + "px; background-color:" + color +
                               "; line-height:1px; position:absolute; left:" + cx + "px; top:" + cy + "px; width:" + length +
                               "px; -moz-transform:rotate(" + angle + "deg); -webkit-transform:rotate(" + angle +
                               "deg); -o-transform:rotate(" + angle + "deg); -ms-transform:rotate(" + angle +
                               "deg); transform:rotate(" + angle + "deg);' />";

                document.getElementById("drawLine").innerHTML+=htmlLine;
            }
            lastClick = [pos.x, pos.y];
            last3dPoint = intersect[0].point;
        }
    };

    function addHotspots(event) {
        var canvas = document.getElementById("canvas-place-holder");
        var pos = getMousePos(canvas, event);

        //event.stopPropagation();
        //event.preventDefault();
        //event.stopImmediatePropagation();

        mouse.x = ( pos.x / that.renderer.domElement.width ) * 2 - 1;
        mouse.y = -( pos.y / that.renderer.domElement.height ) * 2 + 1;

        raycaster.setFromCamera(mouse, that.camera);
        var intersect = raycaster.intersectObject(that.object);

        if (intersect.length > 0) {

            // dynamically create hotspot div and set attributes
            var hotspot_div = document.createElement("div");
            $('#hotspots').append(hotspot_div);

            var tooltip_htmlContent =
            "<div> <br><input type='text' id='hotspot_input_titel' name='titel' placeholder='Titel'><br>" +
            "<textarea spellcheck='false' id='hotspot_input_description' placeholder='Beskrivning'></textarea><br><br>" +
            "<input type='button' value='Avbryt' id='hotspot_cancel'>" +
            "  <input type='button' value='Ok' id='hotspot_ok'> </div> ";

            var hotspotId = "hotspot" + that.hotspot_number;
            hotspot_div.setAttribute("id", hotspotId);
            hotspot_div.setAttribute("data-html", "true");
            hotspot_div.setAttribute("data-original-title", tooltip_htmlContent);
            hotspot_div.setAttribute("class", "hotspot-button");
            hotspot_div.setAttribute("style", "top:" + (pos.y-10) + "px; left: " + (pos.x-10) + "px" );
            hotspot_div.setAttribute("data-toogle", "tooltip-hotspot");
            hotspot_div.setAttribute("data-placement", "right");
            hotspot_div.innerHTML="<div class=hotspot-text >" + that.hotspot_number + "</div>";

            $("#" + hotspotId).tooltip({trigger: 'manual'}).tooltip('show');

            document.getElementById("hotspot_input_titel").focus();
            document.removeEventListener('dblclick', addHotspots, false);

            $("#hotspot_cancel").click(function(){
                // Remove div
                $('#' + hotspotId).tooltip('destroy')
                document.getElementById(hotspotId).remove();
                document.addEventListener('dblclick', addHotspots, false);
            });

            $("#hotspot_ok").click(function(){
                var hotspot = {};
                var titel = document.getElementById("hotspot_input_titel").value;
                var descr = document.getElementById("hotspot_input_description").value;

                var tooltip_htmlContent ="<div> <strong>" + titel + "</strong><br>" + descr + "</div>";
                hotspot_div.setAttribute("data-original-title", tooltip_htmlContent);

                // replace tooltip content
                $("#" + hotspotId).attr('title', tooltip_htmlContent).tooltip('fixTitle').tooltip('hide');

                //hotspot.divElement = hotspot_div;
                hotspot.number = that.hotspot_number;
                hotspot.titel = titel;
                hotspot.description = descr;
                hotspot.hotspotId = hotspotId;
                hotspot.position = intersect[0].point;

                hotspot.camPosition = that.camera.position.clone();
                hotspot.camRotation = that.camera.rotation.clone();

                that.hotspots.push(hotspot);

                $("#" + hotspotId).click(function(){
                    $('#' + hotspotId).tooltip('hide')

                    // Set camera rotation around hotspot.
                    that.controls.target.set(hotspot.position.x,hotspot.position.y,hotspot.position.z);
                    that.camera.position.set(hotspot.camPosition.x, hotspot.camPosition.y, hotspot.camPosition.z);
                    that.camera.rotation.set(hotspot.camRotation.x, hotspot.camRotation.y, hotspot.camRotation.z);
                });


                $("#" + hotspotId).mouseover(function() {
                    $("#" + hotspotId).tooltip('show');
                });

                $("#" + hotspotId).mouseleave(function() {
                    $("#" + hotspotId).tooltip('hide');
                });

                that.hotspot_number++;

                document.addEventListener('dblclick', addHotspots, false);

                $('button').prop('disabled', false);

            });
        }
    }

    function getMousePos(canvas, event) {
        var rect = canvas.getBoundingClientRect();
        return {
            x: event.clientX - rect.left,
            y: event.clientY - rect.top
        };
    }

    function onWindowResize() {
        // Fullscreen. Supports most browsers.
        if (document.webkitIsFullScreen || document.msFullscreenElement || document.mozFullScreen || document.fullscreen) {
            that.camera.aspect = window.innerWidth / window.innerHeight;
            that.camera.updateProjectionMatrix();
            that.renderer.setSize(window.innerWidth, window.innerHeight);
        } else {    // exit fullscreen
            that.camera.aspect = size.width / size.height;
            that.camera.updateProjectionMatrix();
            that.renderer.setSize(size.width, size.height);
            $("#controls").fadeIn(500);

        }
        updateHotspotPositions();
        render();
    }

    function animate() {
        requestAnimationFrame(animate);
        that.controls.update();
        render();
    }

    function render() {
        that.renderer.autoClear = false;
        that.renderer.clear();
        that.renderer.render(bgScene, bgCamera);

        that.renderer.render(that.scene, that.camera);

        updateHotspotPositions();
    }

    function fitCameraToObject() {
        // Set camera rotation around the masscenter of the boundingBox.
        that.controls.target.set(that.bb.box.center().x,that.bb.box.center().y,that.bb.box.center().z);

        var bbsize = that.bb.box.size();

        var height = Math.max(bbsize.x, Math.max(bbsize.y, bbsize.z));
        var width = bbsize.x;
        var depth = bbsize.z;

        var vertical_FOV = that.camera.fov * (Math.PI/ 180);
        var horizontal_FOV = 2 * Math.atan (Math.tan (vertical_FOV/2) * size.width / size.height);

        var distance_vertical = height / (2 * Math.tan(vertical_FOV/2));
        var distance_horizontal = width / (2 * Math.tan(horizontal_FOV/2));
        var z_distance = distance_vertical >= distance_horizontal? distance_vertical : distance_horizontal;

        that.camera.position.z = z_distance + depth;
        that.camera.position.y = 0;
        that.camera.position.x = 0;

        // Save camera settings when restoring camera position
        that.savedCam.position = that.camera.position.clone();
        that.savedCam.rotation = that.camera.rotation.clone();
        that.savedCam.controlCenter = that.controls.center.clone();
    }

    function get2dProjection(coord, camera, width, height) {
        var p = new THREE.Vector3(coord.x, coord.y, coord.z);
        var vector = p.project(camera);

        vector.x = (vector.x + 1) / 2 * width-10;
        vector.y = -(vector.y - 1) / 2 * height-10;

        return vector;
    }

    function updateHotspotPositions() {
        for (var i = 0; i < that.hotspots.length; i++) {
            hotspot = that.hotspots[i];
            if (!isObjectEmpty(hotspot.position)){
                proj = {};

                // If fullscreen
                if (document.webkitIsFullScreen || document.msFullscreenElement || document.mozFullScreen || document.fullscreen) {
                    proj = get2dProjection(hotspot.position, that.camera, window.innerWidth, window.innerHeight);
                } else {
                    proj = get2dProjection(hotspot.position, that.camera, size.width, size.height);

                    if (proj.x < 0)
                        proj.x = 0;
                    if (proj.x > size.width)
                        proj.x = size.width;
                    if (proj.y < 0)
                        proj.y = 0;
                    if (proj.y > size.height)
                        proj.y = size.height;
                }

                var hotspotId = hotspot.hotspotId;

                $("#" + hotspotId).css({top: proj.y, left: proj.x})

            }
        }
    }

    function isObjectEmpty(object){
        var isEmpty = true;
        for(keys in object)
        {
           isEmpty = false;
           break;
        }
        return isEmpty;
    }

}
