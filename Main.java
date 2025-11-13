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

import java.time.LocalDate;
import java.util.*;

public class Main extends Application {

    private final boolean smoothMode = true;   // toggle smoother frame pacing
    private final Random rand = new Random();
    private final List<Heart> hearts = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private List<Restaurant> restaurants;

    private Canvas canvas;
    private GraphicsContext gc;
    private ImageView bear;
    private Label restaurantLabel;
    private Label ticketLabel;

    private double bearX, bearSpeed = 40;
    private int tickets = 3;
    private Restaurant current;

    private static class Heart {
        double x, y, size, speed, opacity, drift, rotation;
        Color color;
        Heart(double x,double y,double s,double sp,double op,Color c){
            x=x; this.x=x; this.y=y; size=s; speed=sp; opacity=op;
            drift=-10+Math.random()*20; rotation=Math.random()*360; color=c;
        }
    }

    private static class Particle {
        double x,y,size,vx,vy,life; Color color;
        Particle(double x,double y,double s,double vx,double vy,double life,Color c){
            this.x=x;this.y=y;this.size=s;this.vx=vx;this.vy=vy;this.life=life;this.color=c;
        }
    }

    private static class Restaurant {
        String name,cuisine; int price; boolean takeout; double rating,miles;
        Restaurant(String n,String c,int p,boolean t,double r,double m){
            name=n;cuisine=c;price=p;takeout=t;rating=r;miles=m;
        }
        String priceTag(){return "$".repeat(price);}
        public String toString(){return name+" ("+cuisine+" · "+priceTag()+" · "+
            (takeout?"Takeout":"Dine-in")+" · ★"+rating+" · "+miles+" mi)";}
    }

    @Override public void start(Stage stage){
        stage.getIcons().add(new Image("file:ramen.png"));
        double W=640,H=420;
        LocalDate d=LocalDate.now();
        if(d.getMonthValue()==11&&(d.getDayOfMonth()==10||d.getDayOfMonth()==12))
            playBirthdayIntro(stage,W,H);
        else showMainApp(stage,W,H);
    }

    private void playBirthdayIntro(Stage stage,double W,double H){
        Canvas c=new Canvas(W,H); GraphicsContext g=c.getGraphicsContext2D();
        StackPane root=new StackPane(c);
        root.setBackground(new Background(new BackgroundFill(Color.web("#ffe6f0"),CornerRadii.EMPTY,Insets.EMPTY)));
        stage.setScene(new Scene(root,W,H)); stage.setTitle("Date Night Dinner"); stage.show();

        List<Heart> hlist=new ArrayList<>();
        for(int i=0;i<80;i++){
            double s=15+rand.nextDouble()*25, sp=25+rand.nextDouble()*35;
            double op=0.5+rand.nextDouble()*0.5; int shade=150+rand.nextInt(60);
            Color col=Color.rgb(255,shade,shade+20);
            hlist.add(new Heart(rand.nextDouble()*W,rand.nextDouble()*H,s,sp,op,col));
        }

        final double duration=8.0; long[] last={System.nanoTime()}; double[] t={0};
        AnimationTimer timer=new AnimationTimer(){
            private double frameDelay=1e9/60,acc=0;
            @Override public void handle(long now){
                double dt=(now-last[0])/1e9; last[0]=now; acc+=dt;
                if(smoothMode&&acc<1.0/60)return; if(smoothMode)acc=0;
                t[0]+=dt; g.setFill(Color.web("#ffe6f0")); g.fillRect(0,0,W,H);

                for(Heart h:hlist){
                    h.y-=h.speed*dt*0.4; h.rotation+=10*dt;
                    if(h.y<-60){h.y=H+60;h.x=rand.nextDouble()*W;}
                    drawHeart(g,h.x,h.y,h.size,h.rotation,h.color,h.opacity);
                }

                double fadeIn=Math.min(1,Math.pow(t[0]/2.0,0.8));
                double fadeOut=Math.max(0,1-Math.pow(Math.max(0,t[0]-6.0)/2.0,0.8));
                double alpha=Math.min(fadeIn,fadeOut);
                g.setGlobalAlpha(alpha);
                g.setFill(Color.web("#ff6699"));
                g.setFont(Font.font("Brush Script MT",FontWeight.BOLD,48));
                g.setEffect(new DropShadow(20,Color.PINK));
                g.fillText("🎉 Happy Birthday Rachel 💕",W/2-230,H/2);
                g.setGlobalAlpha(1);
                if(t[0]>=duration){stop();showMainApp(stage,W,H);}
            }};
        timer.start();
    }

