// Create a constructor
function Viewer(id, modelPath, texturePath) {
	this.scene;
	this.id = id;
	this.camera;
	this.modelPath = modelPath;
	this.texturePath = texturePath;
	this.panY = false;
}

Viewer.prototype.tooglePanY = function() {
	this.panY = !this.panY;
}

Viewer.prototype.initCanvas = function() {
	var that = this;

	if (!Detector.webgl)
		Detector.addGetWebGLMessage();

	var controls, renderer;

	var cross;
	var size = {
		width : $(that.id).width(),
		height : 400
	};

	init();
	animate();

	function init() {

		that.camera = new THREE.PerspectiveCamera(45, size.width / size.height,
				1, 1000);

		controls = new THREE.TrackballControls(that.camera);

		controls.rotateSpeed = 1.0;
		controls.zoomSpeed = 1.2;
		controls.panSpeed = 0.8;

		controls.noZoom = false;
		controls.noPan = false;

		controls.staticMoving = true;
		controls.dynamicDampingFactor = 0.3;

		controls.keys = [ 65, 83, 68 ];

		controls.addEventListener('change', render);

		// world
		that.scene = new THREE.Scene();

		// You can set the color of the ambient light to any value.
		// I have chose a completely white light because I want to paint
		// all the shading into my texture. You propably want something darker.
		var ambient = new THREE.AmbientLight(0xffffff);
		// lights
		that.scene.add(ambient);

		/** * Texture Loading ** */
		var manager = new THREE.LoadingManager();
		manager.onProgress = function(item, loaded, total) {
			console.log(item, loaded, total);
		};
		var texture = new THREE.Texture();
		var loader = new THREE.ImageLoader(manager);

		// You can set the texture properties in this function.
		// The string has to be the path to your texture file.
		loader.load(that.texturePath, function(image) {
			texture.image = image;
			texture.needsUpdate = true;
			// I wanted a nearest neighbour filtering for my low-poly character,
			// so that every pixel is crips and sharp. You can delete this lines
			// if have a larger texture and want a smooth linear filter.
			texture.magFilter = THREE.NearestFilter;
			texture.minFilter = THREE.NearestMipMapLinearFilter;
		});

		/** * OBJ Loading ** */
		var loader = new THREE.OBJLoader(manager);

		// As soon as the OBJ has been loaded this function looks for a mesh
		// inside the data and applies the texture to it.
		loader.load(that.modelPath, function(event) {
			var object = event;
			object.name = that.modelPath;
			object.traverse(function(child) {
				if (child instanceof THREE.Mesh) {
					child.material.map = texture;
					// child.scale.set(100, 100, 100);
					child.geometry.computeBoundingBox();
					child.position.set(0, 0, 0);
					that.scene.position.setY(-child.geometry.boundingBox.max.y / 2);
					that.scene.updateMatrix();
					var bb = new THREE.BoundingBoxHelper(child, 0xffff00);
					bb.update();
					that.scene.add(bb);
					that.camera.position.set(0, 0, 300);
				}
			});

			that.scene.add(object);
		});

		// Grid and Axis
		var axis = new THREE.AxisHelper(100);
		var grid = new THREE.GridHelper(100, 10);
		that.scene.add(axis);
		that.scene.add(grid);
		// renderer

		renderer = new THREE.WebGLRenderer({
			antialias : false
		});
		renderer.setSize(size.width, size.height);

		$(that.id).append(renderer.domElement);

		window.addEventListener('resize', onWindowResize, false);
	}

	function onWindowResize() {

		that.camera.aspect = size.width / size.height;
		that.camera.updateProjectionMatrix();

		renderer.setSize(size.width, size.height);

		controls.handleResize();

		render();

	}

	function animate() {

		requestAnimationFrame(animate);
		controls.update();

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

		renderer.render(that.scene, that.camera);
	}
}