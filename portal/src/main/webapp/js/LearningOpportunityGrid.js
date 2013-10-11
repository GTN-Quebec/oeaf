Ext.define( 'Proeaf.LearningOpportunityGrid', {
    extend: 'Ext.grid.Panel',
    initComponent: function (config) { 

        this.queryUrl = queryEngineUrl + '/rest/searchJson?lang=' + this.lang;

        Ext.define('LearninOpportunityModel', {
            extend: 'Ext.data.Model',
            fields: [ 'id', 'title', 'location', 'date' ]
        });

        this.proxy = Ext.create('Ext.data.proxy.Ajax', {
            url: this.queryUrl,
            reader: {
                root: 'learningOpportunities',
                totalProperty: 'totalCount'
            }
        });

        var myData = [
                { 'id': 'lo-1', "title":"ADM3300", "location":"UQAM", "date":"2013-11-20" },
                { 'id': 'lo-2', "title":"INF6200", "location":"TELUQ", "date":"2013-10-17" },
                { 'id': 'lo-3', "title":"INF5100", "location":"TELUQ", "date":"2013-12-05" },
                { 'id': 'lo-4', "title":"ADM3300", "location":"UQAM", "date":"2013-11-20" },
                { 'id': 'lo-5', "title":"INF6200", "location":"TELUQ", "date":"2013-10-17" },
                { 'id': 'lo-6', "title":"INF5100", "location":"TELUQ", "date":"2013-12-05" },
                { 'id': 'lo-7', "title":"ADM3300", "location":"UQAM", "date":"2013-11-20" },
                { 'id': 'lo-8', "title":"INF6200", "location":"TELUQ", "date":"2013-10-17" },
                { 'id': 'lo-9', "title":"INF5100", "location":"TELUQ", "date":"2013-12-05" }
            ];

        this.loStore = Ext.create('Ext.data.JsonStore', {
            model: 'LearninOpportunityModel',
            pageSize: 20,
            proxy: this.proxy,
            data: myData
        });

        this.pageBar = Ext.create('Ext.toolbar.Paging', {
            store: this.loStore,
            displayInfo: true,
            firstText: tr('First Page'),
            prevText: tr('Previous Page'),
            nextText: tr('Next Page'),
            lastText: tr('Last Page'),
            refreshText: tr('Refresh'),
            afterPageText: tr('of {0}'),
            displayMsg: tr( 'Offers {0} - {1} of {2}' ),
            emptyMsg: tr( "No resource available" )
        } ),

        cfg = {
            store: this.loStore,
            columns: [ 
                { text: 'Id', width: 100,  dataIndex: 'id', hidden: true },
                { text: 'Title', flex: 1, dataIndex: 'title', sortable: true},
                { text: 'Location', width: 80,  dataIndex: 'location', sortable: true},
                { text: 'Date', width: 80,  dataIndex: 'date', sortable: true}
            ],          
            viewConfig: {
                loadingText: tr('Loading') + '...',
                stripeRows: false
            },
            bbar: this.pageBar
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
    }
} );


