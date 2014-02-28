Ext.define( 'Proeaf.GenericLearningOpportunityGrid', {
    extend: 'Ext.grid.Panel',
    initComponent: function (config) { 

        this.queryUrl = 'rest/queryEngine/search?';

        Ext.define('GenericLearninOpportunityModel', {
            extend: 'Ext.data.Model',
            fields: [ 'id', 'title', 'oppType', 'logo' ]
        });

        this.proxy = Ext.create('Ext.data.proxy.Ajax', {
            url: this.queryUrl,
            reader: {
                root: 'learningOpportunities',
                totalProperty: 'totalCount'
            }
        });

        this.gloStore = Ext.create('Ext.data.JsonStore', {
            model: 'GenericLearninOpportunityModel',
            pageSize: 20,
            proxy: this.proxy
        });

        this.gloStore.on( 'load', this.updateResults, this );

        this.pageBar = Ext.create('Ext.toolbar.Paging', {
            store: this.gloStore,
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

        this.renderTitle = function( value, metaData, lo ) {
            return '<table width="100%" border="0">'+
                      '<tr>'+
                          '<td>' + value + '</td>'+ 
                          '<td rowspan="2" align="right"><img style="border:1px solid; border-color:#cccccc; margin-right: 10px" height="60" src="' + lo.data.logo + '"></td>' +
                      '</tr>' +
                      '<tr>'+
                          '<td><font style="color:#15569B;"><i>' + lo.data.oppType + '</i></font></td>'+ 
                      '</tr>' +
                   '</table>';
        },

        cfg = {
            store: this.gloStore,
            columns: [ 
                { text: 'Id', width: 100,  dataIndex: 'id', hidden: true },
                { text: tr('Opportunities'), flex: 1, dataIndex: 'title', sortable: true, renderer: this.renderTitle }
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
    doQuery: function(query) { 
        this.currentQuery = query;
        this.proxy.url = this.queryUrl + '&q=' + encodeURIComponent(JSON.stringify(query)) + '&isFacetInfos=true&lang=' + this.lang;
        this.gloStore.loadPage(1);
    },
    updateResults: function() {
        this.proxy.url = this.queryUrl + '&q=' + encodeURIComponent(JSON.stringify(this.currentQuery)) + '&lang=' + this.lang;
        var isClear = this.proxy.reader.jsonData.isClear;
        this.updateResultInfos(isClear);
        var facetInfos = this.proxy.reader.jsonData.facetInfos;
        if (facetInfos != undefined)
            searchManager.updateFacets(facetInfos, isClear);
    },
    updateResultInfos: function(isClear) { 
        var label = '';
        if (isClear) {
            label = tr('Opportunities');
        }
        else {
            label = tr( 'No opportunity found' );
            var nbResults = this.gloStore.getTotalCount();

            var atLeastOneResult = true;
            if (nbResults == 1)
                label = '1 ' + tr( 'opportunity found' );
            else if (nbResults > 1)
                label = nbResults + ' ' + tr( 'opportunities found' );
            else
                atLeastOneResult = false;
        }
         
        this.columns[1].setText(tr(label));
    },    
    clear: function() {
        this.gloStore.loadRawData([]);
    }
} );


