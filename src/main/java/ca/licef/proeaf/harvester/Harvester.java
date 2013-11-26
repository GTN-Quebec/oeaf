package ca.licef.proeaf.harvester;

import java.util.Date;

public class Harvester implements Runnable {

    public Harvester( String name, String url, String type ) {
        this.name = name;
        this.url = url;
        this.type = type;
        this.isRunning = false;
    }

    public void run() {
        System.out.println( "Starting harvester: " + (new Date() ) );        
        try {
            Thread.sleep( 10000 );
        }
        catch( InterruptedException ignore ) {
        }
        System.out.println( "Ending harvester: " + (new Date() ) );        
        
        isRunning = false;
    }

    public void start() {
System.out.println( "start!!" );        
        if( worker == null ) {
            worker = new Thread( Harvester.this, "PROEAF Harvester" );
            worker.start();
            isRunning = true;
        }
    }

    public void stop() {
System.out.println( "stop!!" );        
        if( worker != null ) {
            worker.stop();
            isRunning = false;
        }
    }

    public boolean isRunning() {
        return( isRunning );
    }

    private Thread worker;
    private boolean isRunning;

    private String name;
    private String url;
    private String type;

}