    private void showMainApp(Stage stage,double W,double H){
        restaurants=createRestaurants(); canvas=new Canvas(W,H); gc=canvas.getGraphicsContext2D();
        initHearts(W,H); initBear(H);

        AnimationTimer loop=new AnimationTimer(){
            long last; double acc;
            @Override public void handle(long now){
                if(last==0)last=now; double dt=(now-last)/1e9; last=now; acc+=dt;
                if(smoothMode&&acc<1.0/60)return; if(smoothMode)acc=0;
                drawBackground(); updateHearts(dt,H); drawHearts(); updateBear(dt,W);
            }};
        loop.start();

        Label title=new Label("Date Night Dinner 💕");
        title.setFont(Font.font("Brush Script MT",FontWeight.BOLD,42));
        title.setTextFill(Color.web("#ff6699")); title.setEffect(new DropShadow(12,Color.PINK));
        restaurantLabel=new Label("Press Pick to choose dinner."); restaurantLabel.setFont(Font.font("Arial",16));
        Button pick=new Button("🎲 Pick"); pick.setOnAction(e->roll());
        ticketLabel=new Label("🎟 Free Tickets: "+tickets);
        ticketLabel.setFont(Font.font("Comic Sans MS",18));
        ticketLabel.setTextFill(Color.web("#ff4d88"));
        ticketLabel.setStyle("-fx-cursor:hand;");
        ticketLabel.setOnMouseClicked(e->openTicketPopup(stage));

        VBox ui=new VBox(20,title,restaurantLabel,pick,ticketLabel);
        ui.setAlignment(Pos.TOP_CENTER); ui.setPadding(new Insets(20));
        StackPane root=new StackPane(canvas,bear,ui);
        root.setBackground(new Background(new BackgroundFill(Color.web("#ffe6f0"),CornerRadii.EMPTY,Insets.EMPTY)));
        stage.setScene(new Scene(root,W,H)); stage.show();
    }

    private void initHearts(double W,double H){
        hearts.clear();
        for(int i=0;i<120;i++){
            double s=15+rand.nextDouble()*20, sp=30+rand.nextDouble()*40;
            double op=0.4+rand.nextDouble()*0.6; int shade=150+rand.nextInt(60);
            Color col=Color.rgb(255,shade,shade+20);
            hearts.add(new Heart(rand.nextDouble()*W,rand.nextDouble()*H,s,sp,op,col));
        }
    }

    private void updateHearts(double dt,double H){
        for(Heart h:hearts){
            h.y+=h.speed*dt; h.x+=h.drift*dt*0.3; h.rotation+=10*dt;
            if(h.y>H+60){h.y=-60;h.x=Math.random()*640;h.drift=-10+Math.random()*20;}
        }
    }

    private void drawHeart(GraphicsContext g,double x,double y,double s,double a,Color c,double op){
        g.save(); g.translate(x+s/2,y+s/2); g.rotate(a); g.setGlobalAlpha(op); g.setFill(c);
        double r=s/2;
        g.beginPath(); g.moveTo(0,r);
        g.bezierCurveTo(r,-r/2,r*1.5,r/2,0,r*1.5);
        g.bezierCurveTo(-r*1.5,r/2,-r,-r/2,0,r);
        g.fill(); g.restore();
    }

