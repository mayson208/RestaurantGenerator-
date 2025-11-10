import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

public class Main extends Application {

    private final Random rand = new Random();
    private final List<Heart> hearts = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private List<Restaurant> restaurants;

    private Canvas canvas;
    private GraphicsContext gc;
    private ImageView bear;
    private Label restaurantLabel;
    private Label ticketLabel;

    private double bearX;
    private double bearSpeed = 40;

    private int tickets = 3;
    private Restaurant current;

    private static class Heart {
        double x, y, size, speed, opacity, drift, rotation;
        Color color;

        Heart(double x, double y, double size, double speed, double opacity, Color color) {
            this.x = x; this.y = y;
            this.size = size;
            this.speed = speed;
            this.opacity = opacity;
            this.drift = -10 + Math.random()*20;
            this.rotation = Math.random()*360;
            this.color = color;
        }
    }

    private static class Particle {
        double x, y, size, vx, vy, life;
        Color color;

        Particle(double x, double y, double size, double vx, double vy, double life, Color color) {
            this.x = x; this.y = y;
            this.size = size;
            this.vx = vx; this.vy = vy;
            this.life = life;
            this.color = color;
        }
    }

    private static class Restaurant {
        String name, cuisine;
        int price;
        boolean takeout;
        double rating, miles;

        Restaurant(String n, String c, int p, boolean t, double r, double m) {
            name=n; cuisine=c; price=p; takeout=t; rating=r; miles=m;
        }

        String priceTag() { return "$".repeat(price); }

        @Override
        public String toString() {
            return name + " (" + cuisine + " · " + priceTag() + " · "
                   + (takeout ? "Takeout" : "Dine-in") + " · ★" + rating + " · " + miles + " mi)";
        }
    }

    @Override
    public void start(Stage stage) {
        double W = 640, H = 420;

        restaurants = createRestaurants();
        canvas = new Canvas(W, H);
        gc = canvas.getGraphicsContext2D();

        initHearts(W, H);
        initBear(H);

        AnimationTimer loop = new AnimationTimer() {
            long lastTime;

            @Override
            public void handle(long now) {
                if (lastTime == 0) lastTime = now;
                double dt = (now - lastTime) / 1e9;
                lastTime = now;

                drawBackground();
                updateHearts(dt, H);
                drawHearts();
                updateBear(dt, W);
            }
        };
        loop.start();

        Label title = new Label("Date Night Dinner 💕");
        title.setFont(Font.font("Brush Script MT", FontWeight.BOLD, 42));
        title.setTextFill(Color.web("#ff6699"));
        title.setEffect(new DropShadow(12, Color.PINK));

        restaurantLabel = new Label("Press Pick to choose dinner.");
        restaurantLabel.setFont(Font.font("Arial", 16));

        Button pick = new Button("🎲 Pick");
        pick.setOnAction(e -> roll(W, H));

        ticketLabel = new Label("🎟 Free Tickets: " + tickets);
        ticketLabel.setFont(Font.font("Comic Sans MS", 18));
        ticketLabel.setTextFill(Color.web("#ff4d88"));
        ticketLabel.setStyle("-fx-cursor: hand;");
        ticketLabel.setOnMouseClicked(e -> openTicketPopup(stage));

        VBox ui = new VBox(20, title, restaurantLabel, pick, ticketLabel);
        ui.setAlignment(Pos.TOP_CENTER);
        ui.setPadding(new Insets(20));

        StackPane root = new StackPane(canvas, bear, ui);
        root.setBackground(new Background(new BackgroundFill(Color.web("#ffe6f0"), CornerRadii.EMPTY, Insets.EMPTY)));

        stage.setScene(new Scene(root, W, H));
        stage.setTitle("Date Night Dinner");
        stage.show();
    }

