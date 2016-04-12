package mx.itesm.donchito;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Esteban on 2/16/2016.
 */
public class FlevorioSays implements Screen{
    private OrthographicCamera camera;
    private final DonChito game;
    private static final float INCREMENTO = 0.6f;
    private Viewport view;

    private boolean rocasCreadas = false;
    private boolean brillando = true;
    private boolean perdio = false;
    private boolean salirMenu = false;

    private GameText instructionsTxt;
    private GameText levelTxt;

    private SimpleAsset botonPausa;
    private SimpleAsset botonPlay;
    private SimpleAsset botonSalirMenu;
    private SimpleAsset botonConfiguracion;

    private SimpleAsset fondoPantalla;
    private SimpleAsset fondoPausa;


    private State estado = State.PLAY;

    private float tiempoEsperar = 1f;
    private boolean reseted = false;
    private int shadowedAndUp = 0;

    private int lastRockPressed = 0;
    private int nivel = 0;
    private int indiceSecuencia = 0;

    private SpriteBatch batch;

    private SimpleAsset fondo;

    private Array<SimpleAsset> rocas;



    private int[] combinaciones = new int[]{0,0,0,0,0,0,0,0,0,0};
    private boolean[] combinacionesPR = new boolean[]{false,false,false,false,false,false,false,false,false,false};

    private Music efectoBoton;
    private Music efectoGanar;
    private Music efectoPerder;
    private Music musicaFondo;
    private Music musicaIntro;


    public FlevorioSays(DonChito game) {
        this.game = game;
    }

    @Override
    public void show() {
        init();
        camera = new OrthographicCamera(DonChito.ANCHO_MUNDO,DonChito.ALTO_MUNDO);
        camera.position.set(DonChito.ANCHO_MUNDO / 2, DonChito.ALTO_MUNDO / 2, 0);
        camera.update();

        view = new StretchViewport(DonChito.ANCHO_MUNDO,DonChito.ALTO_MUNDO,camera);

        leerEntrada();
        cargarRecursos();

        musicaFondo.setLooping(true);
        musicaIntro.play();

        //TODO Refactor next code into an Asset Manager
        batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);
        fondoPantalla = new SimpleAsset(Constants.FLEVORIO_FONDOPANTALLA_PNG,0,0);
        fondo = new SimpleAsset(Constants.FLEVORIO_FONDO_PNG,0,0);
        fondo.getSprite().scale(1);
        fondo.getSprite().setScale(0.1f);

        fondoPausa = new SimpleAsset(Constants.GLOBAL_MENU_PAUSA_PNG,0,0);
        botonPlay = new SimpleAsset(Constants.GLOBAL_BOTON_PLAY_PNG,1050,10);
        botonConfiguracion = new SimpleAsset(Constants.GLOBAL_BOTON_CONFIGURACION_PNG,405,175);
        botonSalirMenu = new SimpleAsset(Constants.GLOBAL_BOTON_SALIRMENU_PNG,405,425);

        instructionsTxt = new GameText(150,700);
        levelTxt = new GameText(1000,700);

