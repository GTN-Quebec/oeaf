Ext.define( 'Proeaf.LearningOpportunityGrid', {
    extend: 'Ext.grid.Panel',
    initComponent: function (config) { 

        this.queryUrl = 'rest/queryEngine/search?';

        Ext.define('LearninOpportunityModel', {
            extend: 'Ext.data.Model',
            fields: [ 'id', 'title', 'location' ]
        });

        this.proxy = Ext.create('Ext.data.proxy.Ajax', {
            url: this.queryUrl,
            reader: {
                root: 'learningOpportunities',
                totalProperty: 'totalCount'
            }
        });

        this.loStore = Ext.create('Ext.data.JsonStore', {
            model: 'LearninOpportunityModel',
            pageSize: 20,
            proxy: this.proxy
        });

        this.loStore.on( 'load', this.updateResults, this );

        this.pageBar = Ext.create('Ext.toolbar.Paging', {
            store: this.loStore,
            displayInfo: true,
            firstText: tr('First Page'),
            prevText: tr('Previous Page'),
            nextText: tr('Next Page'),
            lastText: tr('Last Page'),
            refreshText: tr('Refresh'),
            afterPageText: tr('of {0}'),
            displayMsg: tr( 'Opportunities {0} - {1} of {2}' ),
            emptyMsg: ""
        } ),

        cfg = {
            store: this.loStore,
            columns: [ 
                { text: 'Id', width: 100,  dataIndex: 'id', hidden: true },
                { text: tr('Opportunities'), flex: 1, dataIndex: 'title', sortable: true},
                { text: tr('Location'), width: 80,  dataIndex: 'location', sortable: true}
            ],          
            viewConfig: {
                loadingText: tr('Search in progress') + '...',
                stripeRows: false
            },
            bbar: this.pageBar,
            tbar: this.tPageBar
        };
        Ext.apply(this, cfg);
        this.callParent(arguments);   
    },
    getCurrentPage: function() {
        return this.loStore.currentPage; 
    },    
    getSelected: function() {
        if( this.getSelectionModel().getSelection().length > 0 )
            return this.getSelectionModel().getSelection()[0];
        else
            return null;
    },
    getSelectedId: function() {
        var currSelectedLo = null;
        selected = this.getSelected();
        if (selected)
            currSelectedLo = selected.getData().id;
        return currSelectedLo;
    },
    clear: function() {
        this.loStore.loadRawData([]);
    },
    doQuery: function(query) { 
        this.currentQuery = query;
        this.proxy.url = this.queryUrl + '&q=' + encodeURIComponent(JSON.stringify(query)) + '&isFacetInfos=true';
        this.loStore.load();
    },
    updateResults: function() {
        this.proxy.url = this.queryUrl + '&q=' + encodeURIComponent(JSON.stringify(this.currentQuery));
        this.updateResultInfos();
        var facetInfos = this.proxy.reader.jsonData.facetInfos;
        if (facetInfos != undefined)
            manager.updateFacets(facetInfos);
    },
    updateResultInfos: function() { 
        var nbResults = this.loStore.getTotalCount();
        var label = tr( 'No opportinity found' );
        var atLeastOneResult = true;
        if (nbResults == 1)
            label = '1 ' + tr( 'opportunity found' );
        else if (nbResults > 1)
            label = nbResults + ' ' + tr( 'opportunities found' );
        else
            atLeastOneResult = false;
         
        this.columns[1].setText(tr(label) + '.');
    }    
} );


