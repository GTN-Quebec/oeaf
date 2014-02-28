Ext.define( 'Proeaf.SearchManager', {
    extend: 'Ext.panel.Panel',
    layout: 'border',
    initComponent: function( config ) {
        
        // By default, the interface is displayed in English.
        this.lang = ( Ext.getUrlParam( 'lang' ) || 'en' );

        this.facets = Ext.create('Proeaf.Facets', {
            title: tr('Filters'),
            lang: this.lang,
            region: 'west',                         
            width: 360,
            margin: '10 0 10 10',
            resizable: true,
            resizeHandles: 'e' 
        });

        this.loGrid = Ext.create('Proeaf.GenericLearningOpportunityGrid', {
            border: false,
            lang: this.lang
        });
        
        this.loGrid.on( 'itemdblclick', this.openLoDetails, this );

        this.loDetail = Ext.create('Proeaf.LearningOpportunity', { 
            title: tr('Offer\'s detail'),
            lang: this.lang,
            buttons: [ { text: tr('Close'), handler: this.closeLoDetails, scope: this } ],
            overflowY: 'auto'
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
    openLoDetails: function(grid, record) {
        this.loDetail.init(record.getData().id);
        this.contentPanel.getLayout().setActiveItem(this.loDetail);
        this.setFacetsVisible(false);  
    },
    closeLoDetails: function() {
        this.loDetail.clear();
        this.contentPanel.getLayout().setActiveItem(this.loGrid);
        this.setFacetsVisible(true);
    },
    setFacetsVisible: function(visible) {
        this.facets.setVisible(visible);
    },
    doQuery: function(query) {
        this.loGrid.doQuery(query);
    },
    updateFacets: function(facetInfos, isClear) {
        this.facets.updateFacets(facetInfos, isClear);
    }
});