        botonPausa = new SimpleAsset(Constants.GLOBAL_BOTON_PAUSA_PNG,1050,10);
    }
    private void init() {
        nivel = 0;
        indiceSecuencia = 0;
    }

    private void cargarRecursos() {
        AssetManager assetManager = DonChito.assetManager;
        efectoBoton = assetManager.get(Constants.FLEVORIO_SONIDOBOTON_WAV);
        efectoGanar = assetManager.get(Constants.FLEVORIO_SONIDOVICTORY_WAV);
        efectoPerder = assetManager.get(Constants.FLEVORIO_SONIDOFAIL_WAV);

        musicaFondo = assetManager.get(Constants.FLEVORIO_MUSICAFONDO_WAV);
        musicaIntro = assetManager.get(Constants.FLEVORIO_MUSICAINTRO_WAV);

    }

    private void crearRoca() {
        rocasCreadas = true;
        SimpleAsset nuevo;

        rocas = new Array<SimpleAsset>(3);

        nuevo = new SimpleAsset(Constants.FLEVORIO_BOTONCENTRAL_PNG,505,250);
        nuevo.getSprite().setScale(1.1f);
        rocas.add(nuevo);

        nuevo = new SimpleAsset(Constants.FLEVORIO_BOTON2_PNG,370,365);
        rocas.add(nuevo);
        nuevo = new SimpleAsset(Constants.FLEVORIO_BOTON2_PNG,370,135);
        nuevo.setRotation(180f);
        rocas.add(nuevo);

        nuevo = new SimpleAsset(Constants.FLEVORIO_BOTON3_PNG,253,370);
        nuevo.getSprite().setScale(0.98f);
        rocas.add(nuevo);
        nuevo = new SimpleAsset(Constants.FLEVORIO_BOTON3_PNG,253,18);
        nuevo.setRotation(180f);
        nuevo.getSprite().setScale(0.98f);
        rocas.add(nuevo);
    }
    public void stopMusic(){
        if(musicaFondo.isPlaying()){
            musicaFondo.stop();
        }
        if(musicaIntro.isPlaying()){
            musicaIntro.stop();
        }
        if(efectoBoton.isPlaying()){
            efectoBoton.stop();
        }
        if(efectoGanar.isPlaying()){
            efectoGanar.stop();
        }
        if(efectoPerder.isPlaying()){
            efectoPerder.stop();
        }
    }

    @Override
    public void render(float delta) {
        camera.update();
        view.apply();
        batch.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(indiceSecuencia == nivel*2){
            //correr animacion de SUCCEES!! NEXT LVL...
            lastRockPressed = 0;
            reseted = false;
            nivel++;
            if(nivel == 4){
                stopMusic();
                game.setScreen(new LoadingScreen(LoadingScreen.ScreenSel.MENU,game));
            }
            crearCombinacion(nivel);
            indiceSecuencia = 0;
            if(nivel != 1 && !perdio){
                efectoBoton.stop();
                efectoGanar.play();
                perdio = false;
            }
        }
        fondoPantalla.render(batch);
        fondo.render(batch);
        if(nivel != 4) {
            if (fondo.getSprite().getScaleX() >= 1f) {
                if (!rocasCreadas) {
                    // Sonido que se reproduce al abrir el proyector
                    efectoBoton.play();
                    crearRoca();
                } else {
                    if (!musicaIntro.isPlaying()) {
                        levelTxt.showMessage(batch,"Nivel : "+nivel);
                        if (!musicaFondo.isPlaying()) {
                            musicaFondo.play();
                        }
                        for (SimpleAsset roca : rocas) {
                            roca.render(batch);
                        }
                        if (!efectoGanar.isPlaying() && !efectoPerder.isPlaying()) {
                            if(!reseted) {
                                for (SimpleAsset roca : rocas) {
                                    roca.getSprite().setColor(Color.WHITE);
                                    roca.render(batch);
                                }
                                reseted = true;
                            }
                            for (int i = 0; i < nivel * 2; i++) {
                                if(brillando){instructionsTxt.showMessage(batch,"PENSANDO");}
                                if (!combinacionesPR[i]) {
                                    if (!efectoBoton.isPlaying()) {
                                        efectoBoton.play();
                                    }
                                    rocas.get(combinaciones[i] - 1).getSprite().setColor(103, 128, 150, 1);
                                    if (esperar(delta)) {
                                        rocas.get(combinaciones[i] - 1).getSprite().setColor(Color.WHITE);
                                        combinacionesPR[i] = true;
                                        efectoBoton.stop();
                                    }
                                    break;
                                }
                            }
                            if(!brillando){
                                instructionsTxt.showMessage(batch,"JUEGA");
                            }
                            brillando = false;
                            for (int i = 0; i < nivel * 2; i++) {
                                if (!combinacionesPR[i]) {
                                    brillando = true;
                                    break;
                                }
                            }
                        }
                    }
                    else {
                        instructionsTxt.showMessage(batch,"ESPERA");
                    }
                }

            } else {
                fondo.getSprite().setScale(MathUtils.clamp(delta * INCREMENTO + fondo.getSprite().getScaleX(), .1f, 1f));
            }
        }
        if(estado == State.PAUSA){
            fondoPausa.render(batch);
            botonPlay.render(batch);
            botonConfiguracion.render(batch);
            botonSalirMenu.render(batch);
            if(salirMenu) {
                stopMusic();
                game.setScreen(new LoadingScreen(LoadingScreen.ScreenSel.MENU,game));
            }
        }
        else {
            botonPausa.render(batch);
        }
        batch.end();
    }

    private boolean esperar(float delta){
        if(tiempoEsperar <=0){
            tiempoEsperar = 1f;
            return true;
        }
        tiempoEsperar -= delta;
        return false;
    }

    private void crearCombinacion(int nivel) {
        for(int i=0;i<combinaciones.length;i++){
            combinaciones[i] = 0;
            combinacionesPR[i] = false;
        }
        for(int i=0;i<nivel*2;i++){
            combinaciones[i] = MathUtils.random(1,5);
        }
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
        DonChito.assetManager.clear();
    }
    private void leerEntrada() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown (int x, int y, int pointexr, int button) {
                Vector3 coord = camera.unproject(new Vector3(x,y,0));
                Gdx.app.log("coord: ",coord.toString());
                Gdx.app.log("screen:",Gdx.graphics.getWidth() + " " + Gdx.graphics.getHeight());
                Gdx.app.log("screen:",x + " " + y);
                //Basarlo todo en coord
                if(!brillando){
                    int roca = 0;
                    float distancia = (float) Math.sqrt(Math.pow(625-x,2)+Math.pow(348-y,2));
                    if(distancia>=0 && distancia<= 164){
                        roca = 1;
                    }
                    if(distancia>=165 && distancia<= 269){
                        if(y<358){
                            roca = 2;
                        }
                        else{
                            roca = 3;
                        }
                    }
                    if(distancia>=270 && distancia<= 396){
                        if(y<358){
                            roca = 4;
                        }
                        else{
                            roca = 5;
                        }
                    }
                    if(roca !=0 && estado == State.PLAY){
                        rocas.get(roca-1).getSprite().setColor(Color.GRAY);
                    }
                    shadowedAndUp = roca;
                }
                if(estado == State.PAUSA){
                    //detectar los botones en el menu de pausa.
                    if(botonSalirMenu.isTouched(x,y,camera)){
                        init();
                        musicaFondo.setLooping(false);
                        if(musicaFondo.isPlaying()) {
                            musicaFondo.stop();
                        }
                        salirMenu = true;
                    }
                }
                return true; // return true to indicate the event was handled
            }

            @Override
            public boolean touchUp (int x, int y, int pointer, int button) {
                if(!brillando){
                    for (SimpleAsset roca : rocas) {
                        roca.getSprite().setColor(Color.WHITE);
                    }
                    int roca = 0;
                    float distancia = (float) Math.sqrt(Math.pow(625-x,2)+Math.pow(348-y,2));
                    if(distancia>=0 && distancia<= 164){
                        roca = 1;
                    }
                    if(distancia>=165 && distancia<= 269){
                        if(y<358){
                            roca = 2;
                        }
                        else{
                            roca = 3;
                        }
                    }
                    if(distancia>=270 && distancia<= 396){
                        if(y<358){
                            roca = 4;
                        }
                        else{
                            roca = 5;
                        }
                    }
                    if(roca !=0 && estado == State.PLAY && shadowedAndUp == roca){
                        efectoBoton.stop();
                        efectoBoton.play();
                        if(roca == combinaciones[indiceSecuencia]){
                            if(lastRockPressed != 0){
                                rocas.get(lastRockPressed-1).getSprite().setColor(Color.WHITE);
                            }
                            rocas.get(roca-1).getSprite().setColor(103, 128, 150, 1);
                            indiceSecuencia++;
                            perdio = false;
                            lastRockPressed = roca;
                        }
                        else{
                            rocas.get(roca-1).getSprite().setColor(Color.RED);
                            nivel --;
                            perdio = true;
                            indiceSecuencia = nivel*2;
                            brillando = true;
                            efectoBoton.stop();
                            efectoPerder.play();
                            reseted = false;
                        }
                    }
                    else{
                        if(botonPausa.isTouched(x,y,camera)||botonPlay.isTouched(x,y,camera)){
                            if(estado == State.PLAY){
                                estado = State.PAUSA;
                            }
                            else{
                                estado = State.PLAY;
                            }
                        }
                    }
                }
                if(estado == State.PAUSA){
                    //detectar los botones en el menu de pausa.
                    if(botonSalirMenu.isTouched(x,y,camera)){
                        init();
                        musicaFondo.setLooping(false);
                        if(musicaFondo.isPlaying()) {
                            musicaFondo.stop();
                        }
                        salirMenu = true;
                    }
                }
                return true; // return true to indicate the event was handled
            }
        });
    }
    public enum State
    {
        PAUSA,
        PLAY
    }
}