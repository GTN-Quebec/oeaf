Ext.define( 'Proeaf.Content', {
    extend: 'Ext.panel.Panel',
    layout: 'border',
    initComponent: function( config ) {
        
        this.criterias = Ext.create('Proeaf.Facets', {
            title: 'Critères',
            region: 'west',            
            width: 270,
            margin: 10
        });

        this.loGrid = Ext.create('Proeaf.LearningOpportunityGrid', {
            border: false
        });
        
        this.loGrid.on( 'itemdblclick', this.openLoDetails, this );

        this.loDetail = Ext.create('Proeaf.LearningOpportunity', {
            border: false,
            opener: this
        });

        this.contentPanel = Ext.create('Ext.panel.Panel', {
            id: 'contentPanel',
            layout: 'card',
            region: 'center',
            margin: '10 10 10 0',
            border: false,
            items: [ this.loGrid, this.loDetail ]
        });

        cfg = {
            items: [ this.criterias, this.contentPanel ]
        };
        
        Ext.apply(this, cfg);
        this.callParent(arguments); 
    },
    openLoDetails: function() {
        this.contentPanel.getLayout().setActiveItem(this.loDetail);
    },
    closeLoDetails: function() {
        this.contentPanel.getLayout().setActiveItem(this.loGrid);
    }
});

Ext.onReady( function() {

    var content = Ext.create('Proeaf.Content', {
        border: false,
        region: 'center'
    });

    new Ext.Viewport( {
        layout: 'fit',   
        items: {
            layout: 'border',
            border: false,
            items: [ { html: '<font size="5"><b>Prototype de mise en œuvre du profil OÉAF</b></font> (version 0.1)', 
                       margin: '5 0 0 10', region: 'north' }, 
                     content ]
        }
    } );

} );
