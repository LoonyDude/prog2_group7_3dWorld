package prog2_group7_3dObjects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.collections.ObservableIntegerArray;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public class SkyBox extends Group {

	private final ObservableIntegerArray faces = FXCollections
			.observableIntegerArray();
	private final ObservableFloatArray texCoords = FXCollections
			.observableFloatArray();
	private final ObservableFloatArray points = FXCollections
			.observableFloatArray();

	private final double WIDTH, HEIGHT, DEPTH;
	private TriangleMesh cube;
	private MeshView skyBox;
	private float x0, x1, x2, x3, x4, y0, y1, y2, y3;
	private Image texImg;

	public SkyBox(Image diff) {
		this(20000, 20000, 20000, diff);
	}

	private SkyBox(double w, double h, double d, Image diffMap) {
		this.WIDTH = w;
		this.HEIGHT = h;
		this.DEPTH = d;
		this.cube = new TriangleMesh();
		this.skyBox = new MeshView();
		this.texImg = diffMap;
		PhongMaterial mat = new PhongMaterial();
		mat.setSpecularColor(Color.TRANSPARENT);
		mat.setDiffuseMap(texImg);

		this.calculatePoints();
		this.calculateTexCoords();
		this.calculateFaces();

		this.skyBox.setMesh(cube);
		this.skyBox.setMaterial(mat);
		this.skyBox.setCullFace(CullFace.NONE);

		this.getChildren().add(skyBox);
	}

	private void calculatePoints() {
		float hw = (float) WIDTH / 2f;
		float hh = (float) HEIGHT / 2f;
		float hd = (float) DEPTH / 2f;

		points.addAll(hw, hh, hd, hw, hh, -hd, hw, -hh, hd, hw, -hh, -hd, -hw,
				hh, hd, -hw, hh, -hd, -hw, -hh, hd, -hw, -hh, -hd);
		cube.getPoints().addAll(points);

	}

	private void calculateFaces() {
		faces.addAll(0, 10, 2, 5, 1, 9, 2, 5, 3, 4, 1, 9, 4, 7, 5, 8, 6, 2, 6,
				2, 5, 8, 7, 3, 0, 13, 1, 9, 4, 12, 4, 12, 1, 9, 5, 8, 2, 1, 6,
				0, 3, 4, 3, 4, 6, 0, 7, 3, 0, 10, 4, 11, 2, 5, 2, 5, 4, 11, 6,
				6, 1, 9, 3, 4, 5, 8, 5, 8, 3, 4, 7, 3);
		cube.getFaces().addAll(faces);
	}

	private void calculateTexCoords() {
		x0 = 0f;
		x1 = 1 / 4f;
		x2 = 2 / 4f;
		x3 = 3 / 4f;
		x4 = 1f;
		y0 = 0f;
		y1 = 1 / 3f;
		y2 = 2 / 3f;
		y3 = 1f;
		// x4 = 0; x3 = iw * 0.25f; x2 = iw / 2.0f; x1 = iw * 0.75f; x0 = iw;
		// y3 = 0; y2 = ih * 0.33f; y1 = ih * 0.66f; y0 = ih;

		texCoords.addAll((x1 + 0.001f), (y0 + 0.001f), (x2 - 0.001f), y0, (x0),
				(y1 + 0.001f), (x1 + 0.001f), (y1 + 0.001f), (x2 - 0.001f),
				(y1 + 0.001f), x3, (y1 + 0.001f), (x4), (y1 + 0.001f), (x0),
				(y2 - 0.001f), (x1 + 0.001f), (y2 - 0.001f), x2, (y2 - 0.001f),
				x3, (y2 - 0.001f), (x4), (y2 - 0.001f), (x1 + 0.001f),
				(y3 - 0.001f), x2, (y3 - 0.001f));
		cube.getTexCoords().addAll(texCoords);
	}

	public double getWidth() {
		return WIDTH;
	}

	public double getHeight() {
		return HEIGHT;
	}

	public double getDepth() {
		return DEPTH;
	}
}