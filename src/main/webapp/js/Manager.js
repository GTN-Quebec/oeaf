Ext.define( 'Proeaf.Manager', {
    extend: 'Ext.panel.Panel',
    layout: 'border',
    initComponent: function( config ) {
        
        this.facets = Ext.create('Proeaf.Facets', {
            title: 'Critères',
            region: 'west',            
            width: 270,
            margin: '10 0 10 10'
        });

        this.loGrid = Ext.create('Proeaf.LearningOpportunityGrid', {
            border: false
        });
        
        this.loGrid.on( 'itemdblclick', this.openLoDetails, this );

        this.loDetail = Ext.create('Proeaf.LearningOpportunity', {
            border: false
        });

        this.contentPanel = Ext.create('Ext.panel.Panel', {
            id: 'contentPanel',
            layout: 'card',
            region: 'center',
            margin: '10 10 10 10',
            border: false,
            items: [ this.loGrid, this.loDetail ]
        });

        cfg = {
            items: [ this.facets, this.contentPanel ]
        };
        
        Ext.apply(this, cfg);
        this.callParent(arguments); 
    },
    openLoDetails: function() {
        this.contentPanel.getLayout().setActiveItem(this.loDetail);
        this.setFacetsVisible(false);  
    },
    closeLoDetails: function() {
        this.contentPanel.getLayout().setActiveItem(this.loGrid);
        this.setFacetsVisible(true);
    },
    setFacetsVisible: function(visible) {
        this.facets.setVisible(visible);
    },
    doQuery: function(query) {
        this.loGrid.doQuery(query);
    },
    updateFacets: function(facetInfos) {
        this.facets.updateFacets(facetInfos);
    }
});

Ext.onReady( function() {

    manager = Ext.create('Proeaf.Manager', {
        region: 'center',
        border: false
    });

    new Ext.Viewport( {
        layout: 'fit',   
        items: {
            layout: 'border',
            border: false,
            items: [ { html: '<font size="5"><b>Prototype de mise en œuvre du profil OÉAF</b></font> (version 0.1)', 
                       margin: '5 0 0 10', region: 'north' }, 
                     manager ]
        }
    } );

} );
