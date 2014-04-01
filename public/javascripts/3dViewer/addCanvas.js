// Create a constructor
function Viewer() {
	
	this.scene;
}


Viewer.prototype.initCanvas = function(id, modelPath, texturePath) {
	if (!Detector.webgl)
		Detector.addGetWebGLMessage();

	var camera, controls, renderer;


	var cross;
	var size = {
		width : $(id).width(),
		height : 400
	};

	init();
	animate();

	function init() {

		camera = new THREE.PerspectiveCamera(45, size.width / size.height, 1,
				1000);
		// The zoom
		camera.position.z = 300;

		controls = new THREE.TrackballControls(camera);

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
		scene = new THREE.Scene();

		// You can set the color of the ambient light to any value.
		// I have chose a completely white light because I want to paint
		// all the shading into my texture. You propably want something darker.
		var ambient = new THREE.AmbientLight(0xffffff);
		// lights
		scene.add(ambient);

		/** * Texture Loading ** */
		var manager = new THREE.LoadingManager();
		manager.onProgress = function(item, loaded, total) {
			console.log(item, loaded, total);
		};
		var texture = new THREE.Texture();
		var loader = new THREE.ImageLoader(manager);

		// You can set the texture properties in this function.
		// The string has to be the path to your texture file.
		loader.load(texturePath, function(image) {
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
		loader.load(modelPath, function(event) {
			var object = event;
			object.name = modelPath;
			object.traverse(function(child) {
				if (child instanceof THREE.Mesh) {
					child.material.map = texture;
					// child.scale.set(100, 100, 100);
					child.geometry.computeBoundingBox();
					child.position.y = 0;
					child.position.x = 0;
					child.position.z = 0;
					var bb = new THREE.BoundingBoxHelper(child, 0xffff00);
					bb.update();
					scene.add(bb);
				}
			});

			scene.add(object);
		});

		// Grid and Axis
		var axis = new THREE.AxisHelper(100);
		var grid = new THREE.GridHelper(100, 10);
		scene.add(axis);
		scene.add(grid);
		// renderer

		renderer = new THREE.WebGLRenderer({
			antialias : false
		});
		renderer.setSize(size.width, size.height);

		$(id).append(renderer.domElement);

		window.addEventListener('resize', onWindowResize, false);
	}

	function onWindowResize() {

		camera.aspect = size.width / size.height;
		camera.updateProjectionMatrix();

		renderer.setSize(size.width, size.height);

		controls.handleResize();

		render();

	}

	function animate() {

		requestAnimationFrame(animate);
		controls.update();
		// camera.position.y = -scene.get...geometry.boundingBox.min.y * 100;
		render();
	}

	function render() {

		renderer.render(scene, camera);
	}
}