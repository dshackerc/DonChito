package mx.itesm.donchito;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.Random;


public class LivermorioEscape implements Screen {
    public static final int PLATFORM_OFFSET = 500;
    public static final int RANDOMNESS_INT = 70;
    private OrthographicCamera camera,cameraHUD;
    private final DonChito game;
    private Viewport view;

    //TODO Refactor to interface
    private SimpleAsset fondo,
                        fondo2,
                        fondoPausa,
                        botonPausa,
                        botonPlay,
                        botonSalirMenu,
                        botonConfiguracion,
                        arrowLeft,
                        arrowRight,
                        arrowUp,
                        powerUpAs;
    private SpriteBatch batch;
    private Music musicaFondo;

    Array<SimpleAsset> platforms;

    DonChitoLivermorio player;
    private int leftPointer,rightPointer;
    private static final float DEATH_MOVE_SPEED = 200;
    public static final int PLATFORM_WIDTH = 3512;
    private static final int BACKGROUND_SIZE = PLATFORM_WIDTH;
    private float deathVelocity = 1;
    private Vector2 DeathPosition;
    private Animation animationDeath;
    private float deathStartTime,gameStartTime;
    private int nFondos = 2,posFondos = BACKGROUND_SIZE * 2;
    private TextureRegion regionDeath;


    //pantalla->Plataformas->Coordenada
    private int[] carretasX = new int[]{114,600,1460,1952,2837,3024,3429,3994,4706,5107,5800,5952,7018,7016,7412,8560,8960,9336,8861,9528,9685,10036,10394};
    private int[] carretasY = new int[]{283,90,254,12,18,419,17,173,293,294,10,486,3,2,2,6,18,17,474,478,172,314,452};

    private int[] maderasX = new int[]{810,1217,1965,2206,3600,4029,4460,5064,6414,6339,6643,7119,7613,7963,8219,11001,10922,11404};
    private int[] maderasY = new int[]{431,42,569,281,523,520,20,129,555,192,402,412,598,172,479,516,54,71};

    //sumar 12,000 a todas las x para ciclar

    private boolean powerUp = false;

    private PlayerState playerState = PlayerState.NOTDEAD;
    private GameState gameState = GameState.PLAY;
    private MoveState moveState = MoveState.NONE;