    private void initHearts(double W, double H) {
        hearts.clear();
        for (int i = 0; i < 120; i++) {
            double size = 15 + rand.nextDouble()*20;
            double speed = 40 + rand.nextDouble()*60;
            double opacity = 0.4 + rand.nextDouble()*0.6;
            int shade = 150 + rand.nextInt(60);
            Color color = Color.rgb(255, shade, shade + 20);
            hearts.add(new Heart(Math.random()*W, Math.random()*H, size, speed, opacity, color));
        }
    }

    private void updateHearts(double dt, double H) {
        for (Heart h : hearts) {
            h.y += h.speed * dt;
            h.x += h.drift * dt;
            h.rotation += 10 * dt;

            if (h.y > H + 60) {
                h.y = -60;
                h.x = Math.random()*640;
                h.drift = -10 + Math.random()*20;
            }
        }
    }

    private void drawHeart(double x, double y, double size, double angle, Color color, double opacity) {
        gc.save();
        gc.translate(x + size/2, y + size/2);
        gc.rotate(angle);
        gc.setGlobalAlpha(opacity);
        gc.setFill(color);

        double s = size/2;
        gc.beginPath();
        gc.moveTo(0, s);
        gc.bezierCurveTo(s, -s/2, s*1.5, s/2, 0, s*1.5);
        gc.bezierCurveTo(-s*1.5, s/2, -s, -s/2, 0, s);
        gc.fill();

        gc.restore();
    }

    private void drawHearts() {
        for (Heart h : hearts) {
            drawHeart(h.x, h.y, h.size, h.rotation, h.color, h.opacity);
        }
    }

