Ext.define( 'Proeaf.ConcreteLearningOpportunityGrid', {
    extend: 'Ext.grid.Panel',
    initComponent: function (config) { 
        
        Ext.define('ConcreteLearninOpportunityModel', {
            extend: 'Ext.data.Model',
            fields: [ 'uri', 'start', 'end', 'deliveryMode', 'perfLanguage' ]
        });

        this.proxy = Ext.create('Ext.data.proxy.Ajax', {
            reader: {
                root: 'learningOpportunities'
            }
        });

        this.cloStore = Ext.create('Ext.data.JsonStore', {
            model: 'ConcreteLearninOpportunityModel',
            proxy: this.proxy
        });

        this.renderPerfLang = function( value ) {
            return tr(getlanguage( value ));
        };
       
        cfg = {
            store: this.cloStore,
            columns: [ 
                { text: 'uri', dataIndex: 'uri', hidden: true },
                { text: tr('From'), width: 110, sortable: true, xtype: 'datecolumn', dataIndex: 'start' },
                { text: tr('To'), width: 110, sortable: true, xtype: 'datecolumn', dataIndex: 'end' },
                { text: tr('Diffusion'), width: 110, sortable: true, dataIndex: 'deliveryMode' },
                { text: tr('Language'), flex: 1, sortable: true, dataIndex: 'perfLanguage', renderer: this.renderPerfLang }
            ],          
            viewConfig: {
                loadingText: tr('Search in progress') + '...',
                stripeRows: false
            }            
        };
        Ext.apply(this, cfg);
        this.callParent(arguments);   
    },
    init: function(glo) {
        this.proxy.url = 'rest/queryEngine/concreteLOs?glo=' + glo + '&lang=' + this.lang;
        this.cloStore.load();
    },
    clear: function() {        
        this.cloStore.loadRawData([]);
    }  
} );


