package prog2_group7_3dObjects;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.AmbientLight;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class World3D extends Application {

	private boolean forward, backward, left, right, vertUp, vertDown,
			multiplier;
	private double mouX, mouY, oldX, oldY, deltaX, deltaY;
	private final double cameraYlimit = 90;
	private final PerspectiveCamera camera = new PerspectiveCamera(false);
	private final PerspectiveCamera subSceneCamera = new PerspectiveCamera(true);
	final Xform cameraXform = new Xform();
	final Xform cameraXform2 = new Xform();
	final Xform cameraXform3 = new Xform();
	final double cameraDistance = 450;
	final Group windowRoot = new Group();
	final Group root = new Group();
	final Group world = new Group();
	final Group placedShapes = new Group();
	final PointLight pointLight = new PointLight();
	final AmbientLight ambientLight = new AmbientLight();
	final Group unplacedShapes = new Group();
	final HBox controls = new HBox(10);
	SubScene subScene;
	boolean subSceneVisible = false;
	DrawMode drawModeSetting = DrawMode.FILL;
	Slider radiusSlider, heightSlider, widthSlider, lengthSlider, volumeSlider;
	ColorPicker colorPicker;
	Scene scene = new Scene(windowRoot, 1024, 768, true,
			SceneAntialiasing.BALANCED);

	@Override
	public void start(Stage primaryStage) {
		buildCoords();

		root.getChildren().addAll(world, ambientLight);
		placedShapes.getChildren().add(ambientLight);

		world.getChildren().add(cameraXform);
		cameraXform.getChildren().add(cameraXform2);
		cameraXform2.getChildren().add(cameraXform3);
		cameraXform3.getChildren().add(subSceneCamera);

		subSceneCamera.setFarClip(100000.0D);
		subSceneCamera.setNearClip(0.1D);
		subSceneCamera.setFieldOfView(50.0);
		cameraXform.ry.setAngle(0);
		cameraXform.rx.setAngle(0);

		SkyBox skyBox = new SkyBox(new Image("http://www.zfight.com/misc/images/textures/envmaps/violentdays_large.jpg"));

		scene.setCamera(camera);
		scene.setCursor(Cursor.CROSSHAIR);

		primaryStage.setTitle("Hello World!");
		primaryStage.setScene(scene);
		primaryStage.setFullScreen(true);
		handleStageDimension(primaryStage);

		subScene = createSubScene(scene);
		placedShapes.getChildren().addAll(skyBox);
		root.getChildren().add(placedShapes);
		root.getChildren().add(unplacedShapes);
		makeSphere();
		root.getChildren().add(pointLight);

		final Pane layeredPane = new Pane() {
			@Override
			protected void layoutChildren() {
				double height = scene.getHeight();

				controls.autosize();
				controls.relocate(0, height - controls.getHeight());
			}
		};
		layeredPane.getChildren().addAll(subScene, controls);
		windowRoot.getChildren().add(layeredPane);

		primaryStage.show();

		handleMouse(subScene);
		handleKeyboard(scene);

		
		AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long l) {

				double translationLength = (multiplier ? 10 : 1) * 2.0;

				if (forward) {
					for (Node shape : placedShapes.getChildren()) {
						shape.setTranslateZ(shape.getTranslateZ()
								- translationLength
								* Math.cos(Math.toRadians(cameraXform.ry.getAngle())));
						shape.setTranslateX(shape.getTranslateX()
								- translationLength
								* Math.sin(Math.toRadians(cameraXform.ry.getAngle())));
					}
				}
				if (backward) {
					for (Node shape : placedShapes.getChildren()) {
						shape.setTranslateZ(shape.getTranslateZ()
								+ translationLength
								* Math.cos(Math.toRadians(cameraXform.ry.getAngle())));
						shape.setTranslateX(shape.getTranslateX()
								+ translationLength
								* Math.sin(Math.toRadians(cameraXform.ry.getAngle())));
					}
				}
				if (left) {
					for (Node shape : placedShapes.getChildren()) {
						shape.setTranslateZ(shape.getTranslateZ()
								+ translationLength
								* Math.cos(Math.toRadians(cameraXform.ry.getAngle() + 90)));
						shape.setTranslateX(shape.getTranslateX()
								+ translationLength
								* Math.sin(Math.toRadians(cameraXform.ry.getAngle() + 90)));
					}
				}
				if (right) {
					for (Node shape : placedShapes.getChildren()) {
						shape.setTranslateZ(shape.getTranslateZ()
								- translationLength
								* Math.cos(Math.toRadians(cameraXform.ry.getAngle() + 90)));
						shape.setTranslateX(shape.getTranslateX()
								- translationLength
								* Math.sin(Math.toRadians(cameraXform.ry.getAngle() + 90)));
					}
				}
				if (vertUp) {
					for (Node shape : placedShapes.getChildren()) {
						shape.setTranslateY(shape.getTranslateY() + translationLength);
					}
				}
				if (vertDown) {
					for (Node shape : placedShapes.getChildren()) {
						shape.setTranslateY(shape.getTranslateY() - translationLength);
					}
				}
			}
		};
		timer.start();
		
		
		loadAPlanet();
	}

	/**
	 * Builds Boxes to represent the coordinates 
	 * Red Box = x-axis
	 * Green Box = y-axis
	 * Blue Box = z-axis
	 */
	private void buildCoords() {
		final Box xAxis = new Box(240.0, 1, 1);
		final Box yAxis = new Box(1, 240.0, 1);
		final Box zAxis = new Box(1, 1, 240.0);
		
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.DARKRED);
		redMaterial.setSpecularColor(Color.RED);
		
		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(Color.DARKGREEN);
		greenMaterial.setSpecularColor(Color.GREEN);
		
		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(Color.DARKBLUE);
		blueMaterial.setSpecularColor(Color.BLUE);
		
		xAxis.setMaterial(redMaterial);
		yAxis.setMaterial(greenMaterial);
		zAxis.setMaterial(blueMaterial);

		placedShapes.getChildren().addAll(xAxis, yAxis, zAxis);
	}

	/**
	 * Changes width and height of subScene according to the Stage
	 * @param stage
	 */
	private void handleStageDimension(Stage stage) {
		stage.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
				subScene.setWidth(number2.doubleValue());
			}
		});

		stage.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
				subScene.setHeight(number2.doubleValue());
			}
		});
	}

	/**
	 * The main() method is ignored in correctly deployed JavaFX application.
	 * main() serves only as fallback in case the application can not be
	 * launched through deployment artifacts, e.g., in IDEs with limited FX
	 * support. NetBeans ignores main().
	 *
	 * @param args - the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Handles Keystrokes in the chosen Scene
	 * @param scene
	 */
	private void handleKeyboard(Scene scene) {
		scene.setOnKeyPressed((KeyEvent t) -> {
			switch (t.getCode()) {
			case W:// forward like fps
				forward = true;
				break;
			case S: // backward
				backward = true;
				break;
			case A: // left
				left = true;
				break;
			case D: // right
				right = true;
				break;
			case SPACE: // can go up and down
				vertUp = true;
				break;
			case CONTROL: // can go up and down
				vertDown = true;
				break;
			case SHIFT: // run
				multiplier = true;
				break;
			case LEFT: //turn left
				cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10);
				break;
			case RIGHT: // turn right
				cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10);
				break;
			case UP: // turn up
				cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10);
				break;
			case DOWN: //turn down  --for what?
				cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10);
				break;
			case DIGIT1:
				makeSphere();
				break;
			case DIGIT2:
				makeRectangle();
				break;
			case DIGIT3:
				makeCylinder();
				break;
			case DIGIT4:
				makePyramid();
				break;

			case NUMPAD8:
				cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10);
				makeCylinder();
				break;
			case NUMPAD6:
				cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10);
				makeRectangle();
				break;
			case ENTER:
				placedShapes.getChildren().addAll(unplacedShapes.getChildren());
				unplacedShapes.getChildren().clear();
				break;
			}

		});
		scene.setOnKeyReleased((KeyEvent t) -> {
			switch (t.getCode()) {
			case W:// forward like fps
				forward = false;
				break;
			case S: // backward
				backward = false;
				break;
			case A: // left
				left = false;
				break;
			case D: // right
				right = false;
				break;
			case SPACE: // can go up and down
				vertUp = false;
				break;
			case CONTROL: // can go up and down
				vertDown = false;
				break;
			case SHIFT: // can go up and down
				multiplier = false;
				break;
			}
		});
	}

	/**
	 * Handles Mouse Events for the chosen SubScene
	 * @param subScene
	 */
	private void handleMouse(SubScene subScene) {
		subScene.setOnMouseClicked((MouseEvent t) -> {
			if (!subSceneVisible) {
				Node picked = t.getPickResult().getIntersectedNode();
				if (t.getButton().equals(MouseButton.PRIMARY)) {
					if (null != picked
							&& placedShapes.getChildren().contains(picked)) {
						unplacedShapes.getChildren().add(picked);
						placedShapes.getChildren().remove(picked);
					}
				} else if (t.getButton().equals(MouseButton.SECONDARY)) {
					placedShapes.getChildren().addAll(
							unplacedShapes.getChildren());
					unplacedShapes.getChildren().clear();
				} else if (t.getButton().equals(MouseButton.MIDDLE)
						&& !unplacedShapes.getChildren().contains(picked)) {
					fillControls((Shape3D) picked);
				}
			} else {
				if (t.getButton().equals(MouseButton.SECONDARY)) {
					subSceneVisible = false;
					controls.setVisible(false);
				}
			}
		});

		subScene.setOnScroll((ScrollEvent t) -> {
			for (Node shape : unplacedShapes.getChildren()) {
				shape.setTranslateX(shape.getTranslateX()
						+ t.getDeltaY()
						/ 2
						* Math.sin(Math.toRadians(cameraXform.ry.getAngle()))
						* Math.sin(Math.toRadians(90 + cameraXform.rx
								.getAngle())));
				shape.setTranslateZ(shape.getTranslateZ()
						+ t.getDeltaY()
						/ 2
						* Math.cos(Math.toRadians(cameraXform.ry.getAngle()))
						* Math.sin(Math.toRadians(90 + cameraXform.rx
								.getAngle())));
				shape.setTranslateY(shape.getTranslateY()
						+ t.getDeltaY()
						/ 2
						* Math.sin(Math.toRadians(0 - cameraXform.rx.getAngle())));
			}
		});

		subScene.setOnMouseMoved((MouseEvent t) -> {
			if (!subSceneVisible) {
				oldX = mouX;
				oldY = mouY;
				mouX = t.getSceneX();
				mouY = t.getSceneY();
				deltaX = (mouX - oldX);
				deltaY = (mouY - oldY);
				double modifier = 3.0;
				double modifierFactor = 0.1;

				double xModification = deltaX * modifierFactor * modifier * 2.0;
				double yModification = -deltaY * modifierFactor * modifier
						* 2.0;

				double yRotate = cameraXform.rx.getAngle() + yModification;

				if (yRotate > cameraYlimit) {
					yRotate = cameraYlimit;
					yModification = yRotate - cameraXform.rx.getAngle();
				} else if (yRotate < -cameraYlimit) {
					yRotate = -cameraYlimit;
					yModification = yRotate - cameraXform.rx.getAngle();
				}

				cameraXform.ry.setAngle(cameraXform.ry.getAngle()
						+ xModification);
				cameraXform.rx.setAngle(yRotate);

				if (!t.isShiftDown()) {
					for (Node shape : unplacedShapes.getChildren()) {
						rotateWithCamera((Shape3D) shape, yModification,
								xModification);
					}
				}

			}
		});

	}

	private void loadAPlanet() {
		Sphere s = new Sphere();
		s.setRadius(150);
		s.setDrawMode(drawModeSetting);
		s.setTranslateX(subSceneCamera.getTranslateX() + 500);
		s.setTranslateZ(subSceneCamera.getTranslateZ() + 500);
		placedShapes.getChildren().add(s);
	}

	private void makeSphere() {
		Sphere s = new Sphere(60);
		PhongMaterial mat = new PhongMaterial();
		mat.setDiffuseColor(Color.DARKRED);
		mat.setSpecularColor(Color.RED);
		s.setMaterial(mat);
		s.setDrawMode(drawModeSetting);
		placeShape(s);
		unplacedShapes.getChildren().add(s);
	}

	private void makeRectangle() {
		Box s = new Box(60, 60, 60);
		PhongMaterial mat = new PhongMaterial();
		mat.setDiffuseColor(Color.DARKRED);
		mat.setSpecularColor(Color.RED);
		s.setMaterial(mat);
		s.setDrawMode(drawModeSetting);
		placeShape(s);
		unplacedShapes.getChildren().add(s);
	}

	private void makeCylinder() {
		Cylinder s = new Cylinder(60, 60);
		PhongMaterial mat = new PhongMaterial();
		mat.setDiffuseColor(Color.DARKRED);
		mat.setSpecularColor(Color.RED);
		s.setMaterial(mat);
		s.setDrawMode(drawModeSetting);
		placeShape(s);
		unplacedShapes.getChildren().add(s);
	}

	private void makePyramid() {
		float side = 60;
		float height = (float) (side / 3 * Math.sqrt(6));
		float triangleHeight = (float) (side * Math.sqrt(3));
		TriangleMesh mesh = new TriangleMesh();
		mesh.getPoints().addAll(0, 0, 0, // Point 0 - Top
				0, height, -side / 2, // Point 1 - Front
				-side / 2, height, 0, // Point 2 - Left
				side / 2, height, 0, // Point 3 - Back
				0, height, side / 2 // Point 4 - Right
				);
		mesh.getTexCoords().addAll(0, 0);
		mesh.getFaces().addAll(0, 0, 2, 0, 1, 0, // Front left face
				0, 0, 1, 0, 3, 0, // Front right face
				0, 0, 3, 0, 4, 0, // Back right face
				0, 0, 4, 0, 2, 0, // Back left face
				4, 0, 1, 0, 2, 0, // Bottom rear face
				4, 0, 3, 0, 1, 0 // Bottom front face
				);
		// To add a TriangleMesh to a 3D scene you need a MeshView
		// container object
		MeshView meshView = new MeshView(mesh);
		meshView.setDrawMode(DrawMode.FILL);
		Group pyramidGroup = new Group();
		pyramidGroup.getChildren().add(meshView);
		PhongMaterial mat = new PhongMaterial();
		mat.setDiffuseColor(Color.DARKRED);
		mat.setSpecularColor(Color.RED);
		meshView.setMaterial(mat);
		placeShape(meshView);
		unplacedShapes.getChildren().add(meshView);
	}

	/**
	 * Places Shape in front of camera facing the camera
	 * @param shape
	 */
	private void placeShape(Node shape) {
		shape.setTranslateX(400.0D
				* Math.sin(Math.toRadians(cameraXform.ry.getAngle()))
				* Math.sin(Math.toRadians(90 + cameraXform.rx.getAngle())));
		shape.setTranslateZ(400.0D
				* Math.cos(Math.toRadians(cameraXform.ry.getAngle()))
				* Math.sin(Math.toRadians(90 + cameraXform.rx.getAngle())));
		shape.setTranslateY(400.0D * Math.sin(Math.toRadians(0 - cameraXform.rx
				.getAngle())));
		Rotate srx = new Rotate();
		srx.setAxis(Rotate.X_AXIS);
		srx.setAngle(cameraXform.rx.getAngle() % 360);
		Rotate sry = new Rotate();
		sry.setAxis(Rotate.Y_AXIS);
		sry.setAngle(cameraXform.ry.getAngle());
		shape.getTransforms().addAll(sry, srx);
	}

	/**
	 * Rotates the 
	 * @param shape
	 * @param rotationX
	 * @param rotationY
	 */
	private void rotateWithCamera(Node shape, double rotationX, double rotationY) {
		double xCoords = shape.getTranslateX();
		double yCoords = shape.getTranslateY();
		double zCoords = shape.getTranslateZ();

		double rotationAroundY = -rotationY;
		double distance = Math.sqrt(Math.pow(shape.getTranslateX(), 2)
				+ Math.pow(shape.getTranslateY(), 2)
				+ Math.pow(shape.getTranslateZ(), 2));

		shape.setTranslateX(xCoords * Math.cos(Math.toRadians(rotationAroundY))
				- zCoords * Math.sin(Math.toRadians(rotationAroundY)));

		shape.setTranslateZ(xCoords * Math.sin(Math.toRadians(rotationAroundY))
				+ zCoords * Math.cos(Math.toRadians(rotationAroundY)));

		shape.setTranslateY(yCoords - rotationX * 20);
	}

	private void fillControls(Shape3D shape) {
		colorPicker = new ColorPicker(
				((PhongMaterial) shape.getMaterial()).getSpecularColor());
		colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
			@Override
			public void changed(ObservableValue<? extends Color> observable,
					Color oldValue, Color newValue) {
				((PhongMaterial) shape.getMaterial())
						.setSpecularColor(newValue);
				((PhongMaterial) shape.getMaterial()).setDiffuseColor(newValue);
			}
		});
		controls.getChildren().clear();
		controls.getChildren().add(colorPicker);
		switch (shape.getClass().getSimpleName()) {
		case "Box":
			lengthSlider = new Slider(10, 110, ((Box) shape).getDepth());
			lengthSlider.setBlockIncrement(10);
			lengthSlider.setMajorTickUnit(50);
			lengthSlider.setMinorTickCount(4);
			lengthSlider.setShowTickMarks(true);
			lengthSlider.setSnapToTicks(true);
			lengthSlider.valueProperty().addListener(
					new ChangeListener<Number>() {
						@Override
						public void changed(
								ObservableValue<? extends Number> observable,
								Number oldValue, Number newValue) {
							((Box) shape).setDepth(newValue.doubleValue());
						}
					});
			lengthSlider.setTooltip(new Tooltip("Length"));

			widthSlider = new Slider(10, 110, ((Box) shape).getWidth());
			widthSlider.setBlockIncrement(10);
			widthSlider.setMajorTickUnit(50);
			widthSlider.setMinorTickCount(4);
			widthSlider.setShowTickMarks(true);
			widthSlider.setSnapToTicks(true);
			widthSlider.valueProperty().addListener(
					new ChangeListener<Number>() {
						@Override
						public void changed(
								ObservableValue<? extends Number> observable,
								Number oldValue, Number newValue) {
							((Box) shape).setWidth(newValue.doubleValue());
						}
					});
			widthSlider.setTooltip(new Tooltip("Width"));

			heightSlider = new Slider(10, 110, ((Box) shape).getHeight());
			heightSlider.setBlockIncrement(10);
			heightSlider.setMajorTickUnit(50);
			heightSlider.setMinorTickCount(4);
			heightSlider.setShowTickMarks(true);
			heightSlider.setSnapToTicks(true);
			heightSlider.valueProperty().addListener(
					new ChangeListener<Number>() {
						@Override
						public void changed(
								ObservableValue<? extends Number> observable,
								Number oldValue, Number newValue) {
							((Box) shape).setHeight(newValue.doubleValue());
						}
					});
			heightSlider.setTooltip(new Tooltip("Height"));
			controls.getChildren().addAll(widthSlider, heightSlider,
					lengthSlider);
			break;
		case "Sphere":
			radiusSlider = new Slider(10, 110, ((Sphere) shape).getRadius());
			radiusSlider.setBlockIncrement(10);
			radiusSlider.setMajorTickUnit(50);
			radiusSlider.setMinorTickCount(4);
			radiusSlider.setShowTickMarks(true);
			radiusSlider.setSnapToTicks(true);
			radiusSlider.valueProperty().addListener(
					new ChangeListener<Number>() {
						@Override
						public void changed(
								ObservableValue<? extends Number> observable,
								Number oldValue, Number newValue) {
							((Sphere) shape).setRadius(newValue.doubleValue());
						}
					});
			radiusSlider.setTooltip(new Tooltip("Radius"));
			controls.getChildren().add(radiusSlider);
			break;
		case "Cylinder":
			radiusSlider = new Slider(10, 110, ((Cylinder) shape).getRadius());
			radiusSlider.setBlockIncrement(10);
			radiusSlider.setMajorTickUnit(50);
			radiusSlider.setMinorTickCount(4);
			radiusSlider.setShowTickMarks(true);
			radiusSlider.setSnapToTicks(true);
			radiusSlider.valueProperty().addListener(
					new ChangeListener<Number>() {
						@Override
						public void changed(
								ObservableValue<? extends Number> observable,
								Number oldValue, Number newValue) {
							((Cylinder) shape).setRadius(newValue.doubleValue());
						}
					});
			radiusSlider.setTooltip(new Tooltip("Radius"));
			heightSlider = new Slider(10, 110, ((Cylinder) shape).getHeight());
			heightSlider.setBlockIncrement(10);
			heightSlider.setMajorTickUnit(50);
			heightSlider.setMinorTickCount(4);
			heightSlider.setShowTickMarks(true);
			heightSlider.setSnapToTicks(true);
			heightSlider.valueProperty().addListener(
					new ChangeListener<Number>() {
						@Override
						public void changed(
								ObservableValue<? extends Number> observable,
								Number oldValue, Number newValue) {
							((Cylinder) shape).setHeight(newValue.doubleValue());
						}
					});
			heightSlider.setTooltip(new Tooltip("Height"));
			controls.getChildren().addAll(radiusSlider, heightSlider);
			break;
		case "MeshView":
			volumeSlider = new Slider(1 / 6.0, 11 / 6.0,
					((MeshView) shape).getScaleX());
			volumeSlider.setBlockIncrement(1 / 6.0);
			volumeSlider.setMajorTickUnit(5 / 6.0);
			volumeSlider.setMinorTickCount(4);
			volumeSlider.setShowTickMarks(true);
			volumeSlider.setSnapToTicks(true);
			volumeSlider.valueProperty().addListener(
					new ChangeListener<Number>() {
						@Override
						public void changed(
								ObservableValue<? extends Number> observable,
								Number oldValue, Number newValue) {
							((MeshView) shape).setScaleX(newValue.doubleValue());
							((MeshView) shape).setScaleY(newValue.doubleValue());
							((MeshView) shape).setScaleZ(newValue.doubleValue());
						}
					});
			volumeSlider.setTooltip(new Tooltip("Volume"));
			controls.getChildren().add(volumeSlider);
			break;

		}

		controls.setVisible(true);
		subSceneVisible = true;
	}

	private SubScene createSubScene(Scene scene) {

		SubScene subScene = new SubScene(root, scene.getWidth(),
				scene.getHeight(), true, SceneAntialiasing.BALANCED);
		subScene.setFill(Color.TRANSPARENT);
		subScene.setCamera(subSceneCamera);
		subScene.setVisible(true);

		return subScene;
	}

}