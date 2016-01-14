// Create a constructor
function Viewer(id, modelPath, texturePath) {
    this.renderer;
    this.scene;
    this.id = id;
    this.camera;
    this.modelPath = modelPath;
    this.texturePath = texturePath;
    this.panY = false;
    this.boundingBoxEnabled = false;
    this.bb;
    this.object;
    this.controls;
    this.ambientLight;
    this.dirLight;
    this.texture;
    this.materialWithTexture;
    this.materialWithOutTexture;
    this.textureOn = false;

}

Viewer.prototype.fullscreen = function() {

    $("#camera-control-noFullscreen").hide();
    $("#camera-control-Fullscreen").fadeIn(500);

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

Viewer.prototype.exitFullscreen = function() {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen();
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen();
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen();
    }
}

Viewer.prototype.resetView = function () {
    this.toogleBoundingBox(false);
    this.tooglePanY(false);
    this.toogleTexture(true);
    this.scene.position.setY(-this.bb.box.max.y / 2);
    this.scene.updateMatrix();
    this.camera.position.set(0, 0, 300);
    this.controls.target.set( 0, 10, 0 );

}

Viewer.prototype.tooglePanY = function (forceState) {
    if (typeof forceState !== "undefined") {
        this.panY = forceState;
    } else {
        this.panY = !this.panY;
    }
}

Viewer.prototype.toogleTexture = function (forceState) {
    if (forceState == false || (this.textureOn && forceState === undefined)) {
        this.object.material.map = this.materialWithOutTexture;
        this.scene.remove(this.ambientLight);
        this.scene.remove(this.dirLight);
        this.ambientLight = new THREE.AmbientLight(0x555555);
        this.dirLight = new THREE.DirectionalLight(0xffffff, 0.5);
        this.dirLight.castShadow = true;
        this.scene.add(this.ambientLight);
        this.scene.add(this.dirLight);
        this.object.material.needsUpdate = true;
        this.textureOn = false;
        this.lightUpdate(this);
    } else if (this.texture !== undefined && this.texture !== null) {
        this.object.material.map = this.materialWithTexture;
        this.scene.remove(this.dirLight);
        this.scene.remove(this.ambientLight);
        this.ambientLight = new THREE.AmbientLight(0xeeeeee);
        this.dirLight = new THREE.DirectionalLight(0xffffff, 0.1);
        this.dirLight.castShadow = true;
        this.scene.add(this.ambientLight);
        this.scene.add(this.dirLight);
        this.object.material.map.needsUpdate = true;
        this.object.material.needsUpdate = true;
        this.textureOn = true;
        this.lightUpdate(this);
    }
}

Viewer.prototype.toogleBoundingBox = function (forceState) {
    if (forceState == false || this.boundingBoxEnabled) {
        this.scene.remove(this.bb);
        this.boundingBoxEnabled = false;
    } else {
        this.scene.add(this.bb);
        this.boundingBoxEnabled = true;
    }
}

Viewer.prototype.lightUpdate = function (that) {
    if (that.dirLight !== undefined && that.dirLight !== null
        && that.camera.position !== undefined && that.camera.position !== null) {
        that.dirLight.position.copy(that.camera.position);
    }
}

Viewer.prototype.initCanvas = function () {
    var that = this;

    if (!Detector.webgl)
        Detector.addGetWebGLMessage();

    var size = {
        width: $(that.id).width(),
        height: 400
    };

    // Create the background scene
    var bgScene = new THREE.Scene();
    var bgCamera = new THREE.Camera();

    init();
    animate();

    function init() {

        // The scene
        that.scene = new THREE.Scene();

        // The camera
        that.camera = new THREE.PerspectiveCamera(45, size.width / size.height,
            0.1, 2000);

        /** * Texture Loading ** */
        var manager = new THREE.LoadingManager();
        manager.onProgress = function (item, loaded, total) {
            //console.log(item, loaded, total);
        };

        // Circular loader
        var ctx = document.getElementById('circularLoader').getContext('2d');
        var percentComplete = 0;
        var start = 4.72;
        var cw = ctx.canvas.width;
        var ch = ctx.canvas.height;
        var diff;

        $(document).ready(function(){
            $('[data-toggle="tooltip"]').tooltip();
        });

        $("#camera-control-noFullscreen").hide();
        $("#camera-control-Fullscreen").hide();

        var onProgress = function (xhr){
            if(xhr.lengthComputable){
                percentComplete = Math.round(xhr.loaded / xhr.total * 100, 2);
                diff = ((percentComplete / 100) * Math.PI*2*10).toFixed(2);
              	ctx.clearRect(0, 0, cw, ch);
              	ctx.lineWidth = 5;
                ctx.font="20px Arial";
              	ctx.fillStyle = '#09F';      // color of the text
              	ctx.strokeStyle = "#09F";    // color of the circular loader
              	ctx.textAlign = 'center';
              	ctx.fillText(percentComplete+'%', cw*.5, ch*.5+5, cw);
              	ctx.beginPath();
              	ctx.arc(35, 35, 30, start, diff/10+start, false);
              	ctx.stroke();
                if(percentComplete >= 100){
          			    $("#circularLoader").fadeOut("slow");
          			    $("#canvas-place-holder").fadeIn("slow");
                    $("#camera-control-noFullscreen").fadeIn(500);
	              }
            }
        };

        var onError = function(xhr){
        };

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
                    that.materialWithOutTexture = child.material.map;
                    if (that.texture !== undefined) {
                        child.material.map = that.texture;
                        that.materialWithTexture = child.material.map;
                        that.textureOn = true;
                    }
                    child.geometry.computeBoundingBox();
                    child.position.set(0, 0, 0);
                    var scaleFactor = 200 / child.geometry.boundingBox.max.y;
                    child.scale.set(scaleFactor, scaleFactor, scaleFactor);
                    that.bb = new THREE.BoundingBoxHelper(child, 0xffff00);
                    that.bb.update();
                    that.resetView();
                }
            });

            that.scene.add(object);
        }, onProgress, onError);

        /*
        // Grid and Axis
        var axis = new THREE.AxisHelper(100);
        var grid = new THREE.GridHelper(100, 10);
        that.scene.add(axis);
        that.scene.add(grid);
        */

        // Load the background texture
         var texture = THREE.ImageUtils.loadTexture( "/securedassets/images/background/img.jpg" );
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
        that.renderer = new THREE.WebGLRenderer({antialias: false});
        that.renderer.setSize(size.width, size.height);
        $(that.id).append(that.renderer.domElement);

        that.dirLight.castShadow = true;

        // lights
        that.scene.add(that.ambientLight);
        that.scene.add(that.dirLight);

        // controls
        that.controls = new THREE.OrbitControls( that.camera, that.renderer.domElement );

        // Event listeners
        that.controls.addEventListener('change', function(){that.lightUpdate(that)});
        that.controls.addEventListener('change', render);
        window.addEventListener('resize', onWindowResize, false);

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
            $("#camera-control-noFullscreen").fadeIn(500);
            $("#camera-control-Fullscreen").hide();
        }

        render();
    }

    function animate() {

        requestAnimationFrame(animate);
        that.controls.update();

        if (that.panY) {
            doPanY();
        }
        render();
    }

    function doPanY() {
        var timer = new Date().getTime() * 0.0005;
        that.scene.position.setY(Math.floor(Math.cos(timer) * 100));
        that.scene.updateMatrix();
    }

    function render() {
        that.renderer.autoClear = false;
        that.renderer.clear();
        that.renderer.render(bgScene, bgCamera);
        that.renderer.render(that.scene, that.camera);
    }
}
