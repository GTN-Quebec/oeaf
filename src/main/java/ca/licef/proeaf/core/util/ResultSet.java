package ca.licef.proeaf.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

public class ResultSet {

    public ResultSet() {
    }

    public int getStart() {
        return( start );
    }

    public void setStart( int start ) {
        this.start = start;
    }

    public int getLimit() {
        return( limit );
    }

    public void setLimit( int limit ) {
        this.limit = limit;
    }

    public void addEntry( Object entry ) {
        entries.add( entry );
    }

    public int getSize() {
        return( entries.size() );
    }

    public ListIterator<Object> getEntries() {
        return( entries.listIterator() );
    }

    public int getTotalRecords() {
        return( totalRecords );
    }

    public void setTotalRecords( int totalRecords ) {
        this.totalRecords = totalRecords;
    }

    public void setAdditionalData( String key, String value ) {
        additionalData.put( key, value );
    }

    public String getAdditionalData( String key ) {
        if( !additionalData.containsKey( key ) )
            return( null );
        return( additionalData.get( key ) );
    }

    public Iterator<String> getAdditionalDataKeys() {
        return( additionalData.keySet().iterator() );
    }

    public String getTitle() {
        return( title );
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getDescription() {
        return( description );
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    private String title;
    private String description;

    private int start;
    private int limit;
    private int totalRecords;
    private ArrayList<Object> entries = new ArrayList<Object>();

    private HashMap<String,String> additionalData = new HashMap<String,String>();

}
