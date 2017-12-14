package com.abubusoft.xenon.mesh.tiledmaps.isostaggered;

import com.abubusoft.xenon.ArgonApplication4OpenGL;
import com.abubusoft.xenon.Camera;
import com.abubusoft.xenon.ScreenInfo;
import com.abubusoft.xenon.math.ArgonMath;
import com.abubusoft.xenon.math.Matrix4x4;
import com.abubusoft.xenon.math.Point2;
import com.abubusoft.xenon.mesh.Mesh;
import com.abubusoft.xenon.mesh.MeshFactory;
import com.abubusoft.xenon.mesh.MeshOptions;
import com.abubusoft.xenon.mesh.tiledmaps.ImageLayer;
import com.abubusoft.xenon.mesh.tiledmaps.ObjectLayer;
import com.abubusoft.xenon.mesh.tiledmaps.TiledLayer;
import com.abubusoft.xenon.mesh.tiledmaps.TiledMap;
import com.abubusoft.xenon.mesh.tiledmaps.TiledMapFillScreenType;
import com.abubusoft.xenon.mesh.tiledmaps.TiledMapOptions;
import com.abubusoft.xenon.mesh.tiledmaps.internal.AbstractMapHandler;
import com.abubusoft.xenon.mesh.tiledmaps.internal.LayerOffsetHolder;
import com.abubusoft.xenon.mesh.tiledmaps.internal.TiledMapView;
import com.abubusoft.xenon.opengl.ArgonGL;
import com.abubusoft.xenon.shader.drawers.LineDrawer;
import com.abubusoft.xenon.vbo.BufferAllocationType;
import com.abubusoft.xenon.core.Uncryptable;

import android.graphics.Color;

/**
 * <p>
 * Gestore della mappa.
 * </p>
 * 
 * <h1>Costruzione della view</h1>
 * 
 * <p>
 * La view sulla mappa viene sempre costruita della forma del rombo. Se la mappa è più corta da una delle due dimensioni, i conti verranno fatti come se fosse comunque un rombo completo. La view sulla mappa viene costruita mediante il
 * metodo {@link #onBuildView(TiledMapView, Camera, TiledMapOptions)}. Lo schermo può essere sia landscape che portrait, quindi i risultati possono essere:
 * </p>
 * 
 * <h2>Passaggio da mappa a window</h2>
 * <p>
 * Il passaggio tra mappa e window è diverso rispetto alla situazione orthogonal perchè in memoria, la mappa ha le tile di una certa dimensione, mentre nella window le dimensioni sono diverse.
 * </p>
 * <img src="./doc-files/Map2Window.png"/ >
 * 
 * <h2>Schermo in portrait mode</h2>
 * <p>
 * Quando impostiamo il {@link com.abubusoft.xenon.mesh.tiledmaps.TiledMapFillScreenType#FILL_HEIGHT}:
 * </p>
 * <img src="./doc-files/view1.png"/ >
 * <p>
 * Quando impostiamo il {@link com.abubusoft.xenon.mesh.tiledmaps.TiledMapFillScreenType#FILL_WIDTH}:
 * </p>
 * <img src="./doc-files/view2.png" />
 * 
 * <h2>Schermo in landscape mode</h2>
 * <p>
 * Quando impostiamo il {@link com.abubusoft.xenon.mesh.tiledmaps.TiledMapFillScreenType#FILL_HEIGHT}:
 * </p>
 * <img src="./doc-files/view3.png" />
 * <p>
 * Quando impostiamo il {@link com.abubusoft.xenon.mesh.tiledmaps.TiledMapFillScreenType#FILL_WIDTH}:
 * </p>
 * <img src="./doc-files/view4.png" />
 * 
 * 
 * @author xcesco
 *
 */
public class ISSMapHandler extends AbstractMapHandler<ISSMapController>  implements Uncryptable {

	/**
	 * <p>
	 * Dimensione della tile nel sistema di riferimento isometrico della mappa.
	 * </p>
	 */
	protected float isoTileSize;