    private void drawHearts(){for(Heart h:hearts)drawHeart(gc,h.x,h.y,h.size,h.rotation,h.color,h.opacity);}
    private void drawBackground(){gc.setFill(Color.web("#ffe6f0"));gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());}

    private void initBear(double H){
        Image img=new Image("file:bear-love.gif"); bear=new ImageView(img);
        bear.setFitWidth(150); bear.setPreserveRatio(true); bear.setTranslateY(H-140);
    }

    private void updateBear(double dt,double W){
        double targetSpeed=smoothMode?bearSpeed*0.6:bearSpeed;
        bearX+=targetSpeed*dt;
        if(bearX<0||bearX>W-150)bearSpeed*=-1;
        bear.setTranslateX(bearX);
    }

    private void roll(){
        current=restaurants.get(rand.nextInt(restaurants.size()));
        restaurantLabel.setText(current.toString());
    }

    private void openTicketPopup(Stage owner){
        Stage p=new Stage(); p.initOwner(owner); p.initModality(Modality.WINDOW_MODAL);
        Label t=new Label("Redeem this ticket to let your boyfriend choose your date night meal 💗");
        t.setFont(Font.font("Brush Script MT",26)); t.setTextFill(Color.web("#ff6699")); t.setWrapText(true);
        Label m=new Label("One-time use. Keep it for later or redeem it now.");
        m.setFont(Font.font("Comic Sans MS",16)); m.setTextFill(Color.web("#ff4d88"));
        Button use=new Button("Use Ticket ❤️");
        use.setFont(Font.font("Arial",FontWeight.BOLD,16));
        use.setOnAction(e->{if(tickets>0){tickets--;ticketLabel.setText("🎟 Free Tickets: "+tickets);}p.close();});
        Button cancel=new Button("Cancel"); cancel.setFont(Font.font("Arial",16)); cancel.setOnAction(e->p.close());
        HBox b=new HBox(15,cancel,use); b.setAlignment(Pos.CENTER);
        VBox lay=new VBox(18,t,m,b);
        lay.setPadding(new Insets(24)); lay.setAlignment(Pos.CENTER);
        lay.setBackground(new Background(new BackgroundFill(Color.web("#ffe6f0"),CornerRadii.EMPTY,Insets.EMPTY)));
        p.setScene(new Scene(lay,520,230)); p.showAndWait();
    }

    private List<Restaurant> createRestaurants(){
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
            new Restaurant("Fire at the Ridge", "American/Steakhouse", 3, false, 4.7, 5.8),
new Restaurant("Aria Banquet Hall & Restaurant", "Italian", 3, false, 4.5, 3.9),
new Restaurant("ION Restaurant", "Vegan/Organic", 2, false, 4.8, 8.4),
new Restaurant("Max Pizza II", "Pizza/Italian", 1, true, 4.3, 2.1),
new Restaurant("The Breakfast Nook", "Breakfast/American", 1, true, 4.4, 1.5),
new Restaurant("Sakimura Japanese Fusion", "Japanese/Sushi", 3, false, 4.6, 6.3),
new Restaurant("Rubber Avenue Grill", "American/Bar", 2, true, 4.2, 4.8),
new Restaurant("Los Mariachis", "Mexican", 2, true, 4.3, 5.2),
new Restaurant("Tavern 1757", "American/Bar & Grill", 3, false, 4.4, 7.1),
new Restaurant("Lucca’s", "Italian", 3, false, 4.7, 5.0),
new Restaurant("Pho 501", "Vietnamese", 2, true, 4.6, 9.7),
new Restaurant("Kumo Japanese Restaurant", "Japanese", 2, true, 4.3, 6.5),
new Restaurant("Viron Rondo Express", "Italian", 2, true, 4.4, 2.4),
new Restaurant("The Cue Barbecue", "Barbecue/American", 2, true, 4.5, 10.8),
new Restaurant("The Owl Wine & Food Bar", "Wine Bar/Small Plates", 3, false, 4.8, 8.0),
new Restaurant("Wood-n-Tap", "American/Bar", 2, true, 4.2, 6.7),
new Restaurant("Arethusa al tavolo", "Fine Dining/American", 4, false, 4.9, 15.4),
new Restaurant("Olea Mediterranean Taverna", "Mediterranean", 3, false, 4.8, 12.2),
new Restaurant("Union League Cafe", "French/Fine Dining", 4, false, 4.9, 14.5),
new Restaurant("Prime 16", "American/Burgers", 2, true, 4.5, 13.1),
        );
    }

    public static void main(String[] a){launch();}
}
