Ext.define( 'Proeaf.LearningOpportunity', {
    extend: 'Ext.panel.Panel',
    layout: 'vbox',
    initComponent: function (config) { 
      
        this.loGrid = Ext.create('Proeaf.ConcreteLearningOpportunityGrid', {            
            lang: this.lang,
            title: tr('Schedule'),
            cls: 'schedule',
            border: false,
            margin: '6 0 0 0',
            style: {
                borderColor: '#B5B8C8',
                borderStyle: 'solid',
                borderWidth: '1px'                
            },
            width: 400,
            height: 200
        });

        this.loGrid.on( 'itemclick', this.fetchCloDetails, this );

        this.location = Ext.create('Ext.form.FieldSet', {
            title: tr('Geographical location'),
            layout: 'fit',
            width: 500,
            height: 200
        });

        cfg = {                 
            items: [ 
               { html: '<h1>Chimie 101</h1>Prestataire:<br/> <img src="http://images.jobboom.com/logo/recrutement/fr/G118441.jpg"><br/>' +
                       'Durée: 120h<br/><br/>'
   
               },
               { layout: 'hbox', 
                 items: [ this.loGrid, { xtype: 'tbspacer', width: 40 }, this.location ]
               } ]                        
        };
        Ext.apply(this, cfg);
        this.callParent(arguments);          
    },
    init: function(glo) {        
        this.glo = glo;
        this.loGrid.init(glo);
    },
    fetchCloDetails: function(grid, record) {        
        Ext.Ajax.request( {
            url: 'rest/queryEngine/concreteLODetails?uri=' + record.data.uri,
            method: 'GET',
            success: function( response ) {
                var details = Ext.JSON.decode(response.responseText, true);
                this.setMap(details.location);
            },
            failure: function( response ) {
                Ext.Msg.alert( 'error' );
            },
            scope: this
        } );
    },
    setMap: function(location) {
        this.location.removeAll();

        if (location == undefined) 
            return;   
        
        var lat = parseFloat(location.lat);
        var long = parseFloat(location.long);
        var map = Ext.create('Ext.ux.GMapPanel', {
            margin: '2 0 10 0',  
            mapOptions: { 
                zoom: 14,
                mapTypeId: google.maps.MapTypeId.ROADMAP 
            },
            center: {
                lat: lat,
                lng: long
            },
            markers: [{
                lat: lat,
                lng: long,
                title: replaceAll( location.description, "\\\\n", "\n" )
            }]
        });
        this.location.add(map);
    },  
    clear: function() {        
        this.loGrid.clear();
        this.location.removeAll();
    }     
} );


