package ch.zeeka.deetheesoft.fxgl.tutorial;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BreakoutApp extends GameApplication {

    public static final int PADDLE_WIDTH = 30;
    public static final int PADDLE_HEIGHT = 100;
    public static final int BRICK_WIDTH = 50;
    public static final int BRICK_HEIGHT = 25;
    public static final int BALL_SIZE = 20;
    public static final int PADDLE_SPEED = 5;
    public static final int BALL_SPEED = 5;

    private Entity paddle1,paddle2,ball;
    private enum BreakoutType{
        BRICK, BALL
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Breakout");
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Up 1") {
            @Override
            protected void onAction() {
                paddle1.translateY(-PADDLE_SPEED);
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down 1") {
            @Override
            protected void onAction() {
                paddle1.translateY(PADDLE_SPEED);
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Up 2") {
            @Override
            protected void onAction() {
                paddle2.translateY(-PADDLE_SPEED);
            }
        }, KeyCode.UP);

        getInput().addAction(new UserAction("Down 2") {
            @Override
            protected void onAction() {
                paddle2.translateY(PADDLE_SPEED);
            }
        }, KeyCode.DOWN);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score1", 0);
        vars.put("score2", 0);
    }

    @Override
    protected void initGame() {
        //paddle1 = spawnBat(0, getAppHeight() /2 -PADDLE_HEIGHT/2);
        //paddle2 = spawnBat(getAppWidth() -PADDLE_WIDTH, getAppHeight()/2-PADDLE_HEIGHT/2);
        //ball = spawnBall(getAppWidth()-PADDLE_WIDTH, getAppHeight()/2-BALL_SIZE/2);

        getGameWorld().addEntityFactory(new BreakoutFactory());

        paddle1 = spawn("bat", 0, getAppHeight() /2 -PADDLE_HEIGHT/2);
        paddle2 = spawn("bat", getAppWidth() -PADDLE_WIDTH, getAppHeight()/2-PADDLE_HEIGHT/2);
        ball = spawn("ball", getAppWidth()-PADDLE_WIDTH, getAppHeight()/2-BALL_SIZE/2);

        for(int i = 0; i < 10; i++)
        {
            spawn("brick", getAppWidth()/2-200-BRICK_WIDTH, 100+i*(BRICK_HEIGHT+20));
            spawn("brick", getAppWidth()/2+200, 100+i*(BRICK_HEIGHT+20));
        }
    }

//    private Entity spawnBat(double x, double y)
//    {
//        return entityBuilder().at(x,y).viewWithBBox(new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT)).buildAndAttach();
//    }
//
//    private Entity spawnBall(double x, double y)
//    {
//        return entityBuilder().at(x,y).viewWithBBox(new Rectangle(BALL_SIZE,BALL_SIZE)).with("velocity", new Point2D(BALL_SPEED,BALL_SPEED)).buildAndAttach();
//    }

    @Override
    protected void initUI() {
        Text textScore1 = getUIFactory().newText("", Color.BLACK, 22);
        Text textScore2 = getUIFactory().newText("", Color.BLACK, 22);

        textScore1.setTranslateX(10);
        textScore1.setTranslateY(50);

        textScore2.setTranslateX(getAppWidth()-30);
        textScore2.setTranslateY(50);

        textScore1.textProperty().bind(getGameState().intProperty("score1").asString());
        textScore2.textProperty().bind(getGameState().intProperty("score2").asString());

        getGameScene().addUINodes(textScore1,textScore2);
    }

    @Override
    protected void onUpdate(double tpf) {
        Point2D velocity = ball.getObject("velocity");
        ball.translate(velocity);

        if(ball.getX() == paddle1.getRightX() && ball.getY() < paddle1.getBottomY() && ball.getBottomY() > paddle1.getY()){
            ball.setProperty("velocity", new Point2D(-velocity.getX(), velocity.getY()));
        }

        if(ball.getRightX() == paddle2.getX() && ball.getY() < paddle2.getBottomY() && ball.getBottomY() > paddle2.getY()){
            ball.setProperty("velocity", new Point2D(-velocity.getX(), velocity.getY()));
        }

        if(ball.getX() <=0){
            getGameState().increment("score2", +1);
            resetBall();
        }

        if(ball.getRightX() >= getAppWidth()){
            getGameState().increment("score1", +1);
            resetBall();
        }

        if(ball.getY() <= 0){
            ball.setY(0);
            ball.setProperty("velocity", new Point2D(velocity.getX(), -velocity.getY()));
        }

        if(ball.getBottomY() >= getAppHeight()){
            ball.setY(getAppHeight()-BALL_SIZE);
            ball.setProperty("velocity", new Point2D(velocity.getX(), -velocity.getY()));
        }
    }

    private void resetBall(){
        ball.setPosition(getAppWidth()/2-BALL_SIZE/2, getAppHeight()/2-BALL_SIZE/2);
        ball.setProperty("velocity", new Point2D(BALL_SPEED, BALL_SPEED));
    }

    @Override
    protected void initPhysics() {
        onCollisionBegin(BreakoutType.BALL, BreakoutType.BRICK, (ball, brick) -> {
            brick.removeFromWorld();
            Point2D velocity = ball.getObject("velocity");

            if(FXGLMath.randomBoolean()){
                ball.setProperty("velocity", new Point2D(-velocity.getX(), velocity.getY()));
            } else {
                ball.setProperty("velocity", new Point2D(velocity.getX(), -velocity.getY()));
            }
        });
    }

    public static class BreakoutFactory implements EntityFactory {

        @Spawns("bat")
        public Entity newBat(SpawnData data){
            return entityBuilder().from(data).viewWithBBox(new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT)).build();
        }

        @Spawns("ball")
        public Entity newBall(SpawnData data){
            return entityBuilder().from(data).type(BreakoutType.BALL).viewWithBBox(new Rectangle(BALL_SIZE, BALL_SIZE, Color.BLUE)).collidable().with("velocity", new Point2D(BALL_SPEED,BALL_SPEED)).build();
        }

        @Spawns("brick")
        public Entity newBrick(SpawnData data){
            return entityBuilder().from(data).type(BreakoutType.BRICK).viewWithBBox(new Rectangle(BRICK_WIDTH, BRICK_HEIGHT, Color.RED)).collidable().build();
        }
    }
}