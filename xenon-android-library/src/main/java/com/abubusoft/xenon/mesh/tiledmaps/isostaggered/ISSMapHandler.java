package com.abubusoft.xenon.mesh.tiledmaps.isostaggered;

import com.abubusoft.xenon.XenonApplication4OpenGL;
import com.abubusoft.xenon.android.XenonLogger;
import com.abubusoft.xenon.camera.Camera;
import com.abubusoft.xenon.ScreenInfo;
import com.abubusoft.xenon.math.XenonMath;
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
import com.abubusoft.xenon.mesh.tiledmaps.isometric.IsometricHelper;
import com.abubusoft.xenon.opengl.XenonGL;
import com.abubusoft.xenon.shader.drawers.LineDrawer;
import com.abubusoft.xenon.vbo.BufferAllocationType;
import com.abubusoft.xenon.core.Uncryptable;

import android.graphics.Color;

/**
 * <p>
 * Gestore della mappa.
 * </p>
 * <p>
 * <h1>Costruzione della view</h1>
 * <p>
 * <p>
 * La view sulla mappa viene sempre costruita della forma del rombo. Se la mappa è più corta da una delle due dimensioni, i conti verranno fatti come se fosse comunque un rombo completo. La view sulla mappa viene costruita mediante il
 * metodo {@link #onBuildView(TiledMapView, Camera, TiledMapOptions)}. Lo schermo può essere sia landscape che portrait, quindi i risultati possono essere:
 * </p>
 * <p>
 * <h2>Passaggio da mappa a window</h2>
 * <p>
 * Il passaggio tra mappa e window è diverso rispetto alla situazione orthogonal perchè in memoria, la mappa ha le tile di una certa dimensione, mentre nella window le dimensioni sono diverse.
 * </p>
 * <img src="./doc-files/Map2Window.png"/ >
 * <p>
 * <h2>Schermo in portrait mode</h2>
 * <p>
 * Quando impostiamo il {@link com.abubusoft.xenon.mesh.tiledmaps.TiledMapFillScreenType#FILL_HEIGHT}:
 * </p>
 * <img src="./doc-files/view1.png"/ >
 * <p>
 * Quando impostiamo il {@link com.abubusoft.xenon.mesh.tiledmaps.TiledMapFillScreenType#FILL_WIDTH}:
 * </p>
 * <img src="./doc-files/view2.png" />
 * <p>
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
 * @author xcesco
 */
@Uncryptable
public class ISSMapHandler extends AbstractMapHandler<ISSMapController> {


