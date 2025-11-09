import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {

    // --------- Restaurant inner class ---------
    static class Restaurant {
        String name, cuisine; int price; boolean takeout; double rating, miles;
        Restaurant(String n,String c,int p,boolean t,double r,double m){
            name=n; cuisine=c; price=p; takeout=t; rating=r; miles=m;
        }
        String dollars(){ return "$".repeat(price); }
        public String toString(){
            String dine = takeout?"Takeout":"Dine-in";
            return String.format("%s (%s · %s · %s · ★%.1f · %.1fmi)",
                    name,cuisine,dollars(),dine,rating,miles);
        }
    }

    // --------- Falling hearts ---------
    static class Heart {
        double x,y,size,speed,opacity;
        Heart(double x,double y,double s,double sp,double o){
            this.x=x; this.y=y; this.size=s; this.speed=sp; this.opacity=o;
        }
    }

    private static class Bear {
        double x, y, dx;
        Bear(double x,double y,double dx){ this.x=x; this.y=y; this.dx=dx; }
    }

    private final Random rand = new Random();
    private final List<Heart> hearts = new ArrayList<>();
    private Bear bear;
    private long lastTime = 0;

    private final List<Restaurant> restaurants = List.of(
        new Restaurant("Viron Rondo Osteria","Italian/Mediterranean",3,false,4.1,1.0),
        new Restaurant("Il Gusto Restaurant & Wine Bar","Italian",2,false,4.6,0.8),
        new Restaurant("Butcher Bros Steakhouse","Steakhouse/American",3,false,4.4,0.9),
        new Restaurant("Rossini’s Italian Restaurant & Pizza","Italian",2,true,4.6,0.7),
        new Restaurant("Vespucci’s","Italian/Pizza",2,true,4.0,0.6),
        new Restaurant("Southside Grill","American/Grill",1,true,4.0,0.5)
        // (keep adding the rest of your restaurants here)
    );

    private final List<Restaurant> favorites = loadFavs();
    private Restaurant current;
    private Label restaurantLabel;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Date Night Dinner");

        double W=640, H=420;
        Canvas canvas = new Canvas(W,H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        initHearts(W,H);
        bear = new Bear(50,H-80,40); // starting position and speed

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) lastTime = now;
                double delta = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                update(delta, W, H);
                draw(gc, W, H);
            }
        };
        timer.start();

        Label title = new Label("Date Night Dinner 💕");
        title.setFont(Font.font("Brush Script MT", FontWeight.BOLD, 42));
        title.setTextFill(Color.web("#ff6699"));
        title.setEffect(new DropShadow(10, Color.PINK));

        restaurantLabel = new Label("Click below to choose your dinner spot!");
        restaurantLabel.setWrapText(true);
        restaurantLabel.setFont(Font.font("Arial",16));
        restaurantLabel.setStyle("-fx-text-fill:#333;");
        restaurantLabel.setAlignment(Pos.CENTER);

        Button roll = makeBtn("🎲 Pick","#ffb6c1");
        Button fav = makeBtn("❤️ Fav","#ff8fab");
        Button list = makeBtn("📋 All","#f8cdda");
        Button save = makeBtn("💾 Save","#f6a6b2");

        roll.setOnAction(e->roll());
        fav.setOnAction(e->addFav());
        list.setOnAction(e->showAll());
        save.setOnAction(e->saveFavs());

        HBox row = new HBox(10,roll,fav,list,save);
        row.setAlignment(Pos.CENTER);

        VBox box = new VBox(15,title,restaurantLabel,row);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(25));
        StackPane root = new StackPane(canvas, box);
        root.setBackground(new Background(new BackgroundFill(Color.web("#ffe6f0"),CornerRadii.EMPTY,Insets.EMPTY)));

        Scene scene = new Scene(root, W, H);
        stage.setScene(scene);
        stage.show();

        roll();
    }

    // --------- Animation updates ---------
    private void initHearts(double W,double H){
        hearts.clear();
        for (int i=0;i<80;i++){
            hearts.add(new Heart(rand.nextDouble()*W, rand.nextDouble()*H,
                    8+rand.nextDouble()*8, 20+rand.nextDouble()*40, 0.3+rand.nextDouble()*0.5));
        }
    }

    private void update(double delta,double W,double H){
        // hearts
        for (Heart h:hearts){
            h.y += h.speed * delta;
            if(h.y>H+10){ h.y=-10; h.x=rand.nextDouble()*W; }
        }

        // bear
        bear.x += bear.dx * delta;
        if(bear.x > W-80 || bear.x < 20) bear.dx = -bear.dx;
    }

    private void draw(GraphicsContext gc,double W,double H){
        // background
        gc.setFill(Color.web("#ffe6f0"));
        gc.fillRect(0,0,W,H);

        // hearts
        for (Heart h:hearts){
            gc.setFill(Color.rgb(255,182,193,(float)h.opacity));
            gc.fillOval(h.x,h.y,h.size,h.size);
        }

        // bear
        drawBear(gc,bear.x,bear.y);
    }

    private void drawBear(GraphicsContext gc,double x,double y){
        // body
        gc.setFill(Color.SADDLEBROWN);
        gc.fillOval(x, y, 60, 50);
        // head
        gc.fillOval(x+10, y-35, 40, 40);
        // ears
        gc.fillOval(x+5, y-45, 15, 15);
        gc.fillOval(x+40, y-45, 15, 15);
        // muzzle
        gc.setFill(Color.BISQUE);
        gc.fillOval(x+20, y-20, 20, 15);
        // nose
        gc.setFill(Color.SIENNA);
        gc.fillOval(x+28, y-18, 6, 6);
        // eyes
        gc.setFill(Color.BLACK);
        gc.fillOval(x+18, y-25, 5, 5);
        gc.fillOval(x+37, y-25, 5, 5);
        // heart
        gc.setFill(Color.HOTPINK);
        double hx = x+20, hy = y+5;
        gc.fillOval(hx, hy, 10, 10);
        gc.fillOval(hx+10, hy, 10, 10);
        double[] tx = {hx, hx+20, hx+10};
        double[] ty = {hy+5, hy+5, hy+20};
        gc.fillPolygon(tx, ty, 3);
    }

    // --------- UI buttons and restaurant logic ---------
    private Button makeBtn(String text,String color){
        Button b=new Button(text);
        b.setFont(Font.font("Arial",FontWeight.BOLD,15));
        b.setStyle("-fx-background-color:"+color+";-fx-text-fill:white;-fx-background-radius:12;");
        b.setOnMouseEntered(e->b.setStyle("-fx-background-color:#ff99aa;-fx-text-fill:white;-fx-background-radius:12;"));
        b.setOnMouseExited(e->b.setStyle("-fx-background-color:"+color+";-fx-text-fill:white;-fx-background-radius:12;"));
        return b;
    }

    private void roll(){
        current = restaurants.get(rand.nextInt(restaurants.size()));
        restaurantLabel.setText(current.toString());
    }

    private void addFav(){
        if(current==null)return;
        if(favorites.stream().anyMatch(r->r.name.equals(current.name))){
            alert("Already added!"); return;
        }
        favorites.add(current);
        alert("Added to favorites 💖");
    }

    private void showAll(){
        String joined = restaurants.stream()
                .sorted(Comparator.comparingDouble(r->r.miles))
                .map(Restaurant::toString)
                .collect(Collectors.joining("\n"));
        TextArea ta = new TextArea(joined);
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefWidth(560);
        ta.setPrefHeight(300);
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("All Restaurants");
        a.getDialogPane().setContent(ta);
        a.showAndWait();
    }

    private void alert(String msg){
        Alert a=new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private List<Restaurant> loadFavs(){
        File f=new File("favorites.txt");
        if(!f.exists()) return new ArrayList<>();
        try(BufferedReader br=new BufferedReader(new FileReader(f))){
            List<Restaurant> out=new ArrayList<>();
            String line;
            while((line=br.readLine())!=null){
                String[] p=line.split("\\|");
                if(p.length==6){
                    out.add(new Restaurant(p[0],p[1],Integer.parseInt(p[2]),
                            Boolean.parseBoolean(p[3]),Double.parseDouble(p[4]),Double.parseDouble(p[5])));
                }
            }
            return out;
        } catch(IOException e){ return new ArrayList<>(); }
    }

    private void saveFavs(){
        try(PrintWriter pw=new PrintWriter(new FileWriter("favorites.txt"))){
            for(Restaurant r:favorites){
                pw.printf("%s|%s|%d|%b|%.1f|%.1f%n",
                        r.name,r.cuisine,r.price,r.takeout,r.rating,r.miles);
            }
            alert("Favorites saved 💾");
        }catch(IOException e){ alert("Couldn't save"); }
    }

    public static void main(String[] args){
        System.out.println(">>> HELLO FROM THE REAL FILE <<<");
        launch();
    }
}