	/**
	 * <p>
	 * Dimensione width della window su base isometrica. In altre parole, le dimensioni del diamante di visualizzazione nel sistema della mappa.
	 * </p>
	 */
	protected float isoWindowWidth;

	/**
	 * <p>
	 * Dimensione height della window su base isometrica. In altre parole, le dimensioni del diamante di visualizzazione nel sistema della mappa.
	 * </p>
	 */
	protected float isoWindowHeight;

	private Mesh wireWindow;

	private LineDrawer lineDrawer;

	private Matrix4x4 matrixWire;

	private float tiledWindowHeight;

	private float tiledWindowWidth;

	public ISSMapHandler(TiledMap map) {
		super(map);

		// a differenza della orthogonal, le dimensioni delle tile sulla mappa sono diverse rispetto a quelle
		// sulla view.
		// sulla mappa isometrica le dimensioni delle tile sono dimezzate, quindi la larghezza della mappa cambia
		isoTileSize = map.tileHeight;
		
		// dimensioni map nel sistema di coordinate della mappa
		map.mapWidth = (int) (map.tileColumns * isoTileSize);
		map.mapHeight = (int) (map.tileRows * isoTileSize);
		
	}

	/**
	 * <p>
	 * Calcola la mappa e la disegna. Tra le varie cose aggiorna anche il frame marker
	 * </p>
	 * 
	 * <p>
	 * Ricordarsi di abilitare il blend prima di questo metodo (tipicamente nel {@link ArgonApplication4OpenGL#onSceneReady(android.content.SharedPreferences, boolean, boolean, boolean)})
	 * </p>
	 * 
	 * <pre>
	 * GLES20.glEnable(GLES20.GL_BLEND);
	 * GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	 * </pre>
	 * 
	 * @param deltaTime
	 *            tempo trascorso dall'ultimo draw
	 * @param modelViewProjection
	 *            matrice MVP
	 */
	@Override
	public void draw(long deltaTime, Matrix4x4 modelViewProjection) {
		super.draw(deltaTime, modelViewProjection);
		
		lineDrawer.begin();
		
		// window quadrata, deve contenere la window reaale
		lineDrawer.setColor(Color.GREEN);
		matrixWire.buildScaleMatrix(map.view().windowDimension, map.view().windowDimension, 1f);
		matrixWire.multiply(modelViewProjection,matrixWire);				
		lineDrawer.draw(wireWindow, matrixWire);
		
		lineDrawer.setColor(Color.RED);
		matrixWire.buildScaleMatrix(map.view().windowWidth, map.view().windowHeight, 1f);
		matrixWire.multiply(modelViewProjection,matrixWire);				
		lineDrawer.draw(wireWindow, matrixWire);
		
		lineDrawer.setColor(Color.BLUE);
		matrixWire.buildScaleMatrix(tiledWindowWidth, tiledWindowHeight, 1f);
		matrixWire.multiply(modelViewProjection,matrixWire);				
		lineDrawer.draw(wireWindow, matrixWire);
		
		lineDrawer.end();
		
		
	}