    public enum Status {
        STANDARD,
        DISP_0,
        DISP_1,
        DISP_2,
        DISP_3,
        DISP_4,
        UNSPOSTR
    };

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
     * <p>
     * <p>
     * Ricordarsi di abilitare il blend prima di questo metodo (tipicamente nel {@link XenonApplication4OpenGL#onSceneReady(boolean, boolean, boolean)})
     * </p>
     * <p>
     * <pre>
     * GLES20.glEnable(GLES20.GL_BLEND);
     * GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
     * </pre>
     *
     * @param deltaTime           tempo trascorso dall'ultimo draw
     * @param modelViewProjection matrice MVP
     */
    @Override
    public void draw(long deltaTime, Matrix4x4 modelViewProjection) {
        super.draw(deltaTime, modelViewProjection);

        lineDrawer.begin();

        // window quadrata, deve contenere la window reaale
        lineDrawer.setColor(Color.GREEN);
        matrixWire.buildScaleMatrix(map.view().windowDimension, map.view().windowDimension, 1f);
        matrixWire.multiply(modelViewProjection, matrixWire);
        lineDrawer.draw(wireWindow, matrixWire);

        lineDrawer.setColor(Color.RED);
        matrixWire.buildScaleMatrix(map.view().windowWidth, map.view().windowHeight, 1f);
        matrixWire.multiply(modelViewProjection, matrixWire);
        lineDrawer.draw(wireWindow, matrixWire);

        lineDrawer.setColor(Color.BLUE);
        matrixWire.buildScaleMatrix(tiledWindowWidth, tiledWindowHeight, 1f);
        matrixWire.multiply(modelViewProjection, matrixWire);
        lineDrawer.draw(wireWindow, matrixWire);

        lineDrawer.end();
    }

    /**
     * <p>
     * Nella costruzione della window, per le mappe isometriche, bisogna tenere in considerazione che quello che conta per il riempimento dello schermo contano solo le dimensioni del diamante. L'opzione {@link TiledMapFillScreenType}
     * </p>
     * <p>
     * <img src="./doc-files/windows.png" style="width: 60%"/>
     * <p>
     * <p></p>
     */
    @Override
    public void onBuildView(TiledMapView view, Camera camera, TiledMapOptions options) {
        ScreenInfo screenInfo = XenonGL.screenInfo;
        // impostiamo metodo di riempimento dello schermo
        view.windowDimension = 0;

        switch (options.fillScreenType) {
            case FILL_HEIGHT:
                // calcoliamo prima l'altezza e poi la larghezza in funzione dell'aspect ratio
                view.windowDimension = view.windowHeight = Math.round(map.tileRows * map.tileHeight * 0.5f);
                view.windowWidth = Math.round(view.windowHeight * screenInfo.aspectRatio);

                // le righe sono definite, le colonne vengono ricavate in funzione
                view.windowTileRows = map.tileRows;
                view.windowTileColumns = view.windowWidth / map.tileWidth;

                break;
            case FILL_CUSTOM_HEIGHT:
                // calcoliamo prima l'altezza e poi la larghezza in funzione dell'aspect ratio
                view.windowDimension = view.windowHeight = Math.round(options.visibleTiles * map.tileHeight * 0.5f);
                view.windowWidth = Math.round(view.windowHeight * screenInfo.aspectRatio);

                // le righe sono definite, le colonne vengono ricavate in funzione
                view.windowTileRows = options.visibleTiles;
                view.windowTileColumns = view.windowWidth / map.tileWidth;

                break;
            case FILL_WIDTH:
                view.windowDimension = view.windowWidth = Math.round(map.tileColumns * map.tileWidth);
                view.windowHeight = Math.round(view.windowDimension / screenInfo.aspectRatio);

                // le righe sono definite, le colonne vengono ricavate in funzione
                view.windowTileColumns = map.tileColumns;
                view.windowTileRows = view.windowHeight / map.tileHeight;

                break;
            case FILL_CUSTOM_WIDTH:
                view.windowDimension = view.windowWidth = Math.round(options.visibleTiles * map.tileWidth);
                view.windowHeight = Math.round(view.windowDimension / screenInfo.aspectRatio);

                // le righe sono definite, le colonne vengono ricavate in funzione
                view.windowTileColumns = options.visibleTiles;
                view.windowTileRows = view.windowHeight / map.tileHeight;

                break;
        }

        // il numero di righe e colonne non possono andare oltre il numero di definizione
        view.windowTileColumns = XenonMath.clampI(view.windowTileColumns, 0, map.tileColumns);
        view.windowTileRows = XenonMath.clampI(view.windowTileRows, 0, map.tileRows);

        // calcoliamo le dimensioni della window su base isometrica e definiamo i limiti di spostamento
        isoWindowWidth = view.windowTileColumns * isoTileSize;
        isoWindowHeight = view.windowTileRows * isoTileSize;
        view.mapMaxPositionValueX = map.mapWidth - isoWindowWidth;
        view.mapMaxPositionValueY = map.mapHeight - isoWindowHeight;

        //view.windowDimension *= options.visiblePercentage;

        // il quadrato della dimensione deve essere costruito sempre sulla dimensione massima
        view.windowDimension = XenonMath.max(view.windowWidth, view.windowHeight) * options.visiblePercentage;

        view.distanceFromViewer = XenonMath.zDistanceForSquare(camera, view.windowDimension);
        //TODO distanza
        //view.distanceFromViewer = XenonMath.zDistanceForSquare(camera, view.windowDimension * 2);

        // calcoliamo il centro dello schermo, senza considerare i bordi aggiuntivi
        view.windowCenter.x = view.windowWidth * 0.5f;
        view.windowCenter.y = view.windowHeight * 0.5f;

        // non ci possono essere reminder
        int windowRemainderX = 0;//view.windowWidth % map.tileWidth;
        int windowRemainderY = 0;//view.windowHeight % map.tileHeight;

        view.windowBorder = 1;

        // +2 per i bordi, +1 se la divisione contiene un resto
        view.windowTileColumns += (windowRemainderX == 0 ? 0 : 0) + view.windowBorder * 2;
        view.windowTileRows += (windowRemainderY == 0 ? 0 : 0) + view.windowBorder * 2;

        // si sta disegnando dal vertice più in alto del rombo
        view.windowVerticesBuffer = ISSHelper.buildISSVertexBuffer(view.windowTileRows, view.windowTileColumns, map.tileWidth, map.tileHeight * .5f, map.tileWidth, map.tileHeight);

        // lo impostiamo una volta per tutte, tanto non verrà mai cambiato
        view.windowVerticesBuffer.update();

        // recupera gli offset X e mY maggiori (che vanno comunque a ricoprire gli alitr più piccoli)
        // e li usa per spostare la matrice della maschera. Tutti i tileset devono avere lo stesso screenOffsetX e Y
        /*
         * float maxLayerOffsetX = 0f; float maxLayerOffsetY = 0f; for (int i = 0; i < map.tileSets.size(); i++) { maxLayerOffsetX = XenonMath.max(map.tileSets.get(i).drawOffsetX, maxLayerOffsetX); maxLayerOffsetY =
		 * XenonMath.max(map.tileSets.get(i).drawOffsetY, maxLayerOffsetY); }
		 */
        Mesh tempMesh = MeshFactory.createSprite(1f, 1f, MeshOptions.build().bufferAllocation(BufferAllocationType.STATIC).indicesEnabled(true));
        this.wireWindow = MeshFactory.createWireframe(tempMesh);

        // calcoliamo
        view.tiledWindowWidth = (view.windowTileColumns * map.tileWidth);
        view.tiledWindowHeight = view.windowTileRows * map.tileHeight * .5f;
        tiledWindowWidth = view.tiledWindowWidth;
        tiledWindowHeight = view.tiledWindowHeight;

        // punto di partenza delle tile. tiledWindow è sempre maggiore è il vettore dall'origine blu a quella rossa
        view.tileBase.setCoords(view.tiledWindowWidth - view.windowWidth, view.tiledWindowHeight - view.windowHeight);
        view.tileBase.mul(0.5f);

        lineDrawer = new LineDrawer();
        lineDrawer.setLineWidth(4);
        lineDrawer.setColor(Color.RED);

        matrixWire = new Matrix4x4();
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

    int temp;

    int a, b;


    @Override
    public void convertMap2ViewLayer(LayerOffsetHolder offsetHolder, int mapX, int mapY) {
        // http://stackoverflow.com/questions/1295424/how-to-convert-float-to-int-with-java
        //offsetHolder.tileIndexX = (int) (mapX - ((mapY*2/map.tileHeight) % 2)*map.tileHeight) / (2* map.tileHeight);
        //offsetHolder.tileIndexY = (int) (mapY / map.tileHeight);
        // da centered window a iso window

        //a=mapX-2*mapY;
        //b=mapX+2*mapY;
        //mapX=a;
        //mapY=b;

//        a = (int) (mapX / map.tileWidth);
//        b = (int) (mapY / map.tileHeight);
//
//        offsetHolder.tileIndexX=(a-b-((a+b) %2))/2;
//        offsetHolder.tileIndexY=a+b;


       /* if (offsetHolder.tileIndexY % 2 == 1) {
            temp=offsetHolder.tileIndexX;
            offsetHolder.tileIndexX = offsetHolder.tileIndexY;
            offsetHolder.tileIndexY = temp;
        }*/

        // soluzione fixata
        //offsetHolder.setOffset(ISSHelper.convertIsoMapOffset2ScreenOffset(mapX % map.tileHeight, mapY % map.tileHeight));
        // offsetHolder.screenOffsetX=mapX % map.tileHeight;
        //offsetHolder.screenOffsetY=mapY % map.tileHeight;


        // da centered window a iso window
// da iso window a centered window
        //workPoint.x = (+isoX + isoY) / 2;
        //workPoint.y = (-isoX + isoY) / 4;

        // http://stackoverflow.com/questions/1295424/how-to-convert-float-to-int-with-java
        // v1
//        offsetHolder.tileIndexX = (int) (-(mapX - 2f * mapY) / map.tileWidth);
//        offsetHolder.tileIndexY = (int) ((mapX + 2f * mapY) / map.tileWidth);

        int ix, iy;
        ix = (mapX + 2 * mapY) / 2;
        iy = (-mapX + 2 * mapY) / 2;

        // inverso
        // x = xi - yi
        // y= (xi+yi) / 2

        // v2: ok
        // passiamo da map a iso diamond
        offsetHolder.tileIndexX = (int) (ix / map.tileHeight);
        offsetHolder.tileIndexY = (int) (iy / map.tileHeight);

        a = offsetHolder.tileIndexX;
        b = offsetHolder.tileIndexY;

        // passiamo da diamon a staggered
        offsetHolder.tileIndexX = (a - b + ((a + b) % 2)) / 2;
        offsetHolder.tileIndexY = a + b;

        //  offsetHolder.tileIndexX=0;//(a-b-((a+b) %2))/2;
        //   offsetHolder.tileIndexY=0;//a+b;
        // IsometricHelper.convertRawScreen2IsoMap()

        // soluzione fixata
        // v1
        //offsetHolder.setOffset(IsometricHelper.convertIsoMapOffset2ScreenOffset(mapX % map.tileHeight, mapY % map.tileHeight));
        // v2
        offsetHolder.screenOffsetX = mapX % map.tileWidth;
        offsetHolder.screenOffsetY = mapY % map.tileHeight;
        //v3
        Status volo = Status.STANDARD;

        if (offsetHolder.tileIndexY % 2 == 1) {

            //offsetHolder.screenOffsetX -=map.tileWidth/2;
            //offsetHolder.screenOffsetY -=map.tileHeight/2;

            volo = Status.UNSPOSTR;

            if (offsetHolder.screenOffsetX<map.tileWidth/2) {
                volo=Status.DISP_0;
                offsetHolder.tileIndexY--;

                offsetHolder.screenOffsetX -=map.tileWidth;
                offsetHolder.screenOffsetY -=map.tileHeight*.5f;
            } else {
                volo=Status.DISP_1;
                offsetHolder.tileIndexY--;
                offsetHolder.tileIndexX--;

                offsetHolder.screenOffsetX -=map.tileWidth;
                offsetHolder.screenOffsetY -=map.tileHeight*.5f;

            }
            /*if (offsetHolder.screenOffsetX<map.tileWidth/2 && offsetHolder.screenOffsetY<map.tileHeight/2) {
                offsetHolder.tileIndexX--;
                offsetHolder.tileIndexY--;
                offsetHolder.screenOffsetY -=map.tileHeight/2;
                volo = Status.DISP_0;
            } *//*else if (offsetHolder.screenOffsetX>=map.tileWidth/2 && offsetHolder.screenOffsetY<map.tileHeight/2) {
                offsetHolder.tileIndexY--;
                offsetHolder.tileIndexX++;

                offsetHolder.screenOffsetX -=map.tileWidth;
                //offsetHolder.screenOffsetY +=y;

                volo = Status.DISP_1;
            } else if (offsetHolder.screenOffsetX<map.tileWidth/2  && offsetHolder.screenOffsetY>=map.tileHeight/2) {
                offsetHolder.tileIndexY++;

                //offsetHolder.screenOffsetX -=map.tileWidth;
                //offsetHolder.screenOffsetY -=map.tileHeight;

                volo = Status.DISP_2;
            } else {
                volo = Status.DISP_3;
            }*/



            //int ax=(ix % map.tileHeight)+map.tileHeight;
            //int ay=(iy % map.tileHeight)+map.tileHeight;
            //int x=ax-ay;
            //int y=(ax+ay)/2;
            //offsetHolder.screenOffsetX +=x;
            //offsetHolder.screenOffsetY +=y;


            // x = xi - yi
            // y= (xi+yi) / 2

            //offsetHolder.tileIndexX--;

            // offsetHolder.screenOffsetX +=map.tileWidth*.25f;
            //offsetHolder.screenOffsetY +=map.tileHeight;

            //volo = true;



        }

        //offsetHolder.screenOffsetX=0;
        //offsetHolder.screenOffsetY=0;

        XenonLogger.info("map[%s, %s] -> iso[%s, %s], tiles I[%s, %s] -> S[%s, %s], map off x,y (%s, %s) [%s]", mapX, mapY, ix, iy,a, b, offsetHolder.tileIndexX, offsetHolder.tileIndexY, offsetHolder.screenOffsetX, offsetHolder.screenOffsetY, volo);

        // inverte y
        //offsetHolder.screenOffsetX = mapX % map.tileWidth;
        offsetHolder.screenOffsetY = -offsetHolder.screenOffsetY;
    }

}