    private void drawBackground() {
        gc.setFill(Color.web("#ffe6f0"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void initBear(double H) {
        Image img = new Image("file:bear-love.gif");
        bear = new ImageView(img);
        bear.setFitWidth(150);
        bear.setPreserveRatio(true);
        bear.setTranslateY(H - 140);
    }

    private void updateBear(double dt, double W) {
        bearX += bearSpeed * dt;
        if (bearX < 0 || bearX > W - 150) bearSpeed *= -1;
        bear.setTranslateX(bearX);
    }

    private void roll(double W, double H) {
        current = restaurants.get(rand.nextInt(restaurants.size()));
        restaurantLabel.setText(current.toString());
        playReveal(W, H, current.name);
    }

    private void playReveal(double W, double H, String text) {
        particles.clear();
        createRevealParticles(W, H);

        long[] last = {System.nanoTime()};
        AnimationTimer anim = new AnimationTimer() {
            double fade = 0;

            @Override
            public void handle(long now) {
                double dt = (now - last[0]) / 1e9;
                last[0] = now;

                drawBackground();
                drawHearts();
                updateReveal(dt);

                if (fade < 1) fade += dt * 1.2;

                gc.setGlobalAlpha(fade);
                gc.setFill(Color.web("#ff6699"));
                gc.setFont(Font.font("Brush Script MT", 48));
                gc.fillText(text, W/2 - 150, H/2 + 80);
                gc.setGlobalAlpha(1);

                if (particles.isEmpty() && fade >= 1) stop();
            }
        };
        anim.start();
    }

    private void createRevealParticles(double W, double H) {
        for (int i = 0; i < 40; i++) {
            particles.add(new Particle(
                    Math.random()*W,
                    -200*Math.random()-20,
                    12 + Math.random()*20,
                    0,
                    40 + Math.random()*80,
                    1.5 + Math.random(),
                    Color.rgb(255,150+rand.nextInt(80),180)
            ));
        }

        for (int i = 0; i < 30; i++) {
            double a = Math.random()*Math.PI*2;
            double sp = 60 + Math.random()*120;
            particles.add(new Particle(
                    W/2, H/2,
                    4 + Math.random()*6,
                    Math.cos(a)*sp,
                    Math.sin(a)*sp,
                    1,
                    Color.rgb(255,220,100)
            ));
        }
    }

    private void updateReveal(double dt) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.life -= dt;

            if (p.life <= 0) it.remove();
            else {
                gc.setFill(p.color.deriveColor(0, 1, 1, p.life));
                gc.fillOval(p.x, p.y, p.size, p.size);
            }
        }
    }

    private void openTicketPopup(Stage owner) {
        Stage popup = new Stage();
        popup.initOwner(owner);
        popup.initModality(Modality.WINDOW_MODAL);

        Label title = new Label("Redeem this ticket to let your boyfriend choose your date night meal 💗");
        title.setFont(Font.font("Brush Script MT", 26));
        title.setTextFill(Color.web("#ff6699"));
        title.setWrapText(true);

        Label message = new Label("One-time use. Keep it for later or redeem it now.");
        message.setFont(Font.font("Comic Sans MS", 16));
        message.setTextFill(Color.web("#ff4d88"));

        Button use = new Button("Use Ticket ❤️");
        use.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        use.setOnAction(e -> {
            if (tickets > 0) {
                tickets--;
                ticketLabel.setText("🎟 Free Tickets: " + tickets);
            }
            popup.close();
        });

        Button cancel = new Button("Cancel");
        cancel.setFont(Font.font("Arial", 16));
        cancel.setOnAction(e -> popup.close());

        HBox buttons = new HBox(15, cancel, use);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(18, title, message, buttons);
        layout.setPadding(new Insets(24));
        layout.setAlignment(Pos.CENTER);
        layout.setBackground(new Background(
                new BackgroundFill(Color.web("#ffe6f0"), CornerRadii.EMPTY, Insets.EMPTY)
        ));

        popup.setScene(new Scene(layout, 520, 230));
        popup.showAndWait();
    }

    private List<Restaurant> createRestaurants() {
        return List.of(
                new Restaurant("Viron Rondo Osteria","Italian/Mediterranean",3,false,4.1,1.0),
                new Restaurant("Il Gusto Restaurant & Wine Bar","Italian",2,false,4.6,0.8),
                new Restaurant("Butcher Bros Steakhouse","Steakhouse/American",3,false,4.4,0.9),
                new Restaurant("Rossini’s Italian Restaurant & Pizza","Italian",2,true,4.6,0.7),
                new Restaurant("Vespucci’s","Italian/Pizza",2,true,4.0,0.6),
                new Restaurant("Southside Grill","American/Grill",1,true,4.0,0.5),
                new Restaurant("Cheshire Pizza & Ale","Pizza/Bar",2,true,4.3,1.1),
                new Restaurant("Wasabi Sushi","Japanese/Sushi",3,false,4.5,1.4),
                new Restaurant("Olive Tree","Mediterranean",2,true,4.2,1.3),
                new Restaurant("Le Petit Bistro","French",3,false,4.8,1.9),
                new Restaurant("Salsa’s","Mexican",2,true,4.3,1.6),
                new Restaurant("Green Bowl","Vegan/Vegetarian",1,true,4.2,1.7),
                new Restaurant("K-BBQ","Korean",3,false,4.6,2.0),
                new Restaurant("Ana’s Place","Breakfast/Diner",1,true,4.0,3.0),
                new Restaurant("Circle Diner","American/Diner",1,true,4.1,3.1),
                new Restaurant("Tasty Pho","Vietnamese",1,true,4.1,2.8),
                new Restaurant("Raj’s Indian Cuisine","Indian",2,true,4.3,3.7),
                new Restaurant("BBQ Junction","Barbecue",2,true,4.1,4.5),
                new Restaurant("La Villa Restaurant","Italian",2,true,4.3,4.2),
                new Restaurant("Shanghai Restaurant","Chinese",2,true,4.0,3.9),
                new Restaurant("Tiffany’s Bistro","American/Contemporary",2,false,4.2,4.0),
                new Restaurant("El Patron","Mexican",2,true,4.1,3.2),
                new Restaurant("Greek Gyro Express","Mediterranean",1,true,4.0,2.6),
                new Restaurant("Six One Nine Supper Club","American/Contemporary",3,false,4.7,4.5),
                new Restaurant("The Farm Table","Farm-to-Table/American",3,false,4.8,11.2)
        );
    }

    public static void main(String[] args) {
        launch();
    }
}
