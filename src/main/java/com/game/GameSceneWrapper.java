package com.game;

import com.main.config.Config;
import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;

public class GameSceneWrapper {

    private int dx, dy, x = 150, y = 470, projectileSpeed = 10;
    private int counter = 0, enemySpeed = 4;
    private boolean goLeft, goRight, goUp, goDown, isShooting;
    private int spawnTime = 180;

    private AnchorPane root;
    private Stage stage;
    private Scene scene;

    private Projectile playerProjectile;
    private Circle player = new Circle(x, y, 10, Color.GRAY);
    private Enemy enemy;

    private ArrayList<Projectile> projectiles = new ArrayList();
    private ArrayList<Tower> towers = new ArrayList();
    private ArrayList<Enemy> enemies = new ArrayList();

    private boolean isStopped = false;

    public GameSceneWrapper(
            Stage stage,
            AnchorPane root,
            Scene scene
    ) {
        this.stage = stage;
        this.root = root;
        this.scene = scene;

        root.getChildren().addAll(player);

        controls();
        loop();
    }

    private boolean checkIntersects(Node a, Node b) {
        return a.getBoundsInParent().intersects(b.getBoundsInParent());
    }

    private void controls() {

        scene.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();
            switch (key) {
                case LEFT:
                    goLeft = true;
                    break;
                case RIGHT:
                    goRight = true;
                    break;
                case UP:
                    goUp = true;
                    break;
                case DOWN:
                    goDown = true;
                    break;
                case SPACE:
                    if (!isShooting) {
                        playerProjectile = new Projectile(
                                new Rectangle(5, 5, Color.ORANGERED)
                        );
                        projectiles.add(playerProjectile);
                        playerProjectile.get().relocate(x + player.getRadius(), y);
                        root.getChildren().add(playerProjectile.get());
                        isShooting = true;
                        break;
                    }
                case ENTER:
                    onEnter();
                    break;
                case ESCAPE:
                    isStopped = !isStopped;
                    break;
            }

        });
        scene.setOnKeyReleased(event -> {
            KeyCode key = event.getCode();

            switch (key) {
                case LEFT:
                    goLeft = false;
                    break;
                case RIGHT:
                    goRight = false;
                    break;
                case UP:
                    goUp = false;
                    break;
                case DOWN:
                    goDown = false;
                    break;
                case SPACE:
                    isShooting = false;
                    break;
            }

        });
    }

    private void shoot() {
        for (Projectile p : projectiles) {
            Shape entity = p.get();
            entity.relocate(
                    entity.getLayoutX() + p.getDx(),
                    (entity.getLayoutY() + p.getDy())
            );
        }

        Iterator<Projectile> iterator = projectiles.iterator();
        Projectile temp;
        while (iterator.hasNext()) {
            temp = iterator.next();
            if (temp.get().getLayoutY() < root.getLayoutY()) {
                iterator.remove();
                root.getChildren().remove(temp.get());
            }
        }

    }

    private void spawnEnemy() {

        double spawnPosition = Math.random();

        int eWidth = 20;
        int eHeight = 40;
        double ex = (int) ((Config.STAGE_WIDTH - eWidth) * spawnPosition);
        int ey = (int) (root.getLayoutY());

        if (counter % spawnTime == 0) {
            enemy = new Enemy(new Rectangle(eWidth, eHeight));
//            enemy.get().relocate(ex, ey);
            enemy.getStackPane().relocate(ex, ey);
            enemies.add(enemy);
//            root.getChildren().add(enemy.get());
            root.getChildren().add(enemy.getStackPane());
        }

    }

    public void moveEnemy(int delta) {

//        for (int i = 0; i < enemies.size(); ++i) {
//            enemies.get(i).get().setY(enemies.get(i).get().getY() + delta);
//        }
        for (Enemy enemy: enemies) {
//            Shape entityT = enemy.getStackPane();
//            Rectangle entity = (Rectangle) entityT;
//            entity.setY(entity.getY() + delta);
            StackPane enetity = enemy.getStackPane();
//            enetity.setLayoutY(enetity.getLayoutY() - delta);
            enetity.setTranslateY(enetity.getTranslateY() + delta);
        }

        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            enemy = iterator.next();
            if (enemy.get().getLayoutY() < root.getLayoutY()) {
                iterator.remove();
                root.getChildren().remove(enemy.get());
            }
        }
    }

    private final long[] frameTimes = new long[100];
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;

    private void loop() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isStopped) {
                    return;
                }
                if (goLeft) {
                    dx = -5;
                }
                if (goRight) {
                    dx = 5;
                }
                if (goUp) {
                    dy = -5;
                }
                if (goDown) {
                    dy = 5;
                }
                if (!goLeft && !goRight) {
                    dx = 0;
                }
                if (!goUp && !goDown) {
                    dy = 0;
                }
                int xi = x, yi = y;
                player.relocate(x += dx, y += dy);
                if (isAvailableToMove()) {

                } else {
                    player.relocate(xi, yi);
                }
                shoot();
                counter++;
                spawnEnemy();
                moveEnemy(4);
                triggerTowerShot();
                checkHit();
            }
        };
        timer.start();
    }

    private boolean isAvailableToMove() {
        for (Tower wrapper : towers) {
            if (checkIntersects(wrapper.get(), player)) {
                return false;
            }
        }
        return true;
    }

    private void triggerTowerShot() {
        if (counter % 50 == 0) {
            for (Tower tower : towers) {
                Projectile towerProj = tower.getProjectile();
                projectiles.add(towerProj);
//                System.out.println(tower.get().getLayoutX() + " , " + tower.get().getLayoutY());
                towerProj.get().relocate(tower.get().getLayoutX(), tower.get().getLayoutY());
                root.getChildren().add(towerProj.get());
            }
        }
    }

    private void checkHit() {
        Iterator<Projectile> projectileIterator = projectiles.iterator();
        Projectile p;
        Enemy e;
        while (projectileIterator.hasNext()) {
            p = projectileIterator.next();
            Iterator<Enemy> iterator = enemies.iterator();

            while (iterator.hasNext()) {
                e = iterator.next();
                if (checkIntersects(p.get(), e.getStackPane())) {
                    int hpf = e.applyDamage(p.getDamage());
                    root.getChildren().remove(p.get());
                    if (hpf <= 0) {
                        root.getChildren().remove(e.getStackPane());
                        iterator.remove();
                    }
                    projectileIterator.remove();
                    return;
                }
            }
        }
//        for (Projectile wrapper : projectiles) {
//            Shape p = wrapper.get();
//
////            for (Enemy e : enemies) {
////                if (checkIntersects(p, e.getStackPane())) {
////                    int hpf = e.applyDamage(wrapper.getDamage());
////                    System.out.println(hpf);
////                    if (hpf <= 0) {
////                        root.getChildren().remove(e.getStackPane());
////                    }
////                }
////            }
//        }
    }

    double orgSceneX, orgSceneY;

    private void onEnter() {
        Rectangle rectangle = new Rectangle(10, 10, Color.ORANGERED);
        rectangle.setCursor(Cursor.HAND);
        rectangle.setOnMousePressed((t) -> {
            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();
            Rectangle c = (Rectangle) (t.getSource());
            c.toFront();
        });
        rectangle.setOnMouseDragged((t) -> {
            double offsetX = t.getSceneX() - orgSceneX;
            double offsetY = t.getSceneY() - orgSceneY;
            Rectangle c = (Rectangle) (t.getSource());
            c.setLayoutX(c.getLayoutX() + offsetX);
            c.setLayoutY(c.getLayoutY() + offsetY);
            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();;
        });

        Tower tower = new Tower(rectangle);
        root.getChildren().add(tower.get());
        tower.get().relocate(x, y);
        boolean isIntersect = false;
        for (Tower wrapper : towers) {
            if (checkIntersects(tower.get(), wrapper.get())) {
                isIntersect = true;
                break;
            }
        }
        if (isIntersect) {
            root.getChildren().remove(tower.get());
            tower = null;
        } else {
            towers.add(tower);
        }
    }
}