	/**
	 * <p>
	 * Nella costruzione della window, per le mappe isometriche, bisogna tenere in considerazione che quello che conta per il riempimento dello schermo contano solo le dimensioni del diamante. L'opzione {@link TiledMapFillScreenType}
	 * </p>
	 * 
	 * <img src="./doc-files/windows.png" style="width: 60%"/>
	 * 
	 * <p></p>
	 * 
	 */
	@Override
	public void onBuildView(TiledMapView view, Camera camera, TiledMapOptions options) {
		ScreenInfo screenInfo = ArgonGL.screenInfo;
		// impostiamo metodo di riempimento dello schermo
		view.windowDimension = 0;

		switch (options.fillScreenType) {
		case FILL_HEIGHT:
			// calcoliamo prima l'altezza e poi la larghezza in funzione dell'aspect ratio
			view.windowDimension=view.windowHeight =  Math.round(map.tileRows * map.tileHeight*0.5f);
			view.windowWidth= Math.round(view.windowHeight * screenInfo.aspectRatio);
			
			// le righe sono definite, le colonne vengono ricavate in funzione
			view.windowTileRows=map.tileRows;
			view.windowTileColumns=view.windowWidth/map.tileWidth;
						
			break;
		case FILL_CUSTOM_HEIGHT:
			// calcoliamo prima l'altezza e poi la larghezza in funzione dell'aspect ratio
			view.windowDimension=view.windowHeight =  Math.round(options.visibleTiles  * map.tileHeight*0.5f);
			view.windowWidth= Math.round(view.windowHeight * screenInfo.aspectRatio);
			
			// le righe sono definite, le colonne vengono ricavate in funzione
			view.windowTileRows=options.visibleTiles;
			view.windowTileColumns=view.windowWidth/map.tileWidth;
						
			break;
		case FILL_WIDTH:
			view.windowDimension=view.windowWidth =  Math.round(map.tileColumns * map.tileWidth);
			view.windowHeight= Math.round(view.windowDimension / screenInfo.aspectRatio);
			
			// le righe sono definite, le colonne vengono ricavate in funzione
			view.windowTileColumns=map.tileColumns;
			view.windowTileRows=view.windowHeight/map.tileHeight;			
						
			break;
		case FILL_CUSTOM_WIDTH:
			view.windowDimension=view.windowWidth =  Math.round(options.visibleTiles * map.tileWidth);
			view.windowHeight= Math.round(view.windowDimension / screenInfo.aspectRatio);
			
			// le righe sono definite, le colonne vengono ricavate in funzione
			view.windowTileColumns=options.visibleTiles;
			view.windowTileRows=view.windowHeight/map.tileHeight;
			
			break;
		}				
		
		// il numero di righe e colonne non possono andare oltre il numero di definizione
		view.windowTileColumns=ArgonMath.clampI(view.windowTileColumns, 0, map.tileColumns);
		view.windowTileRows=ArgonMath.clampI(view.windowTileRows, 0, map.tileRows);
		
		// calcoliamo le dimensioni della window su base isometrica e definiamo i limiti di spostamento
		isoWindowWidth= view.windowTileColumns * isoTileSize;
		isoWindowHeight = view.windowTileRows * isoTileSize;		
		view.mapMaxPositionValueX = map.mapWidth - isoWindowWidth;
		view.mapMaxPositionValueY = map.mapHeight - isoWindowHeight;
		
		//view.windowDimension *= options.visiblePercentage;
		
		// il quadrato della dimensione deve essere costruito sempre sulla dimensione massima
		view.windowDimension=ArgonMath.max(view.windowWidth, view.windowHeight)*options.visiblePercentage;
		
		view.distanceFromViewer = ArgonMath.zDistanceForSquare(camera, view.windowDimension*4);

		// calcoliamo il centro dello schermo, senza considerare i bordi aggiuntivi
		view.windowCenter.x = view.windowWidth*0.5f;
		view.windowCenter.y =  view.windowHeight*0.5f;
		
		// non ci possono essere reminder
		int windowRemainderX = 0;//view.windowWidth % map.tileWidth;
		int windowRemainderY = 0;//view.windowHeight % map.tileHeight;
				
		// +2 per i bordi, +1 se la divisione contiene un resto
		view.windowTileColumns +=  (windowRemainderX == 0 ? 0 : 0) + 2;
		view.windowTileRows += (windowRemainderY == 0 ? 0 : 0) + 2;

		// si sta disegnando dal vertice più in alto del rombo
		view.windowVerticesBuffer = ISSHelper.buildISSVertexBuffer(view.windowTileRows, view.windowTileColumns, map.tileWidth, map.tileHeight * .5f, map.tileWidth, map.tileHeight);

		// lo impostiamo una volta per tutte, tanto non verrà mai cambiato
		view.windowVerticesBuffer.update();

		// recupera gli offset X e mY maggiori (che vanno comunque a ricoprire gli alitr più piccoli)
		// e li usa per spostare la matrice della maschera. Tutti i tileset devono avere lo stesso offsetX e Y
		/*
		 * float maxLayerOffsetX = 0f; float maxLayerOffsetY = 0f; for (int i = 0; i < map.tileSets.size(); i++) { maxLayerOffsetX = ArgonMath.max(map.tileSets.get(i).drawOffsetX, maxLayerOffsetX); maxLayerOffsetY =
		 * ArgonMath.max(map.tileSets.get(i).drawOffsetY, maxLayerOffsetY); }
		 */		
		Mesh tempMesh=MeshFactory.createSprite(1f, 1f, MeshOptions.build().bufferAllocation(BufferAllocationType.STATIC).indicesEnabled(true));
		this.wireWindow=MeshFactory.createWireframe(tempMesh);
		
		// calcoliamo 
		view.tiledWindowWidth=(view.windowTileColumns *  map.tileWidth);
		view.tiledWindowHeight=view.windowTileRows * map.tileHeight * .5f;
		tiledWindowWidth=view.tiledWindowWidth;
		tiledWindowHeight=view.tiledWindowHeight;
		
		// punto di partenza delle tile. tiledWindow è sempre maggiore è il vettore dall'origine blu a quella rossa
		view.tileBase.setCoords(view.tiledWindowWidth-view.windowWidth , view.tiledWindowHeight-view.windowHeight);
		view.tileBase.mul(0.5f);
				
		lineDrawer=new LineDrawer();
		lineDrawer.setLineWidth(4);
		lineDrawer.setColor(Color.RED);
		
		matrixWire=new Matrix4x4();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abubusoft.xenon.mesh.tiledmaps.internal.MapHandler#convertScroll(com.abubusoft.xenon.math.Point2, float, float)
	 */
	@Override
	public void convertRawWindow2MapWindow(Point2 scrollInMap, float rawWindowX, float rawWindowY) {
		
		// rawWindowY è rivolto verso il basso, prima di passarlo al metodo, bisogna invertire il segno
		//scrollInMap.set(ISSHelper.convertCenteredWindow2IsoWindow(controller, rawWindowX, -rawWindowY));
		scrollInMap.setCoords(rawWindowX, rawWindowY);

	}

	@SuppressWarnings("unchecked")
	@Override
	public ISSMapController buildMapController(TiledMap tiledMap, Camera cameraValue) {
		controller = new ISSMapController(tiledMap, cameraValue);
		return controller;
	}

	@SuppressWarnings("unchecked")
	public ISSTiledLayerHandler buildTiledLayerHandler(TiledLayer layer) {
		return new ISSTiledLayerHandler(layer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ISSObjectLayerHandler buildObjectLayerHandler(ObjectLayer layer) {
		return new ISSObjectLayerHandler(layer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ISSImageLayerHandler buildImageLayerHandler(ImageLayer layer) {
		return new ISSImageLayerHandler(layer);
	}

	@Override
	public void convertMap2ViewLayer(LayerOffsetHolder offsetHolder, int mapX, int mapY) {
		// http://stackoverflow.com/questions/1295424/how-to-convert-float-to-int-with-java
		offsetHolder.tileIndexX = (int) (mapX / map.tileHeight);
		offsetHolder.tileIndexY = (int) (mapY / map.tileHeight);

		// soluzione fixata
		offsetHolder.setOffset(ISSHelper.convertIsoMapOffset2ScreenOffset(mapX % map.tileHeight, mapY % map.tileHeight));
		//offsetHolder.offsetX=0;
		//offsetHolder.offsetY=0;
		// Logger.info("Original index %s , %s ***   offset x %s y %s ",offsetHolder.tileIndexX, offsetHolder.tileIndexY, offsetHolder.offsetX, offsetHolder.offsetY);

	}

}