    public LivermorioEscape(DonChito game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        camera = new OrthographicCamera(DonChito.ANCHO_MUNDO,DonChito.ALTO_MUNDO);
        camera.position.set(DonChito.ANCHO_MUNDO / 2, DonChito.ALTO_MUNDO / 2, 0);
        camera.update();

        cameraHUD = new OrthographicCamera(DonChito.ANCHO_MUNDO,DonChito.ALTO_MUNDO);
        cameraHUD.position.set(DonChito.ANCHO_MUNDO / 2, DonChito.ALTO_MUNDO / 2, 0);
        cameraHUD.update();

        view = new FitViewport(DonChito.ANCHO_MUNDO,DonChito.ALTO_MUNDO,camera);
        batch = new SpriteBatch();
        platforms = new Array<SimpleAsset>();
        player = new DonChitoLivermorio(400,100);
        gameStartTime = TimeUtils.nanoTime();
        cargaPosiciones();
        cargarAudio();
        cargarRecursos();
        leerEntrada();
        crearElementos();
    }
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        view.apply();
        actualizarCamara();
        renderCamera();
        checkCollisions();
        if(playerState == PlayerState.NOTDEAD){
            if(gameState == GameState.PLAY){
                actualizarCamara();
                update(delta);
                realInput();
                if(MathUtils.nanoToSec * (TimeUtils.nanoTime() - gameStartTime) >=3){
                    gameStartTime = TimeUtils.nanoTime();
                    int randomAs = (new Random()).nextInt(Constants.PLATFORMS.length);
                    /*
                    int randomChoice = (new Random()).nextInt(posPlatforms.length);
                    int yPlat = posPlatforms[randomChoice];
                    SimpleAsset temp = new SimpleAsset(Constants.PLATFORMS[randomAs],player.getX()+800,yPlat);
                    if(!powerUp &&  RANDOMNESS_INT < (int)(Math.random() * ((100-1) + 1) ) && !powerUp){
                        powerUpAs.setPosition(player.getX()+ PLATFORM_OFFSET,yPlat+temp.getSprite().getHeight());
                    }
                    platforms.add(temp);
                    */
                }
                if((posFondos-1280) < player.getX() ){
                    switch ((nFondos+1)%2){
                        case 1:
                            fondo.setPosition(fondo2.getSprite().getX()+fondo2.getSprite().getWidth(),0);
                            break;
                        case 0:
                            fondo2.setPosition(fondo.getSprite().getX()+fondo.getSprite().getWidth(),0);
                            break;
                    }
                    nFondos++;
                    posFondos += PLATFORM_WIDTH;
                }
            }
            renderHUD();
        }else{
            renderDeath();
        }
    }
    private void crearElementos(){
        fondoPausa = new SimpleAsset(Constants.GLOBAL_MENU_PAUSA_PNG,0,0);
        botonPlay = new SimpleAsset(Constants.GLOBAL_BOTON_PLAY_PNG,1110,0);
        botonConfiguracion = new SimpleAsset(Constants.GLOBAL_BOTON_CONFIGURACION_PNG,405,175);
        botonSalirMenu = new SimpleAsset(Constants.GLOBAL_BOTON_SALIRMENU_PNG,405,425);
        botonPausa = new SimpleAsset(Constants.GLOBAL_BOTON_PAUSA_PNG,1110,0);
        botonPausa.setAlpha(0.5f);
        botonPlay.setAlpha(0.5f);
        fondo = new SimpleAsset(Constants.LIVERMORIO_FONDO_PNG,0,0);
        fondo2 = new SimpleAsset(Constants.LIVERMORIO_FONDO_PNG,posFondos/2,0);
        powerUpAs = new SimpleAsset(Constants.ROMAN_PIEDRA,-1233,23);
        powerUpAs.getSprite().setScale(.25f);

        //Change locations,when asset is available
        arrowUp = new SimpleAsset(Constants.CUEVA_ARROW_UP, 1080,225);
        arrowRight = new SimpleAsset(Constants.CUEVA_ARROW_RIGHT, 200,30);
        arrowLeft = new SimpleAsset(Constants.CUEVA_ARROW_LEFT, 10,30);

    }
    private void cargaPosiciones(){

    }
    private void cargarRecursos() {

        //Death by Sandd
        TextureRegion texturaCompleta = new TextureRegion(new Texture(Constants.DEATHBYROBOT));
        TextureRegion[][] texturaDeath = texturaCompleta.split(1280,720);
        animationDeath = new Animation(.15f,texturaDeath[0][0],
                texturaDeath[1][0], texturaDeath[2][0],texturaDeath[3][0]);
        animationDeath.setPlayMode(Animation.PlayMode.LOOP);
        DeathPosition = new Vector2(-2000,0);
        deathStartTime = TimeUtils.nanoTime();
        regionDeath = animationDeath.getKeyFrame(deathStartTime);

    }
    private void leerEntrada() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            public boolean touchUp(int x, int y, int pointer, int button) {
                if (playerState == PlayerState.NOTDEAD) {
                    if (gameState == GameState.PAUSE) {
                        if (botonSalirMenu.isTouched(x, y, cameraHUD,view)) {
                            game.setScreen(new LoadingScreen(LoadingScreen.ScreenSel.MENU,game));
                        }
                        if (botonPlay.isTouched(x, y, cameraHUD,view)) {
                            gameStartTime = TimeUtils.nanoTime();
                            gameState = GameState.PLAY;
                        }
                    } else {
                        if (botonPausa.isTouched(x, y, cameraHUD,view)) {
                            gameState = GameState.PAUSE;
                            moveState = MoveState.NONE;
                            player.stand();
                            leftPointer = -1;
                            rightPointer = -1;
                        }
                        if(leftPointer == pointer){
                            moveState = MoveState.NONE;
                        }
                    }
                } else {
                    DonChito.assetManager.clear();
                    game.setScreen(new LoadingScreen(LoadingScreen.ScreenSel.MENU,game));
                }
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                    if(keycode == Input.Keys.BACK){
                        dispose();
                        game.setScreen(new LoadingScreen(LoadingScreen.ScreenSel.CUEVA, game));
                    }
                return super.keyUp(keycode);
            }

            public boolean touchDown(int x, int y, int pointer, int button) {
                if(gameState == GameState.PLAY){
                    if(arrowLeft.isTouched(x,y,cameraHUD,view)){
                        moveState = MoveState.LEFT;
                        leftPointer = pointer;
                    }else if(arrowRight.isTouched(x,y,cameraHUD,view)){
                        moveState = MoveState.RIGHT;
                        leftPointer = pointer;
                    }
                    if (arrowUp.isTouched(x,y,cameraHUD,view)) {
                        rightPointer = pointer;
                        switch (player.getJumpState()) {
                            case GROUND:
                                player.startJump();
                                break;
                            case JUMPING:
                                player.continueJump();
                        }
                    }else {
                        player.endJump();
                    }
                }
                return true;
            }

            @Override
            public boolean touchDragged(int x, int y, int pointer) {
                if(leftPointer == pointer){
                    if(player.getMoveState() == DonChitoLivermorio.WalkState.WALKING){
                        if (!arrowLeft.isTouched(x, y,cameraHUD,view) && !arrowRight.isTouched(x, y,cameraHUD,view) ) {
                            moveState = MoveState.NONE;
                        }
                    }
                    if(player.getMoveState() == DonChitoLivermorio.WalkState.STANDING){
                        if(arrowLeft.isTouched(x,y,cameraHUD,view)){
                            moveState = MoveState.LEFT;
                            leftPointer = pointer;
                        }else if(arrowRight.isTouched(x,y,cameraHUD,view)){
                            moveState = MoveState.RIGHT;
                            leftPointer = pointer;
                        }
                    }
                }
                return true;
            }

        });
    }
    private void cargarAudio() {

    }


    private void realInput(){
        float delta = Gdx.graphics.getDeltaTime();

        if(gameState == GameState.PLAY){
            if(moveState == MoveState.LEFT){
                player.moveLeft(delta);
            }else if(moveState == MoveState.RIGHT){
                player.moveRight(delta);
            }else{
                player.stand();
            }
        }
        /*
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.moveLeft(delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.moveRight(delta);
        } else {
            player.stand();
        }
        */
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            switch (player.getJumpState()) {
                case GROUND:
                    player.startJump();
                    break;
                case JUMPING:
                    player.continueJump();
            }
        } else {
            player.endJump();
        }

    }


    public void checkCollisions(){
        if(DeathPosition.x + regionDeath.getRegionWidth() -200> player.getX()){
            Gdx.app.log("Death",player.getX()+"");
            playerState = PlayerState.DEAD;
        }
        float pX = player.getX();
        float pXW = pX + player.getWidth();
        float py = player.getY();
        float pyH = py + player.getHeight();

        if(!powerUp && powerUpAs.getSprite().getBoundingRectangle().contains(pX,py) ||
                powerUpAs.getSprite().getBoundingRectangle().contains(pXW,py) ||
                powerUpAs.getSprite().getBoundingRectangle().contains(pX,pyH)||
                powerUpAs.getSprite().getBoundingRectangle().contains(pXW,pyH)){
            powerUp = true;
            DonChito.preferences.putBoolean("Livermorio",true);
        }
    }

    public void renderDeath(){
        batch.setProjectionMatrix(cameraHUD.combined);
        batch.begin();
        SimpleAsset fondoDeath = new SimpleAsset(Constants.CTHULHU,0,0);
        fondoDeath.render(batch);
        batch.end();

    }

    public void renderCamera(){
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fondo.render(batch);
        fondo2.render(batch);
        for (SimpleAsset platform : platforms) {
            platform.render(batch);
        }
        if(!powerUp)powerUpAs.render(batch);
        player.render(batch);
        batch.draw(regionDeath, DeathPosition.x, DeathPosition.y);
        batch.end();
    }

    public void renderHUD(){
        batch.setProjectionMatrix(cameraHUD.combined);
        batch.begin();
        if(gameState == GameState.PAUSE){
            fondoPausa.render(batch);
            botonPlay.render(batch);
            botonConfiguracion.render(batch);
            botonSalirMenu.render(batch);
        }else{
            botonPausa.render(batch);
            arrowRight.render(batch);
            arrowLeft.render(batch);
            arrowUp.render(batch);
        }
        batch.end();
    }

    public void update(float delta){
        player.update(delta, platforms,gameState);
        regionDeath = animationDeath.getKeyFrame(MathUtils.nanoToSec * (TimeUtils.nanoTime() - deathStartTime));
        deathVelocity += delta*.01;
        DeathPosition.x += delta * DEATH_MOVE_SPEED * deathVelocity;
    }
    @Override
    public void resize(int width, int height) {
        view.update(width,height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
    private void actualizarCamara() {
        float posX = player.getX();
        if (posX>=DonChito.ANCHO_MUNDO/2) {
            camera.position.set((int) posX, camera.position.y, 0);
        } else if (posX<DonChito.ANCHO_MUNDO-DonChito.ANCHO_MUNDO/2) {
            camera.position.set(DonChito.ANCHO_MUNDO / 2, DonChito.ALTO_MUNDO / 2, 0);
        }
        camera.update();
    }
    enum PlayerState{
        DEAD,
        NOTDEAD
    }
    enum GameState{
        PLAY,
        PAUSE
    }
    enum MoveState{
        LEFT,
        RIGHT,
        NONE
    }
}
