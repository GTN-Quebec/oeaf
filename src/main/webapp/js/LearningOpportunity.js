Ext.define( 'Proeaf.LearningOpportunity', {
    extend: 'Ext.panel.Panel',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    initComponent: function (config) { 
      
        
        cfg = {
            autoScroll: true,
            title: 'Détail de l\'offre',
            items: [ 
               { html: '<h1>Chimie 101</h1>Prestataire:<br/> <img src="http://images.jobboom.com/logo/recrutement/fr/G118441.jpg"><br/>' +
                       'Durée: 120h<br/><br/>'
   
               },
               { xtype: 'fieldset',
                  title: 'Localisation géographique',
                  margin: '0 10 10 0',
                  maxWidth: 600,
                  items: [ {
                    xtype: 'gmappanel', 
                    height: 200,
                    margin: '8 4 10 4',
                    center: {
                        geoCodeAddr: '5800 St-Denis, Montréal, QC, Canada',
                        marker: { title: 'TÉLUQ Montréal'}
                    },
                    mapOptions: { 
                        zoom: 14,
                        mapTypeId: google.maps.MapTypeId.ROADMAP 
                    }
                   } ] } ],
            buttons: [ { text: tr('Close'), handler: this.close, scope: this } ]
        };
        Ext.apply(this, cfg);
        this.callParent(arguments);          
    },
    close: function() {
        this.opener.closeLoDetails();
    }
} );


